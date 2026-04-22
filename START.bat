@echo off
set IRON_LEDGER_DB_URL=jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:5432/postgres
set IRON_LEDGER_DB_USER=postgres.ynpqqujomjxwrpwaqeop
set IRON_LEDGER_DB_PASSWORD=7mfgKzxmhEOCikeL
set CLASSPATH=out;C:\Users\Raghavendra\.m2\repository\org\postgresql\postgresql\42.6.1\postgresql-42.6.1.jar
java app.Main
pause
