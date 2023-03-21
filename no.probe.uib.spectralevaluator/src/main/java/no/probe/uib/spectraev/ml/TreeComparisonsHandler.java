/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.spectraev.ml;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import no.probe.uib.mgfevaluator.model.TraningDataset;
import no.probe.uib.mgfevaluator.model.TraningTreeResult;
import no.probe.uib.mgfevaluator.processes.UpdatedDataProcessor;
import no.probe.uib.mgfevaluator.processes.handlers.DataStoreHandler;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import smile.classification.DecisionTree;
import smile.math.MathEx;
import smile.regression.RegressionTree;
import smile.validation.ClassificationValidations;
import smile.validation.CrossValidation;
import smile.validation.RegressionValidations;

/**
 *
 * @author yfa041
 */
public class TreeComparisonsHandler {

    private final UpdatedDataProcessor dataProcessor = new UpdatedDataProcessor();
    private final Set<Dataset> finalDataset = new HashSet();
    private final TreeMap<String, Double> dTreeAccuracy = new TreeMap<>();
    private final TreeMap<String, Double> rTreeAccuracy = new TreeMap<>();
    private final TreeMap<String, TraningTreeResult> TraningTreeResults = new TreeMap<>();
    private final DataStoreHandler dsHandler = new DataStoreHandler();

    public boolean clusterTraningData(Map<String, TraningDataset> trainingDataToCluster) {

        int i = 0;
        finalDataset.clear();
        TraningTreeResults.clear();
        for (String str : trainingDataToCluster.keySet()) {
            TraningTreeResults.put(str.split("__")[0], trainTree(trainingDataToCluster.get(str)));
            System.out.println("train ds "+str);

        }

        int total = 0;
        for (String str : trainingDataToCluster.keySet()) {
            String ds1Name = str.split("__")[0];
            if (ds1Name.contains("-")) {
                i++;
                continue;
            }
            int j = 0;
            finalDataset.add(trainingDataToCluster.get(str).getSourceDataset());
            for (String str2 : trainingDataToCluster.keySet()) {
                String ds2Name = str2.split("__")[0];
                if (j <= i || ds2Name.contains("-")) {
                    j++;
                    continue;
                }
                System.out.println("start comparison "+ds1Name+"  vs "+ds2Name);

                if (trainingDataToCluster.get(str).getSelectedFeaturesKey().equalsIgnoreCase(trainingDataToCluster.get(str2).getSelectedFeaturesKey())) {
                   
                    finalDataset.add(trainingDataToCluster.get(str2).getSourceDataset());
                    Dataset d = dataProcessor.mergeDatasets(finalDataset);
                    dataProcessor.reduceDatasetMeasurments(d);
                    TraningDataset t = dataProcessor.prepareDataToTrain(d);
                    t.setDatasetName(ds1Name + "-" + ds2Name);
                    TraningTreeResult result = trainTree(t);
                    TraningTreeResult ds1Result = TraningTreeResults.get(ds1Name);
                    TraningTreeResult ds2Result = TraningTreeResults.get(ds2Name);
                    if ((result.getdTreeAccurcy() >= ds1Result.getdTreeAccurcy() || result.getrTreeAccurcy() >= ds1Result.getrTreeAccurcy() || result.getrTreeR2() >= ds1Result.getrTreeR2()) || (result.getdTreeAccurcy() >= ds2Result.getdTreeAccurcy() || result.getrTreeAccurcy() >= ds2Result.getrTreeAccurcy() || result.getrTreeR2() >= ds2Result.getrTreeR2())) {
                        TraningTreeResults.put(t.getDatasetName(), result);
                        TraningTreeResults.put(t.getDatasetName(), result);
                        d.setName(t.getDatasetName() + "__Q exactive__" + LocalDateTime.now().toLocalDate());
                        dsHandler.storeDataset(d);
                        System.out.println(total + " -- storing new dataset " + ds1Name + "  " + ds1Result.getdTreeAccurcy() + "  " + ds1Result.getrTreeAccurcy() + "  " + ds1Result.getrTreeR2());
                        System.out.println(total + " -- storing new dataset " + ds2Name + "  " + ds2Result.getdTreeAccurcy() + "  " + ds2Result.getrTreeAccurcy() + "  " + ds2Result.getrTreeR2());
                        System.out.println(total + " -- storing new dataset " + d.getName() + "  " + result.getdTreeAccurcy() + "  " + result.getrTreeAccurcy() + "  " + result.getrTreeR2());
                        System.out.println("---------------------------------------------------------------------------------------");
                        total++;
                    }
                    finalDataset.remove(trainingDataToCluster.get(str2).getSourceDataset());
                }

                j++;
            }
            finalDataset.remove(trainingDataToCluster.get(str).getSourceDataset());
            i++;
        }

        for (TraningTreeResult result : TraningTreeResults.values()) {
            System.out.println("dataset " + result.getDatasetName() + "  DT " + result.getdTreeAccurcy() + "%    RT " + result.getrTreeAccurcy() + " %    " + result.getrTreeR2());

        }

        return true;

    }

    public void compareAllData(Map<Integer, Dataset> datasetMap) {
        rTreeAccuracy.clear();
        dTreeAccuracy.clear();
        int i = 0;
        for (Dataset ds1 : datasetMap.values()) {
            int j = 0;
            for (Dataset ds2 : datasetMap.values()) {
                if (j < i) {
                    j++;
                    continue;
                }
                finalDataset.add(ds1);
                finalDataset.add(ds2);

                Dataset traningDS = dataProcessor.mergeDatasets(finalDataset);
                TraningDataset traniningData = dataProcessor.prepareDataToTrain(traningDS);

                traniningData.setDatasetName(ds1.getName().split("__")[0] + "_" + ds2.getName().split("__")[0]);
                trainTree(traniningData);
                if (finalDataset.size() > 1) {
                    finalDataset.remove(ds2);
                }
                j++;
            }
            finalDataset.clear();
            i++;
        }
//        for (String dsCompName : dTreeAccuracy.keySet()) {
//            System.out.println("at  " + dsCompName + " DT: " + dTreeAccuracy.get(dsCompName) + "  ----------------- RT:  " + rTreeAccuracy.get(dsCompName));
//            System.out.println();
//        }

    }

    private TraningTreeResult trainTree(TraningDataset traniningData) {
        TraningTreeResult results = new TraningTreeResult();
        results.setDatasetName(traniningData.getDatasetName());
        results.setSourceDataset(traniningData.getSourceDataset());

        MassSpectrometryData msData = new MassSpectrometryData(traniningData, traniningData);
//        System.out.println("DecisionTree model");
        MathEx.setSeed(19650218); // to get repeatable results.
        int maxnodes = (traniningData.getColumnNames().length);
        int maxdeep = traniningData.getRowNames().length / 10;
        int nodesize = 2;//5;   
        try {
            DecisionTree decisionTree = DecisionTree.fit(msData.formula, msData.train);//, SplitRule.GINI, maxnodes, maxdeep, nodesize);
//            decisionTree.prune(msData.test);

            ClassificationValidations<DecisionTree> result = CrossValidation.classification(10, msData.formula, msData.train,
                    (f, x) -> decisionTree);

//            double[] importance = decisionTree.importance();
//            TreeMap<Double, String> decisionTreeImportancy = new TreeMap<>();
//            for (int i = 0; i < importance.length; i++) {
//                if (importance[i] > 0) {
////                    decisionTreeImportancy.put(importance[i], decisionTree.schema().name(i));
//                }
//            }
//            System.out.println("decision tree: " + result + "   " + result.avg.accuracy);
            dTreeAccuracy.put(traniningData.getDatasetName(), result.avg.accuracy);
            results.setdTreeAccurcy(result.avg.accuracy);
//            System.out.println("<<<<<<<<<<<<<<<<<<<<Regression tree>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            RegressionTree regressionTree = RegressionTree.fit(msData.formula, msData.train);//, maxnodes, maxdeep, nodesize);
            //create decionMap
            Map<Double, String> finalDecisionMap2 = new HashMap<>();
            for (int i = 0; i < msData.y_reg.length; i++) {
                if (!finalDecisionMap2.containsKey(msData.y_reg[i])) {
                    finalDecisionMap2.put(msData.y_reg[i], msData.train.getString(i, "Class"));
                }
                if (finalDecisionMap2.size() == 2) {
                    break;
                }
            }
//            double[] importance2 = regressionTree.importance();//
//            TreeMap<Double, String> regressionTreeImportancy = new TreeMap<>();
//            for (int i = 0; i < importance2.length; i++) {
//                regressionTreeImportancy.put(importance2[i], regressionTree.schema().name(i));
//            }

            double[] selfPrediction = regressionTree.predict(msData.train);

            int errorCount = 0;
            for (int i = 0; i < selfPrediction.length; i++) {
                double roundPredection = Math.round(selfPrediction[i]);
                if (roundPredection != msData.y_reg[i]) {
                    errorCount++;
                }
            }
            double acc = (double) (msData.y_reg.length - errorCount) / (double) msData.y_reg.length;
            acc = acc * 100.0;

            RegressionValidations<RegressionTree> regresult = CrossValidation.regression(10, msData.formula, msData.train, (f, x) -> regressionTree);
//             System.out.println("at self calc accracy " + (acc) + " %"+regresult);
//             System.out.println("regresult " + regresult.avg.r2);
            rTreeAccuracy.put(traniningData.getDatasetName(), regresult.avg.r2);
            results.setrTreeAccurcy(acc);
            results.setrTreeR2(regresult.avg.r2);
//            RegressionMetrics metrics = LOOCV.regression(msData.formula, msData.train, (formula, x) -> regressionTree);
//            System.out.println("regression tree 2 : " + metrics);
//
//            RegressionValidation validation = new RegressionValidation(regressionTree, msData.y_reg, selfPrediction, metrics);
//            System.out.println("validation " + validation);

//            RegressionValidations<RegressionTree> regresult = CrossValidation.(10, msData.formula, msData.train, (f, x) -> regressionTree);
//            System.out.println("dataset " + traniningData.getDatasetName().split("__")[0] + "  DT " + (result.avg.accuracy * 100) + "%    RT " + acc + " %    " + regresult.avg.r2);
        } catch (Exception e) {
            System.out.println("at error " + traniningData.getDatasetName().split("__")[0]);
            e.printStackTrace();
        }
        return results;

//        JFreeChart histogram = ChartFactory.createHistogram("Prediction", "Data", "Frequency", dataset, PlotOrientation.VERTICAL, true, true, true);
//        // add the chart to a panel...
//        ChartPanel chartPanel = new ChartPanel(histogram);
//        JFrame f = new JFrame();
//        f.setSize(500, 500);
//        f.add(chartPanel);
//        f.repaint();
//        f.setVisible(true);
//      
        /**
         * *random forest test**
         */
//        System.out.println("<<<<<<<<<<<<<<<<<<<<random forest  tree>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//
//        RandomForest model3 = RandomForest.fit(msData.formula, msData.train);
//        //create decionMap
//        Map<Double, String> finalDecisionMap3 = new HashMap<>();
//        for (int i = 0; i < msData.y_reg.length; i++) {
//            if (!finalDecisionMap3.containsKey(msData.y_reg[i])) {
//                System.out.println("at i " + i + "  " + msData.y_reg[i] + "  " + msData.y[i]);
//                finalDecisionMap3.put(msData.y_reg[i], msData.train.getString(i, "Class"));
//            }
//            if (finalDecisionMap3.size() == 2) {
//                break;
//            }
////            System.out.println(" i " + i + "  " + msData.y[i] + "   " + msData.train.getString(i, "Class"));
//        }
//        System.out.println(finalDecisionMap3.size() + "  -- at final des map " + finalDecisionMap3);
//        double[] importance3 = model3.importance();//
//        TreeMap<Double, String> regressionTreeImportancy2 = new TreeMap<>();
//        for (int i = 0; i < importance3.length; i++) {
//            regressionTreeImportancy2.put(importance3[i], model3.schema().name(i));
//        }
//
//        k = 0;
//        for (double d : regressionTreeImportancy2.keySet()) {
//            if (!existMeasurment.contains((regressionTreeImportancy2.get(d)))) {
//                System.out.println(k + " at ----------------------------------------------------------------->>>" + (regressionTreeImportancy2.get(d)) + "  --- " + ((int) d));
//                k++;
//            }
//
//        }
//        int[] prediction3 = model3.predict(msData.test);
//        errorCount = 0;
//        for (int i = 0; i < prediction2.length; i++) {
////            double roundPredection = Math.round(prediction2[i]);
//            if (prediction2[i] != msData.testy_reg[i]) {
//                errorCount++;
//            }
//        }
//        double accur3 = (double) (msData.testx.length - errorCount) / (double) msData.testx.length;
//        System.out.println("acc: " + (accur3 * 100) + "%   error 1 " + errorCount + "   " + regressionTreeImportancy2.size());
    }
}
