/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.gui.profileplot;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import no.uib.jexpress_modularized.core.visualization.charts.ChartLabel;
import no.uib.jexpress_modularized.core.visualization.charts.RotChartLabel;

/**
 *
 * @author Yehia Farag
 */
public class LabelAxis extends Object implements Serializable {

    String[][] labels;
    boolean[] visibleLabels;
    Component drawer;
    boolean temp = true;
    private double DEG2RAD = Math.PI / 180;
    public int rotation = 0;
    int offset = 10;
    public Font font = new Font("ARIAL", 0, 10);
    public int[] ylocations;
    public int transparency = 255;
    public double spaceBetween = 0.5;
    public int minSpace = 0;
    public int minimumSize = 300;
    Color[] groupColors;
    public boolean dropFirstGridLine = false;
    public boolean dropLastGridLine = false;
    /**
     *
     * Constant flagging Horizontal Axis
     *
     */
    final int HORIZONTAL = 0;
    /**
     *
     * Constant flagging Vertical Axis
     *
     */
    final int VERTICAL = 1;
    /**
     *
     * Constant flagging Axis position on the graph. * Left side => Vertical
     *
     */
    public final int LEFT = 2;
    /**
     *
     * Constant flagging Axis position on the graph. * Right side => Vertical
     *
     */
    public final int RIGHT = 3;
    /**
     *
     * Constant flagging Axis position on the graph. * Top side => Horizontal
     *
     */
    public final int TOP = 4;
    /**
     *
     * Constant flagging Axis position on the graph. * Bottom side => Horizontal
     *
     */
    public final int BOTTOM = 5;
    /**
     *
     * The first guess on the number of Labeled Major tick marks.
     *
     */
    public int NUMBER_OF_TICS = 4;
    public boolean TICS_IN_BOTH_ENDS = true;
    /*
     *
     ***********************
     *
     ** Public Variables      *
     *********************
     */
    //Space above the labels
    public int topspace = 0;
    /**
     *
     * If <i>true</i> draw a grid positioned on major ticks over the graph
     *
     */
    public boolean paintGrid = true;
    /**
     *
     * If <i>true</i> draw a line positioned on the Zero label tick mark.
     *
     */
    public boolean drawzero = false;
    /**
     *
     * Color of the grid
     *
     */
    public Color gridcolor = null;
    /**
     *
     * Color of the line at the Zero label
     *
     */
    public Color zerocolor = null;
    /**
     *
     * Default value <i>true</i>. Normally never changed. If set <i>false</I>
     *
     * the Axis draw method exits without drawing the axis.
     *
     * @see Axis#drawAxis()
     *
     */
    public boolean redraw = true;
    /**
     *
     * Rescale the axis so that labels fall at the end of the Axis. Default
     *
     * value <i>false</i>.
     *
     */
    public boolean force_end_labels = true;
    /**
     *
     * Size in pixels of the major tick marks
     *
     */
    public int major_tic_size = 7;
    /**
     *
     * Size in pixels of the minor tick marks
     *
     */
    public int minor_tic_size = 4;
    /**
     *
     * Number of minor tick marks between major tick marks
     *
     */
    public int minor_tic_count = 1;
    /**
     *
     * Color of the Axis.
     *
     */
    public Color axiscolor;
    /**
     *
     * Minimum data value of the axis. This is the value used to scale
     *
     * data into the data window. This is the value to alter to force
     *
     * a rescaling of the data window.
     *
     */
    public double minimum;
    /**
     *
     * Maximum data value of the axis. This is the value used to scale
     *
     * data into the data window. This is the value to alter to force
     *
     * a rescaling of the data window.
     *
     */
    public double maximum;
    /**
     *
     * Before the Axis can be positioned correctly and drawn the data window
     *
     * needs to be calculated and passed to the Axis.
     *
     */
    public Dimension data_window = new Dimension(0, 0);
    /**
     *
     * The graph canvas this axis is attached to (if it is attached to any)
     *
     * @see graph.Graph2D
     *
     */
    //    public Graph2D g2d = null;
    /*
     *
     ***********************
     *
     ** Protected Variables      *
     *********************
     */
    /**
     *
     * The position in pixels of the minimum point of the axis line
     *
     */
    protected Point amin;
    /**
     *
     * The position in pixels of the maximum point of the axis line
     *
     */
    protected Point amax;
    /**
     *
     * The orientation of the axis. Either Axis.HORIZONTAL or
     *
     * Axis.VERTICAL
     *
     */
    protected int orientation;
    /**
     *
     * The position of the axis. Either Axis.LEFT, Axis.RIGHT, Axis.TOP, or
     *
     * Axis.BOTTOM
     *
     */
    protected int position;
    /**
     *
     * The width of the Axis. Where width for a horizontal axis is really * the
     * height
     *
     */
    protected int width = 0;
    /**
     *
     * ChartLabel class to contain the title of the axis.
     *
     */
    protected RotChartLabel title = new RotChartLabel("-");
    /**
     *
     * ChartLabel class to hold the labels before printing.
     *
     */
    protected RotChartLabel label = new RotChartLabel("0");
    /**
     *
     * ChartLabel class to hold the label's exponent (if it has one).
     *
     */
    protected RotChartLabel exponent = new RotChartLabel();
    /**
     *
     * The width of the maximum label. Used to position a Vertical Axis.
     *
     */
    protected int max_label_width = 0;
    /**
     *
     * Vector containing a list of attached DataSets
     *
     */
    //protected Vector dataset = new Vector();
    /**
     *
     * String to contain the labels.
     *
     */
    protected String label_string[] = null;
    /**
     *
     * The actual values of the axis labels
     *
     */
    protected float label_value[] = null;
    /**
     *
     * The starting value of the labels
     *
     */
    protected double label_start = 0.0;
    /**
     *
     * The increment between labels
     *
     */
    protected double label_step = 0.0;
    /**
     *
     * The label exponent
     *
     */
    protected int label_exponent = 0;
    /**
     *
     * The number of labels required
     *
     */
    protected int label_count = 4;
    /**
     *
     * Initial guess for the number of labels required
     *
     */
    protected int guess_label_number = 4;
    private int visiblelabs = 0;
    /**
     *
     * If true the axis range must be manually set by setting the
     *
     * Axis.minimum and Axis.maximum variables. The default is false.
     *
     * The default action is for the axis range to be calculated everytime
     *
     * a dataset is attached.
     *
     */
    protected boolean manualRange = true;

    public final void setLabels(String[][] labels, boolean[] visibleLabels) {

        this.labels = labels;

        if (visibleLabels == null) {

            if (labels.length > 0 && labels[0].length > 0) {
                visibleLabels = new boolean[labels[0].length + 2];
            } else {
                visibleLabels = new boolean[2];
            }

            for (int i = 0; i < visibleLabels.length; i++) {
                visibleLabels[i] = true;
            }

        }

        this.visibleLabels = visibleLabels;

        visiblelabs = visibleLabelCount();

    }

    /*
     *
     *********************
     *
     ** Constructors
     *
     *******************
     */
    /**
     *
     * Instantiate the class. The defalt type is a Horizontal axis
     *
     * @param drawer
     * @param labels
     * @param visibleLabels positioned at the bottom of the graph.
     *
     */
    public LabelAxis(Component drawer, String[][] labels, boolean[] visibleLabels) {

        this.drawer = drawer;

        setLabels(labels, visibleLabels);

        orientation = HORIZONTAL;

        position = BOTTOM;

    }

    public void setGroupColors(Color[] groupColors) {

        this.groupColors = groupColors;

    }

    public int visibleLabelCount() {

        int ret = 0;

        for (int i = 1; i < visibleLabels.length - 1; i++) {

            if (visibleLabels[i]) {
                ret++;
            }

        }

        return ret;

    }

    /**
     *
     * Instantiate the class. Setting the position.
     *
     * @param p Set the axis position. Must be one of Axis.BOTTOM,
     * @param drawer
     * @param labels
     * @param visibleLabels
     *
     * Axis.TOP, Axis.LEFT, Axis.RIGHT, Axis.HORIZONTAL or Axis.VERTICAL.
     *
     * If one of the latter two are used then Axis.BOTTOM or * Axis.LEFT is
     * assumed.
     *
     */
    public LabelAxis(int p, Component drawer, String[][] labels, boolean[] visibleLabels) {

        NUMBER_OF_TICS = labels.length;

        //this.labels=labels;
        setLabels(labels, visibleLabels);

        title.setFont(new Font("TIMES NEW ROMAN", 1, 12));

        this.drawer = drawer;

        setPosition(p);

        switch (position) {

            case LEFT:
            case VERTICAL:

                title.setRotation(90);

                break;

            case RIGHT:

                title.setRotation(-90);

                break;

            default:

                title.setRotation(0);

                break;

        }

    }

    /*      *
     * public void resetAxis(String[][] labels) {
     *
     *
     *
     * width=0;
     *
     * minimum = 0.0;
     *
     * maximum = 0.0;
     *
     *
     *
     * resetRange();
     *
     * }
     *
     *
     *
     */
    /*
     *
     *******************
     *
     ** Public Methods
     *
     *****************
     */
    /**
     *
     * Set the axis position.
     *
     * @param p Must be one of Axis.BOTTOM,
     *
     * Axis.TOP, Axis.LEFT, Axis.RIGHT, Axis.HORIZONTAL or Axis.VERTICAL.
     *
     * If one of the latter two are used then Axis.BOTTOM or * Axis.LEFT is
     * assumed.
     *
     */
    public final void setPosition(int p) {

        position = p;

        switch (position) {

            case LEFT:

                orientation = VERTICAL;

                break;

            case RIGHT:

                orientation = VERTICAL;

                break;

            case TOP:

                orientation = HORIZONTAL;

                break;

            case BOTTOM:

                orientation = HORIZONTAL;

                break;

            case HORIZONTAL:

                orientation = HORIZONTAL;

                position = BOTTOM;

                break;

            case VERTICAL:

                orientation = VERTICAL;

                position = LEFT;

                break;

            default:

                orientation = HORIZONTAL;

                position = BOTTOM;

                break;

        }

    }

    public void setRotated(boolean rotated) {

        if (rotated) {
            rotation = 45;
        } else {
            rotation = 90;
        }

    }

    /**
     *
     * Attach a DataSet for the Axis to manage.
     *
     * @param d dataSet to attach
     *
     * @see graph.DataSet
     *
     */

    /*
     *
     * public void attachDataSet( DataSet d ) {
     *
     * if( orientation == HORIZONTAL ) attachXdata( d );
     *
     * else attachYdata( d );
     *
     * }
     *
     */
    /**
     *
     * Detach an attached DataSet
     *
     * @param d dataSet to detach
     *
     * @see graph.DataSet
     *
     */

    /*
     *
     * public void detachDataSet( DataSet d ) {
     *
     * int i = 0;
     *
     *
     *
     * if( d == null ) return;
     *
     *
     *
     * if( orientation == HORIZONTAL ) {
     *
     * d.xaxis = null;
     *
     * } else {
     *
     * d.yaxis = null;
     *
     * }
     *
     * dataset.removeElement(d);
     *
     *
     *
     * if(!manualRange) resetRange();
     *
     * }
     *
     */
    /**
     *
     * Detach All attached dataSets.
     *
     */

    /*
     *
     * public void detachAll() {
     *
     * int i;
     *
     * DataSet d;
     *
     *
     *
     * if( dataset.isEmpty() ) return;
     *
     *
     *
     *
     *
     * if( orientation == HORIZONTAL ) {
     *
     * for (i=0; i<dataset.size(); i++) {
     *
     * d = (DataSet)(dataset.elementAt(i));
     *
     * d.xaxis = null;
     *
     * }
     *
     * } else {
     *
     * for (i=0; i<dataset.size(); i++) {
     *
     * d = (DataSet)(dataset.elementAt(i));
     *
     * d.yaxis = null;
     *
     * }
     *
     * }
     *
     *
     *
     * dataset.removeAllElements();
     *
     *
     *
     * minimum = 0.0;
     *
     * maximum = 0.0;
     *
     * }
     *
     */
    /**
     *
     * Return the position of the Axis.
     *
     * @return One of Axis.LEFT, Axis.RIGHT, Axis.TOP, or Axis.BOTTOM.
     *
     */
    public int getAxisPos() {
        return position;
    }

    /**
     * @return horizontal or vertical If the Axis is Vertical return
     * <i>true</i>.
     *
     */
    public boolean isVertical() {

        return orientation != HORIZONTAL;

    }

    public int getAWidth() {

        return width;

    }

    //The height of the label... (found by taking cos(angle) of the longest string)

    /*
     *
     * public int getMaxLabelHeight(Graphics g){
     *
     * int ret=Integer.MIN_VALUE;
     *
     * Graphics2D g2D = (Graphics2D) g;
     *
     * g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
     * RenderingHints.VALUE_ANTIALIAS_ON);      *
     *
     *
     * FontMetrics fm = g.getFontMetrics(font);
     *
     * for(int i=0;i<labels.length;i++){
     *
     * for(int j=0;j<labels[0].length;j++){
     *
     * if(visibleLabels[j] &&
     * fm.stringWidth(labels[i][j])>ret)ret=fm.stringWidth(labels[i][j]);
     *
     * }
     *
     * }
     *
     *
     *
     * ret=(int)(ret*Math.cos(DEG2RAD*rotation));
     *
     * return ret+tempOffset+fm.getHeight();
     *
     * }
     *
     */
    //The height of the label... (found by taking cos(angle) of the longest string)
    public int getMaxLabelWidth(Graphics g) {

        int ret = Integer.MIN_VALUE;

        Graphics2D g2D = (Graphics2D) g;

        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontMetrics fm = g.getFontMetrics(font);

        for (String[] label1 : labels) {
            for (int j = 0; j < labels[0].length; j++) {
                if (visibleLabels[j + 1] && fm.stringWidth(label1[j]) > ret) {
                    ret = fm.stringWidth(label1[j]);
                }
            }
        }

        int titleHeight = title.getHeight(g);

        //Font f = title.getFont();
        if (ret > 0) {
            ret = (int) (ret * Math.sin(DEG2RAD * rotation));
        } else {
            ret = 0;
        }

        //System.out.print("width: "+(ret*Math.sin(DEG2RAD*rotation)));
        return ret + offset + fm.getHeight() + titleHeight;

        //return titleHeight ;
    }

    public int predictLength() {

        AffineTransform aft = new AffineTransform();

        FontRenderContext fr = new FontRenderContext(aft, true, false);

        int labelWidth = (int) font.getStringBounds("tTSM", fr).getHeight();

        int visibleLab = 0;

        for (int i = 1; i < visibleLabels.length - 1; i++) {
            if (visibleLabels[i]) {
                visibleLab++;
            }
        }

        int labellenght = visibleLab * (labels.length - 1) * labelWidth + ((int) ((labels.length - 1) * (labelWidth * spaceBetween)));

        labellenght = Math.max(labellenght, minimumSize);

        // int additionalLength=Integer.MIN_VALUE;
        int x0 = (int) (labellenght / labels.length - 1);

        labellenght = x0 * (labels.length - 1);

        // System.out.print("\n"+(labellenght));

        /*
         *
         * double sin = 0;
         *
         * if(additionalLength>0)
         * sin=(int)(additionalLength*Math.cos(DEG2RAD*rotation))+(labelWidth);
         *
         * labellenght+=sin;
         *
         */
        // System.out.print("\n"+(labels.length*2));
        // System.out.print("\n"+(labellenght));
        return Math.max(labels.length * 2, labellenght);

    }

    /*
     *
     * public int getAxisLength(Graphics g) {
     *
     * FontMetrics fm = g.getFontMetrics(font);
     *
     * int labelWidth=fm.getHeight();
     *
     *
     *
     * int visibleLab=0;
     *
     * for(int
     * i=1;i<visibleLabels.length-1;i++)if(visibleLabels[i])visibleLab++;
     *
     *
     *
     * //int labellenght=
     * labels[0].length*(labels.length-1)*labelWidth+((int)((labels.length-1)*(labelWidth*1.5)));
     *
     * int labellenght=
     * visibleLab*(labels.length-1)*labelWidth+((int)((labels.length-1)*(labelWidth*spaceBetween)));
     *
     *
     *
     * if(visibleLab==0 && spaceBetween>0.0) labellenght=minimumSize;
     *
     *
     *
     * int x0 = (int)(labellenght/labels.length-1);
     *
     * x0=x0*(labels.length-1);
     *
     *
     *
     * // int labellength =((int)((amax.x - amin.x)/(
     * labels[0].length)))*labels[0].length ;
     *
     * //labellenght+=(endLength(g));
     *
     * System.out.print("\n Set axislength: "+ x0);
     *
     * //return labellenght;
     *
     * //return x0;
     *
     *
     *
     * return x0;
     *
     * // return 40;
     *
     * }
     *
     */
    public int endLength() {

        if (visiblelabs == 0) {
            return 2;
        }

        AffineTransform aft = new AffineTransform();

        FontRenderContext fr = new FontRenderContext(aft, true, false);

        int additionalLength = Integer.MIN_VALUE;

        // FontMetrics fm = g.getFontMetrics(font);
        // int labelWidth=fm.getHeight();
        //System.out.print("\nLabelslength: "+labels[labels[0].length-1].length);
        for (int i = 0; i < labels[0].length; i++) {

            if (visibleLabels[i + 1] && (int) font.getStringBounds(labels[labels.length - 1][i], fr).getWidth() > additionalLength) {
                additionalLength = (int) font.getStringBounds(labels[labels.length - 1][i], fr).getWidth();
            }

        }

        double sin = (int) (additionalLength * Math.cos(DEG2RAD * rotation));

        sin += font.getStringBounds("sdf", fr).getHeight();

        return (int) sin;

    }

    /**
     *
     * Return the Height! of the axis.
     *
     * @param g graphics context.
     * @return max axis width
     */
    public int getAxisWidth(Graphics g) {
        return getMaxLabelWidth(g);
    }

    /**
     *
     * Position the axis at the passed coordinates. The coordinates should match
     *
     * the type of axis.
     *
     * @param xmin The minimum X pixel
     *
     * @param xmax The maximum X pixel. These should be equal if the axis * is
     * vertical
     *
     * @param ymin The minimum Y pixel
     *
     * @param ymax The maximum Y pixel. These should be equal if the axis * is
     * horizontal
     *
     * @return <i>true</i> if there are no inconsistencies.
     *
     */
    public boolean positionAxis(int xmin, int xmax, int ymin, int ymax) {

        amin = null;

        amax = null;

        if (orientation == HORIZONTAL && ymin != ymax) {
            return false;
        }

        if (orientation == VERTICAL && xmin != xmax) {
            return false;
        }

        amin = new Point(xmin, ymin);

        amax = new Point(xmax, ymax);

        return true;

    }

    /**
     *
     * Draw the axis using the passed Graphics context.
     *
     * @param g Graphics context for drawing
     *
     */
    public void drawAxis(Graphics g) {

        Graphics lg;

        //System.out.print("\n\nmaximum before:"+maximum);
        //System.out.print("\nminimum before:"+minimum);
        if (!redraw) {
            return;
        }

        // if( amin.equals(amax) ) return;
        if (width == 0) {
            width = getAxisWidth(g);
        }

        lg = g.create();

        title.setDrawingComponent(drawer);

        label.setDrawingComponent(drawer);

        exponent.setDrawingComponent(drawer);

        if (orientation == HORIZONTAL) {

            drawHAxis(lg);

        } else {

            drawVAxis(lg);

        }

        //System.out.print("\nmaximum after:"+maximum);
        //System.out.print("\nminimum after:"+minimum);
    }

    /**
     *
     * Set the title of the axis
     *
     * @param s string containing text.
     *
     */
    public void setTitleText(String s) {

        title.setText(s);
    }

    public String getTitleText() {
        return title.getText();
    }

    /**
     *
     * Set the color of the title
     *
     * @param c Color of the title.
     *
     */
    public void setTitleColor(Color c) {
        title.setColor(c);
    }

    /**
     *
     * Set the font of the title
     *
     * @param f Title font.
     *
     */
    public void setTitleFont(Font f) {
        title.setFont(f);
    }

    /**
     *
     * Set the title rotation angle. Only multiples of 90 degrees allowed.
     *
     * @param a Title rotation angle in degrees.
     *
     */
    public void setTitleRotation(int a) {
        title.setRotation(a);
    }

    /**
     *
     * Set the color of the labels
     *
     * @param c Color of the labels.
     *
     */
    public void setLabelColor(Color c) {
        label.setColor(c);
    }

    /**
     *
     * Set the font of the labels.
     *
     * @param f font.
     *
     */
    public void setLabelFont(Font f) {
        label.setFont(f);
    }

    /**
     *
     * Set the color of the exponent
     *
     * @param c Color.
     *
     */
    public void setExponentColor(Color c) {
        exponent.setColor(c);
    }

    /**
     *
     * Set the font of the exponent
     *
     * @param f font.
     *
     */
    public void setExponentFont(Font f) {
        exponent.setFont(f);
    }

    /**
     *
     * Is the range of the axis to be set automatically (based on the data)
     *
     * or manually by setting the values Axis.minimum and Axis.maximum?
     *
     * @param b boolean value.
     *
     */
    public void setManualRange(boolean b) {
        manualRange = b;
    }

    /*
     *
     *********************
     *
     ** Protected Methods
     *
     *******************
     */
    /*
     *
     * public Point getCorner(int corner,Graphics g){
     *
     * FontMetrics f = g.getFontMetrics();
     *
     *
     *
     * int x=0;
     *
     * int y=0;
     *
     *
     *
     *
     *
     * switch (corner){
     *
     *
     *
     * case 1: return 0;
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     * default: return 0;
     *
     * }
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     * }
     *
     *
     *
     */
    //Put this after positionAxis() to be able to retrieve values for the axis..
    public void prepareLocations(Graphics g) {

        ylocations = new int[labels.length];

        //System.out.print("\nlabelslength: "+labels.length);
        for (int i = 0; i < labels.length; i++) {

            ylocations[i] = amin.x + (int) (((amax.x - amin.x) / (labels.length - 1)) * i);

        }

    }

    protected void drawHAxis(Graphics g) {

        Graphics lg;

        int i;

        int j;

        int x0, y0, x1, y1;

        int direction;

        //int tempOffset;
        double minor_step;

        if (position == TOP) {
            direction = 1;
        } else {
            direction = -1;
        }

        Color c;

        double val;

        double minor;

        if (axiscolor != null) {
            g.setColor(axiscolor);
        }

        g.drawLine(amin.x, amin.y, amax.x, amax.y);

        //System.out.print("\ndrawing til x: "+amax.x); 
        minor_step = label_step / (minor_tic_count + 1);

        val = label_start;

        ylocations = new int[labels.length];

        for (i = 0; i < labels.length; i++) {

            //if( val >= vmin && val <= vmax ) {
            y0 = amin.y;

            //x0 = amin.x + (int)( ( val - minimum ) * scale);
            //x0 = amin.x + (int)(( (amax.x - amin.x - endLength(g))/( labels[0].length-1 ))*i);
            x0 = amin.x + (int) (((amax.x - amin.x) / (labels.length - 1)) * i);

            if (paintGrid) {

                c = g.getColor();

                if (gridcolor != null) {
                    g.setColor(gridcolor);
                }

                if (transparency != 255) {

                    Graphics2D g2d = (Graphics2D) g;

                    Paint pn = g2d.getPaint();

                    if (gridcolor != null) {
                        g.setColor(new Color(gridcolor.getRed(), gridcolor.getGreen(), gridcolor.getBlue(), (int) transparency));
                    }

                }

                if ((!(i == 0 && dropFirstGridLine)) && (!(i == labels.length - 1 && dropLastGridLine))) {
                    g.drawLine(x0, y0, x0, y0 + data_window.height * direction);
                }

                g.setColor(c);

            }

            x1 = x0;

            y1 = y0 + major_tic_size * direction;

            g.drawLine(x0, y0, x1, y1);

            if (TICS_IN_BOTH_ENDS) {
                g.drawLine(x0, y0 + data_window.height * direction, x0, y0 + data_window.height * direction + major_tic_size);
            }

            ylocations[i] = x0;

            //}
            minor = val + minor_step;

            /*
             *
             * for(j=0; j<minor_tic_count; j++) {
             *
             * //if( minor >= vmin && minor <= vmax ) {
             *
             * y0 = amin.y;
             *
             * x0 = amin.x + (int)( ( minor - minimum ) * scale);
             *
             * if( paintGrid ) {
             *
             * c = g.getColor();
             *
             * if(gridcolor != null) g.setColor(gridcolor);
             *
             * g.drawLine(x0,y0,x0,y0+data_window.height*direction);
             *
             * g.setColor(c);
             *
             * }
             *
             * x1 = x0;
             *
             * y1 = y0 + minor_tic_size*direction;
             *
             * g.drawLine(x0,y0,x1,y1);
             *
             * if(TICS_IN_BOTH_ENDS)
             * g.drawLine(x0,y0+data_window.height*direction,x0,y0+data_window.height*direction+minor_tic_size);
             *
             * // }
             *
             * minor += minor_step;
             *
             * }
             *
             */
            val += label_step;

        }

        /*
         *
         * if(position == TOP ) {
         *
         * tempOffset = - label.getLeading(g) - label.getDescent(g);
         *
         * } else {
         *
         * tempOffset = + label.getLeading(g) + label.getAscent(g);
         *
         * }
         *
         */
        val = label_start;

        g.setFont(font);

        Graphics2D g2D = (Graphics2D) g;

        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontMetrics fm = g.getFontMetrics(font);

        int labelWidth = fm.getHeight();

        //       int labelWidth=16;
        //System.out.print("\n"+labelWidth);
        // int additionalSpace = 0;
        // if(rotation=40) additionalSpace = 
        //         double height = Math.sin(DEG2RAD*rotation)*labelWidth;
//          double additionalSpace = labelWidth/Math.sin(DEG2RAD*(rotation));
        double additionalSpace = 0;

        int height = 0;

//          
        int visibleLabs = 0;

        for (int k = 1; k < labels[0].length; k++) {

            if (visibleLabels[k]) {
                visibleLabs++;
            }

        }

        int beg = (visibleLabs * labelWidth) - (4 * (visibleLabs - 1));

        beg = beg / 2;

        Color before = g.getColor();

        for (i = 0; i < labels.length; i++) {

            if (groupColors != null) {
                g.setColor(groupColors[i]);
            } else {
                g.setColor(Color.GRAY);//new Color(180, 180, 180));
            }

            y0 = amin.y + offset;

            //x0 = amin.x + (int)(( (amax.x - amin.x)/( labels[0].length))*i);
            x0 = amin.x + (int) (((amax.x - amin.x) / (labels.length - 1)) * i);

            //x0 = amin.x + (int)(( (amax.x - amin.x - endLength(g))/( labels[0].length-1 ))*i);
            //int labellength =((int)((amax.x - amin.x)/( labels[0].length)))*labels[0].length ;
            // int endpoint = amin.x +endLength(g)+labellength;
            // g.drawLine( endpoint,y0-4,endpoint,y0+4 );
            x0 = x0 - beg;

            // x0=x0-beg;
            for (j = 0; j < labels[0].length; j++) {

                if (visibleLabels[j + 1]) {

                    drawRotatedLabel(labels[i][j], g, false, x0, y0, rotation);

                    x0 += (labelWidth);

                }

            }

        }

        g.setColor(before);

        //Graphics2D g2=(Graphics2D)g;
        //g2.rotate(45); 
        if (!title.isNull()) {

            if (position == TOP) {

                y0 = amin.y - label.getLeading(g)
                        - label.getDescent(g)
                        - title.getLeading(g)
                        - title.getDescent(g);

            } else {

                y0 = amax.y + label.getLeading(g)
                        + label.getAscent(g)
                        + title.getLeading(g)
                        + title.getAscent(g);

            }

            x0 = amin.x + (amax.x - amin.x) / 2;

            // y0=amax.y+width+3;
            y0 = amax.y + this.getMaxLabelWidth(g) + 6 - (title.getHeight(g));

            // Graphics2D g2D = (Graphics2D) g;
            // g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); 
            //title.setFont(null);
            if (axiscolor != null) {
                title.setColor(axiscolor);
            }

            title.draw(g, x0, y0, ChartLabel.CENTER);

        }

    }

    public int getLeftLabel(int xcor) {

        int ret = 0;

        int x0 = 0;

        for (int i = 0; i < labels.length; i++) {

            x0 = ((int) amin.x + (int) (((amax.x - amin.x) / (labels.length - 1)) * i));

            //System.out.print(" "+x0);
            if (x0 > xcor) {
                ret = i;
                break;
            }

        }

        //System.out.print("\nretuen: "+ret);
        return ret;

    }

    public int getRightLabel(int xcor) {

        int ret = 0;

        int x0 = 0;

        for (int i = 0; i < labels.length; i++) {

            x0 = ((int) amin.x + (int) (((amax.x - amin.x) / (labels.length - 1)) * i));

            if (x0 > xcor) {
                break;
            } else {
                ret = i;
            }

        }

        return ret;

    }

    /**
     *
     * Draw a Vertical Axis.
     *
     * @param g Graphics context.
     *
     */
    protected void drawVAxis(Graphics g) {

        Graphics lg;

        int i;

        int j;

        int x0, y0, x1, y1;

        int direction;

        int tempOffset = 0;

        double minor_step;

        double minor;

        Color c;

        double vmin = minimum * 1.001;

        double vmax = maximum * 1.001;

        double scale = (amax.y - amin.y) / (maximum - minimum);

        double val;

//          System.out.println("Drawing Vertical Axis!");
        if (axiscolor != null) {
            g.setColor(axiscolor);
        }

        g.drawLine(amin.x, amin.y, amax.x, amax.y);

        if (position == RIGHT) {
            direction = -1;
        } else {
            direction = 1;
        }

        minor_step = label_step / (minor_tic_count + 1);

        val = label_start;

        for (i = 0; i < label_count; i++) {

            if (val >= vmin && val <= vmax) {

                x0 = amin.x;

                y0 = amax.y - (int) ((val - minimum) * scale);

                if (Math.abs(label_value[i]) <= 0.0001 && drawzero) {

                    c = g.getColor();

                    if (zerocolor != null) {
                        g.setColor(zerocolor);
                    }

                    g.drawLine(x0, y0, x0 + data_window.width * direction, y0);

                    g.setColor(c);

                } else if (paintGrid) {

                    c = g.getColor();

                    if (gridcolor != null) {
                        g.setColor(gridcolor);
                    }

                    if (transparency != 255) {

                        Graphics2D g2d = (Graphics2D) g;

                        Paint pn = g2d.getPaint();

                        if (gridcolor != null) {
                            g.setColor(new Color(gridcolor.getRed(), gridcolor.getGreen(), gridcolor.getBlue(), (int) transparency));
                        }

                    }

                    if (!(i == 0 && dropFirstGridLine)) {
                        g.drawLine(x0, y0, x0 + data_window.width * direction, y0);
                    }

                    g.setColor(c);

                }

                x1 = x0 + major_tic_size * direction;

                y1 = y0;

                g.drawLine(x0, y0, x1, y1);

                if (TICS_IN_BOTH_ENDS) {
                    g.drawLine(x0 + data_window.width * direction - major_tic_size, y0, x0 + data_window.width * direction, y1);
                }

            }

            minor = val + minor_step;

            for (j = 0; j < minor_tic_count; j++) {

                if (minor >= vmin && minor <= vmax) {

                    x0 = amin.x;

                    y0 = amax.y - (int) ((minor - minimum) * scale);

                    if (paintGrid) {

                        c = g.getColor();

                        if (gridcolor != null) {
                            g.setColor(gridcolor);
                        }

                        if (transparency != 255) {

                            Graphics2D g2d = (Graphics2D) g;

                            Paint pn = g2d.getPaint();

                            if (gridcolor != null) {
                                g.setColor(new Color(gridcolor.getRed(), gridcolor.getGreen(), gridcolor.getBlue(), (int) transparency));
                            }

                        }

                        g.drawLine(x0, y0, x0 + data_window.width * direction, y0);

                        g.setColor(c);

                    }

                    x1 = x0 + minor_tic_size * direction;

                    y1 = y0;

                    g.drawLine(x0, y0, x1, y1);

                    if (TICS_IN_BOTH_ENDS) {
                        g.drawLine(x0 + data_window.width * direction - minor_tic_size, y0, x0 + data_window.width * direction, y1);
                    }

                }

                minor += minor_step;

            }

            val += label_step;

        }

        val = label_start;

        for (i = 0; i < label_count; i++) {

            if (val >= vmin && val <= vmax) {

                x0 = amin.x + tempOffset;

                y0 = amax.y - (int) ((val - minimum) * scale)
                        + label.getAscent(g) / 2;

                if (position == RIGHT) {

                    label.setText(" " + label_string[i]);

                    label.draw(g, x0, y0, ChartLabel.LEFT);

                } else {

                    label.setText(label_string[i] + " ");

                    label.draw(g, x0, y0, ChartLabel.RIGHT);

                }

            }

            val += label_step;

        }

        /*
         *
         * if( !exponent.isNull() ) {
         *
         *
         *
         * y0 = amin.y;
         *
         *
         *
         * if(position == RIGHT ) {
         *
         * x0 = amin.x + max_label_width + exponent.charWidth(g,' ');
         *
         * exponent.draw(g,x0,y0,ChartLabel.LEFT);
         *
         * } else {
         *
         *
         *
         * x0 = amin.x - exponent.getWidth(g)-exponent.charWidth(g,' ');
         *
         * //x0 = amin.x - max_label_width - exponent.charWidth(g,' ');
         *
         * exponent.draw(g,x0,y0,ChartLabel.RIGHT);
         *
         * }
         *
         *
         *
         * }
         *
         */
        if (!title.isNull()) {

            y0 = amin.y + (amax.y - amin.y) / 2;

            if (title.getRotation() == 0 || title.getRotation() == 180) {

                if (position == RIGHT) {

                    x0 = amin.x + max_label_width + title.charWidth(g, ' ');

                    title.draw(g, x0, y0, ChartLabel.LEFT);

                } else {

                    x0 = amin.x - max_label_width - title.charWidth(g, ' ');

                    title.draw(g, x0, y0, ChartLabel.RIGHT);

                }

            } else {

                title.setJustification(ChartLabel.CENTER);

                if (position == RIGHT) {

                    x0 = amin.x + max_label_width - title.getLeftEdge(g)
                            + +title.charWidth(g, ' ');

                } else {

                    x0 = amin.x - max_label_width - title.getRightEdge(g)
                            - title.charWidth(g, ' ');

                }

                title.draw(g, x0, y0);

            }

        }

        if (!exponent.isNull()) {

            y0 = amin.y + exponent.getWidth(g);

            if (title.getRotation() == 0 || title.getRotation() == 180) {

                if (position == RIGHT) {

                    x0 = amin.x + max_label_width + title.charWidth(g, ' ');

                    title.draw(g, x0, y0, ChartLabel.LEFT);

                } else {

                    x0 = amin.x - max_label_width - title.charWidth(g, ' ');

                    title.draw(g, x0, y0, ChartLabel.RIGHT);

                }

            } else {

                if (position == RIGHT) {

                    x0 = amin.x + max_label_width - title.getLeftEdge(g)
                            + +title.charWidth(g, ' ');

                } else {

                    x0 = amin.x - max_label_width - title.getRightEdge(g)
                            - title.charWidth(g, ' ');

                }

                exponent.setBackground(Color.red);

                exponent.setRotation(90);

                exponent.setFontSize(12);

                exponent.setFontStyle(1);

                exponent.setFontName("TIMES NEW ROMAN");

                exponent.draw(g, x0, y0);

            }

        }

    }

    public void drawRotatedLabel(String s, Graphics gr, boolean antialias, int x, int y, int deg) {

        Graphics2D g2D = (Graphics2D) gr;

        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontMetrics fm = gr.getFontMetrics(font);

        int labelWidth = fm.getHeight();

        g2D.translate(x, y);

        g2D.rotate(Math.PI / 4);

        g2D.drawString(s, 0, 0);

        g2D.rotate(-Math.PI / 4);

        g2D.translate(-x, -y);

    }

    
}
