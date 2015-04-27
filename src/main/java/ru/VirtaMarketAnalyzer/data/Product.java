package ru.VirtaMarketAnalyzer.data;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Product {
    final private String productCategory;
    final private String imgUrl;
    final private String id;
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
