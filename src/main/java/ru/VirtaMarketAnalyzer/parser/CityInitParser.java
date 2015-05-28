package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.VirtaMarketAnalyzer.data.Country;
import ru.VirtaMarketAnalyzer.data.Region;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class CityInitParser {
    public static void main(final String[] args) throws IOException {
        final Document doc = Downloader.getDoc("http://virtonomica.ru/olga/main/globalreport/marketing/by_trade_at_cities/");

        final Elements options = doc.select("option");
        //System.out.println(list.outerHtml());
        for (Element opt : options) {
            if (opt.attr("value").matches("/\\d+/\\d+/\\d+")) {
                System.out.println(opt.text());
                System.out.println(opt.attr("value"));
            }
        }
    }

    public static List<Region> getRegions(final String url, final List<Country> countries) throws IOException {
        final List<Region> list = new ArrayList<>();
        for (final Country country : countries) {
            final Document doc = Downloader.getDoc(url + country.getId());

            final Elements rows = doc.select("table[class=\"grid\"] > tbody > tr > td:nth-child(1) > a");
            //System.out.println(list.outerHtml());
            for (final Element row : rows) {
                final String[] data = row.attr("href").split("/");
                final String caption = row.text();
                list.add(new Region(country.getId(), data[data.length - 1], caption));
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
            final String[] data = row.attr("href").split("/");
            final String caption = row.text();
            list.add(new Country(data[data.length - 1], caption));
        }
        return list;
    }
}