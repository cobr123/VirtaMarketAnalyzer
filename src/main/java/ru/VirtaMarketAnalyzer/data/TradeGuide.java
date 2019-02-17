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

    public TradeGuide(final City city, final List<TradeGuideProduct> tradeGuideProduct) {
        this.countryId = city.getCountryId();
        this.regionId = city.getRegionId();
        this.townId = city.getId();
        this.tradeGuideProduct = tradeGuideProduct;
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

    public List<TradeGuideProduct> getTradeGuideProduct() {
        return tradeGuideProduct;
    }

    public String getGeo() {
        return getCountryId() + "/" + getRegionId() + "/" + getTownId();
    }
}
