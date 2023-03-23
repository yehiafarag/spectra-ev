/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.model;

/**
 *
 * @author yfa041
 */
public class DatasetInfo {
   private int index;
   private String accession;
   private String technology;
   private int year;
   private int spect_total_num;
   private int ident_num; 
   private int un_ident_num;
   private String comparable_col;
   private double dt_acc;
   private double rt_acc;
   private double rt_r2;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getSpect_total_num() {
        return spect_total_num;
    }

    public void setSpect_total_num(int spect_total_num) {
        this.spect_total_num = spect_total_num;
    }

    public int getIdent_num() {
        return ident_num;
    }

    public void setIdent_num(int ident_num) {
        this.ident_num = ident_num;
    }

    public int getUn_ident_num() {
        return un_ident_num;
    }

    public void setUn_ident_num(int un_ident_num) {
        this.un_ident_num = un_ident_num;
    }

    public String getComparable_col() {
        return comparable_col;
    }

    public void setComparable_col(String comparable_col) {
        this.comparable_col = comparable_col;
    }

    public double getDt_acc() {
        return dt_acc;
    }

    public void setDt_acc(double dt_acc) {
        this.dt_acc = dt_acc;
    }

    public double getRt_acc() {
        return rt_acc;
    }

    public void setRt_acc(double rt_acc) {
        this.rt_acc = rt_acc;
    }

    public double getRt_r2() {
        return rt_r2;
    }

    public void setRt_r2(double rt_r2) {
        this.rt_r2 = rt_r2;
    }
    
}
