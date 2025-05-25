foreach ($sqlFile in Get-ChildItem -Filter *.sql) {
    sqlcmd -S $server -U $user -P $password -i $sqlFile.FullName
}