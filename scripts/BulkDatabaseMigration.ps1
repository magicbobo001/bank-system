$pythonPath = "C:\Program Files\Python311\Scripts"
# 使用完整路径执行命令
if (Test-Path "$pythonPath\mssql-scripter.exe") {
    Write-Host "mssql-scripter已存在"
} else {
    # 安装命令需要指定完整路径
    & "$pythonPath\pip.exe" install mssql-scripter
}
$outputPath = Join-Path $PSScriptRoot "output"
New-Item -ItemType Directory -Path $outputPath -Force | Out-Null
# 后续脚本保持不变...
# 数据库列表配置文件
$config = Get-Content -Path "databases.json" | ConvertFrom-Json
$server = "localhost"
$user = "sa"
$password = "Zh@ng810975!@#"

foreach ($db in $config.Databases) {
    try {
        & "$pythonPath\mssql-scripter" -S $server -d $db.Name -U $user -P $password `
            --schema-and-data --file-path "$PSScriptRoot\output\$($db.Name)_Full.sql"  # 修正参数并指定输出目录
        
        # 修正BCP命令格式
        bcp "$db.Name..loan_application" out "$outputPath\$($db.Name)_loan_application.csv" `
            -S $server -U $user -P $password -c -t "," -e "$outputPath\error.log"
        
        Write-Host "成功导出数据库: $($db.Name)"
    }
    catch {
        Write-Error "导出失败: $($db.Name) - $_"
    }
}