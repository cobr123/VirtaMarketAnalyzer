package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductCategory;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class ProductInitParser {
    public static void main(final String[] args) throws IOException {
        final Document doc = Downloader.getDoc("http://virtonomica.ru/olga/main/common/main_page/game_info/trading");

        final Elements imgs = doc.select("table[class=\"list\"] > tbody > tr > td > a > img");
        //System.out.println(list.outerHtml());
        for (final Element img : imgs) {
            System.out.println(img.attr("title"));
            final String[] parts = img.parent().attr("href").split("/");
            System.out.println(parts[parts.length - 1]);
            System.out.println(img.attr("src").replace("/products", ""));
        }
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
                    final String[] parts = img.parent().attr("href").split("/");
                    final String id = parts[parts.length - 1];
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
        for (final Product product : products) {
            set.add(product.getProductCategory());
        }
        set.forEach(s -> list.add(new ProductCategory(s)));
        return list;
    }
}
