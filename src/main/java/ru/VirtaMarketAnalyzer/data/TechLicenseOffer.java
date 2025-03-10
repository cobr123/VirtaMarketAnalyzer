package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

final public class TechLicenseOffer {
    @SerializedName("unit_type_id")
    final private String unitTypeID;
    @SerializedName("level")
    final private int level;
    @SerializedName("ask_count")
    final private int askCount;
    @SerializedName("bid_count")
    final private int bidCount;

    public TechLicenseOffer(String unitTypeID, int level, int askCount, int bidCount) {
        this.unitTypeID = unitTypeID;
        this.level = level;
        this.askCount = askCount;
        this.bidCount = bidCount;
    }

    public String getUnitTypeID() {
        return unitTypeID;
    }

    public int getLevel() {
        return level;
    }

    public int getAskCount() {
        return askCount;
    }

    public int getBidCount() {
        return bidCount;
    }
}
