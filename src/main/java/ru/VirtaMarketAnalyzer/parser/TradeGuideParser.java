package ru.VirtaMarketAnalyzer.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        return cities.stream()
                .map(city -> {
                    try {
                        return genTradeGuide(host, realm, city, products);
                    } catch (final Exception e) {
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
            final List<Product> products
    ) throws Exception {
        final Map<String, List<ProductRemain>> productRemains = ProductRemainParser.getRemains(host, realm, products);
        final Map<String, List<TradeAtCity>> stats = CityParser.get(host, realm, city, products);
        return null;
    }

}
