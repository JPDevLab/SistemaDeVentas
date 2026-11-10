# Pruebas E2E - SistemaDeVentas

Proyecto de automatización E2E basado en Serenity BDD, Cucumber, Selenium y Chrome.
Incluye login real contra la aplicación y dos flujos de negocio completos.

## Requisitos

- Java 21.
- Google Chrome instalado.
- PowerShell 5.1 o superior en Windows.
- No es necesario descargar `chromedriver` manualmente: WebDriverManager lo obtiene.

## Ejecución rápida

Desde la raíz del repositorio:

```powershell
.\e2e\run-e2e.ps1
```

El script compila la aplicación, la inicia con el perfil `e2e` en el puerto `8081`,
ejecuta las pruebas, detiene la aplicación y abre el reporte HTML.

Si la aplicación ya está iniciada en `http://localhost:8081`, el script la reutiliza.

Opciones:

```powershell
.\e2e\run-e2e.ps1 -SkipBuild
.\e2e\run-e2e.ps1 -KeepApp
.\e2e\run-e2e.ps1 -NoReport
```

## Ejecución manual

Terminal 1, desde la raíz:

```powershell
.\gradlew.bat bootJar
java -jar build\libs\SistemaDeVentas-1.0-SNAPSHOT.jar --spring.profiles.active=e2e --server.port=8081
```

Terminal 2:

```powershell
Set-Location e2e
.\gradlew.bat test
```

## Escenarios incluidos

Archivo | Escenarios
--- | ---
`login.feature` | Login válido y login inválido.
`producto-inventario.feature` | Crear producto, registrar entrada y verificar stock/movimiento.
`pedido-cancelacion.feature` | Crear pedido con descuento, verificar total/stock y cancelar restaurando stock.

Credenciales de prueba:

- Usuario: `admin`
- Contraseña: `admin123`

## Reportes y evidencias

- Reporte Cucumber: `e2e/build/reports/cucumber.html`
- Reporte JUnit: `e2e/build/reports/tests/test/index.html`
- Resultado JSON: `e2e/build/reports/cucumber.json`
- Log de la aplicación: `build/e2e-app.out.log`

## Estructura

```text
e2e/
├── build.gradle
├── settings.gradle
├── run-e2e.ps1
└── src/test
    ├── java
    │   ├── definitions
    │   ├── hooks
    │   ├── pages
    │   ├── runners
    │   ├── steps
    │   └── support
    └── resources
        ├── features
        ├── junit-platform.properties
        └── serenity.conf
```

## Preparación del video

1. Abre OBS Studio o Xbox Game Bar y comienza a grabar.
2. Muestra la estructura del proyecto y explica brevemente la adaptación.
3. Ejecuta `.\e2e\run-e2e.ps1` desde la raíz.
4. Muestra la app en `http://localhost:8081` y el login con `admin` / `admin123`.
5. Al finalizar, muestra los 4 escenarios en verde y abre `e2e/build/reports/cucumber.html`.
6. Detén la grabación y guarda el video como `e2e/evidencias/e2e-sistema-ventas.mp4`.

## Notas

- La autenticación se desactiva en los perfiles `test` y `zap` para no afectar las pruebas existentes.
- La base E2E es H2 en memoria y se reinicia cada vez que se inicia la aplicación.
- El producto semilla `Producto E2E Base` tiene precio `60000` y stock `20`.
- Si el puerto `8081` está ocupado, detén el proceso que lo usa antes de ejecutar el script.
