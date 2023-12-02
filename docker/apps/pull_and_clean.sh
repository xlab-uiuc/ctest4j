#!/bin/bash

for f in $(find . -maxdepth 1 -mindepth 1 -type d);
do
    echo $f
    (cd $f && git restore . && git status  && git checkout confuzz  && git pull && mvn clean)
done
