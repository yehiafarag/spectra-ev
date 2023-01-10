/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.probe.uib.mgfevaluator.processes.handlers;

import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.io.mass_spectrometry.cms.CmsFileReader;
import com.compomics.util.experiment.mass_spectrometry.spectra.SpectrumUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private final File cmsFile;
    private CmsFileReader csmFileReader;
    private final String spectrumFileName;
//    private final double[] intensityThresholds = new double[]{0.95, 0.9, 0.7,0.5};

    public CMSFileHandler(File cmsFile) {
        this.cmsFile = cmsFile;
        this.spectrumFileName = cmsFile.getName().replace(".cms", "");
    }

    public void processCMS() {
        if (csmFileReader == null) {
            System.out.println("read cms file");
            this.readCSMFile(cmsFile);
        }
    }

//    public void updatePeakList(Set<SpectrumModel> spectrumSet) {
//        spectrumSet.stream().map(spectrum -> {
//            double[][] topPeaks75 = SpectrumUtil.getPeaksAboveIntensityThreshold(csmFileReader.getSpectrum(spectrum.getSpectrumTitle()), AnnotationParameters.IntensityThresholdType.percentile, 0.75);
//            spectrum.setNumTopPeaks(topPeaks75.length);
//            double totalTopPeakIntensity = 0;
//            for (double[] intenArr : topPeaks75) {
//                for (double d : intenArr) {
//                    totalTopPeakIntensity += d;
//                }
//            }
//            spectrum.setTopIntensity(totalTopPeakIntensity);
//
//            return spectrum;
//        }).forEachOrdered(spectrum -> {
//            spectrum.setNumPeaks(csmFileReader.getSpectrum(spectrum.getSpectrumTitle()).getNPeaks());
//            spectrum.setTotalIntensity(csmFileReader.getSpectrum(spectrum.getSpectrumTitle()).getTotalIntensity());
//            double signalToNoiseRatio = spectrum.getTopIntensity() / (spectrum.getTotalIntensity() - spectrum.getTopIntensity());
//            spectrum.setSignalToNoiseEstimation_total(signalToNoiseRatio);
//
//        });
//
//    }
//    public Set<SpectrumModel> calculateNonIdentifiedSpectrumPeakListOnIntensityLevels(Set<String> spectrumIdSet) {
//
//        Set<String> idSpectrumTitleSet = new HashSet<>();
//        for (String id : spectrumIdSet) {
//            idSpectrumTitleSet.add(id.split("_")[1]);
//        }
//
//        Set<String> spectrumSet = new HashSet<>();
//        String[] spectrumTitles = csmFileReader.getSpectrumTitles("");
//        spectrumSet.addAll(Arrays.asList(spectrumTitles));
//        System.out.println("at size 1 " + spectrumSet.size() + "  remove size " + idSpectrumTitleSet.size() + "  final size " + (spectrumSet.size() - idSpectrumTitleSet.size()));
//        System.out.println(spectrumSet.removeAll(idSpectrumTitleSet) + "   " + spectrumSet.containsAll(idSpectrumTitleSet));
//        System.out.println("final size " + spectrumSet.size());
//
//        return calculateSpectrumPeakListOnIntensityLevels(spectrumSet);
//
//    }
//    public Set<SpectrumModel> calculateSpectrumPeakListOnIntensityLevels(Set<String> spectrumIdSet) {
//        Set<SpectrumModel> spectrumSet = new HashSet<>();
//        spectrumIdSet.stream().map(spectrumid -> {
//            SpectrumModel spectrum = new SpectrumModel();
//            if (spectrumid.contains("_")) {
//                spectrum.setFile(spectrumid.split("_")[0]);
//                spectrum.setSpectrumTitle(spectrumid.split("_")[1]);
//            } else {
//                spectrum.setFile("");
//                spectrum.setSpectrumTitle(spectrumid);
//            }
//            return spectrum;
//        }).map(spectrum -> {
//            spectrum.setNumPeaks(csmFileReader.getSpectrum(spectrum.getSpectrumTitle()).getNPeaks());
//            return spectrum;
//        }).map(spectrum -> {
//            spectrum.setTotalIntensity(csmFileReader.getSpectrum(spectrum.getSpectrumTitle()).getTotalIntensity());
//            return spectrum;
//        }).map(spectrum -> {
//            //csmFileReader.getSpectrum(spectrum.getSpectrumTitle());
//            double[] intensities = csmFileReader.getSpectrum(spectrum.getSpectrumTitle()).intensity;
//
//            TreeSet<Double> sortedEntensity = new TreeSet<>();
//            double totalInt = 0;
//            for (double inten : intensities) {
//                sortedEntensity.add(inten);
//                totalInt += inten;
//            }
//            spectrum.setMaxIntensity(sortedEntensity.last());
//            spectrum.setTotalIntensity(totalInt);
//            double dynamicRange = Math.log10(spectrum.getMaxIntensity() / sortedEntensity.first());
//            spectrum.setDynamicRange(dynamicRange);
//            List<Double> avgPeaksIntensitiesDR = new ArrayList<>();
//            for (double intTh : intensityThresholds) {
//                if (intTh == 0.95) {
////                    double level = spectrum.getMaxIntensity() * intTh;
//                    int levelIndex = (int) (sortedEntensity.size() * intTh);
//                    List<Double> calList = new ArrayList<>(sortedEntensity);
//
//                    for (int i = levelIndex; i < calList.size(); i++) {
//                        double[] avgPeakInt = this.calculatePeakAvgDynamicRange(calList.get(i), intensities);
//                        avgPeaksIntensitiesDR.add(avgPeakInt[0]);
//
//                    }
//
////                    NavigableSet<Double> topPeaksOnThersh = sortedEntensity.subSet(level, true, spectrum.getMaxIntensity(), true);
////                    for (double intenArr : topPeaksOnThersh) {
////                        double avgPeakInt = this.calculatePeakAvgDynamicRange(intenArr, intensities);
////                        avgPeaksIntensitiesDR.add(avgPeakInt);
////                    }
//                } else {
////                    double level = spectrum.getMaxIntensity() * intTh;
////                    NavigableSet<Double> subset = sortedEntensity.subSet(level, true, spectrum.getMaxIntensity(), true);
//                    int levelIndex = (int) (sortedEntensity.size() * intTh);
//                    List<Double> calList = new ArrayList<>(sortedEntensity);
//                    double totalTopPeakIntensity = 0;
//                    double selectedCandidate = 0;
//                    List<Double> subset = new ArrayList<>();
//                    for (int i = levelIndex; i < calList.size(); i++) {
//                        totalTopPeakIntensity += calList.get(i);
//                        subset.add(calList.get(i));
//                        double[] avgPeakInt = this.calculatePeakAvgDynamicRange(calList.get(i), intensities);
//                        if (avgPeakInt[1] > 50) {
//                            selectedCandidate++;
//                        }
//
//                    }
//
////                    for (double in : subset) {
////                        totalTopPeakIntensity += in;
////                    }
//                    double signalToNoiseRatio = totalTopPeakIntensity / (spectrum.getTotalIntensity() - totalTopPeakIntensity);
//                    double median = -1;
//                    if (subset.size() % 2 == 0) {
//                        int index = subset.size() / 2;
//                        int index2 = index - 1;
//                        median = (subset.get(index) + subset.get(index2)) / 2.0;
//                    } else {
//                        int index = subset.size() / 2;
//                        median = subset.get(index);
//                    }
//                
//                    spectrum.getSpectrumPeaksLevels().put(intTh, new Double[]{(double) subset.size(), totalTopPeakIntensity, signalToNoiseRatio, median, selectedCandidate});
//
//                }
//            }
//            //calculate avg dynamic range
//            double avgDynamicRange = 0;
//
//            for (double avgPeakIntensity : avgPeaksIntensitiesDR) {
//                avgDynamicRange += avgPeakIntensity;
//            }
//            avgDynamicRange = avgDynamicRange / (double) avgPeaksIntensitiesDR.size();
//            spectrum.setAvgDynamicRange(avgDynamicRange);
//            return spectrum;
//        }).forEachOrdered(spectrum -> {
//            spectrumSet.add(spectrum);
//        });
//        return spectrumSet;
//
//    }
//    private double[] calculatePeakAvgDynamicRange(double peakInt, double[] allPeaksInt) {
//        double[] avgDynamic = new double[]{0.0, 0.0};
//        for (int i = 0; i < allPeaksInt.length; i++) {
//            if (allPeaksInt[i] == peakInt) {
//                if (i == 0) {
//                    avgDynamic[0] = Math.log10(Math.max(peakInt, allPeaksInt[i + 1]) / Math.min(peakInt, allPeaksInt[i + 1]));
//                    avgDynamic[1] = ((peakInt - allPeaksInt[i + 1]) / peakInt) * 100.0;
//                    return avgDynamic;
//                } else if (i == allPeaksInt.length - 1) {
//                    avgDynamic[0] = Math.log10(Math.max(peakInt, allPeaksInt[i - 1]) / Math.min(peakInt, allPeaksInt[i - 1]));
//                    avgDynamic[1] = ((peakInt - allPeaksInt[i - 1]) / peakInt) * 100.0;
//                    return avgDynamic;
//                } else {
//                    double avgDynamic1 = Math.max(peakInt, allPeaksInt[i + 1]) / Math.min(peakInt, allPeaksInt[i + 1]);
//                    double avgDynamic2 = Math.max(peakInt, allPeaksInt[i - 1]) / Math.min(peakInt, allPeaksInt[i - 1]);
//                    avgDynamic[0] = Math.log10((avgDynamic1 + avgDynamic2) / 2.0);
//
//                    double preAvg = ((peakInt - allPeaksInt[i - 1]) / peakInt) * 100.0;
//                    double postAvg = ((peakInt - allPeaksInt[i + 1]) / peakInt) * 100.0;
//                    avgDynamic[1] = (preAvg + postAvg) / 2.0;
//
//                    return avgDynamic;
//                }
//            }
//        }
//        return avgDynamic;
//    }
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
