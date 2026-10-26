# Informe de Calidad de Software — SistemaDeVentas

## 1. Información general

| Campo | Valor |
|---|---|
| Proyecto | SistemaDeVentas (Proyecto 2 — Curso de Calidad de Software) |
| Repositorio | GitHub — `https://github.com/JPDevLab/SistemaDeVentas` (espejo en Azure DevOps) |
| Commit base analizado | Rama `master` (último commit de entrega) |
| Cambios de la entrega | API REST, frontend estático, pruebas y ajustes de calidad |
| Fecha del análisis | 2026-10-25 |
| Integrantes | _[completar nombres y códigos]_ |
| Servidor de análisis | SonarQube Community 26.8.0 (`http://localhost:9000`) |
| Proyecto en SonarQube | `SistemaDeVentas` |
| Período de referencia (leak) | `PREVIOUS_VERSION` (2026-08-27) |

### Stack técnico

| Componente | Versión |
|---|---|
| Java | 21 (Eclipse Temurin 21.0.6) |
| Gradle (wrapper) | 8.10.2 |
| Spring Boot | 3.4.1 |
| Persistencia | Spring Data JPA + PostgreSQL 16 (runtime) / H2 (pruebas) |
| Validación | Jakarta Bean Validation |
| Pruebas | JUnit 5, Mockito, MockMvc, Cucumber 7.18.0 |
| Cobertura | JaCoCo |
| Mutaciones | PITest 1.19.0 (`pitest-junit5-plugin` 1.2.1) |
| Análisis estático | SonarQube + plugin SonarQube Gradle 6.0.1.5171 |

---

## 2. Objetivo y alcance

Evaluar y mejorar la calidad del sistema de ventas mediante:

1. Implementación de la API REST del dominio (productos, inventario y pedidos).
2. Pruebas automatizadas unitarias, de integración y de aceptación (Cucumber).
3. Medición de cobertura con JaCoCo y de eficacia de las pruebas con PITest.
4. Análisis estático con SonarQube y cumplimiento del Quality Gate configurado.

Alcance del análisis: código de producción en `src/main/java/org/example`, pruebas en `src/test`, configuración de build (`build.gradle.kts`) y recursos.

---

## 3. Funcionalidad implementada

### 3.1 Arquitectura

```
controller -> service (interfaz) -> service.impl (@Transactional)
            -> repository (Spring Data JPA) -> model (dominio + reglas)
            -> dto request/response (records) + mapper
            -> exception + GlobalExceptionHandler (respuestas {status, error, message})
```

Puntos relevantes:

- Reglas de negocio en el dominio (`Producto`, `Pedido`, `MovimientoInventario`) con excepciones anidadas.
- Bloqueo pesimista `@Lock(PESSIMISTIC_WRITE)` en `ProductoRepository.findByIdForUpdate` para compras concurrentes.
- Descuentos por volumen calculados en `Pedido` (0 %, 5 %, 10 %, 15 % según el total).
- Zona horaria `America/Bogota` para fechas de pedidos y movimientos.
- Concurrencia optimista con `@Version`.

### 3.2 Endpoints REST

| Método | Ruta | Descripción | Código esperado |
|---|---|---|---|
| POST | `/productos` | Crear producto | 201 / 400 |
| GET | `/productos` | Listar productos | 200 |
| GET | `/productos/{id}` | Obtener producto | 200 / 404 |
| PUT | `/productos/{id}` | Actualizar producto | 200 / 400 / 404 |
| POST | `/inventario/entradas` | Registrar entrada de inventario | 201 / 400 / 404 |
| GET | `/inventario/{productoId}` | Consultar stock | 200 / 404 |
| GET | `/inventario/movimientos[?productoId=]` | Listar movimientos | 200 |
| POST | `/pedidos` | Crear pedido | 201 / 400 / 404 / 409 |
| GET | `/pedidos[?cliente=]` | Listar pedidos (filtro por cliente) | 200 |
| GET | `/pedidos/{id}` | Obtener pedido | 200 / 404 |
| PATCH | `/pedidos/{id}/cancelar` | Cancelar pedido (restaura stock) | 200 / 404 / 409 |
| PATCH | `/pedidos/{id}/entregar` | Entregar pedido | 200 / 404 / 409 |

### 3.3 Frontend estático

Se incluyó una interfaz web servida por el propio Spring Boot desde `src/main/resources/static`, sin build de Node ni dependencias externas:

- SPA con enrutamiento por hash (`#/dashboard`, `#/productos`, `#/inventario`, `#/pedidos`) escrita en JavaScript modular (ESM).
- Consumo de la API con rutas relativas (mismo origen, sin CORS) y manejo uniforme de errores `{status, error, message}`.
- Vistas de dashboard, catálogo de productos, inventario (entradas y movimientos) y pedidos (carrito, descuentos en vivo, detalle, cancelar y entregar).
- Formato de moneda COP y fechas sin conversión de zona horaria; descuentos replicados en el cliente con aritmética en centavos solo para previsualización.
- Diseño responsivo con variables CSS, modales nativos (`<dialog>`), toasts y estados vacíos/carga.

Para evitar que el código de presentación afecte las métricas del backend, `build.gradle.kts` excluye `src/main/resources/static/**` del análisis de SonarQube (`sonar.exclusions`).

---

## 4. Estrategia de pruebas

| Clase de prueba | Tipo | Casos |
|---|---|---|
| `org.example.model.ProductoTest` | Unitaria (dominio) | 10 |
| `org.example.model.PedidoTest` | Unitaria (dominio) | 2 |
| `org.example.model.MovimientoInventarioTest` | Unitaria (dominio) | 6 |
| `org.example.service.impl.ProductoServiceImplTest` | Unitaria (Mockito) | 9 |
| `org.example.service.impl.InventarioServiceImplTest` | Unitaria (Mockito) | 7 |
| `org.example.service.impl.PedidoServiceImplTest` | Unitaria (Mockito) | 13 |
| `org.example.ApiIntegrationTest` | Integración (MockMvc + H2) | 11 |
| `org.example.PedidoConcurrencyIntegrationTest` | Integración concurrente (H2) | 1 |
| `Cucumber` (`pedido.feature` + `steps`) | Aceptación (BDD) | 2 |
| **Total** | | **61** |

Detalles de la estrategia:

- Las pruebas de integración usan el perfil `test` con H2 en memoria (`src/test/resources/application-test.properties`, `ddl-auto=create-drop`), evitando dependencia de PostgreSQL.
- `PedidoConcurrencyIntegrationTest` ejecuta 20 compras concurrentes sobre 10 unidades y verifica 10 éxitos y 10 rechazos por stock, con stock final 0.
- Se validaron manualmente contra la API real (PostgreSQL + puerto 8081) los flujos, fronteras de descuento y errores 400/404/409.

---

## 5. Resultados

### 5.1 Ejecución de pruebas

```text
.\gradlew.bat clean test
BUILD SUCCESSFUL
Tests: 61 | Failures: 0 | Errors: 0 | Skipped: 0
```

### 5.2 Cobertura JaCoCo

| Métrica | Valor |
|---|---|
| Cobertura de líneas (main) | **88.1 %** (392 / 445) |
| Cobertura de ramas | **72.7 %** |
| Cobertura combinada (SonarQube) | **84.6 %** |
| Líneas sin cubrir | 53 |

Clases principales con cobertura parcial:

| Clase | Cobertura de líneas |
|---|---|
| `exception.GlobalExceptionHandler` | 32.4 % |
| `Main` | 33.3 % |
| `controller.InventarioController` | 70.0 % |
| `mapper.ProductoMapper` | 80.6 % |
| `model.Pedido` | 87.7 % |
| `controller.ProductoController` | 87.5 % |
| `controller.PedidoController` | 88.9 % |
| `mapper.MovimientoInventarioMapper` | 88.9 % |
| `mapper.PedidoMapper` | 95.0 % |
| `service.impl.PedidoServiceImpl` | 95.8 % |
| `service.impl.ProductoServiceImpl` | 96.0 % |
| `model.Producto` | 97.8 % |

El porcentaje más bajo corresponde a `GlobalExceptionHandler` porque varias ramas defensivas no se alcanzan desde las pruebas de integración; no afecta el Quality Gate.

### 5.3 Pruebas de mutación (PITest)

```text
.\gradlew.bat pitest
>> Generated 183 mutations Killed 127 (69%)
>> Mutations with no coverage 38
>> Test strength 88%
>> Line Coverage (for mutated classes only): 295/394 (75%)
```

| Métrica | Valor |
|---|---|
| Mutaciones generadas | 183 |
| Mutaciones eliminadas | 127 (69 %) |
| Mutaciones sobrevivientes con cobertura | 18 |
| Mutaciones sin cobertura | 38 |
| Fuerza de pruebas (test strength) | **88 %** |
| Cobertura de líneas de clases mutadas | 75 % |

Interpretación: la fuerza de pruebas del 88 % indica que, en el código efectivamente cubierto, las pruebas detectan la mayoría de mutaciones. Las 38 mutaciones sin cobertura se concentran en ramas defensivas y mapeos/constructores no ejercitados.

### 5.4 Métricas de SonarQube

| Métrica | Valor |
|---|---|
| Líneas de código (ncloc) | 1176 |
| Líneas totales | 1556 |
| Clases | 49 |
| Funciones | 143 |
| Complejidad ciclomática | 198 |
| Complejidad cognitiva | 75 |
| Bugs | **0** |
| Vulnerabilidades | 1 |
| Code smells | **0** |
| Cobertura | **84.6 %** |
| Líneas duplicadas | 0.0 % |
| Deuda técnica | 0 min (0.0 %) |
| Reliability rating | A (1.0) |
| Security rating | B (2.0) |
| Maintainability rating | A (1.0) |
| Security hotspots | 0 |
| **Quality Gate** | **OK** |

### 5.5 Quality Gate

| Condición | Umbral | Valor | Estado |
|---|---|---|---|
| `new_coverage` | ≥ 80 % | 84.6 % | OK |
| `new_duplicated_lines_density` | ≤ 3 % | 0.0 % | OK |
| `new_violations` | ≤ 0 | 0 | OK |

Resultado global: **PASSED (OK)**.

### 5.6 Hallazgos abiertos

| Regla | Tipo | Severidad | Ubicación | Observación |
|---|---|---|---|---|
| `kotlin:S6474` | Vulnerabilidad | Minor | `SistemaDeVentas` | Dependencias no verificadas: falta `gradle/verification-metadata.xml`. Es un hallazgo heredado (anterior a esta entrega) y no forma parte del código nuevo; por eso no afecta el Quality Gate. Se recomienda adoptar verificación de dependencias en una iteración posterior. |

No quedan bugs, code smells ni vulnerabilidades de código nuevo. El hallazgo `secrets:S6702` (token de SonarQube embebido) fue corregido y cerrado en el código: el token se eliminó del repositorio y ahora se inyecta mediante la variable de entorno `SONAR_TOKEN`.

Además, como el token había quedado expuesto en el historial de Git (commit `ed58ac2`, ya publicado en Azure), se aplicó la remediación de seguridad correspondiente:

- Se generó un token nuevo y se validó su funcionamiento.
- Se revocó el token antiguo; las peticiones con el token revocado son rechazadas.
- El token nuevo se almacena fuera del repositorio (variable de entorno / secreto del pipeline).
- El token revocado permanece en el historial, pero al no ser válido ya no representa un riesgo. No se reescribió la historia para evitar un `force-push` sobre un repositorio compartido.

---

## 6. Acciones correctivas aplicadas

| Regla | Hallazgo | Corrección |
|---|---|---|
| `java:S2583` | Condición siempre falsa en `ProductoMapper` (stock nulo) | Se eliminó el ternario; `stock` es obligatorio por `@NotNull` |
| `java:S2589` | Comprobaciones de nulo/blank que siempre eran verdaderas (precio, stock, cantidad) | Se eliminaron en `ProductoMapper`, `ProductoServiceImpl`, `InventarioServiceImpl` y `PedidoServiceImpl`, apoyándose en Bean Validation |
| `java:S1128` | Import no usado (`LocalDateTime`) | Eliminado en `InventarioServiceImpl` |
| `java:S1068` | Campo privado no usado (`ZONA_HORARIA`) | Eliminado en `InventarioServiceImpl` |
| `java:S6204` | `collect(Collectors.toList())` | Reemplazado por `Stream.toList()` |
| `java:S5778` | Lambdas de `assertThrows` con más de una invocación | Se extrajo la creación del request fuera de la lambda |
| `java:S2187` | Runner de Cucumber sin pruebas detectadas por el analizador | Se documentó con `@SuppressWarnings("java:S2187")` |
| `kotlin:S6624` | Versiones codificadas en el build | Launcher gestionado por el BOM; versión de Cucumber en variable `cucumberVersion` |
| `kotlin:S6629` | Dependencias desagrupadas por configuración | Se agruparon por `implementation`, `runtimeOnly`, `testImplementation`, `testRuntimeOnly`, `pitest` |
| `secrets:S6702` | Token de SonarQube en `build.gradle.kts` | Token eliminado; se lee de `SONAR_TOKEN` (entorno) o `sonarToken` (propiedad de Gradle) |

Además:

- `tasks.sonar` ahora depende de `test` y `jacocoTestReport`, garantizando que el XML de cobertura exista antes del análisis.
- La tarea `acceptanceTest` se limitó al runner de Cucumber.
- Se retiró el BOM `junit-bom:6.0.0` que provocaba incompatibilidad con `pitest-junit5-plugin`; el launcher queda resuelto por el BOM de Spring Boot (JUnit 5.11.4), restaurando PITest.
- Se parametrizaron el puerto (`SERVER_PORT`, por defecto 8080) y el log SQL (`JPA_SHOW_SQL`, por defecto `false`) en `application.properties`.
- Se documentó la ejecución del proyecto, las pruebas y el análisis en `README.md`.

### 6.1 Integración continua (Azure Pipelines)

El pipeline `azure-pipeline.yml` presentaba dos problemas que impedían su funcionamiento:

| Problema | Corrección |
|---|---|
| Se disparaba en la rama `main`, que no existe en el repositorio | Trigger cambiado a `master` |
| Usaba la tarea obsoleta `sonarqube` | Reemplazada por `sonar` |
| El paso de Sonar no recibía credenciales | Se inyecta `SONAR_TOKEN` como variable secreta y el paso se habilita con `sonarEnabled=true` |
| No se conservaban evidencias de cobertura/mutación | Se publican los reportes JaCoCo y PITest como artefactos |

Secuencia ejecutada por el pipeline: `clean build` (compilación + pruebas) → `acceptanceTest` (Cucumber) → `pitest` → publicación de artefactos → `sonar`.

---

## 7. Evidencias de ejecución

```powershell
# 1. Iniciar SonarQube (instalación local)
C:\sonarqube-26.8.0.126808\bin\windows-x86-64\StartSonar.bat

# 2. Pruebas + cobertura + mutaciones
.\gradlew.bat clean test jacocoTestReport
.\gradlew.bat pitest

# 3. Análisis estático (token fuera del repositorio)
$env:SONAR_TOKEN = "<token>"
.\gradlew.bat sonar
```

Resultados verificados vía API de SonarQube:

- `/api/qualitygates/project_status?projectKey=SistemaDeVentas` → `status: OK`, `caycStatus: compliant`.
- `/api/measures/component?component=SistemaDeVentas` → `bugs=0`, `code_smells=0`, `vulnerabilities=1`, `coverage=84.6`, `line_coverage=88.1`, `branch_coverage=72.7`.
- `/api/issues/search?...&statuses=OPEN,CONFIRMED,REOPENED` → 1 issue abierto (`kotlin:S6474`).

---

## 8. Riesgos y limitaciones

1. **Frontend sin pruebas automatizadas**: la interfaz se verificó manualmente (smoke tests de rutas y flujos); no se agregó Playwright ni otro framework E2E para mantener el pipeline sin dependencias de Node.
2. **`kotlin:S6474`**: se recomienda generar `gradle/verification-metadata.xml` (`gradlew --write-verification-metadata sha256 help`) y validar el build con verificación estricta; no se aplicó en esta iteración por el riesgo de romper la resolución de dependencias del entorno de evaluación.
3. **Seguridad del token**: el token antiguo ya fue revocado y el nuevo se almacena fuera del repositorio. Falta registrarlo como variable secreta `SONAR_TOKEN` en Azure Pipelines para que el paso de Sonar se ejecute en CI. Conviene rotarlo periódicamente.
4. **Historial de Git**: el token revocado sigue presente en commits antiguos. Si la política del curso exige eliminar secretos del historial, se debe reescribir con `git filter-repo`/BFG y coordinar un `force-push`; no se recomienda por tratarse de un repositorio compartido.
5. **Cobertura de `GlobalExceptionHandler`**: sus ramas defensivas podrían cubrirse con pruebas MockMvc adicionales de errores de validación y excepciones inesperadas.
6. **PITest**: quedan 38 mutaciones sin cobertura en ramas defensivas/mapeos; incrementar pruebas de mappers y constructores permitiría subir la fuerza de pruebas por encima del 90 %.

---

## 9. Conclusiones

- La API REST cumple los flujos funcionales del enunciado y las reglas de negocio, incluida la consistencia de inventario bajo concurrencia.
- Las 61 pruebas automatizadas pasan sin fallos y cubren el 88.1 % de las líneas y el 72.7 % de las ramas del código principal.
- PITest confirma una fuerza de pruebas del 88 % sobre el código cubierto.
- SonarQube reporta **Quality Gate OK**, con 0 bugs y 0 code smells y 84.6 % de cobertura; el único hallazgo abierto es una vulnerabilidad menor heredada sobre verificación de dependencias.
- El token de SonarQube fue revocado y sustituido; el pipeline de Azure quedó corregido para dispararse en `master`, ejecutar las pruebas de mutación y publicar evidencias.
- La entrega queda versionada en la rama `master` (API, frontend, pruebas y pipeline); resta registrar `SONAR_TOKEN` en Azure y completar la información de los integrantes.

---

## Anexo A. Cómo ejecutar la aplicación

```powershell
# Base de datos (si no existe un PostgreSQL local)
docker run -d --name sistema-ventas-db -p 5432:5432 `
  -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=sistema_ventas postgres:16

# Aplicación (8080 por defecto; configurable por variable de entorno)
.\gradlew.bat bootRun

# Si el 8080 está ocupado
$env:SERVER_PORT = "8081"
.\gradlew.bat bootRun

# Interfaz web: http://localhost:8080/ (o el puerto configurado)
```

> Nota: `server.port` se resuelve como `${SERVER_PORT:8080}`. En el entorno de pruebas se ejecutó con `SERVER_PORT=8081` porque el puerto 8080 estaba ocupado por otra aplicación.
