package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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
    @SerializedName("ms")
    final private List<ManufactureSize> sizes;

    public Manufacture(final String id,final String manufactureCategory,final String caption, final List<ManufactureSize> sizes) {
        this.id = id;
        this.manufactureCategory = manufactureCategory;
        this.caption = caption;
        this.sizes = sizes;
    }

    public String getId() {
        return id;
    }

    public String getManufactureCategory() {
        return manufactureCategory;
    }

    public void setManufactureCategory(final String manufactureCategory) {
        this.manufactureCategory = manufactureCategory;
    }

    public String getCaption() {
        return caption;
    }

    public List<ManufactureSize> getSizes() {
        return sizes;
    }
}
