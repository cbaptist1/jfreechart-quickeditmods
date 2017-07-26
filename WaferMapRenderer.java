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
 * ---------------------
 * WaferMapRenderer.java
 * ---------------------
 * (C) Copyright 2003-2008, by Robert Redburn and Contributors.
 *
 * Original Author:  Robert Redburn;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 25-Nov-2003 : Version 1, contributed by Robert Redburn.  Changes have been
 *               made to fit the JFreeChart coding style (DG);
 * 20-Apr-2005 : Small update for changes to LegendItem class (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 *
 */

package org.jfree.chart.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.*;

import org.jfree.chart.ChartColor;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.WaferMapPlot;
import org.jfree.data.general.WaferMapDataset;

import com.google.common.collect.Lists;

/**
 * A renderer for wafer map plots.  Provides color managment facilities.
 */
public class WaferMapRenderer extends AbstractRenderer {
	
	private Paint[] defaultColors = {
            Color.GREEN,
            new Color(0xFF, 0x55, 0x55),
			new Color(0x55, 0x55, 0xFF),
            new Color(0x55, 80, 0x55),
            new Color(0xFF, 0xFF, 0x55),
            new Color(0xFF, 0x55, 0xFF),
            new Color(0x55, 0xFF, 0xFF),
            Color.pink,
            Color.gray,
            ChartColor.DARK_RED,
            ChartColor.DARK_BLUE,
            ChartColor.DARK_YELLOW,
            ChartColor.DARK_MAGENTA,
            ChartColor.DARK_CYAN,
            Color.darkGray,
            ChartColor.LIGHT_RED,
            ChartColor.LIGHT_BLUE,
            ChartColor.LIGHT_GREEN,
            ChartColor.LIGHT_YELLOW,
            ChartColor.LIGHT_MAGENTA,
            ChartColor.LIGHT_CYAN,
            Color.lightGray,
            ChartColor.VERY_DARK_RED,
            ChartColor.VERY_DARK_BLUE,
            //ChartColor.VERY_DARK_GREEN,
            ChartColor.VERY_DARK_YELLOW,
            ChartColor.VERY_DARK_MAGENTA,
            ChartColor.VERY_DARK_CYAN,
            ChartColor.VERY_LIGHT_RED,
            ChartColor.VERY_LIGHT_BLUE,
            //ChartColor.VERY_LIGHT_GREEN,
            ChartColor.VERY_LIGHT_YELLOW,
            ChartColor.VERY_LIGHT_MAGENTA,
            ChartColor.VERY_LIGHT_CYAN,
            Color.BLACK
            };

    /** paint index */
    private Map paintIndex;

    /** plot */
    private WaferMapPlot plot;
    
    // added by cab
    private Map<String,String> binDescriptions = null;

    /** paint limit */
    private int paintLimit;

    /** default paint limit */
    private static final int DEFAULT_PAINT_LIMIT = 33;

    /** default multivalue paint calculation */
    public static final int POSITION_INDEX = 0;

    /** The default value index. */
    public static final int VALUE_INDEX = 1;

    public static final int CONSISTENT_INDEX = 2;
    
    public static final int BLUE_ORANGE_INDEX = 3;
    /** paint index method */
    private int paintIndexMethod;

    /**
     * Creates a new renderer.
     */
    public WaferMapRenderer() {
        this(null, null);
    }

    /**
     * Creates a new renderer.
     *
     * @param paintLimit  the paint limit.
     * @param paintIndexMethod  the paint index method.
     */
    public WaferMapRenderer(int paintLimit, int paintIndexMethod) {
        this(Integer.valueOf(paintLimit), Integer.valueOf(paintIndexMethod));
    }

    /**
     * Creates a new renderer.
     *
     * @param paintLimit  the paint limit.
     * @param paintIndexMethod  the paint index method.
     */
    public WaferMapRenderer(Integer paintLimit, Integer paintIndexMethod) {

        super();
        this.paintIndex = new TreeMap();

        if (paintLimit == null) {
            this.paintLimit = DEFAULT_PAINT_LIMIT;
        }
        else {
            this.paintLimit = paintLimit.intValue();
        }

        this.paintIndexMethod = CONSISTENT_INDEX;
        //this.paintIndexMethod = VALUE_INDEX;
        if (paintIndexMethod != null) {
            if (isMethodValid(paintIndexMethod.intValue())) {
                this.paintIndexMethod = paintIndexMethod.intValue();
            }
        }
    }

    /**
     * Verifies that the passed paint index method is valid.
     *
     * @param method  the method.
     *
     * @return <code>true</code> or </code>false</code>.
     */
    private boolean isMethodValid(int method) {
        switch (method) {
            case POSITION_INDEX: return true;
            case VALUE_INDEX:    return true;
            case CONSISTENT_INDEX: return true;
            case BLUE_ORANGE_INDEX : return true;
            default: return false;
        }
    }

    /**
     * Returns the drawing supplier from the plot.
     *
     * @return The drawing supplier.
     */
    @Override
    public DrawingSupplier getDrawingSupplier() {
        DrawingSupplier result = null;
        WaferMapPlot p = getPlot();
        if (p != null) {
            result = p.getDrawingSupplier();
        }
        return result;
    }

    /**
     * Returns the plot.
     *
     * @return The plot.
     */
    public WaferMapPlot getPlot() {
        return this.plot;
    }

    /**
     * Sets the plot and build the paint index.
     *
     * @param plot  the plot.
     */
    public void setPlot(WaferMapPlot plot) {
        this.plot = plot;
        makePaintIndex();
    }

    /**
     * Returns the paint for a given chip value.
     *
     * @param value  the value.
     *
     * @return The paint.
     */
    public Paint getChipColor(Number value) {
    	if (this.paintScale != null)
    		return paintScale.getPaint(value.doubleValue());
        return getSeriesPaint(getPaintIndex(value));
    }

    /**
     * Returns the paint index for a given chip value.
     *
     * @param value  the value.
     *
     * @return The paint index.
     */
    private int getPaintIndex(Number value) {
        return ((Integer) this.paintIndex.get(value)).intValue();
    }

    /**
     * Builds a map of chip values to paint colors.
     * paintlimit is the maximum allowed number of colors.
     */
    private void makePaintIndex() {
        if (this.plot == null) {
            return;
        }
        WaferMapDataset data = this.plot.getDataset();
        Set uniqueValues = data.getUniqueValues();
        if (this.paintIndexMethod == CONSISTENT_INDEX)
        	makeConsistentIndex(uniqueValues);
        else if (this.paintIndexMethod == BLUE_ORANGE_INDEX){
        	makeBlueOrangeIndex(uniqueValues, data);
        }
        else if (uniqueValues.size() <= this.paintLimit) {
            int count = 0; // assign a color for each unique value
            for (Iterator i = uniqueValues.iterator(); i.hasNext();) {
                this.paintIndex.put(i.next(), Integer.valueOf(count++));
            }
        }
        else {
        	Number dataMin = data.getMinValue();
            Number dataMax = data.getMaxValue();
            // more values than paints so map
            // multiple values to the same color
            switch (this.paintIndexMethod) {
                case POSITION_INDEX:
                    makePositionIndex(uniqueValues);
                    break;
                case VALUE_INDEX:
                    makeValueIndex(dataMax, dataMin, uniqueValues);
                    break;
                default:
                    break;
            }
        }
    }

    private LookupPaintScale paintScale = null;
    private void makeBlueOrangeIndex(Set uniqueValues, WaferMapDataset data){
    	Number min = data.getAllGroupsMinValue();
    	Number max = data.getAllGroupsMaxValue();
    	int size = uniqueValues.size();
    	int i = 0;
    	Set<Number> uniqueNumbers = (Set<Number>)uniqueValues;
    	float[] blueVals = new float[3];
    	Color.RGBtoHSB(Color.BLUE.getRed(), Color.BLUE.getGreen(), Color.BLUE.getBlue(), blueVals);
    	float[] c2Vals = new float[3];
    	Color c2 = Color.RED;
    	double doubleMin = min.doubleValue();
    	double doubleMax = max.doubleValue();
    	//if (doubleMin >= doubleMax)
        	//doubleMin = doubleMin - 1.0; //kluge
        
    	if (min == null || Double.isInfinite(doubleMin)||Double.isNaN(doubleMin)){
    		min = 0.0;
    		doubleMin = min.doubleValue();
    	}
    	if (max == null || Double.isInfinite(doubleMax) || Double.isNaN(doubleMax)){
    		max = 1.0;
    		doubleMax = max.doubleValue();
    	}
    	if (max.equals(min)){
    		if (doubleMax > 0.0)
    			min = doubleMax / 2.0;
    		else if (doubleMax == 0.0)
    			min = -1.0;
    		else
    			min = doubleMax * 2;
    		doubleMin = min.doubleValue();
    	}
    	double interval = (max.doubleValue() - min.doubleValue())/1000;
    	System.out.format("Min %f max %f interval %f\n", min, max, interval);
    	Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), c2Vals);
    	paintScale = new LookupPaintScale(min.doubleValue(), max.doubleValue(), Color.RED);
    	//for (Number n : uniqueNumbers){
    	double value = min.doubleValue();
    	while (value <= max.doubleValue()){
    		float hue = blueVals[0] + 
    				(c2Vals[0] - blueVals[0]) * (float)(i+1) / (float)1000 ;
    		//System.out.format("Hue for %f is %f\n", n.floatValue(), hue);
    		Color c = Color.getHSBColor(hue, 1.0f, 1.0f);
    		//this.setSeriesPaint(i, c);
    		//this.paintIndex.put(n, i);
    		//System.out.format("Make %f %s\n", value, c.toString());
    		paintScale.add(value, c);
    		i++;
    		double lastValue = value;
    		value += interval;
    		if (value > max.doubleValue() && lastValue < max.doubleValue())
    			value = max.doubleValue();
    	}
    }
    public PaintScale getPaintScale(){
    	return paintScale;
    }
    private void makeConsistentIndex(Set uniqueValues){
    	//for (Object n : paintIndex.keySet()){
    	//	System.out.println("Existing key is "+n.toString());
    	//}
    	for (int i = 0; i < defaultColors.length; i++){
    		this.setSeriesPaint(i, defaultColors[i]);
    	}
    	for (Iterator i = uniqueValues.iterator(); i.hasNext();){
    		if (i != null){
    			Number value = (Number)i.next();
    			int position = value.intValue() % this.paintLimit - 1;
    			if (position == -1)
    				position = paintLimit;
    			this.paintIndex.put(value, position);
    			//pSystem.out.format("In consistent, paint for %s is %d\n", value.toString(), position);
    		}
    	}
    	//for (Object n : paintIndex.keySet()){
    	//	System.out.println("Existing key is "+n.toString());
    	//}
    	this.paintIndex.put(0.0, paintLimit);
    }
    
    /**
     * Builds the paintindex by assigning colors based on the number
     * of unique values: totalvalues/totalcolors.
     *
     * @param uniqueValues  the set of unique values.
     */
    private void makePositionIndex(Set uniqueValues) {
        int valuesPerColor = (int) Math.ceil(
            (double) uniqueValues.size() / this.paintLimit
        );
        int count = 0; // assign a color for each unique value
        int paint = 0;
        for (Iterator i = uniqueValues.iterator(); i.hasNext();) {
        	Number value = (Number)i.next();
            this.paintIndex.put(value, Integer.valueOf(paint));
            //System.out.format("In position, paint for %s is %d\n", value.toString(), paint);            
            if (++count % valuesPerColor == 0) {
                paint++;
            }
            if (paint > this.paintLimit) {
                paint = this.paintLimit;
            }
        }
    }

    /**
     * Builds the paintindex by assigning colors evenly across the range
     * of values:  maxValue-minValue/totalcolors
     *
     * @param max  the maximum value.
     * @param min  the minumum value.
     * @param uniqueValues  the unique values.
     */
    private void makeValueIndex(Number max, Number min, Set uniqueValues) {
        double valueRange = max.doubleValue() - min.doubleValue();
        double valueStep = valueRange / this.paintLimit;
        int paint = 0;
        double cutPoint = min.doubleValue() + valueStep;
        for (Iterator i = uniqueValues.iterator(); i.hasNext();) {
            Number value = (Number) i.next();
            while (value.doubleValue() > cutPoint) {
                cutPoint += valueStep;
                paint++;
                if (paint > this.paintLimit) {
                    paint = this.paintLimit;
                }
            }
           // System.out.format("In value, paint for %s is %d\n", value.toString(), paint);
            this.paintIndex.put(value, Integer.valueOf(paint));
        }
    }

    private LegendItemCollection makePaintScaleLegendCollection(){
    	LegendItemCollection result = new LegendItemCollection();
    	if (paintScale == null)
    		return result;
    	 WaferMapDataset data = this.plot.getDataset();
         double dataMin = data.getAllGroupsMinValue().doubleValue();
         double dataMax = data.getAllGroupsMaxValue().doubleValue();
         Set uniqueValues = data.getUniqueValues();
         List<Double> uniqueValueList = Lists.newArrayList();
         uniqueValueList.addAll(uniqueValues);
        //try to show 20
        // System.out.format("Min %f max %f lower %f upper %f\n", dataMin, dataMax,
        	//	 paintScale.getLowerBound(), paintScale.getUpperBound());
         double interval = (paintScale.getUpperBound() - paintScale.getLowerBound()) / 20.0;
         //int indexInterval = uniqueValueList.size() / 20;
         double value = paintScale.getLowerBound();
         DecimalFormat df = new DecimalFormat("0.#####E0");
         //for (int index = 0; index < uniqueValueList.size(); ){
         while (value <= paintScale.getUpperBound()){
        	 String label = "";
        	// if (index == 0 || index ==uniqueValueList.size() - 1)
        	// if (value == dataMin || value == dataMax){
        	 label = df.format(value);
        	// }
        	 Shape shape =  new Rectangle2D.Double(-3.0, -5.0, 6.0, 10.0);
             //Shape shape = new Rectangle2D.Double(1d, 1d, 1d, 1d);
        	 Paint paint = paintScale.getPaint(value);
        	// Paint paint = getSeriesPaint(index);
             if (paint == null)
             	paint = Color.black;
             //System.out.format("Got value %f color %s\n", value, paint.toString());
             Paint outlinePaint = Color.black;
             Stroke outlineStroke = DEFAULT_STROKE;
             result.add(new LegendItem(label, "", null,
                     null, shape, paint, outlineStroke, outlinePaint));
             double oldValue = value;
             value += interval;
             if (value > dataMax)
            	 value = dataMax;
             if (value == oldValue)
            	 break;
         }
         return result;
    }
    /**
     * Builds the list of legend entries.  called by getLegendItems in
     * WaferMapPlot to populate the plot legend.
     *
     * @return The legend items.
     */
    public LegendItemCollection getLegendCollection() {
         if (this.paintIndexMethod == BLUE_ORANGE_INDEX)
        	return makePaintScaleLegendCollection();
         LegendItemCollection result = new LegendItemCollection();    
         if (this.paintIndex != null && this.paintIndex.size() > 0) {
           // if (this.paintIndex.size() <= this.paintLimit) {
                for (Iterator i = this.paintIndex.entrySet().iterator();
                     i.hasNext();) {
                    // in this case, every color has a unique value
                    Map.Entry entry =  (Map.Entry) i.next();
                    String label = entry.getKey().toString();
                    String description = (binDescriptions == null) ? label : binDescriptions.get(label);
                    if (description == null){
                    	description = label;
                    }
                    else
                    	label = description;
                    Shape shape =  new Rectangle2D.Double(-3.0, -5.0, 6.0, 10.0);
                    //Shape shape = new Rectangle2D.Double(1d, 1d, 1d, 1d);
                    Paint paint = null;
                    if (this.paintIndex.size() <= this.paintLimit) {
                    	paint = lookupSeriesPaint(
                            ((Integer) entry.getValue()).intValue());
                    }
                    else 
                    	paint = getSeriesPaint(
                                ((Integer) entry.getValue()).intValue()
                            );
                    if (paint == null)
                    	paint = Color.black;
                    Paint outlinePaint = Color.black;
                    Stroke outlineStroke = DEFAULT_STROKE;
                    result.add(new LegendItem(label, description, null,
                            null, shape, paint, outlineStroke, outlinePaint));

                }
           /* }
            else {
                // in this case, every color has a range of values
                Set unique = new HashSet();
                for (Iterator i = this.paintIndex.entrySet().iterator();
                     i.hasNext();) {
                    Map.Entry entry = (Map.Entry) i.next();
                    if (unique.add(entry.getValue())) {
                      //  String label = getMinPaintValue(
                        //    (Integer) entry.getValue()).toString()
                          //  + " - " + getMaxPaintValue(
                            //    (Integer) entry.getValue()).toString();
                    	String label = entry.getKey().toString();
                    	String description = (binDescriptions == null) ? label : binDescriptions.get(label);
                    	if (description == null)
                    		description = label;
                    	else
                    		label = description;
                        Shape shape = new Rectangle2D.Double(1d, 1d, 1d, 1d);
                        Paint paint = getSeriesPaint(
                            ((Integer) entry.getValue()).intValue()
                        );
                        if (paint == null){
                        	System.out.format("Paint is null for %s\n",entry.toString());
                        	paint = Color.black;
                        }//else 
                        	//System.out.format("paint is not null for %s\n",entry.toString());
                        Paint outlinePaint = Color.black;
                        Stroke outlineStroke = DEFAULT_STROKE;

                        result.add(new LegendItem(label, description,
                                null, null, shape, paint, outlineStroke,
                                outlinePaint));
                    }
                } // end foreach map entry
            } // end else*/
        }
        return result;
    }

    /**
     * Returns the minimum chip value assigned to a color
     * in the paintIndex
     *
     * @param index  the index.
     *
     * @return The value.
     */
    private Number getMinPaintValue(Integer index) {
        double minValue = Double.POSITIVE_INFINITY;
        for (Iterator i = this.paintIndex.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            if (((Integer) entry.getValue()).equals(index)) {
                if (((Number) entry.getKey()).doubleValue() < minValue) {
                    minValue = ((Number) entry.getKey()).doubleValue();
                }
            }
        }
        return new Double(minValue);
    }

    /**
     * Returns the maximum chip value assigned to a color
     * in the paintIndex
     *
     * @param index  the index.
     *
     * @return The value
     */
    private Number getMaxPaintValue(Integer index) {
        double maxValue = Double.NEGATIVE_INFINITY;
        for (Iterator i = this.paintIndex.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            if (((Integer) entry.getValue()).equals(index)) {
                if (((Number) entry.getKey()).doubleValue() > maxValue) {
                    maxValue = ((Number) entry.getKey()).doubleValue();
                }
            }
        }
        return new Double(maxValue);
    }

    public void setBinDescriptions(Map<String,String> value){
    	binDescriptions = value;
    }

} // end class wafermaprenderer
