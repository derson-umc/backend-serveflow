#!/bin/sh
# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script (POSIX) - simplified
# ----------------------------------------------------------------------------

DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$DIR/.mvn/wrapper/maven-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Maven wrapper jar not found: $WRAPPER_JAR" >&2
  exit 1
fi

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi

exec "$JAVACMD" -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$DIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
