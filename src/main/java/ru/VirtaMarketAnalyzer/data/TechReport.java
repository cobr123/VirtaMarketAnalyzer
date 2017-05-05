package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 05.05.2017.
 */
final public class TechReport {
    @SerializedName("unit_type_id")
    final private String unitTypeID;
    @SerializedName("unit_type_symbol")
    final private String unitTypeSymbol;
    @SerializedName("unit_type_name")
    final private String unitTypeName;
    @SerializedName("level")
    final private int level;
    @SerializedName("price")
    final private double price;
    @SerializedName("status")
    final private int status;

    public TechReport(
            final String unitTypeID
            , final String unitTypeSymbol
            , final String unitTypeName
            , final int level
            , final double price
            , final int status
    ) {
        this.unitTypeID = unitTypeID;
        this.unitTypeSymbol = unitTypeSymbol;
        this.unitTypeName = unitTypeName;
        this.level = level;
        this.price = price;
        this.status = status;
    }

    public String getUnitTypeID() {
        return unitTypeID;
    }

    public String getUnitTypeSymbol() {
        return unitTypeSymbol;
    }

    public String getUnitTypeName() {
        return unitTypeName;
    }

    public int getLevel() {
        return level;
    }

    public double getPrice() {
        return price;
    }

    public int getStatus() {
        return status;
    }
}
