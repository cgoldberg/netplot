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


// Extended JPanel for use in rendering JRobin graphs

import javax.swing.*;
import java.awt.*;
import org.jrobin.graph.RrdGraph;


public class GUIGraphPanel extends JPanel {
	private RrdGraph graph;
	private int width, height;

	GUIGraphPanel(RrdGraph graph) {
            this.graph = graph;
	}

	
	public void paintComponent(Graphics g) {
            try {
                // render the image directly on the Graphics object of the JPanel
                // width and height of 0 means autoscale the graph
                graph.specifyImageSize(true);
                graph.renderImage((Graphics2D)g, width, height);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
	}

	
	public void setGraphDimension(Dimension d) {
            width = d.width;
            height = d.height;
            repaint();
	}
}
