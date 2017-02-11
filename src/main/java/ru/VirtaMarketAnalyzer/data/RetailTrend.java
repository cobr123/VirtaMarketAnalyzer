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
    //Емкость рынка (по этому товару в городе)
    @SerializedName("lmvs")
    final private double localMarketVolumeSum;
    //Емкость игроков (по этому товару в городе)
    @SerializedName("smvs")
    final private double shopMarketVolumeSum;
    //Емкость рынка (по всем товарам в городе)
    @SerializedName("lmvst")
    final private double localMarketVolumeSumTotal;
    //Емкость игроков (по всем товарам в городе)
    @SerializedName("smvst")
    final private double shopMarketVolumeSumTotal;
    //Емкость, % = (Емкость игроков) / (Емкость рынка) * 100
    @SerializedName("pmvs")
    final private double percentMarketVolumeSum;
    //Емкость всего, % = (Емкость игроков всего) / (Емкость рынка всего) * 100
    @SerializedName("pmvst")
    final private double percentMarketVolumeSumTotal;

    final private transient Date date;
    final private transient double weigh;
    final public static transient DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public RetailTrend(
            final double localPrice,
            final double localQuality,
            final double shopPrice,
            final double shopQuality,
            final Date date,
            final double volume,
            final double weigh,
            final double localMarketVolumeSum,
            final double shopMarketVolumeSum,
            final double localMarketVolumeSumTotal,
            final double shopMarketVolumeSumTotal,
            final double percentMarketVolumeSum,
            final double percentMarketVolumeSumTotal
    ) {
        this.date = date;
        this.dateStr = dateFormat.format(date);
        this.localPrice = localPrice;
        this.localQuality = localQuality;
        this.shopPrice = shopPrice;
        this.shopQuality = shopQuality;
        this.volume = volume;
        this.weigh = weigh;
        this.localMarketVolumeSum = localMarketVolumeSum;
        this.shopMarketVolumeSum = shopMarketVolumeSum;
        this.localMarketVolumeSumTotal = localMarketVolumeSumTotal;
        this.shopMarketVolumeSumTotal = shopMarketVolumeSumTotal;
        this.percentMarketVolumeSum = percentMarketVolumeSum;
        this.percentMarketVolumeSumTotal = percentMarketVolumeSumTotal;
    }

    public RetailTrend(final TradeAtCity tac) {
        this.date = tac.getDate();
        this.dateStr = dateFormat.format(tac.getDate());
        this.localPrice = tac.getLocalPrice();
        this.localQuality = tac.getLocalQuality();
        this.shopPrice = tac.getShopPrice();
        this.shopQuality = tac.getShopQuality();
        this.volume = tac.getVolume();
        this.weigh = tac.getVolume();
        this.localMarketVolumeSum = tac.getLocalMarketVolumeSum();
        this.localMarketVolumeSumTotal = tac.getLocalMarketVolumeSumTotal();
        this.shopMarketVolumeSum = tac.getShopMarketVolumeSum();
        this.shopMarketVolumeSumTotal = tac.getShopMarketVolumeSumTotal();
        this.percentMarketVolumeSum = tac.getPercentMarketVolumeSum();
        this.percentMarketVolumeSumTotal = tac.getPercentMarketVolumeSumTotal();
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

    public double getWeigh() {
        return weigh;
    }

    public double getVolume() {
        return volume;
    }

    public Date getDate() {
        return date;
    }

    public double getLocalMarketVolumeSum() {
        return localMarketVolumeSum;
    }

    public double getShopMarketVolumeSum() {
        return shopMarketVolumeSum;
    }

    public double getLocalMarketVolumeSumTotal() {
        return localMarketVolumeSumTotal;
    }

    public double getShopMarketVolumeSumTotal() {
        return shopMarketVolumeSumTotal;
    }

    public double getPercentMarketVolumeSum() {
        return percentMarketVolumeSum;
    }

    public double getPercentMarketVolumeSumTotal() {
        return percentMarketVolumeSumTotal;
    }
}
