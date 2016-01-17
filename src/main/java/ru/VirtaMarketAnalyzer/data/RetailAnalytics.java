package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 16.01.16.
 */
public final class RetailAnalytics {
    @SerializedName("ss")
    private final int shopSize;
    @SerializedName("td")
    private final String townDistrict;
    @SerializedName("dc")
    private final double departmentCount;
    @SerializedName("n")
    private final double notoriety;
    @SerializedName("vc")
    private final int visitorsCount;
    @SerializedName("sl")
    final private String serviceLevel;
    @SerializedName("sv")
    private final double sellVolume;
    @SerializedName("p")
    private final double price;
    @SerializedName("q")
    private final double quality;
    @SerializedName("b")
    private final double brand;
    @SerializedName("wi")
    final private double wealthIndex;
    @SerializedName("ei")
    final private double educationIndex;
    @SerializedName("as")
    final private double averageSalary;
    @SerializedName("mi")
    final private String marketIdx;
    @SerializedName("mv")
    final private long marketVolume;
    @SerializedName("sc")
    final private long sellerCnt;
    @SerializedName("lpe")
    final private double localPercent;
    @SerializedName("lpr")
    final private double localPrice;
    @SerializedName("lq")
    final private double localQuality;

    public RetailAnalytics(int shopSize, String townDistrict, double departmentCount, double notoriety, int visitorsCount, String serviceLevel, double sellVolume, double price, double quality, double brand, double wealthIndex, double educationIndex, double averageSalary, String marketIdx, long marketVolume, long sellerCnt, double localPercent, double localPrice, double localQuality) {
        this.shopSize = shopSize;
        this.townDistrict = townDistrict;
        this.departmentCount = departmentCount;
        this.notoriety = notoriety;
        this.visitorsCount = visitorsCount;
        this.serviceLevel = serviceLevel;
        this.sellVolume = sellVolume;
        this.price = price;
        this.quality = quality;
        this.brand = brand;
        this.wealthIndex = wealthIndex;
        this.educationIndex = educationIndex;
        this.averageSalary = averageSalary;
        this.marketIdx = marketIdx;
        this.marketVolume = marketVolume;
        this.sellerCnt = sellerCnt;
        this.localPercent = localPercent;
        this.localPrice = localPrice;
        this.localQuality = localQuality;
    }

    public int getShopSize() {
        return shopSize;
    }

    public String getTownDistrict() {
        return townDistrict;
    }

    public double getDepartmentCount() {
        return departmentCount;
    }

    public double getNotoriety() {
        return notoriety;
    }

    public int getVisitorsCount() {
        return visitorsCount;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

    public double getSellVolume() {
        return sellVolume;
    }

    public double getPrice() {
        return price;
    }

    public double getQuality() {
        return quality;
    }

    public double getBrand() {
        return brand;
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

    public String getMarketIdx() {
        return marketIdx;
    }

    public long getMarketVolume() {
        return marketVolume;
    }

    public long getSellerCnt() {
        return sellerCnt;
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
}
