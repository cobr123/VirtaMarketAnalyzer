package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 16.03.2016.
 * Таможенные пошлины страны.
 */
final public class CountryDutyList {
    @SerializedName("ri")
    final private String regionId;
    @SerializedName("pi")
    final private String productId;
    @SerializedName("etp")
    final private int exportTaxPercent;
    @SerializedName("itp")
    final private int importTaxPercent;
    @SerializedName("ip")
    final private double indicativePrice;

    public CountryDutyList(final String regionId, final String productId, final int exportTaxPercent, final int importTaxPercent, final double indicativePrice) {
        this.regionId = regionId;
        this.productId = productId;
        this.exportTaxPercent = exportTaxPercent;
        this.importTaxPercent = importTaxPercent;
        this.indicativePrice = indicativePrice;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getProductId() {
        return productId;
    }

    public int getExportTaxPercent() {
        return exportTaxPercent;
    }

    public int getImportTaxPercent() {
        return importTaxPercent;
    }

    public double getIndicativePrice() {
        return indicativePrice;
    }
}
