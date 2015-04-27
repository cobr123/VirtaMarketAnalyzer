package ru.VirtaMarketAnalyzer.data;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Region {
    final private String countryId;
    final private String id;
    final private String caption;

    public Region(final String countryId, final String id, final String caption) {
        this.countryId = countryId;
        this.id = id;
        this.caption = caption;
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

}
