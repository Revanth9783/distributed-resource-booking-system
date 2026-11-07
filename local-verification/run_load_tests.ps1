$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (-not $env:JAVA_HOME) { throw 'Set JAVA_HOME to a JDK installation before running load tests.' }
$java = "$env:JAVA_HOME/bin/java.exe"
$jmeterJar = "tools/apache-jmeter-5.6.3/bin/ApacheJMeter.jar"
$runs = @(
    @{plan='baseline-10-users.jmx';name='baseline-10'},
    @{plan='load-test-50-users.jmx';name='load-50'},
    @{plan='load-test-sustained-100-users.jmx';name='sustained-100'},
    @{plan='load-test-500-users.jmx';name='arrivals-500'},
    @{plan='Concurrent Booking Request.jmx';name='booking-500'}
)
New-Item -ItemType Directory -Path "results" -Force | Out-Null
foreach ($run in $runs) {
    Write-Output "START $($run.name) $(Get-Date -Format o)"
    & $java -Xms256m -Xmx1g -jar $jmeterJar -n -t "plans/$($run.plan)" -l "results/$($run.name).jtl" -j "results/$($run.name).log" -e -o "results/$($run.name)-report" *> "results/$($run.name)-console.log"
    if ($LASTEXITCODE -ne 0) { throw "JMeter failed for $($run.name). See console log." }
    if (-not (Test-Path "results/$($run.name).jtl")) { throw "No samples saved for $($run.name)." }
    if (-not (Test-Path "results/$($run.name)-report/index.html")) { throw "No HTML report for $($run.name)." }
    Write-Output "END $($run.name) $(Get-Date -Format o)"
}

