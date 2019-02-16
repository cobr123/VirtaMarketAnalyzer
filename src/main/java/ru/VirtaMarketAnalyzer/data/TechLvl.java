package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 17.10.16.
 */
final public class TechLvl {
    @SerializedName("i")
    final private String techId;
    @SerializedName("l")
    final private int lvl;
    @SerializedName("p")
    final private double price;

    public TechLvl(final String techId, final int lvl, final double price) {
        this.techId = techId;
        this.lvl = lvl;
        this.price = price;
    }

    public String getTechId() {
        return techId;
    }

    public int getLvl() {
        return lvl;
    }

    public double getPrice() {
        return price;
    }
}
