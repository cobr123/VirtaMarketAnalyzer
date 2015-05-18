package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 18.05.2015.
 */
final public class ManufactureResult {
    @SerializedName("pi")
    final private String productID;
    @SerializedName("pbq")
    final private Double prodBaseQty;
    @SerializedName("rq")
    final private Double resultQty;
    @SerializedName("qbp")
    final private Double qualityBonusPercent;

    public ManufactureResult(final String productID, final Double prodBaseQty, final Double resultQty, final Double qualityBonusPercent) {
        this.productID = productID;
        this.qualityBonusPercent = qualityBonusPercent;
        this.prodBaseQty = prodBaseQty;
        this.resultQty = resultQty;
    }

    public String getProductID() {
        return productID;
    }

    public Double getQualityBonusPercent() {
        return qualityBonusPercent;
    }

    public Double getProdBaseQty() {
        return prodBaseQty;
    }

    public Double getResultQty() {
        return resultQty;
    }
}
