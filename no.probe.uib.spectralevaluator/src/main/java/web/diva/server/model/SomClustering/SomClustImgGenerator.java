/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.diva.server.model.SomClustering;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import javax.swing.JLabel;
import no.uib.jexpress_modularized.core.dataset.AnnotationLibrary;
import no.uib.jexpress_modularized.core.dataset.AnnotationManager;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.core.dataset.Group;
import no.uib.jexpress_modularized.core.visualization.colors.colorcomponents.ColorFactory;
import no.uib.jexpress_modularized.core.visualization.colors.ui.ScaleAndAxis;
import no.uib.jexpress_modularized.somclust.model.Node;
import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.encoders.ImageFormat;

/**
 *
 * @author Yehia Farag
 */
public class SomClustImgGenerator {

    private TreeView upperTree, sideTree;
    private final Node rowNode, colNode;
    private final boolean gengenscale = false;
    private final Color GridCol = Color.DARK_GRAY;

    private final int squareW;//= 12;
    private final int squareL;//= 2;
    private final int LeftTreeWidth;//= 350;
    private final int TopTreeHeight = 70;
    private int LeftTreeHeight, TopTreeWidth;

    public int getLeftTreeHeight() {
        return LeftTreeHeight;
    }

    public int getTopTreeWidth() {
        return TopTreeWidth;
    }

    private final boolean ValueDistances = true;
    private ColorFactory colors;
    private final java.text.NumberFormat numformat;

    public SomClustImgGenerator(Node rowNode, Node colNode, int rowNumber) {
        this.rowNode = rowNode;
        this.colNode = colNode;
        numformat = java.text.NumberFormat.getNumberInstance(java.util.Locale.US);
        numformat.setMaximumFractionDigits(3);
        numformat.setMinimumFractionDigits(1);
        if (rowNumber <= 1000) {
            squareL = 4;
            squareW = 12;
            LeftTreeWidth = 200;

        } else if (rowNumber <= 10000) {
            squareL = 4;
            squareW = 12;
            LeftTreeWidth = 300;

        } //        else if(rowNumber <= 20000){
        //        squareL = 4;
        //        squareW = 12;
        //        LeftTreeWidth = 500;       
        //        
        //        }
        else {
            squareL = 2;
            squareW = 12;
            LeftTreeWidth = 2000;
        }

    }

    public int getSquareW() {
        return squareW;
    }

    public int getSquareL() {
        return squareL;
    }

    public TreeView getUpperTree() {
        return upperTree;
    }

    public TreeView getSideTree() {
        return sideTree;
    }

    public BufferedImage generateSideTree(Node root) {
        int verticalItems = countgenes(root);
        root.mark = true;
        sideTree = new TreeView(root, verticalItems, Color.WHITE, Color.BLACK);//"#e3e3e3"Color.decode("#e3e3e3")
        sideTree.leafdist = squareL;
        sideTree.actualLength = verticalItems;
        sideTree.leftmargin = (int) Math.round(squareL / 2);
        sideTree.drawframe = false;
        sideTree.valuedistances = true;
        sideTree.rightmargin = 0;
        sideTree.drawrects = false;
        sideTree.bottommargin = 0;
        //if(result!=null)
        sideTree.treewidth = LeftTreeWidth;
        sideTree.generatecoords();
        LeftTreeHeight = sideTree.getHeight();
        sideTreeBImg = sideTree.getImage();
        return (sideTreeBImg);

    }

    public BufferedImage generateTopTree(Node toproot) {
        int horizontalItems = countgenes(toproot);
        toproot.mark = true;
        upperTree = new TreeView(toproot, horizontalItems, Color.WHITE, Color.black);//Color.decode("#e3e3e3")
        upperTree.leafdist = squareW;
        upperTree.actualLength = horizontalItems;
        upperTree.setHorizontal(false);
        upperTree.leftmargin = (int) Math.round(squareW / 2);
        upperTree.drawframe = false;
        upperTree.valuedistances = ValueDistances;
        upperTree.topmargin = 0;
        upperTree.drawrects = false;
        upperTree.bottommargin = 0;
        upperTree.rightmargin = 0;
        upperTree.treewidth = TopTreeHeight;
        upperTree.generatecoords();
        upperTree.repaint();
        TopTreeWidth = upperTree.getWidth();
        upperTreeBImg = upperTree.getImage();
        return upperTreeBImg;// this.generateEncodedImg(upperTreeBImg);

    }

    private BufferedImage heatMapImg;

    public BufferedImage generateHeatMap(Dataset dataset, boolean clustColumn) {

        heatMapImg = null;
        if (clustColumn) {
            heatMapImg = new BufferedImage((upperTree.getWidth() + squareW), (sideTree.getHeight()), BufferedImage.TYPE_INT_ARGB);
        } else {
            heatMapImg = new BufferedImage((dataset.getColumnIds().length * squareW + squareW), (sideTree.getHeight()), BufferedImage.TYPE_INT_ARGB);
            TopTreeWidth = dataset.getColumnIds().length * squareW + squareW;

        }
        Graphics g = heatMapImg.getGraphics();
        g.setFont(getTableFont(12));
        drawSquares(g, new Point(0, 0), null, dataset, clustColumn);
        return heatMapImg;
    }

    public String getNavgStringImg() {
        return navgStringImg;
    }
    private String navgStringImg;
    private BufferedImage interactiveColumnImg;

    public String generateScale(Dataset dataset, boolean clustColumn) {
        int W = 0;
        if (clustColumn) {
            W = (Math.min((upperTree.getWidth() + 21), 250));
        } else {
            W = (Math.min((dataset.getColumnIds().length * 12 + 21), 250));
        }
        BufferedImage nfo = new BufferedImage(W, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics g = nfo.getGraphics();
        g.setFont(getTableFont(9));
        drawScale(g, new Point(0, 0), W, 30);
        return this.generateEncodedImg(nfo);

    }

    private Rectangle getSquaresBounds(Dataset dataset) {
        int[] upperArrangement = null;
        if (upperTree == null || upperTree.arrangement == null) {
            upperArrangement = new int[dataset.getDataWidth()];
        } else {
            upperArrangement = upperTree.arrangement;
        }
        if (upperArrangement == null || sideTree == null) {
            return new Rectangle(0, 0);
        }
        return new Rectangle(upperArrangement.length * squareW + 1, (sideTree.actualLength * squareL) + 1);
    }

    private void drawScale(Graphics scale, Point st, int width, int height) {
        Rectangle r = new Rectangle(st.x, st.y, width, height);
        if (width < 50 || height < 25) {
            return;
        }
        ScaleAndAxis sc = new ScaleAndAxis();
        sc.setColorFactory(colors);
        scale.translate(st.x, st.y);
        Rectangle bac = scale.getClipBounds();
        sc.setLocation(r.x, r.y);
        sc.setSize(r.width, r.height);
        sc.paintComponent(scale);
        scale.setClip(bac);
        scale.translate(-st.x, -st.y);
    }

//    public String exportToImgGraphics(boolean clustColumn){
//        int totalWidth = sideTreeBImg.getWidth()+ heatMapImg.getWidth()+interactiveColumnImg.getWidth();
//        int totalHeight = 0;
//        totalHeight = sideTree.getImage().getHeight();
//        if (clustColumn) {
//            totalHeight += TopTreeHeight;
//        }
//
//        BufferedImage clusteringImage = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
//        Graphics gg = clusteringImage.getGraphics();
//        Graphics2D g2d = (Graphics2D)gg;
//        g2d.setBackground(Color.WHITE);
////        gg.translate(sideTree.getImage().getWidth(), 0);
//        gg.drawImage(upperTreeBImg, sideTreeBImg.getWidth(), 0, null);
//        gg.drawImage(sideTreeBImg, 0,upperTreeBImg.getHeight(), null);
//        gg.drawImage(heatMapImg, sideTreeBImg.getWidth(), upperTreeBImg.getHeight(), null);
//        gg.drawImage(interactiveColumnImg, sideTreeBImg.getWidth() + heatMapImg.getWidth(),upperTreeBImg.getHeight(), null);
//        System.out.println("start the new drawer for clustering");
//        try {
//            ImageIO.write(clusteringImage, "PNG", new File("C:\\Users\\y-mok_000\\Desktop", "img.png"));
//        } catch(Exception exp){exp.printStackTrace();}
//        
//        
//        return "";
//    }
    public BufferedImage getHeatMapImg() {
        return heatMapImg;
    }

    public BufferedImage getInteractiveColumnImg() {
        return interactiveColumnImg;
    }

    public BufferedImage getSideTreeBImg() {
        return sideTreeBImg;
    }

    public BufferedImage getUpperTreeBImg() {
        return upperTreeBImg;
    }

    private void drawSquares(Graphics squares, Point start, Rectangle bounds, Dataset dataset, boolean clusterColumns) {
//        ColorFactory colors = ColorFactoryList.getInstance().getActiveColorFactory(dataset);
        colors = new ColorFactory();

        Rectangle view = getSquaresBounds(dataset);
        squares.translate(start.x, start.y);
        int rows = this.countgenes(this.rowNode);
        int counter = 0;
        double[] gengenscalevals = null;
        int[] upperArrangement = null;
        if (clusterColumns) {
            upperArrangement = upperTree.arrangement;
        } else {
            upperArrangement = new int[dataset.getColumnIds().length];
            for (int x = 0; x < dataset.getColumnIds().length; x++) {
                upperArrangement[x] = x;
            }
        }
        double[][] dat = null;
        dat = dataset.getData();
        if (sideTree == null) {
            return;
        }
        for (int i = 0; i < sideTree.arrangement.length; i++) {
            double v = 0;
            Rectangle sqr = new Rectangle(0, 0, squareW, squareL);
            for (int j = 0; j < upperArrangement.length; j++) {
                if (bounds == null || bounds.intersects((j * squareW), (i * squareL), squareW, squareL)) {

                    if (upperTree != null) {

                        sqr.setLocation((j * squareW), (i * squareL));
                        if (!view.intersects(sqr)) {
                            continue;
                        }

                        if (sideTree.arrangement[i] != -1 && upperArrangement[j] != -1) {

                            if (dataset.isMissing(sideTree.arrangement[i], upperArrangement[j])) {
                                squares.setColor(colors.getMissing());
                            } else {
                                if (!gengenscale) {
                                    v = dat[sideTree.arrangement[i]][upperArrangement[j]];
                                    squares.setColor(colors.getColor(v));
                                } else {
                                    v = gengenscalevals[upperArrangement[j]];
                                    squares.setColor(colors.getColor(v));
                                }
                            }
                            squares.fillRect((j * squareW), (i * squareL), squareW, squareL);
                        }
                    } else {
                        sqr.setLocation((j * squareW), (i * squareL));
                        if (!view.intersects(sqr)) {
                            continue;
                        }

                        v = dat[sideTree.arrangement[i]][upperArrangement[j]];

                        if (dataset.isMissing(sideTree.arrangement[i], upperArrangement[j])) {
                            squares.setColor(colors.getMissing());
                        } else {
                            squares.setColor(colors.getColor(v));
                        }

                        squares.fillRect((j * squareW), (i * squareL), squareW, squareL);
                    }
                }
            }
            counter++;
            if (counter == rows) {
                break;
            }
        }
        counter = 0;
        if (true) {
            squares.setColor(GridCol);
            for (int i = 0; i < sideTree.arrangement.length + 1; i++) {
                if (bounds == null || bounds.intersects(0, i * squareL, upperArrangement.length * squareW, i * squareL)) {
                    squares.drawLine(0, i * squareL, (upperArrangement.length * squareW) + 0, i * squareL);
                }
                counter++;
                if (counter > rows) {
                    break;
                }
            }
            for (int j = 0; j < upperArrangement.length; j++) {
                if (bounds == null || bounds.intersects(j * squareW, 0, j * squareW, rows * squareL)) {
                    squares.drawLine(j * squareW, 0, j * squareW, rows * squareL);
                }
            }

            if (bounds == null || bounds.intersects(upperArrangement.length * squareW, 0, upperArrangement.length * squareW, rows * squareL)) {
                squares.drawLine(upperArrangement.length * squareW, 0, upperArrangement.length * squareW, rows * squareL);
            }

        }
        squares.translate(-start.x, -start.y);
    }
    private int[] Wd;
    private int[] WdSUM;

    public void drawTable(Graphics gr, Point UL, Dataset dataset, int[] selection, Graphics navgGr, int countNavUnit) {

        Font f = getTableFont(squareL - 1);
        AnnotationManager annManager = AnnotationManager.getAnnotationManager();
        String[] rowIds = dataset.getRowIds();

        Set<String> annotations = dataset.getRowAnnotationNamesInUse();
        if (annotations == null) {
            annotations = annManager.getManagedRowAnnotationNames();
        }

        String[][] inf;   // row annotation matrix
        String[] headers;  // header of the row annotation matrix
        if (annotations.isEmpty()) {
            inf = new String[dataset.getDataLength()][1];
            for (int i = 0; i < inf.length; i++) {
                inf[i][0] = rowIds[i];
            }
            headers = new String[]{"Row ID"};
        } else {
            headers = annotations.toArray(new String[annotations.size()]);
            inf = new String[dataset.getDataLength()][annotations.size()];
            for (int i = 0; i < headers.length; i++) {
                //ann manager need to re implemeinted?
                AnnotationLibrary anns = annManager.getRowAnnotations(headers[i]);
                for (int j = 0; j < inf.length; j++) {
                    inf[j][i] = rowIds[j];//anns.getAnnotation(rowIds[j]);//
                }
            }
        }

        Graphics2D g2d = (Graphics2D) gr;
        Graphics2D g2dNav = (Graphics2D) navgGr;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2dNav.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int X = UL.x;
        int Y = UL.y;
//        int H = squareL;

        int L = dataset.getDataLength();
        int W = headers.length;

        JLabel l = new JLabel("    ");
        JLabel lNav = new JLabel(" ");
//        l.setFont(f);
//        l.setIconTextGap(2);
        javax.swing.border.Border UB = javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 0, Color.WHITE);
        javax.swing.border.Border LB = javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 0, Color.WHITE);

//          Color borderColor = hex2Rgb("#e3e3e3");
        javax.swing.border.Border navBorder = javax.swing.BorderFactory.createMatteBorder(2, 0, 0, 0, Color.WHITE);

        l.setMaximumSize(new Dimension(200, squareL));
        lNav.setSize(new Dimension(2, 5));
        lNav.setBorder(navBorder);

        boolean drawTableHeader = false;

        //if there is not enough room for a header.. skip header.
//        if (UL.y < squareL) {
//            drawTableHeader = false;
//        }
        if (Wd == null) {
            Wd = new int[inf[0].length];
            WdSUM = new int[inf[0].length];

            if (drawTableHeader) {
                for (int i = 0; i < headers.length; i++) {
                    l.setText(headers[i]);
                    l.validate();
                    if (l.getPreferredSize().width > Wd[i]) {
                        Wd[i] = l.getPreferredSize().width + 16;
                    }
                }
            }
            for (String[] inf1 : inf) {
                for (int j = 0; j < Wd.length; j++) {
                    if (squareL < 6) {
                        Wd[j] = 5;
                        continue;
                    }
                    l.setText(inf1[j]);
                    l.validate();
                    if (l.getPreferredSize().width > Wd[j]) {
                        Wd[j] = l.getPreferredSize().width + 16;
                    }
                }
            }

            WdSUM[0] = 0;

            for (int i = 0; i < Wd.length; i++) {
                WdSUM[i] = -1;
                for (int j = 0; j < i; j++) {
                    WdSUM[i] += Wd[j] + 3;
                }
            }
        }

        Rectangle BNDS = new Rectangle();

        l.setBackground(Color.WHITE);
        l.setOpaque(true);

        lNav.setBackground(Color.WHITE);
        lNav.setOpaque(true);

        if (sideTree == null) {
            return;
        }

        f = getTableFont(squareL - 1);
        l.setFont(f);

        int[] LArr = sideTree.arrangement;
        int Rindex = 0;

        //draw the table header.. (if wanted)
//        if (drawTableHeader) {
//
//            l.setBackground(Color.WHITE);
//            l.setForeground(Color.white);
//
//            for (int j = 0; j < W; j++) {
//                X = UL.x + WdSUM[j];
//                Y = UL.y;
//                BNDS.setBounds(X, Y, Wd[j], squareL + 1);
//
//                if (gr.getClipBounds() != null && !gr.getClipBounds().intersects(BNDS)) {
//                    continue;
//                }
//                gr.translate(X, Y);
//                l.setBounds(0, 0, Wd[j] + 1, squareL + 1);
//                l.setBorder(LB);
//
//                if (squareL >= 6) {
//                    l.setText(headers[j]);
//                }
//                l.validate();
//                l.paint(gr);
//                gr.translate(-X, -Y);
//            }
//        }
        l.setForeground(Color.WHITE);

        boolean[] sel = selectedRows((selection == null ? null : selection), dataset);
        boolean coloredNav = false;
        int navCounter = 0;
        for (int i = 0; i < L; i++) {

            Rindex = LArr[i];
            for (int j = 0; j < W; j++) {
                X = UL.x + WdSUM[j];
                Y = UL.y + (squareL * (i + 1));

                BNDS.setBounds(X, Y, Wd[j], squareL + 1);

                if (gr.getClipBounds() != null && !gr.getClipBounds().intersects(BNDS)) {
                    continue;
                }

                if (sel[LArr[i]]) {

                    for (Group group : dataset.getRowGroups()) {
                        if (group.isActive()) {
                            if (group.hasMember(Rindex)) {
                                l.setBackground(group.getColor());
                                if (!coloredNav) {
                                    lNav.setBackground(Color.RED);
                                    lNav.setForeground(Color.RED);
                                    coloredNav = true;
                                }

                                break;

                            }
                        }

                    }

//                    l.setBackground(new Color(225, 225, 255));
                } else {
//                   
//                    if (!coloredNav) {
//                                    lNav.setBackground(Color.WHITE);
//                                    lNav.setForeground(Color.WHITE);                                  
//                                }

                    l.setBackground(Color.WHITE);
                }
                if (i != 0) {
                    gr.translate(X, Y);
                }
                l.setBounds(0, 0, Wd[j] + 1, squareL + 1);

                if (i < L - 1) {
                    l.setBorder(UB);
                } else {
                    l.setBounds(0, 0, Wd[j] + 1, squareL + 1);
                    l.setBorder(LB);
                }
                if (squareL >= 6) {
                    l.setText(inf[Rindex][j]);
                }
                l.validate();
                l.paint(gr);
                gr.translate(-X, -Y);

            }
            if (navCounter >= countNavUnit) {
                navCounter = 0;
                lNav.validate();
                lNav.paint(navgGr);
                navgGr.translate(2, 0);
                coloredNav = false;
                lNav.setBackground(Color.WHITE);
                lNav.setForeground(Color.WHITE);

            }
            navCounter++;
        }

//        if (squareL < 6) {
//            return;
//        }
//
//        l.setBackground(Color.WHITE);
//        f = getTableFont(squareL - 2);
//        //f = new Font("Arial",1,squareL-2);
//        l.setFont(f);
//
//
//        for (int j = 0; j < W; j++) {
//            X = UL.x + WdSUM[j];
//            Y = UL.y;
//
//            BNDS.setBounds(X, Y, Wd[j], squareL + 1);
//            if (gr.getClipBounds() != null && !gr.getClipBounds().intersects(BNDS)) {
//                continue;
//            }
//
//            gr.translate(X, Y);
//            l.setBounds(0, 0, Wd[j], squareL + 1);
////            l.setBorder(javax.swing.BorderFactory.createLineBorder(GridCol));
//            l.setText(headers[j]);
//            l.validate();
//            gr.translate(-X, -Y);
//        }
    }

    private boolean[] selectedRows(int[] selectedRows, Dataset dataset) {
        boolean[] ret = new boolean[dataset.getDataLength()];
        if (selectedRows != null) {
            for (int i = 0; i < selectedRows.length; i++) {
                ret[selectedRows[i]] = true;
            }
        }
//        for (int i = 0; i < draggedOverIndices.size(); i++) {
//            ret[((Integer) draggedOverIndices.elementAt(i)).intValue()] = true;
//        }
        return ret;
    }

    private Font getTableFont(int size) {
        Font f;
        f = new Font("Sans Serif", 0, size);
        return f.deriveFont((float) size);
    }

    public int countgenes(Node trunk) {
        java.util.Stack c = new java.util.Stack();
        int ret = 0;
        c.push(trunk);
        Node tr = trunk;

        if (trunk == null) {
            System.out.print("\n!No trunk\n");
        }

        while (!c.empty()) {
            tr = (Node) c.pop();

            if (tr.merged) {
                c.push(tr.left);
                c.push(tr.right);
            } else {
                ret++;
            }
        }

        return ret;
    }

    public Node getNodeAt(int xcor, int ycor, Node trunk) {
        Node ret = null;

        if (trunk != null) {

            if (trunk.getx() > xcor - squareL && trunk.getx() < xcor + squareL
                    && trunk.gety() > ycor - squareL && trunk.gety() < ycor + squareL) {
                ret = trunk;
            } else {
                ret = getNodeAt(xcor, ycor, trunk.right);
            }
            if (ret == null) {
                ret = getNodeAt(xcor, ycor, trunk.left);
            }
        }

        return ret;
    }

    private String generateEncodedImg(BufferedImage upperTreeBImage) {
        String sideTreeBase64 = "";
        try {
            ImageEncoder in = ImageEncoderFactory.newInstance(ImageFormat.PNG, 0);

            byte[] imageData = in.encode(upperTreeBImage);
            sideTreeBase64 = Base64.encodeBase64String(imageData);
            sideTreeBase64 = "data:image/png;base64," + sideTreeBase64;
            System.gc();
        } catch (IOException exp) {
            System.err.println(exp.getLocalizedMessage());
        }
        return sideTreeBase64;
    }

    private BufferedImage sideTreeBImg;

    private BufferedImage upperTreeBImg;

    public int getLeftTreeWidth() {
        return LeftTreeWidth;
    }

    public int getTopTreeHeight() {
        return TopTreeHeight;
    }

    /**
     *
     * @param colorStr e.g. "#FFFFFF"
     * @return
     */
    private Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }

    private BufferedImage rotateImage(BufferedImage masterImage, int angle) {
        int virtualAngle = getVirtualAngle(angle);
        Dimension size = new Dimension(masterImage.getWidth(), masterImage.getHeight());
        int masterWidth = masterImage.getWidth();
        int masterHeight = masterImage.getHeight();
        double x = 0; //masterWidth / 2.0;
        double y = 0; //masterHeight / 2.0;
        switch (virtualAngle) {
            case 0 -> {
            }
            case 180 -> {
            }
            case 90, 270 -> {
                size = new Dimension(masterImage.getHeight(), masterImage.getWidth());
                x = (masterHeight - masterWidth) / 2.0;
                y = (masterWidth - masterHeight) / 2.0;
            }
        }
        BufferedImage renderedImage = new BufferedImage(size.width, size.height, masterImage.getTransparency());
        Graphics2D g2d = renderedImage.createGraphics();
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.rotate(Math.toRadians(virtualAngle), masterWidth / 2.0, masterHeight / 2.0);
        g2d.drawImage(masterImage, at, null);
        g2d.dispose();
        return renderedImage;
    }

    protected int getVirtualAngle(int angle) {
        float fRotations = (float) angle / 360f;
        int rotations = (int) (fRotations - (fRotations / 1000));

        int virtual = angle - (rotations * 360);

        if (virtual < 0) {
            virtual = 360 + virtual;
        }

        return virtual;
    }
}
