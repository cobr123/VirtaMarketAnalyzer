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
    final private String date;
    @SerializedName("lpr")
    final private double localPrice;
    @SerializedName("lq")
    final private double localQuality;
    @SerializedName("spr")
    final private double shopPrice;
    @SerializedName("sq")
    final private double shopQuality;
    @SerializedName("v")
    final private long volume;
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

    final static transient DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public RetailTrend(
            final double localPrice,
            final double localQuality,
            final double shopPrice,
            final double shopQuality,
            final Date date,
            final long volume,
            final double localMarketVolumeSum,
            final double shopMarketVolumeSum,
            final double localMarketVolumeSumTotal,
            final double shopMarketVolumeSumTotal,
            final double percentMarketVolumeSum,
            final double percentMarketVolumeSumTotal
    ) {
        this.date = dateFormat.format(date);
        this.localPrice = localPrice;
        this.localQuality = localQuality;
        this.shopPrice = shopPrice;
        this.shopQuality = shopQuality;
        this.volume = volume;
        this.localMarketVolumeSum = localMarketVolumeSum;
        this.shopMarketVolumeSum = shopMarketVolumeSum;
        this.localMarketVolumeSumTotal = localMarketVolumeSumTotal;
        this.shopMarketVolumeSumTotal = shopMarketVolumeSumTotal;
        this.percentMarketVolumeSum = percentMarketVolumeSum;
        this.percentMarketVolumeSumTotal = percentMarketVolumeSumTotal;
    }
    public RetailTrend(
            final double localPrice,
            final double localQuality,
            final double shopPrice,
            final double shopQuality,
            final String date,
            final long volume,
            final double localMarketVolumeSum,
            final double shopMarketVolumeSum,
            final double localMarketVolumeSumTotal,
            final double shopMarketVolumeSumTotal,
            final double percentMarketVolumeSum,
            final double percentMarketVolumeSumTotal
    ) {
        this.date = date;
        this.localPrice = localPrice;
        this.localQuality = localQuality;
        this.shopPrice = shopPrice;
        this.shopQuality = shopQuality;
        this.volume = volume;
        this.localMarketVolumeSum = localMarketVolumeSum;
        this.shopMarketVolumeSum = shopMarketVolumeSum;
        this.localMarketVolumeSumTotal = localMarketVolumeSumTotal;
        this.shopMarketVolumeSumTotal = shopMarketVolumeSumTotal;
        this.percentMarketVolumeSum = percentMarketVolumeSum;
        this.percentMarketVolumeSumTotal = percentMarketVolumeSumTotal;
    }

    public RetailTrend(final TradeAtCity tac) {
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        this.date = df.format(tac.getDate());
        this.localPrice = tac.getLocalPrice();
        this.localQuality = tac.getLocalQuality();
        this.shopPrice = tac.getShopPrice();
        this.shopQuality = tac.getShopQuality();
        this.volume = tac.getVolume();
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

    public long getVolume() {
        return volume;
    }

    public String getDate() {
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
