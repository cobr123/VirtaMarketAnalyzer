package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.RentAtCity;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by cobr123 on 11.12.16.
 */
public final class RentAtCityParser {
    private static final Logger logger = LoggerFactory.getLogger(RentAtCityParser.class);

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        final List<RentAtCity> list = getUnitTypeRent(Wizard.host, "olga", "422077");
        logger.info(Utils.getPrettyGson(list));
        System.out.println("list.size() = " + list.size());
    }

    public static List<RentAtCity> getUnitTypeRent(final String host, final String realm, final List<City> cities) throws IOException {
        return cities.parallelStream()
                .map(city -> {
                    try {
                        return getUnitTypeRent(host, realm, city.getId());
                    } catch (final Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    public static List<RentAtCity> getUnitTypeRent(final String host, final String realm, final String cityId) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/geo/city/rent?city_id=" + cityId + "&lang=" + lang;

        final List<RentAtCity> list = new ArrayList<>();
        try {
            final String json = Downloader.getJson(url);
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<List<Map<String, Object>>>() {
            }.getType();
            final List<Map<String, Object>> rentList = gson.fromJson(json, mapType);

            for (final Map<String, Object> rent : rentList) {
                final String unit_class_kind = rent.get("unit_class_kind").toString();
                final double areaRent = Utils.round2(Double.valueOf(rent.get("rent_cost").toString()));
                final double workplaceRent = 0;

                list.add(new RentAtCity("/img/unit_types/" + unit_class_kind + ".gif", cityId, areaRent, workplaceRent));
            }
        } catch (final Exception e) {
            logger.error(url + "&format=debug");
            throw e;
        }
        return list;
    }
}
