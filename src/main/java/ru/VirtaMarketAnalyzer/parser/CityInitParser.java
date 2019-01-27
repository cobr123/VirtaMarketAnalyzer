package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Country;
import ru.VirtaMarketAnalyzer.data.Region;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class CityInitParser {
    private static final Logger logger = LoggerFactory.getLogger(CityInitParser.class);

    public static List<Region> getRegions(final String host, final String realm) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/geo/region/browse?lang=" + lang;

        final List<Region> list = new ArrayList<>();
        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().html();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> mapOfRegions = gson.fromJson(json, mapType);

            for (final String region_id : mapOfRegions.keySet()) {
                final Map<String, Object> region = mapOfRegions.get(region_id);

                final String country_id = region.get("country_id").toString();
                final String id = region.get("id").toString();
                final String caption = region.get("name").toString();
                final double incomeTaxRate = Utils.toDouble(region.get("tax").toString());

                list.add(new Region(country_id, id, caption, incomeTaxRate));
            }
        } catch (final Exception e) {
            logger.error(url);
            throw e;
        }
        return list;
    }

    public static List<Country> getCountries(final String host, final String realm) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/geo/country/browse?lang=" + lang;

        final List<Country> list = new ArrayList<>();
        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().html();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> mapOfCountry = gson.fromJson(json, mapType);


            for (final String country_id : mapOfCountry.keySet()) {
                final Map<String, Object> country = mapOfCountry.get(country_id);

                final String id = country.get("id").toString();
                final String caption = country.get("name").toString();

                list.add(new Country(id, caption));
            }
        } catch (final Exception e) {
            logger.error(url);
            throw e;
        }
        return list;
    }
}