$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
New-Item -ItemType Directory -Path tools -Force | Out-Null
$url = 'https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.3.zip'
if (-not (Test-Path tools/apache-jmeter-5.6.3.zip)) {
    Invoke-WebRequest $url -OutFile tools/apache-jmeter-5.6.3.zip
}
if (-not (Test-Path tools/apache-jmeter-5.6.3.zip.sha512)) {
    Invoke-WebRequest "$url.sha512" -OutFile tools/apache-jmeter-5.6.3.zip.sha512
}
$expected = ((Get-Content tools/apache-jmeter-5.6.3.zip.sha512 -Raw).Trim() -split '\s+')[0]
$actual = (Get-FileHash tools/apache-jmeter-5.6.3.zip -Algorithm SHA512).Hash
if ($actual -ne $expected) { throw 'The downloaded JMeter archive failed checksum verification.' }
if (-not (Test-Path tools/apache-jmeter-5.6.3/bin/ApacheJMeter.jar)) {
    Expand-Archive -LiteralPath tools/apache-jmeter-5.6.3.zip -DestinationPath tools
}
Write-Output 'JMeter 5.6.3 downloaded and checksum-verified.'
