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
 
 
import java.io.*;


public class Utils {

    // run a system command and return output
    public static String cmdExec(String cmdLine) {
        
        String line;
        String output = "";

        try {
            Process p = Runtime.getRuntime().exec(cmdLine);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            while ((line = input.readLine()) != null) {
                output += (line + '\n');
            }

            input.close();
        } 
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return output;
    }

    
    public static void main(String[] args) {
        System.out.println(cmdExec("ls -a"));
    }

}