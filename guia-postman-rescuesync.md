# Guía de pruebas del flujo completo de RescueSync en Postman

## 1) Requisitos

- El backend debe estar levantado en `http://localhost:8083`.
- Si no está levantado, ejecutá:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.3'
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
$env:SERVER_PORT='8083'
Set-Location 'C:\Users\User\eclipse-workspace-sist-dist\dssd'
.\mvnw.cmd spring-boot:run -DskipTests
```

- La base de datos debe estar corriendo en PostgreSQL local con la base `dssd_db`.

## 2) Usuarios de prueba por defecto

- Municipio: `olmos` / `bpm`
- Coordinador: `agente_coordinador_1` / `bpm`
- ONG: `caritas` / `bpm`
- Segunda ONG: `cruz_roja` / `bpm`
- Auditor: `auditor` / `bpm`

## 3) Clave de integración Bonita

```text
rescuesync-bonita-2026
```

Se usa como header:

```http
X-Api-Key: rescuesync-bonita-2026
```

## 4) Variables de entorno recomendadas en Postman

Crear un environment con estas variables:

- `baseUrl` = `http://localhost:8083`
- `tokenMunicipio` = `""`
- `tokenCoordinador` = `""`
- `tokenOng` = `""`
- `tokenMunicipio2` = `""`
- `emergenciaId` = `1`
- `convocatoriaId` = `1`
- `ofertaId` = `1`

## 5) Flujo completo paso a paso

### Paso 1: Login del operador municipal

POST `{{baseUrl}}/api/auth/login`

Body:

```json
{
  "username": "olmos",
  "password": "bpm"
}
```

Guardar el `token` devuelto en la variable `tokenMunicipio`.

---

### Paso 2: Crear emergencia

POST `{{baseUrl}}/api/emergencias`

Headers:

```http
Authorization: Bearer {{tokenMunicipio}}
Content-Type: application/json
```

Body:

```json
{
  "tipoDesastre": "Inundación",
  "nivelGravedad": "ALTA",
  "zonaAfectada": "La Plata",
  "descripcion": "Crecida del canal principal con evacuación parcial."
}
```

Guardar el `id` que devuelve en `emergenciaId`.

---

### Paso 3: Login del coordinador

POST `{{baseUrl}}/api/auth/login`

Body:

```json
{
  "username": "agente_coordinador_1",
  "password": "bpm"
}
```

Guardar el `token` en `tokenCoordinador`.

---

### Paso 4: Listar tipos de recurso

GET `{{baseUrl}}/api/tipos-recurso`

Headers:

```http
Authorization: Bearer {{tokenCoordinador}}
```

Usar algunos IDs reales para la convocatoria. Normalmente funciona con `ALIMENTOS` y `AGUA`.

---

### Paso 5: Crear convocatoria (relevamiento)

POST `{{baseUrl}}/api/emergencias/{{emergenciaId}}/convocatorias`

Headers:

```http
Authorization: Bearer {{tokenCoordinador}}
Content-Type: application/json
```

Body ejemplo:

```json
{
  "necesidades": [
    {
      "tipoRecursoId": 1,
      "cantidad": 100,
      "descripcion": "Raciones de alimento"
    },
    {
      "tipoRecursoId": 2,
      "cantidad": 200,
      "descripcion": "Bidones de agua"
    }
  ]
}
```

Guardar el `id` de la convocatoria en `convocatoriaId`.

---

### Paso 6: Login ONG

POST `{{baseUrl}}/api/auth/login`

Body:

```json
{
  "username": "caritas",
  "password": "bpm"
}
```

Guardar el `token` en `tokenOng`.

---

### Paso 7: Crear oferta de la ONG

POST `{{baseUrl}}/api/ong/ofertas`

Headers:

```http
Authorization: Bearer {{tokenOng}}
Content-Type: application/json
```

Body:

```json
{
  "cantidad": 40,
  "descripcion": "Donación de alimentos y frazadas",
  "tipoRecursoId": 1,
  "convocatoriaId": {{convocatoriaId}}
}
```

Guardar el `id` de la oferta en `ofertaId`.

---

### Paso 8: Bonita registra validación de la oferta

POST `{{baseUrl}}/api/emergencias/{{emergenciaId}}/ofertas/validaciones`

Headers:

```http
Authorization: Bearer {{tokenCoordinador}}
X-Api-Key: rescuesync-bonita-2026
Content-Type: application/json
```

Body:

```json
{
  "resultados": [
    {
      "ofertaId": {{ofertaId}},
      "habilitada": true
    }
  ]
}
```

---

### Paso 9: Login del municipio nuevamente

POST `{{baseUrl}}/api/auth/login`

Body:

```json
{
  "username": "olmos",
  "password": "bpm"
}
```

Guardar el token en `tokenMunicipio2`.

---

### Paso 10: Municipio selecciona ofertas

POST `{{baseUrl}}/api/municipio/emergencias/{{emergenciaId}}/adjudicacion`

Headers:

```http
Authorization: Bearer {{tokenMunicipio2}}
Content-Type: application/json
```

Body:

```json
{
  "ofertaIds": [{{ofertaId}}]
}
```

---

### Paso 11: Bonita confirma adjudicación

POST `{{baseUrl}}/api/emergencias/{{emergenciaId}}/adjudicacion/notificaciones`

Headers:

```http
Authorization: Bearer {{tokenCoordinador}}
X-Api-Key: rescuesync-bonita-2026
```

---

### Paso 12: ONG finaliza la actividad del lote

POST `{{baseUrl}}/api/ong/ofertas/{{ofertaId}}/finalizacion`

Headers:

```http
Authorization: Bearer {{tokenOng}}
Content-Type: application/json
```

Body:

```json
{
  "informe": "Entrega finalizada y comprobada."
}
```

---

## 6) Consultas útiles para verificar resultados

### Ver sesión actual

GET `{{baseUrl}}/api/auth/sesion`

Headers:

```http
Authorization: Bearer {{tokenMunicipio}}
```

### Ver mis emergencias

GET `{{baseUrl}}/api/municipio/emergencias`

Headers:

```http
Authorization: Bearer {{tokenMunicipio}}
```

### Ver ofertas de la ONG

GET `{{baseUrl}}/api/ong/ofertas`

Headers:

```http
Authorization: Bearer {{tokenOng}}
```

### Ver detalle de la emergencia

GET `{{baseUrl}}/api/municipio/emergencias/{{emergenciaId}}`

Headers:

```http
Authorization: Bearer {{tokenMunicipio}}
```

### Ver seguimiento de la emergencia como coordinador

GET `{{baseUrl}}/api/coordinador/emergencias/{{emergenciaId}}`

Headers:

```http
Authorization: Bearer {{tokenCoordinador}}
```

## 7) Criterio de éxito

El flujo está bien si:

- Se crea la emergencia.
- Se crea la convocatoria.
- La ONG puede cargar la oferta.
- Se validan las ofertas por Bonita.
- El municipio selecciona correctamente.
- Bonita confirma la adjudicación.
- La ONG puede finalizar la actividad.
- Las consultas muestran el progreso del flujo.

## 8) Errores comunes

- Puerto incorrecto: usar `8083`.
- Sin `X-Api-Key` en endpoints de integración.
- Token vencido o incorrecto.
- `convocatoriaId` o `ofertaId` todavía no cargados.
- Intentar publicar/validar con usuario no autorizado.

## 9) Comentario final

Con este flujo podés probar la lógica de negocio desde Postman sin depender de un script externo. La colección y el environment ya están preparados para importar.

