package ru.VirtaMarketAnalyzer.data;

import java.util.List;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class TradeAtCity {
    final private String countryId;
    final private String regionId;
    final private String cityId;
    final private double wealthIndex;
    final private String productId;
    final private String marketIdx;
    final private long volume;
    final private long sellerCnt;
    final private long companiesCnt;
    final private double localPercent;
    final private double localPrice;
    final private double localQuality;
    final private double localBrand;
    final private double shopPercent;
    final private double shopPrice;
    final private double shopQuality;
    final private double shopBrand;
    final private List<MajorSellInCity> majorSellInCityList;

    public TradeAtCity(
            final String countryId
            , final String regionId
            , final String cityId
            , final String productId
            , final String marketIdx
            , final long volume
            , final double wealthIndex
            , final long sellerCnt
            , final long companiesCnt
            , final double localPercent
            , final double localPrice
            , final double localQuality
            , final double localBrand
            , final double shopPercent
            , final double shopPrice
            , final double shopQuality
            , final double shopBrand
            , final List<MajorSellInCity> majorSellInCityList
    ) {
        this.countryId = countryId;
        this.regionId = regionId;
        this.cityId = cityId;
        this.productId = productId;
        this.marketIdx = marketIdx;
        this.volume = volume;
        this.wealthIndex = wealthIndex;
        this.sellerCnt = sellerCnt;
        this.companiesCnt = companiesCnt;
        this.localPercent = localPercent;
        this.localPrice = localPrice;
        this.localQuality = localQuality;
        this.localBrand = localBrand;
        this.shopPercent = shopPercent;
        this.shopPrice = shopPrice;
        this.shopQuality = shopQuality;
        this.shopBrand = shopBrand;
        this.majorSellInCityList = majorSellInCityList;
    }

    public List<MajorSellInCity> getMajorSellInCityList() {
        return majorSellInCityList;
    }

    public String getCountryId() {
        return countryId;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getCityId() {
        return cityId;
    }

    public double getWealthIndex() {
        return wealthIndex;
    }

    public String getProductId() {
        return productId;
    }

    public String getMarketIdx() {
        return marketIdx;
    }

    public long getVolume() {
        return volume;
    }

    public long getSellerCnt() {
        return sellerCnt;
    }

    public long getCompaniesCnt() {
        return companiesCnt;
    }

    public double getLocalPercent() {
        return localPercent;
    }

    public double getLocalPrice() {
        return localPrice;
    }

    public double getLocalQuality() {
        return localQuality;
    }

    public double getLocalBrand() {
        return localBrand;
    }

    public double getShopPercent() {
        return shopPercent;
    }

    public double getShopPrice() {
        return shopPrice;
    }

    public double getShopQuality() {
        return shopQuality;
    }

    public double getShopBrand() {
        return shopBrand;
    }
}
