package ru.VirtaMarketAnalyzer.data;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class City {
    final private String countryId;
    final private String regionId;
    final private String id;
    final private String caption;
    private double wealthIndex;

    public City(final String countryId, final String regionId, final String id, final String caption) {
        this.countryId = countryId;
        this.regionId = regionId;
        this.id = id;
        this.caption = caption;
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

    public void setWealthIndex(final double wealthIndex) {
        this.wealthIndex = wealthIndex;
    }
}
