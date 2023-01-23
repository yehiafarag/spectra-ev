/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.probe.uib.mgfevaluator.gui.pca;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serializable;
import java.util.Hashtable;
import javax.swing.JFrame;
import javax.swing.JPanel;
import no.uib.jexpress_modularized.core.visualization.charts.Axis;
import no.uib.jexpress_modularized.core.visualization.charts.ChartLabel;

/**
 *
 * @author y-mok_000
 */
public class BasicChart extends JPanel implements Serializable{
    
    
    public Axis xaxis,
            yaxis;
    public double[] Rx = new double[0];//new double[]{1, 5.35E9}; //Raw X
    public double[] Ry = new double[0];//new double[]{1, 5.35E9}; //Raw Y
    int[] Nx = new int[]{4, 3, 12, 10}; //Normalized X
    int[] Ny = new int[]{4, 36, -12, 10}; //Normalized Y
    String[] legend = new String[]{"test1", "testing2", "testing 3"};
    public int bottom = 45;
    public int left = 60;
    public int top = 10;
    public int right = 20;
    //public int topTitleHeight;
    public ChartLabel topText = new ChartLabel("");
    int LegendWidth;
    public boolean PaintLegend = false;
    public Hashtable props; //where the values for the chart can be stored.
    public int dotsize = 9;

    public void transferValues(int method, boolean X, double[][] source, int index) {

        if (method == 0) {

            Rx = new double[source.length];

            Ry = new double[source.length];

            if (X) {

                for (int i = 0; i < source.length; i++) {

                    Rx[i] = source[i][index];

                }

            } else {

                for (int i = 0; i < source.length; i++) {

                    Ry[i] = source[i][index];

                }

            }

        }

    }

    public void setXaxisLabel(String label) {

        xaxis.setTitleText(label);

    }

    public void setYaxisLabel(String label) {

        yaxis.setTitleText(label);

    }

    public BasicChart(double[] Rxn, double[] Ryn) {

        if (Rxn != null && Ryn != null) {
            this.Rx = Rxn;
            this.Ry = Ryn;
        }

        setPreferredSize(new Dimension(300, 350));

        topText = new ChartLabel("");

        xaxis = new Axis(Rx, 0, this);

        yaxis = new Axis(Ry, 1, this);

        yaxis.correctForCloseValues = false;

        xaxis.gridcolor = new Color(220, 220, 220);

        yaxis.gridcolor = new Color(220, 220, 220);

        yaxis.dropFirstGridLine = true;

        setXaxisLabel("X-Axis");

        setYaxisLabel("Y-Axis");

        xaxis.setTitleFont(new Font("TIMES NEW ROMAN", 1, 13));

        yaxis.setTitleFont(new Font("TIMES NEW ROMAN", 1, 13));

        topText.setFont(new Font("TIMES NEW ROMAN", 1, 15));

        this.setBackground(new Color(210, 204, 204));

    }

    public BasicChart() {

        setPreferredSize(new Dimension(300, 350));

        xaxis = new Axis(Rx, 0, this);

        yaxis = new Axis(Ry, 1, this);

        yaxis.correctForCloseValues = false;

        xaxis.gridcolor = new Color(220, 220, 220);

        yaxis.gridcolor = new Color(220, 220, 220);

        yaxis.dropFirstGridLine = true;

        setXaxisLabel("X-Axis");

        setYaxisLabel("Y-Axis");

        xaxis.setTitleFont(new Font("TIMES NEW ROMAN", 1, 13));

        yaxis.setTitleFont(new Font("TIMES NEW ROMAN", 1, 13));

        topText.setFont(new Font("TIMES NEW ROMAN", 1, 15));

        this.setBackground(new Color(210, 204, 204));

    }

    public int Height() {
        return getHeight();
    }

    public int Width() {
        return getWidth();
    }

    @Override
    public void paintComponent(Graphics g) {

        int TitleHeight = topText.getHeight(g);

        int HTitlePos = Width() / 2 - (topText.getWidth(g) / 2);

        g.setColor(getBackground());

        g.fillRect(0, 0, Width(), Height());

        g.setColor(Color.black);

        topText.draw(g, HTitlePos, top);

        if (PaintLegend) {
            LegendWidth = paintLegend(g);
        }

        xaxis.positionAxis(left, Width() - right - LegendWidth, Height() - bottom, Height() - bottom);

        yaxis.positionAxis(left, left, top + TitleHeight, Height() - bottom);

        xaxis.data_window.setSize(10, Height() - bottom - top - TitleHeight);

        yaxis.data_window.setSize(Width() - left - right - LegendWidth, 10);

        g.setColor(Color.black);

        if (xaxis != null) {
            xaxis.drawAxis(g);
        }

        if (yaxis != null) {
            yaxis.drawAxis(g);
        }

        Nx = getXValues();

        Ny = getYValues();

        g.drawLine(left + 1, top + TitleHeight, Width() - right - LegendWidth, top + TitleHeight);

        g.drawLine(Width() - right - LegendWidth, top + TitleHeight, Width() - right - LegendWidth, Height() - bottom);

        g.setColor(Color.red);

        for (int i = 0; i < Nx.length; i++) {

            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(Color.red);

            g.fillOval(Nx[i] - (dotsize / 2), Ny[i] - (dotsize / 2), dotsize, dotsize);

            g.setColor(Color.black);

            g.drawOval(Nx[i] - (dotsize / 2), Ny[i] - (dotsize / 2), dotsize, dotsize);

        }

    }

    public int paintLegend(Graphics g) {

        Color bf = g.getColor();

        ChartLabel tf = new ChartLabel("", new Font("TIMES NEW ROMAN", 0, 12));

        tf.setColor(Color.black);

        int maxwidth = -1;

        int height = 0;

        for (int i = 0; i < legend.length; i++) {

            tf.setText(legend[i]);

            if (tf.getWidth(g) > maxwidth) {
                maxwidth = tf.getWidth(g);
            }

            height += tf.getHeight(g);

        }

        maxwidth += 8;

        int top = (Height() / 2) - (height / 2);

        for (int i = 0; i < legend.length; i++) {

            tf.setText(legend[i]);

            tf.draw(g, Width() - maxwidth + 3, top + 3 + (i * tf.getHeight(g)));

        }

        g.setColor(bf);

        return maxwidth;

    }

    public int[] getXValues() {

        if (xaxis == null || Rx == null) {
            return null;
        }

        int[] ret = new int[Rx.length];

        for (int i = 0; i < Rx.length; i++) {

            ret[i] = xaxis.getInteger(Rx[i]);

        }

        return ret;

    }

    public int[] getYValues() {

        if (yaxis == null || Ry == null) {
            return null;
        }

        int[] ret = new int[Ry.length];

        for (int i = 0; i < Ry.length; i++) {

            ret[i] = yaxis.getInteger(Ry[i]);

        }

        return ret;

    }

    
    
}
