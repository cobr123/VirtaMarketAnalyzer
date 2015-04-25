package ru.VirtaMarketAnalyzer.data;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class MajorSellInCity {
    private final long shopSize;
    private final String cityDistrict;
    private final long sellVolume;
    private final double price;
    private final double quality;
    private final double brand;

    public MajorSellInCity(final long shopSize, final String cityDistrict, final long sellVolume, final double price, final double quality, final double brand) {
        this.shopSize = shopSize;
        this.cityDistrict = cityDistrict;
        this.sellVolume = sellVolume;
        this.price = price;
        this.quality = quality;
        this.brand = brand;
    }


    public long getShopSize() {
        return shopSize;
    }

    public String getCityDistrict() {
        return cityDistrict;
    }

    public long getSellVolume() {
        return sellVolume;
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
}
