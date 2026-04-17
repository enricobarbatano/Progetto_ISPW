@echo off
echo ==========================================
echo   BUILD & EXE PACKAGER - Progetto_ISPW (.CMD)
echo ==========================================
echo.

REM 1) Build con Maven (fat-jar)
mvn clean package -DskipTests
IF %ERRORLEVEL% NEQ 0 (
    echo ERRORE: build fallito.
    pause
    exit /b 1
)

REM 2) Fat-jar sovrascritto da Shade
set FATJAR=target\Progetto_ISPW-1.0-SNAPSHOT.jar

if not exist "%FATJAR%" (
    echo ERRORE: Fat-JAR non trovato.
    pause
    exit /b 1
)

echo Fat-JAR rilevato: %FATJAR%
echo.

REM 3) Crea cartella installer
set INSTALL_DIR=target\installer

if exist "%INSTALL_DIR%" (
    rmdir /S /Q "%INSTALL_DIR%"
)
mkdir "%INSTALL_DIR%"

REM 4) Crea EXE con jpackage
jpackage --name Progetto_ISPW --input target --main-jar Progetto_ISPW-1.0-SNAPSHOT.jar --main-class com.ispw.App --type exe --dest target\installer --win-menu --win-shortcut

IF %ERRORLEVEL% NEQ 0 (
    echo ERRORE: jpackage non ha creato l'EXE.
    pause
    exit /b 1
)

echo.
echo ✅ Installer creato in:
echo    %INSTALL_DIR%\Progetto_ISPW.exe
echo.
pause