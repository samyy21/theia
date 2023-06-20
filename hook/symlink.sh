#!/bin/bash
pwd
#parentdir="$(dirname "$PWD")"
#gitdir= "$(dirname '$parentdir')"
mda=".git/hooks/commit-msg"

#if [ ! -f $mda ]; then
        pwd
        /bin/cp  hook/commit-msg   $mda # > /dev/null 2>&1
#        echo "=> File doesn't exist"
#fi
