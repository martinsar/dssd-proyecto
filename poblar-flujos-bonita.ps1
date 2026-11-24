param(
    [string]$BaseUrl = "http://localhost:8082",
    [string]$ApiKey = "rescuesync-bonita-2026",
    [string]$Password = "bpm",
    [int]$SegundosVentana = 8,
    [int]$PausaEntreEtapasSeg = 1
)

$ErrorActionPreference = "Stop"

Write-Host "==============================================================" -ForegroundColor Cyan
Write-Host " Carga masiva de flujos reales RescueSync + Bonita" -ForegroundColor Cyan
Write-Host " Backend: $BaseUrl | Ventana de convocatoria: ${SegundosVentana}s" -ForegroundColor Cyan
Write-Host "==============================================================" -ForegroundColor Cyan

function Invoke-Api {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Uri,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )

    $params = @{
        Method      = $Method
        Uri         = $Uri
        Headers     = $Headers
        ContentType = "application/json; charset=utf-8"
    }

    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 20 -Compress
        $params["Body"] = [System.Text.Encoding]::UTF8.GetBytes($json)
    }

    try {
        return Invoke-RestMethod @params -ErrorAction Stop
    }
    catch {
        Write-Host "ERROR en $Method $Uri" -ForegroundColor Red
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            Write-Host $_.ErrorDetails.Message -ForegroundColor DarkRed
        }
        else {
            Write-Host $_.Exception.Message -ForegroundColor DarkRed
        }
        throw
    }
}

function Try-Login {
    param([string]$Username, [string]$Pass)
    try {
        $resp = Invoke-Api -Method Post -Uri "$BaseUrl/api/auth/login" -Body @{ username = $Username; password = $Pass }
        Write-Host "  [OK] Login $Username ($($resp.usuario.rol))" -ForegroundColor DarkGray
        return $resp
    }
    catch {
        Write-Host "  [WARN] No se pudo autenticar '$Username' en Bonita (se usará fallback si existe)." -ForegroundColor Yellow
        return $null
    }
}

function Format-LocalIso {
    param([datetime]$Dt)
    return $Dt.ToString("yyyy-MM-ddTHH:mm:ss")
}

# Verificar backend
try {
    Invoke-WebRequest -Uri "$BaseUrl/swagger-ui.html" -Method Get -TimeoutSec 10 -ErrorAction Stop | Out-Null
}
catch {
    Write-Host "El backend no responde en $BaseUrl. Levantalo antes de ejecutar este script." -ForegroundColor Red
    exit 1
}

Write-Host "`n[1/4] Autenticando actores en Bonita..." -ForegroundColor Green
$sesionesMunicipio = @{}
foreach ($u in @("olmos_municipio", "la_plata_municipio", "ensenada_municipio")) {
    $s = Try-Login -Username $u -Pass $Password
    if ($null -ne $s) { $sesionesMunicipio[$u] = $s }
}
if ($sesionesMunicipio.Count -eq 0) {
    throw "No se pudo autenticar ningún operador municipal en Bonita."
}

$sesionCoord = Try-Login -Username "agente_coordinador_1" -Pass $Password
if ($null -eq $sesionCoord) {
    throw "No se pudo autenticar al coordinador 'agente_coordinador_1' en Bonita."
}

$sesionesOng = @{}
foreach ($u in @("caritas_ong", "cruz_roja_ong", "bomberos_lp_ong", "medicos_mundo_ong", "defensa_civil_ong")) {
    $s = Try-Login -Username $u -Pass $Password
    if ($null -ne $s) { $sesionesOng[$u] = $s }
}
if ($sesionesOng.Count -eq 0) {
    throw "No se pudo autenticar ninguna ONG en Bonita."
}

$sesionAuditor = Try-Login -Username "auditor" -Pass $Password

function Get-Municipio {
    param([string]$Preferido)
    if ($sesionesMunicipio.ContainsKey($Preferido)) { return $sesionesMunicipio[$Preferido] }
    return ($sesionesMunicipio.Values | Select-Object -First 1)
}

function Get-Ong {
    param([string]$Preferida)
    if ($sesionesOng.ContainsKey($Preferida)) { return $sesionesOng[$Preferida] }
    return ($sesionesOng.Values | Select-Object -First 1)
}

# Catálogo de recursos
Write-Host "`n[2/4] Consultando tipos de recurso..." -ForegroundColor Green
$headersCoord = @{ Authorization = "Bearer $($sesionCoord.token)" }
$tipos = Invoke-Api -Method Get -Uri "$BaseUrl/api/tipos-recurso" -Headers $headersCoord
$recursosPorCodigo = @{}
foreach ($t in $tipos) {
    if ($t.codigo) { $recursosPorCodigo[$t.codigo] = [int]$t.id }
}
Write-Host "  Recursos disponibles: $($recursosPorCodigo.Keys -join ', ')" -ForegroundColor DarkGray

# Helper para correr un flujo completo o parcial
function Ejecutar-Escenario {
    param(
        [string]$Titulo,
        [string]$Operador,
        [string]$TipoDesastre,
        [string]$Gravedad,
        [string]$Zona,
        [string]$Descripcion,
        [string]$HastaEtapa, # EN_RELEVAMIENTO | BORRADOR | CONVOCATORIA_ABIERTA | ADJUDICADA | FINALIZADA
        [array]$Lotes,       # @{ codigo = "AGUA"; cantidad = 200; descripcion = "..." }
        [bool]$UsarConsorcio = $false,
        [string]$Ong1 = "caritas_ong",
        [string]$Ong2 = "cruz_roja_ong"
    )

    Write-Host "`n>>> Escenario: $Titulo (objetivo: $HastaEtapa)" -ForegroundColor Cyan
    $mun = Get-Municipio $Operador
    $hMun = @{ Authorization = "Bearer $($mun.token)" }

    # 1. Alta de emergencia (inicia caso en Bonita)
    $em = Invoke-Api -Method Post -Uri "$BaseUrl/api/emergencias" -Headers $hMun -Body @{
        tipoDesastre  = $TipoDesastre
        nivelGravedad = $Gravedad
        zonaAfectada  = $Zona
        descripcion   = $Descripcion
    }
    Write-Host "    Emergencia #$($em.id) creada (Bonita caseId=$($em.caseId))" -ForegroundColor Gray

    if ($HastaEtapa -eq "EN_RELEVAMIENTO") { return $em }
    Start-Sleep -Seconds $PausaEntreEtapasSeg

    # 2. Relevamiento (completa 'Desglosar Lotes' en Bonita)
    $necesidades = @()
    foreach ($l in $Lotes) {
        $necesidades += @{
            tipoRecursoId = $recursosPorCodigo[$l.codigo]
            cantidad      = [int]$l.cantidad
            descripcion   = $l.descripcion
        }
    }
    $conv = Invoke-Api -Method Post -Uri "$BaseUrl/api/emergencias/$($em.id)/convocatorias" -Headers $headersCoord -Body @{
        necesidades = $necesidades
    }
    Write-Host "    Convocatoria #$($conv.id) creada en BORRADOR" -ForegroundColor Gray

    if ($HastaEtapa -eq "BORRADOR") { return $em }
    Start-Sleep -Seconds $PausaEntreEtapasSeg

    # 3. Publicar convocatoria (completa 'Gestionar Convocatoria' en Bonita)
    $cierre = if ($HastaEtapa -eq "CONVOCATORIA_ABIERTA") {
        (Get-Date).AddHours(24)
    }
    else {
        (Get-Date).AddSeconds($SegundosVentana)
    }
    Invoke-Api -Method Post -Uri "$BaseUrl/api/convocatorias/$($conv.id)/publicar" -Headers $headersCoord -Body @{
        fechaCierre = Format-LocalIso $cierre
    } | Out-Null
    Write-Host "    Convocatoria #$($conv.id) PUBLICADA (cierre: $(Format-LocalIso $cierre))" -ForegroundColor Gray

    # 4. Cargar ofertas (individuales o consorcio)
    $o1 = Get-Ong $Ong1
    $o2 = Get-Ong $Ong2
    $hOng1 = @{ Authorization = "Bearer $($o1.token)" }
    $hOng2 = @{ Authorization = "Bearer $($o2.token)" }

    $ofertasCreadas = @()
    $consorcioId = $null
    if ($UsarConsorcio -and ($o1.usuario.organizacionId -ne $o2.usuario.organizacionId)) {
        $oc = Invoke-Api -Method Post -Uri "$BaseUrl/api/ong/ofertas-conjuntas" -Headers $hOng1 -Body @{
            convocatoriaId   = [int]$conv.id
            participantesIds = @([int]$o2.usuario.organizacionId)
        }
        $consorcioId = [int]$oc.id
        Write-Host "    Oferta conjunta #$consorcioId creada ($($o1.usuario.organizacionNombre) + $($o2.usuario.organizacionNombre))" -ForegroundColor Gray
    }

    for ($i = 0; $i -lt $Lotes.Count; $i++) {
        $l = $Lotes[$i]
        $usaSegunda = ($i % 2 -eq 1)
        $hActual = if ($usaSegunda) { $hOng2 } else { $hOng1 }
        $ongActual = if ($usaSegunda) { $o2 } else { $o1 }

        $bodyOferta = @{
            cantidad      = [int]$l.cantidad
            descripcion   = "Oferta $($ongActual.usuario.organizacionNombre) - $($l.descripcion)"
            tipoRecursoId = $recursosPorCodigo[$l.codigo]
            convocatoriaId = [int]$conv.id
        }
        if ($null -ne $consorcioId) {
            $bodyOferta["ofertaConjuntaId"] = $consorcioId
        }
        $of = Invoke-Api -Method Post -Uri "$BaseUrl/api/ong/ofertas" -Headers $hActual -Body $bodyOferta
        $ofertasCreadas += @{ id = [int]$of.id; headers = $hActual; habilitar = $true; seleccionar = $true }
    }

    # Si hay una segunda ONG disponible y no es consorcio, agregamos 1 oferta extra competitiva para enriquecer métricas
    if (-not $UsarConsorcio -and ($o1.usuario.organizacionId -ne $o2.usuario.organizacionId) -and $Lotes.Count -ge 1) {
        $l0 = $Lotes[0]
        $ofExtra = Invoke-Api -Method Post -Uri "$BaseUrl/api/ong/ofertas" -Headers $hOng2 -Body @{
            cantidad       = [int]([Math]::Max(1, [Math]::Floor($l0.cantidad / 2)))
            descripcion    = "Propuesta alternativa $($o2.usuario.organizacionNombre)"
            tipoRecursoId  = $recursosPorCodigo[$l0.codigo]
            convocatoriaId = [int]$conv.id
        }
        $ofertasCreadas += @{ id = [int]$ofExtra.id; headers = $hOng2; habilitar = $true; seleccionar = $false }
    }

    Write-Host "    $($ofertasCreadas.Count) oferta(s) cargada(s)" -ForegroundColor Gray
    if ($HastaEtapa -eq "CONVOCATORIA_ABIERTA") { return $em }

    # Esperar cierre real de la ventana de convocatoria
    $faltan = [int][Math]::Ceiling(($cierre - (Get-Date)).TotalSeconds) + 1
    if ($faltan -gt 0) {
        Write-Host "    Esperando ${faltan}s a que cierre la ventana..." -ForegroundColor DarkGray
        Start-Sleep -Seconds $faltan
    }

    # 5. Validación del Sistema Nacional
    $resultadosVal = @()
    foreach ($o in $ofertasCreadas) {
        $resultadosVal += @{ ofertaId = $o.id; habilitada = $o.habilitar }
    }
    Invoke-Api -Method Post -Uri "$BaseUrl/api/emergencias/$($em.id)/ofertas/validaciones" `
        -Headers @{ Authorization = "Bearer $($sesionCoord.token)"; "X-Api-Key" = $ApiKey } `
        -Body @{ resultados = $resultadosVal } | Out-Null
    Write-Host "    Validaciones registradas" -ForegroundColor Gray
    Start-Sleep -Seconds $PausaEntreEtapasSeg

    # 6. Selección / Adjudicación por el municipio (completa 'Seleccionar ofertas' en Bonita)
    $seleccionIds = @($ofertasCreadas | Where-Object { $_.seleccionar } | ForEach-Object { $_.id })
    $adj = Invoke-Api -Method Post -Uri "$BaseUrl/api/municipio/emergencias/$($em.id)/adjudicacion" `
        -Headers $hMun -Body @{ ofertaIds = $seleccionIds }
    Write-Host "    Adjudicación registrada (etapa=$($adj.etapa))" -ForegroundColor Gray

    # Confirmar notificación formal (idempotente: necesario si integracion-completa=true)
    Invoke-Api -Method Post -Uri "$BaseUrl/api/emergencias/$($em.id)/adjudicacion/notificaciones" `
        -Headers @{ Authorization = "Bearer $($sesionCoord.token)"; "X-Api-Key" = $ApiKey } | Out-Null

    if ($HastaEtapa -eq "ADJUDICADA") { return $em }
    Start-Sleep -Seconds $PausaEntreEtapasSeg

    # 7. Finalización de todos los lotes adjudicados (cierra proyecto y completa 'Finalizar actividades' en Bonita)
    foreach ($o in ($ofertasCreadas | Where-Object { $_.seleccionar })) {
        Invoke-Api -Method Post -Uri "$BaseUrl/api/ong/ofertas/$($o.id)/finalizacion" `
            -Headers $o.headers `
            -Body @{ informe = "Entrega verificada y operativa cerrada en terreno." } | Out-Null
    }
    Write-Host "    Emergencia #$($em.id) FINALIZADA" -ForegroundColor Green
    return $em
}

Write-Host "`n[3/4] Ejecutando 8 escenarios reales contra RescueSync + Bonita..." -ForegroundColor Green

# 4 casos FINALIZADOS (distintas gravedades, tipos y consorcios -> pueblan tiemposPorEtapa, tiemposResolucionCruzados, actividadUsuariosBonita y tiemposEsperaVsEjecucionBonita)
Ejecutar-Escenario -Titulo "Inundación Crítica con Consorcio" -Operador "la_plata_municipio" `
    -TipoDesastre "Inundación" -Gravedad "Crítica" -Zona "La Plata - Casco Norte" `
    -Descripcion "Crecida repentina con despliegue en consorcio (flujo Bonita)." `
    -HastaEtapa "FINALIZADA" -UsarConsorcio $true -Ong1 "caritas_ong" -Ong2 "cruz_roja_ong" `
    -Lotes @(
        @{ codigo = "ALIMENTOS"; cantidad = 300; descripcion = "Raciones de alimento" },
        @{ codigo = "AGUA"; cantidad = 600; descripcion = "Bidones de agua potable" }
    ) | Out-Null

Ejecutar-Escenario -Titulo "Incendio Forestal Alto" -Operador "ensenada_municipio" `
    -TipoDesastre "Incendio forestal" -Gravedad "Alta" -Zona "Ensenada - Reserva Costera" `
    -Descripcion "Foco de incendio forestal con respuesta sanitaria (flujo Bonita)." `
    -HastaEtapa "FINALIZADA" -UsarConsorcio $false -Ong1 "bomberos_lp_ong" -Ong2 "medicos_mundo_ong" `
    -Lotes @(
        @{ codigo = "BOMBEROS"; cantidad = 20; descripcion = "Dotaciones de bomberos" },
        @{ codigo = "MEDICAMENTOS"; cantidad = 120; descripcion = "Botiquines de primeros auxilios" }
    ) | Out-Null

Ejecutar-Escenario -Titulo "Tornado Media con Consorcio" -Operador "olmos_municipio" `
    -TipoDesastre "Tornado" -Gravedad "Media" -Zona "Lisandro Olmos" `
    -Descripcion "Voladura de techos y asistencia energética en consorcio (flujo Bonita)." `
    -HastaEtapa "FINALIZADA" -UsarConsorcio $true -Ong1 "defensa_civil_ong" -Ong2 "caritas_ong" `
    -Lotes @(
        @{ codigo = "GENERADORES"; cantidad = 8; descripcion = "Generadores portátiles" },
        @{ codigo = "FRAZADAS"; cantidad = 250; descripcion = "Frazadas de abrigo" }
    ) | Out-Null

Ejecutar-Escenario -Titulo "Sequía Baja" -Operador "olmos_municipio" `
    -TipoDesastre "Sequía" -Gravedad "Baja" -Zona "Abasto" `
    -Descripcion "Provisión preventiva de agua en puestos rurales (flujo Bonita)." `
    -HastaEtapa "FINALIZADA" -UsarConsorcio $false -Ong1 "cruz_roja_ong" -Ong2 "caritas_ong" `
    -Lotes @(
        @{ codigo = "AGUA"; cantidad = 450; descripcion = "Agua potable envasada" }
    ) | Out-Null

# 4 casos ACTIVOS en distintas etapas (-> pueblan cuellosDeBotellaBonita y casosActivos)
Ejecutar-Escenario -Titulo "Emergencia Adjudicada en curso" -Operador "la_plata_municipio" `
    -TipoDesastre "Inundación" -Gravedad "Alta" -Zona "La Plata - Villa Elvira" `
    -Descripcion "Operativo en ejecución pendiente de finalización de actividades (flujo Bonita)." `
    -HastaEtapa "ADJUDICADA" -UsarConsorcio $true -Ong1 "caritas_ong" -Ong2 "cruz_roja_ong" `
    -Lotes @(
        @{ codigo = "ALIMENTOS"; cantidad = 200; descripcion = "Módulos alimentarios" },
        @{ codigo = "AGUA"; cantidad = 400; descripcion = "Bidones de agua" }
    ) | Out-Null

Ejecutar-Escenario -Titulo "Convocatoria Abierta activa" -Operador "ensenada_municipio" `
    -TipoDesastre "Deslizamiento de tierra" -Gravedad "Media" -Zona "Ensenada - Dique" `
    -Descripcion "Convocatoria abierta recibiendo ofertas de ONGs (flujo Bonita)." `
    -HastaEtapa "CONVOCATORIA_ABIERTA" -UsarConsorcio $false -Ong1 "bomberos_lp_ong" -Ong2 "defensa_civil_ong" `
    -Lotes @(
        @{ codigo = "RESCATISTAS"; cantidad = 15; descripcion = "Rescatistas en talud" },
        @{ codigo = "CARPAS"; cantidad = 30; descripcion = "Carpas familiares" }
    ) | Out-Null

Ejecutar-Escenario -Titulo "Relevamiento en Borrador (pendiente Gestionar Convocatoria)" -Operador "la_plata_municipio" `
    -TipoDesastre "Tornado" -Gravedad "Alta" -Zona "La Plata - San Carlos" `
    -Descripcion "Relevamiento cargado pendiente de publicación formal (flujo Bonita)." `
    -HastaEtapa "BORRADOR" `
    -Lotes @(
        @{ codigo = "FRAZADAS"; cantidad = 150; descripcion = "Frazadas térmicas" },
        @{ codigo = "GENERADORES"; cantidad = 6; descripcion = "Generadores" }
    ) | Out-Null

Ejecutar-Escenario -Titulo "Emergencia recién registrada (pendiente Desglosar Lotes)" -Operador "olmos_municipio" `
    -TipoDesastre "Incendio forestal" -Gravedad "Crítica" -Zona "Etcheverry - Sector Rural" `
    -Descripcion "Alerta recién ingresada pendiente de relevamiento (flujo Bonita)." `
    -HastaEtapa "EN_RELEVAMIENTO" -Lotes @() | Out-Null

# 4. Resumen final desde el endpoint del auditor
Write-Host "`n[4/4] Consultando indicadores del auditor..." -ForegroundColor Green
if ($null -ne $sesionAuditor) {
    $ind = Invoke-Api -Method Get -Uri "$BaseUrl/api/auditor/indicadores" -Headers @{ Authorization = "Bearer $($sesionAuditor.token)" }
    Write-Host "  Totales (DSSD_DB): emergencias=$($ind.totales.emergencias), convocatorias=$($ind.totales.convocatoriasPublicadas), ofertasRecibidas=$($ind.totales.ofertasRecibidas), adjudicadas=$($ind.totales.ofertasAdjudicadas), suministros=$($ind.totales.suministrosMovilizados)" -ForegroundColor White
    Write-Host "  Bonita disponible=$($ind.tiemposPorEtapa.disponible): casosAnalizados=$($ind.tiemposPorEtapa.casosAnalizados), finalizados=$($ind.tiemposPorEtapa.casosFinalizadosEnBonita), activos=$($ind.estadosCasosBonita.casosActivos), pendientes=$($ind.cuellosDeBotellaBonita.totalTareasPendientes)" -ForegroundColor White
}
else {
    Write-Host "  (No se pudo consultar /api/auditor/indicadores porque el usuario 'auditor' no autenticó en Bonita)." -ForegroundColor Yellow
}

Write-Host "`nCarga masiva finalizada." -ForegroundColor Cyan
