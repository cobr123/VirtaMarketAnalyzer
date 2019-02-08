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
    @SerializedName("p")
    final private double price;
    @SerializedName("v")
    final private long volume;
    @SerializedName("iat")
    final private double incomeAfterTax;

    public TradeGuideProduct(
            final String productId,
            final double quality,
            final double price,
            final long volume,
            final double incomeAfterTax
    ) {
        this.productId = productId;
        this.quality = quality;
        this.price = price;
        this.volume = volume;
        this.incomeAfterTax = incomeAfterTax;
    }
}
