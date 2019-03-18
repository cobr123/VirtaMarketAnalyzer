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
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class CityParser {
    private static final Logger logger = LoggerFactory.getLogger(CityParser.class);

    public static List<TradeAtCity> collectByTradeAtCities(final String host, final String realm, final List<City> cities, final Product product) {
        final Map<String, Map<String, List<TradeAtCityBuilder>>> grpByCityByCatId = cities
                .parallelStream()
                .map(city -> new CityProduct(city, product, host, realm).getTradeAtCity())
                .collect(Collectors.groupingBy(TradeAtCityBuilder::getCityId, Collectors.groupingBy(TradeAtCityBuilder::getProductCategoryId)));

        return grpByCityByCatId.keySet().parallelStream()
                .map(cityId -> {
                    final Map<String, List<TradeAtCityBuilder>> grpByCatId = grpByCityByCatId.get(cityId);
                    return grpByCatId.keySet().stream()
                            .map(catId -> {
                                final double localMarketVolumeSumTotal = grpByCatId.get(catId).stream().mapToDouble(TradeAtCityBuilder::getLocalMarketVolumeSum).sum();
                                final double shopMarketVolumeSumTotal = grpByCatId.get(catId).stream().mapToDouble(TradeAtCityBuilder::getShopMarketVolumeSum).sum();
                                return grpByCatId.get(catId).stream()
                                        .map(tacb -> {
                                            tacb.setLocalMarketVolumeSumTotal(localMarketVolumeSumTotal);
                                            tacb.setShopMarketVolumeSumTotal(shopMarketVolumeSumTotal);
                                            return tacb.build();
                                        })
                                        .collect(toList());
                            })
                            .flatMap(Collection::stream)
                            .collect(toList());
                })
                .flatMap(Collection::stream)
                .collect(toList());
    }

    public static List<TradeAtCity> get(
            final String host,
            final String realm,
            final City city,
            final List<Product> products
    ) {
        return products
                .parallelStream()
                .map(product -> new CityProduct(city, product, host, realm).getTradeAtCity().build())
                .collect(toList());
    }

    public static TradeAtCityBuilder get(
            final String host,
            final String realm,
            final City city,
            final Product product
    ) throws Exception {
        final TradeAtCityBuilder builder = new TradeAtCityBuilder();

        builder.setProductId(product.getId());
        builder.setProductCategoryId(product.getProductCategoryID());
        builder.setCountryId(city.getCountryId());
        builder.setRegionId(city.getRegionId());
        builder.setCityId(city.getId());
        builder.setCityCaption(city.getCaption());
        builder.setWealthIndex(city.getWealthIndex());
        builder.setEducationIndex(city.getEducationIndex());
        builder.setAverageSalary(city.getAverageSalary());

        final CountryDutyList importTaxPercent = CountryDutyListParser.getCountryDuty(host, realm, city.getCountryId(), product.getId());
        builder.setImportTaxPercent(importTaxPercent.getImportTaxPercent());

        final Region region = CityInitParser.getRegion(host, realm, city.getRegionId());
        final double incomeTaxRate = region.getIncomeTaxRate();
        builder.setIncomeTaxRate(incomeTaxRate);

        addMetrics(host, realm, city, product, builder);
//        addMajorSellInCity(host, realm, city, product, builder);

        return builder;
    }

    private static void addMajorSellInCity(final String host,
                                           final String realm,
                                           final City city,
                                           final Product product,
                                           final TradeAtCityBuilder builder
    ) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/marketing/report/retail/units?lang=" + lang + "&product_id=" + product.getId() + "&geo=" + city.getGeo();

        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().text();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<List<Map<String, Object>>>() {
            }.getType();
            final List<Map<String, Object>> listOfMapOfMetrics = gson.fromJson(json, mapType);

            final List<MajorSellInCity> majorSellInCityList = new ArrayList<>();
            for (final Map<String, Object> mapOfMetrics : listOfMapOfMetrics) {
                String cityDistrict = "";
                if (mapOfMetrics.get("district_name") == null) {
                    //заправки
                } else {
                    //магазины
                    cityDistrict = mapOfMetrics.get("district_name").toString();
                }
                final String unitId = mapOfMetrics.get("unit_id").toString();
                final long shopSize = Utils.toLong(mapOfMetrics.get("shop_size").toString());
                final double sellVolume = Double.valueOf(mapOfMetrics.get("qty").toString());
                final double price = Double.valueOf(mapOfMetrics.get("price").toString());
                final double quality = Double.valueOf(mapOfMetrics.get("quality").toString());
                final double brand = Double.valueOf(mapOfMetrics.get("brand").toString());
                majorSellInCityList.add(
                        new MajorSellInCity(
                                product.getId(),
                                city.getCountryId(),
                                city.getRegionId(),
                                city.getId(),
                                unitId,
                                shopSize,
                                cityDistrict,
                                sellVolume,
                                price,
                                quality,
                                brand
                        )
                );
            }
            builder.setMajorSellInCityList(majorSellInCityList);
        } catch (final Exception e) {
            logger.error(url + "&format=debug");
            throw e;
        }
    }

    private static void addMetrics(final String host,
                                   final String realm,
                                   final City city,
                                   final Product product,
                                   final TradeAtCityBuilder builder
    ) throws IOException {
        final String url = host + "api/" + realm + "/main/marketing/report/retail/metrics?shares=1&product_id=" + product.getId() + "&geo=" + city.getGeo();

        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().text();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            final Map<String, Object> mapOfMetrics = gson.fromJson(json, mapType);

            builder.setLocalPrice(Double.valueOf(mapOfMetrics.get("local_price").toString()));
            builder.setShopPrice(Double.valueOf(mapOfMetrics.get("avg_price").toString()));

            builder.setLocalQuality(Double.valueOf(mapOfMetrics.get("local_quality").toString()));
            builder.setShopQuality(Double.valueOf(mapOfMetrics.get("avg_quality").toString()));

            builder.setShopBrand(Double.valueOf(mapOfMetrics.get("avg_brand").toString()));

            builder.setVolume(Utils.toLong(mapOfMetrics.get("local_market_size").toString()));
            builder.setSellerCnt(Integer.valueOf(mapOfMetrics.get("shop_count").toString()));
            builder.setCompaniesCnt(Utils.toLong(mapOfMetrics.get("company_count").toString()));

            final int index_min = Integer.valueOf(mapOfMetrics.get("index_min").toString());
            String marketIdx = "";
            switch (index_min) {
                case -1:
                    marketIdx = "AAA";
                    break;
                case 0:
                    marketIdx = "AA";
                    break;
                case 1:
                    marketIdx = "A";
                    break;
                case 2:
                    marketIdx = "B";
                    break;
                case 3:
                    marketIdx = "C";
                    break;
                case 4:
                    marketIdx = "D";
                    break;
                case 5:
                    marketIdx = "E";
                    break;
                case 6:
                    marketIdx = "?";
                    break;
            }
            builder.setMarketIdx(marketIdx);

            final List<Map<String, Object>> listOfMapOfShares = (List<Map<String, Object>>) mapOfMetrics.get("shares");
            builder.setLocalPercent(0);
            for (final Map<String, Object> mapOfShares : listOfMapOfShares) {
                if (mapOfShares.get("company_id").toString().equals("-1")) {
                    builder.setLocalPercent(Double.valueOf(mapOfShares.get("market_size").toString()));
                }
            }
        } catch (final Exception e) {
            logger.error(url + "&format=debug");
            throw e;
        }
    }
}
