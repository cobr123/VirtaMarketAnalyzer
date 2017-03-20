package ru.VirtaMarketAnalyzer.ml.js;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.Utils;

import static ru.VirtaMarketAnalyzer.ml.js.ClassifierToJs.getPrivateFieldValue;

/**
 * Created by cobr123 on 09.03.2017.
 */
public final class LinearRegressionToJson {
    private static final Logger logger = LoggerFactory.getLogger(LinearRegressionToJson.class);

    public static String toString(final LinearRegression classifier) throws Exception {
        final Instances m_TransformedData = (Instances) getPrivateFieldValue(classifier.getClass(), classifier, "m_TransformedData");
        final int m_ClassIndex = (int) getPrivateFieldValue(classifier.getClass(), classifier, "m_ClassIndex");
        final boolean[] m_SelectedAttributes = (boolean[]) getPrivateFieldValue(classifier.getClass(), classifier, "m_SelectedAttributes");
        final double[] m_Coefficients = (double[]) getPrivateFieldValue(classifier.getClass(), classifier, "m_Coefficients");

        if (m_TransformedData == null) {
            return "Linear Regression: No model built yet.";
        }
        try {
            StringBuffer text = new StringBuffer();
            int column = 0;
            boolean first = true;

            text.append("\nLinear Regression Model\n\n");

            text.append(m_TransformedData.classAttribute().name() + " =\n\n");
            for (int i = 0; i < m_TransformedData.numAttributes(); i++) {
                if ((i != m_ClassIndex) && (m_SelectedAttributes[i])) {
                    if (!first) {
                        text.append(" +\n");
                    } else {
                        first = false;
                    }
                    text.append(Utils.doubleToString(m_Coefficients[column], 12, 4) + " * ");
                    text.append(m_TransformedData.attribute(i).name());
                    column++;
                }
            }
            text.append(" +\n" + Utils.doubleToString(m_Coefficients[column], 12, 4));
            return text.toString();
        } catch (Exception e) {
            return "Can't print Linear Regression!";
        }
    }

    public static String toJson(final LinearRegression classifier) throws Exception {
        final Instances m_TransformedData = (Instances) getPrivateFieldValue(classifier.getClass(), classifier, "m_TransformedData");
        final int m_ClassIndex = (int) getPrivateFieldValue(classifier.getClass(), classifier, "m_ClassIndex");
        final boolean[] m_SelectedAttributes = (boolean[]) getPrivateFieldValue(classifier.getClass(), classifier, "m_SelectedAttributes");
        final double[] m_Coefficients = (double[]) getPrivateFieldValue(classifier.getClass(), classifier, "m_Coefficients");

        if (m_TransformedData == null) {
            throw new Exception("Linear Regression: No model built yet.");
        }
        final StringBuffer text = new StringBuffer();
        final StringBuffer json = new StringBuffer();
        int column = 0;
        boolean first = true;

        text.append("\nLinear Regression Model\n\n");

        text.append(m_TransformedData.classAttribute().name() + " =\n\n");

        json.append("{");
        json.append("\"name\": \"" + m_TransformedData.classAttribute().name() + "\"");
        json.append(",\"attrs\": [");
        for (int i = 0; i < m_TransformedData.numAttributes(); i++) {
            if ((i != m_ClassIndex) && (m_SelectedAttributes[i])) {
                if (!first) {
                    text.append(" +\n");
                    json.append(",");
                } else {
                    first = false;
                }
                final String coef = Utils.doubleToString(m_Coefficients[column], 12, 4).trim();
                text.append(coef + " * ");
                text.append(m_TransformedData.attribute(i).name());
                final String[] tmp = m_TransformedData.attribute(i).name().split("=");
                final String attrName = tmp[0];
                json.append("{\"name\": \"" + attrName + "\"");
                json.append(",\"coef\": " + coef);
                if (tmp.length == 1) {
                    json.append(",\"values\": []");
                } else if (tmp.length == 2) {
                    final String attrValues = tmp[1];
                    final String[] attrValue = attrValues.split(",");
                    json.append(",\"values\": [");
                    for (int i1 = 0; i1 < attrValue.length; i1++) {
                        final String value = attrValue[i1];
                        if (i1 > 0) {
                            json.append(",");
                        }
                        json.append("\"" + value + "\"");
                    }
                    json.append("]");
                } else {
                    throw new Exception("tmp.length = " + tmp.length);
                }
                column++;
                json.append("}");
            }
        }
        json.append("]");
        json.append(",\"coef\": " + Utils.doubleToString(m_Coefficients[column], 12, 4).trim());
        json.append("}");
        text.append(" +\n" + Utils.doubleToString(m_Coefficients[column], 12, 4));
        //logger.info(json.toString());
        return json.toString();
    }
}
