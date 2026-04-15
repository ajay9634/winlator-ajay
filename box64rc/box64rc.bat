@echo off
setlocal
color 0B

set "URL=https://raw.githubusercontent.com/ajay9634/winlator-ajay/Winlator-9-proot/box64rc/ajay.box64rc"
set "TARGET_DIR=Z:\etc"
set "TARGET_FILE=%TARGET_DIR%\config.box64rc"
set "TEMP_FILE=%TEMP%\ajay_box64rc_temp"

title Winlator Ajay box64rc Downloader

echo ==========================================
echo     Winlator Ajay box64rc Downloader
echo ==========================================
echo.

if not exist "%TARGET_DIR%" (
    mkdir "%TARGET_DIR%"
)

echo Downloading Ajay box64rc...
:: -# shows a simple # progress bar instead of the full statistics
curl -f -L -# -o "%TEMP_FILE%" "%URL%"

if %ERRORLEVEL% NEQ 0 (
    color 0C
    echo.
    echo Download failed. Check internet connection.
    echo.
    pause
    exit /b
)

for %%I in ("%TEMP_FILE%") do if %%~zI == 0 (
    color 0C
    echo.
    echo File is empty.
    del "%TEMP_FILE%"
    echo.
    pause
    exit /b
)

echo.
echo Replacing configuration...
if exist "%TARGET_FILE%" (
    attrib -R -S -H "%TARGET_FILE%"
    del /f /q "%TARGET_FILE%"
)

copy /y "%TEMP_FILE%" "%TARGET_FILE%" >nul

if %ERRORLEVEL% EQU 0 (
    color 0A
    echo Done: %TARGET_FILE% has been updated. Now Restart the Container.
) else (
    color 0C
    echo Failed to copy. Check if Z: drive is mounted.
)

if exist "%TEMP_FILE%" del "%TEMP_FILE%"

echo.
echo Press any key to exit...
pause >nul
