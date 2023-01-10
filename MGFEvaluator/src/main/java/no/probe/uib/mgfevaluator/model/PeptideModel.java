
package no.probe.uib.mgfevaluator.model;

/**
 *
 * @author yfa041
 */
public class PeptideModel {
    private int index, rank,measuredCharge,identificationCharge,sequenceLength;

    public int getSequenceLength() {
        return sequenceLength;
    }

    public void setSequenceLength(int sequenceLength) {
        this.sequenceLength = sequenceLength;
    }
    private boolean validation,decoy;
    private double algorithmConfidence,m_z,RT;
    private String searchEngine,sequence,modifiedSequence,variableModification,fixedModification,spectrumFile,spectrumTitle;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getMeasuredCharge() {
        return measuredCharge;
    }

    public void setMeasuredCharge(int measuredCharge) {
        this.measuredCharge = measuredCharge;
    }

    public int getIdentificationCharge() {
        return identificationCharge;
    }

    public void setIdentificationCharge(int identificationCharge) {
        this.identificationCharge = identificationCharge;
    }

    public boolean isValidation() {
        return validation;
    }

    public void setValidation(boolean validation) {
        this.validation = validation;
    }

    public boolean isDecoy() {
        return decoy;
    }

    public void setDecoy(boolean decoy) {
        this.decoy = decoy;
    }

    public double getAlgorithmConfidence() {
        return algorithmConfidence;
    }

    public void setAlgorithmConfidence(double algorithmConfidence) {
        this.algorithmConfidence = algorithmConfidence;
    }

    public double getM_z() {
        return m_z;
    }

    public void setM_z(double m_z) {
        this.m_z = m_z;
    }

    public double getRT() {
        return RT;
    }

    public void setRT(double RT) {
        this.RT = RT;
    }

    public String getSearchEngine() {
        return searchEngine;
    }

    public void setSearchEngine(String searchEngine) {
        this.searchEngine = searchEngine;
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

    public String getVariableModification() {
        return variableModification;
    }

    public void setVariableModification(String variableModification) {
        this.variableModification = variableModification;
    }

    public String getFixedModification() {
        return fixedModification;
    }

    public void setFixedModification(String fixedModification) {
        this.fixedModification = fixedModification;
    }

    public String getSpectrumFile() {
        return spectrumFile;
    }

    public void setSpectrumFile(String spectrumFile) {
        this.spectrumFile = spectrumFile;
    }

    public String getSpectrumTitle() {
        return spectrumTitle;
    }

    public void setSpectrumTitle(String spectrumTitle) {
        this.spectrumTitle = spectrumTitle;
    }
    public String getSpectrumKey(){
        return (this.spectrumFile+"_"+this.spectrumTitle);
    }
    
}
