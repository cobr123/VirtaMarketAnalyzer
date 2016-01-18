package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Collector;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.Shop;
import ru.VirtaMarketAnalyzer.data.ShopProduct;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 16.01.16.
 */
public final class ShopParser {
    private static final Logger logger = LoggerFactory.getLogger(ShopParser.class);

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
//        final String url = "http://virtonomica.ru/olga/main/unit/view/5788675";
        final String url = "http://virtonomica.ru/mary/main/unit/view/3463932";
        final List<City> cities = new ArrayList<>();
        cities.add(new City("7060", "7062", "7073", "Астана", 0.0, 0.0, 0.0));
        final List<Product> products = new ArrayList<>();
        products.add(new Product("категория", "/img/products/bourbon.gif", "123", "Бурбон"));
        System.out.println(Utils.getPrettyGson(parse(url, cities, products)));
    }

    public static Shop parse(final String url, final List<City> cities, final List<Product> products) throws IOException {
        final Document doc = Downloader.getDoc(url);
        final Map<String, List<Product>> productsByImgSrc = products.stream().collect(Collectors.groupingBy(Product::getImgUrl));

        final String countryId = Utils.getLastFromUrl(doc.select("table.infoblock > tbody > tr:nth-child(1) > td:nth-child(2) > a:nth-child(1)").attr("href"));
        final String regionId = Utils.getLastFromUrl(doc.select("table.infoblock > tbody > tr:nth-child(1) > td:nth-child(2) > a:nth-child(2)").attr("href"));
        doc.select("table.infoblock > tbody > tr:nth-child(1) > td:nth-child(2)").first().children().remove();
        final String dyrtyCaption = doc.select("table.infoblock > tbody > tr:nth-child(1) > td:nth-child(2)").text();
        final String dyrtyCaptionReplaced = dyrtyCaption.replaceFirst("\\([^\\)]*\\)$", "").trim()
                .replace("San Diego","Сан-Диего")
                .replace("Indianapolis","Индианаполис")
                .replace("San Luis Potosí","Сан-Луис-Потоси")
                .replace("San Luis Potosi","Сан-Луис-Потоси")
                .replace("León","Леон")
                .replace("Filadelfia","Филадельфия")
                .replace("Tartu","Тарту")
                .replace("Belfast","Белфаст")
                .replace("Lisburn","Лисберн")
                .replace("Leeds","Лидс")
                .replace("Coventry","Ковентри")
                .replace("Bamako","Бамако")
                .replace("Rhodes","Родос");
        String townId = "";
        try {
            townId = cities.stream()
                    .filter(c -> c.getCountryId().equals(countryId))
//                    .filter(c -> c.getRegionId().equals(regionId))
                    .filter(c -> c.getCaption().equals(dyrtyCaptionReplaced))
                    .findFirst().get().getId();
        } catch (final Exception e) {
            logger.info(url);
            cities.stream()
                    .filter(c -> c.getCountryId().equals(countryId))
//                    .filter(c -> c.getRegionId().equals(regionId))
                    .forEach(c -> logger.info("'{}'", c.getCaption()));
            logger.info("'countryId = {}'", countryId);
            logger.info("'regionId = {}'", regionId);
            logger.info("'dyrtyCaption = {}'", dyrtyCaptionReplaced);
            throw e;
        }
        final int shopSize = Utils.toInt(doc.select("table.infoblock > tbody > tr:nth-child(3) > td:nth-child(2)").text());
        final String townDistrict = doc.select("table.infoblock > tbody > tr:nth-child(2) > td:nth-child(2)").text();
        final double departmentCount = Utils.toDouble(doc.select("table.infoblock > tbody > tr:nth-child(4) > td:nth-child(2)").text());
        final double notoriety = Utils.toDouble(doc.select("table.infoblock > tbody > tr:nth-child(5) > td:nth-child(2)").text());
        final String visitorsCount = doc.select("table.infoblock > tbody > tr:nth-child(6) > td:nth-child(2)").text().trim();
        final String serviceLevel = doc.select("table.infoblock > tbody > tr:nth-child(7) > td:nth-child(2)").text();

        final List<ShopProduct> shopProducts = new ArrayList<>();
        final Elements rows = doc.select("table[class=\"grid\"] > tbody > tr[class]");
        for (final Element row : rows) {
            try {
                if ("не изв.".equalsIgnoreCase(row.select("> td:nth-child(3)").first().text())) {
                    continue;
                }
                if (row.select("> td:nth-child(1) > img").first().attr("src").contains("/brand/")) {
                    continue;
                }
                final String productId = productsByImgSrc.get(row.select("> td:nth-child(1) > img").first().attr("src")).get(0).getId();
                final String sellVolume = row.select("> td:nth-child(2)").first().text().trim();
                final double quality = Utils.toDouble(row.select("> td:nth-child(3)").first().text());
                final double brand = Utils.toDouble(row.select("> td:nth-child(4)").first().text());
                final double price = Utils.toDouble(row.select("> td:nth-child(5)").first().text());
                final double marketShare = Utils.toDouble(row.select("> td:nth-child(6)").first().text());

                final ShopProduct shopProduct = new ShopProduct(productId, sellVolume, price, quality, brand, marketShare);
                shopProducts.add(shopProduct);
            } catch (final Exception e) {
                logger.info("rows.size() = " + rows.size());
                logger.error(row.outerHtml());
                logger.error(e.getLocalizedMessage(), e);
            }
        }

        return new Shop(countryId, regionId, townId, shopSize, townDistrict, departmentCount, notoriety,
                visitorsCount, serviceLevel, shopProducts);
    }
}
