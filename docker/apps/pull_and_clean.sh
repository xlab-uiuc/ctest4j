#!/bin/bash

for f in $(find . -maxdepth 1 -mindepth 1 -type d);
do
    # if f contains "ctest-runner" then skip
    if [[ $f == *"ctest-runner"* ]]; then
	(cd $f && git restore . && git status  && git checkout junit5  && git pull && mvn clean)
    else
	echo $f
	(cd $f && git restore . && git status  && git checkout ctest-eval  && git pull && mvn clean)
    fi
done
