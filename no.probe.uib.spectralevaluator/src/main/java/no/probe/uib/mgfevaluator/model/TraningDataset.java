/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.model;

import no.uib.jexpress_modularized.core.dataset.Dataset;

/**
 *
 * @author yfa041
 */
public class TraningDataset {
    private String[] columnNames;
    private String[] rowNames;
    private Object[][] data;
    private String[] groupType;
    private String datasetName;
    private double[][] doubleData;
    private String selectedFeaturesKey;

    public String getSelectedFeaturesKey() {
        return selectedFeaturesKey;
    }

    public void setSelectedFeaturesKey(String selectedFeaturesKey) {
        this.selectedFeaturesKey = selectedFeaturesKey;
    }
    private Dataset sourceDataset;

    public Dataset getSourceDataset() {
        return sourceDataset;
    }

    public void setSourceDataset(Dataset sourceDataset) {
        this.sourceDataset = sourceDataset;
    }
   

    public double[][] getDoubleData() {
        return doubleData;
    }

    public void setDoubleData(double[][] doubleData) {
        this.doubleData = doubleData;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public String[] getRowNames() {
        return rowNames;
    }

    public void setRowNames(String[] rowNames) {
        this.rowNames = rowNames;
    }

    public Object[][] getData() {
        return data;
    }

    public void setData(Object[][] data) {
        this.data = data;
    }

    public String[] getGroupType() {
        return groupType;
    }

    public void setGroupType(String[] groupType) {
        this.groupType = groupType;
    }
}
