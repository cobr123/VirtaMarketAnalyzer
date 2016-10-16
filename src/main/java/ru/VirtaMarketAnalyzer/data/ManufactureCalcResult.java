package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cobr123 on 16.10.2016.
 */
public final class ManufactureCalcResult {
    @SerializedName("pi")
    final private String productID;
    @SerializedName("v")
    final private long volume;
    @SerializedName("q")
    final private double quality;
    @SerializedName("c")
    final private double cost;

    public ManufactureCalcResult(
            final String productID
            , final long volume
            , final double quality
            , final double cost
    ) {
        this.productID = productID;
        this.volume = volume;
        this.quality = quality;
        this.cost = cost;
    }

    public String getProductID() {
        return productID;
    }

    public long getVolume() {
        return volume;
    }

    public double getQuality() {
        return quality;
    }

    public double getCost() {
        return cost;
    }
}
