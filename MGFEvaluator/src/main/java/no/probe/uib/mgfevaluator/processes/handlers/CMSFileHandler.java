/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.probe.uib.mgfevaluator.processes.handlers;

import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileReader;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import no.probe.uib.mgfevaluator.model.SpectrumModel;

/**
 *
 * @author yfa041
 */
public class CMSFileHandler {

    public String getSpectrumFileName() {
        return spectrumFileName;
    }

    public Map<String, SpectrumModel> getFullSpectruaMap() {
        return fullSpectruaMap;
    }

    private final File cmsFile;
    private CmsFileReader csmFileReader;
    private final String spectrumFileName;
    private final Map<String, SpectrumModel> fullSpectruaMap;
    private final double[] intensityThresholds = new double[]{0.95, 0.9, 0.7, 0.5};

    public CMSFileHandler(File cmsFile) {
        this.fullSpectruaMap = new HashMap<>();
        this.cmsFile = cmsFile;
        this.spectrumFileName = cmsFile.getName().replace(".cms", "");
    }

    public void processCMS() {
        if (csmFileReader == null) {
            System.out.println("read cms file");
            this.readCSMFile(cmsFile);
            this.initFullSpectraMap();
        }
    }

    private void initFullSpectraMap() {
        fullSpectruaMap.clear();
        String[] spectrumTitles = csmFileReader.getSpectrumTitles("");
        for (String spectrumTitle : spectrumTitles) {
            SpectrumModel spectrumModel = new SpectrumModel();
            Spectrum spectrum = csmFileReader.getSpectrum(spectrumTitle);
            spectrumModel.setSpectrumKey(spectrumFileName + "_" + spectrumTitle);
            spectrumModel.setSpectrumTitle(spectrumTitle);
            spectrumModel.setFile(spectrumFileName);
            spectrumModel.setTheoreticalCharge(spectrum.getPrecursor().getPossibleChargesAsString());
            spectrumModel.setTheoreticalMass(spectrum.getPrecursor().mz);
            spectrumModel.setTotalIntensity(spectrum.getTotalIntensity());
            spectrumModel.setNumPeaks(spectrum.getNPeaks());
            calculateSpectrumPeakListOnIntensityLevels(spectrumModel, spectrum);
            fullSpectruaMap.put(spectrumModel.getSpectrumKey(), spectrumModel);
        }
    }

    private void calculateSpectrumPeakListOnIntensityLevels(SpectrumModel spectrumModel, Spectrum spectrum) {

        double[] intensities = spectrum.intensity;
        TreeSet<Double> sortedEntensity = new TreeSet<>();
        double totalInt = 0;
        for (double inten : intensities) {
            sortedEntensity.add(inten);
            totalInt += inten;
        }
        spectrumModel.setMaxIntensity(sortedEntensity.last());
        spectrumModel.setTotalIntensity(totalInt);
        double dynamicRange = Math.log10(spectrum.getMaxIntensity() / sortedEntensity.first());
        spectrumModel.setDynamicRange(dynamicRange);
        List<Double> avgPeaksIntensitiesDR = new ArrayList<>();
        for (double intTh : intensityThresholds) {
            if (intTh == 0.95) {
                int levelIndex = (int) (sortedEntensity.size() * intTh);
                List<Double> calList = new ArrayList<>(sortedEntensity);
                for (int i = levelIndex; i < calList.size(); i++) {
                    double[] avgPeakInt = this.calculatePeakAvgDynamicRange(calList.get(i), intensities);
                    avgPeaksIntensitiesDR.add(avgPeakInt[0]);
                }
            } else {
                int levelIndex = (int) (sortedEntensity.size() * intTh);
                List<Double> calList = new ArrayList<>(sortedEntensity);
                double totalTopPeakIntensity = 0;
                double selectedCandidate = 0;
                List<Double> subset = new ArrayList<>();
                for (int i = levelIndex; i < calList.size(); i++) {
                    totalTopPeakIntensity += calList.get(i);
                    subset.add(calList.get(i));
                    double[] avgPeakInt = this.calculatePeakAvgDynamicRange(calList.get(i), intensities);
                    if (avgPeakInt[1] > 50) {
                        selectedCandidate++;
                    }

                }
                double signalToNoiseRatio = (totalTopPeakIntensity / (spectrum.getTotalIntensity() - totalTopPeakIntensity));

                double median = -1;
                if (subset.size() % 2 == 0) {
                    int index = subset.size() / 2;
                    int index2 = index - 1;
                    median = (subset.get(index) + subset.get(index2)) / 2.0;
                } else {
                    int index = subset.size() / 2;
                    median = subset.get(index);
                }

                spectrumModel.getSpectrumPeaksLevels().put(intTh, new Double[]{(double) subset.size(), totalTopPeakIntensity, signalToNoiseRatio, median, selectedCandidate});

            }
        }
        //calculate avg dynamic range
        double avgDynamicRange = 0;

        avgDynamicRange = avgPeaksIntensitiesDR.stream().map(avgPeakIntensity -> avgPeakIntensity).reduce(avgDynamicRange, (accumulator, _item) -> accumulator + _item);
        avgDynamicRange = avgDynamicRange / (double) avgPeaksIntensitiesDR.size();
        spectrumModel.setAvgDynamicRange(avgDynamicRange);

    }

    private double[] calculatePeakAvgDynamicRange(double peakInt, double[] allPeaksInt) {
        double[] avgDynamic = new double[]{0.0, 0.0};
        for (int i = 0; i < allPeaksInt.length; i++) {
            if (allPeaksInt[i] == peakInt) {
                if (i == 0) {
                    avgDynamic[0] = Math.log10(Math.max(peakInt, allPeaksInt[i + 1]) / Math.min(peakInt, allPeaksInt[i + 1]));
                    avgDynamic[1] = ((peakInt - allPeaksInt[i + 1]) / peakInt) * 100.0;
                    return avgDynamic;
                } else if (i == allPeaksInt.length - 1) {
                    avgDynamic[0] = Math.log10(Math.max(peakInt, allPeaksInt[i - 1]) / Math.min(peakInt, allPeaksInt[i - 1]));
                    avgDynamic[1] = ((peakInt - allPeaksInt[i - 1]) / peakInt) * 100.0;
                    return avgDynamic;
                } else {
                    double avgDynamic1 = Math.max(peakInt, allPeaksInt[i + 1]) / Math.min(peakInt, allPeaksInt[i + 1]);
                    double avgDynamic2 = Math.max(peakInt, allPeaksInt[i - 1]) / Math.min(peakInt, allPeaksInt[i - 1]);
                    avgDynamic[0] = Math.log10((avgDynamic1 + avgDynamic2) / 2.0);

                    double preAvg = ((peakInt - allPeaksInt[i - 1]) / peakInt) * 100.0;
                    double postAvg = ((peakInt - allPeaksInt[i + 1]) / peakInt) * 100.0;
                    avgDynamic[1] = (preAvg + postAvg) / 2.0;

                    return avgDynamic;
                }
            }
        }
        return avgDynamic;
    }

    public CmsFileReader getCsmFileReader() {
        return csmFileReader;
    }

    private void readCSMFile(File cmsFile) {
        try {
            csmFileReader = new CmsFileReader(cmsFile, null);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
