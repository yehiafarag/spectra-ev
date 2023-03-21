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
public class TraningTreeResult {
    private String datasetName;
    private double dTreeAccurcy;
    private double rTreeAccurcy;
    private double rTreeR2;
    private TraningDataset traningDataset;
    private Dataset sourceDataset;

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public double getdTreeAccurcy() {
        return dTreeAccurcy;
    }

    public void setdTreeAccurcy(double dTreeAccurcy) {
        this.dTreeAccurcy = dTreeAccurcy;
    }

    public double getrTreeAccurcy() {
        return rTreeAccurcy;
    }

    public void setrTreeAccurcy(double rTreeAccurcy) {
        this.rTreeAccurcy = rTreeAccurcy;
    }

    public double getrTreeR2() {
        return rTreeR2;
    }

    public void setrTreeR2(double rTreeR2) {
        this.rTreeR2 = rTreeR2;
    }

    public TraningDataset getTraningDataset() {
        return traningDataset;
    }

    public void setTraningDataset(TraningDataset traningDataset) {
        this.traningDataset = traningDataset;
    }

    public Dataset getSourceDataset() {
        return sourceDataset;
    }

    public void setSourceDataset(Dataset sourceDataset) {
        this.sourceDataset = sourceDataset;
    }
}
