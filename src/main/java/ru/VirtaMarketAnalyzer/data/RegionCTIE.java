package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 19.03.2016.
 * Ставки ЕНВД в регионе
 */
final public class RegionCTIE {
    //в файл не выгружаем, т.к. id будет в имени файла
    transient final private String regionId;
    @SerializedName("pi")
    final private String productId;
    @SerializedName("r")
    final private int rate;

    public RegionCTIE(final String regionId, final String productId, final int rate) {
        this.regionId = regionId;
        this.productId = productId;
        this.rate = rate;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getProductId() {
        return productId;
    }

    public int getRate() {
        return rate;
    }


}
