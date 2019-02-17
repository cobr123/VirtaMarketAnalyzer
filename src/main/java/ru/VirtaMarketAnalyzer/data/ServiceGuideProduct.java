package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cobr123 on 17.02.2019.
 */
final public class ServiceGuideProduct {
    @SerializedName("pi")
    final private String productId;
    @SerializedName("qps")
    final private double quantityPerSell;
    @SerializedName("q")
    final private double quality;
    @SerializedName("bp")
    final private double buyPrice;
    @SerializedName("sp")
    final private double sellPrice;
    @SerializedName("v")
    final private long volume;
    @SerializedName("sui")
    final private List<String> suppliersUnitIds;

    public ServiceGuideProduct(
            final String productId,
            final double quantityPerSell,
            final double quality,
            final double buyPrice,
            final double sellPrice,
            final long volume,
            final List<String> suppliersUnitIds
    ) {
        this.productId = productId;
        this.quantityPerSell = quantityPerSell;
        this.quality = quality;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.volume = volume;
        this.suppliersUnitIds = suppliersUnitIds;
    }

    public String getProductId() {
        return productId;
    }

    public double getQuality() {
        return quality;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public long getVolume() {
        return volume;
    }

    public List<String> getSuppliersUnitIds() {
        return suppliersUnitIds;
    }

    public double getQuantityPerSell() {
        return quantityPerSell;
    }
}
