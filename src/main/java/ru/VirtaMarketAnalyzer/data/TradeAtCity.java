package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class TradeAtCity {
    @SerializedName("ci")
    final private String countryId;
    @SerializedName("ri")
    final private String regionId;
    @SerializedName("ti")
    final private String townId;
    @SerializedName("tc")
    final private String townCaption;
    @SerializedName("wi")
    final private double wealthIndex;
    @SerializedName("ei")
    final private double educationIndex;
    @SerializedName("as")
    final private double averageSalary;
    @SerializedName("pi")
    final private String productId;
    @SerializedName("mi")
    final private String marketIdx;
    @SerializedName("v")
    final private long volume;
    @SerializedName("sc")
    final private int sellerCnt;
    @SerializedName("cc")
    final private long companiesCnt;
    @SerializedName("lpe")
    final private double localPercent;
    @SerializedName("lpr")
    final private double localPrice;
    @SerializedName("lq")
    final private double localQuality;
    @SerializedName("spr")
    final private double shopPrice;
    @SerializedName("sq")
    final private double shopQuality;
    @SerializedName("sb")
    final private double shopBrand;
    @SerializedName("ms")
    final private List<MajorSellInCity> majorSellInCityList;
    @SerializedName("itr")
    final private double incomeTaxRate;
    @SerializedName("itp")
    final private int importTaxPercent;

    public TradeAtCity(
            final String countryId
            , final String regionId
            , final String townId
            , final String townCaption
            , final String productId
            , final String marketIdx
            , final long volume
            , final double wealthIndex
            , final double educationIndex
            , final double averageSalary
            , final int sellerCnt
            , final long companiesCnt
            , final double localPercent
            , final double localPrice
            , final double localQuality
            , final double shopPrice
            , final double shopQuality
            , final double shopBrand
            , final List<MajorSellInCity> majorSellInCityList
            , final double incomeTaxRate
            , final int importTaxPercent
    ) {
        this.countryId = countryId;
        this.regionId = regionId;
        this.townId = townId;
        this.townCaption = townCaption;
        this.productId = productId;
        this.marketIdx = marketIdx;
        this.volume = volume;
        this.wealthIndex = wealthIndex;
        this.educationIndex = educationIndex;
        this.averageSalary = averageSalary;
        this.sellerCnt = sellerCnt;
        this.companiesCnt = companiesCnt;
        this.localPercent = localPercent;
        this.localPrice = localPrice;
        this.localQuality = localQuality;
        this.shopPrice = shopPrice;
        this.shopQuality = shopQuality;
        this.shopBrand = shopBrand;
        this.majorSellInCityList = majorSellInCityList;
        this.incomeTaxRate = incomeTaxRate;
        this.importTaxPercent = importTaxPercent;
    }

    public String getCountryRegionTownIds() {
        return countryId + "|" + regionId + "|" + townId;
    }

    public String getTownCaption() {
        return townCaption;
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

    public String getTownId() {
        return townId;
    }

    public double getWealthIndex() {
        return wealthIndex;
    }

    public double getEducationIndex() {
        return educationIndex;
    }

    public double getAverageSalary() {
        return averageSalary;
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

    public int getSellerCnt() {
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

    public double getShopPrice() {
        return shopPrice;
    }

    public double getShopQuality() {
        return shopQuality;
    }

    public double getShopBrand() {
        return shopBrand;
    }

    public double getIncomeTaxRate() {
        return incomeTaxRate;
    }

    public int getImportTaxPercent() {
        return importTaxPercent;
    }
}
