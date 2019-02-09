package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 08.02.2019.
 */
final public class TradeGuideProduct {
    @SerializedName("pi")
    final private String productId;
    @SerializedName("q")
    final private double quality;
    @SerializedName("bp")
    final private double buyPrice;
    @SerializedName("sp")
    final private double sellPrice;
    @SerializedName("v")
    final private long volume;
    @SerializedName("iat")
    final private double incomeAfterTax;

    public TradeGuideProduct(
            final String productId,
            final double quality,
            final double buyPrice,
            final double sellPrice,
            final long volume,
            final double incomeAfterTax
    ) {
        this.productId = productId;
        this.quality = quality;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.volume = volume;
        this.incomeAfterTax = incomeAfterTax;
    }
}
