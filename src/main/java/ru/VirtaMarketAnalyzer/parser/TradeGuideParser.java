package ru.VirtaMarketAnalyzer.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 08.02.2019.
 */
final public class TradeGuideParser {
    private static final Logger logger = LoggerFactory.getLogger(TradeGuideParser.class);

    /**
     * Создает розничный гид по одной категории товаров для всех городов
     */
    public static List<TradeGuide> genTradeGuide(
            final String host,
            final String realm,
            final ProductCategory productCategory
    ) throws Exception {
        final List<City> cities = CityListParser.getCities(host, realm, false);
        final List<Product> products = ProductInitParser.getTradingProducts(host, realm, productCategory);
        final Map<String, List<ProductRemain>> productRemains = ProductRemainParser.getRemains(host, realm, products);

        return cities.parallelStream()
                .map(city -> {
                    try {
                        return genTradeGuide(host, realm, city, products, productRemains);
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Создает розничный гид по одной категории товаров для одного города.
     * Только если местных больше или равно 10%.
     * Только если хотя бы один продукт прибыльный.
     */
    public static TradeGuide genTradeGuide(
            final String host,
            final String realm,
            final City city,
            final List<Product> products,
            final Map<String, List<ProductRemain>> productRemains
    ) throws Exception {
        final List<TradeGuideProduct> tradeGuideProduct = new ArrayList<>();
        final List<TradeAtCity> stats = CityParser.get(host, realm, city, products);
        for (final TradeAtCity stat : stats) {
            if (stat.getLocalPercent() >= 10) {
                tradeGuideProduct.add(genTradeGuideProduct(host, realm, stat, productRemains.getOrDefault(stat.getProductId(), new ArrayList<>())));
            }
        }
        final boolean positiveExists = tradeGuideProduct.stream().anyMatch(i -> i.getIncomeAfterTax() > 0);
        if (positiveExists) {
            return new TradeGuide(city, tradeGuideProduct);
        } else {
            return null;
        }
    }

    /**
     * Считает прибыльность товара для одного города.
     * Максимальный объем продаж 10% рынка.
     */
    public static TradeGuideProduct genTradeGuideProduct(
            final String host,
            final String realm,
            final TradeAtCity stat,
            final List<ProductRemain> productRemains
    ) throws Exception {
        final double localPqr = stat.getLocalPrice() / stat.getLocalQuality();
        final List<ProductRemain> productRemainsFiltered = productRemains.stream()
                .filter(pr -> pr.getRemainByMaxOrderType() > 0 && pr.getPrice() / pr.getQuality() <= localPqr)
                .sorted(Comparator.comparingDouble(o -> o.getPrice() / o.getQuality()))
                .collect(Collectors.toList());
        final long maxVolume = Math.round(stat.getVolume() * 0.1);
        double quality = 0;
        double buyPrice = 0;
        long volume = 0;
        for (final ProductRemain pr : productRemainsFiltered) {
            final double maxProductRemainVolume = Math.min(pr.getRemainByMaxOrderType(), maxVolume - volume);
            final double priceWithDuty = CountryDutyListParser.addDuty(host, realm, pr.getCountryId(), stat.getCountryId(), pr.getProductID(), pr.getPrice());
            final double transportCost = Utils.repeatOnErr(() -> CountryDutyListParser.getTransportCost(host, realm, pr.getTownId(), stat.getTownId(), pr.getProductID()));
            quality = merge(quality, volume, pr.getQuality(), maxProductRemainVolume);
            buyPrice = merge(buyPrice, volume, priceWithDuty + transportCost, maxProductRemainVolume);
            volume += maxProductRemainVolume;
            if (volume >= maxVolume) {
                break;
            }
        }
        double sellPrice = stat.getLocalPrice();
        if (quality - 30.0 > stat.getLocalQuality()) {
            sellPrice = Utils.round2(stat.getLocalPrice() * 2.5);
        } else if (quality - 20.0 > stat.getLocalQuality()) {
            sellPrice = Utils.round2(stat.getLocalPrice() * 2.0);
        } else if (quality - 10.0 > stat.getLocalQuality()) {
            sellPrice = Utils.round2(stat.getLocalPrice() * 1.5);
        }
        double incomeAfterTax = Utils.round2(volume * (sellPrice - buyPrice));
        if (sellPrice > buyPrice) {
            final Region region = CityInitParser.getRegion(host, realm, stat.getRegionId());
            incomeAfterTax = Utils.round2(incomeAfterTax * (1.0 - region.getIncomeTaxRate() / 100.0));
        }
        return new TradeGuideProduct(stat.getProductId(), quality, buyPrice, sellPrice, volume, incomeAfterTax);
    }

    private static double merge(double quality1, double volume1, double quality2, double volume2) {
        return Utils.round2((quality1 * volume1) / (volume1 + volume2) + (quality2 * volume2) / (volume1 + volume2));
    }

}
