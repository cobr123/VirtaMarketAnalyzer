package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.CityElectricityTariff;
import ru.VirtaMarketAnalyzer.main.Utils;
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
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private static List<CityElectricityTariff> getCityElectricityTariffList(final Element elem, final City city) throws Exception {
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

//    public static List<CityElectricityTariff> getCityElectricityTariffList(final String host, final String realm, final City city) throws IOException {
//        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
//        final String url = host + "api/" + realm + "/main/geo/region/energy?lang=" + lang + "region_id=" + city.getRegionId();
//
//        final List<CityElectricityTariff> list = new ArrayList<>();
//        try {
//            final Document doc = Downloader.getDoc(url, true);
//            final String json = doc.body().text();
//            final Gson gson = new Gson();
//            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
//            }.getType();
//            final Map<String, Map<String, Object>> mapOfCountry = gson.fromJson(json, mapType);
//
//
//            for (final String country_id : mapOfCountry.keySet()) {
//                final Map<String, Object> country = mapOfCountry.get(country_id);
//                final List<Object> productList = (List<Object>) country.get("products");
//
//                final String productId = country.get("id").toString();
//                final String productCategory = country.get("industry_name").toString();
//                final double electricityTariff = Double.valueOf(country.get("energy_tariff").toString());
//
//                list.add(new CityElectricityTariff(city.getId(), productCategory, productId, electricityTariff));
//            }
//        } catch (final Exception e) {
//            logger.error(url + "&format=debug");
//            throw e;
//        }
//        return list;
//    }
}
