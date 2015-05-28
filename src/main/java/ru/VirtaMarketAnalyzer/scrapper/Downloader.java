package ru.VirtaMarketAnalyzer.scrapper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.main.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class Downloader {
    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    public static File get(final String url) throws IOException {
        return get(url, "");
    }

    public static File get(final String url, final String referrer) throws IOException {
        String clearedUrl = "";
        if (referrer != null && !referrer.isEmpty()) {
            final String[] parts = url.split("/");
            final String page = File.separator + parts[parts.length - 2] + File.separator + parts[parts.length - 1];
            clearedUrl = referrer.replace("http://", "").replace("/", File.separator) + page;
        } else {
            clearedUrl = url.replace("http://", "").replace("/", File.separator);
        }
        final String fileToSave = Utils.getDir() + clearedUrl + ".html";
        final File file = new File(fileToSave);
        if (file.exists() && Utils.equalsWoTime(new Date(file.lastModified()), new Date())) {
            logger.trace("Взят из кэша: {}", file.getAbsolutePath());
        } else {
            logger.info("Запрошен адрес: {}", url);

            for (int tries = 1; tries <= 3; ++tries) {
                try {
                    final Connection conn = Jsoup.connect(url);
                    if (referrer != null && !referrer.isEmpty()) {
                        logger.info("referrer: {}", referrer);
                        conn.referrer(referrer);
                    }
                    final Document doc = conn.get();
                    Utils.writeFile(fileToSave, doc.outerHtml());
                    break;
                } catch (final IOException e) {
                    logger.error("Ошибка при запросе, попытка #{}: {}", tries, url);
                    logger.error("Ошибка:", e);
                    waitSecond(3);
                }
            }
        }
        return file;
    }

    public static void waitSecond() {
        waitSecond(1);
    }

    public static void waitSecond(final long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static Document getDoc(final String url) throws IOException {
        return getDoc(url, "");
    }

    public static Document getDoc(final String url, final String referrer) throws IOException {
        final File input = Downloader.get(url, referrer);
        return Jsoup.parse(input, "UTF-8", "http://virtonomica.ru/");
    }

    public static void main(final String[] args) throws IOException {
        final Document doc = Jsoup.connect("http://virtonomica.ru/olga/main/geo/citylist/331858").get();
        Utils.writeFile(Utils.getDir() + "citylist.html", doc.outerHtml());
    }
}