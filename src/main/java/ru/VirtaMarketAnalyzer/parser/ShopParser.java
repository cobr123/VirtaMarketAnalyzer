package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by cobr123 on 16.01.16.
 */
public final class ShopParser {
    private static final Logger logger = LoggerFactory.getLogger(ShopParser.class);

    public static Shop getShop(final String host, final String realm, final Product product, final MajorSellInCity majorSellInCity, final List<TradeAtCity> stats) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/unit/summary?lang=" + lang + "&id=" + majorSellInCity.getUnitId();

        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().text();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            final Map<String, Object> mapOfMetrics = gson.fromJson(json, mapType);

            final TradeAtCity stat = stats.stream().filter(msc -> msc.getGeo().equals(majorSellInCity.getGeo())).findFirst().get();
            final List<ShopProduct> shopProducts = new ArrayList<>();
            final double marketShare = Utils.round2(majorSellInCity.getSellVolume() / (double) stat.getVolume() * 100.0);
            shopProducts.add(new ShopProduct(product.getId(), (long) majorSellInCity.getSellVolume(), majorSellInCity.getPrice(), majorSellInCity.getQuality(), majorSellInCity.getBrand(), marketShare));

            final int shopSize = Integer.valueOf(mapOfMetrics.get("square").toString());
            int departmentCount = 1;
            if (mapOfMetrics.get("section_count") != null) {
                departmentCount = Integer.valueOf(mapOfMetrics.get("section_count").toString());
            }
            final double notoriety = Utils.round2(Utils.toDouble(mapOfMetrics.get("fame").toString()) * 100.0);
            final String visitorsCount = mapOfMetrics.get("customers_count").toString();
            final String serviceLevel = getServiceLevel(host, realm, majorSellInCity.getUnitId());

            return new Shop(majorSellInCity.getCountryId(), majorSellInCity.getRegionId(), majorSellInCity.getTownId()
                    , shopSize, majorSellInCity.getTownDistrict(), departmentCount, notoriety, visitorsCount, serviceLevel, shopProducts
            );
        } catch (final IOException e) {
            logger.error(url + "&format=debug");
            throw e;
        }
    }

    private static String getServiceLevel(final String host, final String realm, final String unitId) {
        final String url = host + realm + "/main/unit/view/" + unitId;
        try {
            final Document doc = Downloader.getDoc(url, 3);

            if (doc.select("table.unit_table").size() == 0) {
                //заправки
                final String serviceLevel = doc.select("table.infoblock > tbody > tr:nth-child(5) > td:nth-child(2)").text();
                return serviceLevel;
            } else if (doc.select("table.unit_table").size() == 2) {
                //магазины
                final String serviceLevel = doc.select("table.unit_table > tbody > tr:nth-child(6) > td:nth-child(2)").text();
                return serviceLevel;
            } else {
                logger.error("Неизвестный тип юнита, url = {}. Возможно еще идет пересчет.", url);
                return "";
            }
        } catch (final Exception e) {
            logger.error("url = {}", url);
            return "";
        }
    }
}
