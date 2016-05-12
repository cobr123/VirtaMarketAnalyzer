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
 * Created by cobr123 on 17.01.16.
 */
public final class BestShopsParser {
    private static final Logger logger = LoggerFactory.getLogger(BestShopsParser.class);

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        final List<Shop> list = getShopList("http://virtonomica.ru/", "olga", new ArrayList<>(), new ArrayList<>());
        System.out.println("list.size() = " + list.size());
    }

    public static List<Shop> getShopList(final String baseUrl, final String realm, final List<City> cities, final List<Product> products) throws Exception {
        final List<Shop> shops = new ArrayList<>();

        final String newRef = baseUrl + realm + "/main/globalreport/marketing/best_shops";
        String nextPageUrl = newRef;
        String ref = "";
        for (int page = 1; page <= 5; ++page) {
            final Document doc = Downloader.getDoc(nextPageUrl, ref);
            final Elements shopLinks = doc.select("table.list > tbody > tr > td:nth-child(3) > div:nth-child(1) > a:nth-child(2)");
            logger.trace("shopLinks.size() = {}", shopLinks.size());
            for (final Element link : shopLinks) {
                final Shop shop = ShopParser.parse(link.attr("href"), cities, products, "");
                if (shop.getShopProducts().size() > 0 && !"Не известен".equals(shop.getTownDistrict()) && !"Не известен".equals(shop.getServiceLevel())) {
                    shops.add(shop);
                }
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
