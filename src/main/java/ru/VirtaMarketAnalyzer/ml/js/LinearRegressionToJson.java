package ru.VirtaMarketAnalyzer.ml.js;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.ml.LinearRegressionSummary;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.Utils;

import static ru.VirtaMarketAnalyzer.ml.js.ClassifierToJs.getPrivateFieldValue;

/**
 * Created by cobr123 on 09.03.2017.
 */
public final class LinearRegressionToJson {
    private static final Logger logger = LoggerFactory.getLogger(LinearRegressionToJson.class);

    public static String toJson(final LinearRegression classifier, final LinearRegressionSummary summary) throws Exception {
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

        text.append(m_TransformedData.classAttribute().name()).append(" =\n\n");

        json.append("{");
        json.append("\"name\": \"").append(m_TransformedData.classAttribute().name()).append("\"");
        json.append(",\"correlation_coefficient\": ").append(summary.getCorrelationCoefficient());
        json.append(",\"mean_absolute_error\": ").append(summary.getMeanAbsoluteError());
        json.append(",\"root_mean_squared_error\": ").append(summary.getRootMeanSquaredError());
        json.append(",\"relative_absolute_error\": ").append(summary.getRelativeAbsoluteError());
        json.append(",\"root_relative_squared_error\": ").append(summary.getRootRelativeSquaredError());
        json.append(",\"num_instances\": ").append(summary.getNumInstances());
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
                text.append(coef).append(" * ");
                text.append(m_TransformedData.attribute(i).name());
                final String[] tmp = m_TransformedData.attribute(i).name().split("=");
                final String attrName = tmp[0];
                json.append("{\"name\": \"").append(attrName).append("\"");
                json.append(",\"coef\": ").append(coef);
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
                        json.append("\"").append(value).append("\"");
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
        json.append(",\"coef\": ").append(Utils.doubleToString(m_Coefficients[column], 12, 4).trim());
        json.append("}");
        text.append(" +\n").append(Utils.doubleToString(m_Coefficients[column], 12, 4));
        //logger.info(json.toString());
        return json.toString();
    }
}
