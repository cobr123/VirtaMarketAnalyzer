package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 06.07.2016.
 */
public final class ServiceSpecRetail {
    @SerializedName("lpr")
    final private double localPrice;
    @SerializedName("lq")
    final private double localQuality;
    @SerializedName("spr")
    final private double shopPrice;
    @SerializedName("sq")
    final private double shopQuality;

    public ServiceSpecRetail(final double localPrice, final double localQuality, final double shopPrice, final double shopQuality) {
        //round up to two decimal points
        this.localPrice = Math.round(localPrice * 100.0) / 100.0;
        this.localQuality = Math.round(localQuality * 100.0) / 100.0;
        this.shopPrice = Math.round(shopPrice * 100.0) / 100.0;
        this.shopQuality = Math.round(shopQuality * 100.0) / 100.0;
    }

    public double getLocalPrice() {
        return localPrice;
    }

    public double getLocalQuality() {
        return localQuality;
    }

    public double getShopPrice() {
        return shopPrice;
    }

    public double getShopQuality() {
        return shopQuality;
    }
}
