package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class City {
    @SerializedName("ci")
    final private String countryId;
    @SerializedName("ri")
    final private String regionId;
    @SerializedName("i")
    final private String id;
    @SerializedName("c")
    final private String caption;
    @SerializedName("wi")
    final private double wealthIndex;

    public City(final String countryId, final String regionId, final String id, final String caption, final double wealthIndex) {
        this.countryId = countryId;
        this.regionId = regionId;
        this.id = id;
        this.caption = caption;
        this.wealthIndex = wealthIndex;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getId() {
        return id;
    }

    public String getCountryId() {
        return countryId;
    }

    public String getCaption() {
        return caption;
    }

    public double getWealthIndex() {
        return wealthIndex;
    }
}
