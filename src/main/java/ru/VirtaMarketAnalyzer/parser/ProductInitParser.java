package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductCategory;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class ProductInitParser {
    private static final Logger logger = LoggerFactory.getLogger(ProductInitParser.class);
    final static Pattern productPattern = Pattern.compile("\\{id\\s+:\\s+'([^']+)',\\s+catid\\s+:\\s+'([^']+)',\\s+symbol\\s+:\\s+'([^']+)',\\s+name\\s+:\\s+\"([^\"]+)\"\\},");

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));

        System.out.println(Utils.getPrettyGson(getTradingProducts(Wizard.host, "olga")));
//        System.out.println(Utils.getPrettyGson(getTradeProductCategories(Wizard.host, "olga")));
//        System.out.println(Utils.getPrettyGson(getTradeProductCategories(Wizard.host_en, "olga")));
//        System.out.println(Utils.getPrettyGson(getManufactureProductCategories(Wizard.host, "olga")));
//        System.out.println(Utils.getPrettyGson(getManufactureProductCategories(Wizard.host_en, "olga")));
    }

    public static List<Product> getTradingProducts(final String host, final String realm) throws IOException {
        final List<ProductCategory> productCategories = getTradeProductCategories(host, realm);
        return get(host, realm, "/main/globalreport/marketing/by_trade_at_cities", productCategories);
    }

    public static List<Product> getManufactureProducts(final String host, final String realm) throws IOException {
        final List<ProductCategory> productCategories = getManufactureProductCategories(host, realm);
        return get(host, realm, "/main/globalreport/manufacture", productCategories);
    }

    public static Product getTradingProduct(final String host, final String realm, final String id) throws IOException {
        return getTradingProducts(host, realm).stream().filter(product -> product.getId().equals(id)).findFirst().get();
    }

    public static Product getManufactureProduct(final String host, final String realm, final String id) throws IOException {
        return getManufactureProducts(host, realm).stream().filter(product -> product.getId().equals(id)).findFirst().get();
    }

    public static List<Product> get(final String host, final String realm, final String path, final List<ProductCategory> productCategories) throws IOException {
        final Document doc = Downloader.getDoc(host + realm + path);
        final List<Product> products = new ArrayList<>();

        final Elements scripts = doc.select("script");
        for (final Element script : scripts) {
            if (!script.html().isEmpty()) {
                final Matcher m = productPattern.matcher(script.html());

                while (m.find()) {
                    final String id = m.group(1);
                    final String productCategoryID = m.group(2);
                    final String productCategory = productCategories.stream().filter(pc -> pc.getId().equals(productCategoryID)).findFirst().get().getCaption();
                    final String symbol = m.group(3);
                    final String caption = m.group(4);
                    products.add(new Product(productCategory, id, caption, productCategoryID, symbol));
                }
            }
        }
        return products;
    }

    public static List<ProductCategory> getTradeProductCategories(final String host, final String realm) throws IOException {
        return getProductCategories(host, realm, "/main/globalreport/marketing/by_trade_at_cities");
    }

    public static List<ProductCategory> getManufactureProductCategories(final String host, final String realm) throws IOException {
        return getProductCategories(host, realm, "/main/globalreport/manufacture");
    }

    public static List<ProductCategory> getProductCategories(final String host, final String realm, final String path) throws IOException {
        final Document doc = Downloader.getDoc(host + realm + path);
        final List<ProductCategory> list = new ArrayList<>();

        final Elements ops = doc.select("#__product_category_list > option");

        for (final Element op : ops) {
            final String id = op.val();
            final String caption = op.text();
            list.add(new ProductCategory(id, caption));
        }
        return list;
    }
}