#!/bin/bash
source ~/.gvm/bin/gvm-init.sh
gvm use grails 2.3.9

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"
set +xe
ant
