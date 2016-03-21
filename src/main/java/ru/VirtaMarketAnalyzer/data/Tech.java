package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 21.03.2016.
 */
final public class Tech {
    @SerializedName("i")
    final private String techId;
    @SerializedName("c")
    final private String caption;

    public Tech(final String techId, final String caption) {
        this.techId = techId;
        this.caption = caption;
    }

    public String getTechId() {
        return techId;
    }

    public String getCaption() {
        return caption;
    }
}
