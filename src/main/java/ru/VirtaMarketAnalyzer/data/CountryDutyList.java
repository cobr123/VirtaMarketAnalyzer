package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 16.03.2016.
 * Таможенные пошлины страны.
 */
final public class CountryDutyList {
    //в файл не выгружаем, т.к. id будет в имени файла
    transient final private String countryId;
    @SerializedName("pi")
    final private String productId;
    @SerializedName("etp")
    final private int exportTaxPercent;
    @SerializedName("itp")
    final private int importTaxPercent;
    @SerializedName("ip")
    final private double indicativePrice;

    public CountryDutyList(final String countryId, final String productId, final int exportTaxPercent, final int importTaxPercent, final double indicativePrice) {
        this.countryId = countryId;
        this.productId = productId;
        this.exportTaxPercent = exportTaxPercent;
        this.importTaxPercent = importTaxPercent;
        this.indicativePrice = indicativePrice;
    }

    public String getCountryId() {
        return countryId;
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
