package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class ProductCategory {
    @SerializedName("i")
    final private String id;
    @SerializedName("c")
    final private String caption;

    public ProductCategory(final String id, final String caption) {
        this.id = id;
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public String getId() {
        return id;
    }
}
