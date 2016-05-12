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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class CityParser {
    private static final Logger logger = LoggerFactory.getLogger(CityParser.class);

    public static void main(final String[] args) throws IOException {
        //final Document doc = Downloader.getDoc("http://virtonomica.ru/olga/main/globalreport/marketing/by_trade_at_cities/422433/422607/422608/422622");
        final Document doc = Downloader.getDoc("http://virtonomica.ru/olga/main/globalreport/marketing/by_trade_at_cities/422549/3054/3055/3056");
        final Element table = doc.select("table[class=\"grid\"]").first();
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

        final Element list = doc.select("table[class=\"list\"]").last();
        //System.out.println(list.outerHtml());
        final Elements bestInTown = list.select("table > tbody > tr");
        for (final Element best : bestInTown) {
            if (!best.select("tr > td:nth-child(1) > div:nth-child(2) > img").eq(0).attr("title").isEmpty()) {
                final String unitID = Utils.getLastFromUrl(best.select("tr > td:nth-child(1) > div:nth-child(1) > a:nth-child(2)").attr("href"));

                System.out.println(unitID);
            }
        }
    }

    public static Map<String, List<TradeAtCity>> collectByTradeAtCities(final String url, final List<City> cities, final List<Product> products, final Map<String, List<CountryDutyList>> countriesDutyList, final List<Region> regions) throws IOException {
        logger.info("парсим данные: {}", cities.size() * products.size());
        return cities
                .parallelStream()
                .flatMap(city -> products.stream().map(product -> new CityProduct(city, product, url)))
                .map(city -> city.getTradeAtCity(countriesDutyList, regions))
                .collect(Collectors.groupingBy(TradeAtCity::getProductId));
    }

    public static TradeAtCity get(final String url, final City city, final Product product, final Map<String, List<CountryDutyList>> countriesDutyList, final List<Region> regions) throws Exception {
        final String fullUrl = url + product.getId() + "/" + city.getCountryId() + "/" + city.getRegionId() + "/" + city.getId();
        final Document doc = Downloader.getDoc(fullUrl);
        final Element table = doc.select("table.grid").first();

        if (table == null) {
            Downloader.invalidateCache(fullUrl);
            throw new IOException("На странице '" + fullUrl + "' не найдена таблица с классом grid");
        }

        final TradeAtCityBuilder builder = new TradeAtCityBuilder();

        builder.setMarketIdx(table.select("table > tbody > tr > td").eq(2).text().replaceAll("[\\W]+", ""));
        builder.setVolume(Utils.toLong(table.select("table > tbody > tr > td").eq(4).text()));
        builder.setSellerCnt(Utils.toInt(table.select("table > tbody > tr > td").eq(6).text()));
        builder.setCompaniesCnt(Utils.toLong(table.select("table > tbody > tr > td").eq(8).text()));

        builder.setProductId(product.getId());
        builder.setCountryId(city.getCountryId());
        builder.setRegionId(city.getRegionId());
        builder.setCityId(city.getId());
        builder.setCityCaption(city.getCaption());
        builder.setWealthIndex(city.getWealthIndex());
        builder.setEducationIndex(city.getEducationIndex());
        builder.setAverageSalary(city.getAverageSalary());

        if (!countriesDutyList.containsKey(city.getCountryId())) {
            throw new Exception("Не найдены таможенные пошлины для страны " + url.replace("/main/globalreport/marketing/by_trade_at_cities/", "/main/geo/countrydutylist/") + city.getCountryId());
        }
        final Optional<CountryDutyList> importTaxPercent = countriesDutyList.get(city.getCountryId())
                .stream().filter(cdl -> cdl.getProductId().equals(product.getId())).findFirst();

        if (!importTaxPercent.isPresent()) {
            throw new Exception("Не найдены таможенные пошлины для продукта '" + product.getCaption() + "', id = '" + product.getId() + "', " + url.replace("/main/globalreport/marketing/by_trade_at_cities/", "/main/geo/countrydutylist/") + city.getCountryId());
        }
        builder.setImportTaxPercent(importTaxPercent.get().getImportTaxPercent());
        final double incomeTaxRate = regions.stream().filter(r -> r.getId().equals(city.getRegionId())).findFirst().get().getIncomeTaxRate();
        builder.setIncomeTaxRate(incomeTaxRate);

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
        for (final Element best : bestInTown) {
            if (!best.select("tr > td:nth-child(1) > div:nth-child(2) > img").eq(0).attr("title").isEmpty()) {
                final String unitUrl = best.select("tr > td:nth-child(1) > div:nth-child(1) > a:nth-child(2)").attr("href");
                final long shopSize = Utils.toLong(best.select("tr > td").eq(1).html());
                final String cityDistrict = best.select("tr > td").eq(2).html();
                final double sellVolume = Utils.toDouble(best.select("tr > td").eq(3).html());
                final double price = Utils.toDouble(best.select("tr > td").eq(4).html());
                final double quality = Utils.toDouble(best.select("tr > td").eq(5).html());
                final double brand = Utils.toDouble(best.select("tr > td").eq(6).html());

                majorSellInCityList.add(
                        new MajorSellInCity(
                                city.getCountryId(),
                                city.getRegionId(),
                                city.getId(),
                                unitUrl,
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
        builder.setMajorSellInCityList(majorSellInCityList);
        return builder.build();
    }
}
