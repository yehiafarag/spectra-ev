package no.probe.uib.mgfevaluator.processes;

import com.compomics.util.experiment.io.mass_spectrometry.MsFileIterator;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingDialog;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import no.probe.uib.mgfevaluator.gui.pca.PcaPlot;
import no.probe.uib.mgfevaluator.model.GroupDescriptiveStatisticsModel;
import no.probe.uib.mgfevaluator.model.SpectraGroup;
import no.probe.uib.mgfevaluator.model.SpectrumModel;
import no.probe.uib.mgfevaluator.model.TraningDataset;
import no.probe.uib.mgfevaluator.processes.handlers.CMSFileHandler;
import no.probe.uib.mgfevaluator.processes.handlers.CustomizedmzIdentMLIdFileReader;
import no.probe.uib.mgfevaluator.processes.handlers.MzIdFileHandler;
import no.probe.uib.mgfevaluator.processes.handlers.PSExportFileHandler;
import no.probe.uib.mgfevaluator.processes.handlers.UpdatedCmsFileWriter;

import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.core.dataset.Group;
import no.uib.jexpress_modularized.pca.computation.PcaCompute;
import no.uib.jexpress_modularized.pca.computation.PcaResults;
import no.uib.jexpress_modularized.rank.computation.util.Stat;
import no.uib.jexpress_modularized.somclust.model.ClusterParameters;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.inference.TTest;

/**
 *
 * @author yfa041
 */
public class DataProcessor {

    private final ExecutorService executorService;
    private CMSFileHandler cmsFileHandler;
    private PSExportFileHandler psExportFileHandler;
    private MzIdFileHandler mzidHandler;
    private final DatasetUtilities datasetUtilities;
    private final StatisticalAnalysisUtilities statUtilities;
    private final String[] groupColors = new String[]{"#FF3933", "#48CD19", "#1970CD", "#CD8919"};
    private int colorIndex = 0;
    private ClusterParameters parameter;

    public DataProcessor() {
        this.executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.datasetUtilities = new DatasetUtilities();
        this.statUtilities = new StatisticalAnalysisUtilities();
        parameter = new ClusterParameters();
        parameter.setDistance(1);
        parameter.setClusterSamples(true);
        parameter.setLink(ClusterParameters.LINKAGE.UPGMA);
    }

    public Dataset processDataset(File PSSearchExport, File cmsFile) {
        String datasetName = PSSearchExport.getName().replace(".txt", "").toLowerCase();
        Future cmsFileHandlerThread = executorService.submit(() -> {
            cmsFileHandler = processCmsFile(datasetName, cmsFile);
        });
        Future searchEnginesInputFileHandlerThread = executorService.submit(() -> {
            psExportFileHandler = PSExportFile(datasetName, PSSearchExport);
        });
        while (!cmsFileHandlerThread.isDone() || !searchEnginesInputFileHandlerThread.isDone()) {
        }
        cmsFileHandler.getFullSpectruaMap().keySet().stream().map(spectraKey -> cmsFileHandler.getFullSpectruaMap().get(spectraKey)).forEachOrdered(spectrum -> {
            psExportFileHandler.updateSpectrumIdentificationInformation(spectrum);
        });
        return initDataset(datasetName, cmsFileHandler.getFullSpectruaMap(), false);
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
            } catch (Exception ex) {
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

        ///Store in a file 
        return initDataset(selectedMzidFile.getName().replace(".mzid", ""), cmsFileHandler.getFullSpectruaMap(), false);

    }

    private Dataset initDataset(String datasetName, Map<String, SpectrumModel> fullSpectruaMap, boolean doStatAnalysis) {
        double[][] measurmentValues = new double[fullSpectruaMap.size()][15];
        boolean[][] missingMesurments = new boolean[fullSpectruaMap.size()][15];
        String[] rowNames = new String[fullSpectruaMap.size()];//, "top_peak_#_90""top_peak_#_70",, "top_peak_#_50"
        String[] colNames = new String[]{"population_variance", "general_SNR", "intPeakRatio", "total_peak_#", "total_intensity", "dynamic_range", "clear_peak_#_90", "top_peak_intensity_90", "SNR_90", "clear_peak_#_70", "top_peak_intensity_70", "SNR_70", "clear_peak_#_50", "top_peak_intensity_50", "SNR_50"};
        boolean[] activeRow = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
        int i = 0;
        List<Integer> unIdentifiedIndices = new ArrayList<>();
        List<Integer> identifiedIndices = new ArrayList<>();
        for (String spectrumKey : fullSpectruaMap.keySet()) {
            SpectrumModel spectrum = fullSpectruaMap.get(spectrumKey);
            rowNames[i] = spectrum.getSpectrumTitle();//, spectrum.getSpectrumPeaksLevels().get(0.9)[0], spectrum.getSpectrumPeaksLevels().get(0.7)[0], spectrum.getSpectrumPeaksLevels().get(0.5)[0]
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
//        Set<Integer> outliers = datasetUtilities.handelDataOutlier(measurmentValues);
//        unIdentifiedIndices.removeAll(outliers);
//        identifiedIndices.removeAll(outliers);
//        String[] updatedRowNames = new String[rowNames.length - outliers.size()];
//        double[][] updatedMeasurmentValues = new double[updatedRowNames.length][15];
//        i = 0;
//        int j = 0;
//        for (String str : rowNames) {
//            if (!outliers.contains(i)) {
//                updatedRowNames[j] = str;
//                updatedMeasurmentValues[j] = measurmentValues[i];
//                j++;
//            }
//            i++;
//        }

        Dataset dataset = new Dataset(measurmentValues, rowNames, colNames);
        dataset.setMissingMeasurements(missingMesurments);
        if (!unIdentifiedIndices.isEmpty()) {
            datasetUtilities.createRowGroup(dataset, "UnIdentified", "", "Unidentified spectra", datasetUtilities.listToArr(unIdentifiedIndices), doStatAnalysis);
        }
        if (!identifiedIndices.isEmpty()) {
            datasetUtilities.createRowGroup(dataset, "Identified", "", "Identified spectra", datasetUtilities.listToArr(identifiedIndices), doStatAnalysis);
        }
        for (Group g : dataset.getColumnGroups()) {
            if (g.getName().equalsIgnoreCase("ALL")) {
                g.setActive(true);
                g.setColor(Color.BLACK);
                g.setHashColor("#000000");
                for (int x = 0; x < dataset.getColumnIds().length; x++) {
                    g.addMember(x);
                }
                break;
            }
        }
        //System.out.println(datasetName+"  --->  outliers size "+outliers.size()+"  total datasize "+rowNames.length);
        dataset.setName(datasetName);
        return dataset;
    }

    private TraningDataset prepareDataToTrain(Dataset inputDatasetToTrain) {
        TraningDataset traningDatasetObject = new TraningDataset();
        traningDatasetObject.setColumnNames(inputDatasetToTrain.getColumnIds());

        traningDatasetObject.setRowNames(inputDatasetToTrain.getRowIds());
        Set<Integer> outlierToRemove = new HashSet<>();
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
            //identify the group type
            Group represetitiveGroup = null;
            for (Group g : inputDatasetToTrain.getRowGroups()) {
                if (g.hasMember(rowIndex)) {
                    traningRowValues[traningRowValues.length - 1] = g.getName();
                    represetitiveGroup = g;
                    break;
                }
            }
            if (represetitiveGroup == null) {
                outlierToRemove.add(rowIndex);
                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<error in data the item does not belong to any group " + rowIndex + "  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                rowIndex++;
                continue;
            }

            for (int colIndex = 0; colIndex < inputDatasetToTrain.getColumnIds().length; colIndex++) {
                double dataValue = row[colIndex];
                if (represetitiveGroup instanceof SpectraGroup) {
                    SpectraGroup subGroup = (SpectraGroup) represetitiveGroup;
                    GroupDescriptiveStatisticsModel gsm = subGroup.getGroupDescriptiveStatisticsModels().get(colIndex);
                    String eval = statUtilities.evaluateGroup(gsm, dataValue);
                    if (!eval.startsWith("MOSTLY_") && !eval.startsWith("MAYBE_")) {
                        outlierToRemove.add(rowIndex);
                    }
                }
                traningRowValues[colIndex + 1] = dataValue;
            }
            traningDataValues[rowIndex + 1] = traningRowValues;
            rowIndex++;
        }
        Object[][] filteredTraningData = new Object[traningDataValues.length - outlierToRemove.size()][traningDataValues[0].length];
        filteredTraningData[0] = traningDataValues[0];
        int reIndex = 1;
        for (int i = 1; i < traningDataValues.length; i++) {
            if (!outlierToRemove.contains(i - 1)) {
                filteredTraningData[reIndex] = traningDataValues[i];
                reIndex++;
            }
        }
        System.out.println("outliers number of data--->> wont indetify: " + outlierToRemove.size());
        traningDatasetObject.setData(filteredTraningData);//
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

    private TraningDataset measureDataSimilarity(Set<Dataset> inputDatasetToTrain) {
        Map<String, HashSet<Dataset>> datasetComparisonsMap = new HashMap<>();
        TreeSet<String> namerder = new TreeSet<>();
        for (Dataset ds : inputDatasetToTrain) {
            namerder.add(ds.getName());
            for (Dataset ds2 : inputDatasetToTrain) {
                namerder.add(ds2.getName());
                if (namerder.size() <= 1) {
                    continue;
                }
                String combName = namerder.toString();
                if (!datasetComparisonsMap.containsKey(combName)) {
                    datasetComparisonsMap.put(combName, new HashSet<>());
                }
                datasetComparisonsMap.get(combName).add(ds);
                datasetComparisonsMap.get(combName).add(ds2);
                namerder.remove(ds2.getName());
            }
            namerder.clear();
        }

        Set<TraningDataset> trainedProjectData = new LinkedHashSet<>();
        for (Dataset ds : inputDatasetToTrain) {
            trainedProjectData.add(prepareProjectstoCompare(ds));
        }
        statUtilities.showBoxPlotforMeasurments(trainedProjectData, "Overview");
        statUtilities.showHistogramPlotforMeasurments(trainedProjectData, "histogram");

        System.out.println("name of comb " + datasetComparisonsMap.keySet());
        return null;

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

    public Dataset compareTraningDatasets(Set<Dataset> datasets) {

        String[] colnames = null;
        int dataLength = 0;
        Map<String, List<Integer>> groupMembers = new HashMap<>();
        for (Dataset ds : datasets) {
            if (colnames == null) {
                colnames = ds.getColumnIds().clone();
            }
            dataLength += ds.getDataLength();
            ds.getRowGroups().stream().filter(g -> (!groupMembers.containsKey(g.getName()))).forEachOrdered(g -> {
                if (g.getName().equalsIgnoreCase("all")) {
                    groupMembers.put("DS-" + ds.getName(), new ArrayList<>());
                }
            });

        }
        String[] rowIds = new String[dataLength];
        double[][] data = new double[dataLength][];
        boolean[][] missingMeasurements = new boolean[dataLength][];

        int i = 0;
        for (Dataset ds : datasets) {
            for (int k = 0; k < ds.getDataLength(); k++) {
                rowIds[i] = ds.getRowIds()[k];
                data[i] = ds.getData()[k];
                missingMeasurements[i] = new boolean[data[i].length];
                for (int z = 0; z < missingMeasurements[i].length; z++) {
                    missingMeasurements[i][z] = true;
                }
                for (Group g : ds.getRowGroups()) {
                    if (g.getName().equalsIgnoreCase("all")) {
                        groupMembers.get("DS-" + ds.getName()).add(i);
                    }
                }
                i++;
            }
        }

        Dataset mergedDs = new Dataset(data, rowIds, colnames);
        colorIndex = 0;
        groupMembers.keySet().forEach(group -> {
            datasetUtilities.createRowGroup(mergedDs, group, groupColors[colorIndex++], group, datasetUtilities.listToArr(groupMembers.get(group)), true);
            if (colorIndex == groupColors.length) {
                colorIndex = 0;
            }

        });
        mergedDs.setMissingMeasurements(missingMeasurements);

        /**
         * *switch (distanceMeasure) { case 0: distanceMeasureStr = "Squared
         * Euclidean"; break; case 1: distanceMeasureStr = "Euclidean"; break;
         * case 2: distanceMeasureStr = "Bray Curtis"; break; case 3:
         * distanceMeasureStr = "Manhattan"; break; case 4: distanceMeasureStr =
         * "Cosine Correlation"; break; case 5: distanceMeasureStr = "Pearson
         * Correlation"; break;
         *
         * case 6: distanceMeasureStr = "Uncentered Pearson Correlation"; break;
         *
         * case 7: distanceMeasureStr = "Euclidean (Nullweighted)"; break; case
         * 8: distanceMeasureStr = "Camberra"; break; case 9: distanceMeasureStr
         * = "Chebychev"; break; case 10: distanceMeasureStr = "Spearman Rank
         * Correlation"; break;
         *
         * }**
         */
        Thread t = new Thread(() -> {
            try {
//                System.out.println("at run clustering start ");
//                ClusterParameters parameter = new ClusterParameters();
//                parameter.setDistance(1);
//                parameter.setClusterSamples(true);              
//                parameter.setLink(ClusterParameters.LINKAGE.SINGLE);
//                SOMClustCompute som = new SOMClustCompute(mergedDs, parameter);
//                ClusterResults results = som.runClustering();
//                System.out.println("at done with calculating sum clust " + (results.getColumnDendrogramRootNode() == null));
//
////                SomclustView view = new SomclustView(mergedDs, parameter, results);
//
//                SomClustImgGenerator imgGenerator = new SomClustImgGenerator(results.getRowDendrogramRootNode(), results.getColumnDendrogramRootNode(), mergedDs.getDataLength());
//               
//                JLabel topTree = this.imageToPanel(imgGenerator.generateTopTree(results.getColumnDendrogramRootNode()));
//                JLabel sideTree = this.imageToPanel(imgGenerator.generateSideTree(results.getRowDendrogramRootNode()));
////                JPanel heatmapPanel = this.imageToPanel(imgGenerator.generateHeatMap(mergedDs, true));
//               
//
//             
//                
//                //view.getClust()
//
//                JFrame jf = new JFrame();
//                jf.add(topTree);
////                jf.add(sideTree);
////                jf.add(heatmapPanel);
//                jf.setSize(new Dimension(1000, 1000));
//                jf.setVisible(true);
                for (Group g : mergedDs.getRowGroups()) {
                    g.setActive(!g.getName().equalsIgnoreCase("all"));
                }
                PcaCompute pcaCompute = new PcaCompute(mergedDs);
                no.uib.jexpress_modularized.pca.computation.PcaResults jResults = pcaCompute.createPCA();
                updatePlot(jResults, mergedDs, 0, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();

        return mergedDs;

    }

    public Set<Dataset> compareTraningProjects(Set<Dataset> projects, Dataset sample, String featuresKey) {
        Map<String, Set<Dataset>> dsSimMap = new HashMap<>();
        for (Dataset ds : projects) {
            for (Group g : ds.getColumnGroups()) {
                if (g.getName().equalsIgnoreCase("active_measurements")) {
                    String key = g.getIndices().toString();
                    if (!dsSimMap.containsKey(key)) {
                        dsSimMap.put(key, new HashSet<>());
                    }
                    dsSimMap.get(key).add(ds);
                }
            }

        }

        Set<Dataset> selectedProjects = null;
        if (dsSimMap.containsKey(featuresKey)) {

            selectedProjects = dsSimMap.get(featuresKey);
        } else {
            for (String key : dsSimMap.keySet()) {
                if (key.replace("[", "").replace("]", "").contains(featuresKey.replace("[", "").replace("]", ""))) {
                    selectedProjects = dsSimMap.get(key);
                }
            }

        }
        if (selectedProjects == null) {
            selectedProjects = projects;
        }

        PcaCompute samplePca = new PcaCompute(sample);
        PcaResults sampleResults = samplePca.createPCA();
        double[][] samplePcavalues = new double[sample.getDataLength()][2];
        int sampleIndexer = 0;
        for (String str : sample.getRowIds()) {
            samplePcavalues[sampleIndexer][0] = sampleResults.ElementAt(sampleIndexer, 0);
            samplePcavalues[sampleIndexer][1] = sampleResults.ElementAt(sampleIndexer, 1);
            sampleIndexer++;

        }

        Map<String, double[][]> datasetPcaAnalysis = new HashMap<>();
        for (Dataset ds : selectedProjects) {
            PcaCompute pca = new PcaCompute(ds);
            PcaResults results = pca.createPCA();
            double[][] pcavalues = new double[ds.getDataLength()][2];
            int indexer = 0;
            for (String str : ds.getRowIds()) {
                pcavalues[indexer][0] = results.ElementAt(indexer, 0);
                pcavalues[indexer][1] = results.ElementAt(indexer, 1);
                indexer++;

            }
            datasetPcaAnalysis.put(ds.getName(), pcavalues);
        }
        TTest ttest = new TTest();
        for (String str2 : datasetPcaAnalysis.keySet()) {
            double[][] pc2 = datasetPcaAnalysis.get(str2);
            if (ttest.tTest(samplePcavalues[0], pc2[0], 0.05) || ttest.tTest(samplePcavalues[1], pc2[1], 0.05)) {
                System.out.println(sample.getName().split("__")[0] + " vs " + str2.split("__")[0]);
                System.out.println("    distance " + ttest.tTest(samplePcavalues[0], pc2[0]) + "   tStatistic " + Stat.tStatistic(samplePcavalues[0], pc2[0]) + "   " + ttest.tTest(samplePcavalues[0], pc2[0], 0.05));
                System.out.println("---------------------------------------------------remove "+str2);
               
            }
        }
        System.out.println("total projoects "+projects.size()+"  selected "+selectedProjects.size());

        return selectedProjects;

    }

    public void reduceDatasetMeasurments(Dataset ds) {

        //remove outliers of the data 
        Set<Integer> outliers = datasetUtilities.handelDataOutlier(ds.getData());

//        unIdentifiedIndices.removeAll(outliers);
//        identifiedIndices.removeAll(outliers);
        String[] updatedRowNames = new String[ds.getRowIds().length - outliers.size()];
        double[][] updatedMeasurmentValues = new double[updatedRowNames.length][ds.getDataWidth()];
        int rowIndex = 0;
        int reIndex = 0;
        for (String str : ds.getRowIds()) {
            if (!outliers.contains(rowIndex)) {
                updatedRowNames[reIndex] = str;
                updatedMeasurmentValues[reIndex] = ds.getData()[rowIndex];
                reIndex++;
            }
            rowIndex++;
        }

        Dataset updatedDs = new Dataset(updatedMeasurmentValues, updatedRowNames, ds.getColumnIds());
        for (Group g : ds.getRowGroups()) {
            if (!g.getName().equalsIgnoreCase("ALL")) {
                g.getIndices().removeAll(outliers);
                List<Integer> updatedGroupIndexex = g.getIndices();
                datasetUtilities.createRowGroup(updatedDs, g.getName(), g.getHashColor(), g.getDescription(), datasetUtilities.listToArr(updatedGroupIndexex), true);
            }
        }

        System.out.println("run dataset column correlations " + ds.getName());
        System.out.println("-----------------------------------");
        PearsonsCorrelation corr = new PearsonsCorrelation();
        List<double[]> columnsData = new ArrayList<>();
        for (int i = 0; i < ds.getDataWidth(); i++) {
            double[] column = new double[ds.getDataLength()];
            for (int j = 0; j < ds.getDataLength(); j++) {
                column[j] = ds.getData()[j][i];
            }
            columnsData.add(column);
        }
        TreeMap<Double, TreeSet<Integer>> treemap = new TreeMap<>();
        List<String> noCorrData = new ArrayList<>();

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
        for (int i = 0; i < ds.getColumnIds().length; i++) {
            if (toremove.contains(i)) {
                continue;
            }
            System.out.println("we keep " + i + "  " + ds.getColumnIds()[i]);
            toKeep.add(i);
        }
        int[] selection = new int[toKeep.size()];
        int i = 0;
        for (int sel : toKeep) {
            selection[i] = sel;
            i++;
        }
        System.out.println("atdone with clustering");
        datasetUtilities.createColumnGroup(ds, "active_measurements", "", "the filtered measurments", selection);

//        Thread t = new Thread(() -> {
//            try {
//
//                PcaCompute pcaCompute = new PcaCompute(ds);
//                no.uib.jexpress_modularized.pca.computation.PcaResults jResults = pcaCompute.createPCA();
//                updatePlot(jResults, ds, 0, 1);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//        t.start();
    }

    private JLabel imageToPanel(BufferedImage img) {
        JPanel imagePanel = new JPanel();
        imagePanel.setSize(img.getWidth(), img.getHeight());
        JLabel jLabel = new JLabel(new ImageIcon(img));
        jLabel.setSize(img.getWidth(), img.getHeight());
//        imagePanel.add(jLabel);
        return jLabel;
    }

    private void updatePlot(PcaResults pcaResults, Dataset dataset, int pcax, int pcay) {
        if (pcaResults == null) {
            return;
        }

        double[][] points = new double[2][(int) pcaResults.nrPoints()];
        for (int i = 0; i < pcaResults.nrPoints(); i++) {
            points[0][i] = pcaResults.ElementAt(i, pcax);
            points[1][i] = pcaResults.ElementAt(i, pcay);

        }

        final PcaPlot plot = new PcaPlot();
        plot.setData(dataset);
        String[] rowIds = dataset.getRowIds();

        plot.zoomout();
        plot.setForceEndLabel(true);
        plot.setPropsAndData(points[0], points[1]);

        plot.setXaxisTitle("Principal Component " + (pcax + 1));
        plot.setYaxisTitle("Principal Component " + (pcay + 1));
        plot.setFullRepaint(true);
        plot.forceFullRepaint();
        plot.setSize(500, 500);
        JFrame jf = new JFrame();
        jf.add(plot);
        jf.setSize(500, 500);
        jf.setVisible(true);

    }

    private CMSFileHandler processCmsFile(String datasetName, File cmsFile) {
//        if (traningData) {
        CMSFileHandler cmsFileHandler = new CMSFileHandler(datasetName, cmsFile);
        cmsFileHandler.processCMS();
        return cmsFileHandler;
//        } else {
//            sampleCmsFileHandler = new CMSFileHandler(cmsFile);
//            sampleCmsFileHandler.processCMS();
//        }
    }

    private PSExportFileHandler PSExportFile(String datasetName, File psExportFile) {

        PSExportFileHandler PSExportFileHandler = new PSExportFileHandler(psExportFile);
        PSExportFileHandler.processPSExportFile(datasetName);
        return PSExportFileHandler;
    }

    public TraningDataset trainDataset(Dataset dataset) {
        return prepareDataToTrain(dataset);
    }

    public TraningDataset sampleDataset(Dataset dataset) {
        return prepareDataToSample(dataset);
    }
    private CMSFileHandler sampleCmsFileHandler;
    private PSExportFileHandler sampleSearchEngineOutputFileHandler;
    private Set<String> sampleSearchEngines = null;

    private TraningDataset prepareProjectstoCompare(Dataset inputDatasetToSample) {
        TraningDataset sampleDatasetObject = new TraningDataset();
        sampleDatasetObject.setColumnNames(inputDatasetToSample.getColumnIds());
        sampleDatasetObject.setRowNames(inputDatasetToSample.getRowIds());
        Set<Integer> outlierToRemove = new HashSet<>();

        Object[][] sampleDataValues = new Object[inputDatasetToSample.getData().length + 1][inputDatasetToSample.getData()[0].length + 2];
        int rowIndex = 0;
        sampleDataValues[0][0] = "Index";
        for (int colIndex = 0; colIndex < inputDatasetToSample.getColumnIds().length; colIndex++) {
            sampleDataValues[0][colIndex + 1] = inputDatasetToSample.getColumnIds()[colIndex];
        };
        sampleDataValues[0][inputDatasetToSample.getColumnIds().length + 1] = "Class";
        for (double[] row : inputDatasetToSample.getData()) {
            Object[] traningRowValues = new Object[inputDatasetToSample.getColumnIds().length + 2];
            traningRowValues[0] = rowIndex;
            for (int colIndex = 0; colIndex < inputDatasetToSample.getColumnIds().length; colIndex++) {
                double dataValue = row[colIndex];
                traningRowValues[colIndex + 1] = dataValue;

            }
            sampleDataValues[rowIndex + 1] = traningRowValues;
            rowIndex++;
        }
        sampleDatasetObject.setData(sampleDataValues);
        return sampleDatasetObject;

    }

    private TraningDataset prepareDataToSample(Dataset inputDatasetToSample) {
        TraningDataset sampleDatasetObject = new TraningDataset();
        sampleDatasetObject.setColumnNames(inputDatasetToSample.getColumnIds());
        sampleDatasetObject.setRowNames(inputDatasetToSample.getRowIds());
        Set<Integer> outlierToRemove = new HashSet<>();

        Object[][] sampleDataValues = new Object[inputDatasetToSample.getData().length + 1][inputDatasetToSample.getData()[0].length + 2];
        int rowIndex = 0;
        sampleDataValues[0][0] = "Index";
        for (int colIndex = 0; colIndex < inputDatasetToSample.getColumnIds().length; colIndex++) {
            sampleDataValues[0][colIndex + 1] = inputDatasetToSample.getColumnIds()[colIndex];
        };
        sampleDataValues[0][inputDatasetToSample.getColumnIds().length + 1] = "Class";
        for (double[] row : inputDatasetToSample.getData()) {
            Object[] traningRowValues = new Object[inputDatasetToSample.getColumnIds().length + 2];
            traningRowValues[0] = rowIndex;
            //identify the group type
            Group represetitiveGroup = null;
            for (Group g : inputDatasetToSample.getRowGroups()) {
                if (g.hasMember(rowIndex)) {
                    traningRowValues[traningRowValues.length - 1] = g.getName();
                    represetitiveGroup = g;
                    break;
                }
            }
            if (represetitiveGroup == null) {
                outlierToRemove.add(rowIndex);
                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<error in data the item does not belong to any group " + rowIndex + "  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                rowIndex++;
                continue;
            }

            for (int colIndex = 0; colIndex < inputDatasetToSample.getColumnIds().length; colIndex++) {
                double dataValue = row[colIndex];
                traningRowValues[colIndex + 1] = dataValue;

            }
            sampleDataValues[rowIndex + 1] = traningRowValues;
            rowIndex++;
        }
        sampleDatasetObject.setData(sampleDataValues);
        return sampleDatasetObject;

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
//                System.out.println("value " + value); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setSecondaryProgressCounterIndeterminate(boolean indeterminate) {
//                System.out.println("indeterminate " + indeterminate); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setMaxSecondaryProgressCounter(int maxProgressValue) {
//                System.out.println("MaxSecondaryProgressCounte " + maxProgressValue); //To change body of generated methods, choose Tools | Templates.
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

}
