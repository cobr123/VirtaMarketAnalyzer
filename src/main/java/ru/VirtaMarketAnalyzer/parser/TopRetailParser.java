package ru.VirtaMarketAnalyzer.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by cobr123 on 16.01.16.
 */
public final class TopRetailParser {
    private static final Logger logger = LoggerFactory.getLogger(TopRetailParser.class);

    public static List<Shop> getShopList(final String host, final String realm, final List<TradeAtCity> stats, final Product product) throws IOException {
        return stats.parallelStream()
                .map(TradeAtCity::getMajorSellInCityList)
                .flatMap(Collection::stream)
                .map(msic -> {
                            try {
                                return ShopParser.getShop(host, realm, product, msic, stats);
                            } catch (final Exception e) {
                                logger.error(e.getLocalizedMessage(), e);
                                return null;
                            }
                        }
                )
                .filter(Objects::nonNull)
                .filter(s -> s.getShopProducts().size() > 0)
                .filter(s -> !"Не известен".equals(s.getTownDistrict()))
                .filter(s -> !"Не известен".equals(s.getServiceLevel()))
                .collect(Collectors.toList());
    }

}
