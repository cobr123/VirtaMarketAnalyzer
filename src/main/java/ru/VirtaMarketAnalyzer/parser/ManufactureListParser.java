package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Manufacture;
import ru.VirtaMarketAnalyzer.data.ManufactureSize;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by cobr123 on 18.05.2015.
 */
final public class ManufactureListParser {
    private static final Logger logger = LoggerFactory.getLogger(ManufactureListParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));

        logger.info(Utils.getPrettyGson(getManufactures(Wizard.host, "olga")));
//        logger.info(Utils.getPrettyGson(getManufactureSizes(Wizard.host, "olga", "380079")));
    }

    public static Manufacture getManufacture(final String host, final String realm, final String id) throws IOException {
        final Optional<Manufacture> opt = getManufactures(host, realm).stream()
                .filter(v -> v.getId().equals(id)).findFirst();
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Не найдено производство с id '" + id + "'");
        }
        return opt.get();
    }

    public static List<Manufacture> getManufactures(final String host, final String realm) throws IOException {
        final List<Manufacture> list = new ArrayList<>();
        final Document doc = Downloader.getDoc(host + realm + "/main/common/main_page/game_info/industry/");

        final Elements rows = doc.select("table[class=\"list\"] > tbody > tr > td > a");

        rows.stream().filter(row -> !row.text().isEmpty()).forEach(row -> {
            final String[] data = row.attr("href").split("/");
            final String id = data[data.length - 1];
            final String caption = row.text();
            final String manufactureCategory = "";
            final List<ManufactureSize> sizes = getManufactureSizes(host, realm, id);
            list.add(new Manufacture(id, manufactureCategory, caption, sizes));
        });
        return list;
    }

    public static List<ManufactureSize> getManufactureSizes(final String host, final String realm, final String id) {
        final List<ManufactureSize> sizes = new ArrayList<>();
        try {
            final Document doc = Downloader.getDoc(host + realm + "/main/industry/unit_type/info/" + id);
            if (doc.select("table[class=\"grid\"]").first() != null) {
                doc.select("table[class=\"grid\"]").first().remove();

                final Elements rows = doc.select("table[class=\"grid\"] > tbody > tr[class]");
                rows.stream().forEach(row -> {
                    final int workplacesCount = Utils.toInt(Utils.getFirstBySep(row.select("> td:nth-child(2)").first().html().toLowerCase(), "<br>"));
                    final int maxEquipment = Utils.toInt(Utils.getLastBySep(row.select("> td:nth-child(2)").first().html().toLowerCase(), "<br>"));
                    final int buildingDurationWeeks = Utils.toInt(row.select("> td:nth-child(3)").first().text());
                    sizes.add(new ManufactureSize(workplacesCount, maxEquipment, buildingDurationWeeks));
                });
            }
        } catch (final Exception e) {
            logger.error(host + realm + "/main/industry/unit_type/info/" + id, e);
        }
        return sizes;
    }
}