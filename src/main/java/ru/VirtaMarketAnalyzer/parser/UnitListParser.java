package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.Shop;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 16.01.16.
 */
public final class UnitListParser {
    private static final Logger logger = LoggerFactory.getLogger(UnitListParser.class);

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        final List<Shop> list = getShopList(Wizard.host, "olga", "2085506", new ArrayList<>(), new ArrayList<>());
        System.out.println("list.size() = " + list.size());
    }

    public static List<Shop> getShopList(final String baseUrl, final String realm, final String companyId, final List<City> cities, final List<Product> products) throws IOException {
        final List<Shop> shops = new ArrayList<>();
        final String newRef = baseUrl + realm + "/main/company/view/" + companyId + "/unit_list";
        String nextPageUrl = newRef;
        String ref = "";
        final Map<String, List<Product>> productsByImgSrc = products.stream().collect(Collectors.groupingBy(Product::getImgUrl));
        for (int i = 0; i < 50; ++i) {
            try {
                final Document doc = Downloader.getDoc(nextPageUrl, ref);
                final Elements shopLinks = doc.select("table > tbody > tr > td[class=\"info i-shop\"] > a");

                if (shopLinks.size() > 0) {
                    logger.trace("page {}, shopLinks.size() = {}", Utils.getLastBySep(nextPageUrl, "/"), shopLinks.size());
                    final List<Shop> tmpShops = shopLinks.stream()
                            .map(sl -> {
                                Shop shop = null;
                                final String cityCaption = sl.parent().previousElementSibling().text();
                                try {
                                    shop = ShopParser.parse(realm, sl.attr("href"), cities, productsByImgSrc, cityCaption);
                                } catch (final Exception e) {
                                    logger.error(e.getLocalizedMessage(), e);
                                }
                                return shop;
                            })
                            .filter(Objects::nonNull)
                            .filter(s -> s.getShopProducts().size() > 0)
                            .filter(s -> !"Не известен".equals(s.getTownDistrict()))
                            .filter(s -> !"Не известен".equals(s.getServiceLevel()))
                            .collect(Collectors.toList());

                    shops.addAll(tmpShops);
                }

                nextPageUrl = Utils.getNextPageHref(doc);
                ref = newRef;
                if (nextPageUrl.isEmpty()) {
                    break;
                }
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }

        return shops;
    }
}
