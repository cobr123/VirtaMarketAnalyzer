package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.RentAtCity;
import ru.VirtaMarketAnalyzer.data.UnitType;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
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

    public static List<RentAtCity> getUnitTypeRent(final String baseUrl, final String realm, final List<City> cities) throws IOException {
        return cities.parallelStream()
                .map(city -> {
                    try {
                        return getUnitTypeRent(baseUrl, realm, city.getId());
                    } catch (final Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toList());
    }

    public static List<RentAtCity> getUnitTypeRent(final String baseUrl, final String realm, final String cityId) throws IOException {
        final Document doc = Downloader.getDoc(baseUrl + realm + "/main/geo/city/" + cityId + "/rent");
        final Elements rows = doc.select("table.list > tbody > tr[class]");

        return rows.stream()
                .map(row -> {
                    final String unitTypeImgSrc = row.select("> td:nth-child(1) > img").attr("src");
                    final double areaRent = Utils.toDouble(row.select("> td:nth-child(2)").text());
                    final double workplaceRent = Utils.toDouble(row.select("> td:nth-child(3)").text());
                    return new RentAtCity(unitTypeImgSrc, cityId, areaRent, workplaceRent);
                })
                .collect(toList());

    }
}
