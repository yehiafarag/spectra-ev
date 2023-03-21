/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.model;

import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;

/**
 *
 * @author yfa041
 */
public class ExtendedPeptideAssumption extends PeptideAssumption{
    boolean valid;

    public boolean isValid() {
        return valid;
    }

    public ExtendedPeptideAssumption(Peptide peptide, int rank, int advocate, int identificationCharge, double rawScore, double score, String identificationFile,boolean valid) {
        super(peptide, rank, advocate, identificationCharge, rawScore, score, identificationFile);
        this.valid=valid;
    }

    public ExtendedPeptideAssumption(Peptide peptide, int rank, int advocate, int identificationCharge, double rawScore, double score,boolean valid) {
        super(peptide, rank, advocate, identificationCharge, rawScore, score);
         this.valid=valid;
    }

    public ExtendedPeptideAssumption(Peptide peptide, int identificationCharge,boolean valid) {
        super(peptide, identificationCharge);
    }

 
    
}
