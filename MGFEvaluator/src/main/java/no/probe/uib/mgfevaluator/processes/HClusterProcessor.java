/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.processes;

import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.somclust.computation.SOMClustCompute;
import no.uib.jexpress_modularized.somclust.model.ClusterParameters;
import no.uib.jexpress_modularized.somclust.model.ClusterResults;
import web.diva.server.model.SomClustering.SomClustImgGenerator;

/**
 *
 * @author yfa041
 */
public class HClusterProcessor {

    private JPanel heatMapComponents;

    public HClusterProcessor() {
        heatMapComponents = new JPanel();
        heatMapComponents.setSize(1000, 1000);
    }

    public void computeSomClustering(Dataset dataset, int linkage, int distanceMeasure, boolean clusterColumns) throws IllegalArgumentException {
        heatMapComponents.removeAll();
        String linkageStr = "WPGMA";
        String distanceMeasureStr = "";
        ClusterParameters.LINKAGE link = null;
        if (linkage == 0) {
            linkageStr = "SINGLE";
            link = ClusterParameters.LINKAGE.SINGLE;
        } else if (linkage == 1) {
            linkageStr = "WPGMA";
            link = ClusterParameters.LINKAGE.WPGMA;
        } else if (linkage == 2) {
            linkageStr = "UPGMA";
            link = ClusterParameters.LINKAGE.UPGMA;
        } else if (linkage == 3) {
            linkageStr = "COMPLETE";
            link = ClusterParameters.LINKAGE.COMPLETE;
        }
        switch (distanceMeasure) {
            case 0:
                distanceMeasureStr = "Squared Euclidean";
                break;
            case 1:
                distanceMeasureStr = "Euclidean";
                break;
            case 2:
                distanceMeasureStr = "Bray Curtis";
                break;
            case 3:
                distanceMeasureStr = "Manhattan";
                break;
            case 4:
                distanceMeasureStr = "Cosine Correlation";
                break;
            case 5:
                distanceMeasureStr = "Pearson Correlation";
                break;

            case 6:
                distanceMeasureStr = "Uncentered Pearson Correlation";
                break;

            case 7:
                distanceMeasureStr = "Euclidean (Nullweighted)";
                break;
            case 8:
                distanceMeasureStr = "Camberra";
                break;
            case 9:
                distanceMeasureStr = "Chebychev";
                break;
            case 10:
                distanceMeasureStr = "Spearman Rank Correlation";
                break;

        }

        ClusterResults results;
        ClusterParameters parameter = new ClusterParameters();
        parameter.setDistance(distanceMeasure);
        parameter.setClusterSamples(clusterColumns);
        parameter.setLink(link);
      
        SOMClustCompute som = new SOMClustCompute(dataset, parameter);
        try{
        results = som.runClustering();
        }catch(Exception e){
            e.printStackTrace();
             results = som.runClustering();
        }
        som = null;

        String[] colNames = new String[dataset.getColumnIds().length];
        int[] colArrangement = new int[dataset.getColumnIds().length];
        if (clusterColumns) {
            somClustImgGenerator = new SomClustImgGenerator(results.getRowDendrogramRootNode(), results.getColumnDendrogramRootNode(), dataset.getDataLength());

            String upperTreeBase64 = somClustImgGenerator.generateTopTree(results.getColumnDendrogramRootNode());
            heatMapComponents.add(somClustImgGenerator.getUpperTree());

            for (int x = 0; x < somClustImgGenerator.getUpperTree().arrangement.length; x++) {
                colNames[x] = dataset.getColumnIds()[somClustImgGenerator.getUpperTree().arrangement[x]];
                colArrangement[x] = somClustImgGenerator.getUpperTree().arrangement[x];
            }
        } else {
            somClustImgGenerator = new SomClustImgGenerator(results.getRowDendrogramRootNode(), null, dataset.getDataLength());
            colNames = dataset.getColumnIds();
            for (int x = 0; x < dataset.getColumnIds().length; x++) {
                colArrangement[x] = x;
            }

        }
        clusterColumn = clusterColumns;

        BufferedImage sideTreeBI = somClustImgGenerator.generateSideTree(results.getRowDendrogramRootNode());
         heatMapComponents.add(somClustImgGenerator.getSideTree());

        final java.text.NumberFormat numformat;
        numformat = java.text.NumberFormat.getNumberInstance(java.util.Locale.US);
        numformat.setMaximumFractionDigits(3);
        numformat.setMinimumFractionDigits(1);
        double[][] values = new double[dataset.getDataLength()][dataset.getDataWidth()];
        for (int x = 0; x < dataset.getDataLength(); x++) {
            double[] row = dataset.getData()[somClustImgGenerator.getSideTree().arrangement[x]];
            double[] arrangedColRow = new double[row.length];
            for (int y = 0; y < row.length; y++) {
                arrangedColRow[y] = Double.valueOf(numformat.format(row[colArrangement[y]]));
            }
            values[x] = arrangedColRow;
        }

        String[] rowNames = new String[somClustImgGenerator.getSideTree().arrangement.length];

        for (int x = 0; x < somClustImgGenerator.getSideTree().arrangement.length; x++) {
            rowNames[x] = dataset.getRowIds()[somClustImgGenerator.getSideTree().arrangement[x]];
        }

        BufferedImage heatmapImg = somClustImgGenerator.generateHeatMap(dataset, clusterColumns);
         heatMapComponents.add(new JLabel(new ImageIcon(heatmapImg)));

//        SplitedImg heatmapUrl = somClustImgGenerator.splitImage(heatmapImg);
        String scaleUrl = somClustImgGenerator.generateScale(dataset, clusterColumns);

    }

    private boolean clusterColumn;
    private SomClustImgGenerator somClustImgGenerator;

    public JPanel getHeatMapComponents() {
        return heatMapComponents;
    }
}
