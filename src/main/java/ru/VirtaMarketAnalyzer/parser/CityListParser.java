package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
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

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class CityListParser {
    private static final Logger logger = LoggerFactory.getLogger(CityListParser.class);

    public static List<City> getCities(final String host, final String realm) throws Exception {
        return getCities(host, realm, true);
    }

    public static List<City> getCities(final String host, final String realm, final Boolean withDemography) throws Exception {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/geo/city/browse?lang=" + lang;

        final List<City> list = new ArrayList<>();
        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().text();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> mapOfCities = gson.fromJson(json, mapType);

            for (final String city_id : mapOfCities.keySet()) {
                final Map<String, Object> city = mapOfCities.get(city_id);

                final String id = city.get("id").toString();
                final String region_id = city.get("region_id").toString();
                final String country_id = city.get("country_id").toString();
                final String caption = city.get("city_name").toString();
                final String wealthIndex = city.get("wealth_level").toString();
                final String educationIndex = city.get("education").toString();
                final String averageSalary = city.get("salary").toString();
                final String population = city.get("population").toString();
                int demography = 0;
                if (withDemography) {
                    demography = Utils.repeatOnErr(() -> CityListParser.getDemography(host, realm, city_id));
                }
                final List<String> mayoralBonuses = new ArrayList<>();
                final int retail_count = Utils.toInt(city.get("retail_count").toString());
                if (retail_count > 0) {
                    final Map<String, Object> mapOfCat = (Map<String, Object>) city.get("retails");
                    for (final String category_id : mapOfCat.keySet()) {
                        final String name = mapOfCat.get(category_id).toString();
                        mayoralBonuses.add(name);
                    }
                }

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
            logger.error(url + "&format=debug");
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
            final String json = doc.body().text();
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
            Downloader.invalidateCache(url);
            logger.error(url + "&format=debug");
            throw e;
        }
        if (demography == -1) {
            Downloader.invalidateCache(url);
            throw new IOException("Значение демографии не найдено, '" + today + "', " + url);
        }
        return demography;
    }

}