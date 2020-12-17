package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
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

    public static List<Product> getTradingProducts(final String host, final String realm, final ProductCategory productCategory) throws IOException {
        return getTradingProducts(host, realm).stream()
                .filter(p -> p.getProductCategoryID().equals(productCategory.getId()))
                .collect(Collectors.toList());
    }

    public static List<Product> getManufactureProducts(final String host, final String realm) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/product/browse?lang=" + lang;
        return getProducts(url);
    }

    private static List<Product> getProducts(final String url) throws IOException {
        final List<Product> list = new ArrayList<>();
        try {
            final String json = Downloader.getJson(url);
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> mapOfRegions = gson.fromJson(json, mapType);

            for (final Map.Entry<String, Map<String, Object>> entry : mapOfRegions.entrySet()) {
                final Map<String, Object> region = entry.getValue();

                final String productCategory = region.get("product_category_name").toString();
                final String productCategoryID = region.get("product_category_id").toString();
                final String id = region.get("id").toString();
                final String caption = region.get("name").toString();
                final String symbol = region.get("symbol").toString();

                list.add(new Product(productCategory, id, caption, productCategoryID, symbol));
            }
        } catch (final Exception e) {
            logger.error(url + "&format=debug");
            throw e;
        }
        return list;
    }

    public static Product getTradingProduct(final String host, final String realm, final String id) throws IOException {
        final Optional<Product> productOpt = getTradingProducts(host, realm).stream().filter(product -> product.getId().equals(id)).findFirst();
        if (!productOpt.isPresent()) {
            throw new IllegalArgumentException("Не найден розничный продукт с id '" + id + "'");
        }
        return productOpt.get();
    }

    public static Product getManufactureProduct(final String host, final String realm, final String id) throws IOException {
        final Optional<Product> productOpt = getManufactureProducts(host, realm).stream().filter(product -> product.getId().equals(id)).findFirst();
        if (!productOpt.isPresent()) {
            throw new IllegalArgumentException("Не найден продукт с id '" + id + "'");
        }
        return productOpt.get();
    }

    public static List<ProductCategory> getTradeProductCategories(final String host, final String realm) throws IOException {
        return getTradingProducts(host, realm)
                .stream()
                .map(p -> new ProductCategory(p.getProductCategoryID(), p.getProductCategory()))
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<ProductCategory> getManufactureProductCategories(final String host, final String realm) throws IOException {
        return getManufactureProducts(host, realm)
                .stream()
                .map(p -> new ProductCategory(p.getProductCategoryID(), p.getProductCategory()))
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<Product> getServiceProducts(final String host, final String realm, final UnitType serviceType) throws IOException {
        final Set<String> set = serviceType.getSpecializations().stream()
                .map(UnitTypeSpec::getRawMaterials)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(RawMaterial::getId)
                .collect(Collectors.toSet());

        return getManufactureProducts(host, realm).stream()
                .filter(p -> set.contains(p.getId()))
                .collect(Collectors.toList());
    }
}