package ru.VirtaMarketAnalyzer.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.RetailAnalytics;
import ru.VirtaMarketAnalyzer.data.Shop;
import ru.VirtaMarketAnalyzer.data.ShopProduct;
import ru.VirtaMarketAnalyzer.data.TradeAtCity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 16.01.16.
 */
public final class PrepareAnalitics {
    private static final Logger logger = LoggerFactory.getLogger(PrepareAnalitics.class);

    public static Map<String, List<RetailAnalytics>> getRetailAnalitincsByProducts(final List<Shop> shops, final Map<String, List<TradeAtCity>> stats) {
        final Map<String, List<Shop>> shopBy = shops.parallelStream().collect(Collectors.groupingBy(Shop::getCountryRegionTownIds));

        final Map<String, List<RetailAnalytics>> retailAnalitincs = new HashMap<>();

        for (final Map.Entry<String, List<TradeAtCity>> entry : stats.entrySet()) {
            retailAnalitincs.put(entry.getKey(), getRetailAnalitincsByProduct(entry.getKey(), shopBy, entry.getValue()));
        }
        return retailAnalitincs;
    }

    public static List<RetailAnalytics> getRetailAnalitincsByProduct(final String productId,
                                                                     final Map<String, List<Shop>> shopBy,
                                                                     final List<TradeAtCity> stats) {
        final Map<String, List<TradeAtCity>> statsBy = stats.parallelStream()
                .filter(tradeAtCity -> tradeAtCity.getProductId().equals(productId))
                .collect(Collectors.groupingBy(TradeAtCity::getCountryRegionTownIds));
        final List<RetailAnalytics> retailAnalitincs = new ArrayList<>();

        for (final Map.Entry<String, List<TradeAtCity>> entry : statsBy.entrySet()) {
            try {
                for (final TradeAtCity tradeAtCity : entry.getValue()) {
                    if (!shopBy.containsKey(entry.getKey())) {
                        continue;
                    }
                    for (final Shop shop : shopBy.get(entry.getKey())) {
                        try {
                            final Optional<ShopProduct> shopProductOpt = shop.getShopProducts().stream().filter(sp -> sp.getProductId().equals(productId)).findFirst();
                            if (!shopProductOpt.isPresent()) {
                                continue;
                            }
                            final ShopProduct shopProduct = shopProductOpt.get();
                            retailAnalitincs.add(
                                    new RetailAnalytics(
                                            shop.getShopSize(),
                                            shop.getTownDistrict(),
                                            shop.getDepartmentCount(),
                                            shop.getNotoriety(),
                                            shop.getVisitorsCount(),
                                            shop.getServiceLevel(),
                                            shopProduct.getSellVolume(),
                                            shopProduct.getPrice(),
                                            shopProduct.getQuality(),
                                            shopProduct.getBrand(),
                                            tradeAtCity.getWealthIndex(),
                                            tradeAtCity.getEducationIndex(),
                                            tradeAtCity.getAverageSalary(),
                                            tradeAtCity.getMarketIdx(),
                                            tradeAtCity.getVolume(),
                                            tradeAtCity.getSellerCnt(),
                                            tradeAtCity.getLocalPercent(),
                                            tradeAtCity.getLocalPrice(),
                                            tradeAtCity.getLocalQuality()
                                    )
                            );
                        } catch (final Exception e) {
                            logger.error(e.getLocalizedMessage(), e);
                        }
                    }
                }
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return retailAnalitincs;
    }
}
