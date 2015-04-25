package ru.VirtaMarketAnalyzer.data;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class City {
    final String countryId;
    final String regionId;
    final String id;
    final String caption;

    public City(final String countryId, final String regionId, final String id, final String caption) {
        this.countryId = countryId;
        this.regionId = regionId;
        this.id = id;
        this.caption = caption;
    }
}
