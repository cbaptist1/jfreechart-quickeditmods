package org.jfree.chart.renderer;

import java.awt.Stroke;
import java.awt.Shape;
import java.util.List;
import org.jfree.chart.LegendItem;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Collection;
import com.google.common.collect.Lists;
import org.jfree.chart.LegendItemCollection;
import java.util.Iterator;
import java.util.Set;
import org.jfree.data.general.WaferMapDataset;
import org.jfree.chart.plot.DrawingSupplier;
import java.util.TreeMap;
import org.jfree.chart.ChartColor;
import java.awt.Color;
import org.jfree.chart.plot.WaferMapPlot;
import java.util.Map;
import java.awt.Paint;

public class WaferMapRenderer extends AbstractRenderer
{
    private Paint[] defaultColors;
    private Map<Number,Integer> paintIndex;
    private WaferMapPlot plot;
    private Map<String, String> binDescriptions;
    private int paintLimit;
    private static final int DEFAULT_PAINT_LIMIT = 33;
    public static final int POSITION_INDEX = 0;
    public static final int VALUE_INDEX = 1;
    public static final int CONSISTENT_INDEX = 2;
    public static final int BLUE_ORANGE_INDEX = 3;
    private int paintIndexMethod;
    private boolean printChipValue;
    private boolean showSurroundingGrid;
    private LookupPaintScale paintScale;
    
    public WaferMapRenderer() {
        this(null, null);
    }
    
    public WaferMapRenderer(final int paintLimit, final int paintIndexMethod) {
        this(Integer.valueOf(paintLimit), Integer.valueOf(paintIndexMethod));
    }
    
    public WaferMapRenderer(final Integer paintLimit, final Integer paintIndexMethod) {
        this.defaultColors = new Paint[] { Color.GREEN, new Color(255, 85, 85), new Color(85, 85, 255), new Color(85, 80, 85), new Color(255, 255, 85), new Color(255, 85, 255), new Color(85, 255, 255), Color.pink, Color.gray, ChartColor.DARK_RED, ChartColor.DARK_BLUE, ChartColor.DARK_YELLOW, ChartColor.DARK_MAGENTA, ChartColor.DARK_CYAN, Color.darkGray, ChartColor.LIGHT_RED, ChartColor.LIGHT_BLUE, ChartColor.LIGHT_GREEN, ChartColor.LIGHT_YELLOW, ChartColor.LIGHT_MAGENTA, ChartColor.LIGHT_CYAN, Color.lightGray, ChartColor.VERY_DARK_RED, ChartColor.VERY_DARK_BLUE, ChartColor.VERY_DARK_YELLOW, ChartColor.VERY_DARK_MAGENTA, ChartColor.VERY_DARK_CYAN, ChartColor.VERY_LIGHT_RED, ChartColor.VERY_LIGHT_BLUE, ChartColor.VERY_LIGHT_YELLOW, ChartColor.VERY_LIGHT_MAGENTA, ChartColor.VERY_LIGHT_CYAN, Color.BLACK };
        this.binDescriptions = null;
        this.printChipValue = false;
        this.showSurroundingGrid = false;
        this.paintScale = null;
        this.paintIndex = new TreeMap<>();
        if (paintLimit == null) {
            this.paintLimit = 33;
        }
        else {
            this.paintLimit = paintLimit;
        }
        this.paintIndexMethod = 2;
        if (paintIndexMethod != null && this.isMethodValid(paintIndexMethod)) {
            this.paintIndexMethod = paintIndexMethod;
        }
    }
    
    private boolean isMethodValid(final int method) {
        switch (method) {
            case 0: {
                return true;
            }
            case 1: {
                return true;
            }
            case 2: {
                return true;
            }
            case 3: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public DrawingSupplier getDrawingSupplier() {
        DrawingSupplier result = null;
        final WaferMapPlot p = this.getPlot();
        if (p != null) {
            result = p.getDrawingSupplier();
        }
        return result;
    }
    
    public WaferMapPlot getPlot() {
        return this.plot;
    }
    
    public void setPlot(final WaferMapPlot plot) {
        this.plot = plot;
        this.makePaintIndex();
    }
    
    public Paint getChipColor(final Number value) {
        if (this.paintScale != null) {
            return this.paintScale.getPaint(value.doubleValue());
        }
        return this.getSeriesPaint(this.getPaintIndex(value));
    }
    
    private int getPaintIndex(final Number value) {
        return this.paintIndex.get(value);
    }
    
    private void makePaintIndex() {
        if (this.plot == null) {
            return;
        }
        final WaferMapDataset data = this.plot.getDataset();
        final Set<Number> uniqueValues = data.getUniqueValues();
        if (this.paintIndexMethod == 2) {
            this.makeConsistentIndex(uniqueValues);
        }
        else if (this.paintIndexMethod == 3) {
            this.makeBlueOrangeIndex(uniqueValues, data);
        }
        else if (uniqueValues.size() <= this.paintLimit) {
            int count = 0;
            final Iterator<Number> i = uniqueValues.iterator();
            while (i.hasNext()) {
                this.paintIndex.put(i.next(), count++);
            }
        }
        else {
            final Number dataMin = data.getMinValue();
            final Number dataMax = data.getMaxValue();
            switch (this.paintIndexMethod) {
                case 0: {
                    this.makePositionIndex(uniqueValues);
                    break;
                }
                case 1: {
                    this.makeValueIndex(dataMax, dataMin, uniqueValues);
                    break;
                }
            }
        }
    }
    
    private void makeBlueOrangeIndex(final Set uniqueValues, final WaferMapDataset data) {
        Number min = data.getAllGroupsMinValue();
        Number max = data.getAllGroupsMaxValue();
        final int size = uniqueValues.size();
        int i = 0;
        final Set<Number> uniqueNumbers = (Set<Number>)uniqueValues;
        final float[] blueVals = new float[3];
        Color.RGBtoHSB(Color.BLUE.getRed(), Color.BLUE.getGreen(), Color.BLUE.getBlue(), blueVals);
        final float[] c2Vals = new float[3];
        final Color c2 = Color.RED;
        double doubleMin = min.doubleValue();
        double doubleMax = max.doubleValue();
        if (min == null || Double.isInfinite(doubleMin) || Double.isNaN(doubleMin)) {
            min = 0.0;
            doubleMin = min.doubleValue();
        }
        if (max == null || Double.isInfinite(doubleMax) || Double.isNaN(doubleMax)) {
            max = 1.0;
            doubleMax = max.doubleValue();
        }
        if (max.equals(min)) {
            if (doubleMax > 0.0) {
                min = doubleMax / 2.0;
            }
            else if (doubleMax == 0.0) {
                min = -1.0;
            }
            else {
                min = doubleMax * 2.0;
            }
            doubleMin = min.doubleValue();
        }
        final double interval = (max.doubleValue() - min.doubleValue()) / 1000.0;
        Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), c2Vals);
        this.paintScale = new LookupPaintScale(min.doubleValue(), max.doubleValue(), (Paint)Color.RED);
        for (double value = min.doubleValue(); value <= max.doubleValue(); value = max.doubleValue()) {
            final float hue = blueVals[0] + (c2Vals[0] - blueVals[0]) * (i + 1) / 1000.0f;
            final Color c3 = Color.getHSBColor(hue, 1.0f, 1.0f);
            this.paintScale.add(value, (Paint)c3);
            ++i;
            final double lastValue = value;
            value += interval;
            if (value > max.doubleValue() && lastValue < max.doubleValue()) {}
        }
    }
    
    public PaintScale getPaintScale() {
        return (PaintScale)this.paintScale;
    }
    
    public void setColorForIndex(final int binNumber, final Paint p) {
        final Integer index = this.paintIndex.get((double)binNumber);
        if (index != null) {
            this.setSeriesPaint((int)index, p);
        }
    }
    
    private void makeConsistentIndex(final Set<Number> uniqueValues) {
        for (int i = 0; i < this.defaultColors.length; ++i) {
            this.setSeriesPaint(i, this.defaultColors[i]);
        }
        final Iterator<Number> j = uniqueValues.iterator();
        while (j.hasNext()) {
            if (j != null) {
                final Number value = j.next();
                int position = value.intValue() % this.paintLimit - 1;
                if (position == -1) {
                    position = this.paintLimit;
                }
                this.paintIndex.put(value, position);
            }
        }
        this.paintIndex.put(0.0, this.paintLimit);
    }
    
    private void makePositionIndex(final Set<Number> uniqueValues) {
        final int valuesPerColor = (int)Math.ceil(uniqueValues.size() / (double)this.paintLimit);
        int count = 0;
        int paint = 0;
        for (final Number value : uniqueValues) {
            this.paintIndex.put(value, paint);
            if (++count % valuesPerColor == 0) {
                ++paint;
            }
            if (paint > this.paintLimit) {
                paint = this.paintLimit;
            }
        }
    }
    
    private void makeValueIndex(final Number max, final Number min, final Set<Number> uniqueValues) {
        final double valueRange = max.doubleValue() - min.doubleValue();
        final double valueStep = valueRange / this.paintLimit;
        int paint = 0;
        double cutPoint = min.doubleValue() + valueStep;
        for (final Number value : uniqueValues) {
            while (value.doubleValue() > cutPoint) {
                cutPoint += valueStep;
                if (++paint > this.paintLimit) {
                    paint = this.paintLimit;
                }
            }
            this.paintIndex.put(value, paint);
        }
    }
    
    private LegendItemCollection makePaintScaleLegendCollection() {
        final LegendItemCollection result = new LegendItemCollection();
        if (this.paintScale == null) {
            return result;
        }
        final WaferMapDataset data = this.plot.getDataset();
        final double dataMin = data.getAllGroupsMinValue();
        final double dataMax = data.getAllGroupsMaxValue();
        final Set<Number> uniqueValues = data.getUniqueValues();
        final List<Double> uniqueValueList = Lists.newArrayList();
		for (Number n : uniqueValues){
			uniqueValueList.add(n.doubleValue());
		}
        final double interval = (this.paintScale.getUpperBound() - this.paintScale.getLowerBound()) / 20.0;
        double value = this.paintScale.getLowerBound();
        final DecimalFormat df = new DecimalFormat("0.#####E0");
        while (value <= this.paintScale.getUpperBound()) {
            String label = "";
            label = df.format(value);
            final Shape shape = new Rectangle2D.Double(-3.0, -5.0, 6.0, 10.0);
            Paint paint = this.paintScale.getPaint(value);
            if (paint == null) {
                paint = Color.black;
            }
            final Paint outlinePaint = Color.black;
            final Stroke outlineStroke = WaferMapRenderer.DEFAULT_STROKE;
            result.add(new LegendItem(label, "", (String)null, (String)null, shape, paint, outlineStroke, outlinePaint));
            final double oldValue = value;
            value += interval;
            if (value > dataMax) {
                value = dataMax;
            }
            if (value == oldValue) {
                break;
            }
        }
        return result;
    }
    
    public LegendItemCollection getLegendCollection() {
        if (this.paintIndexMethod == 3) {
            return this.makePaintScaleLegendCollection();
        }
        final LegendItemCollection result = new LegendItemCollection();
        if (this.paintIndex != null && this.paintIndex.size() > 0) {
            for (final Map.Entry entry : this.paintIndex.entrySet()) {
                String label = entry.getKey().toString();
                String description = (this.binDescriptions == null) ? label : this.binDescriptions.get(label);
                if (description == null) {
                    description = label;
                }
                else {
                    label = description;
                }
                final Shape shape = new Rectangle2D.Double(-3.0, -5.0, 6.0, 10.0);
                Paint paint = null;
                if (this.paintIndex.size() <= this.paintLimit) {
                    paint = this.lookupSeriesPaint((int)entry.getValue());
                }
                else {
                    paint = this.getSeriesPaint((int)entry.getValue());
                }
                if (paint == null) {
                    paint = Color.black;
                }
                final Paint outlinePaint = Color.black;
                final Stroke outlineStroke = WaferMapRenderer.DEFAULT_STROKE;
                result.add(new LegendItem(label, description, (String)null, (String)null, shape, paint, outlineStroke, outlinePaint));
            }
        }
        return result;
    }
    
    private Number getMinPaintValue(final Integer index) {
        double minValue = Double.POSITIVE_INFINITY;
        for (final Map.Entry<Number,Integer> entry : this.paintIndex.entrySet()) {
            if (entry.getValue().equals(index) && entry.getKey().doubleValue() < minValue) {
                minValue = entry.getKey().doubleValue();
            }
        }
        return new Double(minValue);
    }
    
    private Number getMaxPaintValue(final Integer index) {
        double maxValue = Double.NEGATIVE_INFINITY;
        for (final Map.Entry<Number,Integer> entry : this.paintIndex.entrySet()) {
            if (entry.getValue().equals(index) && entry.getKey().doubleValue() > maxValue) {
                maxValue = entry.getKey().doubleValue();
            }
        }
        return new Double(maxValue);
    }
    
    public void setBinDescriptions(final Map<String, String> value) {
        this.binDescriptions = value;
    }
    
    public boolean isPrintChipValue() {
        return this.printChipValue;
    }
    
    public void setPrintChipValue(final boolean printChipValue) {
        this.printChipValue = printChipValue;
    }
    
    public boolean isShowSurroundingGrid() {
        return this.showSurroundingGrid;
    }
    
    public void setShowSurroundingGrid(final boolean showSurroundingGrid) {
        this.showSurroundingGrid = showSurroundingGrid;
    }
}