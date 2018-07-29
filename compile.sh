#!/usr/bin/env bash

cd code
find ./test -name '*.java' | xargs javac -source 1.8 -target 1.8
cd ..
