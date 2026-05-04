@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script (Windows) - simplified
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set DIR=%~dp0
set WRAPPER_JAR=%DIR%.mvn\wrapper\maven-wrapper.jar

if not exist "%WRAPPER_JAR%" (
  echo Maven wrapper jar not found: %WRAPPER_JAR%
  exit /b 1
)

@REM Locate Java
if defined JAVA_HOME (
  set "JAVACMD=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVACMD=java"
)

"%JAVACMD%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%DIR%" org.apache.maven.wrapper.MavenWrapperMain %*

endlocal
