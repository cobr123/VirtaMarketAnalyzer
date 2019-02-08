package ru.VirtaMarketAnalyzer.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.parser.CityParser;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by cobr123 on 27.06.2015.
 */
final public class CityProduct {
    private final City city;
    private final Product product;
    private final String host;
    private final String realm;
    private static final Logger logger = LoggerFactory.getLogger(CityProduct.class);

    public CityProduct(final City city, final Product product, final String host, final String realm) {
        this.city = city;
        this.product = product;
        this.host = host;
        this.realm = realm;
    }

    public City getCity() {
        return city;
    }

    public Product getProduct() {
        return product;
    }

    public TradeAtCityBuilder getTradeAtCity() {
        final int maxTriesCnt = 3;
        for (int tries = 1; tries <= maxTriesCnt; ++tries) {
            try {
                return CityParser.get(host, realm, getCity(), getProduct());
            } catch (final Exception e) {
                logger.error("Ошибка при запросе, попытка #{} из {}", tries, maxTriesCnt);
                logger.error("Ошибка: ", e);
                if (tries < maxTriesCnt) {
                    Utils.waitSecond(3);
                }
            }
        }
        return null;
    }
}
