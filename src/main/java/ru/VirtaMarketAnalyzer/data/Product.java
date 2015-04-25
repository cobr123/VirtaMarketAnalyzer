package ru.VirtaMarketAnalyzer.data;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Product {
    final String imgUrl;
    final String id;
    final String name;
    final String caption;

    public Product(final String imgUrl, final String id, final String name, final String caption) {
        this.imgUrl = imgUrl;
        this.id = id;
        this.name = name;
        this.caption = caption;
    }
}
