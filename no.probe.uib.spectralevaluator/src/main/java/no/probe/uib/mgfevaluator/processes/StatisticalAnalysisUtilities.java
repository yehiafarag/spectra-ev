/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.processes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import no.probe.uib.mgfevaluator.gui.charts.box.BoxAndWhisker;
import no.probe.uib.mgfevaluator.gui.components.SimpleHeatMap;
import no.probe.uib.mgfevaluator.model.GroupDescriptiveStatisticsModel;
import no.probe.uib.mgfevaluator.model.ProjectCombinationMatrix;
import no.probe.uib.mgfevaluator.model.TraningDataset;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TTest;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

/**
 *
 * @author Yehia Mokhtar Farag
 */
public class StatisticalAnalysisUtilities {

    public GroupDescriptiveStatisticsModel calculateGroupDescriptiveStatistics(double[] data) {
        DescriptiveStatistics da = new DescriptiveStatistics(data);
        GroupDescriptiveStatisticsModel groupDescriptiveStatisticsModel = new GroupDescriptiveStatisticsModel();
        double median = da.getPercentile(50);

        double percentile75 = da.getPercentile(75);
        groupDescriptiveStatisticsModel.setQ3(percentile75);
        double percentile25 = da.getPercentile(25);
        groupDescriptiveStatisticsModel.setQ1(percentile25);
        double iqr = percentile75 - percentile25;
        double lowerBound = percentile25 - 1.5 * iqr; //13.23
        groupDescriptiveStatisticsModel.setLowerBond(lowerBound);
        double upperBound = percentile75 + 1.5 * iqr; //115.78  
        double[] filteredData = new double[data.length];
        groupDescriptiveStatisticsModel.setUpperBond(upperBound);
        List<Integer>outlierIndexes =new ArrayList<>();
        int i = 0;
        
        for (double d : data) {
            if (d > upperBound || d < lowerBound) {
               filteredData[i] = d;//median;
               outlierIndexes.add(i);
            } else {
                filteredData[i] = d;
            }
            i++;
        }
        groupDescriptiveStatisticsModel.setFilteredMembers(filteredData);
        groupDescriptiveStatisticsModel.setOutlierIndexes(outlierIndexes);
        return groupDescriptiveStatisticsModel;
//for(int i = 0; i < list.size(); i++) array[i] = list.get(i);
    }

    public String evaluateGroup(GroupDescriptiveStatisticsModel gdModel, double value) {
        String response;
        if (value > gdModel.getUpperBond() || value < gdModel.getLowerBond()) {
            response = RESPONSE.NO.toString() + "_" + gdModel.getGroupName();
        } else if ((value <= gdModel.getUpperBond() && value >= gdModel.getQ3()) || (value >= gdModel.getLowerBond() && value <= gdModel.getQ1())) {
            response = RESPONSE.MAYBE.toString() + "_" + gdModel.getGroupName();
        } else {
            response = RESPONSE.MOSTLY.toString() + "_" + gdModel.getGroupName();
        }
        return response;
    }

    public enum RESPONSE {
        NO("NO"), MAYBE("MAYBE"), MOSTLY("MOSTLY");
        private String responseType;

        private RESPONSE(String responseType) {
            this.responseType = responseType;
            // compiled code
        }

        @Override
        public String toString() {
            return responseType;
        }
    }

    public String selectMostlyResponse(Set<GroupDescriptiveStatisticsModel> gdModels, double value) {
        TreeMap<Double, GroupDescriptiveStatisticsModel> rankingTree = new TreeMap<>();
        for (GroupDescriptiveStatisticsModel gdModel : gdModels) {
            rankingTree.put(calculateMostlyRank(gdModel, value), gdModel);
        }

        GroupDescriptiveStatisticsModel selection = rankingTree.lastEntry().getValue();
        if (selection == null) {
            System.out.println("error in the group");
            return "EROOR";
        } else {
            return "MOSTLY_" + selection.getGroupName();
        }

    }

    public String selectMaybeResponse(Set<GroupDescriptiveStatisticsModel> gdModels, double value) {
        TreeMap<Double, GroupDescriptiveStatisticsModel> rankingTree = new TreeMap<>();
        for (GroupDescriptiveStatisticsModel gdModel : gdModels) {
            rankingTree.put(calculateMaybeRank(gdModel, value), gdModel);
        }
        GroupDescriptiveStatisticsModel selection = rankingTree.lastEntry().getValue();
        if (selection == null) {
            System.out.println("error in the group");
            return "EROOR";
        } else {
            return "MAYBE_" + selection.getGroupName();
        }

    }

    public String selectCloserToResponse(Set<GroupDescriptiveStatisticsModel> gdModels, double value) {
        TreeMap<Double, GroupDescriptiveStatisticsModel> rankingTree = new TreeMap<>();
        for (GroupDescriptiveStatisticsModel gdModel : gdModels) {
            double r = calculatecloserToRank(gdModel, value);
            if (r != -1) {
                rankingTree.put(r, gdModel);
            }
        }
        if (rankingTree.isEmpty()) {
            return "";
        }
        GroupDescriptiveStatisticsModel selection = rankingTree.firstEntry().getValue();
        if (selection == null) {
            System.out.println("error in the group");
            return "EROOR";
        } else {
            return "CLOSERTO_" + selection.getGroupName();
        }

    }

    private double calculateMostlyRank(GroupDescriptiveStatisticsModel gdModel, double value) {
        if (value >= gdModel.getQ1() && value <= gdModel.getMedian()) {
            double calc = (((gdModel.getMedian() - value) / (gdModel.getMedian() - gdModel.getQ1())) * 100.0);
            return calc;
        } else { //if( value>=gdModel.getMedian() && value<=gdModel.getQ3())
            return (((gdModel.getQ3() - value) / (gdModel.getQ3() - gdModel.getMedian())) * 100.0);
        }

    }

    private double calculateMaybeRank(GroupDescriptiveStatisticsModel gdModel, double value) {
        if (value >= gdModel.getLowerBond() && value <= gdModel.getQ1()) {
            double calc = (((gdModel.getQ1() - value) / (gdModel.getQ1() - gdModel.getLowerBond())) * 100.0);
            return calc;
        } else { //if( value>=gdModel.getMedian() && value<=gdModel.getQ3())
            return (((gdModel.getUpperBond() - value) / (gdModel.getUpperBond() - gdModel.getQ3())) * 100.0);
        }

    }

    private double calculatecloserToRank(GroupDescriptiveStatisticsModel gdModel, double value) {
        if (value > gdModel.getUpperBond()) {
            double calc = (value - gdModel.getUpperBond());
            return calc;
        } else if (value < gdModel.getLowerBond()) {
            double calc = gdModel.getLowerBond() - value;
            return calc;
        }
        return -1;

    }

    public void showBoxPlotforMeasurments(String[] colnames, Set<Dataset> datasets, String title) {

        Set<BoxAndWhisker> boxSet = new LinkedHashSet<>();
        Set<Integer> measurments = new HashSet<>(Arrays.asList(new Integer[]{0, 2, 3, 4, 8, 9, 10}));
        String[] mesurmentsIds = new String[measurments.size()];

        int mIndex = 0;
        for (int ci = 0; ci < colnames.length; ci++) {
            String measurment = colnames[ci];
            Map<String, double[]> inputData = new LinkedHashMap<>();
            int dsIndex = 1;
            for (Dataset ds : datasets) {
                double[] columnValue = new double[ds.getDataLength()];
                for (int measRow = 0; measRow < columnValue.length; measRow++) {
                    columnValue[measRow] = ds.getData()[measRow][ci];
                }
                System.out.println("dsIndex " + dsIndex + "   " + ds.getName());
                inputData.put(dsIndex + "", columnValue);
                dsIndex++;

            }
            if (measurments.contains(ci)) {
                mesurmentsIds[mIndex++] = measurment;
                BoxAndWhisker measurmentBox = this.getBoxPlot(inputData, measurment);
                boxSet.add(measurmentBox);
                compareDistribution(measurment, inputData);
            }

        }
        Thread t = new Thread(() -> {
            JPanel panContainer = new JPanel();
            JFrame j = new JFrame();
            j.setTitle(title);
            j.setContentPane(panContainer);
            int currentX = 5;
            int currentY = 5;
            int counter = 0;
            for (BoxAndWhisker boxChart : boxSet) {
                panContainer.add(boxChart);
                boxChart.setBounds(currentX, currentY, 500, 500);
                currentX = currentX + 505;
                counter++;

                if (counter == 4) {
                    currentX = 5;
                    currentY = 510;
                }

            }
            panContainer.setSize(2025, currentY + 1015);
            j.setSize(2025, currentY + 1015);
            j.setVisible(true);
            j.setResizable(false);
            j.pack();
            j.repaint();
        });
//        t.start();

    }

    public void showBoxPlotforMeasurments(Set<TraningDataset> datasets, String title) {

        Set<BoxAndWhisker> boxSet = new LinkedHashSet<>();
        String[] colnames = datasets.iterator().next().getColumnNames();
        Set<Integer> measurments = new HashSet<>(Arrays.asList(new Integer[]{0, 2, 3, 4, 8, 9, 10}));
        String[] mesurmentsIds = new String[measurments.size()];

        ProjectCombinationMatrix matrix = new ProjectCombinationMatrix();
        matrix.setColumnIds(mesurmentsIds);

        int mIndex = 0;
        for (int ci = 0; ci < colnames.length; ci++) {
            String measurment = colnames[ci];
            System.out.println("at measurments "+measurment);
            Map<String, double[]> inputData = new LinkedHashMap<>();
            int dsIndex = 1;
            for (TraningDataset ds : datasets) {
                double[] columnValue = new double[ds.getData().length - 2];
                for (int measRow = 0; measRow < columnValue.length; measRow++) {
                    columnValue[measRow] = (double) ds.getData()[measRow + 1][ci + 1];
                }
                inputData.put(dsIndex + "", columnValue);
                dsIndex++;
            }
            if (measurments.contains(ci)) {
                mesurmentsIds[mIndex] = measurment;
                BoxAndWhisker measurmentBox = this.getBoxPlot(inputData, measurment);
                boxSet.add(measurmentBox);
                Map<String, Double> doneComparison = compareDistribution(measurment, inputData);
                if (matrix.getCombiniations() == null) {
                    matrix.setCombiniations(doneComparison.keySet().toArray(new String[]{}));
                    double[][] data = new double[matrix.getCombiniations().length][mesurmentsIds.length];
                    matrix.setData(data);
                }
                double largest = Double.MIN_VALUE;
                for (String comb : doneComparison.keySet()) {
                    double d = doneComparison.get(comb);
                    largest = Math.max(d, largest);
                }
                int cobIndex = 0;
                for (String comb : doneComparison.keySet()) {
                    matrix.getData()[cobIndex++][mIndex] = doneComparison.get(comb);
                }
                mIndex++;
            }

        }
        SimpleHeatMap hm = new SimpleHeatMap();
        hm.setData(matrix.getColumnIds(), matrix.getCombiniations(), matrix.getData());
        Thread t = new Thread(() -> {
            JPanel panContainer = new JPanel();
            panContainer.setLayout(null);
            JFrame j = new JFrame();
            j.setTitle(title);
            j.setContentPane(panContainer);
            int currentX = 5;
            int currentY = 5;
            panContainer.add(hm);
            hm.setBounds(currentX, currentY, hm.getWidth(), hm.getHeight());
            currentY += hm.getHeight() + 5;
//            hm.setBackground(Color.WHITE);

            int counter = 0;
            for (BoxAndWhisker boxChart : boxSet) {
                panContainer.add(boxChart);
                boxChart.setBounds(currentX, currentY, 500, 500);
                currentX = currentX + 505;
                counter++;

                if (counter == 4) {
                    currentX = 5;
                    currentY = 5 + hm.getHeight() + 10 + 510;
                }

            }
            System.out.println("at current x " + currentX + "  " + currentY + "  ");
            panContainer.setSize(2025, currentY + 510);
//            j.setSize(500, 500);
            j.setVisible(true);
            j.setResizable(true);
//            j.pack();
//            j.repaint();
        });
        t.start();

    }

    public void showHistogramPlotforMeasurments(Set<TraningDataset> datasets, String title) {

        Set<ChartPanel> histgramChartsSet = new LinkedHashSet<>();
        String[] colnames = datasets.iterator().next().getColumnNames();
        Set<Integer> measurments = new HashSet<>(Arrays.asList(new Integer[]{0, 2, 3, 4, 8, 9, 10}));
        int mIndex = 0;
        for (int ci = 0; ci < colnames.length; ci++) {
            String measurment = colnames[ci];
            HistogramDataset dataset = new HistogramDataset();
            Map<String, double[]> inputData = new LinkedHashMap<>();
            int dsIndex = 1;
            for (TraningDataset ds : datasets) {;
                double[] columnValue = new double[ds.getData().length - 2];
                for (int measRow = 0; measRow < columnValue.length; measRow++) {
                    columnValue[measRow] = (double) ds.getData()[measRow + 1][ci + 1];
                }
                System.out.println("dsIndex " + dsIndex + "   " + ds.getDatasetName());
                inputData.put(dsIndex + "", columnValue);
                dataset.addSeries(dsIndex + "", columnValue, 100);
                dsIndex++;
            }
            if (measurments.contains(ci)) {
                histgramChartsSet.add(new ChartPanel(ChartFactory.createHistogram(measurment, "Dataset_index", "Frequency", dataset, PlotOrientation.VERTICAL, false, true, false)));
                mIndex++;
            }

        }

        Thread t = new Thread(() -> {
            JPanel panContainer = new JPanel();
            panContainer.setLayout(null);
            JFrame j = new JFrame();
            j.setTitle(title);
            j.setContentPane(panContainer);
            int currentX = 5;
            int currentY = 5;
            int counter = 0;
            for (ChartPanel boxChart : histgramChartsSet) {
                panContainer.add(boxChart);
                boxChart.setBounds(currentX, currentY, 500, 500);
                currentX = currentX + 505;
                counter++;

                if (counter == 4) {
                    currentX = 5;
                    currentY = 5 + 510;
                }

            }
            panContainer.setSize(2025, currentY + 510);
//            j.setSize(500, 500);
            j.setVisible(true);
            j.setResizable(true);
//            j.pack();
//            j.repaint();
        });
        t.start();

    }

    private Map<String, Double> compareDistribution(String measurment, Map<String, double[]> inputData) {
        Map<String, Double> doneComparison = new LinkedHashMap<>();
        TreeSet<String> nameCombine = new TreeSet<>();
        for (String dsName : inputData.keySet()) {
            nameCombine.clear();
            nameCombine.add(dsName);
            double[] dataI = inputData.get(dsName);
            for (String dsName2 : inputData.keySet()) {
                nameCombine.add(dsName2);
                if (dsName.equals(dsName2)) {
                    continue;
                } else if (doneComparison.containsKey(nameCombine.toString())) {
                    nameCombine.remove(dsName2);
                    continue;
                }
                double[] dataII = inputData.get(dsName2);
                doneComparison.put(nameCombine.toString(), performT_test(dataI, dataII));
                nameCombine.remove(dsName2);
            }
        }
        for (String comparison : doneComparison.keySet()) {
            System.out.println(comparison + "   test  " + doneComparison.get(comparison));
        }
        System.out.println();
        return doneComparison;
    }

    private double performT_test(double[] dataI, double[] dataII) {
//        if (TestUtils.tTest(dataI, dataII, 0.05)) {
        TTest TTest = new TTest();
//        DescriptiveStatistics sm = new DescriptiveStatistics(dataI);
//        DescriptiveStatistics sm2 = new DescriptiveStatistics(dataII);
        if (TTest.tTest(dataI, dataII, 0.05)) {
            return 0;
        }
        return 1;
//        } else {
//            return Double.MAX_VALUE;
//        }
//         return result.tTest(dataI, dataII)+"";

//       // TTest result = TTest.test(dataI, dataII, true);
//        if (result.pvalue < 0.05 && result.t > 0) {
//            return "Significant diffrence --"+result.toString();
//        }
//        if (result.pvalue < 0.05 && result.t <= 0) {
//            return "Significant similer --"+result.toString();
//        }
////         System.out.println("t test results "+result.pvalue+"  "+result.t);
//        return "Not significant --"+result.toString();
//        return result.toString();
    }

    private BoxAndWhisker getBoxPlot(Map<String, double[]> inputData, final String title) {
        BoxAndWhisker plot = new BoxAndWhisker(inputData, title);
        return plot;
    }

}
