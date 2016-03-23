package ru.VirtaMarketAnalyzer.main;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final Pattern fraction_pattern = Pattern.compile("^\\d+(\\.\\d+)?/\\d+(\\.\\d+)?$");

    public static String getDir() {
        String dir = System.getProperty("java.io.tmpdir");
        if (dir.charAt(dir.length() - 1) != File.separatorChar) {
            dir += File.separator;
        }
        return dir + "VirtaMarketAnalyzer" + File.separator;
    }

    public static void writeToGson(final String path, final Object obj) throws IOException {
        logger.trace(path);
        Utils.writeFile(path, getGson(obj));
    }

    public static void writeToGson(final String path, final Object obj, final ExclusionStrategy es) throws IOException {
        logger.trace(path);
        Utils.writeFile(path, getGson(obj, es));
    }

    public static String getGson(final Object obj) {
        final Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static String getGson(final Object obj, final ExclusionStrategy es) {
        final Gson gson = new GsonBuilder().setExclusionStrategies(es).create();
        return gson.toJson(obj);
    }

    public static String getPrettyGson(final Object obj) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(obj);
    }

    public static File mkdirs(final String path) {
        final File file = new File(path);
        if (!file.exists()) {
            final File dir = new File(file.getParent());
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        return file;
    }

    public static void writeFile(final String path, final String content)
            throws IOException {
        final File file = Utils.mkdirs(path);
        FileUtils.writeStringToFile(file, content, "UTF-8");
    }

    public static String readFile(final String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
    }

    public static String clearNumber(final String text) {
        return text.replace("$", "").replace("+", "").replace("%", "").replaceAll("\\p{L}+\\.?", "").replaceAll("\\p{InCyrillic}+\\.?", "").replaceAll("\\s+", "").trim();
    }

    public static int doubleToInt(final double num) {
        final int res = (int) num;
        if (res == num) {
            return res;
        } else {
            throw new IllegalArgumentException("Не удалось преобразовать double \"" + num + "\" в int без потери точности");
        }
    }

    public static double toDouble(final String text) {
        try {
            final String clear = clearNumber(text);
            if (clear.isEmpty() || "-".equals(text)) {
                return 0.0;
            } else {
                final Matcher matcher = fraction_pattern.matcher(clear);
                if (matcher.find()) {
                    final String[] data = clear.split("/");
                    return Double.valueOf(data[0]) / Double.valueOf(data[1]);
                } else {
                    return Double.valueOf(clear);
                }
            }
        } catch (final Exception e) {
            logger.error("Не удалось преобразовать строку \"" + text + "\" в double");
            throw e;
        }
    }

    public static long toLong(final String text) {
        try {
            final String clear = clearNumber(text);
            if (clear.isEmpty()) {
                return 0;
            } else {
                return Long.valueOf(clear);
            }
        } catch (final Exception e) {
            logger.error("Не удалось преобразовать строку \"" + text + "\" в long");
            throw e;
        }
    }

    public static int toInt(final String text) {
        try {
            final String clear = clearNumber(text);
            if (clear.isEmpty()) {
                return 0;
            } else {
                return Integer.valueOf(clear);
            }
        } catch (final Exception e) {
            logger.error("Не удалось преобразовать строку \"" + text + "\" в int");
            throw e;
        }
    }

    public static Date getZeroTimeDate(final Date date) {
        final Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static boolean equalsWoTime(final Date date1, final Date date2) {
        return getZeroTimeDate(date1).equals(getZeroTimeDate(date2));
    }

    public static int daysBetween(final Date date1, final Date date2) {
        return (int) ((getZeroTimeDate(date1).getTime() - getZeroTimeDate(date2).getTime()) / (1000 * 60 * 60 * 24));
    }

    public static String getNextPageHref(final Document doc) {
        final Element currPage = doc.select("ul[class=\"pager_list pull-right\"] > li.selected").last();
        if (currPage == null || currPage.parent() == null) {
            logger.trace("currPage is null");
            return "";
        }
        final Element nextPageLink = currPage.nextElementSibling();
        if (nextPageLink != null && "li".equalsIgnoreCase(nextPageLink.nodeName())) {
            logger.trace("nextPageLink = " + nextPageLink);
            return nextPageLink.select("> a").attr("href");
        } else {
            logger.trace("nextPageLink is null");
        }
        return "";
    }

    public static String getFirstBySep(final String str, final String sep) {
        final String[] data = str.split(sep);
        return data[0];
    }

    public static String getLastBySep(final String str, final String sep) {
        final String[] data = str.split(sep);
        return data[data.length - 1];
    }

    public static String getLastFromUrl(final String url) {
        return getLastBySep(url, "/");
    }
}
