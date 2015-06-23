package ru.VirtaMarketAnalyzer.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final Pattern fraction_pattern = Pattern.compile("^\\d+(\\.\\d+)?/\\d+(\\.\\d+)?$");

    public static String getDir() throws IOException {
        String dir = System.getProperty("java.io.tmpdir");
        if (dir.charAt(dir.length() - 1) != File.separatorChar) {
            dir += File.separator;
        }
        return dir + "VirtaMarketAnalyzer" + File.separator;
    }

    public static void writeToGson(final String path, final Object obj) throws IOException {
        logger.info(path);
        Utils.writeFile(path, getGson(obj));
    }

    public static String getGson(final Object obj) throws IOException {
        final Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static String getPrettyGson(final Object obj) throws IOException {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(obj);
    }

    public static void writeFile(final String path, final String content)
            throws IOException {
        final File file = new File(path);
        if (!file.exists()) {
            final File dir = new File(file.getParent());
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), "UTF-8"))) {
            writer.write(content);
        }
    }

    public static String readFile(final String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
    }

    public static String clearNumber(final String text) {
        return text.replace("$", "").replace("+", "").replace("%", "").replaceAll("\\p{InCyrillic}+\\.?", "").replaceAll("\\s+", "").trim();
    }

    public static double toDouble(final String text) {
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
    }

    public static long toLong(final String text) {
        final String clear = clearNumber(text);
        if (clear.isEmpty()) {
            return 0;
        } else {
            return Long.valueOf(clear);
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
}
