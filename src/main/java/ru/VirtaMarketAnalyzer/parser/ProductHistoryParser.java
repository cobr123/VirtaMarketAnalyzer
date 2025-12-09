package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductHistory;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 16.10.2016.
 */
public final class ProductHistoryParser {
    private static final Logger logger = LoggerFactory.getLogger(ProductHistoryParser.class);

    public static List<ProductHistory> getHistory(final String host, final String realm, final List<Product> materials) throws IOException {
        final String url = host + realm + "/main/globalreport/product_history/";
        final List<ProductHistory> productsHistory = new ArrayList<>(materials.size());

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
                        Downloader.invalidateCache(s);
                    }
                });

        logger.info("парсим обзорный отчет по производству: {}, {}", materials.size(), url);
        for (final Product material : materials) {
            final Document doc = Downloader.getDoc(url + material.getId());
            final Element firstRow = doc.select("table[class=\"list\"] > tbody > tr[class]").first();

            if (firstRow == null) {
                logger.error("Не найдена первая строка в обзорном отчете {}{}", url, material.getId());
                Downloader.invalidateCache(url + material.getId());
            } else {
                if (!firstRow.select("> td:nth-child(2)").isEmpty()) {
                    final long volumeProd = Utils.toLong(firstRow.select("> td:nth-child(2)").text());
                    final long volumeCons = Utils.toLong(firstRow.select("> td:nth-child(3)").text());
                    final double quality = Utils.toDouble(firstRow.select("> td:nth-child(4)").text());
                    final double cost = Utils.toDouble(firstRow.select("> td:nth-child(5)").text());
                    final double assessedValue = Utils.toDouble(firstRow.select("> td:nth-child(6)").text());
                    productsHistory.add(new ProductHistory(material.getId(), volumeProd, volumeCons, quality, cost, assessedValue));
                }
            }
        }
        return productsHistory;
    }
}
