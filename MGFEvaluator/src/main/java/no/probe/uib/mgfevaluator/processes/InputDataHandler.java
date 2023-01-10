package no.probe.uib.mgfevaluator.processes;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import no.probe.uib.mgfevaluator.model.SpectrumModel;
import no.probe.uib.mgfevaluator.processes.handlers.CMSFileHandler;
import no.probe.uib.mgfevaluator.processes.handlers.SearchEnginesInputFileHandler;
import no.uib.jexpress_modularized.core.dataset.Dataset;

/**
 *
 * @author Yehia Mokhtar Farag
 */
public class InputDataHandler {

    private final ExecutorService executorService;
    private SearchEnginesInputFileHandler searchEnginesInputFileHandler;
    private CMSFileHandler cmsFileHandler;

    public InputDataHandler() {
        this.executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }
    private Dataset dataset;
    private boolean[] members;
    private Set<String> searchEngines = null;

    public void processData(File searchEnginesInputFile, File cmsFile) {
        Future cmsFileHandlerThread = executorService.submit(() -> {
            System.out.println("process cms file");
            cmsFileHandler = new CMSFileHandler(cmsFile);
            cmsFileHandler.processCMS();
        });

        Future searchEnginesInputFileHandlerThread = executorService.submit(() -> {
            System.out.println("process se_files");
            searchEnginesInputFileHandler = new SearchEnginesInputFileHandler(searchEnginesInputFile);
            searchEngines = searchEnginesInputFileHandler.processSearchEnginesFile();
        });
        while (!cmsFileHandlerThread.isDone() || !searchEnginesInputFileHandlerThread.isDone()) {
        }
         Map<String,SpectrumModel>spectraMap = searchEnginesInputFileHandler.calculateSpectrumData(searchEngines, cmsFileHandler);
        // create datasets to analyse
        System.out.println("at cms file handeler "+spectraMap.size());
        

//        double[][] data = new double[2][2];
//        String[]names = new String[]{"lolo","bobo"};
//        String[] colnames = new String[]{"bobo","hoho"};
//        dataset = new Dataset(data, names, colnames);
//        JFrame f = new JFrame();
//        f.setSize(500, 500);
//        f.setVisible(true);
//       f.setAlwaysOnTop(true);
//        LineChartView lcv = new LineChartView(dataset , new boolean[]{true,true});
//        JPanel jp = new JPanel();
//        f.add(jp);
//        jp.setSize(500, 500);
//        jp.paint(lcv.getGraph().getGraph().getGraphics());
//       System.out.println("done");
//        List<String> searchEnginList = new ArrayList<>();
//
//        TreeMap<Double, HashSet<String>> spectraIdRank = searchEnginesFileReader.getRankSpectra();
//        TreeMap<Double, Set<SpectrumModel>> spectraRank = new TreeMap<>();
//        Set<String> identifiedSpectrumIdList = new HashSet<>();
//        for (Double i : spectraIdRank.keySet()) {
//            identifiedSpectrumIdList.addAll(spectraIdRank.get(i));
//            Set<SpectrumModel> spectrumSet = cmsFileHandler.calculateSpectrumPeakListOnIntensityLevels(spectraIdRank.get(i));
//            spectraRank.put(i, spectrumSet);
//        }
//        Set<SpectrumModel> spectrumSet = cmsFileHandler.calculateNonIdentifiedSpectrumPeakListOnIntensityLevels(searchEnginesFileReader.getSpectraRank().keySet());
//        spectraRank.put(0.0, spectrumSet);
//
//        spectraRank.values().forEach(spectrumSet_ -> {
//            spectrumSet_.forEach(spectrumModel -> {
//                String key = spectrumModel.getFile()+"_"+spectrumModel.getSpectrumTitle();
//                if (searchEnginesFileReader.getSequenceRank().containsKey(key)) {
//                    spectrumModel.setSequenceScore(searchEnginesFileReader.getSequenceRank().get(key));
//                } else {
//                    spectrumModel.setSequenceScore(0.0);
//                }
//            });
//        });
//
//        exporter.exportCorrelationOverviewTable(searchEnginesFileReader.getDataFolderUrl(), spectraRank);
    }
}
