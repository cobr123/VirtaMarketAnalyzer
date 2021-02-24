package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Manufacture;
import ru.VirtaMarketAnalyzer.data.ManufactureSize;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by cobr123 on 18.05.2015.
 */
final public class ManufactureListParser {
    private static final Logger logger = LoggerFactory.getLogger(ManufactureListParser.class);

    public static Manufacture getManufacture(final String host, final String realm, final String id) throws IOException {
        final Optional<Manufacture> opt = getManufactures(host, realm).stream()
                .filter(v -> v.getId().equals(id)).findFirst();
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Не найдено производство с id '" + id + "'");
        }
        return opt.get();
    }

    public static List<Manufacture> getManufactures(final String host, final String realm) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/unittype/browse?lang=" + lang;

        final List<Manufacture> list = new ArrayList<>();
        try {
            final String json = Downloader.getJson(url);
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> mapOfUnitTypes = gson.fromJson(json, mapType);

            for (final Map.Entry<String, Map<String, Object>> entry : mapOfUnitTypes.entrySet()) {
                final Map<String, Object> unitType = entry.getValue();

                final String id = unitType.get("id").toString();
                final String caption = unitType.get("name").toString();
                final String manufactureCategory = unitType.get("industry_name").toString();

                final int workplacesCount = Integer.parseInt(unitType.get("labor_max").toString());
                final int maxEquipment = Integer.parseInt(unitType.get("equipment_max").toString());
                final int buildingDurationWeeks = Integer.parseInt(unitType.get("building_time").toString());

                final List<ManufactureSize> sizes = new ArrayList<>();
                sizes.add(new ManufactureSize(workplacesCount, maxEquipment, buildingDurationWeeks));

                list.add(new Manufacture(id, manufactureCategory, caption, sizes));
            }
        } catch (final Exception e) {
            logger.error(url + "&format=debug");
            throw e;
        }
        return list;
    }

}