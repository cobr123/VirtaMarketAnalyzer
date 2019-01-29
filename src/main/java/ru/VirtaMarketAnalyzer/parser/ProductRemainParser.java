package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductRemain;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by cobr123 on 20.05.2015.
 */
public final class ProductRemainParser {
    private static final Logger logger = LoggerFactory.getLogger(ProductRemainParser.class);

    public static Map<String, List<ProductRemain>> getRemains(final String host, final String realm, final List<Product> materials) throws IOException {
        return materials.stream()
                .map(material -> {
                    try {
                        return getRemains(host, realm, material);
                    } catch (final Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(groupingBy(ProductRemain::getProductID));
    }

    public static List<ProductRemain> getRemains(final String host, final String realm, final Product material) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final String url = host + "api/" + realm + "/main/marketing/report/trade/offers?product_id=" + material.getId();

        final List<ProductRemain> list = new ArrayList<>();
        try {
            final Document doc = Downloader.getDoc(url, true);
            final String json = doc.body().html();
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> infoAndDataMap = gson.fromJson(json, mapType);
            final Map<String, Object> dataMap = infoAndDataMap.get("data");

            for (final String idx : dataMap.keySet()) {
                final Map<String, Object> city = (Map<String, Object>) dataMap.get(idx);

                final String productId = city.get("product_id").toString();
                String companyName = "";
                if (city.get("company_name") != null) {
                    companyName = city.get("company_name").toString();
                }
                final String unitID = city.get("unit_id").toString();
                int total = 0;
                if (city.get("quantity") != null) {
                    total = Utils.toInt(city.get("quantity").toString());
                }
                int remain = 0;
                if (city.get("free_for_buy") != null) {
                    remain = Utils.toInt(city.get("free_for_buy").toString());
                }
                final double quality = Utils.toDouble(city.get("quality").toString());
                double price = 0;
                if (city.get("price") != null) {
                    price = Utils.toDouble(city.get("price").toString());
                }
                double maxOrder = 0;
                if (city.get("max_qty") != null) {
                    maxOrder = Utils.toInt(city.get("max_qty").toString());
                }
                final ProductRemain.MaxOrderType maxOrderType = (maxOrder > 0) ? ProductRemain.MaxOrderType.L : ProductRemain.MaxOrderType.U;

                list.add(new ProductRemain(productId, companyName, unitID, total, remain, quality, price, maxOrderType, maxOrder));
            }
        } catch (final Exception e) {
            logger.error(url + "&format=debug");
            throw e;
        }
        return list;
    }
}
