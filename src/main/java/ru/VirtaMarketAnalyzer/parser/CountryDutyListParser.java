package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.nodes.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.VirtaMarketAnalyzer.data.Country;
import ru.VirtaMarketAnalyzer.data.CountryDutyList;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by cobr123 on 16.03.2016.
 */
final public class CountryDutyListParser {
    private static final Logger logger = LoggerFactory.getLogger(CountryDutyListParser.class);

    public static Map<String, List<CountryDutyList>> getAllCountryDutyList(final String host, final String realm, final List<Country> countries) {
        return countries.stream().map(country -> {
            try {
                return getCountryDutyList(host, realm, country.getId());
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
        })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(groupingBy(CountryDutyList::getCountryId));
    }

    public static List<CountryDutyList> getCountryDutyList(final String host, final String realm, final String countryId) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/geo/country/duty?lang=" + lang + "&country_id=" + countryId;

        final List<CountryDutyList> list = new ArrayList<>();
        try {
            final String json = Downloader.getJson(url);
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> infoAndDataMap = gson.fromJson(json, mapType);
            final Map<String, Object> dataMap = infoAndDataMap.get("data");

            for (final String productId : dataMap.keySet()) {
                final Map<String, Object> city = (Map<String, Object>) dataMap.get(productId);

                final int exportTaxPercent = Integer.valueOf(city.get("export").toString());
                final int importTaxPercent = Integer.valueOf(city.get("import").toString());
                final double indicativePrice = Double.valueOf(city.get("min_cost").toString());

                list.add(new CountryDutyList(countryId, productId, exportTaxPercent, importTaxPercent, indicativePrice));
            }
        } catch (final Exception e) {
            Downloader.invalidateCache(url);
            logger.error(url + "&format=debug");
            throw e;
        }
        return list;
    }

    public static CountryDutyList getCountryDuty(final String host, final String realm, final String countryId, final String productId) throws IOException {
        final Optional<CountryDutyList> opt = getCountryDutyList(host, realm, countryId).stream()
                .filter(v -> v.getProductId().equals(productId)).findFirst();
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Не найдены ставки пошлины для продукта с id '" + productId + "' в стране с id '" + countryId + "'");
        }
        return opt.get();
    }

    public static double addDuty(final String host, final String realm, final String fromCountryId, final String toCountryId, final String productId, final double price) throws IOException {
        if (fromCountryId.equals(toCountryId)) {
            return price;
        } else {
            final CountryDutyList cdlFrom = getCountryDuty(host, realm, fromCountryId, productId);
            final double dutyFrom = Math.max(price, cdlFrom.getIndicativePrice()) * (cdlFrom.getExportTaxPercent() / 100.0);

            final CountryDutyList cdlTo = getCountryDuty(host, realm, toCountryId, productId);
            final double dutyTo = Math.max(price, cdlTo.getIndicativePrice()) * (cdlTo.getImportTaxPercent() / 100.0);
            return price + dutyFrom + dutyTo;
        }
    }

    public static double getTransportCost(final String host, final String realm, final String fromCityId, final String toCityId, final String productId) throws Exception {
        if (fromCityId.compareTo(toCityId) < 0) {
            return getTransportCostImpl(host, realm, fromCityId, toCityId, productId);
        } else {
            return getTransportCostImpl(host, realm, toCityId, fromCityId, productId);
        }
    }

    private static double getTransportCostImpl(final String host, final String realm, final String fromCityId, final String toCityId, final String productId) throws Exception {
        return Utils.repeatOnErr(() -> getTransportCostImpl(host, realm, fromCityId, toCityId, productId, 1));
    }

    private static double getTransportCostImpl(final String host, final String realm, final String fromCityId, final String toCityId, final String productId, final int page) throws IOException {
//        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final int pageSize = 10_000;
        final String url = host + "api/" + realm + "/main/geo/transport?city_id=" + fromCityId + "&product_id=" + productId + "&pagesize=" + pageSize + "&pagenum=" + page;

        try {
            final String json = Downloader.getJson(url);
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> infoAndDataMap = gson.fromJson(json, mapType);
            final Map<String, Object> infoMap = infoAndDataMap.get("info");
            final Map<String, Object> dataMap = infoAndDataMap.get("data");

            for (final String idx : dataMap.keySet()) {
                final Map<String, Object> data = (Map<String, Object>) dataMap.get(idx);
                if (data.get("city_id").toString().equals(toCityId)) {
                    return Double.valueOf(data.get("transport_cost").toString());
                }
            }
            final int count = Integer.valueOf(infoMap.get("count").toString());
            if (count > pageSize * page) {
                return getTransportCostImpl(host, realm, fromCityId, toCityId, productId, page + 1);
            }
        } catch (final Exception e) {
            Downloader.invalidateCache(url);
            logger.error(url + "&format=debug");
            throw e;
        }
        throw new IOException("Не найдена цена транспортировки.");
    }

    public static double addDutyAndTransportCost(final String host, final String realm,
                                                 final String fromCountryId, final String toCountryId,
                                                 final String fromCityId, final String toCityId,
                                                 final String productId, final double price
    ) throws Exception {
        final double priceWithDuty = CountryDutyListParser.addDuty(host, realm, fromCountryId, toCountryId, productId, price);
        final double transportCost = CountryDutyListParser.getTransportCost(host, realm, fromCityId, toCityId, productId);
        return priceWithDuty + transportCost;
    }

}
