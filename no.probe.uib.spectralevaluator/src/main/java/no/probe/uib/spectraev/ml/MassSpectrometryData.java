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

import static no.probe.uib.spectraev.ml.USPS.formula;
import static no.probe.uib.spectraev.ml.USPS.test;
import static no.probe.uib.spectraev.ml.USPS.testx;
import static no.probe.uib.spectraev.ml.USPS.testy;
import no.probe.uib.mgfevaluator.model.TraningDataset;
import org.apache.commons.csv.CSVFormat;
import smile.data.CategoricalEncoder;
import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.io.Read;
import smile.util.Paths;

/**
 *
 * @author Haifeng
 */
public class MassSpectrometryData {

    public DataFrame test;
    public double[][] testx;
    public int[] testy;

    public DataFrame train;

    public Formula formula = Formula.lhs("Class");
    public double[][] x;
    public int[] y;
    private final DataReader dataReaderUtility;

    public double[] y_reg;
    public double[] testy_reg;
    public String[] dataColumnNames;

    public MassSpectrometryData(TraningDataset traningData, TraningDataset samplingData) {
        this.dataReaderUtility = new DataReader();
        try {
            train = dataReaderUtility.initDataFrame(traningData, Integer.MAX_VALUE);
            test = dataReaderUtility.initDataFrame(samplingData, Integer.MAX_VALUE);
            for (int i = 0; i < samplingData.getSourceDataset().getDataWidth(); i++) {
                if (!samplingData.getSourceDataset().getColumnGroups().get(1).hasMember(i)) {
                    train = train.drop(samplingData.getColumnNames()[i]);
                    test = test.drop(samplingData.getColumnNames()[i]);
                }
            }
            train = train.drop("Index").factorize("Class");
            test = test.drop("Index").factorize("Class");       
            x = formula.x(train).toArray(false, CategoricalEncoder.DUMMY);
            y = formula.y(train).toIntArray();
            y_reg = formula.y(train).toDoubleArray();

            testx = formula.x(test).toArray(false, CategoricalEncoder.DUMMY);
            testy = formula.y(test).toIntArray();
            testy_reg = formula.y(test).toDoubleArray();
        } catch (Exception ex) {
            ex.printStackTrace();
//            System.exit(-1);
        }

    }
}
