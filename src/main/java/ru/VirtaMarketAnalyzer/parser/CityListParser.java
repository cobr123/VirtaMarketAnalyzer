package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.Region;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class CityListParser {
    public static void main(final String[] args) throws IOException {
        final Document doc = Downloader.getDoc("http://virtonomica.ru/olga/main/geo/citylist/331858");
        final Element table = doc.select("table[class=\"grid\"]").last();
        //System.out.println(list.outerHtml());
        final Elements towns = table.select("table > tbody > tr");
        for (Element town : towns) {
            final String[] parts = town.select("tr > td:nth-child(1) > a").eq(0).attr("href").split("/");
            System.out.println(parts[parts.length - 1]);
            System.out.println(Utils.toDouble(town.select("tr > td:nth-child(6)").html()));
        }
    }

    public static List<City> fillWealthIndex(final String url, final List<Region> regions) throws IOException {
        final List<City> cities = new ArrayList<>();
        for (final Region region : regions) {
            getWealthIndex(url, region, cities);
        }
        return cities;
    }

    public static void getWealthIndex(final String url, final Region region, final List<City> cities) throws IOException {
        final Document doc = Downloader.getDoc(url + region.getId());
        final Element table = doc.select("table[class=\"grid\"]").last();
        //System.out.println(list.outerHtml());
        final Elements towns = table.select("table > tbody > tr");
        for (Element town : towns) {
            final String[] parts = town.select("tr > td:nth-child(1) > a").eq(0).attr("href").split("/");
            final String caption = town.select("tr > td:nth-child(1) > a").eq(0).text();
            final String id = parts[parts.length - 1];
            final String wealthIndex = town.select("tr > td:nth-child(6)").html();
            cities.add(new City(region.getCountryId(), region.getId(), id, caption, Utils.toDouble(wealthIndex)));
        }
    }
}
