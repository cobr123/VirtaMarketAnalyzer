package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by r.tabulov on 16.10.2016.
 */
public final class ProductionAboveAverage {
    @SerializedName("mi")
    final private String manufactureID;
    @SerializedName("s")
    final private String specialization;
    @SerializedName("pi")
    final private String productID;
    @SerializedName("v")
    final private long volume;
    @SerializedName("q")
    final private double quality;
    @SerializedName("c")
    final private double cost;
    @SerializedName("ir")
    final private List<ProductRemain> ingredientsRemain;
    @SerializedName("tl")
    final private int techLvl;

    public ProductionAboveAverage(final String manufactureID
            , final String specialization
            , final String productID
            , final long volume
            , final double quality
            , final double cost
            , final List<ProductRemain> ingredientsRemain
            , final int techLvl
    ) {
        this.manufactureID = manufactureID;
        this.specialization = specialization;
        this.productID = productID;
        this.volume = volume;
        this.quality = quality;
        this.cost = cost;
        this.ingredientsRemain = ingredientsRemain;
        this.techLvl = techLvl;
    }
}
