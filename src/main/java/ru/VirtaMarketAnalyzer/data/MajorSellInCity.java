package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class MajorSellInCity {
    @SerializedName("s")
    private final long shopSize;
    @SerializedName("d")
    private final String townDistrict;
    @SerializedName("v")
    private final long sellVolume;
    @SerializedName("p")
    private final double price;
    @SerializedName("q")
    private final double quality;
    @SerializedName("b")
    private final double brand;

    public MajorSellInCity(final long shopSize, final String townDistrict, final long sellVolume, final double price, final double quality, final double brand) {
        this.shopSize = shopSize;
        this.townDistrict = townDistrict;
        this.sellVolume = sellVolume;
        this.price = price;
        this.quality = quality;
        this.brand = brand;
    }


    public long getShopSize() {
        return shopSize;
    }

    public String getTownDistrict() {
        return townDistrict;
    }

    public long getSellVolume() {
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
