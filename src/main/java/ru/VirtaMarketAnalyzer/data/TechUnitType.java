package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 21.03.2016.
 */
final public class TechUnitType {
    @SerializedName("i")
    final private String id;
    @SerializedName("c")
    final private String caption;

    public TechUnitType(final String id, final String caption) {
        this.id = id;
        this.caption = caption;
    }

    public String getId() {
        return id;
    }

    public String getCaption() {
        return caption;
    }
}
