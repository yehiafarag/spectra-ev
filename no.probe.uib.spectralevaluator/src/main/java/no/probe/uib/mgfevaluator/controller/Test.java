/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.probe.uib.mgfevaluator.model.DatasetInfo;
import no.probe.uib.mgfevaluator.processes.UpdatedDataProcessor;
import no.probe.uib.mgfevaluator.processes.handlers.DataStoreHandler;

/**
 *
 * @author yfa041
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        DataStoreHandler dsh = new DataStoreHandler();
        UpdatedDataProcessor updatedDataProcessor = new UpdatedDataProcessor();
        System.out.println("start storing data :-D ");
        Set<DatasetInfo> dsSet = dsh.loadDatasetInformation();
        Map<DatasetInfo, DatasetInfo[]> clusterNodesMap = new HashMap<>();
        Map<String, DatasetInfo> accObjectMap = new HashMap<>();
        Set<String> toremoveAcc = new HashSet<>();
        for (DatasetInfo ds : dsSet) {
            accObjectMap.put(ds.getAccession(), ds);
        }
        Map<String, HashSet<String>> toReplaceDs = new HashMap<>();
        for (String acc : accObjectMap.keySet()) {
            if (acc.contains("-")) {
                String ds1 = acc.split("-")[0];
                String ds2 = acc.split("-")[1];
//                clusterNodesMap.put(accObjectMap.get(acc), new DatasetInfo[]{accObjectMap.get(ds1),accObjectMap.get(ds2)});      
                if (accObjectMap.get(acc).getDt_acc() > accObjectMap.get(ds1).getDt_acc()) {
                    if (!toReplaceDs.containsKey(ds1)) {
                        toReplaceDs.put(ds1, new HashSet<>());
                    }
                    toReplaceDs.get(ds1).add(acc);
                    toremoveAcc.add(ds1);
                } else if (accObjectMap.get(acc).getDt_acc() > accObjectMap.get(ds2).getDt_acc()) {
                    if (!toReplaceDs.containsKey(ds2)) {
                        toReplaceDs.put(ds2, new HashSet<>());
                    }
                    toReplaceDs.get(ds2).add(acc);
                    toremoveAcc.add(ds2);
                } else {
                    toremoveAcc.add(acc);
                }
            }
            
        }
        
        for (String replaceAcc : toReplaceDs.keySet()) {
            double acc = accObjectMap.get(replaceAcc).getDt_acc();
            double topAcc = Double.MIN_VALUE;
            String selected = "";
            for (String replacment : toReplaceDs.get(replaceAcc)) {
                double acc2 = accObjectMap.get(replacment).getDt_acc();
                if (acc2 > topAcc) {
                    topAcc = acc2;
                    selected = replacment;
                }
            }
            
            if (!selected.isBlank()) {
                for (String replacment : toReplaceDs.get(replaceAcc)) {
                    if (!replacment.equalsIgnoreCase(selected)) {
                        toremoveAcc.add(replacment);
                    }
                }
                toremoveAcc.add(replaceAcc);
            }
            
        }
        System.out.println("to remove " + toremoveAcc.size() + " --> " + accObjectMap.size() + "   " + accObjectMap.keySet().containsAll(toremoveAcc));
        //add flag for unused datafiles
        dsh.flagUnusedDatasets(toremoveAcc);
        
    }
    
}
