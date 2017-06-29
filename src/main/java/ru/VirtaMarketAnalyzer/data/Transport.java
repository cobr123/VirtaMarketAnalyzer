package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 29.06.2017.
 */
public final class Transport {
    //в файл не выгружаем, т.к. id будет в имени файла
    transient final private String productID;
    @SerializedName("ci")
    final private String cityToId;
    @SerializedName("d")
    final private int distance;
    @SerializedName("do")
    final private double deliverOne;
    @SerializedName("mce")
    final private double minCostExport;
    @SerializedName("tce")
    final private double totalCostExport;
    @SerializedName("mci")
    final private double minCostImport;
    @SerializedName("tci")
    final private double totalCostImport;
    @SerializedName("i")
    final private String imgSrc;

    public Transport(final String cityToId, final String productID
            , final int distance, final double deliverOne
            , final double minCostExport, final double totalCostExport
            , final double minCostImport, final double totalCostImport
            , final String imgSrc) {
        this.cityToId = cityToId;
        this.productID = productID;
        this.distance = distance;
        this.deliverOne = deliverOne;
        this.minCostExport = minCostExport;
        this.totalCostExport = totalCostExport;
        this.minCostImport = minCostImport;
        this.totalCostImport = totalCostImport;
        this.imgSrc = imgSrc;
    }

    public String getCityToId() {
        return cityToId;
    }

    public String getProductID() {
        return productID;
    }

    public int getDistance() {
        return distance;
    }

    public double getDeliverOne() {
        return deliverOne;
    }

    public double getMinCostExport() {
        return minCostExport;
    }

    public double getTotalCostExport() {
        return totalCostExport;
    }

    public double getMinCostImport() {
        return minCostImport;
    }

    public double getTotalCostImport() {
        return totalCostImport;
    }

    public String getImgSrc() {
        return imgSrc;
    }
}