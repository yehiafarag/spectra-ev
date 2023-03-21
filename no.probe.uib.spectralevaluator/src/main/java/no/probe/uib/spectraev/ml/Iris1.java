/*
 * Copyright (c) 2010-2021 Haifeng Li. All rights reserved.
 *
 * Smile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Smile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Smile.  If not, see <https://www.gnu.org/licenses/>.
 */
package no.probe.uib.spectraev.ml;

import java.awt.Color;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.feature.selection.SumSquaresRatio;
import smile.io.Read;
import smile.util.Paths;
import smile.plot.swing.ScatterPlot;
/**
 *
 * @author Haifeng
 */
public class Iris1 {

    public static DataFrame data;
    public static Formula formula = Formula.lhs("class");

    public static double[][] x;
    public static int[] y;

    static {
        try {
            var iris  = Read.arff(Paths.getTestData("weka/iris.arff"));
            var x = iris.drop("class").toArray();
            var y = iris.column("class").toIntArray();
            SumSquaresRatio.fit(iris,"class");

//            x = iris.select(2, 3).toArray();
//            char[] legends = {'*', '+', 'o'};
//            Color[] colors = {Color.RED, Color.BLUE, Color.CYAN};
//            var canvas = ScatterPlot.of(x, y, legends, colors);
//            canvas.setAxisLabels(iris.names()[2], iris.names()[3]);
//            canvas.window();

        } catch (Exception ex) {
            System.err.println("Failed to load 'iris': " + ex);
            System.exit(-1);
        }
    }
}
