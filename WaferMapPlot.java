package org.jfree.chart.plot;

import java.awt.BasicStroke;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.LegendItemCollection;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.awt.Font;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import org.jfree.ui.RectangleInsets;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import java.text.DecimalFormat;
import org.jfree.chart.renderer.WaferMapRenderer;
import org.jfree.data.general.WaferMapDataset;
import java.awt.Stroke;
import java.io.Serializable;
import org.jfree.chart.event.RendererChangeListener;

public class WaferMapPlot extends Plot implements RendererChangeListener, Cloneable, Serializable, Zoomable
{
    private static final long serialVersionUID = 4668320403707308156L;
    public static final Stroke DEFAULT_GRIDLINE_STROKE;
    private PlotOrientation orientation;
    private WaferMapDataset dataset;
    private WaferMapRenderer renderer;
    private static DecimalFormat df;
    
    public WaferMapPlot() {
        this(null);
    }
    
    public WaferMapPlot(final WaferMapDataset dataset) {
        this(dataset, null);
    }
    
    public WaferMapPlot(final WaferMapDataset dataset, final WaferMapRenderer renderer) {
        this.orientation = PlotOrientation.VERTICAL;
        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener((DatasetChangeListener)this);
        }
        if ((this.renderer = renderer) != null) {
            renderer.setPlot(this);
            renderer.addChangeListener((RendererChangeListener)this);
        }
    }
    
    public String getPlotType() {
        return "WMAP_Plot";
    }
    
    public WaferMapDataset getDataset() {
        return this.dataset;
    }
    
    public void setDataset(final WaferMapDataset dataset) {
        if (this.dataset != null) {
            this.dataset.removeChangeListener((DatasetChangeListener)this);
        }
        if ((this.dataset = dataset) != null) {
            this.setDatasetGroup(dataset.getGroup());
            dataset.addChangeListener((DatasetChangeListener)this);
        }
        this.datasetChanged(new DatasetChangeEvent((Object)this, (Dataset)dataset));
    }
    
    public void setRenderer(final WaferMapRenderer renderer) {
        if (this.renderer != null) {
            this.renderer.removeChangeListener((RendererChangeListener)this);
        }
        if ((this.renderer = renderer) != null) {
            renderer.setPlot(this);
        }
        this.fireChangeEvent();
    }
    
    public void draw(final Graphics2D g2, final Rectangle2D area, final Point2D anchor, final PlotState state, final PlotRenderingInfo info) {
        final boolean b1 = area.getWidth() <= 10.0;
        final boolean b2 = area.getHeight() <= 10.0;
        if (b1 || b2) {
            return;
        }
        if (info != null) {
            info.setPlotArea(area);
        }
        final RectangleInsets insets = this.getInsets();
        insets.trim(area);
        this.drawChipGrid(g2, area, 100.0);
        this.drawWaferEdge(g2, area, 100.0);
    }
    
    protected int getXChips() {
        return this.dataset.getMaxChipX() + 2;
    }
    
    protected int getYChips() {
        return this.dataset.getMaxChipY() + 2;
    }
    
    public String findChipAtPoint(final double x, final double y, final Rectangle2D plotArea) {
        final double[] xValues = this.getChipXValues(plotArea, this.getXChips(), this.dataset.getChipSpace());
        final double startX = xValues[1];
        final double chipWidth = xValues[0];
        final int ychips = this.getYChips();
        final double[] yValues = this.getChipYValues(plotArea, ychips, this.dataset.getChipSpace());
        final double startY = yValues[1];
        final double chipHeight = yValues[0];
        final double chipSpace = this.dataset.getChipSpace();
        int chipX = (int)Math.floor((x - startX + chipWidth + chipSpace) / (chipWidth + chipSpace));
        int chipY = (int)Math.floor((y - startY + chipHeight + chipSpace) / (chipHeight + chipSpace));
        chipX = chipX - this.getXOffset() - 1;
        chipY = ychips - chipY - this.getYOffset() - 0;
        return this.makeValueString(chipX, chipY);
    }
    
    private String makeValueString(final int x, final int y) {
        final int logicalX = this.getLogicalX(x, y);
        final int logicalY = this.getLogicalY(x, y);
        final Number value = this.dataset.getChipValue(logicalX, logicalY);
        if (value == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder("(");
        String valueStr = "";
        if (value instanceof Double) {
            if (this.renderer.getPaintScale() == null) {
                valueStr = Integer.toString(value.intValue());
            }
            else {
                valueStr = WaferMapPlot.df.format(value.doubleValue());
            }
        }
        sb.append(logicalX).append(",").append(logicalY).append(") ").append(valueStr);
        return sb.toString();
    }
    
    protected int getDisplayX(final int logicalX, final int logicalY) {
        return logicalX;
    }
    
    protected int getDisplayY(final int logicalX, final int logicalY) {
        return logicalY;
    }
    
    protected int getLogicalX(final int x, final int y) {
        return x;
    }
    
    protected int getLogicalY(final int x, final int y) {
        return y;
    }
    
    protected void drawChipGrid(final Graphics2D g2, final Rectangle2D plotArea, final double scalePct) {
        final Shape savedClip = g2.getClip();
        if (!this.renderer.isShowSurroundingGrid()) {
            g2.setClip(this.getWaferEdge(plotArea));
        }
        final Rectangle2D chip = new Rectangle2D.Double();
        int xchips = 35;
        int ychips = 20;
        double space = 1.0;
        if (this.dataset != null) {
            xchips = this.getXChips();
            ychips = this.getYChips();
            space = this.dataset.getChipSpace();
        }
        final double[] xValues = this.getChipXValues(plotArea, xchips, space);
        final double[] yValues = this.getChipYValues(plotArea, ychips, space);
        final double startX = xValues[1];
        final double chipWidth = xValues[0];
        final double startY = yValues[1];
        final double chipHeight = yValues[0];
        final int xOffset = this.getXOffset();
        final int yOffset = this.getYOffset();
        final boolean printChipValue = this.renderer.isPrintChipValue();
        final Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        g2.setFont(g2.getFont().deriveFont(0, 8.0f));
        final float printXOffset = (float)Math.max(1.0, chipWidth * 0.35);
        final float printYOffset = (float)Math.max(1.0, chipHeight * 0.3);
        for (int x = 1; x <= xchips; ++x) {
            final double upperLeftX = startX - chipWidth + chipWidth * x + space * (x - 1);
            for (int y = 1; y <= ychips; ++y) {
                final double upperLeftY = startY - chipHeight + chipHeight * y + space * (y - 1);
                chip.setFrame(upperLeftX, upperLeftY, chipWidth, chipHeight);
                g2.setColor(Color.white);
                final Number value = this.getChipValueFromDisplay(x - 1 - xOffset, ychips - y - 0 - yOffset);
                if (value != null) {
                    g2.setPaint(this.renderer.getChipColor(value));
                }
                g2.fill(chip);
                if (printChipValue && value != null) {
                    g2.setColor(Color.black);
                    g2.drawString(Integer.toString(value.intValue()), (float)(printXOffset + upperLeftX), (float)(upperLeftY + chipHeight - printYOffset));
                }
                g2.setColor(Color.lightGray);
                g2.draw(chip);
            }
        }
        g2.setClip(savedClip);
    }
    
    protected int getXOffset() {
        return this.dataset.getXOffset();
    }
    
    protected int getYOffset() {
        return this.dataset.getYOffset();
    }
    
    protected Number getChipValueFromDisplay(final int displayX, final int displayY) {
        return this.dataset.getChipValue(displayX, displayY);
    }
    
    protected double[] getChipXValues(final Rectangle2D plotArea, final int xchips, final double space) {
        double startX = plotArea.getX();
        double chipWidth = 1.0;
        if (plotArea.getWidth() != plotArea.getHeight()) {
            double major;
            double minor;
            if (plotArea.getWidth() > plotArea.getHeight()) {
                major = plotArea.getWidth();
                minor = plotArea.getHeight();
            }
            else {
                major = plotArea.getHeight();
                minor = plotArea.getWidth();
            }
            if (plotArea.getWidth() == minor) {
                chipWidth = (plotArea.getWidth() - (space * xchips - 1.0)) / xchips;
            }
            else {
                startX += (major - minor) / 2.0;
                chipWidth = (plotArea.getHeight() - (space * xchips - 1.0)) / xchips;
            }
        }
        final double[] xValues = { chipWidth, startX };
        return xValues;
    }
    
    protected double[] getChipYValues(final Rectangle2D plotArea, final int ychips, final double space) {
        double chipHeight = 1.0;
        double startY = plotArea.getY();
        if (plotArea.getWidth() != plotArea.getHeight()) {
            double major;
            double minor;
            if (plotArea.getWidth() > plotArea.getHeight()) {
                major = plotArea.getWidth();
                minor = plotArea.getHeight();
            }
            else {
                major = plotArea.getHeight();
                minor = plotArea.getWidth();
            }
            if (plotArea.getWidth() == minor) {
                startY += (major - minor) / 2.0;
                chipHeight = (plotArea.getWidth() - (space * ychips - 1.0)) / ychips;
            }
            else {
                chipHeight = (plotArea.getHeight() - (space * ychips - 1.0)) / ychips;
            }
        }
        final double[] yValues = { chipHeight, startY };
        return yValues;
    }
    
    protected Ellipse2D getWaferEdge(final Rectangle2D plotArea) {
        final Ellipse2D edge = new Ellipse2D.Double();
        double diameter = plotArea.getWidth();
        double upperLeftX = plotArea.getX();
        double upperLeftY = plotArea.getY();
        if (plotArea.getWidth() != plotArea.getHeight()) {
            double major;
            double minor;
            if (plotArea.getWidth() > plotArea.getHeight()) {
                major = plotArea.getWidth();
                minor = plotArea.getHeight();
            }
            else {
                major = plotArea.getHeight();
                minor = plotArea.getWidth();
            }
            diameter = minor;
            if (plotArea.getWidth() == minor) {
                upperLeftY = plotArea.getY() + (major - minor) / 2.0;
            }
            else {
                upperLeftX = plotArea.getX() + (major - minor) / 2.0;
            }
        }
        edge.setFrame(upperLeftX, upperLeftY, diameter, diameter);
        return edge;
    }
    
    protected void drawWaferEdge(final Graphics2D g2, final Rectangle2D plotArea, final double includePct) {
        final Ellipse2D waferEdge = this.getWaferEdge(plotArea);
        g2.setColor(Color.black);
        g2.draw(waferEdge);
        final int[] x = new int[3];
        final int[] y = new int[3];
        final Rectangle2D waferFrame = waferEdge.getFrame();
        final double notchDepth = waferFrame.getWidth() * 0.005;
        if (this.orientation == PlotOrientation.HORIZONTAL) {
            final double upperLeftX = waferFrame.getX() + waferFrame.getWidth() - notchDepth;
            final double upperLeftY = waferFrame.getY() + waferFrame.getHeight() / 2.0 - notchDepth;
            x[0] = (int)upperLeftX;
            y[0] = (int)(upperLeftY + notchDepth * 0.5);
            x[1] = (int)(upperLeftX + notchDepth);
            y[1] = (int)upperLeftY;
            x[2] = x[1];
            y[2] = (int)(upperLeftY + notchDepth);
        }
        else {
            final double upperLeftX = waferFrame.getX() + waferFrame.getWidth() / 2.0 - notchDepth;
            final double upperLeftY = waferFrame.getY() + waferFrame.getHeight() - notchDepth;
            x[0] = (int)(upperLeftX + notchDepth * 0.5);
            y[0] = (int)upperLeftY;
            x[1] = (int)upperLeftX;
            y[1] = (int)(upperLeftY + notchDepth);
            x[2] = (int)(upperLeftX + notchDepth);
            y[2] = y[1];
        }
        final Polygon notch = new Polygon(x, y, 3);
        g2.setColor(Color.white);
        g2.fillPolygon(notch);
        g2.setColor(Color.black);
        g2.drawPolygon(notch);
    }
    
    public LegendItemCollection getLegendItems() {
        return this.renderer.getLegendCollection();
    }
    
    public void rendererChanged(final RendererChangeEvent event) {
        this.fireChangeEvent();
    }
    
    public boolean isDomainZoomable() {
        return false;
    }
    
    public boolean isRangeZoomable() {
        return false;
    }
    
    public PlotOrientation getOrientation() {
        return this.orientation;
    }
    
    public void zoomDomainAxes(final double factor, final PlotRenderingInfo state, final Point2D source) {
        this.zoomDomainAxes(factor, state, source, true);
    }
    
    public void zoomDomainAxes(final double factor, final PlotRenderingInfo state, final Point2D source, final boolean useAnchor) {
        this.zoomDomainAxes(factor, factor, state, source);
    }
    
    public void zoomDomainAxes(final double lowerPercent, final double upperPercent, final PlotRenderingInfo state, final Point2D source) {
    }
    
    public void zoomRangeAxes(final double factor, final PlotRenderingInfo state, final Point2D source) {
        this.zoomRangeAxes(factor, state, source, true);
    }
    
    public void zoomRangeAxes(final double factor, final PlotRenderingInfo state, final Point2D source, final boolean useAnchor) {
    }
    
    public void zoomRangeAxes(final double lowerPercent, final double upperPercent, final PlotRenderingInfo state, final Point2D source) {
    }
    
    static {
        DEFAULT_GRIDLINE_STROKE = new BasicStroke(0.5f, 0, 2, 0.0f, new float[] { 2.0f, 2.0f }, 0.0f);
        WaferMapPlot.df = new DecimalFormat("0.#####E0");
    }
}