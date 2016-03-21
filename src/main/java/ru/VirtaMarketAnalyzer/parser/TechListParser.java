package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Tech;
import ru.VirtaMarketAnalyzer.data.TechAskBid;
import ru.VirtaMarketAnalyzer.data.TechLvl;
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
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));

        final String realm = "olga";
        final String url = Wizard.host + realm + "/main/globalreport/technology";
        final List<Tech> techList = getTechList(url);
        logger.info(Utils.getPrettyGson(techList));
        logger.info("techList.size() = {}", techList.size());
    }

    private static List<Tech> getTechList(final String url) throws IOException {
        final Document doc = Downloader.getDoc(url);
        final Elements types = doc.select("select#unittype > option");

        return types.stream().map(type -> {
            final String id = type.attr("value");
            final String caption = type.text();
            return new Tech(id, caption);
        }).collect(toList());
    }
}
