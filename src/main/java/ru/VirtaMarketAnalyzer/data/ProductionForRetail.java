package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;
import ru.VirtaMarketAnalyzer.main.Utils;

import java.util.List;

/**
 * Created by cobr123 on 14.02.2019.
 */
final public class ProductionForRetail {
    @SerializedName("ci")
    final private String countryId;
    @SerializedName("ri")
    final private String regionId;
    @SerializedName("ti")
    final private String townId;
    @SerializedName("mi")
    final private String manufactureID;
    @SerializedName("s")
    final private String specialization;
    @SerializedName("pi")
    final private String productID;
    @SerializedName("v")
    final private long volume;
    @SerializedName("q")
    final private double quality;
    @SerializedName("c")
    final private double cost;
    @SerializedName("sp")
    final private double sellPrice;
    @SerializedName("tl")
    final private double techLvl;
    @SerializedName("ir")
    final private List<ProductRemain> ingredientsRemain;
    @SerializedName("ctm")
    final private boolean cheaperThenMarket;

    public ProductionForRetail(final TradeAtCity stat, final ProductionAboveAverage paa) {
        this.manufactureID = paa.getManufactureID();
        this.specialization = paa.getSpecialization();
        this.productID = paa.getProductID();
        this.volume = paa.getVolume();
        this.quality = paa.getQuality();
        this.cost = paa.getCost();
        this.techLvl = paa.getTechLvl();
        this.ingredientsRemain = paa.getIngredientsRemain();
        this.cheaperThenMarket = paa.isCheaperThenMarket();

        this.countryId = stat.getCountryId();
        this.regionId = stat.getRegionId();
        this.townId = stat.getTownId();

        if (quality - 30.0 > stat.getLocalQuality()) {
            sellPrice = Utils.round2(stat.getLocalPrice() * 2.5);
        } else if (quality - 20.0 > stat.getLocalQuality()) {
            sellPrice = Utils.round2(stat.getLocalPrice() * 2.0);
        } else if (quality - 10.0 > stat.getLocalQuality()) {
            sellPrice = Utils.round2(stat.getLocalPrice() * 1.5);
        } else {
            sellPrice = stat.getLocalPrice();
        }
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

    public String getGeo() {
        return getCountryId() + "/" + getRegionId() + "/" + getTownId();
    }

    public String getManufactureID() {
        return manufactureID;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getProductID() {
        return productID;
    }

    public long getVolume() {
        return volume;
    }

    public double getQuality() {
        return quality;
    }

    public double getCost() {
        return cost;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public double getTechLvl() {
        return techLvl;
    }

    public List<ProductRemain> getIngredientsRemain() {
        return ingredientsRemain;
    }

    public boolean isCheaperThenMarket() {
        return cheaperThenMarket;
    }
}
