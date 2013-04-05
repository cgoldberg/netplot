#!/bin/bash

cd build
export CLASSPATH=./
export CLASSPATH=$CLASSPATH:./lib/jrobin-1.4.0.jar
export CLASSPATH=$CLASSPATH:./lib/skinlf.jar
java NetPlot
cd ..
