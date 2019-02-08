package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cobr123 on 08.02.2019.
 */
final public class TradeGuide {
    @SerializedName("ci")
    final private String countryId;
    @SerializedName("ri")
    final private String regionId;
    @SerializedName("ti")
    final private String townId;
    @SerializedName("tgp")
    final private List<TradeGuideProduct> tradeGuideProduct;

    public TradeGuide(
            final String countryId,
            final String regionId,
            final String townId,
            final List<TradeGuideProduct> tradeGuideProduct
    ) {
        this.countryId = countryId;
        this.regionId = regionId;
        this.townId = townId;
        this.tradeGuideProduct = tradeGuideProduct;
    }
}
