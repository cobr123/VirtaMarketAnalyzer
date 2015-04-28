package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class ProductCategory {
    @SerializedName("c")
    final private String caption;

    public ProductCategory(final String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

}
