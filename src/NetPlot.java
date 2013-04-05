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
 
 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.l2fprod.gui.plaf.skin.*;
import java.util.Date;
import java.io.IOException;
import org.jrobin.core.*;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraph;



public class NetPlot extends JFrame implements ActionListener {
    
    private static NetPlot frame;
    private Container container;
    private JButton jbtRun = new JButton("Run");
    private JButton jbtStop = new JButton("Stop");
    private JTabbedPane jtp = new JTabbedPane();
    private JTextField jtfHost, jtfInterval;
    private JPanel jpControl = new JPanel(new BorderLayout());
    private JPanel jpControlTab = new JPanel();
    private JPanel jpOutTab = new JPanel(new BorderLayout());
    private static GUIGraphPanel graphPanel;
    private static RrdGraph graph;
    private static RrdGraphDef gDef;
    private static final String rrd = "file.rrd";
    private static long start;
    private static Date end;
    private String graphTitle;
    private String graphXLabel;
    private String graphYLabel;
    private Stopwatch timer = new Stopwatch();
    private static boolean stopMonitor = false;  // flag used to stop MonitorWorker threads
    
    
    public NetPlot() {
        container = getContentPane();
               
        // create top menus
        JMenuBar jmb = new JMenuBar();
        setJMenuBar(jmb);
        
        JMenu fileMenu = new JMenu("File");
            JMenuItem jmiExit = new JMenuItem("Exit");
            fileMenu.add(jmiExit);
            jmiExit.addActionListener(
                new ActionListener() { 
                    public void actionPerformed(ActionEvent ev) {
                        System.exit(0);    
                    }
                }
            );
            
        JMenu opsMenu = new JMenu("Options");
        ButtonGroup group = new ButtonGroup();
            JMenuItem lookToxic = new JRadioButtonMenuItem("Toxic Look");
            opsMenu.add(lookToxic);
            group.add(lookToxic);  // add to radio group 
            lookToxic.setSelected(true);  // default selection
            lookToxic.addActionListener(
                new ActionListener() { 
                    public void actionPerformed(ActionEvent ev) {
                        try {
                            Skin appSkin = SkinLookAndFeel.loadThemePack("lib/toxicthemepack.zip");
                            SkinLookAndFeel.setSkin(appSkin);
                            UIManager.setLookAndFeel(new SkinLookAndFeel());
                            SwingUtilities.updateComponentTreeUI(NetPlot.this);
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            );
            JMenuItem lookJava = new JRadioButtonMenuItem("Java Look");
            opsMenu.add(lookJava); 
            group.add(lookJava);
            lookJava.addActionListener(
                new ActionListener() { 
                    public void actionPerformed(ActionEvent ev) {
                        try {
                            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                            SwingUtilities.updateComponentTreeUI(NetPlot.this);
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            );
            
        JMenu helpMenu = new JMenu("Help");
            JMenuItem jmiHelp = new JMenuItem("About");
            helpMenu.add(jmiHelp);
            jmiHelp.addActionListener(
                new ActionListener() { 
                    public void actionPerformed(ActionEvent ev) {
                        new GUIHelpFrame();    
                    }
                }
        );
            
        jmb.add(fileMenu);
        jmb.add(opsMenu);
        jmb.add(helpMenu); 
        
        // add top level tabs
        container.add(jtp, BorderLayout.CENTER); // outer tabbed pane
        jtp.addTab("Control", jpControlTab);
        jtp.addTab("Output", jpOutTab);
        jpControlTab.add(jpControl, BorderLayout.CENTER);  
        
        // box up the controls
        Box boxControls = Box.createVerticalBox();
            boxControls.setBorder(BorderFactory.createEtchedBorder(0));
            JPanel logoPanel = new JPanel();  // logo
                logoPanel.add(new JLabel(new ImageIcon("lib/NetPlot.png")));
                boxControls.add(logoPanel);
            boxControls.add(Box.createVerticalStrut(15));  // padding above run/stop buttons
            Box controlButtons = Box.createHorizontalBox();
                controlButtons.add(jbtRun);
                controlButtons.add(Box.createHorizontalStrut(25));
                controlButtons.add(jbtStop);
                boxControls.add(controlButtons);
                boxControls.add(Box.createVerticalStrut(25));
        JPanel jpHost = new JPanel();
            jpHost.add(Box.createHorizontalStrut(25)); 
            jtfHost = new JTextField("", 18);
            //jtfHost.setHorizontalAlignment(JTextField.RIGHT);  // right justify text
            jpHost.add(new JLabel("Host / IP:"));
            jpHost.add(jtfHost);
            boxControls.add(jpHost);
        JPanel jpInterval = new JPanel();
            jpInterval.add(Box.createHorizontalStrut(25)); 
            jtfInterval = new JTextField("15", 3); 
            //jtfInterval.setHorizontalAlignment(JTextField.RIGHT);  // right justify text
            jpInterval.add(new JLabel("Collection Interval:"));
            jpInterval.add(jtfInterval);
            boxControls.add(jpInterval);	
        jpControl.add(boxControls);
        
        jbtStop.setEnabled(false);  // start with the Stop button disabled
              
        // register listeners
        jbtRun.addActionListener(this);
        jbtStop.addActionListener(this);
    }

    
    
    //handle events
    public void actionPerformed(ActionEvent ev) {
    	if (ev.getSource() == jbtRun) {
    		jpOutTab.removeAll();  // clear the output tab
    		disableGUIComponents();  // disable run controls while running
    		stopMonitor = false;  // reset the flag to allow MonitorWorker thread to run
    		// spawn thread to run the Network Monitor
			Thread monitorWorker = new Thread(new MonitorWorker());
				monitorWorker.setDaemon(true);  // never let threads prevent the app from exiting
				monitorWorker.start();
    	}
    	
    	if (ev.getSource() == jbtStop) {
    		stopMonitor = true;  // set the flag to stop the  MonitorWorker
            enableGUIComponents();  // enable run controls once stopped
    	}
    }
    
    
    private void enableGUIComponents() {
        // flip the run/stop buttons and re-enable components
        jbtRun.setEnabled(true);
        jbtStop.setEnabled(false);
        jtfHost.setEnabled(true);
        jtfInterval.setEnabled(true);
    }
    
    
    private void disableGUIComponents() {
        // flip the run/stop buttons and disable components
        jbtRun.setEnabled(false);
        jbtStop.setEnabled(true);
        jtfHost.setEnabled(false);
        jtfInterval.setEnabled(false);
    }


    public void createMonitor(MonitorPlugin plugin, long interval, String graphTitle, String graphYLabel, String graphXLabel) {
        this.graphTitle = graphTitle;
        this.graphXLabel = graphXLabel;
        this.graphYLabel = graphYLabel;
        
        long time;
        
        try {	
            this.prepareRrd();
            this.prepareFrame();

            RrdDb rrdDb = new RrdDb(rrd, RrdBackendFactory.getFactory("MEMORY"));
            Sample sample = rrdDb.createSample();
            
            while (!stopMonitor) {  // run the thread until the flag is set for it to stop
                timer.start();
                
                // run the plugin and get the avg ping response time
                double resultTime = plugin.run();
                
                time = Util.getTimestamp();

                // update the rrd
                sample.setTime(time);
                sample.setValue("a", resultTime);
                sample.update();
                
                // set custom graph settings
                gDef.setTimePeriod(start - 5, time);
                end.setTime(time);

                // regenerate the graph
                frame.repaint();
                
                // sleep between collections
                long sleepTime = (1000 * interval) - timer.getElapsedTime();
                if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
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
        gDef.setTimePeriod(start - 5, start);
        gDef.setImageBorder(Color.WHITE, 0);
        gDef.setTitle(graphTitle);
        gDef.setVerticalLabel(graphYLabel);
        gDef.setTimeAxisLabel(graphXLabel);
        gDef.datasource("a", rrd, "a", "AVERAGE", "MEMORY");
        gDef.datasource( "avg", "a", "AVERAGE");
        gDef.area("a", Color.decode("0xb6e4"), "Real");
        gDef.line("avg", Color.RED,  "Average@l", 2);
        gDef.gprint("a", "MIN", "@lmin = @2 ms@l");
        gDef.gprint("a", "MAX", "max = @2 ms@l");
        gDef.gprint("a", "AVERAGE", "avg = @2 ms@l");
        gDef.time("@t", "MMM dd, yyyy -  HH:mm:ss", start);
        gDef.time("to  @t@c", "HH:mm:ss");
        gDef.comment("@l@l");  // some spacing
        
        // create graph
        graph = new RrdGraph(gDef);

        // add swing components
        graphPanel = new GUIGraphPanel(graph);
        graphPanel.setGraphDimension(frame.getContentPane().getSize());        
        jpOutTab.add(graphPanel);
        jtp.setSelectedIndex(1);  // switch to output tab
        
        // listener to re-render when the window is resized
        jpOutTab.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                graphPanel.setGraphDimension(frame.getContentPane().getSize());
            }
        });
    }
	
	
    public static void main(String[] args) throws Exception {
    	Skin appSkin = SkinLookAndFeel.loadThemePack("lib/toxicthemepack.zip");
        SkinLookAndFeel.setSkin(appSkin);
        UIManager.setLookAndFeel(new SkinLookAndFeel());
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        
        frame = new NetPlot();
        frame.setTitle("NetPlot - Network Latency Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(150, 150, 500, 500);   
        frame.setVisible(true); 
    }


    // inner class used to run Network Monitor
    class MonitorWorker implements Runnable {           
        public void run() {
            // get Collection Interval from the UI
            String interval = jtfInterval.getText();
            long intervalSecs;
            
            try {
                if (!interval.matches("[0-9]+")) {
                    throw new NumberFormatException();    
                }
                intervalSecs = Long.parseLong(interval);
            }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Interval must be a positive integer", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                enableGUIComponents();
                return;    
            }
            
            // get Host or IP from the UI
            String host = jtfHost.getText();
            try {
                if (host.equals("")) {
                    throw new Exception();    
                }
            }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "You must specify a Host Name or IP Address", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                enableGUIComponents();
                return;    
            }
            
            start = Util.getTimestamp();
            end = new Date(start);
	
        	MonitorPlugin pingPlugin = new Ping(host);
        	createMonitor(pingPlugin, intervalSecs, "Network Latency (ping)", "latency (ms)", "time");
        }
    }  
    
}
