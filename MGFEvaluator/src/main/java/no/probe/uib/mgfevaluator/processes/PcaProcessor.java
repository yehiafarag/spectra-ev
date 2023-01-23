/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.processes;

import java.util.TreeMap;
import no.probe.uib.mgfevaluator.gui.pca.PCAImageGenerator;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.core.dataset.Group;
import no.uib.jexpress_modularized.pca.computation.PcaCompute;
import no.uib.jexpress_modularized.pca.computation.PcaResults;

/**
 *
 * @author yfa041
 */
public class PcaProcessor {  
  
    public PcaResults getPCAResults(Dataset dataset, int pcx, int pcy) {
       
        PcaCompute pcaCompute = new PcaCompute(dataset);
        no.uib.jexpress_modularized.pca.computation.PcaResults jResults = pcaCompute.createPCA();      
        return jResults;
    }

}
