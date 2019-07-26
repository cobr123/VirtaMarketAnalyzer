package ru.VirtaMarketAnalyzer.main;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public static void writeToGsonZip(final String path, final Object obj) throws IOException {
        logger.trace(path);
        Utils.writeToZip(path, obj);
        final File file = new File(path);
        if (file.exists()) {
            file.deleteOnExit();
        }
    }

    public static void writeToZip(final String path, final Object obj) throws IOException {
        final File file = Utils.mkdirs(path + ".zip");
        try (final ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(file.toPath()))) {
            final ZipEntry e = new ZipEntry(new File(path).getName());
            out.putNextEntry(e);

            final byte[] data = getGson(obj).getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
        }
    }

    public static String readFromZip(final String path, final ByteArrayOutputStream os) throws IOException {
        try (final ZipFile zipFile = new ZipFile(new SeekableInMemoryByteChannel(os.toByteArray()))) {
            return IOUtils.toString(zipFile.getInputStream(zipFile.getEntry(path)), StandardCharsets.UTF_8);
        }
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
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    public static String clearNumber(final String text) {
        if (text.equals("-")) {
            return "";
        } else if (text.toUpperCase().contains("E-") || text.toUpperCase().contains("E+")) {
            return text.replace("$", "").replace("+", "").replace("*", "").replace("%", "").replace("©", "").replaceAll("\\s+", "").trim();
        } else {
            return text.replace("$", "").replace("+", "").replace("*", "").replace("%", "").replace("©", "").replaceAll("\\p{L}+\\.?", "").replaceAll("\\p{InCyrillic}+\\.?", "").replaceAll("\\s+", "").trim();
        }
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
                    return Double.parseDouble(data[0]) / Double.parseDouble(data[1]);
                } else {
                    return Double.parseDouble(clear);
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
                return Long.parseLong(clear);
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
                return Integer.parseInt(clear);
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

    public static double round2(final double num) {
        return Math.round(num * 100.0) / 100.0;
    }

    public static <T> T repeatOnErr(final Callable<T> func) throws Exception {
        try {
            return func.call();
        } catch (final Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            Utils.waitSecond(1);
            return func.call();
        }
    }


    private static class Prefix<T> {
        final T value;
        final Prefix<T> parent;

        Prefix(Prefix<T> parent, T value) {
            this.parent = parent;
            this.value = value;
        }

        // put the whole prefix into given collection
        <C extends Collection<T>> C addTo(C collection) {
            if (parent != null)
                parent.addTo(collection);
            collection.add(value);
            return collection;
        }
    }

    private static <T, C extends Collection<T>> Stream<C> comb(
            List<? extends Collection<T>> values, int offset, Prefix<T> prefix,
            Supplier<C> supplier) {
        if (offset == values.size() - 1)
            return values.get(offset).stream()
                    .map(e -> new Prefix<>(prefix, e).addTo(supplier.get()));
        return values.get(offset).stream()
                .flatMap(e -> comb(values, offset + 1, new Prefix<>(prefix, e), supplier));
    }

    public static <T, C extends Collection<T>> Stream<C> ofCombinations(
            Collection<? extends Collection<T>> values, Supplier<C> supplier) {
        if (values.isEmpty())
            return Stream.empty();
        return comb(new ArrayList<>(values), 0, null, supplier);
    }

    //максимальное кол-во работающих с заданной квалификацией на предприятиии для заданной квалификации игрока (топ-1)
    public static double calcMaxTop1(final double playerQuality, final double workersQuality) {
        final double workshopLoads = 50.0;
        return Math.floor(workshopLoads * 14.0 * playerQuality * playerQuality / Math.pow(1.4, workersQuality) / 5.0);
    }

    //квалификация игрока необходимая для данного уровня технологии
    public static double calcPlayerQualityForTech(final double techLvl) {
        return Math.pow(2.72, Math.log(techLvl) / (1.0 / 3.0)) * 0.0064;
    }

    //квалификация рабочих необходимая для данного уровня технологии
    public static double calcWorkersQualityForTech(final double techLvl) {
        return Math.pow(techLvl, 0.8);
    }

    public static void waitSecond(final long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (null == json) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<T>() {
        }.getType());
    }
}
