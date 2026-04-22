$env:IRON_LEDGER_DB_URL = "jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:5432/postgres"
$env:IRON_LEDGER_DB_USER = "postgres.ynpqqujomjxwrpwaqeop"
$env:IRON_LEDGER_DB_PASSWORD = "7mfgKzxmhEOCikeL"
$cp = "out;C:\Users\Raghavendra\.m2\repository\org\postgresql\postgresql\42.6.1\postgresql-42.6.1.jar"
java "-Djava.net.preferIPv6Addresses=true" -cp $cp app.Main
