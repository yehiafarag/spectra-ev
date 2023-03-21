package no.probe.uib.mgfevaluator.processes.handlers;

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
import no.probe.uib.mgfevaluator.model.PeptideModel;
import no.probe.uib.mgfevaluator.model.SpectrumModel;

/**
 *
 * @author yfa041
 */
public class PSExportFileHandler {

    private final File dataFile;
    private String dataFolderUrl;
    private Map<String, Double> spectraRank;
    private TreeMap<Double, HashSet<String>> rankSpectra;
    private Map<String, HashSet<String>> spectraPeptideMatch;
    private Map<String, HashSet<String>> spectraSearchEngines;

    private Map<String, Double> sequenceRankMap;
    private TreeMap<Double, HashSet<String>> rankSequence;

    private final List<PeptideModel> peptidesList;
    private final Set<String> searchEngines;
    private Map<String, HashMap<String, PeptideModel>> spectraSearchEnginesData;
    private final Map<String, Set<PeptideModel>> spectraPeptidesMap;

    private double avgSequenceLength;
    private double avgSearchCharge;

    public PSExportFileHandler(File dataFile) {
        this.dataFile = dataFile;
        this.peptidesList = new ArrayList<>();
        this.searchEngines = new HashSet<>();
        this.spectraPeptidesMap = new HashMap<>();
    }

    public Set<String> getSearchEngines() {
        return searchEngines;
    }

    public void processPSExportFile(String fileName) {
        try {

            spectraSearchEnginesData = new HashMap<>();
            dataFolderUrl = dataFile.getParent();
            FileReader input = new FileReader(dataFile);
            peptidesList.clear();
            searchEngines.clear();

            spectraPeptideMatch = new HashMap<>();
            spectraSearchEngines = new HashMap<>();
            spectraPeptidesMap.clear();
            String line = "empty";
            try (BufferedReader reader = new BufferedReader(input)) {
                //escape header
                line = reader.readLine();
                String[] headers = line.split("\\t");
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
                    peptide.setSpectrumFile(fileName);
                    peptide.setSpectrumTitle(values[9]);
                    peptide.setRT(Double.parseDouble(values[11]));
                    peptide.setM_z(Double.parseDouble(values[12]));
                    if (!values[13].trim().equalsIgnoreCase("")) {
                        peptide.setMeasuredCharge(Integer.parseInt(values[13].replace("+", "")));
                    }
                    peptide.setIdentificationCharge(Integer.parseInt(values[15]));
                    String searchEngine = values[16].replace(" (", "_").split("_")[0].trim();
                    searchEngines.add(searchEngine);

                    peptide.setAlgorithmConfidence(Double.parseDouble(values[18]));
                    peptide.setValidation(true);
                    peptidesList.add(peptide);
                    if (!spectraPeptideMatch.containsKey(peptide.getSpectrumKey())) {
                        spectraPeptideMatch.put(peptide.getSpectrumKey(), new HashSet<>());
                    }
                    spectraPeptideMatch.get(peptide.getSpectrumKey()).add(peptide.getSequence());
                    if (!spectraSearchEngines.containsKey(peptide.getSpectrumKey())) {
                        spectraSearchEngines.put(peptide.getSpectrumKey(), new HashSet<>());
                    }
                    spectraSearchEngines.get(peptide.getSpectrumKey()).add(peptide.getSearchEngine());
                    peptide.setSearchEngine(searchEngine);

                    if (!spectraSearchEnginesData.containsKey(peptide.getSpectrumKey())) {
                        spectraSearchEnginesData.put(peptide.getSpectrumKey(), new HashMap<>());
                    }
                    spectraSearchEnginesData.get(peptide.getSpectrumKey()).put(peptide.getSearchEngine(), peptide);

                    if (!spectraPeptidesMap.containsKey(peptide.getSpectrumKey())) {
                        spectraPeptidesMap.put(peptide.getSpectrumKey(), new HashSet<>());
                    }
                    spectraPeptidesMap.get(peptide.getSpectrumKey()).add(peptide);
                }
            } catch (FileNotFoundException ex) {
                System.err.println(PSExportFileHandler.class.getName() + " -- " + ex);
            } catch (IOException ex) {
                System.err.println(PSExportFileHandler.class.getName() + " -- " + ex);
            } catch (NumberFormatException ex) {
                System.out.println("exc here " + line);
                int i = 0;
                for (String str : line.split("\t")) {
                    System.out.println("at index " + i + "  " + str);
                    i++;
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
//
//    public Map<String, SpectrumModel> calculateSpectrumData(Set<String> searchEngines, CMSFileHandler csmFileHandler) {
//        String[] spectrumTitles = csmFileHandler.getCsmFileReader().getSpectrumTitles("");
//        String fileName = csmFileHandler.getSpectrumFileName();
//        Map<String, SpectrumModel> spectraMap = new HashMap<>();
//        for (String spectrumTitle : spectrumTitles) {
//            Spectrum spectrum = csmFileHandler.getCsmFileReader().getSpectrum(spectrumTitle);
//            SpectrumModel spectrumModel = new SpectrumModel();
//            spectrumModel.setSpectrumKey(fileName + "_" + spectrumTitle);
//            spectrumModel.setSpectrumTitle(spectrumTitle);
//            spectrumModel.setFile(fileName);
//            spectrumModel.setTheoreticalCharge(spectrum.getPrecursor().getPossibleChargesAsString());
//            spectrumModel.setTheoreticalMass(spectrum.getPrecursor().mz);
//            spectrumModel.setTotalIntensity(spectrum.getTotalIntensity());
//            spectrumModel.setNumPeaks(spectrum.getNPeaks());
//            calculateSpectrumPeakListOnIntensityLevels(spectrumModel, spectrum);
//            if (spectraSearchEnginesData.containsKey(spectrumModel.getSpectrumKey())) {
//                spectrumModel.setIdentified(true);
//                double searchEngineRank = (double) spectraSearchEnginesData.get(spectrumModel.getSpectrumKey()).size() * 100 / (double) searchEngines.size();
//                spectrumModel.setSearchEngineRank((int) searchEngineRank);
//                //calculate sequence rank 
//                Set<String> sequences = new HashSet<>();
//                avgSequenceLength = 0;
//                avgSearchCharge = 0;
//                spectraSearchEnginesData.get(spectrumModel.getSpectrumKey()).values().forEach(peptide -> {
//                    sequences.add(peptide.getSequence());
//                    avgSequenceLength += (double) peptide.getSequence().length();
//                    avgSearchCharge += (double) peptide.getIdentificationCharge();
//                });
//                avgSequenceLength = avgSequenceLength / (double) spectraSearchEnginesData.get(spectrumModel.getSpectrumKey()).size();
//                avgSearchCharge = avgSearchCharge / (double) spectraSearchEnginesData.get(spectrumModel.getSpectrumKey()).size();
//                double sequenceRank = (double) searchEngines.size() / (double) sequences.size();
//                spectrumModel.setSequenceRank(sequenceRank);
//                spectrumModel.setAvgSequenceLength(avgSequenceLength);
//                spectrumModel.setAvgSearchCharge(avgSearchCharge);
//            }
//            spectraMap.put(spectrumModel.getSpectrumKey(), spectrumModel);
//
//        }
//        return spectraMap;
//
//    }

    public void updateSpectrumIdentificationInformation(SpectrumModel spectrumModel) {

        String key = spectrumModel.getSpectrumKey();
        if (!spectraSearchEngines.containsKey(key)) {
            spectrumModel.setIdentified(false);
            spectrumModel.setSearchEngineRank(0);
            spectrumModel.setSequenceRank(0);
            spectrumModel.setValidation("Not valid");
            return;
        }

        Set<String> spectrumSearchEngine = spectraSearchEngines.get(key);
        spectrumModel.setSearchEnginesList(spectrumSearchEngine);
        double seRank = ((double) spectrumSearchEngine.size() / (double) searchEngines.size()) * 100.0;
        spectrumModel.setSearchEngineRank((int) seRank);

        Set<String> sequences = spectraPeptideMatch.get(key);
        double x = (double) searchEngines.size() - (double) sequences.size() + 1;
        double sequenceRank = (x * 100.0) / (double) searchEngines.size();
        spectrumModel.setSequenceRank(sequenceRank);

        spectrumModel.setPeptides(spectraPeptidesMap.get(key));

    }
//
//    private void calculateSpectrumRank(Map<String, HashSet<String>> spectra_peptide_match, Map<String, HashSet<String>> spectraSearchEngines, int totalSearchEngNumber) {
//        spectraRank = new HashMap<>();
//        rankSpectra = new TreeMap<>();
//
//        sequenceRankMap = new HashMap<>();
//        rankSequence = new TreeMap<>();
//
//        spectra_peptide_match.keySet().forEach(spectrumKey -> {
//
//            double seFactor = ((double) spectraSearchEngines.get(spectrumKey).size() / (double) totalSearchEngNumber) * 100.0;
//            double sequencesRankFactor = Math.round((100.0 / (double) spectra_peptide_match.get(spectrumKey).size()));// (100.0-seFactor)/ spectra_peptide_match.get(spectrumKey).size() ;
//            double rank = Math.round((seFactor));// + sequencesFactor / 2.0
//
//            spectraRank.put(spectrumKey, rank);
//            sequenceRankMap.put(spectrumKey, sequencesRankFactor);
//            if (!rankSpectra.containsKey(rank)) {
//                rankSpectra.put(rank, new HashSet<>());
//            }
//            rankSpectra.get(rank).add(spectrumKey);
//
//            if (!rankSequence.containsKey(sequencesRankFactor)) {
//                rankSequence.put(sequencesRankFactor, new HashSet<>());
//            }
//            rankSequence.get(sequencesRankFactor).add(spectrumKey);
//        });
//        System.out.println(rankSequence.keySet());
//
//    }
}
