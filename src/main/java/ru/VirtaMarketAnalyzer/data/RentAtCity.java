package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 11.12.16.
 */
public final class RentAtCity {
    @SerializedName("utis")
    final private String unitTypeImgSrc;
    @SerializedName("ci")
    final private String cityId;
    //Rent 1 thousands m2
    @SerializedName("ar")
    final private double areaRent;
    //Office rent for one employee
    @SerializedName("wr")
    final private double workplaceRent;

    public RentAtCity(final String unitTypeImgSrc, final String cityId, final double areaRent, final double workplaceRent) {
        this.unitTypeImgSrc = unitTypeImgSrc;
        this.cityId = cityId;
        this.areaRent = areaRent;
        this.workplaceRent = workplaceRent;
    }

    public String getUnitTypeImgSrc() {
        return unitTypeImgSrc;
    }

    public String getCityId() {
        return cityId;
    }

    public double getAreaRent() {
        return areaRent;
    }

    public double getWorkplaceRent() {
        return workplaceRent;
    }
}
