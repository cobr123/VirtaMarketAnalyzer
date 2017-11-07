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

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
//        final Document doc = Downloader.getDoc(Wizard.host + "olga/main/globalreport/marketing/by_trade_at_cities/");
//
//        final Elements options = doc.select("option");
//        //System.out.println(list.outerHtml());
//        options.stream().filter(opt -> opt.attr("value").matches("/\\d+/\\d+/\\d+")).forEach(opt -> {
//            logger.info(opt.text());
//            logger.info(opt.attr("value"));
//        });
        System.out.println(Utils.getGson(getCountries(Wizard.host, "olga")));
        System.out.println(Utils.getGson(getRegions(Wizard.host, "olga")));
    }

    public static List<Region> getRegions(final String host, final String realm) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String json = IOUtils.toString(new URL(host + "api/" + realm + "/main/geo/region/browse?lang=" + lang), Charset.forName("UTF-8"));

        final Gson gson = new Gson();
        final Type mapType = new TypeToken<Map<String, Map<String, String>>>() {
        }.getType();
        final Map<String, Map<String, String>> mapOfRegions = gson.fromJson(json, mapType);

        final List<Region> list = new ArrayList<>();
        for (final String region_id : mapOfRegions.keySet()) {
            final Map<String, String> region = mapOfRegions.get(region_id);

            final String country_id = region.get("country_id");
            final String id = region.get("id");
            final String caption = region.get("name");
            final double incomeTaxRate = Utils.toDouble(region.get("tax"));

            list.add(new Region(country_id, id, caption, incomeTaxRate));
        }
        return list;
    }

    public static List<Country> getCountries(final String host, final String realm) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String json = IOUtils.toString(new URL(host + "api/" + realm + "/main/geo/country/browse?lang=" + lang), Charset.forName("UTF-8"));

        final Gson gson = new Gson();
        final Type mapType = new TypeToken<Map<String, Map<String, String>>>() {
        }.getType();
        final Map<String, Map<String, String>> mapOfCountry = gson.fromJson(json, mapType);

        final List<Country> list = new ArrayList<>();

        for (final String country_id : mapOfCountry.keySet()) {
            final Map<String, String> country = mapOfCountry.get(country_id);

            final String id = country.get("id");
            final String caption = country.get("name");

            list.add(new Country(id, caption));
        }
        return list;
    }
}