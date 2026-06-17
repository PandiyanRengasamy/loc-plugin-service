@echo off
REM ─────────────────────────────────────────────────────────────────────────
REM  intellij-plugin-loc-service  —  Windows launcher
REM
REM  Layout expected (all in the same folder as this script):
REM    start.bat
REM    intellij-plugin-loc-service-1.0.0.jar   ← executable fat JAR
REM    config\
REM        application.yml                      ← edit this to change settings
REM    lib\                                     ← runtime dependency JARs
REM ─────────────────────────────────────────────────────────────────────────

SET SCRIPT_DIR=%~dp0
SET JAR=%SCRIPT_DIR%intellij-plugin-loc-service-1.0.0.jar

REM Spring Boot automatically picks up config\application.yml
REM when the config\ folder sits next to the JAR (search-location default).
REM We also pass it explicitly for clarity.
java -jar "%JAR%" ^
     --spring.config.location=file:%SCRIPT_DIR%config/application.yml

pause

