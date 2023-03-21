/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.probe.uib.mgfevaluator.gui.pca;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;
import javax.swing.border.Border;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.pca.computation.PcaResults;
import no.uib.jexpress_modularized.pca.model.ArrayUtils;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.jfree.chart.ChartUtilities;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author Yehia Farag
 */
public class PCAImageGenerator implements Serializable{
    //For speeding lookup

    private final Dataset dataset;
    private final PcaPlot plot;
    private final no.uib.jexpress_modularized.pca.computation.PcaResults pcaResults;
    private boolean zoom = false;
    private double[] zoomedRect = new double[4];
    private int pcax = 0;
    private int pcay = 1;
    private int pcaz = 2;
    private boolean shadowUnselected = false;

    private int[] notshadIndex;

    public boolean[] zoomedSelectionChange(int[] sel) {
        ArrayList<Integer> reIndexSel = new ArrayList<Integer>();
        for (int x : sel) {
            if (indexToZoomed[x] != -100) {
                reIndexSel.add(indexToZoomed[x]);
            }
        }
        int[] zoomSel = new int[reIndexSel.size()];
        for (int x = 0; x < zoomSel.length; x++) {
            zoomSel[x] = reIndexSel.get(x);
        }
        boolean[] notShaded = getSelectedIndexes(zoomSel);
        return notShaded;

    }

    public void selectionChanged(int[] sel) {
        notshadIndex = sel;
        boolean[] notShaded = getSelectedIndexes(sel);
        plot.setNotShaded(notShaded);
        if (shadowUnselected == false) {
            plot.forceFullRepaint();
        }
    }

    public boolean isShadowUnselected() {
        return shadowUnselected;
    }

    public void setShadowUnselected(boolean shadowUnselected) {
        this.shadowUnselected = shadowUnselected;
    }
    private final String[] pcaLabelData;
    private final String totalvarStr;

    public PCAImageGenerator(PcaResults pcaResults, Dataset divaDataset, int pcax, int pcay) {
        this.dataset = divaDataset;
        this.pcaResults = pcaResults;
        this.pcax = pcax;
        this.pcay = pcay;
        this.plot = new PcaPlot();
        plot.setMaximumSize(new Dimension(32767, 32767));
        plot.setMinimumSize(new Dimension(900, 900));
        plot.setPreferredSize(new Dimension(900, 900));
        plot.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLUE));
        plot.setLayout(new java.awt.FlowLayout(0, 5, 1));
        plot.setSize(900, 900);
        plot.setBackground(Color.WHITE);
        indexToZoomed = new int[divaDataset.getDataLength()];//        
        updatePlot();
        pcaLabelData = new String[pcaResults.eigenvalues.length];
        for (int i = 0; i < pcaResults.eigenvalues.length; i++) {
            pcaLabelData[i] = ("Principal Component nr." + String.valueOf(i + 1) + " - " + pcaResults.varianceastr(i) + "% var.");
        }
        double totalvar = 0.0;
        java.text.NumberFormat numformat = java.text.NumberFormat.getNumberInstance(java.util.Locale.US);
        numformat.setMaximumFractionDigits(1);
        if (pcax != pcay) {
            totalvar = pcaResults.varianceaccounted(pcax) + pcaResults.varianceaccounted(pcay);
        } else {
            totalvar = pcaResults.varianceaccounted(pcax);
        }
        totalvarStr = ("Total variance retained: " + numformat.format(totalvar) + "% var.");

    }


    
    /**
     * If this is a som representation, light up the neuron at point p.
     *
     * @param p The neuron to be lit.
     */
    public void setSelected(java.awt.Point p) {
        plot.setHighLightedNeuron(p);

    }

    public void forceFullRepaint() {
        plot.setFullRepaint(true);
//        plot.repaint();
        plot.forceFullRepaint();
    }

     public void zoomOut(){       
            zoom = false;
            updatePlot();
        
    
    }

    public Vector getIndexesAtPoint(Point point, int radius) {
        return plot.getIndexesAtPoint(point, radius);
    }

    public boolean isZoompca() {
        return plot.isZoompca();
    }

    public boolean isPaintNamesonClick() {

        return plot.isPaintNamesonClick();
    }

    public boolean[] getFramedIndexes() {
        return plot.getFramedIndexes();
    }

    public double[] getZoomedArea() {
        return plot.getZoomedArea();
    }

    public void setSpotNames(String[][] SpotNames) {
        plot.setSpotNames(SpotNames);
    }

    public void setBorder(Border border) {
        plot.setBorder(border);

    }
    public void setLayout(LayoutManager loutManager) {
        plot.setLayout(loutManager);
    }

    public void setNotShaded(boolean[] notShaded) {
        plot.setNotShaded(notShaded);
    }

    /**
     * @param sel selection indexes
     * @return an array where the selected indexes are flagged as true. If there
     * are no indexes that are currently selected, or if the current Selection
     * from the SelectionManager is null, then an array of only false values are
     * returned.
     */
    private boolean[] getSelectedIndexes(int[] sel) {   
            if (sel == null) {
                return new boolean[dataset.getDataLength()];
            }
            boolean[] ret = ArrayUtils.toBooleanArray(dataset.getDataLength(), sel);
            return ret;
    }

    private double[][] points;
    private int[] zoomedToNormalIndex ;
    private final int[] indexToZoomed ;
    private String[] rowIds;

    public double[][] getPoints() {
        return points;
    }
    private void updatePlot() {
        if (pcaResults == null) {
            return;
        }

            points = new double[2][(int) pcaResults.nrPoints()];
        for (int i = 0; i < pcaResults.nrPoints(); i++) {
            points[0][i] = pcaResults.ElementAt(i, pcax);
            points[1][i] = pcaResults.ElementAt(i, pcay);

        }
        plot.setData(dataset);
        rowIds = dataset.getRowIds();
        if (zoom) {
            plot.setPropsAndData(points[0], points[1], zoomedRect);
        } else {
            plot.zoomout();
            plot.setForceEndLabel(true);
            plot.setPropsAndData(points[0], points[1]);
        }
        
        plot.setXaxisTitle("Principal Component " + (pcax + 1));
        plot.setYaxisTitle("Principal Component " + (pcay + 1));
        plot.setFullRepaint(true);        
        plot.forceFullRepaint();
        
    }
    private BufferedImage image;

    public BufferedImage getImage() {
        return image;
    }
    public int getImageHeight(){
        return image.getHeight();
    
    }
    public int getImageWidth(){
        return image.getWidth();
    
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
   public String toImage(){
        image = (BufferedImage)plot.getImage();   
        
        byte[] imageData = null;
        try{
        imageData = ChartUtilities.encodeAsPNG(image);
        }catch(Exception e){e.printStackTrace();}
        String base64 ="";//; Base64.encodeBase64String(imageData);
        base64 = "data:image/png;base64," + base64;
        
        return base64;

    }

    public void setZoomPca(boolean zoom) {
        plot.setZoompca(zoom);
    }
     public void setZoom(boolean zoom,int startX, int startY, int endX, int endY) {
        this.zoom = zoom;        
        this.zoomedRect = this.getSelectionRecatangle(startX, startY, endX, endY);
        this.updatePlot();
    }

    public void setPaintNamesonClick(boolean paint) {
        plot.setPaintNamesonClick(paint);
    }

    

    public int getPcax() {
        return pcax;
    }

    public void setPcax(int pcax) {
        this.pcax = pcax;
    }

    public int getPcay() {
        return pcay;
    }

    public void setPcay(int pcay) {
        this.pcay = pcay;
    }

    public int getPcaz() {
        return pcaz;
    }

    public void setPcaz(int pcaz) {
        this.pcaz = pcaz;
    }

    public PcaPlot getPlot() {
        return plot;
    }
    @SuppressWarnings("CallToPrintStackTrace")
     public String toPdfFile(File userFolder, String url) {
        try {
            BufferedImage pdfImage = image;
              DOMImplementation domImpl = new SVGDOMImplementation();
            String svgNS = "http://www.w3.org/2000/svg";
            SVGDocument svgDocument = (SVGDocument) domImpl.createDocument(svgNS, "svg", null);
            SVGGraphics2D svgGenerator = new SVGGraphics2D(svgDocument);
            svgGenerator.setSVGCanvasSize(new Dimension(pdfImage.getWidth(), pdfImage.getHeight()));
            svgGenerator.setPaint(Color.WHITE);
            svgGenerator.drawImage(pdfImage,0,0,null);
            File pdfFile = new File(userFolder,dataset.getName()+ "_PCA_PLOT" + ".pdf");
            if (!pdfFile.exists()) {
                pdfFile.createNewFile();
            } else {
                pdfFile.delete();
                pdfFile.createNewFile();
            }
            // write the svg file
            File svgFile = new File(pdfFile.getAbsolutePath() + ".temp");
            OutputStream outputStream = new FileOutputStream(svgFile);
            BufferedOutputStream bos = new BufferedOutputStream(outputStream);
            Writer out = new OutputStreamWriter(bos, "UTF-8");
            
            
            
            svgGenerator.stream(out, true /* use css */);
            outputStream.flush();
            outputStream.close();
            bos.close();
            System.gc();
            String svgURI = svgFile.toURI().toString();
            TranscoderInput svgInputFile = new TranscoderInput(svgURI);

            OutputStream outstream = new FileOutputStream(pdfFile);
            bos = new BufferedOutputStream(outstream);
            TranscoderOutput output = new TranscoderOutput(bos);

//             write as pdf
            Transcoder pdfTranscoder = new PDFTranscoder();
            pdfTranscoder.addTranscodingHint(PDFTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 0.084666f);
            pdfTranscoder.transcode(svgInputFile, output);
            outstream.flush();
            outstream.close();
            bos.close();
            System.gc();
            return url + userFolder.getName() + "/" + pdfFile.getName();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }
//    
//    @SuppressWarnings("CallToPrintStackTrace")
//    public UpdatedTooltip getTooltipsInformationData() {
//        tooltips = new UpdatedTooltip();
//        if (zoom) {
//            
//            try{
//            int plotWidthArea = (plot.getWidth() - plot.left - plot.right);
//            int plotHeightArea = plot.getHeight() - plot.top - plot.bottom;
//
//            double xDataArea = plot.getZoomedArea()[1] - plot.getZoomedArea()[0];
//            double xUnitPix = xDataArea / (double) plotWidthArea;
//
//            tooltips.setPlotWidth(plot.getWidth());
//            tooltips.setPlotHeight(plot.getHeight());
//            tooltips.setPlotHeightArea(plotHeightArea);
//            tooltips.setPlotWidthArea(plotWidthArea);
//            tooltips.setPlotRight(plot.right);
//            tooltips.setPlotTop(plot.top);
//            tooltips.setPlotBottom(plot.bottom);
//            tooltips.setPlotLeft(plot.left);
//            tooltips.setyAxisFactor(0);
//            tooltips.setxAxisFactor(0);
//            tooltips.setxUnitPix(xUnitPix);
//            double yDataArea = plot.getZoomedArea()[3] - plot.getZoomedArea()[2];
//            double yUnitPix = yDataArea / (double) plotHeightArea;
//            tooltips.setyUnitPix(yUnitPix);
//            tooltips.setMinX(plot.getZoomedArea()[0]);
//            tooltips.setMaxY(plot.getZoomedArea()[3]);
//            tooltips.setPoints(points);
//            tooltips.setRowIds(rowIds);
//            }catch(Exception exp){exp.printStackTrace();}
//
//        } else {//step 1
//            int yAxisFactor = plot.left - plot.yaxis.predictWidth();                        
//            int xAxisFactor = plot.bottom - plot.xaxis.predictWidth();
//            
//            int plotWidthArea = plot.Width() - (plot.left) - plot.right + yAxisFactor;
//            int plotHeightArea = plot.getHeight() - plot.top - plot.bottom + xAxisFactor;
//            tooltips.setPlotWidth(plot.getWidth());
//            tooltips.setPlotHeight(plot.getHeight());
//            tooltips.setPlotHeightArea(plotHeightArea);
//            tooltips.setPlotWidthArea(plotWidthArea);
//            tooltips.setPlotRight(plot.right);
//            tooltips.setPlotTop(plot.top);
//            tooltips.setPlotBottom(plot.bottom);
//            tooltips.setPlotLeft(plot.left);
//            tooltips.setyAxisFactor(yAxisFactor);
//            tooltips.setxAxisFactor(xAxisFactor);
//            double xDataArea = plot.xaxis.maximum -plot.xaxis.minimum;
//            double xUnitPix = xDataArea / (double) plotWidthArea;
//            tooltips.setxUnitPix(xUnitPix);
//            double yDataArea = plot.yaxis.maximum - plot.yaxis.minimum;
//            double yUnitPix = yDataArea / (double) plotHeightArea;
//            tooltips.setyUnitPix(yUnitPix);
//            tooltips.setMinX(plot.xaxis.minimum);
//            tooltips.setMaxY(plot.yaxis.maximum);
//            tooltips.setPoints(points);
//            tooltips.setRowIds(rowIds);
//        }
//        return tooltips;
//    }


    @SuppressWarnings("CallToPrintStackTrace")
     public int[] getPCASelection(int startX, int startY, int endX, int endY) {
        
         double[] selectRect = null;
         if (zoom) {
             try {
                 selectRect = getZoomedSelectionRecatangle(startX, startY, endX, endY);        

             } catch (Exception exp) {
                 exp.printStackTrace();
             }

         } else {
             selectRect = this.getSelectionRecatangle(startX, startY, endX, endY);
             
         }
         if(selectRect == null)
              return new int[]{}; 

         HashSet<Integer> selectedPoints = new HashSet<Integer>();
         for (int x = 0; x < points[0].length; x++) {
             double pointX = points[0][x];
             double pointY = points[1][x];
             if (pointX >= selectRect[0] && pointX <= selectRect[1] && pointY >= selectRect[2] && pointY <= selectRect[3]) {
                 selectedPoints.add(x);
             }

         }
         if (selectedPoints.size() > 0) {

             Integer[] selectedIndexes = new Integer[selectedPoints.size()];
             System.arraycopy(selectedPoints.toArray(), 0, selectedIndexes, 0, selectedIndexes.length);
             int[] arr = new int[selectedIndexes.length];
             arr = org.apache.commons.lang3.ArrayUtils.toPrimitive(selectedIndexes, selectedIndexes.length);
             return arr;
         }

         return new int[]{};
    }
     
     private double[] getZoomedSelectionRecatangle(int startX, int startY, int endX, int endY) {
        
        double[] selectionRect = new double[4];
        int maxXM = Math.max(startX, endX);
        int minXM = Math.min(startX, endX);
        int maxYM = Math.max(startY, endY);
        int minYM = Math.min(startY, endY);
//        
    
       
        
       int plotWidthArea = (plot.getWidth() - plot.left - plot.right) ;
       int plotHeightArea = plot.getHeight() - plot.top - plot.bottom ;
        

        if ((minXM < (plot.left) && maxXM < (plot.left)) || (minXM > (plot.left+plotWidthArea))) {
            return null;
        }
        if ((minYM < plot.top && maxXM < plot.left) || (minYM > plot.top + plotHeightArea)) {
            return null;
        }
        minXM = minXM - plot.left ;        
         maxXM= maxXM - plot.left ;
         minYM-=plot.top;
         maxYM-=plot.top;
        
        if((minXM<0 && maxXM >= 0))
             minXM =0;// plot.left;
        if(maxXM > plotWidthArea && minXM>= 0)
              maxXM = plotWidthArea;
        if((minYM<=0 && maxYM > 0))//plot.top))
             minYM = 0;//plot.top;
        if(maxYM >plotHeightArea&& minYM>= 0)
              maxXM = plotHeightArea;
        
        
          double xDataArea = plot.getZoomedArea()[1]  - plot.getZoomedArea()[0] ;
          

          
         double xUnitPix = xDataArea/(double) plotWidthArea ;
         double modStartX = (minXM * xUnitPix) + plot.getZoomedArea()[0] ;//xstart units from min    
         selectionRect[0]= modStartX;
         double modEndX = (maxXM * xUnitPix) + plot.getZoomedArea()[0] ;
         selectionRect[1]= modEndX;
         
         double yDataArea = plot.getZoomedArea()[3]  - plot.getZoomedArea()[2] ; 
         double yUnitPix =  yDataArea/(double) plotHeightArea ;
         double modStartY = plot.getZoomedArea()[3]  - (maxYM * yUnitPix);
         selectionRect[2]=modStartY;
         double modEndY = plot.getZoomedArea()[3]  - (minYM * yUnitPix);
         
         selectionRect[3]=modEndY;
         return selectionRect;
     
     }

    private double[] getSelectionRecatangle(int startX, int startY, int endX, int endY) {
        double[] selectionRect = new double[4];
        int maxXM = Math.max(startX, endX);
        int minXM = Math.min(startX, endX);
        int maxYM = Math.max(startY, endY);
        int minYM = Math.min(startY, endY);

        int yAxisFactor = plot.left - plot.yaxis.predictWidth();
        
        int xAxixFactor = plot.bottom - plot.xaxis.predictWidth();
                
        int plotWidthArea = (plot.getWidth() - plot.left - plot.right) + (yAxisFactor);
        int plotHeightArea = plot.getHeight() - plot.top - plot.bottom + xAxixFactor;
        

        if ((minXM < (plot.left - yAxisFactor) && maxXM < (plot.left - yAxisFactor)) || (minXM > (plot.left + plotWidthArea))) {
            return null;
        }
        if ((minYM < plot.top && maxXM < plot.left) || (minYM > plot.top + plotHeightArea)) {
            return null;
        }
        minXM = minXM - plot.left + yAxisFactor;
         maxXM= maxXM - plot.left + yAxisFactor;
         minYM-=plot.top;
         maxYM-=plot.top;
        
        if((minXM<0 && maxXM >= 0))
             minXM =0;
        if(maxXM > plotWidthArea && minXM>= 0)
              maxXM = plotWidthArea;
        if((minYM<=0 && maxYM > 0))
             minYM = 0;
        if(maxYM >plotHeightArea&& minYM>= 0)
              maxXM = plotHeightArea;
        
        
          double xDataArea = plot.xaxis.maximum - plot.xaxis.minimum;
         double xUnitPix = xDataArea/(double) plotWidthArea ;
         double modStartX = (minXM * xUnitPix) + plot.xaxis.minimum;//xstart units from min    
         selectionRect[0]= modStartX;
         double modEndX = (maxXM * xUnitPix) + plot.xaxis.minimum;
         selectionRect[1]= modEndX;
         
         double yDataArea = plot.yaxis.maximum - plot.yaxis.minimum;
         double yUnitPix =  yDataArea/(double) plotHeightArea ;
         double modStartY = plot.yaxis.maximum - (maxYM * yUnitPix);
         selectionRect[2]=modStartY;
         double modEndY = plot.yaxis.maximum - (minYM * yUnitPix);
         selectionRect[3]=modEndY;
         
         return selectionRect;
     
     }
     

    public void initZoomInteraction(int[] selection) {

        zoomedToNormalIndex = selection;
        for (int x = 0; x < indexToZoomed.length; x++) {
            indexToZoomed[x] = -100;
        }
        for (int i = 0; i < selection.length; i++) {
            indexToZoomed[selection[i]] = i;
        }
    }

    public boolean[] getZoomedNotshadIndex() {

        if (notshadIndex != null) {
            return zoomedSelectionChange(notshadIndex);//notshadIndex
        } else {
            return null;
        }

     
     }
     
     public int[] reindexZoomedSelectionIndexes(int[] zoomedSelection){
     int [] reindexSelection = new int[zoomedSelection.length];
     for(int z=0;z<zoomedSelection.length;z++){
         reindexSelection[z] = zoomedToNormalIndex[zoomedSelection[z]];
     }
     return reindexSelection;     
     }

    public String[] getPcaLabelData() {
        return pcaLabelData;
    }

    public String getTotalvarStr() {
        return totalvarStr;
    }
}
