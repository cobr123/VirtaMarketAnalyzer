package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 18.05.2015.
 */
final public class Manufacture {
    @SerializedName("i")
    final private String id;
    @SerializedName("mc")
    private String manufactureCategory;
    @SerializedName("c")
    final private String caption;

    public Manufacture(final String id,final String manufactureCategory,final String caption) {
        this.id = id;
        this.manufactureCategory = manufactureCategory;
        this.caption = caption;
    }

    public String getId() {
        return id;
    }

    public String getManufactureCategory() {
        return manufactureCategory;
    }

    public void setManufactureCategory(String manufactureCategory) {
        this.manufactureCategory = manufactureCategory;
    }

    public String getCaption() {
        return caption;
    }
}
