package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 21.03.2016.
 */
final public class TechAskBid {
    @SerializedName("p")
    final private double price;
    @SerializedName("q")
    final private int quantity;

    public TechAskBid(final Double price, final int quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
