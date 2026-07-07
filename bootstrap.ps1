$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

Write-Host "Bootstrapping Bank Transaction System with Docker Compose..."
docker compose up --build -d
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Stack is starting."
Write-Host ""
Write-Host "  Login:        http://localhost:8083/login"
Write-Host "  Home:         http://localhost:8083/home"
Write-Host "  Transactions: http://localhost:8083/transactions"
Write-Host "  Analytics:    http://localhost:8090/docs"
Write-Host ""
Write-Host "Default credentials:"
Write-Host "  admin / admin123   (administrator)"
Write-Host "  admin1 / admin123  (employee)"
Write-Host ""
Write-Host "View logs: docker compose logs -f bank-app"
