/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.gui.components;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 *
 * @author yfa041
 */
public class SimpleHeatMap extends JPanel {
    
    private final Border border;
//    private final Color[] postiveColor;
    private final Color[] negativeColor;
    
    public SimpleHeatMap() {
//        this.postiveColor = createGradient(Color.WHITE, Color.GREEN, 100);
        this.negativeColor = createGradient(Color.GREEN, Color.RED, 101);
        this.border = new LineBorder(Color.LIGHT_GRAY, 1);
        this.setLayout(null);
        this.setBorder(new LineBorder(Color.BLACK, 2));
    }
    
    public void setData(String[] columnids, String[] rowIds, double[][] data) {
        this.removeAll();
        int width = ((columnids.length + 1) * 100) + 10;
        int height = ((rowIds.length + 1) * 20) + 10;
        this.setSize(width, height);
        int currentX = 105, currentY = 5;

//        double[] maxValues = new double[columnids.length];
//        double[] minValues = new double[columnids.length];
//        for (int i = 0; i < maxValues.length; i++) {
//            maxValues[i] = Double.MIN_VALUE;
//            minValues[i] = Double.MAX_VALUE;
//        }
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (double[] data1 : data) {
            for (int j = 0; j < data[0].length; j++) {
//                maxValues[j] = Math.max(maxValues[j], data1[j]);
//                minValues[j] = Math.min(minValues[j], data1[j]);
                max = Math.max(max, data1[j]);
                min = Math.min(min, data1[j]);
                
            }
        }
        
        for (String columnId : columnids) {
            JLabel cell = generateLabel(columnId);
            this.add(cell);
            cell.setBounds(currentX, currentY, 100, 20);
            currentX += 100;
            
        }
        currentX = 5;
        currentY = 25;
        for (String rowId : rowIds) {
            JLabel cell = generateLabel(rowId);
            this.add(cell);
            cell.setBounds(currentX, currentY, 100, 20);
            currentY += 20;
        }
        currentY = 5;
        for (double[] dArr : data) {
            currentX = 105;
            currentY += 20;
            int columnIndex = 0;
            for (double d : dArr) {
//                JPanel cell = generateColorLabel(maxValues[columnIndex], minValues[columnIndex], d);
                JPanel cell = generateColorLabel(max, min, d);
                this.add(cell);
                cell.setBounds(currentX, currentY, 100, 20);
                currentX += 100;
                columnIndex++;
            }
            
        }
    }
    
    private JLabel generateLabel(String label) {
        JLabel cell = new JLabel(label);
        cell.setSize(50, 20);
        cell.setBorder(border);
        return cell;
        
    }
    
    private JPanel generateColorLabel(double max, double min, double value) {
        JPanel cell = new JPanel();
        cell.setSize(50, 20);
        cell.setBorder(border);
        if (value <= 0.05) {
            cell.setBackground(Color.WHITE);
        } else {
            double r = max - 0.05;
            double v = value - 0.05;
            int i = (int) Math.round((v * 100.0) / r);
            cell.setBackground(negativeColor[i]);
            
        }
//        } else if (value == max && value > 0) {
//            cell.setBackground(Color.GREEN);
//        } else if (value == min && value < 0) {
//            cell.setBackground(Color.RED);
//        } else if (value > 0) {
//            int index= (int)Math.round(value*100.0/max);
//             cell.setBackground(postiveColor[index]);
//        } else if (value < 0) {
//            int index=(int) Math.abs(Math.round(value*100.0/min));
//             cell.setBackground(negativeColor[index]);
//        }

//        cell.setBackground(colors.getColor(value));//(getColor(max, min, Color.GREEN, Color.RED, value));
        JLabel label = new JLabel(value + "");
        cell.add(label);
        cell.repaint();
        return cell;
        
    }

    /**
     * Creates an array of Color objects for use as a gradient, using a linear
     * interpolation between the two specified colors.
     *
     * @param one Color used for the bottom of the gradient
     * @param two Color used for the top of the gradient
     * @param numSteps The number of steps in the gradient. 250 is a good
     * number.
     */
    private Color[] createGradient(final Color one, final Color two, final int numSteps) {
        int r1 = one.getRed();
        int g1 = one.getGreen();
        int b1 = one.getBlue();
        int a1 = one.getAlpha();
        
        int r2 = two.getRed();
        int g2 = two.getGreen();
        int b2 = two.getBlue();
        int a2 = two.getAlpha();
        
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int newA = 0;
        
        Color[] gradient = new Color[numSteps];
        double iNorm;
        for (int i = 0; i < numSteps; i++) {
            iNorm = i / (double) numSteps; //a normalized [0:1] variable
            newR = (int) (r1 + iNorm * (r2 - r1));
            newG = (int) (g1 + iNorm * (g2 - g1));
            newB = (int) (b1 + iNorm * (b2 - b1));
            newA = (int) (a1 + iNorm * (a2 - a1));
            gradient[i] = new Color(newR, newG, newB, newA);
        }
        
        return gradient;
    }
    
    private Color getColor(double highBound, double lowBound, Color highColor, Color lowColor, double val) {
        if (val == 0) {
            return Color.WHITE;
        }
        if (val == highBound) {
            return highColor;
        }
        if (val == lowBound) {
            return lowColor;
        }
        if (val > highBound || val < lowBound) {
            System.out.println("no.probe.uib.mgfevaluator.gui.components.SimpleHeatMap.getColor()");
            return Color.ORANGE;
        }
        // proportion the val is between the high and low bounds
        double ratio = (val - lowBound) / (highBound - lowBound);
        int[] rgb = new int[3];
        // step through each color and find the value that represents the approriate proportional value 
        // between the high and low colors
        for (int i = 0; i < 3; i++) {
            int hc;
            int lc;
            switch (i) {
                case 0:
                    hc = highColor.getRed();
                    lc = lowColor.getRed();
                    System.out.print("r " + hc + "  " + lc + "|||");
                    break;
                case 1:
                    hc = highColor.getGreen();
                    lc = lowColor.getGreen();
                    System.out.print("g " + hc + "  " + lc + "|||");
                    break;
                default:
                    hc = highColor.getBlue();
                    lc = lowColor.getBlue();
                    System.out.print("b " + hc + "  " + lc + "|||");
            }
            // high color is lower than low color - reverse the subtracted vals
            boolean reverse = hc < lc;
            // difference between the high and low values
            int diff = reverse ? lc - hc : hc - lc;
            // lowest value of the two
            int baseVal = reverse ? hc : lc;
            rgb[i] = (int) Math.round((double) diff * ratio) + baseVal;
            
        }
        System.out.println("-------------------------------------");
        return new Color(rgb[0], rgb[1], rgb[2]);
    }
    
}
