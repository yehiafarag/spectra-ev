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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import no.uib.jexpress_modularized.core.visualization.charts.CLabel;
import no.uib.jexpress_modularized.core.visualization.charts.ChartLabel;
import no.uib.jexpress_modularized.core.visualization.charts.RotChartLabel;

/**
 *
 * @author y-mok_000
 */
public class Axis  extends Object implements Serializable{

    public boolean useLog2 = false;
    //double[][] values={{0.0003,0.0005},{0.00042,0.00035},{-0.000275,0.000334},{0.00034,0.00023},{0.00042,0.000126},{0.00079,0.00023},{0.00023,0.00057},{0.00026,0.00024}};
    double[] values = null;
    Component drawer;
    boolean temp = true;
    public int nullI;
    public int LabelAditionalLength = 0;
    private double SetMin = 0.0;
    private double SetMax = 0.0;
    public float transparency = 255;
    //public boolean forcedValues = false;
    public boolean correctForCloseValues = false;
    public Font exponentfont = new Font("Times New Roman", 0, 10);
    private boolean useManualMaxMin = false;
    /*
     *
     ***************************
     *
     ** Public  Values      *
     *************************
     */
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
     * Constant flagging Axis position on the graph.      *
     * Left side => Vertical
     *
     */
    public  final int LEFT = 2;
    /**
     *
     * Constant flagging Axis position on the graph.      *
     * Right side => Vertical
     *
     */
    public  final int RIGHT = 3;
    /**
     *
     * Constant flagging Axis position on the graph.      *
     * Top side => Horizontal
     *
     */
    public  final int TOP = 4;
    /**
     *
     * Constant flagging Axis position on the graph.      *
     * Bottom side => Horizontal
     *
     */
    public  final int BOTTOM = 5;
    /**
     *
     * The first guess on the number of Labeled Major tick marks.
     *
     */
    public int NUMBER_OF_TICS = 6;
    public boolean TICS_IN_BOTH_ENDS = true;
    /*
     *
     ***********************
     *
     ** Public Variables      *
     *********************
     */
    public boolean[] invalidValues; //this controls valid values and is for instance false for log values of 0 and below..
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
    public boolean drawzero = true;
    /**
     *
     * Color of the grid
     *
     */
    public Color gridcolor ;
    /**
     *
     * Color of the line at the Zero label
     *
     */
    public Color zerocolor ;
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
    public boolean force_end_labels = false;
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
    public int minor_tic_count = 4;
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
     * The width of the Axis. Where width for a horizontal axis is really      *
     * the height
     *
     */
    public int width = 0;
    /**
     *
     * ChartLabel class to contain the title of the axis.
     *
     */
    protected RotChartLabel title = new RotChartLabel(" ");
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
    //protected CLabel clabel = new CLabel();
    protected CLabel cexponent = new CLabel();
    protected CLabel ctitle = new CLabel();
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
    protected double label_step = 3.0;
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
    protected int label_count = 6;
    /**
     *
     * Initial guess for the number of labels required
     *
     */
    protected int guess_label_number = 6;
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
    protected boolean manualRange = false;
    public  int Log_Transform = 1;
    public  int Not_Log_Transform = 0;
    public int transform = 0;

    public void setTransform(int transform) {

        this.invalidValues = null;

        this.transform = transform;

        //resetAxis(values);   

    }

    //if(transform==Log_Transform)
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
     * positioned at the bottom of the graph.
     *
     */
    public Axis(double[] values, Component drawer) {

        this.values = values;

        this.drawer = drawer;

        orientation = HORIZONTAL;

        position = BOTTOM;

    }

    /**
     *
     * Instantiate the class. Setting the position.
     *
     * @param p Set the axis position. Must be one of Axis.BOTTOM,
     *
     * Axis.TOP, Axis.LEFT, Axis.RIGHT, Axis.HORIZONTAL or Axis.VERTICAL.
     *
     * If one of the latter two are used then Axis.BOTTOM or      *
     * Axis.LEFT is assumed.
     *
     */
    public Axis(double[] values, int p, Component drawer) {

        if (transform == Log_Transform) {
            this.values = logtrans(values);
        } else {
            this.values = values;
        }



        if (p == 1) {

            ctitle.setRotated(true);

            cexponent.setRotated(true);

        }

        this.drawer = drawer;



        setPosition(p);







        /*
         *
         * switch (position) {
         *
         * case LEFT: case VERTICAL:
         *
         * title.setRotation(90);
         *
         * break;
         *
         * case RIGHT:
         *
         * title.setRotation(-90);
         *
         * break;
         *
         * default:
         *
         * title.setRotation(0);
         *
         * break;
         *
         * }
         *
         *
         *
         */

    }

    public double[] logtrans(double[] source) {

        if (source == null) {
            return null;
        }

        invalidValues = null;

        int notnulls = 0;

        for (int i = 0; i < source.length; i++) {
            if (source[i] != 0.0) {
                notnulls++;
            }
        }



        double[] values = new double[notnulls];



        int c = 0;

        for (int i = 0; i < source.length; i++) {

            if (source[i] > 0.0) {

                if (!useLog2) {
                    values[c] = log10(source[i]);
                } else {
                    values[c] = log2(source[i]);
                }

                c++;

            } else {

                if (invalidValues == null) {
                    invalidValues = new boolean[source.length];
                }

                invalidValues[i] = true;



            }

        }

        return values;

    }

    public void setValueRange(double min, double max) {

        this.SetMin = min;

        this.SetMax = max;

        this.minimum = min;

        this.maximum = max;

        resetRange();

    }

    public Axis(int p, Component drawer, double min, double max) {

        //this.values=values;

        this.SetMin = min;

        this.SetMax = max;

        this.drawer = drawer;

        if (p == 1) {

            ctitle.setRotated(true);

            cexponent.setRotated(true);

        }

        setPosition(p);

        // title.setFont(new Font("TIMES NEW ROMAN",1,12));

        /*
         *
         *
         *
         * switch (position) {
         *
         * case LEFT: case VERTICAL:
         *
         * title.setRotation(90);
         *
         * break;
         *
         * case RIGHT:
         *
         * title.setRotation(-90);
         *
         * break;
         *
         * default:
         *
         * title.setRotation(0);
         *
         * break;
         *
         * }
         *
         */

    }

    public boolean[] resetAxis(double[] values) {

        if (transform == Log_Transform) {
            this.values = logtrans(values);
        } else {
            this.values = values;
        }

        width = 0;

        minimum = 0.0;

        maximum = 0.0;



        resetRange();

        return invalidValues;

    }

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
     * If one of the latter two are used then Axis.BOTTOM or      *
     * Axis.LEFT is assumed.
     *
     */
    public void setPosition(int p) {

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
     * Return the minimum value of All datasets attached to the axis.
     *
     * @return Data minimum
     *
     */
    public double getDataMin() {

        if (values == null) {
            return SetMin;
        } else if (values.length == 0) {
            return 0;
        }



        double min = 9999999;

        for (int i = 0; i < values.length; i++) {
            if (min > values[i]) {

                if (invalidValues == null || !invalidValues[i]) {

                    if (transform == Log_Transform && values[i] > 0) {
                        min = values[i];
                    } else {
                        min = values[i];
                    }

                }





            }
        }

        //System.out.print("\nmin: "+min);

        return min;



        //    return -1.0;

    }

    /**
     *
     * Return the maximum value of All datasets attached to the axis.
     *
     * @return Data maximum
     *
     */
    public double getDataMax() {

        if (values == null) {
            return SetMax;
        } else if (values.length == 0) {
            return 0;
        }

        double max = -9999999;

        for (int i = 0; i < values.length; i++) {
            if (max < values[i]) {
                max = values[i];
            }
        }

        //System.out.print("\nmax: "+max);

        //This (+1) could possibly cause problems! Is is there because the plot does not contain

        //the rightmost point..

        return max;





        //return 0.5;

    }

    /**
     *
     * Return the pixel equivalent of the passed data value. Using the      *
     * position of the axis and the maximum and minimum values convert
     *
     * the data value into a pixel value
     *
     * @param v data value to convert
     *
     * @return equivalent pixel value
     *
     * @see graph.Axis#getDouble( )
     *
     */
    public int getInteger(double v) {

        double scale;

        if (transform == Log_Transform && (!(v > 0.0 || v > 0))) {
            return Integer.MAX_VALUE;
        }

        // if(v==0.0 || v==0) return Integer.MIN_VALUE;



        if (transform == Log_Transform) {
            if (useLog2) {
                v = log2(v);
            } else {
                v = log10(v);
            }
        }

        if (amin == null) {
            return -1;
        }



        if (orientation == HORIZONTAL) {

            scale = (double) (amax.x - amin.x) / (maximum - minimum);

            return amin.x + (int) ((v - minimum) * scale);

        } else {

            scale = (double) (amax.y - amin.y) / (maximum - minimum);

            return amin.y + (int) ((maximum - v) * scale);

        }

    }

    public int[][] getInteger(double[][] v) {

        return getInteger(v, 0, v[0].length);

    }

    public int[][] getInteger(double[][] v, int from, int to) {

        double scale;

        int[][] ret = new int[v.length][to - from];



        int aminx = amin.x;

        int aminy = amin.y;

        double lval = 0.0;


        if (orientation == HORIZONTAL) {

            scale = (double) (amax.x - amin.x) / (maximum - minimum);



            for (int i = 0; i < ret.length; i++) {

                for (int j = 0; j < ret[0].length; j++) {

                    if (transform == Log_Transform) {
                        if (useLog2) {
                            lval = log2(v[i][j + from]);
                        } else {
                            lval = log10(v[i][j + from]);
                        }

                        ret[i][j] = aminx + (int) ((lval - minimum) * scale);
                    } else {
                        ret[i][j] = aminx + (int) ((v[i][j + from] - minimum) * scale);
                    }

                }

            }

        } else {

            scale = (double) (amax.y - amin.y) / (maximum - minimum);

            for (int i = 0; i < ret.length; i++) {

                for (int j = 0; j < ret[0].length; j++) {

                    if (transform == Log_Transform) {
                        if (useLog2) {
                            lval = log2(v[i][j + from]);
                        } else {
                            lval = log10(v[i][j + from]);
                        }
                        ret[i][j] = aminy + (int) ((maximum - lval) * scale);
                    } else {
                        ret[i][j] = aminy + (int) ((maximum - v[i][j + from]) * scale);
                    }

                }

            }

        }

        return ret;

    }

    /**
     *
     * Return the data value equivalent of the passed pixel position.
     *
     * Using the      *
     * position of the axis and the maximum and minimum values convert
     *
     * the pixel position into a data value
     *
     * @param i pixel value
     *
     * @return equivalent data value
     *
     * @see graph.Axis#getInteger( )
     *
     */
    public double getDouble(int i) {

        double scale;

        if (amax == null || amin == null) {
            return Double.MAX_VALUE;
        }

        if (orientation == HORIZONTAL) {

            scale = (maximum - minimum) / (double) (amax.x - amin.x);

            return (minimum + (i - amin.x) * scale);

        } else {

            scale = (maximum - minimum) / (double) (amax.y - amin.y);

            return maximum - (i - amin.y) * scale;

        }

    }

    /**
     *
     * Reset the range of the axis (the minimum and maximum values) to the
     *
     * default data values.
     *
     */
    public void resetRange() {

        minimum = getDataMin();

        maximum = getDataMax();



        double madd = 0;

        double midd = 0;

        if (correctForCloseValues) {



            madd = ((maximum - minimum) * 2) / 100;

            midd = ((maximum - minimum) * 2) / 100;

        }

        //System.out.print("\nexpanding bounds!");



        maximum += madd;

        minimum -= midd;



        if (minimum == 0.0 && maximum == 0.0) {
            minimum = 0;
            maximum = 10;
        }





    }

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
     *
     * If the Axis is Vertical return <i>true</i>.
     *
     */
    public boolean isVertical() {

        if (orientation == HORIZONTAL) {
            return false;
        } else {
            return true;
        }

    }

    public int getAWidth() {

        return width;

    }

    public int predictWidth() {

        resetRange();



        BufferedImage tempim = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

        Graphics tempg = tempim.getGraphics();



        return getAxisWidth(tempg);

    }

    /**
     *
     * Return the width of the axis.
     *
     * @param g graphics context.
     *
     */
    public int getAxisWidth(Graphics g) {



        int i;

        width = 0;



        if (minimum == maximum) {
            return 0;
        }



        calculateGridLabels();



        //exponent.setText(null);

        /*
         *
         * if(label_exponent != 0) {
         *
         * exponent.copyState(label);
         *
         * //exponent.setText("x 10^"+String.valueOf(label_exponent));
         *
         * cexponent.setText("x 10");//+String.valueOf(label_exponent));
         *
         * cexponent.setExponent(String.valueOf(label_exponent));
         *
         * //title.setText(title.getText()+"
         * ("+"{x10^"+String.valueOf(label_exponent)+"})");
         *
         * }
         *
         */

        if (orientation == HORIZONTAL) {



            //width = clabel.getPreferredSize().height;

            width = ctitle.getPreferredSize().height + 5;

            //width+ = exponen.getPreferredSize().height;

            width += label.getRHeight(g);// + label.getLeading(g); 

            //width += Math.max(title.getRHeight(g),exponent.getRHeight(g));

            max_label_width = width;

            //   width = width+ctitle.getPreferredSize().height;

        } else {

            for (i = 0; i < label_string.length; i++) {

                label.setText(label_string[i]);

                width = LabelAditionalLength + Math.max(label.getRWidth(g), width);

            }



            max_label_width = width;

            width = width + ctitle.getPreferredSize().width + 5;

            //      System.out.print("t:"+width);









            //width = 0;



            if (!title.isNull()) {
                //width = Math.max(width,title.getRWidth(g)+ title.charWidth(g,' '));
                //width = width+ctitle.getPreferredSize().height;
                //Math.max(width,ctitle.getPreferredSize().height+5);
            }



            if (!exponent.isNull()) {
                //width = Math.max(width,exponent.getRWidth(g)+ exponent.charWidth(g,' '));
                //       width = Math.max(width,exponent.getHeight(g));
            }

            //    width += max_label_width;





        }





        return width;

    }

    /**
     *
     * Position the axis at the passed coordinates. The coordinates should match
     *
     * the type of axis.
     *
     * @param xmin The minimum X pixel
     *
     * @param xmax The maximum X pixel. These should be equal if the axis      *
     * is vertical
     *
     * @param ymin The minimum Y pixel
     *
     * @param ymax The maximum Y pixel. These should be equal if the axis      *
     * is horizontal
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

    public Point getMin() {
        return amin;
    }

    public Point getMax() {
        return amax;
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



        Graphics2D g2D = (Graphics2D) g;

        g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);







        //System.out.print("\n\nmaximum before:"+maximum);

        //System.out.print("\nminimum before:"+minimum);



        if (!redraw) {
            return;
        }

        if (minimum == maximum) {



            if (!manualRange) {
                resetRange();
            }

            if (minimum == maximum) {



                return;

            }

        }

        if (amin.equals(amax)) {
            return;
        }



        if (width == 0) {
            width = getAxisWidth(g);
        }





        lg = g.create();





        if (force_end_labels) {

            minimum = label_start;

            maximum = minimum + (label_count - 1) * label_step;

        }







        /*
         *
         ** For rotated text set the Component that is being drawn into
         *
         */

        //title.setDrawingComponent(drawer);

        label.setDrawingComponent(drawer);

        //exponent.setDrawingComponent(drawer);



        cexponent.setFont(exponentfont);



        cexponent.setText("");

        cexponent.setExponent("");



        if (label_exponent != 0) {

            cexponent.setText("x 10");//+String.valueOf(label_exponent));

            cexponent.setExponent(String.valueOf(label_exponent));

        }



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

        ctitle.setText(s);
    }

    public String getTitleText() {
        return ctitle.getText();
    }

    /**
     *
     * Set the color of the title
     *
     * @param c Color of the title.
     *
     */
    public void setTitleColor(Color c) {
        ctitle.setForeground(c);
    }

    /**
     *
     * Set the font of the title
     *
     * @param c Title font.
     *
     */
    public void setTitleFont(Font f) {
        ctitle.setFont(f);
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

        //useManualMaxMin=b;

        //this.force_end_labels=false;

    }

    /*
     *
     *********************
     *
     ** Protected Methods
     *
     *******************
     */
    /**
     *
     * Draw a Horizontal Axis.
     *
     * @param g Graphics context.
     *
     */
    protected void drawHAxis(Graphics g) {

        Graphics lg;

        int i;

        int j;

        int x0, y0, x1, y1;

        int direction;

        int offset;

        double minor_step;



        Color c;



        double vmin = minimum * 1.001;

        double vmax = maximum * 1.001;



        double scale = (amax.x - amin.x) / (maximum - minimum);

        double val;

        double minor;



//          System.out.println("Drawing Horizontal Axis!");





        if (axiscolor != null) {
            g.setColor(axiscolor);
        }



        g.drawLine(amin.x, amin.y, amax.x, amax.y);



        if (position == TOP) {
            direction = 1;
        } else {
            direction = -1;
        }



        minor_step = label_step / (minor_tic_count + 1);

        val = label_start;



        for (i = 0; i < label_count; i++) {

            if (val >= vmin && val <= vmax) {

                y0 = amin.y;

                x0 = amin.x + (int) ((val - minimum) * scale);

                if (Math.abs(label_value[i]) <= 0.0001 && drawzero) {

                    c = g.getColor();

                    if (zerocolor != null) {
                        g.setColor(new Color((int) zerocolor.getRed(), (int) zerocolor.getGreen(), (int) zerocolor.getBlue(), (int) transparency));
                    }

                    g.drawLine(x0, y0, x0, y0 + data_window.height * direction);

                    nullI = x0;

                    g.setColor(c);



                } else if (paintGrid) {

                    c = g.getColor();

                    if (gridcolor != null) {
                        g.setColor(gridcolor);
                    }

                    if (transparency != 255) {

                        Graphics2D g2d = (Graphics2D) g;

                        Paint pn = g2d.getPaint();

                        //if(gridcolor != null) g.setColor(new Color(gridcolor.getRed(),gridcolor.getGreen(),gridcolor.getBlue(),transparency));

                        if (gridcolor != null) {
                            g.setColor(new Color((int) gridcolor.getRed(), (int) gridcolor.getGreen(), (int) gridcolor.getBlue(), (int) transparency));
                        }

                    }

                    g.drawLine(x0, y0, x0, y0 + data_window.height * direction);

                    g.setColor(c);

                }

                x1 = x0;

                y1 = y0 + major_tic_size * direction;

                g.drawLine(x0, y0, x1, y1);

                if (TICS_IN_BOTH_ENDS) {
                    g.drawLine(x0, y0 + data_window.height * direction, x0, y0 + data_window.height * direction + major_tic_size);
                }





            }



            minor = val + minor_step;



            int lastTic = amax.x;



            for (j = 0; j < minor_tic_count; j++) {

                if (minor >= vmin && minor <= vmax) {

                    y0 = amin.y;

                    x0 = amin.x + (int) ((minor - minimum) * scale);

                    if (paintGrid) {

                        c = g.getColor();

                        if (gridcolor != null) {
                            g.setColor(gridcolor);
                        }



                        if (transparency != 255) {

                            Graphics2D g2d = (Graphics2D) g;

                            Paint pn = g2d.getPaint();

                            //if(gridcolor != null) g.setColor(new Color(gridcolor.getRed(),gridcolor.getGreen(),gridcolor.getBlue(),transparency));

                            if (gridcolor != null) {
                                g.setColor(new Color((int) gridcolor.getRed(), (int) gridcolor.getGreen(), (int) gridcolor.getBlue(), (int) transparency));
                            }

                        }







                        if (j != minor_tic_count) {
                            g.drawLine(x0, y0, x0, y0 + data_window.height * direction);
                        }

                        g.setColor(c);

                    }

                    x1 = x0;


                    y1 = y0 + minor_tic_size * direction;









                    lastTic = x0;



                    if (j != minor_tic_count) {
                        g.drawLine(x0, y0, x1, y1);
                    }

                    if (TICS_IN_BOTH_ENDS && j != minor_tic_count) {
                        g.drawLine(x0, y0 + data_window.height * direction, x0, y0 + data_window.height * direction + minor_tic_size);
                    }







                }







                minor += minor_step;

            }



            val += label_step;

        }



        if (this.transform == this.Log_Transform) {

            y0 = amin.y;
            y1 = y0;

            for (int k = 0; k < 100; k++) {
                double low = (double) (amin.x + (minor_step * (k) * scale));
                double high = (double) (amin.x + (minor_step * (k + 1) * scale));

                for (int l = 1; l < 10; l++) {

//                  previously was
//                  int n = (int) (low + ((high - low) * ((expresscomponents.tools.log10((double) l)))));
                    int n = (int) (low + ((high - low) * (Math.log((double) l)/Math.log(10))));

                    if (n < amax.x) {
                        g.drawLine(n, y0, n, y1 - 3);
                    }
                }

                if (low > amax.x) {
                    break;
                }

            }



        }



        if (position == TOP) {

            offset = -label.getLeading(g) - label.getDescent(g);

        } else {

            offset = +label.getLeading(g) + label.getAscent(g);

        }





        val = label_start;

        for (i = 0; i < label_count; i++) {

            if (val >= vmin && val <= vmax) {

                y0 = amin.y + offset;

                x0 = amin.x + (int) ((val - minimum) * scale);

                //if(label_exponent!=0)label.setText(label_string[i]+" E^"+String.valueOf(label_exponent));

                label.setText(label_string[i]);

                label.draw(g, x0, y0 + 5, ChartLabel.CENTER);

            }

            val += label_step;

        }





        /*
         *
         * y0 = amax.y + label.getLeading(g)          *
         * + label.getAscent(g)
         *
         * + title.getLeading(g)
         *
         * + title.getAscent(g);
         *
         */





        y0 = amin.y + label.getAscent(g) + label.getDescent(g) + 5;//.getRWidth(g);// +ctitle.getPreferredSize().height;



        //ctitle.setArea(new Rectangle(0,0,100,100));



        Rectangle Rexp = new Rectangle(amax.x - cexponent.getPreferredSize().width, y0, cexponent.getPreferredSize().width, cexponent.getPreferredSize().height);

        //        cexponent.setAlignment(javax.swing.SwingConstants.RIGHT);

        cexponent.setArea(Rexp);

        cexponent.setBounds(Rexp);

        cexponent.paint(g);







        //Rectangle R = new Rectangle(5 , 2+cexponent.getPreferredSize().height + y0, ctitle.getPreferredSize().width ,amax.y-amin.y- cexponent.getPreferredSize().height);



        Rectangle R = new Rectangle(amin.x, y0, (amax.x - amin.x) - cexponent.getPreferredSize().width, ctitle.getPreferredSize().height);



        ctitle.setArea(R);

        ctitle.setBounds(R);

        ctitle.paint(g);





        /*
         *
         * if( !title.isNull() ) {
         *
         *
         *
         * if(position == TOP ) {
         *
         * y0 = amin.y - label.getLeading(g)          *
         * - label.getDescent(g)
         *
         * - title.getLeading(g)
         *
         * - title.getDescent(g);
         *
         * } else {
         *
         * y0 = amax.y + label.getLeading(g)          *
         * + label.getAscent(g)
         *
         * + title.getLeading(g)
         *
         * + title.getAscent(g);
         *
         * }
         *
         *
         *
         * x0 = amin.x + ( amax.x - amin.x)/2;
         *
         *
         *
         *
         *
         * // title.setFont(new Font("Times New Roman",1,12));
         *
         * // title.setFontName("TIMES NEW ROMAN");
         *
         * // Graphics2D g2D = (Graphics2D) g;
         *
         * // g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
         * RenderingHints.VALUE_TEXT_ANTIALIAS_ON);          *
         * title.draw(g,x0,y0+8,ChartLabel.CENTER);
         *
         *
         *
         * }
         *
         */





        /*          *
         * if( !exponent.isNull() ){
         *
         *
         *
         * x0 = amax.x - exponent.getWidth(g);          *
         *
         *
         * if(position == TOP ) {
         *
         * y0 = amin.y - label.getLeading(g)          *
         * - label.getDescent(g)
         *
         * - title.getLeading(g)
         *
         * - title.getDescent(g);
         *
         * } else {
         *
         * y0 = amax.y + label.getLeading(g)          *
         * + label.getAscent(g)
         *
         * + title.getLeading(g)
         *
         * + title.getAscent(g);
         *
         * }
         *
         *
         *
         *
         *
         *
         *
         * //exponent.setBackground(Color.red);
         *
         * exponent.setJustification(exponent.RIGHT);
         *
         * exponent.setFontSize(12);
         *
         * exponent.setFontStyle(1);
         *
         * exponent.setFontName("TIMES NEW ROMAN");
         *
         *
         *
         * if(title.getText()==null ||title.getText().equals("") )
         * y0+=20;//exponent.getLeading(g)+exponent.getAscent(g);
         *
         * // System.out.print(title);
         *
         * exponent.draw(g,0,0);
         *
         * }
         *
         */



        /*
         *
         * if( !exponent.isNull() ) {
         *
         * //if(orientation==VERTICAL)
         *
         * //exponent.setRotation(45);
         *
         * if(position == TOP ) {
         *
         * y0 = amin.y - label.getLeading(g)          *
         * - label.getDescent(g)
         *
         * - title.getLeading(g)
         *
         * - title.getDescent(g);
         *
         * //- exponent.getLeading(g)
         *
         * //- exponent.getDescent(g);
         *
         * } else {
         *
         * y0 = amax.y + label.getLeading(g)          *
         * + label.getAscent(g)
         *
         * + title.getLeading(g)
         *
         * + title.getAscent(g);
         *
         * //+ exponent.getLeading(g)
         *
         * //+ exponent.getAscent(g);
         *
         * }
         *
         *
         *
         * x0 = amax.x;
         *
         *
         *
         * exponent.draw(g,x0,y0,ChartLabel.LEFT);
         *
         *
         *
         * }
         *
         */









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
        int i, j;
        int x0, y0, x1, y1;
        int direction;
        int offset = 0;
        double minor_step, minor;
        Color c;
        FontMetrics fm;
        Color gc = g.getColor();
        Font gf = g.getFont();
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
                        g.setColor(new Color((int) zerocolor.getRed(), (int) zerocolor.getGreen(), (int) zerocolor.getBlue(), (int) transparency));
                    }

                    g.drawLine(x0, y0, x0 + data_window.width * direction, y0);

                    nullI = y0;

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
                            g.setColor(new Color((int) gridcolor.getRed(), (int) gridcolor.getGreen(), (int) gridcolor.getBlue(), (int) transparency));
                        }

                    }

                    if ((!(i == 0 && dropFirstGridLine)) && (!(i == label_count - 1 && dropLastGridLine))) {
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

                            //if(gridcolor != null) g.setColor(new Color(gridcolor.getRed(),gridcolor.getGreen(),gridcolor.getBlue(),transparency));

                            if (gridcolor != null) {
                                g.setColor(new Color((int) gridcolor.getRed(), (int) gridcolor.getGreen(), (int) gridcolor.getBlue(), (int) transparency));
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

        if (this.transform == this.Log_Transform) {

            x0 = amin.x;
            x1 = x0;

            for (int k = 0; k < 100; k++) {

                double low = (double) (amin.y + (minor_step * (k) * scale));
                double high = (double) (amin.y + (minor_step * (k + 1) * scale));

                for (int l = 9; l > 0; l--) {

//                  previously was
//                  int n = (int) (high - ((high - low) * ((expresscomponents.tools.log10((double) (l))))));
                    int n = (int) (low + ((high - low) * (Math.log((double) l)/Math.log(10))));
                    if (n < amax.y && n > amin.y) {
                        g.drawLine(x0, n, x0 + 2, n);
                    }
                }

                if (low > amax.y) {
                    break;
                }
            }

        }


        val = label_start;
        for (i = 0; i < label_count; i++) {

            if (val >= vmin && val <= vmax) {

                x0 = amin.x + offset - LabelAditionalLength;

                y0 = amax.y - (int) ((val - minimum) * scale) + label.getAscent(g) / 2;



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


        //This is the vertical axis..



        x0 = amin.x;

        y0 = amin.y;

        //y0=amax.y+label.getAscent(g) + label.getDescent(g)+5;//.getRWidth(g);// +ctitle.getPreferredSize().height;



        //ctitle.setArea(new Rectangle(0,0,100,100));



        Rectangle Rexp = new Rectangle(5, y0, cexponent.getPreferredSize().width, cexponent.getPreferredSize().height);

        cexponent.setArea(Rexp);

        cexponent.setBounds(Rexp);

        cexponent.paint(g);





        Rectangle R = new Rectangle(5, cexponent.getPreferredSize().height + y0, ctitle.getPreferredSize().width, amax.y - amin.y - cexponent.getPreferredSize().height);



        ctitle.setArea(R);

        ctitle.setBounds(R);

        ctitle.paint(g);







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









        /*
         *
         * if( !title.isNull() ) {
         *
         *
         *
         * y0 = amin.y + (amax.y-amin.y)/2;
         *
         *
         *
         * if( title.getRotation() == 0 || title.getRotation() == 180 ) {
         *
         * if(position == RIGHT ) {
         *
         * x0 = amin.x + max_label_width + title.charWidth(g,' ');
         *
         * title.draw(g,x0,y0,ChartLabel.LEFT);
         *
         * } else {
         *
         * x0 = amin.x - max_label_width - title.charWidth(g,' ');
         *
         * title.draw(g,x0,y0,ChartLabel.RIGHT);
         *
         * }
         *
         * } else {
         *
         * title.setJustification(ChartLabel.CENTER);
         *
         * if(position == RIGHT ) {
         *
         * x0 = amin.x + max_label_width - title.getLeftEdge(g)+
         *
         * + title.charWidth(g,' ');
         *
         * } else {
         *
         * x0 = amin.x - max_label_width - title.getRightEdge(g)
         *
         * - title.charWidth(g,' ');
         *
         * }
         *
         *
         *
         * //Graphics2D g2D = (Graphics2D) g;
         *
         * //g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
         * RenderingHints.VALUE_TEXT_ANTIALIAS_ON);          *
         * //title.setFont(null);
         *
         *
         *
         * // title.setFont(new Font("Times New Roman",1,12));
         *
         * // title.setFontStyle(1);
         *
         * // title.setFontName("TIMES NEW ROMAN");
         *
         * //g.setFont(new Font("Times New Roman",1,12));
         *
         *
         *
         * title.draw(g,x0,y0);
         *
         * }          *
         *
         *
         * }
         *
         *
         *
         *
         *
         * if( !exponent.isNull() ){
         *
         *
         *
         * y0 = amin.y + exponent.getWidth(g);
         *
         *
         *
         * if( title.getRotation() == 0 || title.getRotation() == 180 ) {
         *
         * if(position == RIGHT ) {
         *
         * x0 = amin.x + max_label_width + title.charWidth(g,' ');
         *
         * title.draw(g,x0,y0,ChartLabel.LEFT);
         *
         * } else {
         *
         * x0 = amin.x - max_label_width - title.charWidth(g,' ');
         *
         * title.draw(g,x0,y0,ChartLabel.RIGHT);
         *
         * }
         *
         * } else {
         *
         *
         *
         * if(position == RIGHT ) {
         *
         * x0 = amin.x + max_label_width - title.getLeftEdge(g)+
         *
         * + title.charWidth(g,' ');
         *
         * } else {
         *
         * x0 = amin.x - max_label_width - title.getRightEdge(g)
         *
         * - title.charWidth(g,' ');
         *
         * }
         *
         *
         *
         * Graphics2D g2D = (Graphics2D) g;
         *
         * g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
         * RenderingHints.VALUE_TEXT_ANTIALIAS_ON);          *
         *
         *
         * //exponent.setForeground(Color.red);          *
         * exponent.setJustification(exponent.LEFT);
         *
         * exponent.setRotation(90);
         *
         * exponent.setFontSize(12);
         *
         * exponent.setFontStyle(1);
         *
         * exponent.setFontName("TIMES NEW ROMAN");
         *
         * exponent.draw(g,0,0);
         *
         *
         *
         * }          *
         *
         *
         * }          *
         */







    }

    /**
     *
     * calculate the labels
     *
     */
    //NOTE: this method was originially protected in JExpress, but this was changed due to the class LineChart needing to use this method,
    //and them being in different packages. Maybe move them to the same package and revert to protected access modifier?
    public void calculateGridLabels() {



        double val;

        //  int i;

        int j;

        boolean ok = false;

        double maxstart = 0.0;

        double minstart = 0.0;





        //The rounding is sometimes different after a number of iterations..

        //Do the iterations in this method instead of having different values after 

        //repainting... (a small bug)..



        if (Double.isNaN(maximum) || Double.isNaN(minimum) || maximum == minimum) {



            maximum = 10.0;

            minimum = -10.0;



            label_step = 0.5;

            label_start = -10;



            label_string = new String[]{"e"};

            label_value = new float[]{-10.0f};



            return;

        }





        while (!ok) {



            maxstart = maximum;

            minstart = minimum;

            double lval = 0.0;



            if (Math.abs(minimum) > Math.abs(maximum)) {
                if (useLog2) {
                    lval = log2(Math.abs(minimum));
                } else {
                    lval = log10(Math.abs(minimum));
                }
                label_exponent = ((int) Math.floor(lval / 3.0)) * 3;
            } else {
                if (useLog2) {
                    lval = log2(Math.abs(maximum));
                } else {
                    lval = log10(Math.abs(maximum));
                }
                label_exponent = ((int) Math.floor(lval / 3.0)) * 3;
            }







            label_step = 0.5;





            label_step = RoundUp((maximum - minimum) / guess_label_number);



            label_start = Math.floor(minimum / label_step) * label_step;









            val = label_start;

            label_count = 1;

            while (val < maximum) {
                val += label_step;
                label_count++;
            }



            label_string = new String[label_count];

            label_value = new float[label_count];



            //      if(orientation == VERTICAL) System.out.print("\nStep:"+label_step);

            //      if(orientation == VERTICAL) System.out.print("\nstart:"+label_start);



            //     if(orientation == VERTICAL) label_step=0.2;

            //     if(orientation == VERTICAL){ label_start=-0.6;



            /*
             *
             * java.math.BigDecimal bd = new java.math.BigDecimal(label_start);
             *
             * bd = bd.setScale(1,java.math.BigDecimal.ROUND_HALF_DOWN);
             *
             * label_start= bd.doubleValue();
             *
             *
             *
             * bd = new java.math.BigDecimal(label_step);
             *
             * bd = bd.setScale(1,java.math.BigDecimal.ROUND_HALF_DOWN);
             *
             * label_step= bd.doubleValue();
             *
             *
             *
             *
             *
             * System.out.print("\nstartAfterround:"+label_start);
             *
             */



            //     }



            //       if(orientation == VERTICAL) System.out.print("\nStep:"+label_step);

            //       if(orientation == VERTICAL) System.out.print("\nstart:"+label_start);

            double t = 0.0;

            //      if(orientation == VERTICAL) System.out.print("\n");

            for (int i = 0; i < label_count; i++) {

                val = label_start + (((double) i) * label_step);

                //t = -0.6 + (i * 0.5);

                if (!Double.isNaN(val)) {

                    java.math.BigDecimal bd = new java.math.BigDecimal(val);

                    bd = bd.setScale(1, java.math.BigDecimal.ROUND_HALF_DOWN);

                    val = bd.doubleValue();

                } else {
                    val = -10;
                }

                //if(orientation == VERTICAL) System.out.print("\n>"+val);

                // if(orientation == VERTICAL) System.out.print("\n>"+String.valueOf(val));





                if (label_exponent < 0) {
                    for (j = label_exponent; j < 0; j++) {
                        val *= 10.0;
                    }
                } else {
                    for (j = 0; j < label_exponent; j++) {
                        val /= 10.0;
                    }
                }



                float fl = 0.0f;

                fl = (float) val;

                //if(transform==Log_Transform) label_string[i] = String.valueOf(fl);

                //else 

                label_string[i] = String.valueOf(fl);

                label_value[i] = fl;

            }











            if (force_end_labels) {

                minimum = label_start;

                maximum = minimum + (label_count - 1) * label_step;

            }



            if (maxstart == maximum && minstart == minimum) {
                ok = true;
            }



        }

        /*          *
         * boolean hundred=true;
         *
         * while(hundred){
         *
         *
         *
         * for(int i=0;i<label_value.length;i++){
         *
         * if((label_value[i]==0.0 || Math.abs(label_value[i])>=1000) &&
         * (label_value[i]/10 == ((int)label_value[i])/10) ) hundred=true;
         *
         * else hundred=false;
         *
         * }
         *
         *
         *
         * if(hundred){
         *
         * for(int i=0;i<label_value.length;i++){
         *
         * label_value[i]=label_value[i]/10;
         *
         * label_string[i] = String.valueOf(label_value);
         *
         * }
         *
         * if(label_exponent<0)label_exponent+=1;
         *
         * else label_exponent-=1;
         *
         * }
         *
         *
         *
         * }
         *
         */

        for (int i = 0; i < label_value.length; i++) {

            float fl = 0.0f;

            fl = (float) label_value[i];

            //if(transform==Log_Transform) label_string[i] = String.valueOf(fl);

            //else 

            label_string[i] = String.valueOf(fl);

            label_value[i] = fl;



        }



    }

    /*
     *
     *******************
     *
     ** Private Methods
     *
     *****************
     */
    /**
     *
     * Round up the passed value to a NICE value.
     *
     */
    private double RoundUp(double val) {

        int exponent;

        int i;


        double lval = 0.0;

        if (useLog2) {
            lval = log2(Math.abs(val));
        } else {
            lval = log10(Math.abs(val));
        }

        exponent = (int) (Math.floor(lval));



        //System.out.print("\nin:"+val);



        if (exponent < 0) {

            for (i = exponent; i < 0; i++) {
                val *= 10.0;
            }

        } else {

            for (i = 0; i < exponent; i++) {
                val /= 10.0;
            }

        }



        if (val > 5.0) {
            val = 10.0;
        } else if (val > 2.0) {
            val = 5.0;
        } else if (val > 1.0) {
            val = 2.0;
        } else {
            val = 1.0;
        }



        if (exponent < 0) {

            for (i = exponent; i < 0; i++) {
                val /= 10.0;
            }

        } else {

            for (i = 0; i < exponent; i++) {
                val *= 10.0;
            }

        }

        //System.out.print("\nout:"+val);

        return val;

        //return 0.5;

    }

    public double log10(double x) throws ArithmeticException {

        if (x < 0.0) {
            throw new ArithmeticException("range exception");
        }

        //return Math.log(x)/2.30258509299404568401;

        return Math.log(x) / Math.log(10);

    }

    public double log2(double x) throws ArithmeticException {

        if (x < 0.0) {
            throw new ArithmeticException("range exception");
        }

        //return Math.log(x)/2.30258509299404568401;

        return Math.log(x) / Math.log(2);

    }
}
