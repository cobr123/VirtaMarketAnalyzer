package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

/**
 * Created by cobr123 on 20.05.2015.
 */
public final class ProductRemain {
    //U - UNLIMITED, L - LIMITED
    public enum MaxOrderType {
        U, L
    }

    //Infinity
    @SerializedName("pi")
    final private String productID;
    @SerializedName("ui")
    final private String unitID;
    @SerializedName("t")
    final private long total;
    @SerializedName("r")
    final private long remain;
    @SerializedName("q")
    final private double quality;
    @SerializedName("p")
    final private double price;
    @SerializedName("mot")
    final private MaxOrderType maxOrderType;
    @SerializedName("mo")
    final private double maxOrder;

    transient private Date date;

    public ProductRemain(final String productID, final String unitID, final long total, final long remain, final double quality, final double price, final MaxOrderType maxOrderType, final double maxOrder) {
        this.productID = productID;
        this.unitID = unitID;
        this.total = total;
        this.remain = remain;
        this.quality = quality;
        this.price = price;
        this.maxOrderType = maxOrderType;
        this.maxOrder = maxOrder;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(productID)
                .append(unitID)
                .append(total)
                .append(remain)
                .append(quality)
                .append(price)
                .append(maxOrderType)
                .append(maxOrder)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ProductRemain) {
            final ProductRemain other = (ProductRemain) obj;
            return new EqualsBuilder()
                    .append(productID, other.productID)
                    .append(unitID, other.unitID)
                    .append(total, other.total)
                    .append(remain, other.remain)
                    .append(quality, other.quality)
                    .append(price, other.price)
                    .append(maxOrderType, other.maxOrderType)
                    .append(maxOrder, other.maxOrder)
                    .isEquals();
        } else {
            return false;
        }
    }

    public String getProductID() {
        return productID;
    }

    public String getUnitID() {
        return unitID;
    }

    public long getTotal() {
        return total;
    }

    public double getRemain() {
        return remain;
    }

    public double getQuality() {
        return quality;
    }

    public double getPrice() {
        return price;
    }

    public double getMaxOrder() {
        return maxOrder;
    }

    public MaxOrderType getMaxOrderType() {
        return maxOrderType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }
}
