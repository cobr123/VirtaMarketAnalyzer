package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Country {
    @SerializedName("i")
    final private String id;
    @SerializedName("c")
    final private String caption;

    public Country(final String id, final String caption) {
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
