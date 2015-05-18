package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 18.05.2015.
 */
final public class ManufactureIngredient {
    @SerializedName("pi")
    final private String productID;
    @SerializedName("q")
    final private Double qty;
    @SerializedName("mq")
    final private Double minQuality;

    public ManufactureIngredient(final String productID, final Double qty,final Double minQuality) {
        this.productID = productID;
        this.qty = qty;
        this.minQuality = minQuality;
    }

    public String getProductID() {
        return productID;
    }

    public Double getQty() {
        return qty;
    }

    public Double getMinQuality() {
        return minQuality;
    }
}
