package ru.VirtaMarketAnalyzer.ml;

/**
 * Created by cobr123 on 16.03.2017.
 */
public final class LinearRegressionSummary {
        private final String productID;
        private final double correlationCoefficient;
        private final double meanAbsoluteError;
        private final double rootMeanSquaredError;
        private final double relativeAbsoluteError;
        private final double rootRelativeSquaredError;
        private final double numInstances;

    public LinearRegressionSummary(String productID, double correlationCoefficient, double meanAbsoluteError, double rootMeanSquaredError, double relativeAbsoluteError, double rootRelativeSquaredError, double numInstances) {
        this.productID = productID;
        this.correlationCoefficient = correlationCoefficient;
        this.meanAbsoluteError = meanAbsoluteError;
        this.rootMeanSquaredError = rootMeanSquaredError;
        this.relativeAbsoluteError = relativeAbsoluteError;
        this.rootRelativeSquaredError = rootRelativeSquaredError;
        this.numInstances = numInstances;
    }

    public String getProductID() {
        return productID;
    }

    public double getCorrelationCoefficient() {
        return correlationCoefficient;
    }

    public double getMeanAbsoluteError() {
        return meanAbsoluteError;
    }

    public double getRootMeanSquaredError() {
        return rootMeanSquaredError;
    }

    public double getRelativeAbsoluteError() {
        return relativeAbsoluteError;
    }

    public double getRootRelativeSquaredError() {
        return rootRelativeSquaredError;
    }

    public double getNumInstances() {
        return numInstances;
    }
}
