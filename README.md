# SistemaDeVentas

Aplicación de gestión de productos, inventario y pedidos con API REST y frontend web, desarrollada con Spring Boot 3.4.1 y Java 21. Incluye pruebas unitarias, de integración y de aceptación, cobertura con JaCoCo, pruebas de mutación con PITest y análisis estático con SonarQube.

Repositorio: [github.com/JPDevLab/SistemaDeVentas](https://github.com/JPDevLab/SistemaDeVentas).

## Requisitos

| Herramienta | Versión |
|---|---|
| Java | 21 |
| Gradle | wrapper incluido (8.10.2) |
| PostgreSQL | 16 (o Docker) |
| SonarQube | 26.x (opcional, solo para análisis) |

## Base de datos

Con Docker:

```powershell
docker run -d --name sistema-ventas-db -p 5432:5432 `
  -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=sistema_ventas postgres:16
```

La aplicación usa por defecto `jdbc:postgresql://localhost:5432/sistema_ventas` con usuario `postgres`. Se puede sobrescribir con `DB_USER` y `DB_PASSWORD`.

## Ejecución

```powershell
# Puerto por defecto: 8080
.\gradlew.bat bootRun

# Si el 8080 está ocupado
$env:SERVER_PORT = "8081"
.\gradlew.bat bootRun
```

También se puede desactivar el log SQL con `JPA_SHOW_SQL=false` (valor por defecto).

## Frontend

La interfaz web se sirve desde el mismo Spring Boot y consume la API con rutas relativas. Una vez iniciada la aplicación, abrir `http://localhost:8080/` (o el puerto configurado en `SERVER_PORT`).

- SPA con rutas por hash: `#/dashboard`, `#/productos`, `#/inventario` y `#/pedidos`.
- Módulos ESM en `src/main/resources/static/js` y estilos en `css/styles.css`; no requiere Node ni compilación.
- El código estático se excluye del análisis de SonarQube (`sonar.exclusions`).

## Pruebas

```powershell
# Unitarias + integración (H2 en memoria) = 61 pruebas
.\gradlew.bat clean test

# Pruebas de aceptación (Cucumber)
.\gradlew.bat acceptanceTest
```

Las pruebas de integración usan el perfil `test` (`src/test/resources/application-test.properties`) con H2, por lo que no requieren PostgreSQL.

## Cobertura y mutaciones

```powershell
.\gradlew.bat jacocoTestReport   # build/reports/jacoco/test/html/index.html
.\gradlew.bat pitest             # build/reports/pitest/index.html
```

Resultados de referencia: 88.1 % de líneas, 72.7 % de ramas y 84.6 % de cobertura combinada; PITest con 183 mutaciones, 69 % eliminadas y 88 % de fuerza de pruebas.

## Análisis con SonarQube

```powershell
# Iniciar SonarQube (instalación local)
C:\sonarqube-26.8.0.126808\bin\windows-x86-64\StartSonar.bat

# El token se inyecta por variable de entorno; nunca se versiona
$env:SONAR_TOKEN = "<token>"
.\gradlew.bat sonar
```

El token se resuelve desde `SONAR_TOKEN` (entorno) o desde la propiedad de Gradle `sonarToken`. El Quality Gate exige cobertura nueva >= 80 %, duplicación <= 3 % y cero violaciones nuevas.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/productos` | Crear producto |
| GET | `/productos` | Listar productos |
| GET | `/productos/{id}` | Obtener producto |
| PUT | `/productos/{id}` | Actualizar producto |
| POST | `/inventario/entradas` | Registrar entrada de inventario |
| GET | `/inventario/{productoId}` | Consultar stock |
| GET | `/inventario/movimientos[?productoId=]` | Listar movimientos |
| POST | `/pedidos` | Crear pedido |
| GET | `/pedidos[?cliente=]` | Listar pedidos |
| GET | `/pedidos/{id}` | Obtener pedido |
| PATCH | `/pedidos/{id}/cancelar` | Cancelar pedido y restaurar stock |
| PATCH | `/pedidos/{id}/entregar` | Entregar pedido |

## Integración continua

El pipeline de Azure (`azure-pipeline.yml`) se dispara en la rama `master` y ejecuta: compilación y pruebas, Cucumber, PITest y publicación de reportes JaCoCo/PITest. El análisis SonarQube es opcional: para habilitarlo, definir la variable `sonarEnabled=true` y la variable secreta `SONAR_TOKEN` en el pipeline.

## Estructura

```
src/main/java/org/example
  controller/   endpoints REST
  service/      interfaces + implementaciones transaccionales
  repository/   Spring Data JPA
  model/        entidades y reglas de negocio
  dto/          request/response (records)
  mapper/       conversión entidad <-> DTO
  exception/    excepciones de dominio y manejador global
src/main/resources/static
  index.html    shell de la SPA
  css/          estilos responsivos
  js/           api, formato, UI y vistas por módulo
src/test/java/org/example
  model/        pruebas de dominio
  service/      pruebas unitarias con Mockito
  *.java        pruebas de integración (MockMvc + H2)
src/test/resources/features  escenarios Cucumber
```
