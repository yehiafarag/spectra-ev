/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.processes;

import java.util.LinkedHashSet;
import java.util.TreeMap;
import no.probe.uib.mgfevaluator.model.GroupDescriptiveStatisticsModel;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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
        int i = 0;
        for (double d : data) {
            if (d > upperBound || d < lowerBound) {
                filteredData[i] = median;
            } else {
                filteredData[i] = d;
            }
            i++;
        }
        groupDescriptiveStatisticsModel.setFilteredMembers(filteredData);
        return groupDescriptiveStatisticsModel;
//for(int i = 0; i < list.size(); i++) array[i] = list.get(i);
    }

    public String evaluateGroup(GroupDescriptiveStatisticsModel gdModel, double value) {
        if (value > gdModel.getUpperBond() || value < gdModel.getLowerBond()) {
            return RESPONSE.NO.toString() + "_" + gdModel.getGroupName();
        } else if ((value <= gdModel.getUpperBond() && value >= gdModel.getQ3()) || (value >= gdModel.getLowerBond() && value <= gdModel.getQ1())) {
            return RESPONSE.MAYBE.toString() + "_" + gdModel.getGroupName();
        } else {
            return RESPONSE.MOSTLY.toString() + "_" + gdModel.getGroupName();
        }
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

    public String selectMostlyResponse(LinkedHashSet<GroupDescriptiveStatisticsModel> gdModels, double value) {
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

    public String selectMaybeResponse(LinkedHashSet<GroupDescriptiveStatisticsModel> gdModels, double value) {
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
}
