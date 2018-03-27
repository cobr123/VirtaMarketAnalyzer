package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.Region;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class CityListParser {
    private static final Logger logger = LoggerFactory.getLogger(CityListParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));

        final List<City> list = CityListParser.getCities(Wizard.host, "olga");
        System.out.println(list.size());
    }

    public static List<City> getCities(final String host, final String realm) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/geo/city/browse?lang=" + lang;

        final List<City> list = new ArrayList<>();
        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().html();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> mapOfCities = gson.fromJson(json, mapType);

            for (final String city_id : mapOfCities.keySet()) {
                final Map<String, Object> city = mapOfCities.get(city_id);

                final String id = city.get("id").toString();
                final String region_id = city.get("region_id").toString();
                final String country_id = city.get("country_id").toString();
                final String caption = city.get("name").toString();
                final String wealthIndex = city.get("wealth_level").toString();
                final String educationIndex = city.get("education").toString();
                final String averageSalary = city.get("salary").toString();
                final String population = city.get("population").toString();
                final int demography = CityListParser.getDemography(host, realm, city_id);
                final List<String> mayoralBonuses = CityListParser.getMayoralBonuses(host, realm, city_id);

                list.add(new City(country_id, region_id, id, caption
                        , Utils.toDouble(wealthIndex)
                        , Utils.toDouble(educationIndex)
                        , Utils.toDouble(averageSalary)
                        , demography
                        , Utils.toInt(population)
                        , mayoralBonuses
                ));
            }
        } catch (final Exception e) {
            logger.error(url);
            throw e;
        }
        return list;
    }

    public static int getDemography(final String host, final String realm, final String city_id) throws IOException {
        final String url = host + "api/" + realm + "/main/geo/city/history?city_id=" + city_id;

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String today = df.format(new Date());
        int demography = -1;
        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().html();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<List<Map<String, Object>>>() {
            }.getType();
            final List<Map<String, Object>> listOfHist = gson.fromJson(json, mapType);

            for (final Map<String, Object> hist : listOfHist) {
                final String real_date = hist.get("real_date").toString();
                if (real_date.equals(today)) {
                    final String population_real = hist.get("population_real").toString();
                    demography = Utils.toInt(population_real);
                    break;
                }
            }
        } catch (final Exception e) {
            logger.error(url);
            throw e;
        }
        if (demography == -1) {
            throw new IOException("Значение демографии не найдено");
        }
        return demography;
    }

    public static List<String> getMayoralBonuses(final String host, final String realm, final String city_id) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/geo/city/bonus?city_id=" + city_id + "&lang=" + lang;

        final List<String> mayoralBonuses = new ArrayList<>();
        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().html();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            final Map<String, Object> mapOfBonusAndRestrictions = gson.fromJson(json, mapType);

            final Object mapOfBonuses = mapOfBonusAndRestrictions.get("retails");

            if (mapOfBonuses != null) {
                final Map<String, Map<String, Object>> mapOfCat = (Map<String, Map<String, Object>>) mapOfBonuses;
                for (final String category_id : mapOfCat.keySet()) {
                    final Map<String, Object> bonus = mapOfCat.get(category_id);
                    final String name = bonus.get("name").toString();
                    mayoralBonuses.add(name);
                }
            }
        } catch (final Exception e) {
            logger.error(url);
            throw e;
        }
        return mayoralBonuses;
    }

}