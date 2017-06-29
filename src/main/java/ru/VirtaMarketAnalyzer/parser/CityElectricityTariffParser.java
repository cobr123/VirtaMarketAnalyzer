package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.CityElectricityTariff;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by cobr123 on 16.03.2016.
 */
final public class CityElectricityTariffParser {
    private static final Logger logger = LoggerFactory.getLogger(CityElectricityTariffParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        final String realm = "olga";
        final List<City> cities = new ArrayList<>();
        final City city = new City("3010", "3023", "3055", "Николаев", 10, 0, 0, 0, 0, null);
        cities.add(city);
        final Map<String, List<CityElectricityTariff>> allCityElectricityTariffList = getAllCityElectricityTariffList(Wizard.host, realm, cities);
        logger.info(Utils.getPrettyGson(allCityElectricityTariffList));
        logger.info("" + allCityElectricityTariffList.get("3868").size());
    }

    public static Map<String, List<CityElectricityTariff>> getAllCityElectricityTariffList(final String host, final String realm, final List<City> cities) throws IOException {
        return cities.stream().map(city -> {
            try {
                return getCityElectricityTariffList(host, realm, city);
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
        })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(groupingBy(CityElectricityTariff::getProductID));
    }

    public static List<CityElectricityTariff> getCityElectricityTariffList(final String host, final String realm, final City city) throws IOException {
        final Document doc = Downloader.getDoc(host + realm + "/main/geo/tariff/" + city.getRegionId());
        final Elements elems = doc.select("table.list > tbody > tr[class] > td:nth-child(1)");
        return elems
                .stream()
                .map(el -> {
            try {
                return getCityElectricityTariffList(el, city);
            } catch (final Exception e) {
                logger.info("{}{}{}{}", host, realm, "/main/geo/tariff/", city.getRegionId());
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
        })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static List<CityElectricityTariff> getCityElectricityTariffList(final Element elem, final City city) throws Exception {
        final List<CityElectricityTariff> list = new ArrayList<>();
        final String productCategory = elem.text();
        final double electricityTariff = Utils.toDouble(Utils.getFirstBySep(elem.nextElementSibling().nextElementSibling().text(), "/"));

        final Elements imgElems = elem.nextElementSibling().select("> a > img");
        if (imgElems.isEmpty()) {
            //list.add(new CityElectricityTariff(city.getId(), productCategory, "", electricityTariff));
        } else {
            for (final Element imgElem : imgElems) {
                final String productId = Utils.getLastFromUrl(imgElem.parent().attr("href"));
                list.add(new CityElectricityTariff(city.getId(), productCategory, productId, electricityTariff));
            }
        }
        return list;
    }
}
