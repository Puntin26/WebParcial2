#!/usr/bin/env bash
set -euo pipefail
mkdir -p "$HOME/.eventos-h2"
java -cp ~/.m2/repository/com/h2database/h2/2.2.224/h2-2.2.224.jar org.h2.tools.Server -tcp -tcpAllowOthers -ifNotExists -baseDir "$HOME"
