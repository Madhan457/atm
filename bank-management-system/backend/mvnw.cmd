@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script for Windows
@REM ----------------------------------------------------------------------------
@echo off
set ERROR_CODE=0

@REM Set local scope for variables
setlocal

@REM Find maven command
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    mvn %*
    exit /b %ERRORLEVEL%
) else (
    echo [ERROR] Maven (mvn) is not recognized in system PATH.
    echo Please install Apache Maven or ensure 'mvn' is added to your PATH environment variable.
    exit /b 1
)
