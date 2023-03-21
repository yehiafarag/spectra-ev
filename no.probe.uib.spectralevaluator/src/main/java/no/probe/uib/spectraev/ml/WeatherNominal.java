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
public class WeatherNominal {

    public static DataFrame data;
    public static Formula formula = Formula.lhs("play");

    public static double[][] level;
    public static double[][] dummy;
    public static double[][] onehot;
    public static int[] y;

    static {
        try {
            data = Read.arff(Paths.getTestData("D:/Apps/weka/weather.nominal.arff"));

            level = formula.x(data).toArray(false, CategoricalEncoder.LEVEL);
            dummy = formula.x(data).toArray(false, CategoricalEncoder.DUMMY);
            onehot = formula.x(data).toArray(false, CategoricalEncoder.ONE_HOT);
            y = formula.y(data).toIntArray();
         
        } catch (Exception ex) {
            System.err.println("Failed to load 'weather nominal': " + ex);
            System.exit(-1);
        }
    }
}
