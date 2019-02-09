package ru.VirtaMarketAnalyzer.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 08.02.2019.
 */
final public class TradeGuideParser {
    private static final Logger logger = LoggerFactory.getLogger(TradeGuideParser.class);

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
                    } catch (IOException e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static TradeGuide genTradeGuide(
            final String host,
            final String realm,
            final City city,
            final List<Product> products,
            final Map<String, List<ProductRemain>> productRemains
    ) throws IOException {
        final List<TradeGuideProduct> tradeGuideProduct = new ArrayList<>();
        final List<TradeAtCity> stats = CityParser.get(host, realm, city, products);
        for (final TradeAtCity stat : stats) {
            if (stat.getLocalPercent() >= 10) {
                tradeGuideProduct.add(genTradeGuideProduct(host, realm, stat, productRemains.getOrDefault(stat.getProductId(), new ArrayList<>())));
            }
        }
        return new TradeGuide(city, tradeGuideProduct);
    }

    public static TradeGuideProduct genTradeGuideProduct(
            final String host,
            final String realm,
            final TradeAtCity stat,
            final List<ProductRemain> productRemains
    ) throws IOException {
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
            final double transportCost = CountryDutyListParser.getTransportCost(host, realm, pr.getTownId(), stat.getTownId(), pr.getProductID());
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
        final RegionCTIE regionCTIE = RegionCTIEParser.getRegionCTIE(host, realm, stat.getRegionId(), stat.getProductId());
        double incomeAfterTax;
        if (sellPrice > buyPrice) {
            incomeAfterTax = Utils.round2(volume * (sellPrice - buyPrice) * (regionCTIE.getRate() / 100.0));
        } else {
            incomeAfterTax = Utils.round2(volume * (sellPrice - buyPrice));
        }
        return new TradeGuideProduct(stat.getProductId(), quality, buyPrice, sellPrice, volume, incomeAfterTax);
    }

    private static double merge(double quality1, double volume1, double quality2, double volume2) {
        return Utils.round2((quality1 * volume1) / (volume1 + volume2) + (quality2 * volume2) / (volume1 + volume2));
    }

}
