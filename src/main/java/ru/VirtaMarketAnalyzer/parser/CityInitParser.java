package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Country;
import ru.VirtaMarketAnalyzer.data.Region;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class CityInitParser {
    private static final Logger logger = LoggerFactory.getLogger(CityInitParser.class);

    public static void main(final String[] args) throws IOException {
        final Document doc = Downloader.getDoc("http://virtonomica.ru/olga/main/globalreport/marketing/by_trade_at_cities/");

        final Elements options = doc.select("option");
        //System.out.println(list.outerHtml());
        options.stream().filter(opt -> opt.attr("value").matches("/\\d+/\\d+/\\d+")).forEach(opt -> {
            logger.info(opt.text());
            logger.info(opt.attr("value"));
        });
    }

    public static List<Region> getRegions(final String url, final List<Country> countries) throws IOException {
        final List<Region> list = new ArrayList<>();
        for (final Country country : countries) {
            final Document doc = Downloader.getDoc(url + country.getId());

            final Elements rows = doc.select("table[class=\"grid\"] > tbody > tr > td:nth-child(1) > a");
            //System.out.println(list.outerHtml());
            for (final Element row : rows) {
                final String id = Utils.getLastFromUrl(row.attr("href"));
                final String caption = row.text();
                list.add(new Region(country.getId(), id, caption));
            }
        }
        return list;
    }

    public static List<Country> getCountries(final String url) throws IOException {
        final Document doc = Downloader.getDoc(url);
        final List<Country> list = new ArrayList<>();

        final Elements rows = doc.select("table[class=\"grid\"] > tbody > tr > td:nth-child(1) > a");
        //System.out.println(list.outerHtml());
        for (final Element row : rows) {
            final String id = Utils.getLastFromUrl(row.attr("href"));
            final String caption = row.text();
            list.add(new Country(id, caption));
        }
        return list;
    }
}