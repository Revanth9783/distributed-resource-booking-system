$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$compose = @('compose','-p','booking-evaluation','-f','compose.local.json')
$fixtures = Get-Content fixtures.json -Raw | ConvertFrom-Json
$baseUrl = 'http://127.0.0.1:8082'
$results = [ordered]@{}
$resources = Invoke-RestMethod "$baseUrl/api/resources"
$cachedResources = Invoke-RestMethod "$baseUrl/api/resources"
$results.resourceListCacheRead = @($resources).Count -eq @($cachedResources).Count
$slotRows = Invoke-RestMethod "$baseUrl/api/slots/resource/$($fixtures.resourceId)"
$results.readSlotCount = @($slotRows).Count
$loadSlotId = [long]$fixtures.loadSlotId
$sql = "SELECT count(*) AS confirmed_bookings,coalesce(sum(quantity),0) AS booked_units FROM bookings WHERE slot_id=$loadSlotId AND status='CONFIRMED'; SELECT id,capacity,available_capacity,status FROM availability_slots WHERE id=$loadSlotId; SELECT count(*) AS negative_slots FROM availability_slots WHERE available_capacity<0; SELECT status,count(*) FROM outbox_events GROUP BY status; SELECT count(*) AS processed_events FROM processed_events;"
& docker @compose exec -T postgres psql -U postgres -d resource_booking -c $sql *> database-verification.txt
if ($LASTEXITCODE -ne 0) { throw 'Database verification failed.' }
& docker @compose logs --tail 300 nginx *> nginx-evidence.log
$results.nginxUpstreams = @(Select-String -Path nginx-evidence.log -Pattern 'upstream=([0-9.]+):8080' -AllMatches | ForEach-Object { $_.Matches.Groups[1].Value } | Sort-Object -Unique)
& docker @compose stop app2 *> replica-stop.log
if ($LASTEXITCODE -ne 0) { throw 'Could not stop the verification replica.' }
try {
    $responses = @(1..100 | ForEach-Object {
        try { [int](Invoke-WebRequest "$baseUrl/api/slots/resource/$($fixtures.resourceId)" -TimeoutSec 10 -SkipHttpErrorCheck).StatusCode }
        catch { 'Exception' }
    })
    $results.replicaStop = @($responses | Group-Object | Select-Object Name,Count)
} finally {
    & docker @compose start app2 *> replica-restart.log
}
$results | ConvertTo-Json -Depth 10 | Set-Content post-check-results.json -Encoding utf8
Write-Output 'Saved cache, database, routing, and replica-stop verification evidence.'
