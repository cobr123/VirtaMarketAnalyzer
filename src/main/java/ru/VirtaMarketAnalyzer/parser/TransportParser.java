package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cobr123 on 29.06.2017.
 */
public final class TransportParser {
    private static final Logger logger = LoggerFactory.getLogger(TransportParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        final String host = Wizard.host;
        final String realm = "olga";
        logger.info("begin");
        final List<Country> countries = CityInitParser.getCountries(host, realm);
        logger.info("countries.size = {}", countries.size());
        final List<Region> regions = CityInitParser.getRegions(host, realm);
        logger.info("regions.size = {}", regions.size());
        final List<City> cities = CityListParser.fillWealthIndex(host, realm, regions);
        logger.info("cities.size = {}", cities.size());
        final List<Product> materials = ProductInitParser.getManufactureProducts(host, realm);
        logger.info("materials.size = {}", materials.size());
        logger.info("парсим транспортные расходы, {}", materials.size() * cities.size());

        TransportParser.setRowsOnPage(host, realm, Math.max(400, cities.size()), cities.get(0), materials.get(0));
        final List<Transport> list = TransportParser.parseTransport(host, realm, cities, cities.get(0), materials.get(0));
        logger.info(Utils.getPrettyGson(list));
        logger.info("list.size = {}", list.size());
    }

    public static void setRowsOnPage(final String host, final String realm, final int cnt, final City cityFrom, final Product material) throws IOException {
        final String ref = host + realm + "/main/geo/transport/" + cityFrom.getId()
                + "/" + material.getId() + "/" + cityFrom.getCountryId() + "/" + cityFrom.getRegionId()
                + "/" + cityFrom.getId();
        Downloader.getDoc(host + realm + "/main/common/util/setpaging/dbproduct/transportReport/" + cnt, ref);
    }

    public static List<Transport> parseTransport(final String host, final String realm, final List<City> cities, final City cityFrom, final Product material) throws IOException {
        final List<Transport> list = new ArrayList<>();

        final String baseUrl = host + realm + "/main/geo/transport/" + cityFrom.getId()
                + "/" + material.getId() + "/" + cityFrom.getCountryId() + "/" + cityFrom.getRegionId()
                + "/" + cityFrom.getId();
        String nextPageUrl = baseUrl;
        String ref = "";
        for (; ; ) {
            final Document doc = Downloader.getDoc(nextPageUrl, ref);
            final Elements rows = doc.select("table[class=\"unit-list-2014\"] > tbody > tr[class]");

            for (final Element row : rows) {
                if (row.select("> td").size() == 8) {
                    final String cityCaption = row.select("> td:nth-child(1)").text().trim();

                    final String cityToId = cities.stream().filter(c -> c.getCaption().equalsIgnoreCase(cityCaption)).findAny().get().getId();
                    final int distance = Utils.toInt(row.select("> td:nth-child(2)").text());
                    final double deliverOne = Utils.toDouble(row.select("> td:nth-child(3)").text());
                    final double minCostExport = Utils.toDouble(row.select("> td:nth-child(4)").text());
                    final double totalCostExport = Utils.toDouble(row.select("> td:nth-child(5)").text());
                    final double minCostImport = Utils.toDouble(row.select("> td:nth-child(6)").text());
                    final double totalCostImport = Utils.toDouble(row.select("> td:nth-child(7)").text());
                    final String imgSrc = row.select("> td:nth-child(8) > img").attr("src");
                    list.add(new Transport(cityToId, material.getId()
                            , distance, deliverOne
                            , minCostExport, totalCostExport
                            , minCostImport, totalCostImport
                            , imgSrc
                    ));
                }
            }
            nextPageUrl = Utils.getNextPageHref(doc);
            ref = baseUrl;
            if (nextPageUrl.isEmpty()) {
                break;
            }
        }

        return list;
    }
}
