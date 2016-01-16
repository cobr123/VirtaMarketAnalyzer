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
public final class UnitListParser {
    private static final Logger logger = LoggerFactory.getLogger(UnitListParser.class);

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        final List<Shop> list = getShopList("http://virtonomica.ru/olga/main/company/view/", "2085506");
        System.out.println("list.size() = " + list.size());
    }

    public static List<Shop> getShopList(final String url, final String companyId) throws IOException {
        final List<Shop> shops = new ArrayList<>();

        String nextPageUrl = url + companyId + "/unit_list";
        String ref = "";
        for (; ; ) {
            final Document doc = Downloader.getDoc(nextPageUrl, ref);
            final Elements shopLinks = doc.select("table > tbody > tr > td[class=\"info i-shop\"] > a");

            logger.trace("shopLinks.size() = " + shopLinks.size());
            for (final Element link : shopLinks) {
                final Shop shop = ShopParser.parse(link.attr("href"));
                shops.add(shop);
            }

            nextPageUrl = Utils.getNextPageHref(doc);
            ref = url + companyId + "/unit_list";
            if (nextPageUrl.isEmpty()) {
                break;
            }
        }

        return shops;
    }
}
