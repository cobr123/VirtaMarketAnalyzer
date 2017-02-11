package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cobr123 on 09.02.2017.
 */
public final class RetailTrend {
    @SerializedName("d")
    final private String dateStr;
    @SerializedName("lpr")
    final private double localPrice;
    @SerializedName("lq")
    final private double localQuality;
    @SerializedName("spr")
    final private double shopPrice;
    @SerializedName("sq")
    final private double shopQuality;
    @SerializedName("v")
    final private double volume;

    final private transient Date date;
    final public static transient DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public RetailTrend(
            final double localPrice,
            final double localQuality,
            final double shopPrice,
            final double shopQuality,
            final Date date,
            final double volume
    ) {
        this.date = date;
        this.dateStr = dateFormat.format(date);
        this.localPrice = localPrice;
        this.localQuality = localQuality;
        this.shopPrice = shopPrice;
        this.shopQuality = shopQuality;
        this.volume = volume;
    }

    public double getLocalPrice() {
        return localPrice;
    }

    public double getLocalQuality() {
        return localQuality;
    }

    public double getShopPrice() {
        return shopPrice;
    }

    public double getShopQuality() {
        return shopQuality;
    }

    public double getVolume() {
        return volume;
    }

    public Date getDate() {
        return date;
    }

}
