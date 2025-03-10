package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

final public class TechLicenseLvlPrice {
    @SerializedName("ask_count")
    final private int askCount;
    @SerializedName("bid_count")
    final private int bidCount;
    @SerializedName("price")
    final private double price;

    public TechLicenseLvlPrice(int askCount, int bidCount, double price) {
        this.askCount = askCount;
        this.bidCount = bidCount;
        this.price = price;
    }

    public int getAskCount() {
        return askCount;
    }

    public int getBidCount() {
        return bidCount;
    }

    public double getPrice() {
        return price;
    }
}
