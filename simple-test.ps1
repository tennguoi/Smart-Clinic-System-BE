# Auto Test Clinic Update Persistence
$baseUrl = "http://localhost:8082"

Write-Host "=== AUTO TEST: CLINIC UPDATE ===" -ForegroundColor Cyan
Write-Host "Nhap email admin:"
$email = Read-Host
Write-Host "Nhap password:"
$password = Read-Host -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

try {
    # Login
    Write-Host "[1] Dang nhap..." -ForegroundColor Cyan
    $loginBody = @{email=$email;password=$passwordPlain} | ConvertTo-Json
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body $loginBody
    $token = $loginResponse.token
    Write-Host "OK - Token: $($token.Substring(0,20))..." -ForegroundColor Green
    
    $headers = @{"Authorization"="Bearer $token";"Content-Type"="application/json"}
    
    # Get current
    Write-Host "[2] Get current info..." -ForegroundColor Cyan
    $current = Invoke-RestMethod -Uri "$baseUrl/api/admin/clinic" -Method GET -Headers $headers
    Write-Host "Current name: $($current.name)" -ForegroundColor Yellow
    
    # Update
    $timestamp = Get-Date -Format "HH:mm:ss"
    $newName = "Test Update - $timestamp"
    Write-Host "[3] Update to: $newName" -ForegroundColor Cyan
    $updateBody = @{name=$newName} | ConvertTo-Json
    $updated = Invoke-RestMethod -Uri "$baseUrl/api/admin/clinic" -Method PUT -Headers $headers -Body $updateBody
    Write-Host "Response name: $($updated.name)" -ForegroundColor Green
    
    # Wait & verify
    Start-Sleep -Seconds 3
    Write-Host "[4] Verify after 3 sec..." -ForegroundColor Cyan
    $verify1 = Invoke-RestMethod -Uri "$baseUrl/api/admin/clinic" -Method GET -Headers $headers
    
    Start-Sleep -Seconds 5
    Write-Host "[5] Verify after 8 sec..." -ForegroundColor Cyan
    $verify2 = Invoke-RestMethod -Uri "$baseUrl/api/admin/clinic" -Method GET -Headers $headers
    
    # Results
    Write-Host "=== RESULTS ===" -ForegroundColor Cyan
    if ($verify1.name -eq $newName) {
        Write-Host "[OK] Test 1 (3 sec): PASSED" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] Test 1: Name = $($verify1.name)" -ForegroundColor Red
    }
    
    if ($verify2.name -eq $newName) {
        Write-Host "[OK] Test 2 (8 sec): PASSED" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] Test 2: Name = $($verify2.name)" -ForegroundColor Red
    }
    
    Write-Host "Current DB value: $($verify2.name)" -ForegroundColor Yellow
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
}
