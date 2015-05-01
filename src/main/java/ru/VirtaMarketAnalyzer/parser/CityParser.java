package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class CityParser {
    private static final Logger logger = LoggerFactory.getLogger(CityParser.class);

    public static void main(final String[] args) throws IOException {
        //final Document doc = Downloader.getDoc("http://virtonomica.ru/olga/main/globalreport/marketing/by_trade_at_cities/422433/422607/422608/422622");
        final Document doc = Downloader.getDoc("http://virtonomica.ru/olga/main/globalreport/marketing/by_trade_at_cities/422433/335165/335166/422021");
        final Element table = doc.select("table[class=\"grid\"]").first();
//        System.out.println(table.outerHtml());
//        System.out.println(table.select("table > tbody > tr > td").eq(2).text().replaceAll("[\\W]+", ""));
//        System.out.println(table.select("table > tbody > tr > td:nth-child(5)").text());
//        System.out.println(table.select("table > tbody > tr > td").eq(6).text());
//        System.out.println(table.select("table > tbody > tr > td").eq(8).text());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td > img").attr("src"));
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td > table > tbody > tr > td").eq(4).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td > table > tbody > tr:nth-child(3) > td").eq(4).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(2) > td").eq(0).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(2) > td").eq(1).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(3) > td").eq(0).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(3) > td").eq(1).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(4) > td").eq(0).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(4) > td").eq(1).html());

        final Elements percs = table.nextElementSibling().select("table > tbody > tr > td > table > tbody > tr > td");

        for (int i = 0; i < percs.size(); ++i) {
            if ("Местные поставщики".equals(percs.eq(i).text())) {
                System.out.println("Местные поставщики");
                if (percs.eq(i + 2).text().contains("%")) {
                    System.out.println(Utils.toDouble(percs.eq(i + 2).html()));
                }
                break;
            }
        }
//        final Element list = doc.select("table[class=\"list\"]").last();
//        System.out.println(list.outerHtml());
//        final Elements bestInTown = list.select("table > tbody > tr");
//        for (Element best : bestInTown) {
//            System.out.println(best.select("tr > td:nth-child(1) > div:nth-child(2) > img").eq(0).attr("title"));
//            best.select("tr > td:nth-child(1) > div:nth-child(2) > img").eq(0).remove();
//            System.out.println(best.select("tr > td:nth-child(1) > div:nth-child(2)").html().replace("&nbsp;", " ").trim());
//            System.out.println(Utils.toLong(best.select("tr > td").eq(1).html()));
//            System.out.println(best.select("tr > td").eq(2).html());
//            System.out.println(Utils.toLong(best.select("tr > td").eq(3).html()));
//            System.out.println(Utils.toDouble(best.select("tr > td").eq(4).html()));
//            System.out.println(Utils.toDouble(best.select("tr > td").eq(5).html()));
//            System.out.println(Utils.toDouble(best.select("tr > td").eq(6).html()));
//        }
    }

    public static Map<String, List<TradeAtCity>> collectByTradeAtCities(final String url, final List<City> cities, final List<Product> products) throws IOException {
        final Map<String, List<TradeAtCity>> map = new HashMap<>();
        final long total = cities.size() * products.size();
        long cnt = 1;
        final List<String> urls = new ArrayList<>(cities.size() * products.size());

        for (final City city : cities) {
            for (final Product product : products) {
                urls.add(url + product.getId() + "/" + city.getCountryId() + "/" + city.getRegionId() + "/" + city.getId());
            }
        }
        logger.info("греем кэш");
        urls.parallelStream().forEach(s -> {
            try {
                Downloader.get(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        logger.info("парсим данные");
        long prevPerc = 0;
        for (final City city : cities) {
            for (final Product product : products) {
                final long curPerc = cnt * 100 / total;
                if (prevPerc != curPerc) {
                    logger.info("{} из {}, {}%", cnt, total, curPerc);
                }
                if (!map.containsKey(product.getId())) {
                    map.put(product.getId(), new ArrayList<>());
                }
                map.get(product.getId()).add(get(url, city, product));
//                if (cnt > 10) {
//                    //todo: test only
//                    return map;
//                }
                ++cnt;
                prevPerc = curPerc;
            }
        }
        return map;
    }

    public static TradeAtCity get(final String url, final City city, final Product product) throws IOException {
        final Document doc = Downloader.getDoc(url + product.getId() + "/" + city.getCountryId() + "/" + city.getRegionId() + "/" + city.getId());
        final Element table = doc.select("table[class=\"grid\"]").first();

        final TradeAtCityBuilder builder = new TradeAtCityBuilder();

        builder.setMarketIdx(table.select("table > tbody > tr > td").eq(2).text().replaceAll("[\\W]+", ""));
        builder.setVolume(Utils.toLong(table.select("table > tbody > tr > td").eq(4).text()));
        builder.setSellerCnt(Utils.toLong(table.select("table > tbody > tr > td").eq(6).text()));
        builder.setCompaniesCnt(Utils.toLong(table.select("table > tbody > tr > td").eq(8).text()));

        builder.setProductId(product.getId());
        builder.setCountryId(city.getCountryId());
        builder.setRegionId(city.getRegionId());
        builder.setCityId(city.getId());
        builder.setCityCaption(city.getCaption());
        builder.setWealthIndex(city.getWealthIndex());

        final Elements percs = table.nextElementSibling().select("table > tbody > tr > td > table > tbody > tr > td");
        for (int i = 0; i < percs.size(); ++i) {
            if ("Местные поставщики".equals(percs.eq(i).text())) {
                if (percs.eq(i + 2).text().contains("%")) {
                    builder.setLocalPercent(Utils.toDouble(percs.eq(i + 2).html()));
                }
                break;
            }
        }

        builder.setLocalPrice(Utils.toDouble(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(2) > td").eq(0).html()));
        builder.setShopPrice(Utils.toDouble(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(2) > td").eq(1).html()));

        builder.setLocalQuality(Utils.toDouble(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(3) > td").eq(0).html()));
        builder.setShopQuality(Utils.toDouble(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(3) > td").eq(1).html()));

        builder.setShopBrand(Utils.toDouble(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(4) > td").eq(1).html()));

        final List<MajorSellInCity> majorSellInCityList = new ArrayList<>();
        final Element list = doc.select("table[class=\"list\"]").last();
        //System.out.println(list.outerHtml());
        final Elements bestInTown = list.select("table > tbody > tr");
        for (Element best : bestInTown) {
            if (!best.select("tr > td:nth-child(1) > div:nth-child(2) > img").eq(0).attr("title").isEmpty()) {
                final long shopSize = Utils.toLong(best.select("tr > td").eq(1).html());
                final String cityDistrict = best.select("tr > td").eq(2).html();
                final long sellVolume = Utils.toLong(best.select("tr > td").eq(3).html());
                final double price = Utils.toDouble(best.select("tr > td").eq(4).html());
                final double quality = Utils.toDouble(best.select("tr > td").eq(5).html());
                final double brand = Utils.toDouble(best.select("tr > td").eq(6).html());

                majorSellInCityList.add(
                        new MajorSellInCity(
                                shopSize,
                                cityDistrict,
                                sellVolume,
                                price,
                                quality,
                                brand
                        )
                );
            }
        }
        //builder.setMajorSellInCityList(majorSellInCityList);

        return builder.build();
    }
}
