@echo off
setlocal
set "APP_HOME=%~dp0.."
set "JAVA=%APP_HOME%\bin\java.exe"
set "CP=%APP_HOME%\drivers\*"

"%JAVA%" --class-path "%CP%" -m com.ispw.progetto/com.ispw.App
endlocal