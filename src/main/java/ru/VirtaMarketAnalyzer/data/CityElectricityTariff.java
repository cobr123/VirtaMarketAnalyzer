package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 22.06.2017.
 * Тарифы на электроэнергию
 */
final public class CityElectricityTariff {
    @SerializedName("ci")
    final private String cityId;
    //@SerializedName("pc")
    //в файл не выгружаем пока что
    transient final private String productCategory;
    //в файл не выгружаем, т.к. id будет в имени файла
    transient final private String productID;
    @SerializedName("et")
    final private double electricityTariff;

    public CityElectricityTariff(final String cityId, final String productCategory, final String productID, final double electricityTariff) {
        this.cityId = cityId;
        this.productCategory = productCategory;
        this.productID = productID;
        this.electricityTariff = electricityTariff;
    }

    public String getCityId() {
        return cityId;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public String getProductID() {
        return productID;
    }

    public double getElectricityTariff() {
        return electricityTariff;
    }
}
