package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cobr123 on 13.02.2017.
 */
public final class ProductRemainTrend {
    @SerializedName("d")
    final private String dateStr;
    @SerializedName("r")
    final private double remain;
    @SerializedName("q")
    final private double quality;
    @SerializedName("p")
    final private double price;

    final private transient Date date;
    final public static transient DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public ProductRemainTrend(
            final double remain,
            final double quality,
            final double price,
            final Date date
    ) {
        this.date = date;
        this.dateStr = dateFormat.format(date);
        this.remain = remain;
        this.quality = quality;
        this.price = price;
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

    public Date getDate() {
        return date;
    }

}
