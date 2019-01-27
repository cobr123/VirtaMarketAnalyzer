package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductCategory;
import ru.VirtaMarketAnalyzer.data.Region;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class ProductInitParser {
    private static final Logger logger = LoggerFactory.getLogger(ProductInitParser.class);

    public static List<Product> getTradingProducts(final String host, final String realm) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/product/goods?lang=" + lang;
        return getProducts(url);
    }

    public static List<Product> getManufactureProducts(final String host, final String realm) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/product/browse?lang=" + lang;
        return getProducts(url);
    }

    private static List<Product> getProducts(final String url) throws IOException {
        final List<Product> list = new ArrayList<>();
        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().html();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> mapOfRegions = gson.fromJson(json, mapType);

            for (final String region_id : mapOfRegions.keySet()) {
                final Map<String, Object> region = mapOfRegions.get(region_id);

                final String productCategory = region.get("product_category_name").toString();
                final String productCategoryID = region.get("product_category_id").toString();
                final String id = region.get("id").toString();
                final String caption = region.get("name").toString();
                final String symbol = region.get("symbol").toString();

                list.add(new Product(productCategory, id, caption, productCategoryID, symbol));
            }
        } catch (final Exception e) {
            logger.error(url);
            throw e;
        }
        return list;
    }

    public static Product getTradingProduct(final String host, final String realm, final String id) throws IOException {
        return getTradingProducts(host, realm).stream().filter(product -> product.getId().equals(id)).findFirst().get();
    }

    public static Product getManufactureProduct(final String host, final String realm, final String id) throws IOException {
        return getManufactureProducts(host, realm).stream().filter(product -> product.getId().equals(id)).findFirst().get();
    }

    public static List<ProductCategory> getTradeProductCategories(final String host, final String realm) throws IOException {
        return getTradingProducts(host, realm)
                .stream()
                .map(p -> new ProductCategory(p.getProductCategoryID(), p.getCaption()))
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<ProductCategory> getManufactureProductCategories(final String host, final String realm) throws IOException {
        return getManufactureProducts(host, realm)
                .stream()
                .map(p -> new ProductCategory(p.getProductCategoryID(), p.getCaption()))
                .distinct()
                .collect(Collectors.toList());
    }

}