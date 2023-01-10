package no.probe.uib.mgfevaluator.model;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class represent spectrum object where the spectrum data is stored
 *
 * @author Yehia Mokhtar Farag
 */
public class SpectrumModel implements Comparable<SpectrumModel> {

    @Override
    public int compareTo(SpectrumModel o) {
        if (algorithm.equalsIgnoreCase(o.getAlgorithm()) && searchEngineRank > o.getSearchEngineRank()) {
            return -1;
        } else if (!algorithm.equalsIgnoreCase(o.getAlgorithm())) {
            if (index < o.getIndex()) {
                return -1;
            }
        }
        return 1;
    }
    private int searchEngineRank;
    private String sequence;
    private String modifiedSequence;
    private String variableModifications;
    private String fixedModifications;
    private String spectrumTitle;
    private String identificationCharge;
    private double algorithmScore;
    private double algorithmRawScore;
    private double algorithmConfidence;
    private String validation;
    private int index;
    private String algorithm;
    private double m_z;
    private String theoreticalCharge;
    private double theoreticalMass;
    private int numTopPeaks;
    private int numPeaks;
    private double totalIntensity;
    private double topIntensity;
    private double signalToNoiseEstimation_total;
    private String quality;
    private String file;
    private String spectrumKey;
    private double dynamicRange;
    private double avgDynamicRange;
    private double maxIntensity;
    private boolean identified =false;
    private double avgSequenceLength;
     private double avgSearchCharge;

    public double getAvgSearchCharge() {
        return avgSearchCharge;
    }

    public void setAvgSearchCharge(double avgSearchCharge) {
        this.avgSearchCharge = avgSearchCharge;
    }

    public double getAvgSequenceLength() {
        return avgSequenceLength;
    }

    public void setAvgSequenceLength(double avgSequenceLength) {
        this.avgSequenceLength = avgSequenceLength;
    }

    public boolean isIdentified() {
        return identified;
    }

    public void setIdentified(boolean identified) {
        this.identified = identified;
    }
    private double sequenceRank;

    public double getSequenceRank() {
        return sequenceRank;
    }

    public void setSequenceRank(double sequenceRank) {
        this.sequenceRank = sequenceRank;
    }

    public double getMaxIntensity() {
        return maxIntensity;
    }

    public void setMaxIntensity(double maxIntensity) {
        this.maxIntensity = maxIntensity;
    }
    /**
     * Map of IntensityLevel to topPeaksNumber, topPeaksIntensity,signalToNoiseRatio
     */
    private final Map<Double, Double[]> spectrumPeaksLevels = new TreeMap<>();

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public double getSignalToNoiseEstimation_total() {
        return signalToNoiseEstimation_total;
    }

    public void setSignalToNoiseEstimation_total(double signalToNoiseEstimation_total) {
        this.signalToNoiseEstimation_total = signalToNoiseEstimation_total;
    }

    public double getSignalToNoiseEstimation_avg() {
        return signalToNoiseEstimation_avg;
    }

    public void setSignalToNoiseEstimation_avg(double signalToNoiseEstimation_avg) {
        this.signalToNoiseEstimation_avg = signalToNoiseEstimation_avg;
    }
    private double signalToNoiseEstimation_avg;

    public double getTotalIntensity() {
        return totalIntensity;
    }

    public void setTotalIntensity(double totalIntensity) {
        this.totalIntensity = totalIntensity;
    }

    public int getNumTopPeaks() {
        return numTopPeaks;
    }

    public void setNumTopPeaks(int numTopPeaks) {
        this.numTopPeaks = numTopPeaks;
    }

    public int getNumPeaks() {
        return numPeaks;
    }

    public void setNumPeaks(int numPeaks) {
        this.numPeaks = numPeaks;
    }

    public double getM_z() {
        return m_z;
    }

    public void setM_z(double m_z) {
        this.m_z = m_z;
    }

    public String getTheoreticalCharge() {
        return theoreticalCharge;
    }

    public void setTheoreticalCharge(String theoreticalCharge) {
        this.theoreticalCharge = theoreticalCharge;
    }

    public double getTheoreticalMass() {
        return theoreticalMass;
    }

    public void setTheoreticalMass(double theoreticalMass) {
        this.theoreticalMass = theoreticalMass;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSearchEngineRank() {
        return searchEngineRank;
    }

    public void setSearchEngineRank(int searchEngineRank) {
        this.searchEngineRank = searchEngineRank;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getModifiedSequence() {
        return modifiedSequence;
    }

    public void setModifiedSequence(String modifiedSequence) {
        this.modifiedSequence = modifiedSequence;
    }

    public String getVariableModifications() {
        return variableModifications;
    }

    public void setVariableModifications(String variableModifications) {
        this.variableModifications = variableModifications;
    }

    public String getFixedModifications() {
        return fixedModifications;
    }

    public void setFixedModifications(String fixedModifications) {
        this.fixedModifications = fixedModifications;
    }

    public String getSpectrumTitle() {
        return spectrumTitle;
    }

    public void setSpectrumTitle(String spectrumTitle) {
        this.spectrumTitle = spectrumTitle;
    }

    public String getIdentificationCharge() {
        return identificationCharge;
    }

    public void setIdentificationCharge(String identificationCharge) {
        this.identificationCharge = identificationCharge;
    }

    public double getAlgorithmScore() {
        return algorithmScore;
    }

    public void setAlgorithmScore(double algorithmScore) {
        this.algorithmScore = algorithmScore;
    }

    public double getAlgorithmRawScore() {
        return algorithmRawScore;
    }

    public void setAlgorithmRawScore(double algorithmRawScore) {
        this.algorithmRawScore = algorithmRawScore;
    }

    public double getAlgorithmConfidence() {
        return algorithmConfidence;
    }

    public void setAlgorithmConfidence(double algorithmConfidence) {
        this.algorithmConfidence = algorithmConfidence;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public double getTopIntensity() {
        return topIntensity;
    }

    public void setTopIntensity(double topIntensity) {
        this.topIntensity = topIntensity;
    }

    public Map<Double, Double[]> getSpectrumPeaksLevels() {
        return spectrumPeaksLevels;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getSpectrumKey() {
        return spectrumKey;
    }

    public void setSpectrumKey(String spectrumKey) {
        this.spectrumKey = spectrumKey;
    }

    public double getDynamicRange() {
        return dynamicRange;
    }

    public void setDynamicRange(double dynamicRange) {
        this.dynamicRange = dynamicRange;
    }

    public double getAvgDynamicRange() {
        return avgDynamicRange;
    }

    public void setAvgDynamicRange(double avgDynamicRange) {
        this.avgDynamicRange = avgDynamicRange;
    }
}
