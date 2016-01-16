package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cobr123 on 16.01.16.
 */
public final class Shop {
    @SerializedName("ui")
    final private String unitId;
    @SerializedName("ci")
    final private String countryId;
    @SerializedName("ri")
    final private String regionId;
    @SerializedName("ti")
    final private String townId;
    @SerializedName("s")
    private final int shopSize;
    @SerializedName("d")
    private final String townDistrict;
    @SerializedName("dc")
    private final int departmentCount;
    @SerializedName("n")
    private final double notoriety;
    @SerializedName("vc")
    private final int visitorsCount;
    @SerializedName("sl")
    final private String serviceLevel;
    @SerializedName("sp")
    final private List<ShopProduct> shopProducts;

    public Shop(String unitId, String countryId, String regionId, String townId, int shopSize, String townDistrict, int departmentCount, double notoriety, int visitorsCount, String serviceLevel, List<ShopProduct> shopProducts) {
        this.unitId = unitId;
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

    public String getUnitId() {
        return unitId;
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

    public int getVisitorsCount() {
        return visitorsCount;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

    public List<ShopProduct> getShopProducts() {
        return shopProducts;
    }
}
