$ErrorActionPreference = 'Stop'
for ($attempt = 1; $attempt -le 60; $attempt++) {
    try {
        $health = Invoke-RestMethod 'http://127.0.0.1:8082/actuator/health' -TimeoutSec 5
        if ($health.status -eq 'UP') {
            Write-Output 'The verification API is ready.'
            return
        }
    } catch {
        # A new deployment can return connection failures or HTTP 502 while starting.
    }
    Start-Sleep -Seconds 2
}
throw 'API did not become ready. Inspect docker compose -p booking-evaluation -f compose.local.json logs --tail 80.'
