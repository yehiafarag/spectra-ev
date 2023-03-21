/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.processes;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import no.probe.uib.mgfevaluator.facad.WebServicesFacad;
import no.probe.uib.mgfevaluator.model.PrideFileModel;
import no.probe.uib.mgfevaluator.model.PrideProject;

/**
 *
 * @author yfa041
 */
public class APIUtilities {

    private final WebServicesFacad webServicesFacad;
    private final Map<String, PrideProject> finalProjectsResult;
    private int pageNumber = 1;
    private int maxPageNumber = 50;
    public final String download_folder_url = "src\\main\\resources\\downloads";

    public APIUtilities() {
        this.finalProjectsResult = new LinkedHashMap<>();
        webServicesFacad = new WebServicesFacad();
    }

    private boolean downloadDatasetFiles(String fileIdentification, String mzmlUrl, String mzidUrl) {
        File f = new File(download_folder_url, fileIdentification + ".mzml");
        if (!f.exists()) {
            try {
                f.createNewFile();
             File downloadedFile =    webServicesFacad.FTPDownloadFile(mzmlUrl, f);
             if(downloadedFile==null){
              System.out.println("error downloading file "+f.getName());       
                f.delete();
                return false;
            }
            } catch (IOException ex) {
                System.out.println("error downloading file "+f.getName());
                ex.printStackTrace();
                f.delete();
                return false;
            }

        }
        f = new File(download_folder_url, fileIdentification + ".mzid");
        if (!f.exists()) {
            if (mzidUrl.endsWith(".gz")) {
                f = new File(download_folder_url, fileIdentification + ".mzid.gz");
            }
        }
        if (!f.exists()) {
            try {
                f.createNewFile();
                f = webServicesFacad.FTPDownloadFile(mzidUrl, f);

            } catch (IOException ex) {
                 System.out.println("error downloading file "+f.getName());
                ex.printStackTrace();
                f.delete();
                return false;
            }

        }
        if (mzidUrl.endsWith(".gz")) {          
            File mzmlDecomp = new File(download_folder_url, fileIdentification + ".mzid");
            if (!mzmlDecomp.exists()) {  
                System.out.println("unzip the file "+mzidUrl);
                decompressGzip(f, mzmlDecomp);
            }
        }

        //load dataset then delete the files
        return true;

    }

    public void reset() {
        finalProjectsResult.clear();
        pageNumber = 1;
    }

    public Map<String, Boolean> downloadSelectedProjects(Set<String> projectsIdSet) {
        Map<String, Boolean> downloadedFiles = new HashMap<>();
        for (String id : projectsIdSet) {
            PrideProject project = finalProjectsResult.get(id);
            if (project == null) {
                project = this.getPrideProject(id);
                if (project == null || project.getFiles()==null) {
                    return null;
                }

            }
            String mzml = "";
            String mzid = "";
            int index = 1;
            for (String fileid : project.getFiles().keySet()) {
                for (PrideFileModel file : project.getFiles().get(fileid)) {
                    if (file.getType().equalsIgnoreCase("mzid")) {
                        mzid = file.getFtpLink();
                    } else if (file.getType().equalsIgnoreCase("mzml")) {
                        mzml = file.getFtpLink();
                    }
                }
                if (!mzml.equals("") && !mzid.equals("")) {
                    downloadedFiles.put(project.getProjectFileIdentification()+"__"+ + index, downloadDatasetFiles((project.getProjectFileIdentification() +"__"+ index), mzml, mzid));                
                    index++;
                }
            }

        }
        return downloadedFiles;
    }

    public Map<String, PrideProject> getFinalProjectsResult() {
        return finalProjectsResult;
    }

    public Map<String, PrideProject> loadNextProjects() {
        int searchLimit = 30;
        int i = 1;
        if (pageNumber > maxPageNumber) {
            return finalProjectsResult;
        }
        Set<PrideProject> foundProjectsResult = searchPrideDatabase("keyword=*%3A*&filter=project_submission_type%3D%3Dcomplete%2Corganisms_facet%3D%3DHomo%20sapiens%20(human)%2Cinstruments_facet%3D%3DQ%20exactive%2Cproject_identified_ptms_facet%3D%3DCarbamidomethyl%2Cproject_identified_ptms_facet%3D%3DOxidation&pageSize=" + searchLimit + "&page=" + pageNumber + "&sortDirection=DESC&sortFields=publication_date");

        for (PrideProject project : foundProjectsResult) {
            Map<String, Set<PrideFileModel>> files = searchPrideFiles(project.getAccession());
            if (files != null) {
                project.setFiles(files);
                finalProjectsResult.put(project.getAccession(), project);
            }

        }
        pageNumber++;
        return finalProjectsResult;
    }

    public void decompressGzip(File source, File target) {
        System.out.println("at file target "+target+"  file sorce "+source);
        try (GZIPInputStream gis = new GZIPInputStream(
                new FileInputStream(source));
                FileOutputStream fos = new FileOutputStream(target)) {

            // copy GZIPInputStream to FileOutputStream
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            gis.close();
            fos.close();
            source.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to " + newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PrideProject getPrideProject(String projectAccession) {

        try {
            String url = "https://www.ebi.ac.uk/pride/ws/archive/v2/projects/" + projectAccession;
            PrideProject foundProjectsResult = new PrideProject();
            JsonObject response = new JsonObject(webServicesFacad.doGetCurl(url));

            PrideProject projectModel = new PrideProject();
            projectModel.setTitle(response.getString("title"));
            projectModel.setAccession(response.getString("accession"));
            projectModel.setDate(response.getString("publicationDate"));
            projectModel.setDescription(response.getString("projectDescription"));
            projectModel.setInstrument(response.getJsonArray("instruments").getJsonObject(0).getString("name"));
            projectModel.setDatasetFtpUrl(response.getJsonObject("_links").getJsonObject("datasetFtpUrl").getString("href"));
            Map<String, Set<PrideFileModel>> files = searchPrideFiles(projectModel.getAccession());
            projectModel.setFiles(files);
            return foundProjectsResult;
//
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public Set<PrideProject> searchPrideDatabase(String filters) {

        Set<PrideProject> foundProjectsResult = new LinkedHashSet<>();
        try {
            String url = "https://www.ebi.ac.uk/pride/ws/archive/v2/search/projects?" + filters;

            JsonObject response = new JsonObject(webServicesFacad.doGetCurl(url));
            maxPageNumber = response.getJsonObject("page").getInteger("totalPages");
            JsonArray compactProjects = response.getJsonObject("_embedded").getJsonArray("compactprojects");
            for (int i = 0; i < compactProjects.size(); i++) {
                JsonObject project = compactProjects.getJsonObject(i);
                PrideProject projectModel = new PrideProject();
                projectModel.setTitle(project.getString("title"));
                projectModel.setAccession(project.getString("accession"));
                projectModel.setDate(project.getString("publicationDate"));
                projectModel.setDescription(project.getString("projectDescription"));
                projectModel.setInstrument(project.getJsonArray("instruments").getString(0));
                projectModel.setDatasetFtpUrl(project.getJsonObject("_links").getJsonObject("datasetFtpUrl").getString("href"));
                foundProjectsResult.add(projectModel);
            }
//
        } catch (Exception e) {
            e.printStackTrace();
        }

        return foundProjectsResult;
    }

    public Map<String, Set<PrideFileModel>> searchPrideFiles(String projectAccession) {

        Map<String, Set<PrideFileModel>> foundFilesResult = new LinkedHashMap<>();
        boolean mzid = false;
        boolean mzml = false;
        try {
            String url = "https://www.ebi.ac.uk/pride/ws/archive/v2/projects/" + projectAccession + "/files?filter=fileName%3Dregex%3D.mz&pageSize=300&sortDirection=DESC&sortConditions=fileName";
            JsonArray response = new JsonObject(webServicesFacad.doGetCurl(url)).getJsonObject("_embedded").getJsonArray("files");
            for (int i = 0; i < response.size(); i++) {
                JsonObject file = response.getJsonObject(i);
                PrideFileModel fileModel = new PrideFileModel();
                fileModel.setProjectAccession(projectAccession);
                fileModel.setSize((double) file.getLong("fileSizeBytes") / 1000000000.0);
                fileModel.setName(file.getString("fileName"));
                fileModel.setFtpLink(file.getJsonArray("publicFileLocations").getJsonObject(0).getString("value"));
                if (fileModel.getType().equals("mzml") || fileModel.getType().equals("mzid")) {
                    if (!foundFilesResult.containsKey(fileModel.getName())) {
                        foundFilesResult.put(fileModel.getName(), new TreeSet<>());
                    }
                    foundFilesResult.get(fileModel.getName()).add(fileModel);
                }
            }
            Map<String, Set<PrideFileModel>> filteredFilesResult = new LinkedHashMap<>();
            foundFilesResult.keySet().stream().filter(id -> (foundFilesResult.get(id).size() == 2)).forEachOrdered(id -> {
                filteredFilesResult.put(id, foundFilesResult.get(id));
            });
            if (!filteredFilesResult.isEmpty()) {
                return filteredFilesResult;
            }

//
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
