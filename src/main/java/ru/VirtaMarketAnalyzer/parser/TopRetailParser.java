package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.Product;
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
        final List<Shop> list = getShopList("http://virtonomica.ru/", "olga", new ArrayList<>(), new ArrayList<>());
        System.out.println("list.size() = " + list.size());
    }

    public static List<Shop> getShopList(final String baseUrl, final String realm, final List<City> cities, final List<Product> products) throws IOException {
        final List<Shop> shops = new ArrayList<>();

        final String newRef = baseUrl + realm + "/main/company/toplist/retail";
        String nextPageUrl = newRef;
        String ref = "";
        for (int page = 1; page <= 10; ++page) {
            final Document doc = Downloader.getDoc(nextPageUrl, ref);
            final Elements companyLinks = doc.select("table > tbody > tr > td:nth-child(2) > span > a");
            logger.info("companyLinks.size() = {}", companyLinks.size());
            final int shopsSizeBefore = shops.size();
            for (final Element link : companyLinks) {
                final String companyId = Utils.getLastFromUrl(link.attr("href"));
                final List<Shop> tmp = UnitListParser.getShopList(baseUrl, realm, companyId, cities, products);
                shops.addAll(tmp);
            }

            nextPageUrl = Utils.getNextPageHref(doc);
            ref = newRef;
            if (nextPageUrl.isEmpty()) {
                break;
            }
            logger.info("nextPageUrl: {}", nextPageUrl);
            logger.info("shops.size(): {}", shops.size());
            logger.info("shops.size() diff: {}", shops.size() - shopsSizeBefore);
        }

        return shops;
    }
}
