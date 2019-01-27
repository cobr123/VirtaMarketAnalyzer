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
                return getCountryDutyList(host, realm, country);
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
        })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(groupingBy(CountryDutyList::getCountryId));
    }

    public static List<CountryDutyList> getCountryDutyList(final String host, final String realm, final Country country) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/geo/country/duty?lang=" + lang + "&country_id=" + country.getId();

        final List<CountryDutyList> list = new ArrayList<>();
        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().html();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> infoAndDataMap = gson.fromJson(json, mapType);
            final Map<String, Object> dataMap = infoAndDataMap.get("data");

            for (final String productId : dataMap.keySet()) {
                final Map<String, Object> city = (Map<String, Object>) dataMap.get(productId);

                final int exportTaxPercent = Utils.toInt(city.get("export").toString());
                final int importTaxPercent = Utils.toInt(city.get("import").toString());
                final double indicativePrice = Utils.toDouble(city.get("min_cost").toString());

                list.add(new CountryDutyList(country.getId(), productId, exportTaxPercent, importTaxPercent, indicativePrice));
            }
        } catch (final Exception e) {
            logger.error(url);
            throw e;
        }
        return list;
    }

}
