NetPlot - Network Latency Monitor in Java
=========================================

*Corey Goldberg - 2006*

NetPlot is a GUI network monitoring tool written in Java. It uses your system's ping utility to send ICMP ECHO_REQUEST to a host or device. With each collection, it sends 3 pings to get the average latency. Results are then plotted in real-time so you can monitor network latency.

Dev Home: [https://github.com/cgoldberg/netplot](https://github.com/cgoldberg/netplot)

Download
--------

[http://goldb.org/netplot.html](http://goldb.org/netplot.html)

Platforms
---------

NetPlot requires a Java Runtime Environment (JRE). It uses a `Ping` class that is compatible with output from Linux (`iputils`) and Windows ping. Other ping implementations can easily be supported by editing Ping.java and rebuilding NetPlot.

Build / Run
-----------

 * To Build/Compile: use `ant` with included `build.xml`
 * To Run: `run.sh` (linux), `run.bat` (windows)

Screenshots
-----------

![foo](http://goldb.org/images/netplot_output.jpg)
![bar](http://goldb.org/images/netplot_control.jpg)
