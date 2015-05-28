package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Manufacture;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cobr123 on 18.05.2015.
 */
final public class ManufactureListParser {
    private static final Logger logger = LoggerFactory.getLogger(ManufactureListParser.class);

    public static void main(final String[] args) throws IOException {
        final String url = "http://virtonomica.ru/olga/main/common/main_page/game_info/industry/";

        logger.info(Utils.getPrettyGson(getManufactures(url)));
    }

    public static List<Manufacture> getManufactures(final String url) throws IOException {
        final List<Manufacture> list = new ArrayList<>();
        final Document doc = Downloader.getDoc(url);

        final Elements rows = doc.select("table[class=\"list\"] > tbody > tr > td > a");
        //System.out.println(list.outerHtml());
        for (final Element row : rows) {
            if (!row.text().isEmpty()) {
                final String[] data = row.attr("href").split("/");
                final String id = data[data.length - 1];
                final String caption = row.text();
                final String manufactureCategory = "";
                list.add(new Manufacture(id, manufactureCategory, caption));
            }
        }
        return list;
    }
}
