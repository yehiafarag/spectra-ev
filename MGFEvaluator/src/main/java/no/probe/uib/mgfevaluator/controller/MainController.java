package no.probe.uib.mgfevaluator.controller;

import com.compomics.util.gui.UtilitiesGUIDefaults;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import no.probe.uib.mgfevaluator.gui.MainGUI;
import no.probe.uib.mgfevaluator.machinelearning.Id3Implementation;
import no.probe.uib.mgfevaluator.model.TraningDataset;
import no.probe.uib.mgfevaluator.processes.DataProcessor;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.core.dataset.Group;

/**
 * Main controller class
 *
 * @author Yehia Mokhtar Farag
 */
public class MainController {
    
    private final MainGUI mainViewFrame;
    
    private final DataProcessor dataProcessor;
    
    public MainController() {
        
        this.dataProcessor = new DataProcessor();
        mainViewFrame = new MainGUI() {
            @Override
            public void processData(File selectedTraningOutputFile, File selectedTraningCmsFile, File selectedSampleOutputFile, File selectedSampleCmsFile) {
                invokeProcessAction(selectedTraningOutputFile, selectedTraningCmsFile, selectedSampleOutputFile, selectedSampleCmsFile);
            }
            
        };
    }
    
    public void excuteApp() {
        mainViewFrame.setVisible(true);
    }
    
    private void invokeProcessAction(File selectedTraningOutputFile, File selectedTraningCmsFile, File selectedSampleOutputFile, File selectedSampleCmsFile) {
        dataProcessor.processTraningData(selectedTraningOutputFile, selectedTraningCmsFile);
        Dataset traningDataset = dataProcessor.getDatasetToTrain();
        TraningDataset traningData = dataProcessor.getTraningDataset();

        //prepare saples to test 
        dataProcessor.processSampleData(selectedSampleOutputFile, selectedSampleCmsFile);
        Dataset datasetToSample = dataProcessor.getDatasetToSample();
        TraningDataset sampleData = dataProcessor.getSampleDataset();
        
        String[][] testData = new String[sampleData.getData().length][sampleData.getData()[0].length - 1];
        for (int i = 0; i < testData.length; i++) {
            for (int j = 0; j < testData[0].length; j++) {
                testData[i][j] = sampleData.getData()[i][j];
            }
            
        }
        Id3Implementation classifier = new Id3Implementation();
        System.out.println("step 3 is done ");
        classifier.train(traningData.getData());
        System.out.println("step 4 is done ");
        classifier.printTree();
        System.out.println("step 5 is done ");
        Map<String, String> resultsMap = classifier.classify(testData, datasetToSample.getRowIds());
        
        Map<String, String> errorDecision = new LinkedHashMap<>();
        Map<String, Integer> errorIdentifiedDecision = new LinkedHashMap<>();
        int i = 0;
        for (String key : resultsMap.keySet()) {
            if (!resultsMap.get(key).equalsIgnoreCase(sampleData.getData()[i + 1][sampleData.getData()[0].length - 1])) {
                String value = (resultsMap.get(key) + "--shouldbe--" + (sampleData.getData()[i + 1][sampleData.getData()[0].length - 1]));
                errorDecision.put(key, value);
                if (value.contains("UnIdentified") && value.contains("Identified")) {
                    if (!errorIdentifiedDecision.containsKey(value)) {
                        errorIdentifiedDecision.put(value, 0);
                    }
                    int z = errorIdentifiedDecision.get(value) + 1;
                    errorIdentifiedDecision.replace(value, z);
                }
            }
            
            i++;
            
        }
        double spec1 = (((double) datasetToSample.getDataLength() - (double) errorDecision.size()) / (double) datasetToSample.getDataLength()) * 100.0;
        System.out.println("total error number is " + errorDecision.size() + "   specifity is " + spec1);
        
        double spec2 = (((double) datasetToSample.getDataLength() - (double) errorIdentifiedDecision.size()) / (double) datasetToSample.getDataLength()) * 100.0;
        System.out.println("Id error number is " + errorIdentifiedDecision.size() + "   specifity is " + spec2);
        for (String key : errorIdentifiedDecision.keySet()) {
            System.out.println(key + " -- " + errorIdentifiedDecision.get(key));
        }

//        
//        testData[0] = datasetToTrain.getColumnIds();
//        for (int testIndex = 1; testIndex < 12; testIndex++) {
//            String[] row = new String[datasetToTrain.getColumnIds().length];
//            for (int testColIn = 0; testColIn < datasetToTrain.getColumnIds().length; testColIn++) {
//                row[testColIn] = traningDataValues[testIndex][testColIn];
//            }
//            testData[testIndex] = row;
//        }
//
//        
//        ProfilePlotImgeGenerator profilePlot = new ProfilePlotImgeGenerator(datasetToTrain, members);
//        profilePlot.setData(datasetToTrain);
////      profilePlot.updateColumnGroupColors();
//        profilePlot.setDraw(members);
//        profilePlot.setSize(1500, 1500);
        JPanel container = new JPanel();
        container.setLayout(null);
        int colIndex = 0;
        int x = 0;
        int y = 0;
        int counter = 0;
//        for (String col : dataset.getColumnIds()) {
//           BoxAndWhisker plot = new BoxAndWhisker(dataset, col, colIndex,false);
//           initAndViewJFarame(plot);
//           
//           
//           BoxAndWhisker plot2 = new BoxAndWhisker(dataset, col+"_Filtered", colIndex,true);
//           initAndViewJFarame(plot2);
//           colIndex++;
//           
//
//        }
        Group all = null;
        for (Group g : traningDataset.getRowGroups()) {
            System.out.println(g.getName());
            if (g.getName().equalsIgnoreCase("all")) {
                all = g;
            }
        }
        traningDataset.removeRowGroup(traningDataset, all);
//        PcaProcessor pcsProcessor = new PcaProcessor();
//        PcaResults rsults = pcsProcessor.getPCAResults(dataset, 1, 2);
//        PCAImageGenerator pca = new PCAImageGenerator(rsults, dataset, 1, 2);
//        pca.forceFullRepaint();
//        initAndViewJFarame(pca.getPlot());

//        HClusterProcessor hcProcessor = new HClusterProcessor();
//        hcProcessor.computeSomClustering(dataset, 1, 1, false);
//        initAndViewJFarame(hcProcessor.getHeatMapComponents());
    }
    
    private void initAndViewJFarame(JPanel panel) {
        JFrame frame = new JFrame();
        
        frame.setResizable(true);
        frame.setAlwaysOnTop(true);
        frame.setContentPane(panel);
        frame.setVisible(true);
        frame.setMaximumSize(new Dimension(500, 500));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
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
    }
    
}
