/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.processes;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import no.probe.uib.mgfevaluator.model.GroupDescriptiveStatisticsModel;
import no.probe.uib.mgfevaluator.model.SpectraGroup;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.core.dataset.Group;
import no.uib.jexpress_modularized.core.model.Selection;

/**
 *
 * @author yfa041
 */
public class DatasetUtilities {

    private final Random rand = new Random();
    private final StatisticalAnalysisUtilities statUtil = new StatisticalAnalysisUtilities();

    /**
     * This method is used to create row groups
     *
     * @param dataset input dataset
     * @param name - row group name
     * @param color - row group hashed color
     * @param description - group description (row)
     * @param selection - omics data indexes
     * @param doStatAnalysis
     */
    public void createRowGroup(Dataset dataset, String name, String color, String description, int[] selection, boolean doStatAnalysis) {
        Color c = null;
        color = color.trim();
        if (color == null || color.equals("")) {
            c = generatRandColor();
            color = toHexString(c);
        } else {
            c = hex2Rgb(color);
            color = toHexString(c);
        }
        Selection.TYPE s = Selection.TYPE.OF_ROWS;
        SpectraGroup jG = new SpectraGroup(name, c, new Selection(s, selection));
        jG.setActive(true);
        jG.setHashColor(color);
        jG.setDescription(description);
        Map<Integer, String> geneIndexNameMap = initIndexNameGeneMap(dataset.getRowIds());
        jG.setGeneList(initGroupGeneList(geneIndexNameMap, jG.getMembers()));
        Collections.reverse(dataset.getRowGroups());
        dataset.addRowGroup(jG);
        Collections.reverse(dataset.getRowGroups());
        jG.setActive(true);
        if (doStatAnalysis) {
            double[][] groupMeasurments = new double[selection.length][dataset.getDataWidth()];
            double[][] groupFilteredMeasurments = new double[selection.length][dataset.getDataWidth()];
            int x = 0;
            for (int i : selection) {
                double[] measurment = dataset.getData()[i];
                groupMeasurments[x++] = measurment;
            }
            jG.setMeasurments(groupMeasurments);
            List<double[]> columnsList = new ArrayList<>();
            for (int i = 0; i < groupMeasurments[0].length; i++) {
                double[] column = new double[groupMeasurments.length]; // Here I assume a rectangular 2D array! 
                for (int j = 0; j < column.length; j++) {
                    column[j] = groupMeasurments[j][i];
                }
                GroupDescriptiveStatisticsModel groupDescriptiveStatisticsModel = statUtil.calculateGroupDescriptiveStatistics(column);
                groupDescriptiveStatisticsModel.setGroupName(jG.getName());
                columnsList.add(groupDescriptiveStatisticsModel.getFilteredMembers());
                jG.addGroupDescriptiveStatisticsModel(i, groupDescriptiveStatisticsModel);

            }
            for (int i = 0; i < groupFilteredMeasurments.length; i++) {
                double[] row = new double[dataset.getDataWidth()];
                for (int j = 0; j < columnsList.size(); j++) {
                    row[j] = columnsList.get(j)[i];
                }
                groupFilteredMeasurments[i] = row;
            }
            jG.setFilteredMeasurments(groupFilteredMeasurments);
        }
    }

    /**
     * This method is used to create row groups
     *
     * @param dataset input dataset
     * @param name - row group name
     * @param color - row group hashed color
     * @param description - group description (row)
     * @param selection - omics data indexes
     * @return tempDivaDataset
     */
    public void createColumnGroup(Dataset dataset, String name, String color, String description, int[] selection) {
        Color c = null;
        if (color == null || color.equals("")) {
            c = generatRandColor();
            color = toHexString(c);
        } else {
            c = hex2Rgb(color);
            color = toHexString(c);
        }
        Selection.TYPE s = Selection.TYPE.OF_COLUMNS;
        Group jG = new Group(name, c, new Selection(s, selection));
        jG.setActive(true);
        jG.setHashColor(color);
        jG.setDescription(description);
        dataset.addColumnGroup(jG);

    }

    /**
     * This method is used to generate random colors
     *
     * @return group color
     */
    private Color generatRandColor() {

        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();
        Color randomColor = new Color(r, g, b);
        return randomColor;
    }

    /**
     * This method is used to convert hashed colors into awt colors
     *
     * @param colorStr -hashed color
     * @return group color
     */
    private Color hex2Rgb(String colorStr) {
        Color color = null;
        if (!colorStr.contains("#") || colorStr.length() < 7) {
            try {
                Field field = Class.forName("java.awt.Color").getField(colorStr);
                color = (Color) field.get(null);
            } catch (Exception e) {
                e.printStackTrace();
                color = generatRandColor(); // Not defined
            }
        } else {
            color = new Color(
                    Integer.valueOf(colorStr.substring(1, 3), 16),
                    Integer.valueOf(colorStr.substring(3, 5), 16),
                    Integer.valueOf(colorStr.substring(5, 7), 16));
        }
//        System.out.println("final color is " + color.toString());
        return color;
    }

    private String toHexString(Color colour) throws NullPointerException {
        String hexColour = Integer.toHexString(colour.getRGB() & 0xffffff);
        if (hexColour.length() < 6) {
            hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
        }
        return "#" + hexColour;
    }

    public List<String> initGroupGeneList(Map<Integer, String> geneIndexNameMap, int[] members) {
        List<String> geneList = new ArrayList<String>();
        for (int x : members) {
            geneList.add(geneIndexNameMap.get(x));
        }

        return geneList;

    }

    public Map<Integer, String> initIndexNameGeneMap(String[] rowIds) {
        Map<Integer, String> geneMap = new HashMap<>();

        for (int index = 0; index < rowIds.length; index++) {
            geneMap.put(index, rowIds[index]);
        }
        return geneMap;
    }

    public int[] listToArr(List<Integer> inputList) {
        int[] arr = new int[inputList.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = inputList.get(i);
        }
        return arr;
    }

    public Set<Integer> handelDataOutlier(double[][] measurmentValues) {
        Set<Integer> outlierIndexes = new HashSet<>();
        for (int i = 0; i < measurmentValues[0].length; i++) {
            double[] column = new double[measurmentValues.length];
            for (int j = 0; j < column.length; j++) {
                column[j] = measurmentValues[j][i];
            }
            GroupDescriptiveStatisticsModel columnStatisticsModel = statUtil.calculateGroupDescriptiveStatistics(column);
            double pers = (double) columnStatisticsModel.getOutlierIndexes().size() / (double) measurmentValues.length;
            System.out.println("at column " + i + "  outlier % " + pers + "%");
            outlierIndexes.addAll(columnStatisticsModel.getOutlierIndexes());
        }
        return outlierIndexes;

    }

}
