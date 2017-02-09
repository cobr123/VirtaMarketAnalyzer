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
    private String date;
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

    public RetailTrend(final TradeAtCity tac){
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
}
