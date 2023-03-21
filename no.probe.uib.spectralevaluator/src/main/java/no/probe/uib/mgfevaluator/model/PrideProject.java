/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.model;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author yfa041
 */
public class PrideProject {

    private String accession;
    private String title;
    private String description;
    private String date;
    private String datasetFtpUrl;
    private Map<String, Set<PrideFileModel>> files;
    private String instrument;
    private String projectFileIdentification;

    public String getProjectFileIdentification() {
        return  accession + "__" + instrument + "__" + date ;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDatasetFtpUrl() {
        return datasetFtpUrl;
    }

    public void setDatasetFtpUrl(String datasetFtpUrl) {
        this.datasetFtpUrl = datasetFtpUrl;
    }

    public Map<String, Set<PrideFileModel>> getFiles() {
        return files;
    }

    public void setFiles(Map<String, Set<PrideFileModel>> files) {
        this.files = files;
    }
    private double filesSize;

    public double getFilesSize() {
        if (filesSize == 0.0) {
            getFiles().values().forEach(fs -> {
                fs.forEach(f -> {
                    filesSize += f.getSize();
                });
            });
        }
        return filesSize;

    }
    
}
