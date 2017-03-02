package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    @SerializedName("s")
    final private List<ProductRemainTrendSup> sup;

    final private transient Date date;
    final public static transient DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public ProductRemainTrend(
            final double remain,
            final double quality,
            final double price,
            final Date date,
            final List<ProductRemainTrendSup> sup
    ) {
        this.date = date;
        this.dateStr = dateFormat.format(date);
        this.remain = remain;
        this.quality = quality;
        this.price = price;
        this.sup = sup;
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

    public List<ProductRemainTrendSup> getSup() {
        return sup;
    }
}
