import java.io.Serializable;
import java.util.ArrayList;
import java.text.*;
import java.lang.Math;

public class DecisionTree implements Serializable {

    DTNode rootDTNode;
    int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split

    // Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
    public static final long serialVersionUID = 343L;

    public DecisionTree(ArrayList<Datum> datalist , int min) {
        minSizeDatalist = min;
        rootDTNode = (new DTNode()).fillDTNode(datalist);
    }

    class DTNode implements Serializable{
        //Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
        public static final long serialVersionUID = 438L;
        boolean leaf;
        int label = -1;      // only defined if node is a leaf
        int attribute; // only defined if node is not a leaf
        double threshold;  // only defined if node is not a leaf

        DTNode left, right; //the left and right child of a particular node. (null if leaf)

        DTNode() {
            leaf = true;
            threshold = Double.MAX_VALUE;
        }


        // this method takes in a datalist (ArrayList of type datum). It returns the calling DTNode object
        // as the root of a decision tree trained using the datapoints present in the datalist variable and minSizeDatalist.
        // Also, KEEP IN MIND that the left and right child of the node correspond to "less than" and "greater than or equal to" threshold
        DTNode fillDTNode(ArrayList<Datum> datalist) {

            if(datalist.size() == 0 ||datalist == null )
                return null;


            if(datalist.size() >= minSizeDatalist){

                boolean sameLabel = true;
                int sample = datalist.get(0).y;

                    for (Datum data : datalist) {

                        if (data.y != sample) {
                            sameLabel = false;
                            break;
                        }
                    }

                if(sameLabel){

                    DTNode temp = new DTNode();
                    temp.leaf = true;
                    temp.label = sample;
                    return temp;
                }
                else{

                    double threshold, entropy;
                    double bestThreshold = -1;
                    double bestEntropy = Double.MAX_VALUE;
                    int bestAttribute = -1;


                    for (int i = 0; i < datalist.get(0).x.length; i++) {

                        for (int j = 0; j < datalist.size(); j++) {

                            threshold = datalist.get(j).x[i];

                            ArrayList<Datum> data_1 = new ArrayList<>();
                            ArrayList<Datum> data_2 = new ArrayList<>();

                            for (int k = 0; k < datalist.size(); k++) {

                                if (datalist.get(k).x[i] < threshold)
                                    data_1.add(datalist.get(k));

                                else
                                    data_2.add(datalist.get(k));

                            }

                            double entropyData_1 = calcEntropy(data_1);
                            double entropyData_2 = calcEntropy(data_2);

                            double averageData_1 = (double)(data_1.size()) / (data_1.size() + data_2.size());
                            double averageData_2 = (double)(data_2.size()) / (data_1.size() + data_2.size());

                            entropy = averageData_1 * entropyData_1 + averageData_2 * entropyData_2;

                            if (bestEntropy > entropy) {

                                bestEntropy = entropy;
                                bestAttribute = i;
                                bestThreshold = threshold;


                            }
                        }
                    }


                    if(bestEntropy == calcEntropy(datalist)){

                        DTNode temp = new DTNode();

                        temp.leaf = true;
                        temp.label = findMajority(datalist);

                        return temp;


                    }

                    DTNode newTemp = new DTNode();
                    newTemp.leaf = false;
                    newTemp.attribute = bestAttribute;
                    newTemp.threshold = bestThreshold;


                    ArrayList<Datum> dataset_1 = new ArrayList<>();
                    ArrayList<Datum> dataset_2 = new ArrayList<>();

                    for(int a = 0; a < datalist.size(); a++) {

                        if (datalist.get(a).x[bestAttribute] < bestThreshold)
                            dataset_1.add(datalist.get(a));

                        else
                            dataset_2.add(datalist.get(a));

                    }




                    newTemp.left = fillDTNode(dataset_1);
                    newTemp.right = fillDTNode(dataset_2);

                    return newTemp;
                }



            }

            else{

                DTNode temp = new DTNode();

                temp.leaf = true;
                temp.label = findMajority(datalist);

                return temp;

            }
        }




        // This is a helper method. Given a datalist, this method returns the label that has the most
        // occurrences. In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
        int findMajority(ArrayList<Datum> datalist) {

            int [] votes = new int[2];

            //loop through the data and count the occurrences of datapoints of each label
            for (Datum data : datalist)
            {
                votes[data.y]+=1;
            }

            if (votes[0] >= votes[1])
                return 0;
            else
                return 1;
        }




        // This method takes in a datapoint (excluding the label) in the form of an array of type double (Datum.x) and
        // returns its corresponding label, as determined by the decision tree
        int classifyAtNode(double[] xQuery) {

            if (this.leaf)
                return this.label;

            else{
                if (xQuery[this.attribute] < this.threshold)
                    return left.classifyAtNode(xQuery);

                else
                    return right.classifyAtNode(xQuery);
            }
        }


        //given another DTNode object, this method checks if the tree rooted at the calling DTNode is equal to the tree rooted
        //at DTNode object passed as the parameter
        public boolean equals(Object dt2) {

            if (!(dt2 instanceof DTNode)) return false;

            DTNode node = (DTNode) dt2;

            boolean inputLeaf = node.leaf;
            boolean originalLeaf = this.leaf;

            if (inputLeaf && originalLeaf)
                return this.label == node.label;

            double inputThreashold = node.threshold;
            double originalThreshold = this.threshold;

            int inputAttribute = node.attribute;
            int originalAttribute = this.attribute;

            if (inputThreashold != originalThreshold || inputAttribute != originalAttribute )
                return false;

            if (inputThreashold == originalThreshold && inputAttribute == originalAttribute) {

                boolean leftBool = this.left.equals(node.left);
                boolean rightBool = this.right.equals(node.right);
                return leftBool && rightBool;

            }

            return false;
        }
    }



    //Given a dataset, this returns the entropy of the dataset
    double calcEntropy(ArrayList<Datum> datalist) {
        double entropy = 0;
        double px = 0;
        float [] counter= new float[2];
        if (datalist.size()==0)
            return 0;
        double num0 = 0.00000001,num1 = 0.000000001;

        //calculates the number of points belonging to each of the labels
        for (Datum d : datalist)
        {
            counter[d.y]+=1;
        }
        //calculates the entropy using the formula specified in the document
        for (int i = 0 ; i< counter.length ; i++)
        {
            if (counter[i]>0)
            {
                px = counter[i]/datalist.size();
                entropy -= (px*Math.log(px)/Math.log(2));
            }
        }

        return entropy;
    }


    // given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object
    int classify(double[] xQuery ) {
        return this.rootDTNode.classifyAtNode( xQuery );
    }

    // Checks the performance of a DecisionTree on a dataset
    // This method is provided in case you would like to compare your
    // results with the reference values provided in the PDF in the Data
    // section of the PDF
    String checkPerformance( ArrayList<Datum> datalist) {
        DecimalFormat df = new DecimalFormat("0.000");
        float total = datalist.size();
        float count = 0;

        for (int s = 0 ; s < datalist.size() ; s++) {
            double[] x = datalist.get(s).x;
            int result = datalist.get(s).y;
            if (classify(x) != result) {
                count = count + 1;
            }
        }

        return df.format((count/total));
    }


    //Given two DecisionTree objects, this method checks if both the trees are equal by
    //calling onto the DTNode.equals() method
    public static boolean equals(DecisionTree dt1,  DecisionTree dt2)
    {
        boolean flag = true;
        flag = dt1.rootDTNode.equals(dt2.rootDTNode);
        return flag;
    }

}
