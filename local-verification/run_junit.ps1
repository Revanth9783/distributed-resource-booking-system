$ErrorActionPreference = 'Stop'
if (-not $env:JAVA_HOME) { throw 'Set JAVA_HOME to a JDK installation before running tests.' }
$env:DB_PASSWORD = Get-Content "$PSScriptRoot/.db-password" -Raw
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:55432/resource_booking'
$env:SPRING_DATASOURCE_USERNAME = 'postgres'
$env:REDIS_HOST = 'localhost'
$env:REDIS_PORT = '16379'
$env:KAFKA_BOOTSTRAP_SERVERS = 'localhost:19092'
$env:SERVER_PORT = '18080'
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    & ./mvnw.cmd -B test
    if ($LASTEXITCODE -ne 0) { throw 'JUnit execution failed.' }
} finally {
    Pop-Location
}
