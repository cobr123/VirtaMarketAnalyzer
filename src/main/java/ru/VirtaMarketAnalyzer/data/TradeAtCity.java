package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;
import ru.VirtaMarketAnalyzer.main.Utils;

import java.util.Date;
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
    @SerializedName("pci")
    final private String productCategoryId;
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
    @SerializedName("itr")
    final private double incomeTaxRate;
    @SerializedName("itp")
    final private int importTaxPercent;
    //Емкость рынка (по этому товару в городе)
    @SerializedName("lmvs")
    final private double localMarketVolumeSum;
    //Емкость игроков (по этому товару в городе)
    @SerializedName("smvs")
    final private double shopMarketVolumeSum;
    //Емкость рынка (по всем товарам в городе)
    @SerializedName("lmvst")
    final private double localMarketVolumeSumTotal;
    //Емкость игроков (по всем товарам в городе)
    @SerializedName("smvst")
    final private double shopMarketVolumeSumTotal;
    //Емкость, % = (Емкость игроков) / (Емкость рынка) * 100
    @SerializedName("pmvs")
    final private double percentMarketVolumeSum;
    //Емкость всего, % = (Емкость игроков всего) / (Емкость рынка всего) * 100
    @SerializedName("pmvst")
    final private double percentMarketVolumeSumTotal;

    final private transient List<MajorSellInCity> majorSellInCityList;
    transient private Date date;

    public TradeAtCity(
            final String countryId
            , final String regionId
            , final String townId
            , final String townCaption
            , final String productId
            , final String productCategoryId
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
            , final double localMarketVolumeSum
            , final double shopMarketVolumeSum
            , final double localMarketVolumeSumTotal
            , final double shopMarketVolumeSumTotal
    ) {
        this.countryId = countryId;
        this.regionId = regionId;
        this.townId = townId;
        this.townCaption = townCaption;
        this.productId = productId;
        this.productCategoryId = productCategoryId;
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
        this.localMarketVolumeSum = localMarketVolumeSum;
        this.shopMarketVolumeSum = shopMarketVolumeSum;
        this.localMarketVolumeSumTotal = localMarketVolumeSumTotal;
        this.shopMarketVolumeSumTotal = shopMarketVolumeSumTotal;
        //Емкость, % = (Емкость игроков) / (Емкость рынка) * 100
        this.percentMarketVolumeSum = Utils.round2(shopMarketVolumeSum / localMarketVolumeSum * 100.0);
        //Емкость всего, % = (Емкость игроков всего) / (Емкость рынка всего) * 100
        this.percentMarketVolumeSumTotal = Utils.round2(shopMarketVolumeSumTotal / localMarketVolumeSumTotal * 100.0);
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

    public String getProductCategoryId() {
        return productCategoryId;
    }

    public double getLocalMarketVolumeSum() {
        return localMarketVolumeSum;
    }

    public double getShopMarketVolumeSum() {
        return shopMarketVolumeSum;
    }

    public double getLocalMarketVolumeSumTotal() {
        return localMarketVolumeSumTotal;
    }

    public double getShopMarketVolumeSumTotal() {
        return shopMarketVolumeSumTotal;
    }

    public double getPercentMarketVolumeSum() {
        return percentMarketVolumeSum;
    }

    public double getPercentMarketVolumeSumTotal() {
        return percentMarketVolumeSumTotal;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }
}
