package no.probe.uib.mgfevaluator.controller;

import com.compomics.util.gui.UtilitiesGUIDefaults;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import no.probe.uib.mgfevaluator.gui.DecisionTreeControllerGUI;
import no.probe.uib.mgfevaluator.gui.ExternalLoadDataGUI;
import no.probe.uib.mgfevaluator.gui.MainGUI;
import no.probe.uib.mgfevaluator.model.PrideProject;
import no.probe.uib.mgfevaluator.model.TraningDataset;
import no.probe.uib.mgfevaluator.processes.APIUtilities;
import no.probe.uib.mgfevaluator.processes.DataProcessor;
import no.probe.uib.mgfevaluator.processes.DatasetUtilities;
import no.probe.uib.mgfevaluator.processes.UpdatedDataProcessor;
import no.probe.uib.mgfevaluator.processes.handlers.DataStoreHandler;
import no.probe.uib.spectraev.ml.TreeComparisonsHandler;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import org.apache.commons.collections4.map.LinkedMap;

/**
 * Main controller class
 *
 * @author Yehia Mokhtar Farag
 */
public class MainController {
    
    private final MainGUI MAIN_VIEW_GUI;
    private final ExternalLoadDataGUI PRIDE_DATA_IMPORT_GUI;
    
    private final DataProcessor dataProcessor;
    private final UpdatedDataProcessor updatedDataProcessor;
    private final Map<Integer, Dataset> datasetMap;
    private int datasetIndex = 0;
    private final DataStoreHandler datastoreHandler;
    private final DatasetUtilities datasetUtil;
    private final TreeComparisonsHandler treeCompHandler;
    /**
     * Creates new form ExternalLoadDataGUI
     */
    private final APIUtilities apiUtilities;
    
    public MainController() {
        this.treeCompHandler = new TreeComparisonsHandler();
        this.apiUtilities = new APIUtilities();
        this.datastoreHandler = new DataStoreHandler();
        this.dataProcessor = new DataProcessor();
        this.updatedDataProcessor = new UpdatedDataProcessor();
        this.datasetUtil = new DatasetUtilities();
        this.datasetMap = new LinkedMap<>();
        MAIN_VIEW_GUI = new MainGUI() {
            @Override
            public void addDatasetPSOutput(File PSSearchExport, File cmsFile) {
                initDataset(PSSearchExport, cmsFile);
            }
            
            @Override
            public void processMzData(File selectedMzidFile, File selectedMzmlFile) {
                initDatasetFromMzFiles(selectedMzidFile, selectedMzmlFile);
            }
            
            @Override
            public boolean runPredection(Map<String, HashSet<String>> userselection) {
                return runMLPredection(userselection);
            }
            
            @Override
            public boolean viewPrideProjects() {
                return browesDataFromAPI();
            }
            
            @Override
            public boolean clustrTraningData() {
                return MainController.this.clustrTraningData();
            }
            
            @Override
            public void loadAPIDataView() {
                Thread t1 = new Thread(() -> {
                    this.setVisible(false);
                    PRIDE_DATA_IMPORT_GUI.setVisible(true);
                    PRIDE_DATA_IMPORT_GUI.setLocationRelativeTo(this);
                    PRIDE_DATA_IMPORT_GUI.showProgress(true);
                });
                Thread t2;
                t2 = new Thread(() -> {
                    Map<String, PrideProject> projects;
                    Set<PrideProject> filteredProjects = new LinkedHashSet<>();
                    while (filteredProjects.isEmpty()) {
                        if (PRIDE_DATA_IMPORT_GUI.isEmptyTable()) {
                            projects = apiUtilities.loadNextProjects();
                        } else {
                            projects = apiUtilities.getFinalProjectsResult();
                        }
                        
                        for (PrideProject project : projects.values()) {
                            if (!datastoreHandler.checkDataExisted(project.getProjectFileIdentification() + ".txt")) {
                                filteredProjects.add(project);
                            }
                        }
                    }
                    
                    PRIDE_DATA_IMPORT_GUI.loadData(filteredProjects);
                });
                t1.start();
                while (t1.isAlive()) {
                    
                }
                t2.start();
                
            }
            
        };
        loadStoredData();
        PRIDE_DATA_IMPORT_GUI = new ExternalLoadDataGUI() {
            @Override
            public void hideView() {
                MAIN_VIEW_GUI.setVisible(true);
            }
            
            @Override
            public boolean importSelectedTraningData(Set<String> selectedProjects) {
                boolean done = MainController.this.importProjects(selectedProjects);
                this.showProgress(false);
                return done;
            }
            
            @Override
            public void retriveProjects() {
                Map<String, PrideProject> projects = apiUtilities.loadNextProjects();
                Set<PrideProject> filteredProjects = new LinkedHashSet<>();
                projects.values().stream().filter(project -> (!datastoreHandler.checkDataExisted(project.getProjectFileIdentification() + ".txt"))).forEachOrdered(project -> {
                    filteredProjects.add(project);
                });
                
                PRIDE_DATA_IMPORT_GUI.loadData(filteredProjects);
            }
            
        };
        
    }
    
    private boolean clustrTraningData() {
        HashMap<String, TraningDataset> traningDatasets = new LinkedHashMap<>();
        
        datasetMap.values().stream().forEachOrdered(ds -> {
            //check data mesurment to apply then reduce data dimensions
            ds = updatedDataProcessor.reduceDatasetMeasurments(ds);
            TraningDataset traniningData = updatedDataProcessor.prepareDataToTrain(ds);
            traningDatasets.put(ds.getName(), traniningData);
        });
       return treeCompHandler.clusterTraningData(traningDatasets);
        
    }
    
    private boolean importProjects(Set<String> data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        Set<String> toImportData = new LinkedHashSet<>(data);
        data.stream().filter(str -> (datastoreHandler.checkDataExisted(str))).forEachOrdered(str -> {
            toImportData.remove(str);
        });
        if (toImportData.isEmpty()) {
            return true;
        }
        Map<String, Boolean> successDownloadFiles = apiUtilities.downloadSelectedProjects(toImportData);
        if (successDownloadFiles == null) {
            return false;
        }
        Map<String, HashSet<Dataset>> projectDatasets = new HashMap<>();
        successDownloadFiles.keySet().stream().filter(acc -> (successDownloadFiles.get(acc))).forEachOrdered(acc -> {
            String[] accArr = acc.split("__");
            String projectId = accArr[0] + "__" + accArr[1] + "__" + accArr[2];
            if (!datastoreHandler.checkDataExisted(projectId + ".txt")) {
                Dataset ds = initDatasetFromDownloadedMzFiles(new File(apiUtilities.download_folder_url, acc + ".mzid"), new File(apiUtilities.download_folder_url, acc + ".mzml"));
                if (!projectDatasets.containsKey(projectId)) {
                    projectDatasets.put(projectId, new HashSet<>());
                }
                projectDatasets.get(projectId).add(ds);
            }
            
        });
        //merge dataset into projects
        projectDatasets.keySet().stream().map(projectId -> {
            Dataset projectDataset = updatedDataProcessor.mergeDatasets(projectDatasets.get(projectId));
            projectDataset.setName(projectId);
            return projectDataset;
        }).map(projectDataset -> {
            datastoreHandler.storeDataset(projectDataset);
            return projectDataset;
        }).forEachOrdered(projectDataset -> {
            datasetMap.put(++datasetIndex, projectDataset);
        });
        MAIN_VIEW_GUI.updateDatasetTable(datasetMap);
        System.out.println("Done importing");
        
        return true;
    }
    
    private void loadStoredData() {
        
        Thread t = new Thread(() -> {
            MAIN_VIEW_GUI.progress(true);
            Set<Dataset> storedDatasets = datastoreHandler.loadStoredData();
            storedDatasets.forEach(ds -> {
                datasetMap.put(++datasetIndex, ds);
            });
            MAIN_VIEW_GUI.updateDatasetTable(datasetMap);
            MAIN_VIEW_GUI.progress(false);
//            treeCompHandler.compareAllData(datasetMap);
        });
        t.start();
        
    }
    
    public void excuteApp() {
        MAIN_VIEW_GUI.setVisible(true);
    }
    
    private void initDataset(File PSSearchExport, File cmsFile) {
        if (!datastoreHandler.checkDataExisted(PSSearchExport.getName())) {
            MAIN_VIEW_GUI.setErrorMessage("");
            Dataset dataset = dataProcessor.processDataset(PSSearchExport, cmsFile);
            datastoreHandler.storeDataset(dataset);
            datasetMap.put(++datasetIndex, dataset);
            MAIN_VIEW_GUI.updateDatasetTable(datasetMap);
        } else {
            MAIN_VIEW_GUI.setErrorMessage("Data existed, try another file or change the file name");
        }
        
    }
    
    private void initDatasetFromMzFiles(File selectedMzidFile, File selectedMzmlFile) {
        String datasetName = selectedMzidFile.getName().replace(".mzid", "");
        if (!datastoreHandler.checkDataExisted(datasetName + ".txt")) {
            MAIN_VIEW_GUI.setErrorMessage("");
            Dataset dataset = dataProcessor.processMzDataset(datasetName, selectedMzidFile, selectedMzmlFile);
            datastoreHandler.storeDataset(dataset);
            datasetMap.put(++datasetIndex, dataset);
            MAIN_VIEW_GUI.updateDatasetTable(datasetMap);
        } else {
            MAIN_VIEW_GUI.setErrorMessage("Data existed, try another file or change the file name");
        }
        
    }
    
    private Dataset initDatasetFromDownloadedMzFiles(File selectedMzidFile, File selectedMzmlFile) {
        String datasetName = selectedMzidFile.getName().replace(".mzid", "");
        Dataset dataset = updatedDataProcessor.processMzDataset(datasetName, selectedMzidFile, selectedMzmlFile);//           
        return dataset;
        
    }
    
    @SuppressWarnings("CallToPrintStackTrace")
    private boolean runMLPredection(Map<String, HashSet<String>> userselection) {
        
        TraningDataset sampleData = sampleData(userselection.get("S"));
        if (sampleData == null) {
            return false;
        }
        TraningDataset traniningData = trainData(userselection.get("T"), sampleData.getSourceDataset(), sampleData.getSelectedFeaturesKey());
        
        if (traniningData == null) {
            return false;
        }
        java.awt.EventQueue.invokeLater(() -> {
            DecisionTreeControllerGUI decisionTreeController = new DecisionTreeControllerGUI();
            decisionTreeController.runTreeAnalyzer(traniningData, sampleData);
        });
        
        return true;
    }
    
    private TraningDataset trainData(HashSet<String> datasetAccession, Dataset sample, String featureKey) {
        HashSet<Dataset> tDataset = new LinkedHashSet<>();
        datasetAccession.forEach(acc -> {
            datasetMap.values().stream().filter(ds -> (ds.getName().toUpperCase().contains(acc))).forEachOrdered(ds -> {
                //check data mesurment to apply then reduce data dimensions
                ds = updatedDataProcessor.reduceDatasetMeasurments(ds);
                tDataset.add(ds);
            });
            
        });
        Set<Dataset> finalDataset = dataProcessor.compareTraningProjects(tDataset, sample, featureKey);
        Dataset traningDS = updatedDataProcessor.mergeDatasets(finalDataset, true);
        traningDS = updatedDataProcessor.reduceDatasetMeasurments(traningDS);
        TraningDataset traniningData = updatedDataProcessor.prepareDataToTrain(traningDS);
        return traniningData;
        
    }
    
    private TraningDataset sampleData(HashSet<String> datasetAccession) {
        HashSet<Dataset> sDataset = new LinkedHashSet<>();
        datasetAccession.forEach(acc -> {
            datasetMap.values().stream().filter(ds -> (ds.getName().toUpperCase().contains(acc))).forEachOrdered(ds -> {
                //check data mesurment to apply then reduce data dimensions
                ds = updatedDataProcessor.reduceDatasetMeasurments(ds);
                sDataset.add(ds);
            });
            
        });
        Dataset traningDS = dataProcessor.mergeDatasets(sDataset, false);
        traningDS = updatedDataProcessor.reduceDatasetMeasurments(traningDS);
        
        TraningDataset samplingData = dataProcessor.trainDataset(traningDS);
        return samplingData;
        
    }
    
    private boolean browesDataFromAPI() {
        
        return true;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        var t = new Thread(() -> {
            try {
                
                UtilitiesGUIDefaults.setLookAndFeel();
                // fix for the scroll bar thumb disappearing...
                LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
                UIDefaults defaults = lookAndFeel.getDefaults();
                defaults.put("ScrollBar.minimumThumbSize", new Dimension(30, 30));
                MainController mainController = new MainController();
                mainController.excuteApp();
            } catch (IOException ex) {
                System.out.println(MainController.class.getName() + "  " + ex.getMessage());
            }
            
        });
        t.start();
    }
    
}
