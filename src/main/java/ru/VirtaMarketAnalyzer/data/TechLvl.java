package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 20.03.16.
 */
final public class TechLvl {
    @SerializedName("i")
    final private String techId;
    @SerializedName("l")
    final private int lvl;

    public TechLvl(final String techId, final int lvl) {
        this.techId = techId;
        this.lvl = lvl;
    }

    public String getTechId() {
        return techId;
    }

    public int getLvl() {
        return lvl;
    }
}
