#!/bin/sh

#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

app_path=$0

# Resolve the directory containing this script
APP_HOME=$(cd "$(dirname "$0")" >/dev/null && pwd)

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support.
case "$(uname)" in
  CYGWIN* | Darwin* | MINGW*)
    darwin=true
    ;;
  MSYS* | CYGWIN*)
    msys=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ]; then
    if [ -x "$JAVA_HOME/jre/sh/java" ]; then
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ]; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD=java
    command -v java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    MAX_FD_LIMIT=$(ulimit -H -n 2>/dev/null || echo)
    if [ "$MAX_FD" = "maximum" ] && [ -n "$MAX_FD_LIMIT" ] ; then
        MAX_FD=$MAX_FD_LIMIT
    fi
    case $MAX_FD in
      max*)
        ulimit -n "$MAX_FD" 2>/dev/null || warn "Could not set maximum file descriptor limit: $MAX_FD"
        ;;
      '' | *[0-9]*)
        ulimit -n "$MAX_FD" 2>/dev/null || warn "Could not set maximum file descriptor limit: $MAX_FD"
        ;;
    esac
fi

# Collect all arguments for the java command, stacking in reverse order.
for arg in "$@" ; do
    eval "set -- $arg \"\$@\""
done

exec "$JAVACMD" \
    -classpath "$CLASSPATH" \
    -Dorg.gradle.appname="$APP_BASE_NAME" \
    -Dorg.gradle.welcome=never \
    -XX:MaxMetaspaceSize=256m \
    -XX:+HeapDumpOnOutOfMemoryError \
    -Dfile.encoding=UTF-8 \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
