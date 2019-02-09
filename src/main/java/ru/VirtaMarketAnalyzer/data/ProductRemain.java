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
    @SerializedName("cn")
    final private String companyName;
    @SerializedName("pi")
    final private String productID;
    @SerializedName("ui")
    final private String unitID;
    @SerializedName("ti")
    final private String townId;
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
    final private long maxOrder;

    transient private Date date;

    public ProductRemain(final String productID, final String companyName, final String unitID, final String townId, final long total, final long remain, final double quality, final double price, final MaxOrderType maxOrderType, final long maxOrder) {
        this.productID = productID;
        this.companyName = companyName;
        this.unitID = unitID;
        this.townId = townId;
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
                .append(companyName)
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
                    .append(companyName, other.companyName)
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

    public String getCompanyName() {
        return companyName;
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

    public long getRemain() {
        return remain;
    }

    public double getQuality() {
        return quality;
    }

    public double getPrice() {
        return price;
    }

    public long getMaxOrder() {
        return maxOrder;
    }

    public MaxOrderType getMaxOrderType() {
        return maxOrderType;
    }

    public long getRemainByMaxOrderType() {
        return (getMaxOrderType() == MaxOrderType.L && getRemain() > getMaxOrder()) ? getMaxOrder() : getRemain();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getTownId() {
        return townId;
    }
}
