package no.probe.uib.mgfevaluator.processes.handlers;

import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import no.probe.uib.mgfevaluator.model.PeptideModel;
import no.probe.uib.mgfevaluator.model.SpectrumModel;

/**
 *
 * @author yfa041
 */
public class SearchEnginesInputFileHandler {

    private final File dataFile;
    private String dataFolderUrl;
    private Map<String, Double> spectraRank;
    private TreeMap<Double, HashSet<String>> rankSpectra;
    private Map<String, HashSet<String>> spectraPeptideMatch;
    private Map<String, HashSet<String>> spectraSearchEngines;

    private Map<String, Double> sequenceRankMap;
    private TreeMap<Double, HashSet<String>> rankSequence;

    public Map<String, Double> getSequenceRankMap() {
        return sequenceRankMap;
    }

    public TreeMap<Double, HashSet<String>> getRankSequence() {
        return rankSequence;
    }

    public String getDataFolderUrl() {
        return dataFolderUrl;
    }

    public Map<String, Double> getSpectraRank() {
        return spectraRank;
    }

    public TreeMap<Double, HashSet<String>> getRankSpectra() {
        return rankSpectra;
    }

    public Map<String, HashSet<String>> getSpectraPeptideMatch() {
        return spectraPeptideMatch;
    }

    public SearchEnginesInputFileHandler(File dataFile) {
        this.dataFile = dataFile;
    }

    private Map<String, HashMap<String, PeptideModel>> spectraSearchEnginesData;
    private final double[] intensityThresholds = new double[]{0.95, 0.9, 0.7, 0.5};
    private double avgSequenceLength;
    private double avgSearchCharge;

    public Set<String> processSearchEnginesFile() {
        try {
            spectraSearchEnginesData = new HashMap<>();
            dataFolderUrl = dataFile.getParent();
            FileReader input = new FileReader(dataFile);
            List<PeptideModel> peptides = new ArrayList<>();
            Set<String> searchEngines = new HashSet<>();

            spectraPeptideMatch = new HashMap<>();
            spectraSearchEngines = new HashMap<>();
            String line = "empty";
            try (BufferedReader reader = new BufferedReader(input)) {
                //escape header
                line = reader.readLine();
                String[] headers = line.split("\\t");
                System.out.println(headers.length + " - line: -" + line);
                int index = 1;
                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] values = line.split("\\t");

                    if (values.length < headers.length || !values[20].equalsIgnoreCase("Confident") || values[19].equalsIgnoreCase("1")) {
                        continue;
                    }
                    PeptideModel peptide = new PeptideModel();
                    peptide.setIndex(index++);
                    peptide.setRank(Integer.parseInt(values[1]));
                    peptide.setSequence(values[4]);
                    peptide.setSequenceLength(peptide.getSequence().length());
                    peptide.setModifiedSequence(values[5]);
                    peptide.setVariableModification(values[6]);
                    peptide.setFixedModification(values[7]);
                    peptide.setSpectrumFile(values[8]);
                    peptide.setSpectrumTitle(values[9]);
                    peptide.setRT(Double.parseDouble(values[11]));
                    peptide.setM_z(Double.parseDouble(values[12]));
                    if (!values[13].trim().equalsIgnoreCase("")) {
                        peptide.setMeasuredCharge(Integer.parseInt(values[13].replace("+", "")));
                    }
                    peptide.setIdentificationCharge(Integer.parseInt(values[15]));
                    String searchEngine = values[16].replace(" (", "_").split("_")[0].trim();
                    searchEngines.add(searchEngine);
                    peptide.setSearchEngine(searchEngine);
                    peptide.setAlgorithmConfidence(Double.parseDouble(values[18]));
                    peptide.setValidation(true);
                    peptides.add(peptide);
                    if (!spectraPeptideMatch.containsKey(peptide.getSpectrumKey())) {
                        spectraPeptideMatch.put(peptide.getSpectrumKey(), new HashSet<>());
                    }
                    spectraPeptideMatch.get(peptide.getSpectrumKey()).add(peptide.getSequence());
                    if (!spectraSearchEngines.containsKey(peptide.getSpectrumKey())) {
                        spectraSearchEngines.put(peptide.getSpectrumKey(), new HashSet<>());
                    }
                    spectraSearchEngines.get(peptide.getSpectrumKey()).add(peptide.getSearchEngine());

                    if (!spectraSearchEnginesData.containsKey(peptide.getSpectrumKey())) {
                        spectraSearchEnginesData.put(peptide.getSpectrumKey(), new HashMap<>());
                    }
                    spectraSearchEnginesData.get(peptide.getSpectrumKey()).put(peptide.getSearchEngine(), peptide);

                }
            } catch (FileNotFoundException ex) {
                System.err.println(SearchEnginesInputFileHandler.class.getName() + " -- " + ex);
            } catch (IOException ex) {
                System.err.println(SearchEnginesInputFileHandler.class.getName() + " -- " + ex);
            } catch (NumberFormatException ex) {
                System.out.println("exc here " + line);
                int i = 0;
                for (String str : line.split("\t")) {
                    System.out.println("at index " + i + "  " + str);
                    i++;
                }
            }

            this.calculateSpectrumRank(spectraPeptideMatch, spectraSearchEngines, searchEngines.size());
            System.out.println("at total peptides after filtering " + peptides.size() + "  SE " + searchEngines + "  " + rankSpectra.firstKey() + "   " + rankSpectra.lastKey());
            rankSpectra.keySet().forEach(rank -> {
                System.out.println(rank + "  " + rankSpectra.get(rank).size());
            });
            return searchEngines;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Map<String, SpectrumModel> calculateSpectrumData(Set<String> searchEngines, CMSFileHandler csmFileHandler) {
        String[] spectrumTitles = csmFileHandler.getCsmFileReader().getSpectrumTitles("");
        String fileName = csmFileHandler.getSpectrumFileName();
        Map<String, SpectrumModel> spectraMap = new HashMap<>();
        for (String spectrumTitle : spectrumTitles) {
            Spectrum spectrum = csmFileHandler.getCsmFileReader().getSpectrum(spectrumTitle);
            SpectrumModel spectrumModel = new SpectrumModel();
            spectrumModel.setSpectrumKey(fileName + "_" + spectrumTitle);
            spectrumModel.setSpectrumTitle(spectrumTitle);
            spectrumModel.setFile(fileName);
            spectrumModel.setTheoreticalCharge(spectrum.getPrecursor().getPossibleChargesAsString());
            spectrumModel.setTheoreticalMass(spectrum.getPrecursor().mz);
            spectrumModel.setTotalIntensity(spectrum.getTotalIntensity());
            spectrumModel.setNumPeaks(spectrum.getNPeaks());
            calculateSpectrumPeakListOnIntensityLevels(spectrumModel, spectrum);
            if (spectraSearchEnginesData.containsKey(spectrumModel.getSpectrumKey())) {
                spectrumModel.setIdentified(true);
                double searchEngineRank = (double) spectraSearchEnginesData.get(spectrumModel.getSpectrumKey()).size() * 100 / (double) searchEngines.size();
                spectrumModel.setSearchEngineRank((int) searchEngineRank);
                //calculate sequence rank 
                Set<String> sequences = new HashSet<>();
                avgSequenceLength = 0;
                avgSearchCharge = 0;
                spectraSearchEnginesData.get(spectrumModel.getSpectrumKey()).values().forEach(peptide -> {
                    sequences.add(peptide.getSequence());
                    avgSequenceLength += (double) peptide.getSequence().length();
                    avgSearchCharge += (double) peptide.getIdentificationCharge();
                });
                avgSequenceLength = avgSequenceLength / (double) spectraSearchEnginesData.get(spectrumModel.getSpectrumKey()).size();
                avgSearchCharge = avgSearchCharge / (double) spectraSearchEnginesData.get(spectrumModel.getSpectrumKey()).size();
                double sequenceRank = (double) searchEngines.size() / (double) sequences.size();
                spectrumModel.setSequenceRank(sequenceRank);
                spectrumModel.setAvgSequenceLength(avgSequenceLength);
                spectrumModel.setAvgSearchCharge(avgSearchCharge);
            }
            spectraMap.put(spectrumModel.getSpectrumKey(), spectrumModel);

        }
        return spectraMap;

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
                double signalToNoiseRatio = totalTopPeakIntensity / (spectrum.getTotalIntensity() - totalTopPeakIntensity);
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

    private void calculateSpectrumRank(Map<String, HashSet<String>> spectra_peptide_match, Map<String, HashSet<String>> spectraSearchEngines, int totalSearchEngNumber) {
        spectraRank = new HashMap<>();
        rankSpectra = new TreeMap<>();

        sequenceRankMap = new HashMap<>();
        rankSequence = new TreeMap<>();

        spectra_peptide_match.keySet().forEach(spectrumKey -> {

            double seFactor = ((double) spectraSearchEngines.get(spectrumKey).size() / (double) totalSearchEngNumber) * 100.0;
            double sequencesRankFactor = Math.round((100.0 / (double) spectra_peptide_match.get(spectrumKey).size()));// (100.0-seFactor)/ spectra_peptide_match.get(spectrumKey).size() ;
            double rank = Math.round((seFactor));// + sequencesFactor / 2.0

            spectraRank.put(spectrumKey, rank);
            sequenceRankMap.put(spectrumKey, sequencesRankFactor);
            if (!rankSpectra.containsKey(rank)) {
                rankSpectra.put(rank, new HashSet<>());
            }
            rankSpectra.get(rank).add(spectrumKey);

            if (!rankSequence.containsKey(sequencesRankFactor)) {
                rankSequence.put(sequencesRankFactor, new HashSet<>());
            }
            rankSequence.get(sequencesRankFactor).add(spectrumKey);
        });
        System.out.println(rankSequence.keySet());

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
}
