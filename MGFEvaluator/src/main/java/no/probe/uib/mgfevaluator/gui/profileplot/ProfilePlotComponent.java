package no.probe.uib.mgfevaluator.gui.profileplot;

/**
 * Main profile plot chart
 *
 * @author Yehia Mokhtar Farag
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.core.dataset.Group;
import no.uib.jexpress_modularized.core.visualization.BackgroundFactory;
import no.uib.jexpress_modularized.core.visualization.LineStyles.LineMark;
import no.uib.jexpress_modularized.core.visualization.Print.PrintPreview2;
import no.uib.jexpress_modularized.core.visualization.charts.ChartLabel;
import no.uib.jexpress_modularized.core.visualization.charts.GUI.LineProps2;

public class ProfilePlotComponent extends JComponent {

    private JScrollPane scrollPane = new JScrollPane();
    private final boolean start = true;
    private final BackgroundFactory bgf = new BackgroundFactory(new boolean[]{false, true, true, true, true, true, true, true, true, true, true});
    private LineProps2 sp;
    private boolean[] members = null;
    private Hashtable props;
    private Dataset data = null;//new DataSet(new double[][]{{0.0,0.0,0.0},{0.0,0.0,0.0},{0.0,0.0,0.0}},new String[][]{{"  ","  ","  "},{"  ","  ","  "}},new String[]{"  ","  ","  "})    ;
    private Axis yaxis;
    private LabelAxis xaxis;
    private final ChartLabel topText = new ChartLabel("");
    //scrollpic scp;
    private int maxUnitIncrement = 10; //For the scrollable interface..
    private final double minthresholdpercent = 15.0;
    private double minthreshold = 0.0; //settes i paint..
    private final int minover = 1;
    private final int minVisible = 1000;
    private boolean useSpeeding = true;
    private final boolean enableSpeeding = true;
    private Dimension dsize = new Dimension(1500,1500);
    private boolean HorizontalLinesOnly = true;
    private final boolean shadow = true;
    //TODO: document this field (draw)
    private boolean[] draw;
    private final float dashSize = 5;
    private final boolean multiGroups = true;
    //This contains columns which corresponds to gaps in the lines..
    private boolean[] bgaps;
    private final boolean rangeChanged = false;
    public boolean grayScale = false;
    public boolean FullRepaint = true;
    public boolean LockFullRepaint = false;
    private BufferedImage plot;
    public boolean move = false;
    public boolean frame = true;
    public Rectangle valueArea;
    public Rectangle valueframe;  //The frame created by mouse drag
    public boolean paintTags = true;

    public Axis getYaxis() {
        return yaxis;
    }

    public void setYaxis(Axis yaxis) {
        this.yaxis = yaxis;
    }

    public LabelAxis getXaxis() {
        return xaxis;
    }

    public void setAntialias(boolean antialias) {
        this.antialias = antialias;
    }
    private final Color shadowColor = Color.LIGHT_GRAY;// new Color(160, 160, 160);
    private int lineWidth = 1;
    private boolean antialias = false;
    private boolean zoomed = false;
    //For bordering (profiler functions) This is the upper (profile[0]) and lower (profile[1]) borders..
    private int[][] profiler;
    private Vector selectedPoints; //For profiler points that are to be painted another color..
    private int bottom = 45;
    private int left = 60;
    private final int top = 10;
    private int startlabel;
    private int endlabel;
    private int startx, endx;  //For mouse event framing
    private int width, height;

    public ProfilePlotComponent() {
        this(null, null, null);
        width = 1500;
        height = 1500;
        setPreferredSize(new Dimension(width, height));

    }

    public ProfilePlotComponent(Hashtable props, Dataset data, final JScrollPane scroll) {

        if (data != null) {
            this.data = data;
        }
        if (scroll != null) {
            scrollPane = scroll;
        }
        this.props = props;
        sp = getPropsWindow();
        init();
    }

    public final LineProps2 getPropsWindow() {
        LineProps2 tempSp = new LineProps2(null, true);
        bgf.fillCombo(tempSp.SbgSV);
        bgf.fillGradCombo(tempSp.gradtypeSV);
//        readValues(tempSp);
        return tempSp;
    }

    public void setPropertiesAndScrollPane(JScrollPane sp) {
        scrollPane = null;

        if (sp != null) {
            sp.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    //  System.out.println("the lol fole is ( "+xaxis.predictLength()+xaxis.endLength()+yaxis.predictWidth()+"  ,  "+LineChart.this.scrollPane.getViewport().getSize().height);
                    //  dsize=new Dimension(xaxis.predictLength()+xaxis.endLength()+yaxis.predictWidth(),LineChart.this.scrollPane.getViewport().getSize().height);
                    int width = 0;
                    if ((xaxis.predictLength() + xaxis.endLength() + yaxis.predictWidth()) > width) {
                        width = (xaxis.predictLength() + xaxis.endLength() + yaxis.predictWidth());
                    } else {
                        width = 900;
                    }
                    setDsize(new Dimension(width, height));
                    //  System.out.println("the lol fole is ( "+(dsize.calcWidth)+"  ,  "+dsize.height);
                    //
                    // dsize.setSize(500, 400);
                    setPreferredSize(new Dimension(getDsize().width, getDsize().height));
                    setSize(new Dimension(getDsize().width, getDsize().height));
                    forceFullRepaint();
                }
            });
        }
        //  dsize=new Dimension(xaxis.predictLength()+xaxis.endLength()+yaxis.predictWidth(),tempSp.getViewport().getSize().height);
        // dsize=new Dimension(200,200);
//        int calcWidth = 0;
        if ((xaxis.predictLength() + xaxis.endLength() + yaxis.predictWidth()) > width) {
            width = (xaxis.predictLength() + xaxis.endLength() + yaxis.predictWidth());
        } else {
            width = 1500;
        }
        dsize = new Dimension(width, height);
        setPreferredSize(new Dimension(dsize.width, dsize.height));
        setSize(new Dimension(dsize.width, dsize.height));
        scrollPane = sp;
        //this.props = p;
//        readValues(this.sp);
        readForm();
        forceFullRepaint();

    }

    public Rectangle getValueBounds() {
        return new Rectangle(valueArea.x - 1, valueArea.y - 1, valueArea.width + 2, valueArea.height + 2);
    }

    private int[] getSelection(int x, int y) {
        double[] sel = getSelectionRecatangle(x - 1, y - 1, x + 2, y + 2);
//        for(int i=0;i<xaxis.ylocations.length;i++){
//            int value = xaxis.ylocations[i];
//            if(sel[0])
//        
//        }
//         this.xaxis.ylocations
//        for (int x = 0; x < points[0].length; x++) {
//             double pointX = points[0][x];
//             double pointY = points[1][x];
//             if (pointX >= selectRect[0] && pointX <= selectRect[1] && pointY >= selectRect[2] && pointY <= selectRect[3]) {
//                 selectedPoints.add(x);
//             }
//
//         }
        return null;
    }

    public Vector getIndexesAtPoint(Point p, int radius) {
        Vector ret = new Vector();
        Vector dst = new Vector();
        //double rpx = xaxis.getDouble(p.x);
        //double rpy = yaxis.getDouble(p.y);

        boolean[] tolerated = bgf.getTolerated();
        double d = 0.0;
        double d1 = 0.0;
        double d2 = 0.0;
        int index = 0;
        for (boolean b : tolerated) {
            System.out.println(index++ + "    tolerated selecteon : " + b);
        }

        Vector sret = new Vector();
        return sret;
    }

    public void setData(Dataset data) {

        this.data = data;
        yaxis.setValueRange(data.getMinMeasurement(), data.getMaxMeasurement());

        boolean[] visibleRows = data.getusedColInfos();//createVisibleRowIndexes();
        xaxis.setLabels(data.getColInfos(), visibleRows);

        readForm();
        updateForm();
        forceFullRepaint();
    }

    public void updateSize(int width, int height) {
        this.width = width;
        this.height = height;
        setSize(new Dimension(width, height));
        setDsize(new Dimension(width, height));
        readForm();
        updateForm();
//        forceFullRepaint();
    }

    public void setXaxisLabels() {

        boolean[] visibleRows = null;
        String[][] labels = new String[endlabel - startlabel][data.getColInfos()[0].length];
        for (int i = 0; i < labels.length; i++) {
            System.arraycopy(data.getColInfos()[i + startlabel], 0, labels[i], 0, labels[0].length);
        }

        //NOTE: fix
        visibleRows = data.getusedColInfos();//createVisibleRowIndexes();
        xaxis.setLabels(labels, visibleRows);
    }

    public void setMembers(boolean[] members) {
        this.members = members;
        forceFullRepaint();

    }

    public boolean[] getMembers() {
        return members;

    }

    public void setData(Dataset data, int startlabel, int endlabel, double max, double min, boolean[] members) {
        this.data = data;
        this.startlabel = startlabel;
        this.endlabel = endlabel;
        this.members = members;
        zoomed = true;
        yaxis.setManualRange(true);
        yaxis.force_end_labels = true;
        yaxis.setValueRange(min, max);
        setXaxisLabels();

        setSize(new Dimension(xaxis.predictLength() + yaxis.predictWidth(), Height()));
        readForm();
        updateForm();
        forceFullRepaint();
    }

    public double[] getValueFrame() {
        double[] ret = null;

        if (valueframe == null) {
            return null;
        } else {
            ret = new double[4];
            //System.out.print("\nStartLabel: "+xaxis.getLeftLabel(startx));
            //System.out.print("\nEndLabel: "+xaxis.getRightLabel(endx));
            ret[0] = yaxis.getDouble(valueframe.y);
            ret[1] = yaxis.getDouble(valueframe.y + valueframe.height);
            ret[2] = (double) xaxis.getLeftLabel(startx);
            ret[3] = (double) xaxis.getRightLabel(endx) + 1;

        }

        // else return new Rectangle( yaxis.getDouble(valueframe.x,valueframe.y,
        // if(valueframe==null) return null;
        // mainGraph gr= new mainGraph(data,cl,false);
        return ret;

    }

    public void setData(Dataset data, boolean[] members) {

        this.data = data;
        this.members = members;
        yaxis.setValueRange(data.getMinMeasurement(), data.getMaxMeasurement());
        boolean[] visibleRows = data.getusedColInfos();
        if (data.getColumnGroups().size() > 1) {
            Color[] columnLabelColor = new Color[data.getColumnIds().length];
            for (int x = 0; x < columnLabelColor.length; x++) {
                columnLabelColor[x] = Color.BLACK;
            }

            for (int x = 0; x < columnLabelColor.length; x++) {
                for (Group g : data.getColumnGroups()) {
                    if (!g.getName().equalsIgnoreCase("All") && g.hasMember(x)) {
                        columnLabelColor[x] = g.getColor();
                        break;
                    }

                }
            }
            xaxis.setGroupColors(columnLabelColor);
        }

        xaxis.setLabels(data.getColInfos(), visibleRows);
        dsize = new Dimension(xaxis.predictLength() + xaxis.endLength() + yaxis.predictWidth(), Math.max(100, ProfilePlotComponent.this.scrollPane.getSize().height));
        readForm();
        updateForm();
        forceFullRepaint();

    }

    /*
     * //public void showPropertiesWindow(){ public void
     * showPropertiesWindow(Point Location, Frame owner){
     * tempSp.setLocation(LineChart.this.getLocationOnScreen().x+10,LineChart.this.getLocationOnScreen().y+10);
     * if(owner!=null) sp2.setLocationRelativeTo(owner); else
     * sp2.setLocation(Location);
     *
     * //sp.setVisible(true); tempSp.pack(); tempSp.show(); }
     */

    public void setXaxisTitle(String label) {
        xaxis.setTitleText(label);
    }

    public void setYaxisTitle(String label) {
        yaxis.setTitleText(label);

    }

    public final void init() {
        setForeground(Color.BLACK);
        if (scrollPane != null) {
            scrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, new JPanel());
        }
        if (data != null) {
            yaxis = new Axis(1, this, data.getMinMeasurement(), data.getMaxMeasurement());
        } else {
            yaxis = new Axis(1, this, -10, 10);
        }

        yaxis.correctForCloseValues = false;

        boolean[] visibleRows = null;

        if (data != null) {
            xaxis = new LabelAxis(0, this, data.getColInfos(), visibleRows);
        } else {
            xaxis = new LabelAxis(0, this, new String[][]{{"s"}, {" "}}, new boolean[]{true, true});
        }

        xaxis.axiscolor = getForeground();
        yaxis.axiscolor = getForeground();
        yaxis.dropFirstGridLine = true;
        yaxis.dropLastGridLine = true;
        xaxis.dropFirstGridLine = true;
        xaxis.dropLastGridLine = true;
        setXaxisTitle("");
        setYaxisTitle("");
        xaxis.setTitleFont(new Font("Times New Roman", 1, 15));
        yaxis.setTitleFont(new Font("Times New Roman", 1, 15));
        topText.setFont(new Font("Times New Roman", 1, 16));

    }

    public void setDraw(boolean[] draw) {
        this.draw = draw;
        forceFullRepaint();
    }

    public boolean[] getDataSelection(int[] selectedRaw) {
        boolean[] selection = new boolean[data.getDataLength()];
        for (int index = 0; index < data.getDataLength(); index++) {
            boolean selected = false;
            for (int x : selectedRaw) {
                if (index == x) {
                    selected = true;
                    break;
                }
            }
            selection[index] = selected;
        }
        return selection;

    }

    public boolean[] getDraw() {
        return draw;
    }

    public int Height() {
        return getSize().height;
    }

    public int Width() {
        return getSize().width;
    }

    public void forceFullRepaint() {
        FullRepaint = true;
        repaint();
    }

    public void forceFullRepaint(Graphics2D gr) {
        FullRepaint = true;
        paint(gr);
    }

    @Override
    public void paintComponent(Graphics gr) {
        int[] x = null;
        Graphics g = null;
        if (data != null) {
            double mm = data.getMaxMeasurement();
            if (mm < Math.abs(data.getMinMeasurement())) {
                mm = Math.abs(data.getMinMeasurement());
            }
            minthreshold = (minthresholdpercent / 100.0) * mm;
        }

        int totalvis = totalVisble();
        int totalshad = totalShadowed();

        useSpeeding = true;

//        if ((totalvis > minVisible && (totalshad > minVisible || totalshad == 0))) {
//            useSpeeding = true;
//        } else {
//            useSpeeding = false;
//        }
//        if (useSpeeding && !enableSpeeding) {
//            useSpeeding = enableSpeeding; //enablespeeding is set globally and has veto
//        } 
        if (LockFullRepaint) {
            FullRepaint = true;
        }
        boolean bufferAll = true; //Are we buffering all in a bufferedImage or drawing directly?
        if (FullRepaint || LockFullRepaint || !bufferAll) {
            if (props != null && props.containsKey("WholeLine")) {
                HorizontalLinesOnly = (((Boolean) props.get("WholeLine")));
            }

            HorizontalLinesOnly = false; // @TODO: should not be hardcoded...
            yaxis.force_end_labels = sp.endlabelsSV.isSelected();
//            if (!LockFullRepaint && bufferAll) {
//                if (!grayScale) {
//                    plot = new BufferedImage(Width(), Height(), BufferedImage.TYPE_INT_RGB);
//                } else {
//                    plot = new BufferedImage(Width(), Height(), BufferedImage.TYPE_BYTE_GRAY);
//                }
//                g = plot.getGraphics();
//            } else {
//                g = gr;
//            }

            g = gr; // @TODO: should not be hardcoded, but using the BufferedImage does not seem to work...

            if (rangeChanged && !zoomed) {
                double max = sp.ymax.getValue();
                double min = sp.ymin.getValue();
                if (min < max) {
                    yaxis.setManualRange(true);
                    yaxis.setValueRange(min, max);
                }
//                else {
////                    JOptionPane.showMessageDialog(null, "Invalid bounds for chart in dataset\nContinuing with precalculated bounds.", "Error", JOptionPane.ERROR_MESSAGE);
//                }
            }
//            else if (zoomed) {
//                yaxis.setManualRange(true);
//                // if(data.structures.containsKey("ForcedEndLabels") && !zoomed)yaxis.force_end_labels= new Boolean((String)data.structures.get("ForcedEndLabels")).booleanValue();
//                yaxis.force_end_labels = false;
//            }

            if (data == null) {
                return;
            }
            double[][] dat = data.getData();
            boolean[] painted = new boolean[data.getDataLength()];
            int[] y = new int[dat[0].length];
            if (y.length == 1 && !HorizontalLinesOnly) {
                return;
            }
            int TitleHeight = topText.getHeight(g);
            int HTitlePos = Width() / 2 - (topText.getWidth(g) / 2);
            if (Width() == 0 || Height() == 0) {
                return;
            }
            int xaxisLength = xaxis.predictLength();
            g.setColor(getBackground());
            g.fillRect(0, 0, Width(), Height());
            g.setColor(getForeground());
            topText.draw(g, HTitlePos, top + 5);
            left = yaxis.getAxisWidth(g);
            bottom = xaxis.getAxisWidth(g);
            if (!xaxis.positionAxis(left, left + xaxisLength, Height() - bottom, Height() - bottom)) {
                System.out.print("\nFailed to init xaxis");
            }
            if (!yaxis.positionAxis(left, left, top + TitleHeight, Height() - bottom)) {
                System.out.print("\nFailed to init yaxis");
            }
            xaxis.prepareLocations(g);
            x = xaxis.ylocations;
            yaxis.calculateGridLabels();
            yaxis.data_window.setSize(xaxisLength, 10);
            xaxis.data_window.setSize(10, Height() - bottom - top - TitleHeight);
            valueArea = new Rectangle(left, top + TitleHeight, xaxisLength, Height() - bottom - top - TitleHeight);

            //valueArea=this.getVisibleRect();
            if (!LockFullRepaint) {
                g.setClip(valueArea);
            }
            bgf.externalImage = sp.pathSV.getText();
//            bgf.tileImages = sp.tileSV.isSelected();
            bgf.paintBackground(g, valueArea, sp.SbgSV.getSelectedIndex());
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(lineWidth));

            if (antialias) {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            Group group = null;
            boolean activeSet = true;
//            Color classColor = null;

            //Generate the painting order of the lines..----------------------------
            int[] paintorder = new int[data.getDataLength()];
//            LineMark mark = null;
//            Vector[] paintColors = null;
            Color[] singleColors = null;
//            Vector[] Tags = null;

            ArrayList<Group> rowGroups = null;
            //If only single colors are chosen, drawing will be much faster and use less memory
            //if we consider this in this drawing section.
            if (multiGroups) {
//                paintColors = new Vector[data.getDataLength()];
            } else {
                //NOTE: from JExpress
//                singleColors = new Color[data.getGroups().dsize()];
                //NOTE: attempt to reimplement the same functionality with modularized DataSet class:
                rowGroups = (ArrayList<Group>) data.getRowGroups();
                singleColors = new Color[rowGroups.size()];
            }
            if (paintTags) {
//                Tags = new Vector[data.getDataLength()];
            }
            for (int i = 0; i < paintorder.length; i++) {
                paintorder[i] = -1;  //This makes sure that rows not member in any visible group will not be painted
            }
            Font fbefore = g.getFont();
            int[][] integers = null;
            Color[] allGroupColors = new Color[data.getRowGroups().size()];
            int[][] groupColor = new int[data.getRowGroups().size()][];
            @SuppressWarnings("MismatchedReadAndWriteOfArray")
            LineMark[] allmarks = new LineMark[data.getRowGroups().size()];
            int[][] groupMark = new int[data.getRowGroups().size()][];

            int[] buffer = new int[data.getDataLength()];
            int bufferPointer = 0;
            int[] markbuffer = new int[data.getDataLength()];
            int markbufferpointer = 0;
            if (zoomed) {
                integers = yaxis.getInteger(data.getData(), startlabel, endlabel);
            } else {
                integers = yaxis.getInteger(data.getData());
            }
            Color allColor = null;

            if (data.getRowGroups().size() > 0) {
                for (int j = data.getRowGroups().size() - 1; j > -1; j--) {
                    group = (Group) data.getRowGroups().get(j); //NOTE: was method call elementAt(j) (but that was for the class Vector)
                    activeSet = group.isActive();
                    //NOTE: seems important (The GraphView view in J-Express glitches out if this part is commented out)
                    allGroupColors[j] = group.getColor();//(Color) Class.elementAt(2);              
                    if (j == data.getRowGroups().size() - 1) {
                        allColor = group.getColor();
                    }
                    if (activeSet) {
                        for (int i = 0; i < painted.length; i++) {
                            if ((members == null || members[i])) {
                                if (multiGroups) {
                                    if (group.hasMember(i) && j != data.getRowGroups().size() - 1) {
                                        buffer[bufferPointer] = (int) i;
                                        bufferPointer++;
                                    }
                                    if (group.hasMember(i) && paintTags) {
                                        markbuffer[markbufferpointer] = (int) i;
                                        markbufferpointer++;
                                    }
                                } else if (group.hasMember(i)) {
                                    singleColors[j] = group.getColor();//(Color)Class.elementAt(2);
                                }
                                if (activeSet && group.hasMember(i)) {
                                    paintorder[i] = j;
                                }
                            }
                        }
                        //Create the tag and color arrays..
                    }
                    groupColor[j] = new int[bufferPointer];
                    groupMark[j] = new int[markbufferpointer];
                    System.arraycopy(buffer, 0, groupColor[j], 0, bufferPointer);
                    System.arraycopy(markbuffer, 0, groupMark[j], 0, markbufferpointer);
                    markbufferpointer = 0;
                    bufferPointer = 0;
                }
            }
            //remap matrices...
            //remapping colors------------------------------
            int[] tmpmat = new int[data.getDataLength()];
            for (int[] groupColor1 : groupColor) {
                for (int j = 0; j < groupColor1.length; j++) {
                    tmpmat[groupColor1[j]]++;
                }
            }
            int[][] tmpmat2 = new int[data.getDataLength()][];
            for (int i = 0; i < tmpmat2.length; i++) {
                tmpmat2[i] = new int[tmpmat[i]];
            }
            int[] counters = new int[data.getDataLength()];
            int pointer = 0;
            for (int i = 0; i < groupColor.length; i++) {
                for (int j = 0; j < groupColor[i].length; j++) {
                    pointer = groupColor[i][j];
                    tmpmat2[pointer][counters[pointer]] = (int) i;
                    counters[pointer]++;
                }
            }
            groupColor = tmpmat2;
            if (paintTags) {
                java.util.Arrays.fill(tmpmat, (int) 0);
                for (int[] groupMark1 : groupMark) {
                    for (int j = 0; j < groupMark1.length; j++) {
                        tmpmat[groupMark1[j]]++;
                    }
                }
                tmpmat2 = new int[data.getDataLength()][];
                for (int i = 0; i < tmpmat2.length; i++) {
                    tmpmat2[i] = new int[tmpmat[i]];
                }
                java.util.Arrays.fill(counters, 0);
                pointer = 0;
                for (int i = 0; i < groupMark.length; i++) {
                    for (int j = 0; j < groupMark[i].length; j++) {
                        pointer = groupMark[i][j];
                        tmpmat2[pointer][counters[pointer]] = (int) i;
                        counters[pointer]++;
                    }
                }
                groupMark = tmpmat2;
            }
            if (shadow) {
                //Draw the shadowed rows...
                boolean useSpeed = enableSpeeding;
                if ((totalvis - totalshad) < minVisible) {
                    useSpeed = false;
                }

                g.setColor(shadowColor);
                for (int i = 0; i < dat.length; i++) {
                    if ((members == null || members[i]) && draw != null && !draw[i] && paintorder[i] != -1) {
                        drawPolyLine(x, integers[i], dat[i], g, useSpeed);
                        painted[i] = true;
                    }
                }
            }
            //if(true) return;

            float[] dashPattern = null;

            //Paint the lines..-----------------------------------------------------
            if (data.getRowGroups().size() > 0) {

                for (int j = data.getRowGroups().size() - 1; j > -1; j--) {
                    for (int i = 0; i < paintorder.length; i++) {
                        if ((members == null || members[i]) && !painted[i]) {
                            if (paintorder[i] == j) {
                                y = integers[i];
                                if (multiGroups) {
                                    if (groupColor[i].length > 0) {
                                        dashPattern = new float[]{dashSize, dashSize * (groupColor[i].length - 1)};
                                        for (int k = 0; k < groupColor[i].length; k++) {
                                            if (dashPattern != null) {
                                                g2d.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 10, dashPattern, k * dashSize));
                                            }
                                            g.setColor(allGroupColors[groupColor[i][k]]);
                                            drawPolyLine(x, y, dat[i], g2d, false);
                                        }
                                    } else {
                                        g.setColor(allColor);
                                        g2d.setStroke(new BasicStroke(lineWidth));
                                        drawPolyLine(x, y, dat[i], g, false);
                                    }
//                                    if (paintTags && groupMark != null && groupMark[i].length > 0) {
//                                        g2d.setStroke(new BasicStroke(1));
//                                        int step = (x[1] - x[0]);
//                                        for (int m = 0; m < groupMark[i].length; m++) {
//                                            LineMark ct = allmarks[groupMark[i][m]];
//                                            if (ct != null) {
////                                                totwidth += ct.getBoundWidth();
//                                            }
//                                        }
//                                        int mrk = 0;
////                                        int hwdt = 0;
////                                        int hhgt = 0;
//                                        for (int l = 0; l < x.length; l++) {
////                                            widthUsed = 0;
//                                            LineMark ct = allmarks[groupMark[i][mrk % groupMark[i].length]];   //(LineMark) Tags[i].elementAt(m);
//                                            mrk++;
//                                            if (ct != null) {
//                                                ct.paintAt(x[l], y[l], g2d);
//                                            }
//                                        }
//                                    }
                                } else {
                                    g.setColor(singleColors[j]);
                                    drawPolyLine(x, y, dat[i], g, false);
                                }
                                painted[i] = true;
                            }
                        }
                    }
                }
            } //Should not happen.. All datasets should have a nullgroup..
            else {
                for (int i = 0; i < dat.length; i++) {
                    if (!painted[i]) {
                        y = integers[i];
                        drawPolyLine(x, y, dat[i], g, false);
                        painted[i] = true;
                    }
                }
            }
            System.gc();
            g.setFont(fbefore);
            g2d.setStroke(new BasicStroke());
            g.setColor(getForeground());
            g2d.setClip(null);
            xaxis.axiscolor = getForeground();
            yaxis.axiscolor = getForeground();
            if (yaxis != null) {
                yaxis.drawAxis(g);
            }
            g.setClip(null);

            g.setClip(null);
            if (xaxis != null) {
                xaxis.drawAxis(g);
            }
            sp.ymin.setValue(yaxis.minimum);
            sp.ymax.setValue(yaxis.maximum);
            g.setColor(getForeground());
            g.drawLine(left + 1, top + TitleHeight, left + xaxisLength, top + TitleHeight);
            g.drawLine(left + xaxisLength, top + TitleHeight, left + xaxisLength, Height() - bottom);
            firePropertyChange("painted", false, true);

        }
    }

    public void drawPolyLine(int[] x, int[] y, double[] dx, Graphics g, boolean forceall) {
        int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
        if (HorizontalLinesOnly) {
            int step = ((x[1] - x[0]) / 2) - 3;

            for (int i = 0; i < x.length; i++) {
                x1 = x[i] - step;
                y1 = y[i];
                x2 = x[i] + step;
                if (!Double.isNaN(dx[i])) {
                    g.drawLine(x1, y1, x2, y1);
                }
            }
        } else {
            if (!overcenterdist(dx) && (useSpeeding || forceall)) {
                return;
            }

            for (int i = 0; i < x.length - 1; i++) {
                if (!Double.isNaN(dx[i]) && !Double.isNaN(dx[i + 1])) {

                    if (bgaps == null || !bgaps[i]) {
                        x1 = x[i];
                        x2 = x[i + 1];
                        y1 = y[i];
                        y2 = y[i + 1];
                        g.drawLine(x1, y1, x2, y2);
                    }
                }
            }
        }
    }

    public int totalVisble() {
        int ret = 0;
        if (data == null) {
            return 0;
        }
        if (members == null) {
            return data.getDataLength();
        }
        for (int i = 0; i < members.length; i++) {
            if ((members != null && members[i])) {
                ret++;
            }
        }
        return ret;
    }

    public int totalShadowed() {
        int ret = 0;
        if (data == null) {
            return 0;
        }
        if (draw != null) {
            for (int i = 0; i < draw.length; i++) {
                if (draw[i]) {
                    ret++;
                }
            }
            return ret;
        }
        return ret;
    }

    public Image getProfilerImage() {
        BufferedImage cl = new BufferedImage(plot.getWidth(), plot.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics gr = cl.getGraphics();
        gr.drawImage(plot, 0, 0, this);

        if (profiler != null) {
            //gr.setClip(null);
            gr.setClip(((JViewport) getParent()).getViewRect());
            Graphics2D g2d = (Graphics2D) gr;

            int[] x = xaxis.ylocations;
            for (int i = 0; i < profiler[0].length; i++) {
                g2d.setStroke(new BasicStroke(1));
                gr.setColor(Color.green);
                if (selectedPoints != null) {
                    for (int j = 0; j < selectedPoints.size(); j++) {
                        if (((Point) selectedPoints.elementAt(j)).x == i && ((Point) selectedPoints.elementAt(j)).y == 0) {
                            gr.setColor(Color.red);
                        }
                    }
                }

                gr.drawRect(x[i] - 3, profiler[0][i] - 3, 6, 6);
                gr.drawLine(x[i] - 8, profiler[0][i], x[i] + 8, profiler[0][i]);

                gr.setColor(Color.green);
                if (selectedPoints != null) {
                    for (int j = 0; j < selectedPoints.size(); j++) {
                        if (((Point) selectedPoints.elementAt(j)).x == i && ((Point) selectedPoints.elementAt(j)).y == 2) {
                            gr.setColor(Color.red);
                        }
                    }
                }
                gr.drawRect(x[i] - 3, profiler[1][i] - 3, 6, 6);
                gr.drawLine(x[i] - 8, profiler[1][i], x[i] + 8, profiler[1][i]);

                gr.setColor(new Color(160, 250, 160, 160));
                g2d.setStroke(new BasicStroke(2));
                if (profiler[0][i] > profiler[1][i]) {
                    gr.setColor(new Color(250, 160, 160, 160));
                }
                gr.drawLine(x[i], profiler[0][i], x[i], profiler[1][i]);

            }

        }

        return cl;
    }

    public boolean overcenterdist(double[] dat) {
        int cnt = 0;
        for (int i = 0; i < dat.length; i++) {
            if (Math.abs(dat[i]) > minthreshold) {
                cnt++;
                if (cnt == minover) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setProfiler(int[][] profiler) {
        this.profiler = profiler;
        repaint();
    }

//If this is bordered by profiler arrays, return the point at x,y or null if the profiles are empty or no hit is found.
    public Point getProfilerPointAt(int x, int y) {
        getIndexesAtPoint(new Point(x, y), 2);
        System.out.println("y location " + xaxis.ylocations[1] + " length " + xaxis.ylocations.length + "  yaxis ");
        System.out.println("y location " + xaxis.ylocations[2]);
        System.out.println("y location " + xaxis.ylocations[7]);
//        Rectangle r = getSelectionBounds(x, y);
//        Point res = null;
//
//        if (profiler == null) {
//            System.out.print("\nProfiles are null!");
//            return null;
//        } else {
//
//            // System.out.print("\nLooking for points at:" +)
//            for (int i = 0; i < profiler[0].length; i++) {
//                r = new Rectangle(xaxis.ylocations[i] - 3, profiler[0][i] - 3, 6, 6);
//                if (r.contains(x, y)) {
//                    res = new Point(i, 0);
//                    break;
//                }
//                r = new Rectangle(xaxis.ylocations[i] - 3, profiler[1][i] - 3, 6, 6);
//                if (r.contains(x, y)) {
//                    res = new Point(i, 2);
//                    break;
//                }
//            }
//        }

        return null;

    }

    public void printImage() {
        LockFullRepaint = true;
        PrintPreview2 pw = new PrintPreview2(null, true);
        pw.setComponent(this);
        pw.setVisible(true);
        LockFullRepaint = false;
    }

    public Image getImage() {
        return plot;
    }

    public void updateForm() {
        // tempSp.mixticsSV.setValue(xaxis.minor_tic_count);
        sp.transparencySV.setValue(xaxis.transparency);
        sp.miyticsSV.setValue(yaxis.minor_tic_count);
        sp.paintGridSV.setSelected(xaxis.paintGrid & yaxis.paintGrid);
        sp.paintGridSV.setSelected(xaxis.paintGrid & yaxis.paintGrid);
        sp.gridColorSV.setBackground(yaxis.gridcolor);
        sp.Xaxis.setText(xaxis.getTitleText());
        sp.Yaxis.setText(yaxis.getTitleText());

    }

    public void readForm() {
//        topText.setText(sp.Header.getText());
        //   topText.setText("koko wawa");

        xaxis.paintGrid = yaxis.paintGrid = true;//sp.paintGridSV.isSelected();

        xaxis.transparency = sp.transparencySV.getValue();
        yaxis.transparency = sp.transparencySV.getValue();

        xaxis.minimumSize = height;//sp.mixsizeSV.getValue();

        xaxis.gridcolor = Color.LIGHT_GRAY;//sp.gridColorSV.getBackground();
        yaxis.gridcolor = Color.LIGHT_GRAY;//sp.gridColorSV.getBackground();

        if (sp.Xaxis.getText().length() > 0) {
            xaxis.setTitleText(sp.Xaxis.getText());
        }
        if (sp.Yaxis.getText().length() > 0) {
            yaxis.setTitleText(sp.Yaxis.getText());
        }

        xaxis.TICS_IN_BOTH_ENDS = sp.xticsbothsidesSV.isSelected();
        yaxis.TICS_IN_BOTH_ENDS = sp.yticsbothsidesSV.isSelected();

        yaxis.minor_tic_count = sp.miyticsSV.getValue();

        xaxis.setRotated(sp.RotXlabelsSV.isSelected());

        bgf.GradientType = 1;//sp.gradtypeSV.getSelectedIndex();
        bgf.Single = Color.WHITE;// tempSp.singlebgSV.getColor();

        bgf.gradient1 = sp.grad1SV.getColor();
        bgf.gradient2 = sp.grad2SV.getColor();

        lineWidth = sp.lineSizeSV.getValue();
        //setSize(400, 400);
        setBackground(Color.WHITE);//sp.chartbgSV.getColor());
        setForeground(Color.GRAY);//sp.AxisColorSV.getBackground());
        if (scrollPane != null) {
            scrollPane.getViewport().setOpaque(true);
        }
        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(Color.WHITE);//sp.chartbgSV.getColor());
        }

//        int calcWidth =  Math.max(xaxis.predictLength() + yaxis.predictWidth() + xaxis.endLength(), sp.mixsizeSV.getValue() + xaxis.endLength());
        Dimension dim = new Dimension(900, 700);//calcWidth, sp.minHeightSV.getValue());

        setMinimumSize(dim);
        setPreferredSize(dim);
        setSize(dim);

    }

//    public void writeValues() {
//        Tools to = new Tools();
//        if (props != null) {
//            to.writedialogStatus(sp, "LineChart", props);
//        }
//    }
//    public void readValues(JDialog sp) {
//        Tools to = new Tools();
//////        if (props != null) {
//////            to.readialogStatus(sp, "LineChart", props);
//////        }
//    }
//  
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public void setMaxUnitIncrement(int pixels) {
        maxUnitIncrement = pixels;
    }

    public Dimension getDsize() {
        return dsize;
    }

    public void setDsize(Dimension dsize) {
        this.dsize = dsize;
    }

    @Override
    public void paintAll(Graphics graphic) {
        super.paint(graphic);
    }

    private double[] getSelectionRecatangle(int startX, int startY, int endX, int endY) {
        double[] selectionRect = new double[4];
        int maxXM = Math.max(startX, endX);
        int minXM = Math.min(startX, endX);
        int maxYM = Math.max(startY, endY);
        int minYM = Math.min(startY, endY);

        int yAxisFactor = this.left - this.yaxis.predictWidth();
        int xAxixFactor = this.bottom - this.xaxis.width;
        int plotWidthArea = (this.getWidth() - this.left - this.endx) + (yAxisFactor);
        int plotHeightArea = this.getHeight() - this.top - this.bottom + xAxixFactor;

        if ((minXM < (this.left - yAxisFactor) && maxXM < (this.left - yAxisFactor)) || (minXM > (this.left + plotWidthArea))) {
            return null;
        }
        if ((minYM < this.top && maxXM < this.left) || (minYM > this.top + plotHeightArea)) {
            return null;
        }
//         System.out.println("plot.left  "+plot.left+"   plot.yaxis.predictWidth() "+plot.yaxis.getAWidth()+"  plot.bottom  " +plot.bottom +"plot.xaxis.predictWidth()  "+plot.xaxis.getAWidth()+"  yAxisFactor  "+yAxisFactor+"   xAxisFactor "+xAxixFactor);
        minXM = minXM - this.left + yAxisFactor;
        maxXM = maxXM - this.left + yAxisFactor;
        minYM -= this.top;
        maxYM -= this.top;

        if ((minXM < 0 && maxXM >= 0)) {
            minXM = 0;// plot.left;
        }
        if (maxXM > plotWidthArea && minXM >= 0) {
            maxXM = plotWidthArea;
        }
        if ((minYM <= 0 && maxYM > 0))//plot.top))
        {
            minYM = 0;//plot.top;
        }
        if (maxYM > plotHeightArea && minYM >= 0) {
            maxXM = plotHeightArea;
        }

        double xDataArea = this.xaxis.maximum - this.xaxis.minimum;
        double xUnitPix = xDataArea / (double) plotWidthArea;
        double modStartX = (minXM * xUnitPix) + this.xaxis.minimum;//xstart units from min    
        selectionRect[0] = modStartX;
//         zoomedRect[0]=modStartX;
        double modEndX = (maxXM * xUnitPix) + this.xaxis.minimum;
//         zoomedRect[1]=modEndX;
        selectionRect[1] = modEndX;

        double yDataArea = this.yaxis.maximum - this.yaxis.minimum;
        double yUnitPix = yDataArea / (double) plotHeightArea;
        double modStartY = this.yaxis.maximum - (maxYM * yUnitPix);
        selectionRect[2] = modStartY;
//         zoomedRect[2]= modStartY;
        double modEndY = this.yaxis.maximum - (minYM * yUnitPix);
        selectionRect[3] = modEndY;

        return selectionRect;

    }
}
