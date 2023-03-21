package no.probe.uib.mgfevaluator.processes;

import com.compomics.util.experiment.io.mass_spectrometry.MsFileIterator;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingDialog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import no.probe.uib.mgfevaluator.model.GroupDescriptiveStatisticsModel;
import no.probe.uib.mgfevaluator.model.SpectraGroup;
import no.probe.uib.mgfevaluator.model.SpectrumModel;
import no.probe.uib.mgfevaluator.model.TraningDataset;
import no.probe.uib.mgfevaluator.processes.handlers.CMSFileHandler;
import no.probe.uib.mgfevaluator.processes.handlers.CustomizedmzIdentMLIdFileReader;
import no.probe.uib.mgfevaluator.processes.handlers.MzIdFileHandler;
import no.probe.uib.mgfevaluator.processes.handlers.UpdatedCmsFileWriter;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.core.dataset.Group;
import no.uib.jexpress_modularized.pca.computation.PcaCompute;
import no.uib.jexpress_modularized.pca.computation.PcaResults;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import smile.feature.extraction.PCA;
import smile.plot.swing.PlotPanel;

/**
 *
 * @author Yehia Mokhtar Farag
 */
public class UpdatedDataProcessor {

    private final ExecutorService executorService;
    private CMSFileHandler cmsFileHandler;
//    private PSExportFileHandler psExportFileHandler;
    private MzIdFileHandler mzidHandler;
    private final DatasetUtilities datasetUtilities;
    private final StatisticalAnalysisUtilities statUtilities;
//    private final String[] groupColors = new String[]{"#FF3933", "#48CD19", "#1970CD", "#CD8919"};
//    private int colorIndex = 0;
//    private ClusterParameters parameter;

    public UpdatedDataProcessor() {
        this.executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.datasetUtilities = new DatasetUtilities();
        this.statUtilities = new StatisticalAnalysisUtilities();
//        parameter = new ClusterParameters();
//        parameter.setDistance(1);
//        parameter.setClusterSamples(true);
//        parameter.setLink(ClusterParameters.LINKAGE.UPGMA);
    }

    public Dataset processMzDataset(String datasetName, File selectedMzidFile, File selectedMzmlFile) {
        File cmsFile = new File(datasetName + ".cms");
        if (cmsFile.exists()) {
            cmsFile.delete();
        }
        Future mzMlFileHandlerThread = executorService.submit(() -> {
            try {
                writeCmsFile(selectedMzmlFile, cmsFile);
                cmsFileHandler = processCmsFile(datasetName, cmsFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        });
        Future MzmlIdFileHandlerThread = executorService.submit(() -> {
            CustomizedmzIdentMLIdFileReader mzIdReader;
            try {
                mzIdReader = new CustomizedmzIdentMLIdFileReader(selectedMzidFile);
                mzidHandler = new MzIdFileHandler(mzIdReader);
                mzidHandler.processMzIdFile(datasetName, selectedMzmlFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        });
        while (!mzMlFileHandlerThread.isDone() || !MzmlIdFileHandlerThread.isDone()) {
        }
        try {
            cmsFileHandler.getFullSpectruaMap().keySet().stream().map(spectraKey -> cmsFileHandler.getFullSpectruaMap().get(spectraKey)).forEachOrdered(spectrum -> {
                mzidHandler.updateSpectrumIdentificationInformation(spectrum);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return initDataset(selectedMzidFile.getName().replace(".mzid", ""), cmsFileHandler.getFullSpectruaMap(), false);

    }

    public Dataset mergeDatasets(Set<Dataset> datasets) {
        if (datasets == null || datasets.isEmpty()) {
            return null;
        }


        if (datasets.size() == 1) {
            return datasets.iterator().next();
        } else {
            String[] colnames = null;
            int dataLength = 0;
            Map<String, List<Integer>> groupMembers = new HashMap<>();
            for (Dataset ds : datasets) {
                if (colnames == null) {
                    colnames = ds.getColumnIds().clone();
                }
                dataLength += ds.getDataLength();
                ds.getRowGroups().stream().filter(g -> (!groupMembers.containsKey(g.getName()))).forEachOrdered(g -> {
                    if (!g.getName().equalsIgnoreCase("all")) {
                        groupMembers.put(g.getName(), new ArrayList<>());
                    }
                });

            }
            String[] rowIds = new String[dataLength];
            double[][] data = new double[dataLength][];
            int i = 0;
            for (Dataset ds : datasets) {
                for (int k = 0; k < ds.getDataLength(); k++) {
                    rowIds[i] = ds.getRowIds()[k];
                    data[i] = ds.getData()[k];
                    for (Group g : ds.getRowGroups()) {
                        if (g.getName().equalsIgnoreCase("all")) {
                            continue;
                        }
                        if (g.hasMember(k)) {
                            groupMembers.get(g.getName()).add(i);
                        }

                    }
                    i++;
                }
            }
            Dataset mergedDs = new Dataset(data, rowIds, colnames);
            groupMembers.keySet().forEach(group -> {
                datasetUtilities.createRowGroup(mergedDs, group, "", group, datasetUtilities.listToArr(groupMembers.get(group)), false);
            });

            return mergedDs;
        }
    }

    private Dataset initDataset(String datasetName, Map<String, SpectrumModel> fullSpectruaMap, boolean doStatAnalysis) {
        double[][] measurmentValues = new double[fullSpectruaMap.size()][15];
        boolean[][] missingMesurments = new boolean[fullSpectruaMap.size()][15];
        String[] rowNames = new String[fullSpectruaMap.size()];
        String[] colNames = new String[]{"population_variance", "general_SNR", "intPeakRatio", "total_peak_#", "total_intensity", "dynamic_range", "clear_peak_#_90", "top_peak_intensity_90", "SNR_90", "clear_peak_#_70", "top_peak_intensity_70", "SNR_70", "clear_peak_#_50", "top_peak_intensity_50", "SNR_50"};
        boolean[] activeRow = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
        int i = 0;
        List<Integer> unIdentifiedIndices = new ArrayList<>();
        List<Integer> identifiedIndices = new ArrayList<>();
        for (String spectrumKey : fullSpectruaMap.keySet()) {
            SpectrumModel spectrum = fullSpectruaMap.get(spectrumKey);
            rowNames[i] = spectrum.getSpectrumTitle();
            double[] measurmentRow = new double[]{spectrum.getPopulationVariance(), spectrum.getGeneralSNR(), spectrum.getPeaksIntensityRatio(), spectrum.getNumPeaks(), spectrum.getTotalIntensity(), spectrum.getDynamicRange(), spectrum.getSpectrumPeaksLevels().get(0.9)[4], spectrum.getSpectrumPeaksLevels().get(0.9)[1], spectrum.getSpectrumPeaksLevels().get(0.9)[2], spectrum.getSpectrumPeaksLevels().get(0.7)[4], spectrum.getSpectrumPeaksLevels().get(0.7)[1], spectrum.getSpectrumPeaksLevels().get(0.7)[2], spectrum.getSpectrumPeaksLevels().get(0.5)[4], spectrum.getSpectrumPeaksLevels().get(0.5)[1], spectrum.getSpectrumPeaksLevels().get(0.5)[2]};
            measurmentValues[i] = measurmentRow;
            missingMesurments[i] = activeRow.clone();
            if (spectrum.getSearchEngineRank() == 0) {
                unIdentifiedIndices.add(i);
            } else {
                identifiedIndices.add(i);
            }
            i++;
        }
        Dataset dataset = new Dataset(measurmentValues, rowNames, colNames);
        dataset.setMissingMeasurements(missingMesurments);
        if (!unIdentifiedIndices.isEmpty()) {
            datasetUtilities.createRowGroup(dataset, "UnIdentified", "", "Unidentified spectra", datasetUtilities.listToArr(unIdentifiedIndices), doStatAnalysis);
        }
        if (!identifiedIndices.isEmpty()) {
            datasetUtilities.createRowGroup(dataset, "Identified", "", "Identified spectra", datasetUtilities.listToArr(identifiedIndices), doStatAnalysis);
        }
        dataset.setName(datasetName);
        return dataset;
    }

    private CMSFileHandler processCmsFile(String datasetName, File cmsFile) {
        var tempcmsFileHandler = new CMSFileHandler(datasetName, cmsFile);
        tempcmsFileHandler.processCMS();
        return tempcmsFileHandler;
    }

    /**
     * Writes a cms file for the given mass spectrometry file.
     *
     * @param msFile The mass spectrometry file.
     * @param cmsFile The cms file.
     * @param waitingHandler The waiting handler.
     *
     * @throws IOException Exception thrown if an error occurred while reading
     * or writing a file.
     */
    private void writeCmsFile(File msFile, File cmsFile) throws IOException {
        WaitingDialog w = new WaitingDialog() {
            @Override
            public void setSecondaryProgressCounter(int value) {
            }

            @Override
            public void setSecondaryProgressCounterIndeterminate(boolean indeterminate) {
            }

            @Override
            public void setMaxSecondaryProgressCounter(int maxProgressValue) {
            }
        };
        try (MsFileIterator iterator = MsFileIterator.getMsFileIterator(msFile, w)) {
            try (UpdatedCmsFileWriter writer = new UpdatedCmsFileWriter(cmsFile)) {
                String spectrumTitle;
                while ((spectrumTitle = iterator.next()) != null) {
                    Spectrum spectrum = iterator.getSpectrum();
                    writer.addSpectrum(spectrumTitle, spectrum);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Dataset reduceDatasetMeasurments(Dataset updatedDs) {

        //remove outliers of the data       
        PearsonsCorrelation corr = new PearsonsCorrelation();
        List<double[]> columnsData = new ArrayList<>();
        for (int i = 0; i < updatedDs.getDataWidth(); i++) {
            double[] column = new double[updatedDs.getDataLength()];
            for (int j = 0; j < updatedDs.getDataLength(); j++) {
                column[j] = updatedDs.getData()[j][i];
            }
            columnsData.add(column);
        }
        TreeMap<Double, TreeSet<Integer>> treemap = new TreeMap<>();
        int counter = 0;
        for (double[] col : columnsData) {
            int counter2 = 0;
            for (double[] col2 : columnsData) {
                if (counter2 <= counter) {
                    counter2++;
                    continue;
                }
                double corrilation = corr.correlation(col, col2);
                if (corrilation > 0.8) {
                    if (!treemap.containsKey(corrilation)) {
                        treemap.put(corrilation, new TreeSet<>());
                    }
                    treemap.get(corrilation).add(counter);
                    treemap.get(corrilation).add(counter2);
                }
                counter2++;

            }
            counter++;
        }
        TreeMap<Double, TreeSet<Integer>> toUpdate = new TreeMap<>(treemap);
        Set<Integer> toKeep = new HashSet<>();
        Set<Integer> toremove = new HashSet<>();

        for (double d : treemap.descendingKeySet()) {
            int small = treemap.get(d).first();
            int toReplace = treemap.get(d).last();
            toremove.add(toReplace);
            treemap.descendingKeySet().stream().map(d2 -> treemap.get(d2)).filter(set -> (set.contains(toReplace))).map(_item -> {
                toUpdate.get(d).remove(toReplace);
                return _item;
            }).forEachOrdered(_item -> {
                toUpdate.get(d).add(small);
            });
        }
        for (int i = 0; i < updatedDs.getColumnIds().length; i++) {
            if (toremove.contains(i)) {
                continue;
            }
//            System.out.println("we keep " + i + "  " + updatedDs.getColumnIds()[i]);
            toKeep.add(i);
        }
        int[] selection = new int[toKeep.size()];
        int i = 0;
        for (int sel : toKeep) {
            selection[i] = sel;
            i++;
        }
        datasetUtilities.createColumnGroup(updatedDs, "active_measurements", "", "the filtered measurments", selection);
        return updatedDs;
    }

    public TraningDataset prepareDataToTrain(Dataset inputDatasetToTrain) {
        TraningDataset traningDatasetObject = new TraningDataset();
        traningDatasetObject.setColumnNames(inputDatasetToTrain.getColumnIds());

        traningDatasetObject.setRowNames(inputDatasetToTrain.getRowIds());
//        Set<Integer> outlierToRemove = new HashSet<>();
        Object[][] traningDataValues = new Object[inputDatasetToTrain.getRowIds().length + 1][inputDatasetToTrain.getColumnIds().length + 2];
        int rowIndex = 0;
        traningDataValues[0][0] = "Index";
        for (int colIndex = 0; colIndex < inputDatasetToTrain.getColumnIds().length; colIndex++) {
            traningDataValues[0][colIndex + 1] = inputDatasetToTrain.getColumnIds()[colIndex];
        };
        traningDataValues[0][inputDatasetToTrain.getColumnIds().length + 1] = "Class";
        for (double[] row : inputDatasetToTrain.getData()) {
            Object[] traningRowValues = new Object[inputDatasetToTrain.getColumnIds().length + 2];
            traningRowValues[0] = rowIndex;
            int c = 1;
            for (double d : row) {
                traningRowValues[c] = d;
                c++;
            }
            for (Group g : inputDatasetToTrain.getRowGroups()) {
                if (!g.getName().equalsIgnoreCase("ALL") && g.hasMember(rowIndex)) {
                    traningRowValues[traningRowValues.length - 1] = g.getName();
                }

            }
            traningDataValues[rowIndex + 1] = traningRowValues;
            rowIndex++;
        }
        traningDatasetObject.setData(traningDataValues);//filteredTraningData
        traningDatasetObject.setDatasetName(inputDatasetToTrain.getName());
        traningDatasetObject.setSourceDataset(inputDatasetToTrain);
        ///filter final data to id only 
        for (Group g : inputDatasetToTrain.getColumnGroups()) {
            if (g.getName().equalsIgnoreCase("active_measurements")) {
                String key = g.getIndices().toString();
                traningDatasetObject.setSelectedFeaturesKey(key);
                break;

            }
        }
        return traningDatasetObject;

    }

    public Dataset mergeDatasets(Set<Dataset> datasets, boolean train) {
        if (datasets == null || datasets.isEmpty()) {
            return null;
        }
        if (datasets.size() == 1) {
            return datasets.iterator().next();
        } else {
            String[] colnames = null;
            int dataLength = 0;
            Map<String, List<Integer>> groupMembers = new HashMap<>();
            Map<String, List<Integer>> colGroupMembers = new HashMap<>();
            for (Dataset ds : datasets) {
                if (colnames == null) {
                    colnames = ds.getColumnIds().clone();
                }
                dataLength += ds.getDataLength();
                if (train) {
                    ds.getRowGroups().stream().filter(g -> (!groupMembers.containsKey(g.getName()))).forEachOrdered(g -> {
                        if (!g.getName().equalsIgnoreCase("all")) {
                            groupMembers.put(g.getName(), new ArrayList<>());
                        }
                    });
                }
            }
            String[] rowIds = new String[dataLength];
            double[][] data = new double[dataLength][];
            int i = 0;
            for (Dataset ds : datasets) {
                for (int k = 0; k < ds.getDataLength(); k++) {
                    rowIds[i] = ds.getRowIds()[k];
                    data[i] = ds.getData()[k];
                    if (train) {
                        for (Group g : ds.getRowGroups()) {
                            if (g.getName().equalsIgnoreCase("all")) {
                                continue;
                            }
                            if (g.hasMember(k)) {
                                groupMembers.get(g.getName()).add(i);
                            }
                        }
                    }
                    i++;
                }

            }

            Dataset mergedDs = new Dataset(data, rowIds, colnames);
//            if()
//            System.out.println("column group name " + datasets.iterator().next().getColumnGroups().get(1));
//            mergedDs.addColumnGroup(datasets.iterator().next().getColumnGroups().get(1));
            if (train) {
                groupMembers.keySet().forEach(group -> {
                    datasetUtilities.createRowGroup(mergedDs, group, "", group, datasetUtilities.listToArr(groupMembers.get(group)), true);
                });
            }

            return mergedDs;
        }
    }

}
