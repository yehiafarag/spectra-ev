/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.model;

/**
 *
 * @author yfa041
 */
public class GroupDescriptiveStatisticsModel {
    private double mean;
    private double median;
    private double lowerBond;
    private double upperBond;
    private double Q1;
    private double Q3;
    private double[] filteredMembers;
    private String groupName;

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getLowerBond() {
        return lowerBond;
    }

    public void setLowerBond(double lowerBond) {
        this.lowerBond = lowerBond;
    }

    public double getUpperBond() {
        return upperBond;
    }

    public void setUpperBond(double upperBond) {
        this.upperBond = upperBond;
    }

    public double getQ1() {
        return Q1;
    }

    public void setQ1(double Q1) {
        this.Q1 = Q1;
    }

    public double getQ3() {
        return Q3;
    }

    public void setQ3(double Q3) {
        this.Q3 = Q3;
    }

    public double[] getFilteredMembers() {
        return filteredMembers;
    }

    public void setFilteredMembers(double[] filteredMembers) {
        this.filteredMembers = filteredMembers;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    
}
