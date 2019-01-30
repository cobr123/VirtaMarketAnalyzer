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

    final private transient String unitId;
    final private transient String countryId;
    final private transient String regionId;
    final private transient String townId;
    final private transient String productId;

    public MajorSellInCity(final String productId, final String countryId, final String regionId, final String townId, final String unitId, final long shopSize, final String townDistrict, final double sellVolume, final double price, final double quality, final double brand) {
        this.productId = productId;
        this.countryId = countryId;
        this.regionId = regionId;
        this.townId = townId;
        this.unitId = unitId;
        this.shopSize = shopSize;
        this.townDistrict = townDistrict;
        this.sellVolume = sellVolume;
        this.price = price;
        this.quality = quality;
        this.brand = brand;
    }

    public String getProductId() {
        return productId;
    }

    public String getGeo() {
        return getCountryId() + "/" + getRegionId() + "/" + getTownId();
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

    public String getUnitId() {
        return unitId;
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
