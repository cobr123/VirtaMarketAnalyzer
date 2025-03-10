package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 21.03.2016.
 */
final public class TechLicenseAskBid {
    @SerializedName("p")
    final private double price;
    @SerializedName("q")
    final private int quantity;

    public TechLicenseAskBid(final double price, final int quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
