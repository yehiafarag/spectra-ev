/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.model;

/**
 *
 * @author yfa041
 */
public class ProjectCombinationMatrix {
    private String[]columnIds;
    private String[]combiniations;
    private double[][] data;

    public String[] getColumnIds() {
        return columnIds;
    }

    public void setColumnIds(String[] columnIds) {
        this.columnIds = columnIds;
    }

    public String[] getCombiniations() {
        return combiniations;
    }

    public void setCombiniations(String[] combiniations) {
        this.combiniations = combiniations;
    }

    public double[][] getData() {
        return data;
    }

    public void setData(double[][] data) {
        this.data = data;
    }
}
