/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.model;

/**
 *
 * @author yfa041
 */
public class PrideFileModel implements Comparable<PrideFileModel> {

    private String projectAccession;
    private double size;
    private String type;
    private String name;
    private String ftpLink;

    public String getProjectAccession() {
        return projectAccession;
    }

    public void setProjectAccession(String projectAccession) {
        this.projectAccession = projectAccession;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name.toLowerCase().replace(".mzml", "").replace(".mzid.gz", "").replace(".mzid", "");
    }

    public void setName(String name) {
        this.name = name;
        if (name.toLowerCase().contains("tmt")) {
            type = "not_supported_type";
        } else if (name.toLowerCase().contains(".mzml")) {
            type = "mzml";
        } else if (name.toLowerCase().endsWith(".mzid") || name.toLowerCase().endsWith(".mzid.gz")) {
            type = "mzid";
        } else {
            type = "not_supported_type";
        }
    }

    public String getFtpLink() {
        return ftpLink;
    }

    public void setFtpLink(String ftpLink) {
        this.ftpLink = "https://ftp.pride.ebi.ac.uk/pride/data/archive" + ftpLink.split("pride/data/archive")[1];
    }

    @Override
    public int compareTo(PrideFileModel o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return this.name; //To change body of generated methods, choose Tools | Templates.
    }

}
