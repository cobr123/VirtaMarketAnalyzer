package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 26.02.16.
 */
public final class ServiceAtCityParser {
    private static final Logger logger = LoggerFactory.getLogger(ServiceAtCityParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %c %x - %m%n")));
//http://virtonomica.ru/olga/main/globalreport/marketing/by_service/422825/422607/422609/422626
        final String realm = "olga";
        final List<Country> countries = CityInitParser.getCountries(Wizard.host + realm + "/main/common/main_page/game_info/world/");
        final List<Region> regions = CityInitParser.getRegions(Wizard.host + realm + "/main/geo/regionlist/", countries);
        final ServiceAtCity city = get(Wizard.host, realm, new City("422607", "422609", "422626", "Агуаскальентес", 10, 0, 0), "422825", regions);
        logger.info(Utils.getPrettyGson(city));
    }

    public static ServiceAtCity get(final String host, final String realm, final City city, final String serviceId, final List<Region> regions) throws IOException {
        final String fullUrl = host + realm + "/main/globalreport/marketing/by_service/" + serviceId + "/" + city.getCountryId() + "/" + city.getRegionId() + "/" + city.getId();
        final Document doc = Downloader.getDoc(fullUrl);
        final Element table = doc.select("table.grid").first();

        if (table == null) {
            Downloader.invalidateCache(fullUrl);
            throw new IOException("На странице '" + fullUrl + "' не найдена таблица с классом grid");
        }

        final double marketDevelopmentIndex = Utils.toDouble(table.select(" > tbody > tr > td:nth-child(1) > b").text());
        final long volume = Utils.toLong(table.select(" > tbody > tr > td:nth-child(2) > b").text());
        final int subdivisionsCnt = Utils.toInt(table.select(" > tbody > tr > td:nth-child(3) > b").text());
        final long companiesCnt = Utils.toLong(table.select(" > tbody > tr > td:nth-child(4) > b").text());
        final double price = Utils.toDouble(table.select(" > tbody > tr > td:nth-child(5) > b").text());
        final Map<String, Double> percentBySpec = new HashMap<>();
        table.nextElementSibling().select(" > tbody > tr:nth-child(2) > td:nth-child(2) > table > tbody > tr > td:nth-child(3)").stream()
                .forEach(element -> {
                    final String key = element.text();
                    final Double val = Utils.toDouble(element.nextElementSibling().nextElementSibling().text());
                    percentBySpec.put(key, val);
                });
        final double incomeTaxRate = regions.stream().filter(r -> r.getId().equals(city.getRegionId())).findFirst().get().getIncomeTaxRate();

        return new ServiceAtCity(city.getCountryId()
                , city.getRegionId()
                , city.getId()
                , volume
                , price
                , subdivisionsCnt
                , companiesCnt
                , marketDevelopmentIndex
                , percentBySpec
                , city.getWealthIndex()
                , incomeTaxRate
        );
    }

    public static List<ServiceAtCity> get(final String host, final String realm, final List<City> cities, final String serviceId, final List<Region> regions) {
        return cities.parallelStream().map(city -> {
            try {
                return get(host, realm, city, serviceId, regions);
            } catch (final IOException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
        }).collect(Collectors.toList());
    }
}
