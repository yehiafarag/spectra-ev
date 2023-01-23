/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.processes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import no.probe.uib.mgfevaluator.machinelearning.Id3Implementation;
import no.probe.uib.mgfevaluator.model.GroupDescriptiveStatisticsModel;
import no.probe.uib.mgfevaluator.model.SpectraGroup;
import no.probe.uib.mgfevaluator.model.SpectrumModel;
import no.probe.uib.mgfevaluator.model.TraningDataset;
import no.probe.uib.mgfevaluator.processes.handlers.CMSFileHandler;
import no.probe.uib.mgfevaluator.processes.handlers.SearchEngineOutputFileHandler;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.core.dataset.Group;

/**
 *
 * @author yfa041
 */
public class DataProcessor {

    private final ExecutorService executorService;
    private CMSFileHandler traningCmsFileHandler;
    private SearchEngineOutputFileHandler traningSearchEngineOutputFileHandler;
    private Set<String> traningSearchEngines = null;
    private final DatasetUtilities datasetUtilities;
    private final StatisticalAnalysisUtilities statUtilities;

    public DataProcessor() {
        this.executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.datasetUtilities = new DatasetUtilities();
        this.statUtilities = new StatisticalAnalysisUtilities();
    }

    public void processTraningData(File selectedTraningSearchEngineOutputFile, File selectedTraningCmsFile) {

        Future cmsFileHandlerThread = executorService.submit(() -> {
            processCmsFile(selectedTraningCmsFile, true);
            System.out.println("process cms file");

        });
        Future searchEnginesInputFileHandlerThread = executorService.submit(() -> {
            processSearchEnginesInputFile(selectedTraningSearchEngineOutputFile, true);
        });
        while (!cmsFileHandlerThread.isDone() || !searchEnginesInputFileHandlerThread.isDone()) {
        }

        traningCmsFileHandler.getFullSpectruaMap().keySet().stream().map(spectraKey -> traningCmsFileHandler.getFullSpectruaMap().get(spectraKey)).forEachOrdered(spectrum -> {
            traningSearchEngineOutputFileHandler.updateSpectrumIdentificationInformation(spectrum);
        });
        datasetToTrain = initDataset(traningCmsFileHandler.getFullSpectruaMap());
        traningDataset = prepareDataToTrain(datasetToTrain);

    }

    private TraningDataset traningDataset;
    private Dataset datasetToTrain;

    private TraningDataset prepareDataToTrain( Dataset inputDatasetToTrain) {
        TraningDataset traningDatasetObject = new TraningDataset();
        traningDatasetObject.setColumnNames(inputDatasetToTrain.getColumnIds());
        traningDatasetObject.setRowNames(inputDatasetToTrain.getRowIds());
        Set<Integer> maybeOutlierToRemove = new HashSet<>();

        Map<String, LinkedHashSet<GroupDescriptiveStatisticsModel>> specGroupMap = new HashMap<>();
        specGroupMap.put("MOSTLY", new LinkedHashSet<>());
        specGroupMap.put("MAYBE", new LinkedHashSet<>());
        String[][] traningDataValues = new String[inputDatasetToTrain.getData().length + 1][inputDatasetToTrain.getData()[0].length + 1];
        int rowIndex = 0;
        int notbelong = 0;
        for (int colIndex = 0; colIndex < inputDatasetToTrain.getColumnIds().length; colIndex++) {
            traningDataValues[0][colIndex] = inputDatasetToTrain.getColumnIds()[colIndex];
        };
        traningDataValues[0][inputDatasetToTrain.getColumnIds().length] = "class";
        for (double[] row : inputDatasetToTrain.getData()) {
            String[] traningRowValues = new String[inputDatasetToTrain.getColumnIds().length + 1];
            for (int colIndex = 0; colIndex < inputDatasetToTrain.getColumnIds().length; colIndex++) {
                double dataValue = row[colIndex];
                specGroupMap.get("MOSTLY").clear();
                specGroupMap.get("MAYBE").clear();
                for (int groupIndex = 0; groupIndex < inputDatasetToTrain.getRowGroups().size(); groupIndex++) {
                    Group group = inputDatasetToTrain.getRowGroups().get(groupIndex);//(SpectraGroup) 
                    if (group instanceof SpectraGroup) {
                        SpectraGroup subGroup = (SpectraGroup) group;
                        GroupDescriptiveStatisticsModel gsm = subGroup.getGroupDescriptiveStatisticsModels().get(colIndex);
                        String eval = statUtilities.evaluateGroup(gsm, dataValue);
                        if (eval.startsWith("MOSTLY_")) {
                            specGroupMap.get("MOSTLY").add(gsm);
                        } else if (eval.startsWith("MAYBE_")) {
                            specGroupMap.get("MAYBE").add(gsm);
                        }
                    }
                }
                String final_value;
                if (!specGroupMap.get("MOSTLY").isEmpty() && specGroupMap.get("MOSTLY").size() == 1) {
                    final_value = "MOSTLY_" + specGroupMap.get("MOSTLY").iterator().next().getGroupName();
                } else if (!specGroupMap.get("MOSTLY").isEmpty() && specGroupMap.get("MOSTLY").size() > 1) {
                    final_value = statUtilities.selectMostlyResponse(specGroupMap.get("MOSTLY"), dataValue);
                } else if (!specGroupMap.get("MAYBE").isEmpty() && specGroupMap.get("MAYBE").size() == 1) {
                    final_value = "MAYBE_" + specGroupMap.get("MAYBE").iterator().next().getGroupName();
                } else if (!specGroupMap.get("MAYBE").isEmpty() && specGroupMap.get("MAYBE").size() > 1) {
                    final_value = statUtilities.selectMaybeResponse(specGroupMap.get("MAYBE"), dataValue);
                } else {
                    final_value = "NOTAVAILABLE";
                    notbelong++;
                    maybeOutlierToRemove.add(rowIndex);
                }
                traningRowValues[colIndex] = final_value;

            }

            for (Group g : inputDatasetToTrain.getRowGroups()) {
                if (g.hasMember(rowIndex)) {
                    traningRowValues[traningRowValues.length - 1] = g.getName();
                    break;
                }
            }

//            if (unIdentifiedIndices.contains(rowIndex)) {
//                traningRowValues[traningRowValues.length - 1] = "UnIdentified";
//            } else if (identifiedIndices_all_same.contains(rowIndex)) {
//                traningRowValues[traningRowValues.length - 1] = "Identified_ALL_Same";
//            } else if (identifiedIndices_all_diffrent.contains(rowIndex)) {
//                traningRowValues[traningRowValues.length - 1] = "Identified_All_Diffrent";
//            } else if (identifiedIndices_some_same.contains(rowIndex)) {
//                traningRowValues[traningRowValues.length - 1] = "Identified_Some_Same";
//            } else if (identifiedIndices_some_diffrent.contains(rowIndex)) {
//                traningRowValues[traningRowValues.length - 1] = "Identified_Some_diffrent";
//            } else {
//                System.out.println("Error in group related data " + rowIndex);
//            }
            traningDataValues[rowIndex + 1] = traningRowValues;
//            System.out.println("at total rowindex " + rowIndex + "  " + traningDataValues.length);
//            System.out.println("# of not belong " + notbelong + "  out of " + this.datasetToTrain.getDataLength() + "*" + this.datasetToTrain.getDataWidth() + "  rows " + maybeOutlierToRemove.size());
            rowIndex++;
        }
        traningDatasetObject.setData(traningDataValues);
        return traningDatasetObject;

    }

    private Dataset initDataset(Map<String, SpectrumModel> fullSpectruaMap) {
        double[][] measurmentValues = new double[fullSpectruaMap.size()][14];
        String[] rowNames = new String[fullSpectruaMap.size()];
        String[] colNames = new String[]{"total_peak_#", "total_intensity", "dynamic_range", "clear_peak_#_90", "top_peak_#_90", "top_peak_intensity_90", "SNR_90", "clear_peak_#_70", "top_peak_#_70", "top_peak_intensity_70", "SNR_70", "clear_peak_#_50", "top_peak_#_50", "top_peak_intensity_50", "SNR_50"};
//        boolean[] members = new boolean[rowNames.length];
        int i = 0;
        List<Integer> unIdentifiedIndices = new ArrayList<>();
        List<Integer> identifiedIndices_all_same = new ArrayList<>();
        List<Integer> identifiedIndices_all_diffrent = new ArrayList<>();
        List<Integer> identifiedIndices_some_same = new ArrayList<>();
        List<Integer> identifiedIndices_some_diffrent = new ArrayList<>();
        for (String spectrumKey : fullSpectruaMap.keySet()) {
            SpectrumModel spectrum = fullSpectruaMap.get(spectrumKey);
            rowNames[i] = spectrum.getSpectrumTitle();
            double[] measurmentRow = new double[]{spectrum.getNumPeaks(), spectrum.getTotalIntensity(), spectrum.getDynamicRange(), spectrum.getSpectrumPeaksLevels().get(0.9)[4], spectrum.getSpectrumPeaksLevels().get(0.9)[0], spectrum.getSpectrumPeaksLevels().get(0.9)[1], spectrum.getSpectrumPeaksLevels().get(0.9)[2], spectrum.getSpectrumPeaksLevels().get(0.7)[4], spectrum.getSpectrumPeaksLevels().get(0.7)[0], spectrum.getSpectrumPeaksLevels().get(0.7)[1], spectrum.getSpectrumPeaksLevels().get(0.7)[2], spectrum.getSpectrumPeaksLevels().get(0.5)[4], spectrum.getSpectrumPeaksLevels().get(0.5)[0], spectrum.getSpectrumPeaksLevels().get(0.5)[1], spectrum.getSpectrumPeaksLevels().get(0.5)[2]};
            measurmentValues[i] = measurmentRow;
            if (spectrum.getSearchEngineRank() == 0) {
                unIdentifiedIndices.add(i);
            } else if (spectrum.getSearchEngineRank() == 100 && spectrum.getSequenceRank() == 100) {
                identifiedIndices_all_same.add(i);
            } else if (spectrum.getSearchEngineRank() == 100 && spectrum.getSequenceRank() < 100) {
                identifiedIndices_all_diffrent.add(i);
            } else if (spectrum.getSearchEngineRank() < 100 && spectrum.getSequenceRank() == 100) {
                identifiedIndices_some_same.add(i);
            } else if (spectrum.getSearchEngineRank() < 100 && spectrum.getSequenceRank() < 100) {
                identifiedIndices_some_diffrent.add(i);
            }
//            members[i] = true;
            i++;
        }
        Dataset dataset = new Dataset(measurmentValues, rowNames, colNames);
        datasetUtilities.createRowGroup(dataset, "UnIdentified", "", "Unidentified spectra", datasetUtilities.listToArr(unIdentifiedIndices));
        datasetUtilities.createRowGroup(dataset, "Identified_ALL_Same", "", "Identified with all SE same sequence", datasetUtilities.listToArr(identifiedIndices_all_same));
        datasetUtilities.createRowGroup(dataset, "Identified_All_Diffrent", "", "Identified by all SE with diffrent sequence", datasetUtilities.listToArr(identifiedIndices_all_diffrent));
        datasetUtilities.createRowGroup(dataset, "Identified_Some_Same", "", "Identified by some SE with same sequence", datasetUtilities.listToArr(identifiedIndices_some_same));
        datasetUtilities.createRowGroup(dataset, "Identified_Some_diffrent", "", "Identified by some SE and with diffrent sequence", datasetUtilities.listToArr(identifiedIndices_some_diffrent));
//        dataset
        return dataset;
        ///create traning data 

    }

    public Dataset getDatasetToTrain() {
        return datasetToTrain;
    }

    private void processCmsFile(File cmsFile, boolean traningData) {
        if (traningData) {
            traningCmsFileHandler = new CMSFileHandler(cmsFile);
            traningCmsFileHandler.processCMS();
        } else {
            sampleCmsFileHandler = new CMSFileHandler(cmsFile);
            sampleCmsFileHandler.processCMS();
        }
    }

    private void processSearchEnginesInputFile(File selectedSearchEngineOutputFile, boolean traningData) {
        if (traningData) {
            traningSearchEngineOutputFileHandler = new SearchEngineOutputFileHandler(selectedSearchEngineOutputFile);
            traningSearchEngineOutputFileHandler.processSearchEnginesFile();
            traningSearchEngines = traningSearchEngineOutputFileHandler.getSearchEngines();
        } else {
            sampleSearchEngineOutputFileHandler = new SearchEngineOutputFileHandler(selectedSearchEngineOutputFile);
            sampleSearchEngineOutputFileHandler.processSearchEnginesFile();
            sampleSearchEngines = traningSearchEngineOutputFileHandler.getSearchEngines();

        }
    }

    public TraningDataset getTraningDataset() {
        return traningDataset;
    }

    public void processSampleData(File selectedSampleSearchEngineOutputFile, File selectedSampleCmsFile) {

        Future cmsFileHandlerThread = executorService.submit(() -> {
            processCmsFile(selectedSampleCmsFile, false);
            System.out.println("process sample cms file");

        });
        Future searchEnginesInputFileHandlerThread = executorService.submit(() -> {
            processSearchEnginesInputFile(selectedSampleSearchEngineOutputFile, false);
        });
        while (!cmsFileHandlerThread.isDone() || !searchEnginesInputFileHandlerThread.isDone()) {
        }

        sampleCmsFileHandler.getFullSpectruaMap().keySet().stream().map(spectraKey -> sampleCmsFileHandler.getFullSpectruaMap().get(spectraKey)).forEachOrdered(spectrum -> {
            sampleSearchEngineOutputFileHandler.updateSpectrumIdentificationInformation(spectrum);
        });
        datasetToSample = initDataset(sampleCmsFileHandler.getFullSpectruaMap());
        sampleDataset = prepareDataToTrain(datasetToSample);

    }

    private TraningDataset sampleDataset;
    private Dataset datasetToSample;
    private CMSFileHandler sampleCmsFileHandler;
    private SearchEngineOutputFileHandler sampleSearchEngineOutputFileHandler;
    private Set<String> sampleSearchEngines = null;

    public TraningDataset getSampleDataset() {
        return sampleDataset;
    }

    public Dataset getDatasetToSample() {
        return datasetToSample;
    }

}
