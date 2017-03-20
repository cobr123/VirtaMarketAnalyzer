package ru.VirtaMarketAnalyzer.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 16.01.16.
 */
public final class PrepareAnalitics {
    private static final Logger logger = LoggerFactory.getLogger(PrepareAnalitics.class);

    public static Map<String, List<RetailAnalytics>> getRetailAnalitincsByProducts(
            final List<Shop> shops
            , final Map<String, List<TradeAtCity>> stats
            , final List<Product> products
            , final List<City> cities
    ) {
        final Map<String, List<Shop>> shopBy = shops.parallelStream().collect(Collectors.groupingBy(Shop::getCountryRegionTownIds));

        final Map<String, List<RetailAnalytics>> retailAnalitincs = new HashMap<>();

        for (final Map.Entry<String, List<TradeAtCity>> entry : stats.entrySet()) {
            final Product product = products.stream().filter(p -> p.getId().equals(entry.getKey())).findFirst().get();
            retailAnalitincs.put(entry.getKey(), getRetailAnalitincsByProduct(product, shopBy, entry.getValue(), cities));
        }
        return retailAnalitincs;
    }

    public static List<RetailAnalytics> getRetailAnalitincsByProduct(final Product product,
                                                                     final Map<String, List<Shop>> shopBy,
                                                                     final List<TradeAtCity> stats,
                                                                     final List<City> cities
    ) {
        final Map<String, List<TradeAtCity>> statsBy = stats.parallelStream()
                .filter(tradeAtCity -> tradeAtCity.getProductId().equals(product.getId()))
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
                            final Optional<ShopProduct> shopProductOpt = shop.getShopProducts().stream().filter(sp -> sp.getProductId().equals(product.getId())).findFirst();
                            if (!shopProductOpt.isPresent()) {
                                continue;
                            }
                            final ShopProduct shopProduct = shopProductOpt.get();
                            retailAnalitincs.add(
                                    new RetailAnalytics(
                                            product.getId(),
                                            product.getProductCategory(),
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
                                            shopProduct.getMarketShare(),
                                            tradeAtCity.getWealthIndex(),
                                            tradeAtCity.getEducationIndex(),
                                            tradeAtCity.getAverageSalary(),
                                            tradeAtCity.getMarketIdx(),
                                            tradeAtCity.getVolume(),
                                            tradeAtCity.getSellerCnt(),
                                            tradeAtCity.getLocalPercent(),
                                            tradeAtCity.getLocalPrice(),
                                            tradeAtCity.getLocalQuality(),
                                            cities.stream().filter(c -> c.getId().equals(tradeAtCity.getTownId())).findAny().get().getDemography()
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
