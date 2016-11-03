package ru.VirtaMarketAnalyzer.data;

import java.util.List;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class TradeAtCityBuilder {
    private String countryId;
    private String regionId;
    private String cityId;
    private String cityCaption;
    private double wealthIndex;
    private double educationIndex;
    private double averageSalary;
    private String productId;
    private String productCategoryId;
    private String marketIdx;
    private long volume;
    private int sellerCnt;
    private long companiesCnt;
    private double localPercent;
    private double localPrice;
    private double localQuality;
    private double shopPrice;
    private double shopQuality;
    private double shopBrand;
    private List<MajorSellInCity> majorSellInCityList;
    private double incomeTaxRate;
    private int importTaxPercent;
    private double localMarketVolumeSumTotal;
    private double shopMarketVolumeSumTotal;

    public TradeAtCity build() {
        return new TradeAtCity(
                countryId
                , regionId
                , cityId
                , cityCaption
                , productId
                , productCategoryId
                , marketIdx
                , volume
                , wealthIndex
                , educationIndex
                , averageSalary
                , sellerCnt
                , companiesCnt
                , localPercent
                , localPrice
                , localQuality
                , shopPrice
                , shopQuality
                , shopBrand
                , majorSellInCityList
                , incomeTaxRate
                , importTaxPercent
                , getLocalMarketVolumeSum()
                , getShopMarketVolumeSum()
                , localMarketVolumeSumTotal
                , shopMarketVolumeSumTotal
        );
    }

    public TradeAtCityBuilder setIncomeTaxRate(final double incomeTaxRate) {
        this.incomeTaxRate = incomeTaxRate;
        return this;
    }

    public TradeAtCityBuilder setImportTaxPercent(final int importTaxPercent) {
        this.importTaxPercent = importTaxPercent;
        return this;
    }

    public TradeAtCityBuilder setCityCaption(final String cityCaption) {
        this.cityCaption = cityCaption;
        return this;
    }

    public TradeAtCityBuilder setMajorSellInCityList(final List<MajorSellInCity> majorSellInCityList) {
        this.majorSellInCityList = majorSellInCityList;
        return this;
    }

    public TradeAtCityBuilder setCountryId(final String countryId) {
        this.countryId = countryId;
        return this;
    }

    public TradeAtCityBuilder setRegionId(final String regionId) {
        this.regionId = regionId;
        return this;
    }

    public TradeAtCityBuilder setCityId(final String cityId) {
        this.cityId = cityId;
        return this;
    }

    public TradeAtCityBuilder setWealthIndex(final double wealthIndex) {
        this.wealthIndex = wealthIndex;
        return this;
    }

    public TradeAtCityBuilder setEducationIndex(final double educationIndex) {
        this.educationIndex = educationIndex;
        return this;
    }

    public TradeAtCityBuilder setAverageSalary(final double averageSalary) {
        this.averageSalary = averageSalary;
        return this;
    }

    public TradeAtCityBuilder setProductId(final String productId) {
        this.productId = productId;
        return this;
    }

    public TradeAtCityBuilder setMarketIdx(final String marketIdx) {
        this.marketIdx = marketIdx;
        return this;
    }

    public TradeAtCityBuilder setVolume(final long volume) {
        this.volume = volume;
        return this;
    }

    public TradeAtCityBuilder setSellerCnt(final int sellerCnt) {
        this.sellerCnt = sellerCnt;
        return this;
    }

    public TradeAtCityBuilder setCompaniesCnt(final long companiesCnt) {
        this.companiesCnt = companiesCnt;
        return this;
    }

    public TradeAtCityBuilder setLocalPercent(final double localPercent) {
        this.localPercent = localPercent;
        return this;
    }

    public TradeAtCityBuilder setLocalPrice(final double localPrice) {
        this.localPrice = localPrice;
        return this;
    }

    public TradeAtCityBuilder setLocalQuality(final double localQuality) {
        this.localQuality = localQuality;
        return this;
    }

    public TradeAtCityBuilder setShopPrice(final double shopPrice) {
        this.shopPrice = shopPrice;
        return this;
    }

    public TradeAtCityBuilder setShopQuality(final double shopQuality) {
        this.shopQuality = shopQuality;
        return this;
    }

    public TradeAtCityBuilder setShopBrand(final double shopBrand) {
        this.shopBrand = shopBrand;
        return this;
    }

    public TradeAtCityBuilder setLocalMarketVolumeSumTotal(final double localMarketVolumeSumTotal) {
        this.localMarketVolumeSumTotal = Math.round(localMarketVolumeSumTotal);
        return this;
    }

    public TradeAtCityBuilder setShopMarketVolumeSumTotal(final double shopMarketVolumeSumTotal) {
        this.shopMarketVolumeSumTotal = Math.round(shopMarketVolumeSumTotal);
        return this;
    }

    public TradeAtCityBuilder setProductCategoryId(final String productCategoryId) {
        this.productCategoryId = productCategoryId;
        return this;
    }

    public String getProductCategoryId() {
        return productCategoryId;
    }

    public double getShopMarketVolumeSum() {
        final double price = (shopPrice - localPercent * localPrice / 100.0) / (100.0 - localPercent) * 100.0;
        return Math.round(volume * (100.0 - localPercent) * price / 100.0);
    }

    public double getLocalMarketVolumeSum() {
        return Math.round(volume * localPrice);
    }

    public String getCityId() {
        return cityId;
    }
}
