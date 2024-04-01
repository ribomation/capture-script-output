#!/usr/bin/env bash
set -e            #stop at first error (non-zero exit code)
set -u            #stop if using undef variable
set -x            #echo all commands
set -o pipefail   #set exit code to first failed cmd in a pipe

TS=`date --iso-8601=ns`
echo "timestamp: $TS"
echo "directory: $PWD"
echo "script   : @NAME@"
@CMD@
