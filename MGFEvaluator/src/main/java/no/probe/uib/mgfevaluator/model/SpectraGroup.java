/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.model;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import no.uib.jexpress_modularized.core.dataset.Group;
import no.uib.jexpress_modularized.core.model.Selection;

/**
 *
 * @author yfa041
 */
public class SpectraGroup extends Group{

    private double[][] measurments;
    private final Map<Integer,GroupDescriptiveStatisticsModel>groupDescriptiveStatisticsModels= new LinkedHashMap<>();

    public double[][] getMeasurments() {
        return measurments;
    }

    public void setMeasurments(double[][] measurments) {
        this.measurments = measurments;
    }

    public double[][] getFilteredMeasurments() {
        return filteredMeasurments;
    }

    public void setFilteredMeasurments(double[][] filteredMeasurments) {
        this.filteredMeasurments = filteredMeasurments;
    }
    private double[][] filteredMeasurments;
    public SpectraGroup(String name, Color color, TYPE type, int[] indices) {
        super(name, color, type, indices);
    }

    public SpectraGroup(String name, Color color, Selection selection) {
        super(name, color, selection);
    }

    public Map<Integer, GroupDescriptiveStatisticsModel> getGroupDescriptiveStatisticsModels() {
        return groupDescriptiveStatisticsModels;
    }

    public void addGroupDescriptiveStatisticsModel(int i, GroupDescriptiveStatisticsModel groupDescriptiveStatisticsModel) {
        this.groupDescriptiveStatisticsModels.put(i,groupDescriptiveStatisticsModel);
    }
    
}
