#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export PATH="$JAVA_HOME/bin:$SCRIPT_DIR/.tools/apache-maven-3.9.9/bin:$PATH"

"$SCRIPT_DIR/mvn-local" clean spring-boot:run
