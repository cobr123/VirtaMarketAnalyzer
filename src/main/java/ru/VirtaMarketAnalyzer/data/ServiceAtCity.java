package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by cobr123 on 25.02.16.
 */
public final class ServiceAtCity {
    @SerializedName("ci")
    final private String countryId;
    @SerializedName("ri")
    final private String regionId;
    @SerializedName("ti")
    final private String townId;
    @SerializedName("v")
    final private long volume;
    @SerializedName("p")
    final private double price;
    @SerializedName("sc")
    final private int subdivisionsCnt;
    @SerializedName("cc")
    final private long companiesCnt;
    @SerializedName("mdi")
    final private double marketDevelopmentIndex;
    @SerializedName("pbs")
    final private Map<String, Double> percentBySpec;
    /*spec, prodictID, stats*/
    @SerializedName("rbs")
    final private Map<String, Map<String, ServiceSpecRetail>> retailBySpec;
    /*spec, prodictID, stats calc*/
    @SerializedName("cbs")
    final private Map<String, ServiceSpecRetail> retailCalcBySpec;
    @SerializedName("wi")
    final private double wealthIndex;
    @SerializedName("itr")
    final private double incomeTaxRate;
    @SerializedName("ar")
    final private double areaRent;


    public ServiceAtCity(final String countryId, final String regionId, final String townId,
                         final long volume, final double price, final int subdivisionsCnt, final long companiesCnt,
                         final double marketDevelopmentIndex, final Map<String, Double> percentBySpec,
                         final double wealthIndex, final double incomeTaxRate,
                         final Map<String, Map<String, ServiceSpecRetail>> retailBySpec,
                         final Map<String, ServiceSpecRetail> retailCalcBySpec,
                         final double areaRent
    ) {
        this.countryId = countryId;
        this.regionId = regionId;
        this.townId = townId;
        this.volume = volume;
        this.price = price;
        this.subdivisionsCnt = subdivisionsCnt;
        this.companiesCnt = companiesCnt;
        this.marketDevelopmentIndex = marketDevelopmentIndex;
        this.percentBySpec = percentBySpec;
        this.wealthIndex = wealthIndex;
        this.incomeTaxRate = incomeTaxRate;
        this.retailBySpec = retailBySpec;
        this.retailCalcBySpec = retailCalcBySpec;
        this.areaRent = areaRent;
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

    public long getVolume() {
        return volume;
    }

    public double getPrice() {
        return price;
    }

    public int getSubdivisionsCnt() {
        return subdivisionsCnt;
    }

    public long getCompaniesCnt() {
        return companiesCnt;
    }

    public double getMarketDevelopmentIndex() {
        return marketDevelopmentIndex;
    }

    public Map<String, Double> getPercentBySpec() {
        return percentBySpec;
    }

    public double getWealthIndex() {
        return wealthIndex;
    }

    public double getIncomeTaxRate() {
        return incomeTaxRate;
    }
}
