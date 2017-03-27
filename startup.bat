@ECHO OFF
ECHO Starting GPCS Calculation Service...

SET MY_PATH=%~dp0
SET PREV_PATH=%cd%
SET LINK_NAME=GPCS.Utils.Startup.lnk
SET START_UP_PATH="%appdata%\Microsoft\Windows\Start Menu\Programs\Startup\"

REM ADD GPCS CALCULATION DAILY SERVICE TO WINDOWS STARTUP
IF NOT "%MY_PATH%" == %START_UP_PATH% (
    CD /d %START_UP_PATH%
    IF EXIST %LINK_NAME% (DEL %LINK_NAME%)
    MKLINK %LINK_NAME% "%~f0"
    CD /d %PREV_PATH%
)

FOR %%X IN (git-bash.exe) DO (SET FOUND=%%~$PATH:X)
IF NOT DEFINED FOUND (
    ECHO Error: Please add git-bash.exe to your path environment variables before using this script.
    PAUSE
    EXIT
)

CD /d %MY_PATH%
START git-bash.exe ./bin/GPCS.Utils --daily --service
CD /d %PREV_PATH%
EXIT