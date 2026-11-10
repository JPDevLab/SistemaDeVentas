param(
    [switch]$SkipBuild,
    [switch]$KeepApp,
    [switch]$NoReport
)

$ErrorActionPreference = "Stop"

$e2eDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = Split-Path -Parent $e2eDir
$jarPath = Join-Path $rootDir "build\libs\SistemaDeVentas-1.0-SNAPSHOT.jar"
$baseUrl = "http://localhost:8081"
$stdout = Join-Path $rootDir "build\e2e-app.out.log"
$stderr = Join-Path $rootDir "build\e2e-app.err.log"

function Test-AppHealth {
    try {
        $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -TimeoutSec 2
        return $health.status -eq "UP"
    } catch {
        return $false
    }
}

if (-not $SkipBuild) {
    Push-Location $rootDir
    try {
        & "$rootDir\gradlew.bat" clean bootJar --no-daemon --console=plain
        if ($LASTEXITCODE -ne 0) {
            throw "No se pudo compilar la aplicacion."
        }
    } finally {
        Pop-Location
    }
}

if (-not (Test-Path -LiteralPath $jarPath)) {
    throw "No se encontro el jar de la aplicacion: $jarPath"
}

$appProcess = $null
$startedApp = $false
try {
    if (Test-AppHealth) {
        Write-Host "Usando la aplicacion existente en $baseUrl" -ForegroundColor Yellow
    } else {
        Write-Host "Iniciando SistemaDeVentas con el perfil E2E en $baseUrl" -ForegroundColor Cyan
        $appProcess = Start-Process -FilePath "java" `
            -ArgumentList @("-jar", $jarPath, "--spring.profiles.active=e2e", "--server.port=8081") `
            -RedirectStandardOutput $stdout `
            -RedirectStandardError $stderr `
            -PassThru
        $startedApp = $true

        $ready = $false
        for ($i = 0; $i -lt 60; $i++) {
            Start-Sleep -Seconds 1
            if (Test-AppHealth) {
                $ready = $true
                break
            }
        }
        if (-not $ready) {
            throw "La aplicacion no respondio en $baseUrl. Revisa $stdout y $stderr."
        }
    }

    Push-Location $e2eDir
    try {
        & "$e2eDir\gradlew.bat" clean test --no-daemon --console=plain
        $testExitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }

    if ($testExitCode -ne 0) {
        throw "Las pruebas E2E fallaron. Revisa $e2eDir\build\reports\cucumber.html"
    }

    Write-Host "Pruebas E2E finalizadas correctamente." -ForegroundColor Green
    if (-not $NoReport) {
        Start-Process -FilePath (Join-Path $e2eDir "build\reports\cucumber.html")
    }
} finally {
    if ($startedApp -and -not $KeepApp -and $appProcess) {
        Write-Host "Deteniendo la aplicacion E2E (PID $($appProcess.Id))" -ForegroundColor Cyan
        Stop-Process -Id $appProcess.Id -Force -ErrorAction SilentlyContinue
    }
}
