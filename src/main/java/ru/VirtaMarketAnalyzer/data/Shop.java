package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cobr123 on 16.01.16.
 */
public final class Shop {
    @SerializedName("ci")
    final private String countryId;
    @SerializedName("ri")
    final private String regionId;
    @SerializedName("ti")
    final private String townId;
    @SerializedName("ss")
    private final int shopSize;
    @SerializedName("td")
    private final String townDistrict;
    @SerializedName("dc")
    private final int departmentCount;
    @SerializedName("n")
    private final double notoriety;
    @SerializedName("vc")
    private final String visitorsCount;
    @SerializedName("sl")
    final private String serviceLevel;
    @SerializedName("sp")
    final private List<ShopProduct> shopProducts;

    public Shop(final String countryId, final String regionId, final String townId,
                final int shopSize, final String townDistrict, final int departmentCount,
                final double notoriety, final String visitorsCount, final String serviceLevel,
                final List<ShopProduct> shopProducts) {
        this.countryId = countryId;
        this.regionId = regionId;
        this.townId = townId;
        this.shopSize = shopSize;
        this.townDistrict = townDistrict;
        this.departmentCount = departmentCount;
        this.notoriety = notoriety;
        this.visitorsCount = visitorsCount;
        this.serviceLevel = serviceLevel;
        this.shopProducts = shopProducts;
    }

    public String getGeo() {
        return getCountryId() + "/" + getRegionId() + "/" + getTownId();
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

    public int getShopSize() {
        return shopSize;
    }

    public String getTownDistrict() {
        return townDistrict;
    }

    public int getDepartmentCount() {
        return departmentCount;
    }

    public double getNotoriety() {
        return notoriety;
    }

    public String getVisitorsCount() {
        return visitorsCount;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

    public List<ShopProduct> getShopProducts() {
        return shopProducts;
    }
}
