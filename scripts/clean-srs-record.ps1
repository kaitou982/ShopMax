# SRS DVR cleanup script - delete recordings older than N days
$recordPath = "D:\CC-Switch\ShopMax\docker\srs\record\live"
$daysToKeep = 2

if (!(Test-Path $recordPath)) {
    Write-Host "Record path not found: $recordPath"
    exit 0
}

$cutoff = (Get-Date).AddDays(-$daysToKeep)
$deleted = 0
$freed = 0

# Remove stale .tmp files (interrupted recordings)
Get-ChildItem -Path $recordPath -Filter "*.flv.tmp" | ForEach-Object {
    Write-Host "Removing stale: $($_.Name)"
    Remove-Item $_.FullName -Force
    $deleted++
    $freed += $_.Length
}

# Remove old .flv files
Get-ChildItem -Path $recordPath -Filter "*.flv" |
    Where-Object { $_.LastWriteTime -lt $cutoff } |
    ForEach-Object {
        Write-Host "Removing expired: $($_.Name)"
        Remove-Item $_.FullName -Force
        $deleted++
        $freed += $_.Length
    }

$freedMB = [math]::Round($freed / 1MB, 2)
Write-Host "Done: removed $deleted files, freed $freedMB MB"
