@echo off
java -Djava.library.path="%~dp0target" -jar "%~dp0target/banking-system-1.0-SNAPSHOT-jar-with-dependencies.jar"
pause
