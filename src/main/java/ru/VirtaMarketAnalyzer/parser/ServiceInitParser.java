package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.UnitType;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 25.02.16.
 */
public final class ServiceInitParser {
    private static final Logger logger = LoggerFactory.getLogger(ServiceInitParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %c %x - %m%n")));

        final List<UnitType> list = getServiceUnitTypes(Wizard.host, "olga");
        list.stream().forEach(opt -> {
            logger.info(Utils.getPrettyGson(opt));
        });
        final List<UnitType> list_en = getServiceUnitTypes(Wizard.host_en, "olga");
        list_en.stream().forEach(opt -> {
            logger.info(Utils.getPrettyGson(opt));
        });
    }

    public static List<UnitType> getServiceUnitTypes(final String host, final String realm) throws IOException {
        final Document doc = Downloader.getDoc(host + realm + "/main/globalreport/marketing/by_service");
        final List<UnitType> list = new ArrayList<>();

        final Elements links = doc.select("#mainContent > fieldset > span > a > img");
        //System.out.println(list.outerHtml());
        return links.stream()
                .map(elem -> {
                    try {
                        return getServiceUnitType(host, realm, elem);
                    } catch (IOException e) {
                        logger.error(e.getLocalizedMessage() ,e);
                    }
                    return null;
                }).collect(Collectors.toList());
    }

    public static UnitType getServiceUnitType(final String host, final String realm, final Element elem) throws IOException {
        final String id = Utils.getLastBySep(elem.parent().attr("href"), "/");
        final String caption = elem.attr("title");
        final String imgUrl = elem.attr("src");
        final List<String> specs = getServiceSpecs(host, realm, id);
        //System.out.println(list.outerHtml());
        return new UnitType(id, caption, imgUrl, specs);
    }

    public static List<String> getServiceSpecs(final String host, final String realm, final String id) throws IOException {
        final Document doc = Downloader.getDoc(host + realm + "/main/industry/unit_type/info/" + id);

        final Elements specElems = doc.select("#mainContent > table:nth-child(4) > tbody > tr > td:nth-child(1) > b");
        return specElems.stream().map(Element::text).collect(Collectors.toList());
    }
}
