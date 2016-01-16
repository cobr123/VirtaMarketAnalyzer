package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Shop;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cobr123 on 16.01.16.
 */
public final class TopRetailParser {
    private static final Logger logger = LoggerFactory.getLogger(TopRetailParser.class);

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        final List<Shop> list = getShopList("http://virtonomica.ru/","olga");
        System.out.println("list.size() = " + list.size());
    }

    public static List<Shop> getShopList(final String baseUrl, final String realm) throws IOException {
        final List<Shop> shops = new ArrayList<>();

        final String newRef = baseUrl + realm +  "/main/company/toplist/retail";
        String nextPageUrl = newRef;
        String ref = "";
        for (; ; ) {
            final Document doc = Downloader.getDoc(nextPageUrl, ref);
            final Elements companyLinks = doc.select("table > tbody > tr > td:nth-child(2) > span > a");
            logger.trace("companyLinks.size() = " + companyLinks.size());
            for (final Element link : companyLinks) {
                final String companyId = Utils.getLastFromUrl(link.attr("href"));
                final List<Shop> tmp = UnitListParser.getShopList(baseUrl, realm, companyId);
                shops.addAll(tmp);
            }

            nextPageUrl = Utils.getNextPageHref(doc);
            ref = newRef;
            if (nextPageUrl.isEmpty()) {
                break;
            }
        }

        return shops;
    }
}
