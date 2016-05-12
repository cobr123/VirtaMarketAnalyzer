package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class MajorSellInCity {
    @SerializedName("s")
    private final long shopSize;
    @SerializedName("d")
    private final String townDistrict;
    @SerializedName("v")
    private final double sellVolume;
    @SerializedName("p")
    private final double price;
    @SerializedName("q")
    private final double quality;
    @SerializedName("b")
    private final double brand;

    final private transient String unitUrl;
    final private transient String countryId;
    final private transient String regionId;
    final private transient String townId;

    public MajorSellInCity(final String countryId, final String regionId, final String townId, final String unitUrl, final long shopSize, final String townDistrict, final double sellVolume, final double price, final double quality, final double brand) {
        this.countryId = countryId;
        this.regionId = regionId;
        this.townId = townId;
        this.unitUrl = unitUrl;
        this.shopSize = shopSize;
        this.townDistrict = townDistrict;
        this.sellVolume = sellVolume;
        this.price = price;
        this.quality = quality;
        this.brand = brand;
    }

    public String getCountryId() {
        return countryId;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getTownId() {
        return townId;
    }

    public String getUnitUrl() {
        return unitUrl;
    }

    public long getShopSize() {
        return shopSize;
    }

    public String getTownDistrict() {
        return townDistrict;
    }

    public double getSellVolume() {
        return sellVolume;
    }

    public double getPrice() {
        return price;
    }

    public double getQuality() {
        return quality;
    }

    public double getBrand() {
        return brand;
    }
}
