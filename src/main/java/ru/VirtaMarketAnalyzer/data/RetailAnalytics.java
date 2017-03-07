package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import ru.VirtaMarketAnalyzer.main.Utils;

import java.util.Date;

/**
 * Created by cobr123 on 16.01.16.
 */
public final class RetailAnalytics {
    @SerializedName("pi")
    private final String productId;
    @SerializedName("pc")
    private final String productCategory;
    @SerializedName("ss")
    private final int shopSize;
    @SerializedName("td")
    private final String townDistrict;
    @SerializedName("dc")
    private final int departmentCount;
    @SerializedName("n")
    private final double notoriety;
    @SerializedName("vc")
    private final String visitorsCount;
    @SerializedName("sl")
    final private String serviceLevel;
    @SerializedName("sv")
    private final String sellVolume;
    @SerializedName("p")
    private final double price;
    @SerializedName("q")
    private final double quality;
    @SerializedName("b")
    private final double brand;
    @SerializedName("ms")
    private final double marketShare;
    @SerializedName("wi")
    final private double wealthIndex;
    @SerializedName("ei")
    final private double educationIndex;
    @SerializedName("as")
    final private double averageSalary;
    @SerializedName("mi")
    final private String marketIdx;
    @SerializedName("mv")
    final private long marketVolume;
    @SerializedName("sc")
    final private int sellerCnt;
    @SerializedName("lpe")
    final private double localPercent;
    @SerializedName("lpr")
    final private double localPrice;
    @SerializedName("lq")
    final private double localQuality;
    transient final private long sellVolumeAsNumber;
    transient private Date date;

    public RetailAnalytics(final String productId, final String productCategory, final int shopSize, final String townDistrict, final int departmentCount,
                           final double notoriety, final String visitorsCount, final String serviceLevel,
                           final String sellVolume, final double price, final double quality, final double brand, final double marketShare,
                           final double wealthIndex, final double educationIndex, final double averageSalary,
                           final String marketIdx, final long marketVolume, final int sellerCnt, final double localPercent,
                           final double localPrice, final double localQuality) {
        this.productId = productId;
        this.productCategory = productCategory;
        this.shopSize = shopSize;
        this.townDistrict = fixTownDistrict(townDistrict);
        this.departmentCount = departmentCount;
        this.notoriety = notoriety;
        this.visitorsCount = visitorsCount;
        this.serviceLevel = serviceLevel;
        this.sellVolume = sellVolume;
        this.price = price;
        this.quality = quality;
        this.brand = brand;
        this.marketShare = marketShare;
        this.wealthIndex = wealthIndex;
        this.educationIndex = educationIndex;
        this.averageSalary = averageSalary;
        this.marketIdx = marketIdx;
        this.marketVolume = marketVolume;
        this.sellerCnt = sellerCnt;
        this.localPercent = localPercent;
        this.localPrice = localPrice;
        this.localQuality = localQuality;
        this.sellVolumeAsNumber = calcSellVolumeAsNumber();
    }

    public static String fixTownDistrict(final String townDistrict) {
        return townDistrict
                .replace("Outskirts", "Окраина")
                .replace("Suburb", "Пригород")
                .replace("城市中心", "Центр города")
                .replace("City center", "Центр города")
                .replace("City centre", "Центр города")
                .replace("Центр міста", "Центр города")
                .replace("Stadtzentrum", "Центр города")
                .replace("Centro de la ciudad", "Центр города")
                .replace("社区", "Спальный район")
                .replace("Residential area", "Спальный район")
                .replace("Área residencial", "Спальный район")
                .replace("Elitesbezirk", "Фешенебельный район")
                .replace("Trendy neighborhood", "Фешенебельный район")
                .replace("Фешенебельний район", "Фешенебельный район");
    }

    //для совместимости
    public RetailAnalytics(final String productId, final String productCategory, final RetailAnalytics ra) {
        this.productId = productId;
        this.productCategory = productCategory;
        this.shopSize = ra.shopSize;
        this.townDistrict = ra.townDistrict;
        this.departmentCount = ra.departmentCount;
        this.notoriety = ra.notoriety;
        this.visitorsCount = ra.visitorsCount;
        this.serviceLevel = ra.serviceLevel;
        this.sellVolume = ra.sellVolume;
        this.price = ra.price;
        this.quality = ra.quality;
        this.brand = ra.brand;
        this.marketShare = ra.marketShare;
        this.wealthIndex = ra.wealthIndex;
        this.educationIndex = ra.educationIndex;
        this.averageSalary = ra.averageSalary;
        this.marketIdx = ra.marketIdx;
        this.marketVolume = ra.marketVolume;
        this.sellerCnt = ra.sellerCnt;
        this.localPercent = ra.localPercent;
        this.localPrice = ra.localPrice;
        this.localQuality = ra.localQuality;
        this.sellVolumeAsNumber = calcSellVolumeAsNumber();
        this.date = ra.date;
    }

    public static RetailAnalytics fillProductId(final String productId, final String productCategory, final RetailAnalytics ra) {
        if (ra.getProductId() == null || ra.getProductId().isEmpty() || ra.getProductCategory() == null || ra.getProductCategory().isEmpty()) {
            return new RetailAnalytics(productId, productCategory, ra);
        } else {
            return ra;
        }
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(productId)
                .append(productCategory)
                .append(shopSize)
                .append(townDistrict)
                .append(departmentCount)
                .append(notoriety)
                .append(visitorsCount)
                .append(serviceLevel)
                .append(sellVolume)
                .append(price)
                .append(quality)
                .append(brand)
                .append(marketShare)
                .append(wealthIndex)
                .append(educationIndex)
                .append(averageSalary)
                .append(marketIdx)
                .append(marketVolume)
                .append(sellerCnt)
                .append(localPercent)
                .append(localPrice)
                .append(localQuality)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof RetailAnalytics) {
            final RetailAnalytics other = (RetailAnalytics) obj;
            return new EqualsBuilder()
                    .append(productId, other.productId)
                    .append(productCategory, other.productCategory)
                    .append(shopSize, other.shopSize)
                    .append(townDistrict, other.townDistrict)
                    .append(departmentCount, other.departmentCount)
                    .append(notoriety, other.notoriety)
                    .append(visitorsCount, other.visitorsCount)
                    .append(serviceLevel, other.serviceLevel)
                    .append(sellVolume, other.sellVolume)
                    .append(price, other.price)
                    .append(quality, other.quality)
                    .append(brand, other.brand)
                    .append(marketShare, other.marketShare)
                    .append(wealthIndex, other.wealthIndex)
                    .append(educationIndex, other.educationIndex)
                    .append(averageSalary, other.averageSalary)
                    .append(marketIdx, other.marketIdx)
                    .append(marketVolume, other.marketVolume)
                    .append(sellerCnt, other.sellerCnt)
                    .append(localPercent, other.localPercent)
                    .append(localPrice, other.localPrice)
                    .append(localQuality, other.localQuality)
                    .isEquals();
        } else {
            return false;
        }
    }

    public String getProductId() {
        return productId;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public int getShopSize() {
        return shopSize;
    }

    public String getTownDistrict() {
        return fixTownDistrict(townDistrict);
    }

    public int getDepartmentCount() {
        return departmentCount;
    }

    public double getNotoriety() {
        return notoriety;
    }

    public String getVisitorsCount() {
        return visitorsCount;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

//    public String getSellVolume() {
//        return sellVolume;
//    }
    public double getSellVolumeNumber() {
        return Math.round(getMarketShare() / 100.0 * getMarketVolume());
    }

    public double getPrice() {
        return price;
    }

    public double getQuality() {
        return quality;
    }

    public double getBrand() {
        return brand;
    }

    public double getMarketShare() {
        return marketShare;
    }

    public double getWealthIndex() {
        return wealthIndex;
    }

    public long getWealthIndexRounded() {
        return Math.round(wealthIndex);
    }

    public double getEducationIndex() {
        return educationIndex;
    }

    public double getAverageSalary() {
        return averageSalary;
    }

    public String getMarketIdx() {
        return marketIdx;
    }

    public long getMarketVolume() {
        return marketVolume;
    }

    public int getSellerCnt() {
        return sellerCnt;
    }

    public double getLocalPercent() {
        return localPercent;
    }

    public double getLocalPrice() {
        return localPrice;
    }

    public double getLocalQuality() {
        return localQuality;
    }

    private long calcSellVolumeAsNumber() {
        long mod = 0;
        if (sellVolume.startsWith("менее")) {
            mod = -1;
        } else if (sellVolume.startsWith("более")) {
            mod = 1;
        }
        return Utils.toLong(sellVolume) + mod;
    }

    public long getSellVolumeAsNumber() {
        return sellVolumeAsNumber;
    }

}
