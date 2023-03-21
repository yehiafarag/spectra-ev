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

import org.apache.commons.csv.CSVFormat;
import smile.data.CategoricalEncoder;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;
import no.probe.uib.spectraev.ml.CSV;
import smile.util.Paths;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 *
 * @author Haifeng
 */
public class USPS {

    public static DataFrame train;
    public static DataFrame test;
    public static double[][] testx;
    public static int[] testy;
    public static Formula formula = Formula.lhs("class");

    public static double[][] x;
    public static int[] y;
   

    static {
        ArrayList<StructField> fields = new ArrayList<>();
        fields.add(new StructField("class", DataTypes.ByteType));
        IntStream.range(1, 257).forEach(i -> fields.add(new StructField("V" + i, DataTypes.DoubleType)));
        StructType schema = DataTypes.struct(fields);

        CSVFormat format = CSVFormat.Builder.create().setDelimiter(' ').build();
        CSV csv = new CSV(format);
        csv.schema(schema);

        try {
            train = csv.read(Paths.getTestData("usps/zip.train"));
            test = csv.read(Paths.getTestData("usps/zip.test"));
            for (String str : train.names()) {
                System.out.println("train: "+str);
            }
             for (String str : test.names()) {
                System.out.println("test: "+str);
            }
            x = formula.x(train).toArray(false, CategoricalEncoder.DUMMY);
            y = formula.y(train).toIntArray();

            testx = formula.x(test).toArray(false, CategoricalEncoder.DUMMY);
            testy = formula.y(test).toIntArray();
        } catch (Exception ex) {
            System.err.println("Failed to load 'USPS': " + ex);
            System.exit(-1);
        }
    }
}
