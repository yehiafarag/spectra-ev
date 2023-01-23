/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.gui.charts.barchart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import no.probe.uib.mgfevaluator.model.SpectraGroup;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.util.Log;
import org.jfree.util.LogContext;

/**
 *
 * @author yfa041
 */
public class BoxAndWhisker extends JPanel {

    /**
     * Access to logging facilities.
     */
    private static final LogContext LOGGER = Log.createContext(BoxAndWhisker.class);
    private JFreeChart chart;

//    public BoxAndWhisker(Dataset dataset, final String title, int measurmentIndex) {
//
//        BoxAndWhisker.this.setPreferredSize(new Dimension(500, 500));
//        BoxAndWhisker.this.setVisible(true);
//        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
//        renderer.setFillBox(false);
//        renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
//        final BoxAndWhiskerCategoryDataset jfreedataset = createDataset(dataset, renderer, measurmentIndex, title);
//
//        final CategoryAxis xAxis = new CategoryAxis("Group");
//        final NumberAxis yAxis = new NumberAxis("Value");
//        yAxis.setAutoRangeIncludesZero(false);
//
//        final CategoryPlot plot = new CategoryPlot(jfreedataset, xAxis, yAxis, renderer);
//
//        final JFreeChart chart = new JFreeChart(
//                title,
//                new Font("SansSerif", Font.BOLD, 14),
//                plot,
//                true
//        );
//        final ChartPanel chartPanel = new ChartPanel(chart);
//        chartPanel.setPreferredSize(new java.awt.Dimension(500, 500));
//        chartPanel.setBorder(new LineBorder(Color.BLUE, 1));
//        BoxAndWhisker.this.add(chartPanel);
//    }

    public BoxAndWhisker(Dataset dataset, final String title, int measurmentIndex, boolean filtered) {

        BoxAndWhisker.this.setPreferredSize(new Dimension(500, 500));
        BoxAndWhisker.this.setVisible(true);
        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setFillBox(false);
        renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        final BoxAndWhiskerCategoryDataset jfreedataset = createDataset(dataset, renderer, measurmentIndex, title, filtered);

        final CategoryAxis xAxis = new CategoryAxis("Group");
        final NumberAxis yAxis = new NumberAxis("Value");
        yAxis.setAutoRangeIncludesZero(false);

        final CategoryPlot plot = new CategoryPlot(jfreedataset, xAxis, yAxis, renderer);

        final JFreeChart chart = new JFreeChart(
                title,
                new Font("SansSerif", Font.BOLD, 14),
                plot,
                true
        );
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 500));
        chartPanel.setBorder(new LineBorder(Color.BLUE, 1));
        BoxAndWhisker.this.add(chartPanel);
    }

    /**
     * Returns a sample dataset.
     *
     * @return The dataset.
     */
//    private BoxAndWhiskerCategoryDataset createDataset(Dataset inputDataset, BoxAndWhiskerRenderer renderer, int measurmentIndex, String title) {
//        final int seriesCount = inputDataset.getRowGroups().size() - 1;
//        final int categoryCount = 1;
//        final DefaultBoxAndWhiskerCategoryDataset dataset
//                = new DefaultBoxAndWhiskerCategoryDataset();
//        for (int i = 0; i < seriesCount; i++) {
//            for (int j = 0; j < categoryCount; j++) {
//                final List list = new ArrayList();
//                int[] index = inputDataset.getRowGroups().get(i).getMembers();
//                renderer.setSeriesPaint(i, inputDataset.getRowGroups().get(i).getColor());
//                // add some values...
//                for (int k = 0; k < index.length; k++) {
//                    final double value1 = inputDataset.getData()[index[k]][measurmentIndex];
//                    list.add(value1);
//                }
//                dataset.add(list, inputDataset.getRowGroups().get(i).getName(), title);
//            }
//
//        }
//
//        return dataset;
//
//    }

    /**
     * Returns a sample dataset.
     *
     * @return The dataset.
     */
    private BoxAndWhiskerCategoryDataset createDataset(Dataset inputDataset, BoxAndWhiskerRenderer renderer, int measurmentIndex, String title, boolean filtered) {
        final int seriesCount = inputDataset.getRowGroups().size() - 1;
        final int categoryCount = 1;
        final DefaultBoxAndWhiskerCategoryDataset dataset
                = new DefaultBoxAndWhiskerCategoryDataset();
        for (int i = 0; i < seriesCount; i++) {
            for (int j = 0; j < categoryCount; j++) {
                final List list = new ArrayList();
                SpectraGroup group = (SpectraGroup) inputDataset.getRowGroups().get(i);
                renderer.setSeriesPaint(i, group.getColor());
                double[][] values;
                if (filtered){                  
                    values = group.getFilteredMeasurments();
                } else {
                    values = group.getMeasurments();
                }
                // add some values...
                for (int k = 0; k < values.length; k++) {
                    final double value1 = values[k][measurmentIndex];
                    list.add(value1);
                }
                dataset.add(list, inputDataset.getRowGroups().get(i).getName(), title);
            }

        }

        return dataset;

    }

    /**
     * Creates a sample chart.
     *
     * @param dataset the dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart(final CategoryDataset dataset) {

        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
                "Bar Chart Demo", // chart title
                "Category", // domain axis label
                "Value", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips?
                false // URLs?
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // disable bar outlines...
        final BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);

        // set up gradient paints for series...
        final GradientPaint gp0 = new GradientPaint(
                0.0f, 0.0f, Color.blue,
                0.0f, 0.0f, Color.lightGray
        );
        final GradientPaint gp1 = new GradientPaint(
                0.0f, 0.0f, Color.green,
                0.0f, 0.0f, Color.lightGray
        );
        final GradientPaint gp2 = new GradientPaint(
                0.0f, 0.0f, Color.red,
                0.0f, 0.0f, Color.lightGray
        );
        renderer.setSeriesPaint(0, gp0);
        renderer.setSeriesPaint(1, gp1);
        renderer.setSeriesPaint(2, gp2);

        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
        );
        // OPTIONAL CUSTOMISATION COMPLETED.

        return chart;

    }
}
