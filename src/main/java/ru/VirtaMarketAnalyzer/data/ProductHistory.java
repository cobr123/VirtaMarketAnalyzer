package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 16.10.2016.
 */
public final class ProductHistory {
    @SerializedName("pi")
    final private String productID;
    @SerializedName("vp")
    final private long volumeProd;
    @SerializedName("vc")
    final private long volumeCons;
    @SerializedName("q")
    final private double quality;
    @SerializedName("c")
    final private double cost;
    @SerializedName("av")
    final private double assessedValue;

    public ProductHistory(
            final String productID
            , final long volumeProd
            , final long volumeCons
            , final double quality
            , final double cost
            , final double assessedValue
    ) {
        this.productID = productID;
        this.volumeProd = volumeProd;
        this.volumeCons = volumeCons;
        this.quality = quality;
        this.cost = cost;
        this.assessedValue = assessedValue;
    }

    public String getProductID() {
        return productID;
    }

    public long getVolumeProd() {
        return volumeProd;
    }

    public long getVolumeCons() {
        return volumeCons;
    }

    public double getQuality() {
        return quality;
    }

    public double getCost() {
        return cost;
    }

    public double getAssessedValue() {
        return assessedValue;
    }
}
