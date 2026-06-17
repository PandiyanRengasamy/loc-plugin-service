#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────
#  intellij-plugin-loc-service  —  Linux / macOS launcher
#
#  Layout expected (all in the same folder as this script):
#    start.sh
#    intellij-plugin-loc-service-1.0.0.jar   ← executable fat JAR
#    config/
#        application.yml                      ← edit this to change settings
#    lib/                                     ← runtime dependency JARs
# ─────────────────────────────────────────────────────────────────────────

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$SCRIPT_DIR/intellij-plugin-loc-service-1.0.0.jar"

# Spring Boot automatically picks up config/application.yml
# when the config/ folder sits next to the JAR (search-location default).
# We also pass it explicitly for clarity.
java -jar "$JAR" \
     --spring.config.location=file:"$SCRIPT_DIR"/config/application.yml

