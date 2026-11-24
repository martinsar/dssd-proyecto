$ErrorActionPreference = "Stop"

$BaseUrl = "http://localhost:8082"
$ApiKey = "rescuesync-bonita-2026"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " Flujo completo RescueSync" -ForegroundColor Cyan
Write-Host " Base: $BaseUrl" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

function Invoke-Api {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Uri,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )

    $params = @{
        Method = $Method
        Uri = $Uri
        Headers = $Headers
        ContentType = "application/json"
    }

    if ($null -ne $Body) {
        $params["Body"] = ($Body | ConvertTo-Json -Depth 20 -Compress)
    }

    try {
        return Invoke-RestMethod @params -ErrorAction Stop
    }
    catch {
        Write-Host "ERROR en llamada: $Method $Uri" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        throw
    }
}

function Login {
    param(
        [Parameter(Mandatory = $true)][string]$Username,
        [Parameter(Mandatory = $true)][string]$Password
    )

    $body = @{ username = $Username; password = $Password }
    return Invoke-Api -Method Post -Uri "$BaseUrl/api/auth/login" -Body $body
}

function Test-Backend {
    try {
        Invoke-WebRequest -Uri "$BaseUrl/swagger-ui.html" -Method Get -TimeoutSec 10 -ErrorAction Stop | Out-Null
        return $true
    }
    catch {
        Write-Host "El backend no responde en $BaseUrl" -ForegroundColor Red
        Write-Host "Verificá que la app esté levantada en puerto 8083." -ForegroundColor Yellow
        return $false
    }
}

if (-not (Test-Backend)) {
    exit 1
}

Write-Host "[1/8] Login operador municipal" -ForegroundColor Green
$municipioLogin = Login -Username "olmos" -Password "bpm"
$tokenMunicipio = $municipioLogin.token
Write-Host "Token operador: $tokenMunicipio" -ForegroundColor Gray

Write-Host "[2/8] Crear emergencia" -ForegroundColor Green
$emergenciaBody = @{
    tipoDesastre = "Inundación"
    nivelGravedad = "ALTA"
    zonaAfectada = "La Plata"
    descripcion = "Crecida del canal principal con evacuación parcial."
}
$emergencia = Invoke-Api -Method Post -Uri "$BaseUrl/api/emergencias" -Headers @{ Authorization = "Bearer $tokenMunicipio" } -Body $emergenciaBody
$emergenciaId = $emergencia.id
Write-Host "Emergencia creada: ID $emergenciaId" -ForegroundColor Gray

Write-Host "[3/8] Login coordinador" -ForegroundColor Green
$coordinadorLogin = Login -Username "agente_coordinador_1" -Password "bpm"
$tokenCoordinador = $coordinadorLogin.token
Write-Host "Token coordinador: $tokenCoordinador" -ForegroundColor Gray

Write-Host "[4/8] Obtener tipos de recurso" -ForegroundColor Green
$tipos = Invoke-Api -Method Get -Uri "$BaseUrl/api/tipos-recurso" -Headers @{ Authorization = "Bearer $tokenCoordinador" }
$tipoAlimentos = $tipos | Where-Object { $_.codigo -eq "ALIMENTOS" } | Select-Object -First 1
$tipoAgua = $tipos | Where-Object { $_.codigo -eq "AGUA" } | Select-Object -First 1

if ($null -eq $tipoAlimentos) { $tipoAlimentos = $tipos[0] }
if ($null -eq $tipoAgua) { $tipoAgua = $tipos[1] }

Write-Host "Tipo ALIMENTOS: $($tipoAlimentos.id) / $($tipoAlimentos.codigo)" -ForegroundColor Gray
Write-Host "Tipo AGUA: $($tipoAgua.id) / $($tipoAgua.codigo)" -ForegroundColor Gray

Write-Host "[5/8] Crear convocatoria (relevamiento)" -ForegroundColor Green
$convocatoriaBody = @{
    necesidades = @(
        @{ tipoRecursoId = [int]$tipoAlimentos.id; cantidad = 100; descripcion = "Raciones de alimento" },
        @{ tipoRecursoId = [int]$tipoAgua.id; cantidad = 200; descripcion = "Bidones de agua" }
    )
}
$convocatoria = Invoke-Api -Method Post -Uri "$BaseUrl/api/emergencias/$emergenciaId/convocatorias" -Headers @{ Authorization = "Bearer $tokenCoordinador" } -Body $convocatoriaBody
$convocatoriaId = $convocatoria.id
Write-Host "Convocatoria creada: ID $convocatoriaId" -ForegroundColor Gray

Write-Host "[6/8] Login ONG" -ForegroundColor Green
$ongLogin = Login -Username "caritas" -Password "bpm"
$tokenOng = $ongLogin.token
Write-Host "Token ONG: $tokenOng" -ForegroundColor Gray

Write-Host "[7/8] Crear oferta" -ForegroundColor Green
$ofertaBody = @{
    cantidad = 40
    descripcion = "Donación de alimentos y frazadas"
    tipoRecursoId = [int]$tipoAlimentos.id
    convocatoriaId = [int]$convocatoriaId
}
$oferta = Invoke-Api -Method Post -Uri "$BaseUrl/api/ong/ofertas" -Headers @{ Authorization = "Bearer $tokenOng" } -Body $ofertaBody
$ofertaId = $oferta.id
Write-Host "Oferta creada: ID $ofertaId" -ForegroundColor Gray

Write-Host "[8/8] Validar, adjudicar y finalizar" -ForegroundColor Green
$validacionBody = @{
    resultados = @(
        @{ ofertaId = [int]$ofertaId; habilitada = $true }
    )
}
$validacion = Invoke-Api -Method Post -Uri "$BaseUrl/api/emergencias/$emergenciaId/ofertas/validaciones" -Headers @{ Authorization = "Bearer $tokenCoordinador"; "X-Api-Key" = $ApiKey } -Body $validacionBody
Write-Host "Validación registrada: $($validacion | ConvertTo-Json -Depth 10 -Compress)" -ForegroundColor Gray

$municipioLogin2 = Login -Username "olmos" -Password "bpm"
$tokenMunicipio2 = $municipioLogin2.token
$adjudicacionBody = @{ ofertaIds = @([int]$ofertaId) }
$adjudicacion = Invoke-Api -Method Post -Uri "$BaseUrl/api/municipio/emergencias/$emergenciaId/adjudicacion" -Headers @{ Authorization = "Bearer $tokenMunicipio2" } -Body $adjudicacionBody
Write-Host "Adjudicación: $($adjudicacion | ConvertTo-Json -Depth 10 -Compress)" -ForegroundColor Gray

$confirmacion = Invoke-Api -Method Post -Uri "$BaseUrl/api/emergencias/$emergenciaId/adjudicacion/notificaciones" -Headers @{ Authorization = "Bearer $tokenCoordinador"; "X-Api-Key" = $ApiKey }
Write-Host "Confirmación Bonita: $($confirmacion | ConvertTo-Json -Depth 10 -Compress)" -ForegroundColor Gray

$finalizacionBody = @{ informe = "Entrega finalizada y comprobada." }
$finalizacion = Invoke-Api -Method Post -Uri "$BaseUrl/api/ong/ofertas/$ofertaId/finalizacion" -Headers @{ Authorization = "Bearer $tokenOng" } -Body $finalizacionBody
Write-Host "Finalización: $($finalizacion | ConvertTo-Json -Depth 10 -Compress)" -ForegroundColor Gray

Write-Host "" 
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " Flujo completo ejecutado correctamente" -ForegroundColor Cyan
Write-Host " Resumen:" -ForegroundColor Cyan
Write-Host " - Emergencia: $emergenciaId" -ForegroundColor Cyan
Write-Host " - Convocatoria: $convocatoriaId" -ForegroundColor Cyan
Write-Host " - Oferta: $ofertaId" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "" 
Write-Host "Validaciones extras que podés hacer:" -ForegroundColor Yellow
Write-Host "  1) GET $BaseUrl/api/auth/sesion con el token del usuario" -ForegroundColor Yellow
Write-Host "  2) GET $BaseUrl/api/ong/ofertas" -ForegroundColor Yellow
Write-Host "  3) GET $BaseUrl/api/municipio/emergencias/$emergenciaId" -ForegroundColor Yellow
Write-Host "  4) GET $BaseUrl/api/coordinador/emergencias/$emergenciaId" -ForegroundColor Yellow
Write-Host "  5) GET $BaseUrl/api/ong/notificaciones" -ForegroundColor Yellow
