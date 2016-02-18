package ru.VirtaMarketAnalyzer.data;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * Created by r.tabulov on 18.02.2016.
 */
public final class HistAnalytExclStrat implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(final FieldAttributes fieldAttributes) {
        switch (fieldAttributes.getName().toLowerCase()) {
            case "productid":
            case "productcategory":
            case "localpercent":
            case "localprice":
            case "localquality":
                return true;
            default:
                return false;

        }
    }

    @Override
    public boolean shouldSkipClass(final Class<?> aClass) {
        return false;
    }
}
