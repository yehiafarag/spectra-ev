package no.probe.uib.mgfevaluator.machinelearning;

/**
 * Each node of the tree contains either the attribute number (for non-leaf
 * nodes) or class number (for leaf nodes) in <b>value</b>, and an array of tree
 * nodes in <b>children</b> containing each of the children of the node (for
 * non-leaf nodes). The attribute number corresponds to the column number in the
 * training and test files. The children are ordered in the same order as the
 * Strings in strings[][]. E.g., if value == 3, then the array of children
 * correspond to the branches for attribute 3 (named data[0][3]): children[0] is
 * the branch for attribute 3 == strings[3][0] children[1] is the branch for
 * attribute 3 == strings[3][1] children[2] is the branch for attribute 3 ==
 * strings[3][2] etc. The class number (leaf nodes) also corresponds to the
 * order of classes in strings[][]. For example, a leaf with value == 3
 * corresponds to the class label strings[attributes-1][3].
 *
 */

public class TreeNode {

    private String[][] data;	// Training data indexed by example, attribute
    private String[][] strings; // Unique strings for each attribute
    private TreeNode[] children;
    private int value;
    private int attributes; 	// Number of attributes (including the class)

    public TreeNode(TreeNode[] ch, int val) {
        value = val;
        children = ch;
    } // constructor

    public void setData(String[][] data) {
        this.data = data;
    }

    public void setStrings(String[][] strings) {
        this.strings = strings;
    }

    public void setAttributes(int attributes) {
        this.attributes = attributes;
    }

    public String toString(String indent) {
        if (children != null) {
            String s = "";
            for (int i = 0; i < children.length; i++) {
                s += indent + data[0][value] + "="
                        + strings[value][i] + "\n"
                        + children[i].toString(indent + '\t');
            }
            return s;
        } else {
            return indent + "Class: " + strings[attributes - 1][value] + "\n";
        }
    }

    public TreeNode[] getChildren() {
        return children;
    }

    public int getValue() {
        return value;
    }

    public void setChildren(TreeNode[] children) {
        this.children = children;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
