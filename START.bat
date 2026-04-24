@echo off
if not exist target\iron-ledger.jar (
    powershell -ExecutionPolicy Bypass -File package\build-fat-jar.ps1
)
java -jar target\iron-ledger.jar
pause
