param(
    [string]$server = "localhost",
    [string]$user = "sa",
    [string]$password = "Zh@ng810975!@#",
    [string]$outputPath = "output"
)

# 导入output目录下所有SQL文件
foreach ($sqlFile in Get-ChildItem -Path $outputPath -Filter *.sql) {
    sqlcmd -S $server -U $user -P $password -i $sqlFile.FullName
    Write-Host "已导入: $($sqlFile.Name)"
}