package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class ShopProduct {
    @SerializedName("pi")
    final private String productId;
    @SerializedName("v")
    private final double sellVolume;
    @SerializedName("p")
    private final double price;
    @SerializedName("q")
    private final double quality;
    @SerializedName("b")
    private final double brand;
    @SerializedName("ms")
    private final double marketShare;

    public ShopProduct(String productId, double sellVolume, double price, double quality, double brand, double marketShare) {
        this.productId = productId;
        this.sellVolume = sellVolume;
        this.price = price;
        this.quality = quality;
        this.brand = brand;
        this.marketShare = marketShare;
    }

    public String getProductId() {
        return productId;
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

    public double getMarketShare() {
        return marketShare;
    }
}
