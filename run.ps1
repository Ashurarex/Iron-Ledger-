if (!(Test-Path "target\iron-ledger.jar")) {
    .\package\build-fat-jar.ps1
}

java "-Djava.net.preferIPv6Addresses=true" -jar "target\iron-ledger.jar"
