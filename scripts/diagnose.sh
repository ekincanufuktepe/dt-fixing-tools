#!/usr/bin/env bash

# Usage: bash diagnose.sh [optional args]
# The optional args will be passed directly into the diagnoser.
# Should be run in the same folder as the pom.xml on the desired subject, after having run:
# $ mvn compile test-compile dependency:copy-dependencies

(
    ROOT=$(cd "$(dirname $BASH_SOURCE)"; pwd)

    OUTPUT_DIR="diagnosis/"

    CLASSPATH="$ROOT/../target/dt-fixing-tools-1.0.0-SNAPSHOT.jar:$ROOT/../target/dependency/*:$ROOT/../lib/*:"
    JAVA_AGENT="$ROOT/../target/dependency/dtdetector-1.2.1-SNAPSHOT.jar"

    if [[ ! -e "$JAVA_AGENT" ]]; then
        (
            cd "$ROOT/.."
            mvn dependency:copy-dependencies
        )
    fi

    mvn testrunner:testplugin
    # NOTE: JAVA_HOME libraries must come first in classpath (not sure why, but Soot fails otherwise).
    # java -cp "$JAVA_HOME/jre/lib/*:$CLASSPATH:$SUBJ_CLASSPATH:" edu.illinois.cs.dt.tools.diagnosis.Diagnoser --outputDir "$OUTPUT_DIR" --javaagent "$JAVA_AGENT" $@
) |& tee log.txt

