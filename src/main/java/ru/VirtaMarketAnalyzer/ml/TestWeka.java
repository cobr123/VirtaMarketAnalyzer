package ru.VirtaMarketAnalyzer.ml;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.*;

/**
 * Created by cobr123 on 15.01.2016.
 */
public class TestWeka {
    private static FastVector attrs;
    private static Instances isTrainingSet;

    public static void main(String[] args) throws Exception {
        // Declare numeric attributes
        attrs = new FastVector(6);
        attrs.addElement(new Attribute("houseSize"));
        attrs.addElement(new Attribute("lotSize"));
        attrs.addElement(new Attribute("bedrooms"));

        attrs.addElement(new Attribute("granite"));
        attrs.addElement(new Attribute("bathroom"));
        attrs.addElement(new Attribute("sellingPrice"));

        // add the instance
        createTrainingSet();

        // Create a LinearRegression classifier
        Classifier cModel = new LinearRegression();
        cModel.buildClassifier(isTrainingSet);
        // Print the result à la Weka explorer:
        System.out.println(cModel.toString());

        // TestWeka the model
        Evaluation eTest = new Evaluation(isTrainingSet);
        eTest.evaluateModel(cModel, isTrainingSet);

        // Print the result à la Weka explorer:
        System.out.println(eTest.toSummaryString());

        // Specify that the instance belong to the training set
        // in order to inherit from the set description
        Instance iUse = createInstance(3198, 9669, 5, 0, 1, 0);
        iUse.setDataset(isTrainingSet);

        // Get the likelihood of each classes
        double[] fDistribution = cModel.distributionForInstance(iUse);
        System.out.println(fDistribution[0]);
    }

    static void createTrainingSet() {
        // Create an empty training set
        isTrainingSet = new Instances("house", attrs, 7);
        // Set class index (sellingPrice)
        isTrainingSet.setClassIndex(5);

        isTrainingSet.add(createInstance(3529, 9191, 6, 0, 0, 205000));
        isTrainingSet.add(createInstance(3247, 10061, 5, 1, 1, 224900));
        isTrainingSet.add(createInstance(4032, 10150, 5, 0, 1, 197900));

        isTrainingSet.add(createInstance(2397, 14156, 4, 1, 0, 189900));
        isTrainingSet.add(createInstance(2200, 9600, 4, 0, 1, 195000));
        isTrainingSet.add(createInstance(3536, 19994, 6, 1, 1, 325000));

        isTrainingSet.add(createInstance(2983, 9365, 5, 0, 1, 230000));
    }

    static public Instance createInstance(int houseSize, int lotSize, int bedrooms,
                                          int granite, int bathroom, int sellingPrice) {
        Instance instance = new Instance(6);
        instance.setValue((Attribute) attrs.elementAt(0), houseSize);
        instance.setValue((Attribute) attrs.elementAt(1), lotSize);
        instance.setValue((Attribute) attrs.elementAt(2), bedrooms);
        instance.setValue((Attribute) attrs.elementAt(3), granite);
        instance.setValue((Attribute) attrs.elementAt(4), bathroom);
        instance.setValue((Attribute) attrs.elementAt(5), sellingPrice);
        return instance;
    }
}
