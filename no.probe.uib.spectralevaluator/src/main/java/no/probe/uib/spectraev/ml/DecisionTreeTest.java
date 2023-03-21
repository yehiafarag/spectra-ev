/** *****************************************************************************
 * Copyright (c) 2010 Haifeng Li
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ****************************************************************************** */
package no.probe.uib.spectraev.ml;

import java.util.TreeMap;
import smile.base.cart.SplitRule;
import smile.math.MathEx;
import smile.validation.*;
import smile.validation.metric.Error;

import static org.junit.Assert.*;
import smile.classification.DecisionTree;
import smile.data.formula.Term;
import smile.math.matrix.Matrix;
import smile.regression.RegressionTree;
import no.probe.uib.spectraev.ml.PenDigits;

/**
 *
 * @author Haifeng
 */
public class DecisionTreeTest {

    public DecisionTreeTest() {
    }

    public static void setUpClass() throws Exception {
    }

    public static void tearDownClass() throws Exception {
    }

    public void setUp() {
    }

    public void tearDown() {
    }

    public void testWeather() throws Exception {
        System.out.println("Weather");

        DecisionTree model = DecisionTree.fit(WeatherNominal.formula, WeatherNominal.data, SplitRule.GINI, 8, 10, 1);
        System.out.println(model);

        double[] importance = model.importance();
        for (int i = 0; i < importance.length; i++) {
            System.out.format("%-15s %.4f%n", model.schema().name(i), importance[i]);
        }
//
//        java.nio.file.Path temp = smile.data.Serialize.write(model);
//        smile.data.Serialize.read(temp);

        ClassificationMetrics metrics = LOOCV.classification(WeatherNominal.formula, WeatherNominal.data, (f, x) -> DecisionTree.fit(f, x, SplitRule.GINI, 8, 10, 1));

        System.out.println(metrics);
        assertEquals(0.5, metrics.accuracy, 1E-4);
    }

    public void testIris() {
        System.out.println("Iris");

        DecisionTree model = DecisionTree.fit(Iris.formula, Iris.data);
        System.out.println(model);

        double[] importance = model.importance();
        for (int i = 0; i < importance.length; i++) {
            System.out.format("%-15s %.4f%n", model.schema().name(i), importance[i]);
        }

        ClassificationMetrics metrics = LOOCV.classification(Iris.formula, Iris.data, DecisionTree::fit);

        System.out.println(metrics);
        assertEquals(0.94, metrics.accuracy, 1E-4);
    }

    public void testPenDigits() {
        System.out.println("Pen Digits");

        MathEx.setSeed(19650218); // to get repeatable results.
        ClassificationValidations<DecisionTree> result = CrossValidation.classification(10, PenDigits.formula, PenDigits.data,
                (f, x) -> DecisionTree.fit(f, x, SplitRule.GINI, 20, 100, 5));

        System.out.println(result);
        assertEquals(0.9532, result.avg.accuracy, 1E-4);
    }

    public void testBreastCancer() {
        System.out.println("Breast Cancer");
        Matrix m = BreastCancer.data.toMatrix();
        MathEx.setSeed(19650218); // to get repeatable results.
        DecisionTree model = DecisionTree.fit(BreastCancer.formula, BreastCancer.data, SplitRule.GINI, 20, 100, 5);
        ClassificationValidations<DecisionTree> result = CrossValidation.classification(10, BreastCancer.formula, BreastCancer.data,
                (f, x) -> model);

        int[] prediction = model.predict(BreastCancer.data);
        Term[] predictors = model.formula().predictors();
        System.out.println("at predection size " + prediction.length + "  " + BreastCancer.y.length);
        for (int i = 0; i < prediction.length; i++) {
//            System.out.println(" i " + prediction[i] + "  " + BreastCancer.y[i] + "  " + BreastCancer.data.getString(i, "diagnosis"));
        }

        System.out.println("DT results--->> " + result);

        RegressionTree model2 = RegressionTree.fit(BreastCancer.formula, BreastCancer.data, 100, 20, 2);
        double[] importance2 = model2.importance();

        TreeMap<Double, String> columnimportancy3 = new TreeMap<>();
        for (int i = 0; i < importance2.length; i++) {
            columnimportancy3.put(importance2[i], model2.schema().name(i));
        }
        double[] prediction2 = model2.predict(BreastCancer.data);
        
        System.out.println("at predectison size " + prediction2.length + "   ");
        for(Term t:predictors){
            System.out.println("at term "+t.expand());
        }
        
        for (int i = 0; i < prediction2.length; i++) {
            System.out.println(" i " + i + "  " + prediction2[i] + "   " + BreastCancer.regy[i] + "   " + BreastCancer.data.getString(i, "diagnosis"));
        }
        RegressionMetrics metrics = LOOCV.regression(BreastCancer.formula, BreastCancer.data, (formula, x) -> model2);
        System.out.println("regression tree 2 : " + metrics);
        
        RegressionValidation validation = new RegressionValidation(model2, BreastCancer.regy,prediction2, metrics);
        
        RegressionValidations<RegressionTree> result2 = CrossValidation.regression(10, BreastCancer.formula, BreastCancer.data,
                (f, x) -> model2);
        System.out.println("RT-resutes: ---> "+result2);
        System.out.println("RT-validation: ---> "+validation);
        
        
        

//        assertEquals(0.9275, result.avg.accuracy, 1E-4);
    }

    public void testMassSpectrometryData() {

    }

    public void testSegment() {
        System.out.println("Segment");

        DecisionTree model = DecisionTree.fit(Segment.formula, Segment.train, SplitRule.ENTROPY, 20, 100, 5);
        System.out.println(model);

        double[] importance = model.importance();
        for (int i = 0; i < importance.length; i++) {
            System.out.format("%-15s %.4f%n", model.schema().name(i), importance[i]);
        }

        int[] prediction = model.predict(Segment.test);
        int error = Error.of(Segment.testy, prediction);

        System.out.println("Error = " + error);
        assertEquals(43, error, 1E-4);
    }

    public void testUSPS() {
        System.out.println("USPS");
       
        DecisionTree model = DecisionTree.fit(USPS.formula, USPS.train, SplitRule.ENTROPY, 20, 500, 5);
//        System.out.println(model);

        double[] importance = model.importance();
        for (int i = 0; i < importance.length; i++) {
            System.out.format("%-15s %.4f%n", model.schema().name(i), importance[i]);
        }

        int[] prediction = model.predict(USPS.test);
        int error = Error.of(USPS.testy, prediction);

        Term[] predictors = model.formula().predictors();
        for (Term i : predictors) {
            System.out.println(" i " + i.variables() + "  " + i.expand());
        }

        System.out.println("Error = " + error);
        assertEquals(331, error);
    }

    public void testPrune() {
        System.out.println("USPS");

        // Overfitting with very large maxNodes and small nodeSize
        DecisionTree model = DecisionTree.fit(USPS.formula, USPS.train, SplitRule.ENTROPY, 20, 3000, 1);
        System.out.println(model);

        double[] importance = model.importance();
        for (int i = 0; i < importance.length; i++) {
            System.out.format("%-15s %.4f%n", model.schema().name(i), importance[i]);
        }

        int[] prediction = model.predict(USPS.test);
        int error = Error.of(USPS.testy, prediction);

        System.out.println("Error = " + error);
        assertEquals(897, model.size());
        assertEquals(324, error);

        DecisionTree lean = model.prune(USPS.test);
        System.out.println(lean);

        importance = lean.importance();
        for (int i = 0; i < importance.length; i++) {
            System.out.format("%-15s %.4f%n", lean.schema().name(i), importance[i]);
        }

        // The old model should not be modified.
        prediction = model.predict(USPS.test);
        error = Error.of(USPS.testy, prediction);

        System.out.println("Error of old model after pruning = " + error);
        assertEquals(897, model.size());
        assertEquals(324, error);

        prediction = lean.predict(USPS.test);
        error = Error.of(USPS.testy, prediction);

        System.out.println("Error of pruned model after pruning = " + error);
        assertEquals(743, lean.size());
        assertEquals(273, error);
    }

    public void testShap() {
        MathEx.setSeed(19650218); // to get repeatable results.
        DecisionTree model = DecisionTree.fit(Iris.formula, Iris.data, SplitRule.GINI, 20, 100, 5);
        String[] fields = java.util.Arrays.stream(model.schema().fields()).map(field -> field.name).toArray(String[]::new);
        double[] importance = model.importance();
        double[] shap = model.shap(Iris.data);
        System.out.println("----- importance -----");
        for (int i = 0; i < importance.length; i++) {
            System.out.format("%-15s %.4f%n", fields[i], importance[i]);
        }
        System.out.println("----- SHAP -----");
        for (int i = 0; i < fields.length; i++) {
            System.out.format("%-15s %.4f    %.4f    %.4f%n", fields[i], shap[2 * i], shap[2 * i + 1], shap[2 * i + 2]);
        }
    }
}
