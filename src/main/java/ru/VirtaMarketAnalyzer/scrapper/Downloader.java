package ru.VirtaMarketAnalyzer.scrapper;

import com.google.common.util.concurrent.RateLimiter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.*;
import java.util.Date;
import java.util.Map;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class Downloader {
    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    public static String getClearedUrl(final String url, final String referrer) {
        String clearedUrl;
        if (referrer != null && !referrer.isEmpty()) {
            final String[] parts = url.split("/");
            final String page = File.separator + parts[parts.length - 2] + File.separator + parts[parts.length - 1];
            clearedUrl = referrer.replace("http://", "").replace("https://", "").replace("/", File.separator) + page;
        } else {
            clearedUrl = url
                    .replace("http://", "")
                    .replace("https://", "")
                    .replace("/", File.separator)
                    .replace("?", File.separator)
                    .replace("=", File.separator);
        }
        return clearedUrl;
    }

    public static void invalidateCache(final String url) {
        invalidateCache(url, "");
    }

    public static void invalidateCache(final String url, final String referrer) {
        final String clearedUrl = getClearedUrl(url, referrer);
        final File htmlFile = new File(Utils.getDir() + clearedUrl + ".html");
        if (htmlFile.exists()) {
            logger.info("invalidateCache: {}", htmlFile.getAbsolutePath());
            htmlFile.delete();
        }
        final File jsonFile = new File(Utils.getDir() + clearedUrl + ".json");
        if (jsonFile.exists()) {
            logger.info("invalidateCache: {}", jsonFile.getAbsolutePath());
            jsonFile.delete();
        }
    }

    public static String getJson(final String url) throws IOException {
        return getJson(url, null, 99);
    }

    final static Map<String, String> loginCookies;

    static {
        Connection.Response res = null;
        try {
            final String login = System.getenv("vma.login");
            final String password = System.getenv("vma.password");
            if (login == null || login.isEmpty()) {
                throw new IllegalArgumentException("Необходим логин виртономики, иначе api вернет данные реалма vera для всех остальных реалмов (vma.login)");
            }
            if (password == null || password.isEmpty()) {
                throw new IllegalArgumentException("Необходим пароль виртономики, иначе api вернет данные реалма vera для всех остальных реалмов (vma.password)");
            }
            res = Jsoup.connect(Wizard.host + "olga/main/user/login")
                    .data("userData[login]", login, "userData[password]", password)
                    .method(Connection.Method.POST)
                    .execute();
        } catch (final IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        loginCookies = res.cookies();
    }

    public static String getJson(final String url, final String referrer, final int maxTriesCnt) throws IOException {
        final String clearedUrl = getClearedUrl(url, referrer);
        final String fileToSave = Utils.getDir() + clearedUrl + ".json";
        final File file = new File(fileToSave);
        if (file.exists() && Utils.equalsWoTime(new Date(file.lastModified()), new Date())) {
            logger.trace("Взят из кэша: {}", file.getAbsolutePath());
            return Utils.readFile(file.getAbsolutePath());
        } else {
            logger.trace("Запрошен адрес: {}", url);

            for (int tries = 1; tries <= maxTriesCnt; ++tries) {
                try {
                    rateLimiter.acquire();
                    final Connection conn = Jsoup.connect(url);
                    if (referrer != null && !referrer.isEmpty()) {
                        logger.trace("referrer: {}", referrer);
                        conn.referrer(referrer);
                    }
                    conn.header("Accept-Language", "ru");
                    conn.header("Accept-Encoding", "gzip, deflate");
                    conn.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0");
                    conn.maxBodySize(0);
                    conn.timeout(60_000);
                    conn.ignoreContentType(true);
                    conn.cookies(loginCookies);
                    final String json = conn.execute().body();
                    Utils.writeFile(file.getAbsolutePath(), json);
                    return json;
                } catch (final IOException e) {
                    logger.error("Ошибка при запросе, попытка #{} из {}: {}", tries, maxTriesCnt, url);
                    logger.error("Ошибка: {}", e.getLocalizedMessage());
                    if (maxTriesCnt == tries) {
                        throw new IOException(e);
                    } else {
                        Utils.waitSecond(3L * tries);
                    }
                }
            }
        }
        return null;
    }

    public static Document getDoc(final String url) throws IOException {
        return getDoc(url, null);
    }

    public static Document getDoc(final String url, final int maxTriesCnt) throws IOException {
        return getDoc(url, null, maxTriesCnt);
    }

    public static Document getDoc(final String url, final String referrer) throws IOException {
        return getDoc(url, referrer, 99);
    }

    public static final double permitsPerSecond = 10.0;

    private static final RateLimiter rateLimiter = RateLimiter.create(permitsPerSecond);

    public static Document getDoc(final String url, final String referrer, final int maxTriesCnt) throws IOException {
        final String clearedUrl = getClearedUrl(url, referrer);
        final String fileToSave = Utils.getDir() + clearedUrl + ".html";
        final File file = new File(fileToSave);
        if (file.exists() && Utils.equalsWoTime(new Date(file.lastModified()), new Date())) {
            logger.trace("Взят из кэша: {}", file.getAbsolutePath());
            return Jsoup.parse(file, "UTF-8", Wizard.host);
        } else {
            logger.trace("Запрошен адрес: {}", url);

            for (int tries = 1; tries <= maxTriesCnt; ++tries) {
                try {
                    rateLimiter.acquire();
                    final Connection conn = Jsoup.connect(url);
                    if (referrer != null && !referrer.isEmpty()) {
                        logger.trace("referrer: {}", referrer);
                        conn.referrer(referrer);
                    }
                    conn.header("Accept-Language", "ru");
                    conn.header("Accept-Encoding", "gzip, deflate");
                    conn.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0");
                    conn.maxBodySize(0);
                    conn.timeout(60_000);
                    final Document doc = conn.get();
                    Utils.writeFile(fileToSave, doc.outerHtml());
                    return doc;
                } catch (final IOException e) {
                    logger.error("Ошибка при запросе, попытка #{} из {}: {}", tries, maxTriesCnt, url);
                    logger.error("Ошибка: {}", e.getLocalizedMessage());
                    if (maxTriesCnt == tries) {
                        throw new IOException(e);
                    } else {
                        Utils.waitSecond(3L * tries);
                    }
                }
            }
        }
        return null;
    }

    public static void main(final String[] args) throws IOException {
        final Document doc = Jsoup.connect(Wizard.host + "olga/main/geo/citylist/331858").get();
        Utils.writeFile(Utils.getDir() + "citylist.html", doc.outerHtml());
    }
}