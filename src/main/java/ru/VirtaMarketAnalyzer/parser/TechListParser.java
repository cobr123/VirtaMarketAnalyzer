package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.TechUnitType;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by cobr123 on 21.03.2016.
 */
final public class TechListParser {
    private static final Logger logger = LoggerFactory.getLogger(TechListParser.class);

    public static void main(String[] args) throws IOException {
        final String realm = "olga";
        final List<TechUnitType> techList = getTechUnitTypes(Wizard.host , realm);
        logger.info(Utils.getPrettyGson(techList));
        logger.info("techList.size() = {}", techList.size());
    }

    public static List<TechUnitType> getTechUnitTypes(final String host,final String realm) throws IOException {
        final Document doc = Downloader.getDoc(host + realm + "/main/globalreport/technology");
        final Elements types = doc.select("select#unittype > option");

        return types.stream().map(type -> {
            final String id = type.attr("value");
            final String caption = type.text();
            return new TechUnitType(id, caption);
        }).collect(toList());
    }
}
