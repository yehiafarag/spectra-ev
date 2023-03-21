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

import smile.data.CategoricalEncoder;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.io.Read;
import smile.util.Paths;

/**
 *
 * @author Haifeng
 */
public class Segment {

    public static DataFrame train;
    public static DataFrame test;
    public static Formula formula = Formula.lhs("class");

    public static double[][] x;
    public static int[] y;
    public static double[][] testx;
    public static int[] testy;

    static {
        try {
            train = Read.arff(Paths.getTestData("D:/Apps/weka/segment-challenge.arff"));
            test = Read.arff(Paths.getTestData("D:/Apps/weka/segment-test.arff"));

            x = formula.x(train).toArray(false, CategoricalEncoder.DUMMY);
            y = formula.y(train).toIntArray();
            testx = formula.x(test).toArray(false, CategoricalEncoder.DUMMY);
            testy = formula.y(test).toIntArray();
        } catch (Exception ex) {
            System.err.println("Failed to load 'segment': " + ex);
            System.exit(-1);
        }
    }
}
