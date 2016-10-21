package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Product {
    @SerializedName("pc")
    final private String productCategory;
    @SerializedName("pci")
    final private String productCategoryID;
    @SerializedName("s")
    final private String imgUrl;
    @SerializedName("i")
    final private String id;
    @SerializedName("c")
    final private String caption;
    @SerializedName("sym")
    final private String symbol;

    public Product(final String productCategory, final String id, final String caption, final String productCategoryID, final String symbol) {
        this.productCategory = productCategory;
        this.imgUrl = "/img/products/" + symbol + ".gif";
        this.id = id;
        this.caption = caption;
        this.productCategoryID = productCategoryID;
        this.symbol = symbol;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(productCategory)
                .append(imgUrl)
                .append(id)
                .append(caption)
                .append(productCategoryID)
                .append(symbol)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Product) {
            final Product other = (Product) obj;
            return new EqualsBuilder()
                    .append(productCategory, other.productCategory)
                    .append(imgUrl, other.imgUrl)
                    .append(id, other.id)
                    .append(caption, other.caption)
                    .append(productCategoryID, other.productCategoryID)
                    .append(symbol, other.symbol)
                    .isEquals();
        } else {
            return false;
        }
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

    public String getProductCategoryID() {
        return productCategoryID;
    }

    public String getSymbol() {
        return symbol;
    }
}
