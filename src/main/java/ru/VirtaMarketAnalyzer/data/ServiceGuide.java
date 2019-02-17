package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.parser.CityInitParser;

import java.io.IOException;
import java.util.List;

/**
 * Created by cobr123 on 17.02.2019.
 */
final public class ServiceGuide {
    @SerializedName("ci")
    final private String countryId;
    @SerializedName("ri")
    final private String regionId;
    @SerializedName("ti")
    final private String townId;
    @SerializedName("ssi")
    final private String serviceSpecId;
    @SerializedName("sgp")
    final private List<ServiceGuideProduct> serviceGuideProducts;
    @SerializedName("bps")
    final private double buyPriceSum;
    @SerializedName("sps")
    final private double sellPriceSum;
    @SerializedName("sc")
    final private long sellCnt;
    @SerializedName("iats")
    final private double incomeAfterTaxSum;

    public ServiceGuide(
            final String host,
            final String realm,
            final String serviceSpecId,
            final City city,
            final List<ServiceGuideProduct> serviceGuideProducts,
            final long planSellCnt
    ) throws IOException {
        this.countryId = city.getCountryId();
        this.regionId = city.getRegionId();
        this.townId = city.getId();
        this.serviceSpecId = serviceSpecId;
        this.serviceGuideProducts = serviceGuideProducts;
        this.buyPriceSum = serviceGuideProducts.stream().mapToDouble(p -> p.getBuyPrice() * p.getQuantityPerSell()).sum();
        this.sellPriceSum = serviceGuideProducts.stream().mapToDouble(p -> p.getSellPrice() * p.getQuantityPerSell()).sum();
        final long actualSellCnt = Math.round(serviceGuideProducts.stream().mapToDouble(p -> p.getVolume() / p.getQuantityPerSell() / 0.1).min().orElse(0));
        this.sellCnt = Math.min(actualSellCnt, planSellCnt);
        if (sellPriceSum > buyPriceSum) {
            final Region region = CityInitParser.getRegion(host, realm, city.getRegionId());
            this.incomeAfterTaxSum = Utils.round2(sellCnt * (sellPriceSum - buyPriceSum) * (1.0 - region.getIncomeTaxRate() / 100.0));
        } else {
            this.incomeAfterTaxSum = Utils.round2(sellCnt * (sellPriceSum - buyPriceSum));
        }
    }

    public String getCountryId() {
        return countryId;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getTownId() {
        return townId;
    }

    public List<ServiceGuideProduct> getServiceGuideProducts() {
        return serviceGuideProducts;
    }

    public String getServiceSpecId() {
        return serviceSpecId;
    }

    public String getGeo() {
        return getCountryId() + "/" + getRegionId() + "/" + getTownId();
    }

    public double getBuyPriceSum() {
        return buyPriceSum;
    }

    public double getSellPriceSum() {
        return sellPriceSum;
    }

    public long getSellCnt() {
        return sellCnt;
    }

    public double getIncomeAfterTaxSum() {
        return incomeAfterTaxSum;
    }
}
