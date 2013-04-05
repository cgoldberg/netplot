/*
 *  Copyright 2006 Corey Goldberg (cgoldberg _at_ gmail.com)
 *
 *  This file is part of NetPlot.
 *
 *  NetPlot is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  NetPlot is distributed in the hope that it will be useful,
 *  but without any warranty; without even the implied warranty of
 *  merchantability or fitness for a particular purpose.  See the
 *  GNU General Public License for more details.
 */
 
 
/*
 * - send ICMP ECHO_REQUEST to a host or device.
 * - send 3 pings at 1 sec intervals and get avg response.
 * - parameters and output format require 
 * - ping from Linux iputils or ping from MS Windows.
 */

import java.util.regex.*;


public class Ping implements MonitorPlugin {
    
    private String cmdLine;
    private String osName = System.getProperty("os.name");

    public Ping(String host) {
        // if we are running on Windows
        if (this.osName.contains("indows")) {
            this.cmdLine = "ping -n 3 " + host;  // do 3 pings
        }
        // if we are running on a system with iputils (Linux)
        else {
            this.cmdLine = "ping " + host + " -c 3";  // do 3 pings
        }
    }

	
    public double run() {
        try {
            String cmdOutput = Utils.cmdExec(cmdLine);
            
            Pattern pattern;
            if (this.osName.contains("indows")) {
                pattern = Pattern.compile("Average = (.*)ms");  // get avg from ping output
            }
            else {
                pattern = Pattern.compile(" = (.*?)/(.*?)/");  // get avg from ping output
            }
            Matcher matcher = pattern.matcher(cmdOutput);
            matcher.find();
            
            double avg;
            if (this.osName.contains("indows")) {
                avg = Double.parseDouble(matcher.group(1));  // avg response time in millisecs
	        } 
            else {
                avg = Double.parseDouble(matcher.group(2));  // avg response time in millisecs
	        }
            
            return avg;            
        }
        catch (Exception ex) {
            System.err.println("ERROR - invalid response from system ping");
            ex.printStackTrace();
            return 00;
        }
    }
		
	
    public static void main(String[] args) {
        Ping p = new Ping("www.goldb.org");
        double respTime = p.run();
        System.out.println(respTime);
    }
}
