/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2013, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * -----------------
 * WaferMapPlot.java
 * -----------------
 *
 * (C) Copyright 2003-2008, by Robert Redburn and Contributors.
 *
 * Original Author:  Robert Redburn;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 25-Nov-2003 : Version 1 contributed by Robert Redburn (DG);
 * 05-May-2005 : Updated draw() method parameters (DG);
 * 10-Jun-2005 : Changed private --> protected for drawChipGrid(),
 *               drawWaferEdge() and getWafterEdge() (DG);
 * 16-Jun-2005 : Added default constructor and setDataset() method (DG);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.renderer.WaferMapRenderer;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.WaferMapDataset;
import org.jfree.ui.RectangleInsets;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

/**
 * A wafer map plot.
 */
public class WaferMapPlot extends Plot implements RendererChangeListener,
        Cloneable, Serializable, Zoomable {

    /** For serialization. */
    private static final long serialVersionUID = 4668320403707308156L;

    /** The default grid line stroke. */
    public static final Stroke DEFAULT_GRIDLINE_STROKE = new BasicStroke(0.5f,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL,
        0.0f,
        new float[] {2.0f, 2.0f},
        0.0f);

    /** The default grid line paint. */
   // public static final Paint DEFAULT_GRIDLINE_PAINT = Color.lightGray;

    /** The default crosshair visibility. */
    //public static final boolean DEFAULT_CROSSHAIR_VISIBLE = false;

    /** The default crosshair stroke. */
    //public static final Stroke DEFAULT_CROSSHAIR_STROKE
      //      = DEFAULT_GRIDLINE_STROKE;

    /** The default crosshair paint. */
    //public static final Paint DEFAULT_CROSSHAIR_PAINT = Color.blue;

    /** The resourceBundle for the localization. */
  //  protected static ResourceBundle localizationResources
    //        = ResourceBundleWrapper.getBundle(
      //              "org.jfree.chart.plot.LocalizationBundle");

    /** The plot orientation.
     *  vertical = notch down
     *  horizontal = notch right
     */
    private PlotOrientation orientation;

    /** The dataset. */
    private WaferMapDataset dataset;

    /**
     * Object responsible for drawing the visual representation of each point
     * on the plot.
     */
    private WaferMapRenderer renderer;

    /**
     * Creates a new plot with no dataset.
     */
    public WaferMapPlot() {
        this(null);
    }

    /**
     * Creates a new plot.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     */
    public WaferMapPlot(WaferMapDataset dataset) {
        this(dataset, null);
    }

    /**
     * Creates a new plot.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     * @param renderer  the renderer (<code>null</code> permitted).
     */
    public WaferMapPlot(WaferMapDataset dataset, WaferMapRenderer renderer) {

        super();

        this.orientation = PlotOrientation.VERTICAL;

        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        this.renderer = renderer;
        if (renderer != null) {
            renderer.setPlot(this);
            renderer.addChangeListener(this);
        }

    }

    /**
     * Returns the plot type as a string.
     *
     * @return A short string describing the type of plot.
     */
    @Override
    public String getPlotType() {
        return ("WMAP_Plot");
    }

    /**
     * Returns the dataset
     *
     * @return The dataset (possibly <code>null</code>).
     */
    public WaferMapDataset getDataset() {
        return this.dataset;
    }

    /**
     * Sets the dataset used by the plot and sends a {@link PlotChangeEvent}
     * to all registered listeners.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     */
    public void setDataset(WaferMapDataset dataset) {
        // if there is an existing dataset, remove the plot from the list of
        // change listeners...
        if (this.dataset != null) {
            this.dataset.removeChangeListener(this);
        }

        // set the new dataset, and register the chart as a change listener...
        this.dataset = dataset;
        if (dataset != null) {
            setDatasetGroup(dataset.getGroup());
            dataset.addChangeListener(this);
        }

        // send a dataset change event to self to trigger plot change event
        datasetChanged(new DatasetChangeEvent(this, dataset));
    }

    /**
     * Sets the item renderer, and notifies all listeners of a change to the
     * plot.  If the renderer is set to <code>null</code>, no chart will be
     * drawn.
     *
     * @param renderer  the new renderer (<code>null</code> permitted).
     */
    public void setRenderer(WaferMapRenderer renderer) {
        if (this.renderer != null) {
            this.renderer.removeChangeListener(this);
        }
        this.renderer = renderer;
        if (renderer != null) {
            renderer.setPlot(this);
        }
        fireChangeEvent();
    }

    /**
     * Draws the wafermap view.
     *
     * @param g2  the graphics device.
     * @param area  the plot area.
     * @param anchor  the anchor point (<code>null</code> permitted).
     * @param state  the plot state.
     * @param info  the plot rendering info.
     */
    @Override
    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
                     PlotState state,
                     PlotRenderingInfo info) {

        // if the plot area is too small, just return...
        boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
        boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
        if (b1 || b2) {
            return;
        }

        // record the plot area...
        if (info != null) {
            info.setPlotArea(area);
        }

        // adjust the drawing area for the plot insets (if any)...
        RectangleInsets insets = getInsets();
        insets.trim(area);
        //todo restore rotate if necessary
       // g2.rotate(this.dataset.getRotation(), area.getCenterX(), area.getCenterY());
        drawChipGrid(g2, area, 100.0);
        drawWaferEdge(g2, area, 100.0);
        //g2.rotate(this.dataset.getRotation(), area.getCenterX(), area.getCenterY());
    }

    protected int getXChips() {
    	 return this.dataset.getMaxChipX() + 2;
    }
    
    protected int getYChips() {
    	return this.dataset.getMaxChipY()+ 2;
    }
    static private DecimalFormat df = new DecimalFormat("0.#####E0");
    public String findChipAtPoint(double x, double y, Rectangle2D plotArea){
    	double[] xValues = this.getChipXValues(plotArea, this.getXChips(), dataset.getChipSpace());
    	double startX = xValues[1];
    	double chipWidth = xValues[0];
    	int ychips = this.getYChips();
        double[] yValues = this.getChipYValues(plotArea, ychips, dataset.getChipSpace());
        double startY = yValues[1];
       // System.out.format("x %f y %f chip x %s startX %f chip y %s startY %f\n",x,y,  xValues, startX, yValues, startY);
        double chipHeight = yValues[0];
    	double chipSpace = dataset.getChipSpace();
    	int chipX = (int)Math.floor((x - startX + chipWidth + chipSpace) / (chipWidth + chipSpace));
    	int chipY = (int)Math.floor((y - startY + chipHeight + chipSpace) / (chipHeight + chipSpace));
    	chipX = chipX - getXOffset() - 1;
    	chipY = ychips - chipY - getYOffset() - 1;
    	return makeValueString(chipX, chipY);
    }
    
    private String makeValueString(int x, int y) {
    	int logicalX = this.getLogicalX(x, y);
    	int logicalY = this.getLogicalY(x, y);
    	Number value = dataset.getChipValue(logicalX, logicalY);
    	StringBuilder sb = new StringBuilder("(");
    	String valueStr = "";
    	if (value instanceof Double){ 
    		if (this.renderer.getPaintScale() == null)
    			valueStr = Integer.toString(value.intValue());
    		else
    			valueStr = df.format(value.doubleValue());
    	}
    	sb.append(logicalX).append(",").append(logicalY).append(") ").append(valueStr);
    	//System.out.println(sb.toString());
    	return sb.toString();
    }
    
    protected int getDisplayX(int logicalX, int logicalY) {
    	return logicalX;
    }
    
    protected int getDisplayY(int logicalX, int logicalY) {
    	return logicalY;
    }
    
    protected int getLogicalX(int x, int y){
    	return x;
    }
    
    protected int getLogicalY(int x, int y) {
    	return y;
    }
      /**
     * Calculates and draws the chip locations on the wafer.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     */
    protected void drawChipGrid(Graphics2D g2, Rectangle2D plotArea, double scalePct) {
        Shape savedClip = g2.getClip();        
        g2.setClip(getWaferEdge(plotArea));
        
        Rectangle2D chip = new Rectangle2D.Double();
        //these are display counts, not logical model
        int xchips = 35;
        int ychips = 20;
        double space = 1d;
        if (this.dataset != null) {
            xchips = getXChips();
            ychips = getYChips();
            space = this.dataset.getChipSpace();
        }
        double[] xValues = getChipXValues(plotArea, xchips, space);
        double[] yValues = getChipYValues(plotArea, ychips, space);
        double startX = xValues[1];
        double chipWidth = xValues[0];
        double startY = yValues[1];
        double chipHeight = yValues[0];
        int xOffset = getXOffset();
        int yOffset = getYOffset();
        //System.out.format("Plot area is %s, startx,y are %f,%f, chip dimensions are %f %f\n", plotArea, startX,startY,
        	//	chipWidth, chipHeight);
        for (int x = 1; x <= xchips; x++) {
            double upperLeftX = (startX - chipWidth) + (chipWidth * x)
                + (space * (x - 1));
            for (int y = 1; y <= ychips; y++) {
                double upperLeftY = (startY - chipHeight) + (chipHeight * y)
                    + (space * (y - 1));
              //  System.out.format("upper left x %f chip x %d upper left y %f chip y %d\n", upperLeftX, x, upperLeftY, y);
                chip.setFrame(upperLeftX, upperLeftY, chipWidth, chipHeight);
                g2.setColor(Color.white);
                //System.out.format("x %d xOffset %d, ychips %d y %d yoffset %d\n", x, xOffset, ychips, y, yOffset);
                Number value = getChipValueFromDisplay(x - 1 - xOffset, ychips - y - 1 - yOffset);
                if (value != null) {
                    g2.setPaint(
                        this.renderer.getChipColor(value)
                    );
                }
                g2.fill(chip);
                g2.setColor(Color.lightGray);
                g2.draw(chip);
            }
           // System.out.format("For upperLeftX is %f x_loc is %d\n", upperLeftX, x - 1 - xOffset );
        }
        g2.setClip(savedClip);
    }
    
    protected int getXOffset() {
    	return dataset.getXOffset();
    }
    
    protected int getYOffset() {
    	return dataset.getYOffset();
    }
    
    protected Number getChipValueFromDisplay(int displayX, int displayY) {
    	return this.dataset.getChipValue(displayX, displayY);
    }
    
    
    protected double[] getChipXValues(Rectangle2D plotArea, int xchips, double space){
    	double startX = plotArea.getX();
        //double startY = plotArea.getY();        
    	double chipWidth = 1d;
        if (plotArea.getWidth() != plotArea.getHeight()) {
            double major, minor;
            if (plotArea.getWidth() > plotArea.getHeight()) {
                major = plotArea.getWidth();
                minor = plotArea.getHeight();
            }
            else {
                major = plotArea.getHeight();
                minor = plotArea.getWidth();
            }
            //set upperLeft point
            if (plotArea.getWidth() == minor) { // x is minor
          //      startY += (major - minor) / 2;
                chipWidth = (plotArea.getWidth() - (space * xchips - 1))
                    / xchips;
             }
            else { // y is minor
                startX += (major - minor) / 2;
                chipWidth = (plotArea.getHeight() - (space * xchips - 1))
                    / xchips;
            }
        }
        double[] xValues = new double[2];
        xValues[0] = chipWidth;
        xValues[1] = startX;
        return xValues;
    }
    
    protected double[] getChipYValues(Rectangle2D plotArea, int ychips, double space){
    	double chipHeight = 1d;
    	double startY = plotArea.getY();
        if (plotArea.getWidth() != plotArea.getHeight()) {
            double major, minor;
            if (plotArea.getWidth() > plotArea.getHeight()) {
                major = plotArea.getWidth();
                minor = plotArea.getHeight();
            }
            else {
                major = plotArea.getHeight();
                minor = plotArea.getWidth();
            }
            //set upperLeft point
            if (plotArea.getWidth() == minor) { // x is minor
                startY += (major - minor) / 2;
                chipHeight = (plotArea.getWidth() - (space * ychips - 1))
                    / ychips;
            }
            else { // y is minor
                //startX += (major - minor) / 2;
                chipHeight = (plotArea.getHeight() - (space * ychips - 1))
                    / ychips;
            }
        }
        double[] yValues = new double[2];
        yValues[0] = chipHeight;
        yValues[1] = startY;
        return yValues;
    }
    
    

    /**
     * Calculates the location of the waferedge.
     *
     * @param plotArea  the plot area.
     *
     * @return The wafer edge.
     */
    protected Ellipse2D getWaferEdge(Rectangle2D plotArea) {
        Ellipse2D edge = new Ellipse2D.Double();
        double diameter = plotArea.getWidth();
        double upperLeftX = plotArea.getX();
        double upperLeftY = plotArea.getY();
        //get major dimension
        if (plotArea.getWidth() != plotArea.getHeight()) {
            double major, minor;
            if (plotArea.getWidth() > plotArea.getHeight()) {
                major = plotArea.getWidth();
                minor = plotArea.getHeight();
            }
            else {
                major = plotArea.getHeight();
                minor = plotArea.getWidth();
            }
            //ellipse diameter is the minor dimension
            diameter = minor;
            //set upperLeft point
            if (plotArea.getWidth() == minor) { // x is minor
                upperLeftY = plotArea.getY() + (major - minor) / 2;
            }
            else { // y is minor
                upperLeftX = plotArea.getX() + (major - minor) / 2;
            }
        }
        edge.setFrame(upperLeftX, upperLeftY, diameter, diameter);
        return edge;
    }

    /**
     * Draws the waferedge, including the notch.
     *
     * @param g2  the graphics device.
     * @param plotArea  the plot area.
     */
    protected void drawWaferEdge(Graphics2D g2, Rectangle2D plotArea, double includePct) {
        // draw the wafer
        Ellipse2D waferEdge = getWaferEdge(plotArea);
        g2.setColor(Color.black);
        g2.draw(waferEdge);
        // calculate and draw the notch
        // horizontal orientation is considered notch right
        // vertical orientation is considered notch down
        int[] x = new int[3];
    	int[] y = new int[3];
    	Polygon notch;
        Rectangle2D waferFrame = waferEdge.getFrame();
        double notchDepth = waferFrame.getWidth() * 0.005;
        if (this.orientation == PlotOrientation.HORIZONTAL) {
        	double upperLeftX = waferFrame.getX() + waferFrame.getWidth()
            	- (notchDepth);
        	double upperLeftY =  waferFrame.getY() + (waferFrame.getHeight() / 2)
        		- (notchDepth);
        	x[0] = (int)upperLeftX;
        	y[0] = (int)(upperLeftY + notchDepth * .5);
        	x[1] = (int)(upperLeftX + notchDepth);
        	y[1] = (int)upperLeftY;
        	x[2] = x[1];
        	y[2] = (int)(upperLeftY + notchDepth);
        }
        else {
        	double upperLeftX = waferFrame.getX() + (waferFrame.getWidth() / 2)  - (notchDepth );
        	double upperLeftY =  waferFrame.getY() + waferFrame.getHeight() - (notchDepth);
        	
        	x[0] = (int)(upperLeftX + notchDepth *.5);
        	y[0] = (int)upperLeftY;
        	x[1] = (int)upperLeftX;
        	y[1] = (int)(upperLeftY + notchDepth);
        	x[2] = (int)(upperLeftX + notchDepth);
        	y[2] = y[1];        	
        }
        notch = new Polygon(x, y, 3);
    	
        g2.setColor(Color.white);
        g2.fillPolygon(notch);
        g2.setColor(Color.black);
        g2.drawPolygon(notch);
    }

    /**
     * Return the legend items from the renderer.
     *
     * @return The legend items.
     */
    @Override
    public LegendItemCollection getLegendItems() {
        return this.renderer.getLegendCollection();
    }

    /**
     * Notifies all registered listeners of a renderer change.
     *
     * @param event  the event.
     */
    @Override
    public void rendererChanged(RendererChangeEvent event) {
        fireChangeEvent();
    }

	@Override
	public boolean isDomainZoomable() {
		return false;
	}

	@Override
	public boolean isRangeZoomable() {
		return false;
	}

	@Override
	public PlotOrientation getOrientation() {
		return this.orientation;
	}

	@Override
	public void zoomDomainAxes(double factor, PlotRenderingInfo state,
			Point2D source) {
		 zoomDomainAxes(factor,state, source, true);
		
	}

	@Override
	public void zoomDomainAxes(double factor, PlotRenderingInfo state,
			Point2D source, boolean useAnchor) {
		zoomDomainAxes(factor, factor, state, source);
		
	}

	@Override
	public void zoomDomainAxes(double lowerPercent, double upperPercent,
			PlotRenderingInfo state, Point2D source) {
		
		
	}

	@Override
	public void zoomRangeAxes(double factor, PlotRenderingInfo state,
			Point2D source) {
		 zoomRangeAxes(factor, state, source, true);
		
	}

	@Override
	public void zoomRangeAxes(double factor, PlotRenderingInfo state,
			Point2D source, boolean useAnchor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void zoomRangeAxes(double lowerPercent, double upperPercent,
			PlotRenderingInfo state, Point2D source) {
		// TODO Auto-generated method stub
		
	}
    
    

}
