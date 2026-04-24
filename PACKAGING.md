# Iron Ledger Packaging

Build the fat JAR:

```powershell
.\package\build-fat-jar.ps1
```

Build the Windows installer:

```powershell
.\package\package-windows.ps1
```

Outputs:

- `target\iron-ledger.jar`
- `installer\Iron Ledger-1.0.exe` when WiX is installed
- `installer\Iron Ledger\Iron Ledger.exe` app-image fallback when WiX is unavailable

Runtime config:

- `config.properties` supplies `DB_URL`, `DB_USER`, `DB_PASSWORD`, and `DB_POOL_SIZE`.
- Environment variables `IRON_LEDGER_DB_URL`, `IRON_LEDGER_DB_USER`, and `IRON_LEDGER_DB_PASSWORD` override the file.
