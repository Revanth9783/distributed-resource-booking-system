$ErrorActionPreference = 'Stop'
$baseUrl = 'http://127.0.0.1:8082'
$results = [Collections.Generic.List[object]]::new()

function Send-ApiRequest {
    param([string]$Method, [string]$Path, $Body = $null, [hashtable]$Headers = @{})
    $parameters = @{
        Uri = "$baseUrl$Path"; Method = $Method; Headers = $Headers
        SkipHttpErrorCheck = $true; TimeoutSec = 30
    }
    if ($null -ne $Body) {
        $parameters.ContentType = 'application/json'
        $parameters.Body = $Body | ConvertTo-Json -Depth 10
    }
    $response = Invoke-WebRequest @parameters
    return [pscustomobject]@{ Status = [int]$response.StatusCode; Body = ($response.Content | ConvertFrom-Json) }
}

function Check {
    param([string]$Name, [bool]$Condition)
    $results.Add([pscustomobject]@{check=$Name; passed=$Condition})
    if (-not $Condition) { throw "Check failed: $Name" }
}

function New-TestSlot {
    param([long]$ResourceId, [int]$Capacity, [int]$Offset)
    $start = (Get-Date).AddDays(2).AddHours($Offset)
    $response = Send-ApiRequest POST '/api/slots' @{
        resourceId=$ResourceId; startTime=$start.ToString('yyyy-MM-ddTHH:mm:ss')
        endTime=$start.AddHours(1).ToString('yyyy-MM-ddTHH:mm:ss')
        capacity=$Capacity; availableCapacity=$Capacity
    }
    Check 'Create future slot' ($response.Status -eq 201)
    return $response.Body
}

$suffix = [guid]::NewGuid().ToString('N')
$users = @(1..2 | ForEach-Object {
    $response = Send-ApiRequest POST '/api/users' @{
        name="Verification User $_"; email="verification-$_-$suffix@example.com"; password='LocalTestPass123!'
    }
    Check 'Register test user' ($response.Status -eq 201)
    $response.Body
})
$resource = Send-ApiRequest POST '/api/resources' @{
    resourceTypeId=1; name="Verification Resource $suffix"; capacity=100; location='Local test'
}
Check 'Create resource' ($resource.Status -eq 201)
$resourceId = $resource.Body.id
$slot = New-TestSlot $resourceId 10 0
$key = [guid]::NewGuid().ToString()
$body = @{userId=$users[0].id; slotId=$slot.id; quantity=1}
$first = Send-ApiRequest POST '/api/bookings' $body @{'Idempotency-Key'=$key}
Check 'Create confirmed booking' ($first.Status -eq 201 -and $first.Body.status -eq 'CONFIRMED')
$second = Send-ApiRequest POST '/api/bookings' $body @{'Idempotency-Key'=$key}
Check 'Same-key replay returns same booking' ($second.Status -eq 201 -and $second.Body.id -eq $first.Body.id)
$slots = Send-ApiRequest GET "/api/slots/resource/$resourceId"
$current = $slots.Body | Where-Object id -eq $slot.id
Check 'Retry deducts capacity once' ($current.availableCapacity -eq 9)
$changed = Send-ApiRequest POST '/api/bookings' @{userId=$users[0].id;slotId=$slot.id;quantity=2} @{'Idempotency-Key'=$key}
Check 'Changed payload with same key conflicts' ($changed.Status -eq 409)
$cancel = Send-ApiRequest POST "/api/bookings/$($first.Body.id)/cancel"
Check 'Cancellation succeeds' ($cancel.Status -eq 200 -and $cancel.Body.status -eq 'CANCELLED')
$duplicate = Send-ApiRequest POST "/api/bookings/$($first.Body.id)/cancel"
Check 'Duplicate cancellation conflicts' ($duplicate.Status -eq 409)
$slots = Send-ApiRequest GET "/api/slots/resource/$resourceId"
$current = $slots.Body | Where-Object id -eq $slot.id
Check 'Cancellation restores capacity once' ($current.availableCapacity -eq 10)
$loadSlot = New-TestSlot $resourceId 100 2
$readSlots = @(1..19 | ForEach-Object { New-TestSlot $resourceId 100 (4 + $_ * 2) })
$fixtures = @{
    userId=$users[0].id;secondUserId=$users[1].id;resourceId=$resourceId
    functionalSlotId=$slot.id;bookingId=$first.Body.id;loadSlotId=$loadSlot.id
    readSlotCount=21;createdAt=(Get-Date -Format o)
}
$fixtures | ConvertTo-Json | Set-Content "$PSScriptRoot/fixtures.json" -Encoding utf8
$results | ConvertTo-Json -Depth 10 | Set-Content "$PSScriptRoot/functional-results.json" -Encoding utf8
Write-Output "Functional checks passed: $($results.Count). Created isolated load-test fixtures."
