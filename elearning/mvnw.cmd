@ECHO OFF
SETLOCAL EnableDelayedExpansion

set WRAPPER_DIR=%~dp0.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set WRAPPER_PROPS=%WRAPPER_DIR%\maven-wrapper.properties

if not exist "%WRAPPER_JAR%" (
  echo Downloading Maven wrapper jar...
  for /f "usebackq delims=" %%I in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "$p = Get-Content -Raw '%WRAPPER_PROPS%'; if ($p -match 'wrapperUrl=(.+)') { $matches[1].Trim() }"`) do set WRAPPER_URL=%%I
  if "!WRAPPER_URL!"=="" set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
  if "!WRAPPER_URL!"=="" (
    echo Could not resolve wrapperUrl from %WRAPPER_PROPS%
    exit /b 1
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command "New-Item -ItemType Directory -Force '%WRAPPER_DIR%' | Out-Null; Invoke-WebRequest -Uri '!WRAPPER_URL!' -OutFile '%WRAPPER_JAR%'"
  if errorlevel 1 (
    echo Failed to download Maven wrapper jar.
    exit /b 1
  )
)

if "%JAVA_HOME%"=="" (
  set JAVA_EXE=java
) else (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory=%~dp0 -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
exit /b %errorlevel%
