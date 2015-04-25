package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.File;
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

    public static List<City> getCities(final String url) throws IOException {
        final Document doc = Downloader.getDoc(url);
        final List<City> list = new ArrayList<>();

        final Elements options = doc.select("option");
        //System.out.println(list.outerHtml());
        for (final Element opt : options) {
            if (opt.attr("value").matches("/\\d+/\\d+/\\d+")) {
                final String[] data = opt.attr("value").substring(1).split("/");
                list.add(new City(data[0], data[1],data[2], opt.text()));
            }
        }
        return list;
    }
}
