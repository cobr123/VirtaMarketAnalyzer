package ru.VirtaMarketAnalyzer.scrapper;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.RateLimiter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class Downloader {
    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    private static final Cache<String, String> jsonCache = Caffeine.newBuilder()
            .maximumSize(500)
            .build();

    private static final Cache<String, Document> htmlCache = Caffeine.newBuilder()
            .maximumSize(500)
            .build();

    public static void invalidateCache(final String url) {
        jsonCache.invalidate(url);
        htmlCache.invalidate(url);
    }

    public static String getJson(final String url) throws IOException {
        return getJson(url, 99);
    }

    final static Map<String, String> loginCookies = new HashMap<>();

    static {
        try {
            final String login = System.getenv("vma_login");
            final String password = System.getenv("vma_password");
            if (login == null || login.isEmpty()) {
                throw new IllegalArgumentException("Необходим логин виртономики, иначе api вернет данные реалма vera для всех остальных реалмов (vma_login)");
            }
            if (password == null || password.isEmpty()) {
                throw new IllegalArgumentException("Необходим пароль виртономики, иначе api вернет данные реалма vera для всех остальных реалмов (vma_password)");
            }
            final Connection.Response res = Jsoup.connect(Wizard.host + "olga/main/user/login")
                    .data("userData[login]", login, "userData[password]", password)
                    .method(Connection.Method.POST)
                    .execute();
            loginCookies.clear();
            loginCookies.putAll(res.cookies());
        } catch (final IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public static String getJson(final String url, final int maxTriesCnt) {
        return jsonCache.get(url, key -> {
            logger.trace("Запрошен адрес: {}", url);

            for (int tries = 1; tries <= maxTriesCnt; ++tries) {
                try {
                    rateLimiter.acquire();
                    final Connection conn = Jsoup.connect(url);
                    conn.header("Accept-Language", "ru");
                    conn.header("Accept-Encoding", "gzip, deflate");
                    conn.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0");
                    conn.maxBodySize(0);
                    conn.timeout(60_000);
                    conn.ignoreContentType(true);
                    conn.cookies(loginCookies);
                    return conn.execute().body();
                } catch (final IOException e) {
                    logger.error("Ошибка при запросе, попытка #{} из {}: {}", tries, maxTriesCnt, url);
                    logger.error("Ошибка: {}", e.getLocalizedMessage());
                    if (maxTriesCnt == tries) {
                        throw new RuntimeException(e);
                    } else {
                        Utils.waitSecond(3L * tries);
                    }
                }
            }
            return null;
        });
    }

    public static Document getDoc(final String url) throws IOException {
        return getDoc(url, "");
    }

    public static Document getDoc(final String url, final int maxTriesCnt) throws IOException {
        return getDoc(url, "", maxTriesCnt);
    }

    public static Document getDoc(final String url, final String referrer) throws IOException {
        return getDoc(url, referrer, 99);
    }

    public static final double permitsPerSecond = 10.0;

    private static final RateLimiter rateLimiter = RateLimiter.create(permitsPerSecond);

    public static Document getDoc(final String url, final String referrer, final int maxTriesCnt) {
        return htmlCache.get(url + referrer, key -> {
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
                    return conn.get();
                } catch (final IOException e) {
                    logger.error("Ошибка при запросе, попытка #{} из {}: {}", tries, maxTriesCnt, url);
                    logger.error("Ошибка: {}", e.getLocalizedMessage());
                    if (maxTriesCnt == tries) {
                        throw new RuntimeException(e);
                    } else {
                        Utils.waitSecond(3L * tries);
                    }
                }
            }
            return null;
        });
    }

}