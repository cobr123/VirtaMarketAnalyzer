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
        final List<Country> countries = CityInitParser.getCountries(host + realm + "/main/common/main_page/game_info/world/");
        final List<Region> regions = CityInitParser.getRegions(host + realm + "/main/geo/regionlist/", countries);
        final List<City> cities = CityListParser.fillWealthIndex(host, realm, regions);

        final City city = new City("1480", "359837", "359838", "Алмере", 0, 0, 0, 0, 0, null);
        final Product product = new Product("", "359845", "", "", "");

        final List<Transport> list = TransportParser.parseTransport(host, realm, cities, city, product);
        System.out.println(Utils.getPrettyGson(list));
        System.out.println(list.size());
    }

    public static List<Transport> parseTransport(final String host, final String realm, final List<City> cities, final City cityFrom, final Product material) throws IOException {
        final List<Transport> list = new ArrayList<>();

        final String url = host + realm + "/main/geo/transport/";
        String nextPageUrl = url + material.getId()
                + "/" + cityFrom.getCountryId() + "/" + cityFrom.getRegionId() + "/" + cityFrom.getId()
                + "/" + material.getId();
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
            ref = url + material.getId();
            if (nextPageUrl.isEmpty()) {
                break;
            }
        }

        return list;
    }
}
