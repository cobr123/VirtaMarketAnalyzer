package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cobr123 on 17.02.2019.
 */
final public class ServiceGuide {
    @SerializedName("ci")
    final private String countryId;
    @SerializedName("ri")
    final private String regionId;
    @SerializedName("ti")
    final private String townId;
    @SerializedName("ssi")
    final private String serviceSpecId;
    @SerializedName("sgp")
    final private List<ServiceGuideProduct> serviceGuideProducts;

    public ServiceGuide(final String serviceSpecId, final City city, final List<ServiceGuideProduct> serviceGuideProducts) {
        this.countryId = city.getCountryId();
        this.regionId = city.getRegionId();
        this.townId = city.getId();
        this.serviceSpecId = serviceSpecId;
        this.serviceGuideProducts = serviceGuideProducts;
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

    public List<ServiceGuideProduct> getServiceGuideProducts() {
        return serviceGuideProducts;
    }

    public String getServiceSpecId() {
        return serviceSpecId;
    }

    public String getGeo() {
        return getCountryId() + "/" + getRegionId() + "/" + getTownId();
    }
}
