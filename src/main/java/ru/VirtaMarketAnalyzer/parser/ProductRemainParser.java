package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductRemain;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 20.05.2015.
 */
public final class ProductRemainParser {
    private static final Logger logger = LoggerFactory.getLogger(ProductRemainParser.class);

    public static void main(final String[] args) throws IOException {
        final String url = Wizard.host + "olga/main/globalreport/marketing/by_products/";
        final List<Product> products = new ArrayList<>();
        products.add(new Product("", "", "1482", ""));
        System.out.println(Utils.getPrettyGson(getRemains(url, products)));
    }

    public static Map<String, List<ProductRemain>> getRemains(final String url, final List<Product> materials) throws IOException {
        final Map<String, ProductRemain> map = new HashMap<>();

        logger.info("греем кэш: {}", url);
        materials.stream()
                .map(material -> url + material.getId())
                .collect(Collectors.toList())
                .parallelStream()
                .forEach(s -> {
                    try {
                        Downloader.getDoc(s);
                    } catch (final IOException e) {
                        logger.error("Ошибка:", e);
                    }
                });

        logger.info("парсим остатки: {}, {}", materials.size(), url);
        for (final Product material : materials) {
            String nextPageUrl = url + material.getId();
            String ref = "";
            for (; ; ) {
                final Document doc = Downloader.getDoc(nextPageUrl, ref);
                final Elements rows = doc.select("table[class=\"grid\"] > tbody > tr[class]");

                for (final Element row : rows) {
                    if (!row.select("> td:nth-child(1)").isEmpty()) {
                        final String unitID = Utils.getLastFromUrl(row.select("> td:nth-child(1) > table > tbody > tr > td:nth-child(2) > a").attr("href"));
                        final double maxOrder = Utils.toDouble(row.select("> td:nth-child(2) > span").text().replace("Max:", ""));
                        final ProductRemain.MaxOrderType maxOrderType = (maxOrder > 0) ? ProductRemain.MaxOrderType.L : ProductRemain.MaxOrderType.U;
                        row.select("> td:nth-child(2)").first().children().remove();
                        final String totalStr = row.select("> td:nth-child(2)").text();
                        long total;
                        if ("Не огр.".equalsIgnoreCase(totalStr)) {
                            total = Long.MAX_VALUE;
                        } else {
                            total = Utils.toLong(totalStr);
                        }
                        final String remainStr = row.select("> td:nth-child(3)").text();
                        long remain;
                        if ("Не огр.".equalsIgnoreCase(remainStr)) {
                            remain = Long.MAX_VALUE;
                        } else {
                            remain = Utils.toLong(remainStr);
                        }
                        final double quality = Utils.toDouble(row.select("> td:nth-child(4)").text());
                        final double price = Utils.toDouble(row.select("> td:nth-child(5)").text());
                        if (remain > 0) {
                            map.put(material.getId() + "|" + unitID, new ProductRemain(material.getId(), unitID, total, remain, quality, price, maxOrderType, maxOrder));
                        }
                    }
                }
                nextPageUrl = Utils.getNextPageHref(doc);
                ref = url + material.getId();
                if (nextPageUrl.isEmpty()) {
                    break;
                }
            }
        }
        final Map<String, List<ProductRemain>> productRemains = new HashMap<>();
        for (final Map.Entry<String, ProductRemain> entry : map.entrySet()) {
            if (!productRemains.containsKey(entry.getValue().getProductID())) {
                productRemains.put(entry.getValue().getProductID(), new ArrayList<>());
            }
            productRemains.get(entry.getValue().getProductID()).add(entry.getValue());
        }
        return productRemains;
    }
}
