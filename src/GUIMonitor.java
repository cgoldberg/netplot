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


import org.jrobin.core.*;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraph;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.Date;


public class GUIMonitor {
    private static JFrame frame;
    private static GUIGraphPanel graphPanel;
    private static RrdGraph graph;
    private static RrdGraphDef gDef;
    private static final String rrd = "file.rrd";
    private static final long START = Util.getTimestamp();
    private static Date end = new Date(START);
    private Stopwatch timer = new Stopwatch();
    private String graphTitle;
    private String graphXLabel;
    private String graphYLabel;

	
    public GUIMonitor(MonitorPlugin plugin, int interval, String graphTitle, String graphYLabel, String graphXLabel) {
        this.graphTitle = graphTitle;
        this.graphXLabel = graphXLabel;
        this.graphYLabel = graphYLabel;
        
        long time;
        
        try {	
            this.prepareRrd();
            this.prepareFrame();

            RrdDb rrdDb = new RrdDb(rrd, RrdBackendFactory.getFactory("MEMORY"));
            Sample sample = rrdDb.createSample();
            
            while (true) {
                timer.start();
                
                // get the avg ping response time
                double resultTime = plugin.run();
                System.out.println(resultTime);
                
                time = Util.getTimestamp();

                // update the rrd
                sample.setTime(time);
                sample.setValue("a", resultTime);
                sample.update();
                
                // set custom graph settings
                gDef.setTimePeriod(START - 10, time);
                end.setTime(time);

                // regenerate the graph
                frame.repaint();
                
                long pace = (1000 * interval) - timer.getElapsedTime();
                if (pace > 0) {
                        Thread.sleep(pace);
                }
                
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
	
		
    private void prepareRrd() throws IOException, RrdException {
        RrdDef rrdDef = new RrdDef(rrd, 5);
        rrdDef.addDatasource("a", "GAUGE", 600, Double.NaN, Double.NaN);
        rrdDef.addArchive("AVERAGE", 0.5, 1, 17280);
        RrdDb rrdDb = new RrdDb(rrdDef, RrdBackendFactory.getFactory("MEMORY"));
        rrdDb.close();
    }

	
	
    private void prepareFrame() throws RrdException {
        gDef = new RrdGraphDef();
        gDef.setTimePeriod(START - 10, START);
        gDef.setImageBorder(Color.WHITE, 0);
        gDef.setTitle(graphTitle);
        gDef.setVerticalLabel(graphYLabel);
        gDef.setTimeAxisLabel(graphXLabel);
        gDef.datasource("a", rrd, "a", "AVERAGE", "MEMORY");
        gDef.datasource( "avg", "a", "AVERAGE");
        gDef.area("a", Color.decode("0xb6e4"), "Real");
        gDef.line("avg", Color.RED,  "Average@l");
        gDef.gprint("a", "MIN", "min = @2 ms@l");
        gDef.gprint("a", "MAX", "max = @2 ms@l");
        gDef.gprint("a", "AVERAGE", "avg = @2 ms");
        gDef.time("@l@lTime period: @t", "MMM dd, yyyy    HH:mm:ss", START);
        gDef.time("to  @t@c", "HH:mm:ss");

        // create graph
        graph = new RrdGraph(gDef);

        // create swing components
        graphPanel = new GUIGraphPanel(graph);
        frame = new JFrame("Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(graphPanel);
        frame.pack();
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Dimension d = frame.getContentPane().getSize();
                graphPanel.setGraphDimension(d);
            }
        });
        frame.setBounds(100, 100, 500, 400);
        frame.setVisible(true);
    }


    public static void main(String[] args) {
        MonitorPlugin plugin = new Ping("www.goldb.org");
        new GUIMonitor(plugin, 15, "Network Latency (ping)", "latency (ms)", "time");
    }

}

