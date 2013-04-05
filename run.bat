@ECHO OFF
cd build
set CLASSPATH=.;.\lib\jrobin-1.4.0.jar;.\lib\skinlf.jar
java NetPlot
cd ..
