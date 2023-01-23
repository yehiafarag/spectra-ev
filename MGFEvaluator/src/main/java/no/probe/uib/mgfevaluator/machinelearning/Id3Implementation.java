/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.machinelearning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import no.probe.uib.mgfevaluator.model.TraningDataset;

/**
 *
 * @author yfa041
 */
public class Id3Implementation {

    private String used;
    private int attributes; 	// Number of attributes (including the class)
    private int examples;		// Number of training examples
    private TreeNode decisionTree;	// Tree learnt in training, used for classifying
    private String[][] data;	// Training data indexed by example, attribute
    private String[][] strings; // Unique strings for each attribute
    private int[] stringCount;  // Number of unique strings for each attribute

    public Id3Implementation() {
        used = "used";
        attributes = 0;
        examples = 0;
        decisionTree = null;
        data = null;
        strings = null;
        stringCount = null;
    }

    public void printTree() {
        if (decisionTree == null) {
            error("Attempted to print null Tree");
        } else {
            System.out.println(decisionTree);
        }
    } // printTree()

    /**
     * Print error message and exit. *
     */
    static void error(String msg) {
        System.err.println("Error: " + msg);
        System.exit(1);
    } // error()

    static final double LOG2 = Math.log(2.0);

    static double xlogx(double x) {
        return x == 0 ? 0 : x * Math.log(x) / LOG2;
    } // xlogx()

    /**
     * Execute the decision tree on the given examples in testData, and print
     * the resulting class names, one to a line, for each example in testData.
     *
     */
    public void classify(String[][] testData) {
        if (decisionTree == null) {
            error("Please run training phase before classification");
        }
        for (int i = 1; i < testData.length; i++) {
            String ans = transverse(decisionTree, testData[i]); // Send in the tree and a row to go along with it
            System.out.println(i + "-->" + ans); // Output classification to console which can be then be mapped to a file using "> xyz.file"
        }

    } // classify()

    /**
     * Execute the decision tree on the given examples in testData, and print
     * the resulting class names, one to a line, for each example in testData.
     *
     * @param testData
     * @param rowIds
     * @return
     */
    public Map<String, String> classify(String[][] testData, String[] rowIds) {
        Map<String, String> decisionMap = new LinkedHashMap<>();
        if (decisionTree == null) {
            error("Please run training phase before classification");
        }
        for (int i = 1; i < testData.length; i++) {
            String ans = transverse(decisionTree, testData[i]); // Send in the tree and a row to go along with it
//            System.out.println(i+"--"+rowIds[i-1]+"--->"+ans); // Output classification to console which can be then be mapped to a file using "> xyz.file"
            decisionMap.put(rowIds[i - 1], ans);
        }
        return decisionMap;
    } // classify()

    public String transverse(TreeNode currentNode, String[] row) {
        // Base case should return leaf node which means esentially it would only be the node where it's children are null
        if (currentNode.getChildren() == null) {
            // Returns [attributes-1] because we want the class and [currentNode value] to get the value stored at that attribute
            //  since it is a leaf node
            return strings[attributes - 1][currentNode.getValue()];
        } else { // Transverse through tree compare for each unique string for the attribute using Strings[currentNode.value]
            // In doing so, keep this in a loop and check to see if the test data value is equal to the strings[][] value
            // Once done return the position where the string was found and return the node with children on it.
            int posInStrings = -1;
            for (int i = 0; i < strings[currentNode.getValue()].length; i++) {
                if (row[currentNode.getValue()].equals(strings[currentNode.getValue()][i])) {
                    posInStrings = i;
                }

            }
            // Transverse the decision tree by calling the method again via recursion but passing the current node children
            // until the node it is currently on has no children in which just return the classification which is returned back
            // as a string

            return transverse(currentNode.getChildren()[posInStrings], row);

        }

    } // transverse()

    public void train(String[][] trainingData) {
        indexStrings(trainingData);
        String[] usedAttributes = data[0].clone(); // Get all the headers or rather attribute names
        decisionTree = new TreeNode(null, 0);
        buildTree(decisionTree, trainingData, usedAttributes);
    } // train()

    public void train(TraningDataset trainingData) {
        indexStrings(trainingData.getData());
        String[] usedAttributes = data[0].clone(); // Get all the headers or rather attribute names
        decisionTree = new TreeNode(null, 0);
        buildTree(decisionTree, data, usedAttributes);
    } // train()

    /**
     * Returns a boolean if all the attributes have been used and have been
     * replaced with "used" string. There is a running counter and if used is
     * equal to the number of columns/attributes then that means that all the
     * attributes have been used
     *
     */
    boolean checkUsedAttributes(String[] attrCol) {
        int attrCounter = 0;
        //boolean usedAttribute;
        for (int i = 0; i < attrCol.length - 1; i++) {
            if (attrCol[i].equals(used)) {
                attrCounter++;
            }

        }
        /* we don't include class so -1 */
        if (attrCounter == attrCol.length - 1) {
            return true;
        } else {
            return false;
        }

    } //checkUsedAttributes

    /**
     * Grabs a subset or rather makes a subset of the currentDataSet that it is
     * given.
     *
     *
     */
    public String[][] getSubset(String[][] currentDataSet, int attr, int attrVal) {
        int attrCounter = countAttributes(currentDataSet, attr, attrVal);
        // Again we don't want a class "attribute" column
        String[][] subSet = new String[attrCounter + 1][currentDataSet[0].length - 1];
        int rowCount = 1;
        int rows = currentDataSet.length;
        subSet[0] = currentDataSet[0];
        for (int i = 1; i < rows; i++) {
            if (currentDataSet[i][attr].equals(strings[attr][attrVal])) {
                subSet[rowCount] = currentDataSet[i];
                rowCount++;
            }
        }
        return subSet;

    } //getSubset()

    /**
     * The heart of the program. Here we build the decision tree. It takes in an
     * array of data and the current TreeNode. Each call on buildTree will
     * esentially split the dataset on the best attribute. The current node that
     * is set to be split will have it's value the same as the best attribute's
     * value. It's children are then also added with their respective indexes.
     * If it's a leaf node (entropy = 0 or no more attributes) the method would
     * return and we will have our tree!
     *
     */
    public void buildTree(TreeNode node, String[][] currentDataSet, String[] usedAttributes) {
        //Calculate the root entropy
        double rootEntropy = calcEntropy(currentDataSet);
        double rows = examples - 1;
        double comparator = 0;
        int bestAttribute = 0;
        double[] infoGain = new double[attributes];
        double[] subSetEntropy;
        double[] instanceCount;

        // most common attribute in the subset
        if (rootEntropy <= 0.0 || checkUsedAttributes(usedAttributes)) {
            int leafClass = 0;
            int instances = 0;
            for (int z = 0; z < stringCount[attributes - 1]; z++) {
                if (instances < countAttributes(currentDataSet, currentDataSet[0].length - 1, z)) {
                    instances = countAttributes(currentDataSet, currentDataSet[0].length - 1, z);
                    leafClass = z;
                }
            }
            node.setValue(leafClass);
            return;
        } else {
            //check every attribute for the highest information gain to split on
            for (int i = 0; i < currentDataSet[0].length - 1; i++) {
                if (usedAttributes[i].equals(used)) {
                    //ignore these attributes;
                    infoGain[i] = 0;
                } else {
                    //initalise variables needed to calculate information gain
                    subSetEntropy = new double[stringCount[i]];
                    instanceCount = new double[stringCount[i]];
                    for (int j = 0; j < stringCount[i]; j++) {
                        //Every attribute gets a subset and then we calculate their respective entropy for their children nodes and
                        // count every instance in their attribute in order to calculate entropy and later information gain!
                        String[][] subSet = getSubset(currentDataSet, i, j);
                        subSetEntropy[j] = calcEntropy(subSet);
                        instanceCount[j] = countAttributes(subSet, i, j);
                    }
                    //now we have all the info we can calculate information gain
                    infoGain[i] = rootEntropy;
                    double tmp = 0;
                    for (int a = 0; a < subSetEntropy.length; a++) {
                        //You get NaN on empty subset so we need to check for it and deal with it appropriately
                        tmp = (instanceCount[a] / rows * subSetEntropy[a]);
                        if (!Double.isNaN(tmp)) {
                            infoGain[i] -= tmp;
                        }
                    }
                    infoGain[i] = Math.abs(infoGain[i]); // Make sure value is positive
                    //highest gain so far will be the attribute to split on
                    if (infoGain[i] >= comparator && !usedAttributes[i].equals(used)) {
                        comparator = infoGain[i];
                        bestAttribute = i;
                    }
                }
            }
            //Since it went through this else statement, it is a non-leaf node and therefore
            // we adjust accordingling by chaing the node's value
            node.setValue(bestAttribute);
            node.setChildren(new TreeNode[stringCount[bestAttribute]]);

            for (int n = 0; n < stringCount[bestAttribute]; n++) {
                String[] temp = usedAttributes.clone();
                String[][] newSubSet = getSubset(currentDataSet, bestAttribute, n);
                node.getChildren()[n] = new TreeNode(null, 0);
                if (newSubSet.length != 1) {
                    temp[bestAttribute] = used;
                    buildTree(node.getChildren()[n], newSubSet, temp);

                } else {
                    // split data has no rows so force that node to be checked by setting all their attribute values to "used"
                    for (int m = 0; m < temp.length - 1; m++) {
                        temp[m] = used;
                    }
                    buildTree(node.getChildren()[n], currentDataSet, temp);
                }
            }
        }

    } // buildTree()

    public int countAttributes(String[][] currentDataSet, int attr, int attrVal) {
        int count = 0;
        if (currentDataSet.length == 1) {
            return count;
        }
        // Don't want class headers
        for (int i = 1; i < currentDataSet.length; i++) {
            if (currentDataSet[i][attr].equals(strings[attr][attrVal])) {
                count++;
            }
        }
        return count;
    } // countAttributes()

    /**
     * Pass the dataset we want to calculate the entropy from. I am making a big
     * assumption where the last column (should be representing class) is the
     * class I will utilise the stringCount variable and manipulate it in order
     * to get the right number of columns and rows
     *
     */
    public double calcEntropy(String[][] currentDataSet) {
        double rows = currentDataSet.length - 1;
        double[] noClassInstances = new double[stringCount[attributes - 1]];
        //loops through each class's instances and returns a value to say how many instances are in there
        for (int i = 0; i < stringCount[attributes - 1]; i++) {
            noClassInstances[i] = countAttributes(currentDataSet, attributes - 1, i);
        }
        // E(S) = -xlogx(P+) - xlogx(P-)
        double entropy = -xlogx(noClassInstances[0] / rows);
        for (int a = 1; a < noClassInstances.length; a++) {
            entropy -= (xlogx(noClassInstances[a] / rows));
        }
        return Math.abs(entropy); // due to being a double need to force values to be positive or negative. Sometimes can get -0.0 because of float
    } // calcEntropy()

    /**
     * Given a 2-dimensional array containing the training data, numbers each
     * unique value that each attribute has, and stores these Strings in
     * instance variables; for example, for attribute 2, its first value would
     * be stored in strings[2][0], its second value in strings[2][1], and so on;
     * and the number of different values in stringCount[2].
     *
     */
    void indexStrings(String[][] inputData) {
        data = inputData;
        examples = data.length;
        attributes = data[0].length;
        stringCount = new int[attributes];
        strings = new String[attributes][examples];// might not need all columns
        int index = 0;
        for (int attr = 0; attr < attributes; attr++) {
            stringCount[attr] = 0;
            for (int ex = 1; ex < examples; ex++) {
                for (index = 0; index < stringCount[attr]; index++) {
                    if (data[ex][attr] == null || strings[attr][index] == null) {
                        System.out.println(ex + " - :" + attr + "   index: " + index + "  :-  data[ex][attr] " + data[ex][attr] + "    " + strings[attr][index]);
                    }
                    if (data[ex][attr].equals(strings[attr][index])) {
                        break;	// we've seen this String before
                    }
                }
                if (index == stringCount[attr]) // if new String found
                {
                    strings[attr][stringCount[attr]++] = data[ex][attr];
                }
            } // for each example
        } // for each attribute
    } // indexStrings()

    /**
     * For debugging: prints the list of attribute values for each attribute and
     * their index values.
     *
     */
    void printStrings() {
        for (int attr = 0; attr < attributes; attr++) {
            for (int index = 0; index < stringCount[attr]; index++) {
                System.out.println(data[0][attr] + " value " + index
                        + " = " + strings[attr][index]);
            }
        }
    } // printStrings()

    /**
     * Reads a text file containing a fixed number of comma-separated values on
     * each line, and returns a two dimensional array of these values, indexed
     * by line number and position in line.
     *
     */
    static String[][] parseCSV(String fileName)
            throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String s = br.readLine();
        int fields = 1;
        int index = 0;
        while ((index = s.indexOf(',', index) + 1) > 0) {
            fields++;
        }
        int lines = 1;
        while (br.readLine() != null) {
            lines++;
        }
        br.close();
        String[][] data = new String[lines][fields];
        Scanner sc = new Scanner(new File(fileName));
        sc.useDelimiter("[,\n]");
        for (int n = 0; n < lines; n++) {
            for (int f = 0; f < fields; f++) {
                if (sc.hasNext()) {
                    data[n][f] = sc.next();
                } else {
                    error("Scan error in " + fileName + " at " + n + ":" + f);
                }
            }
        }
        sc.close();
        return data;
    } // parseCSV()

    public static void main(String[] args) throws FileNotFoundException,
            IOException {
        System.out.println("step 0 is done ");
        String[][] trainingData = parseCSV("D:\\Apps\\downloaded ID3\\ID3-master\\ID3-master\\realEstateTrain.csv");
        System.out.println("step 1 is done ");
        String[][] testData = parseCSV("D:\\Apps\\downloaded ID3\\ID3-master\\ID3-master\\realEstateTest.csv");
        System.out.println("step 2 is done ");
        Id3Implementation classifier = new Id3Implementation();
        System.out.println("step 3 is done ");
        classifier.train(trainingData);
        System.out.println("step 4 is done ");
        classifier.printTree();
        System.out.println("step 5 is done ");
        classifier.classify(testData);
    } // main()
}
