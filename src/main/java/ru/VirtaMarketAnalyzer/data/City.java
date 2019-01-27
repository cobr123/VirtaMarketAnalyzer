package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class City {
    @SerializedName("ci")
    final private String countryId;
    @SerializedName("ri")
    final private String regionId;
    @SerializedName("i")
    final private String id;
    @SerializedName("c")
    final private String caption;
    @SerializedName("wi")
    final private double wealthIndex;
    @SerializedName("ei")
    final private double educationIndex;
    @SerializedName("as")
    final private double averageSalary;
    @SerializedName("d")
    final private int demography;
    @SerializedName("p")
    final private int population;
    //названия спонсируемых мэром продуктовых категорий
    @SerializedName("mb")
    final private List<String> mayoralBonuses;

    public City(final String countryId, final String regionId
            , final String id, final String caption
            , final double wealthIndex, final double educationIndex
            , final double averageSalary, final int demography
            , final int population, final List<String> mayoralBonuses
    ) {
        this.countryId = countryId;
        this.regionId = regionId;
        this.id = id;
        this.caption = caption;
        this.wealthIndex = wealthIndex;
        this.educationIndex = educationIndex;
        this.averageSalary = averageSalary;
        this.demography = demography;
        this.population = population;
        this.mayoralBonuses = mayoralBonuses;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getId() {
        return id;
    }

    public String getCountryId() {
        return countryId;
    }

    public String getCaption() {
        return caption;
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

    public int getDemography() {
        return demography;
    }

    public int getPopulation() {
        return population;
    }

    public String getGeo() {
        return getCountryId() + "/" + getRegionId() + "/" + getId();
    }
}
