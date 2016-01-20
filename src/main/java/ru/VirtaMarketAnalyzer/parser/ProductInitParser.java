package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductCategory;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class ProductInitParser {
    public static void main(final String[] args) throws IOException {
        final String url = "http://virtonomica.ru/olga/main/common/main_page/game_info/trading";

        System.out.println(Utils.getPrettyGson(getProducts(url)));
    }

    public static List<Product> getProducts(final String url) throws IOException {
        final Document doc = Downloader.getDoc(url);
        final List<Product> list = new ArrayList<>();

        final Elements rows = doc.select("table[class=\"list\"] > tbody > tr");

        String productCategory = null;
        for (final Element row : rows) {
            if (!row.select("tr > th").isEmpty()) {
                productCategory = row.select("tr > th").text();
//                Utils.log(productCategory);
            } else if (!row.select("tr > td > a > img").isEmpty()) {
                final Elements imgs = row.select("tr > td > a > img");
                for (final Element img : imgs) {
                    final String caption = img.attr("title");
                    final String id = Utils.getLastFromUrl(img.parent().attr("href"));
                    final String imgUrl = img.attr("src");
                    list.add(new Product(productCategory, imgUrl, id, caption));
                }
            }
        }
        return list;
    }

    public static List<ProductCategory> getProductCategories(final List<Product> products) {
        final Set<String> set = new HashSet<>();
        final List<ProductCategory> list = new ArrayList<>();
        set.addAll(products.stream().map(Product::getProductCategory).collect(Collectors.toList()));
        set.forEach(s -> list.add(new ProductCategory(s)));
        return list;
    }
}