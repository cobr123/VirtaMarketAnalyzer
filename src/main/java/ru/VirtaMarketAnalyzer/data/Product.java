package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Product {
    @SerializedName("pc")
    final private String productCategory;
    @SerializedName("s")
    final private String imgUrl;
    @SerializedName("i")
    final private String id;
    @SerializedName("c")
    final private String caption;

    public Product(final String productCategory, final String imgUrl, final String id, final String caption) {
        this.productCategory = productCategory;
        this.imgUrl = imgUrl;
        this.id = id;
        this.caption = caption;
    }
    public String getImgUrl() {
        return imgUrl;
    }

    public String getId() {
        return id;
    }

    public String getCaption() {
        return caption;
    }

    public String getProductCategory() {
        return productCategory;
    }
}
