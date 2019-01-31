package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
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
final public class RegionCTIEParser {
    private static final Logger logger = LoggerFactory.getLogger(RegionCTIEParser.class);

    public static Map<String, List<RegionCTIE>> getAllRegionsCTIEList(final String host, final String realm, final List<Region> regions) {
        return regions.stream().map(region -> {
            try {
                return getRegionCTIEList(host, realm, region);
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
        })
                .flatMap(Collection::stream)
                .collect(groupingBy(RegionCTIE::getRegionId));
    }

    public static List<RegionCTIE> getRegionCTIEList(final String host, final String realm, final Region region) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/geo/region/envd?lang=" + lang + "&region_id=" + region.getId();

        final List<RegionCTIE> list = new ArrayList<>();
        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().text();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> infoAndDataMap = gson.fromJson(json, mapType);
            final Map<String, Object> dataMap = infoAndDataMap.get("data");

            for (final String productId : dataMap.keySet()) {
                final Map<String, Object> city = (Map<String, Object>) dataMap.get(productId);

                final int rate = Utils.toInt(city.get("tax").toString());

                list.add(new RegionCTIE(region.getId(), productId, rate));
            }
        } catch (final Exception e) {
            logger.error(url + "&format=debug");
            throw e;
        }
        return list;
    }
}
