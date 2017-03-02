package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by cobr123 on 02.03.2017.
 */
public final class ProductRemainTrendSup {
    @SerializedName("ui")
    final private String unitID;
    @SerializedName("t")
    final private double total;
    @SerializedName("r")
    final private double remain;
    @SerializedName("q")
    final private double quality;
    @SerializedName("p")
    final private double price;
    @SerializedName("mot")
    final private ProductRemain.MaxOrderType maxOrderType;
    @SerializedName("mo")
    final private double maxOrder;

    public ProductRemainTrendSup(String unitID, double total, double remain, double quality, double price, ProductRemain.MaxOrderType maxOrderType, double maxOrder) {
        this.unitID = unitID;
        this.total = total;
        this.remain = remain;
        this.quality = quality;
        this.price = price;
        this.maxOrderType = maxOrderType;
        this.maxOrder = maxOrder;
    }

    public String getUnitID() {
        return unitID;
    }

    public double getTotal() {
        return total;
    }

    public double getRemain() {
        return remain;
    }

    public double getQuality() {
        return quality;
    }

    public double getPrice() {
        return price;
    }

    public ProductRemain.MaxOrderType getMaxOrderType() {
        return maxOrderType;
    }

    public double getMaxOrder() {
        return maxOrder;
    }
}
