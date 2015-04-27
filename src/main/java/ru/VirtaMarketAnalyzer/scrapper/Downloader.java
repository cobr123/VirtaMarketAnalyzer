package ru.VirtaMarketAnalyzer.scrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.VirtaMarketAnalyzer.main.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class Downloader {
    private static long lastAccess = 0;
    //1000 milliseconds is one second.
    private static final long timeoutInMillis = 1000;

    public static File get(final String url) throws IOException {
        final String clearedUrl = url.replace("http://", "").replace("/", File.separator);
        final String fileToSave = Utils.getDir() + clearedUrl + ".html";
        final File file = new File(fileToSave);
        if (file.exists() && Utils.equalsWoTime(new Date(file.lastModified()), new Date())) {
            Utils.log("Взят из кэша: ", file.getAbsolutePath());
        } else {
            Utils.log("Запрошен адрес: ", url);
            final long elapsed = System.currentTimeMillis() - lastAccess;
            if (elapsed < timeoutInMillis) {
                Utils.log("Ожидаем ", timeoutInMillis - elapsed);
                try {
                    Thread.sleep(timeoutInMillis - elapsed); 
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            lastAccess = System.currentTimeMillis();
            final Document doc = Jsoup.connect(url).get();
            Utils.writeFile(fileToSave, doc.outerHtml());
        }
        return file;
    }
    public static Document getDoc(final String url) throws IOException {
        final File input = Downloader.get(url);
        return Jsoup.parse(input, "UTF-8", "http://virtonomica.ru/");
    }

    public static void main(final String[] args) throws IOException {
        final Document doc = Jsoup.connect("http://virtonomica.ru/olga/main/geo/citylist/331858").get();
        Utils.writeFile(Utils.getDir() + "citylist.html", doc.outerHtml());
    }
}
