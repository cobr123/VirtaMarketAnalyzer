package ru.VirtaMarketAnalyzer.main;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Utils {
    public static String getDir() throws IOException {
        return System.getProperty("java.io.tmpdir") + File.separator + "VirtaMarketAnalyzer" + File.separator;
    }

    public static void writeToGson(final String path, final Object obj) throws IOException {
        final Gson gson = new Gson();
        Utils.log(path);
        Utils.writeFile(path, gson.toJson(obj));
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
        return text.replace("$", "").replace("%", "").replace("ед.", "").replace("менее", "").replace("около", "").replace("более", "").replaceAll("\\s+", "").trim();
    }

    public static double toDouble(final String text) {
        final String clear = clearNumber(text);
        if (clear.isEmpty() || "-".equals(text)) {
            return 0.0;
        } else {
            return Double.valueOf(clear);
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

    public static void log(final Object... lineParts) {
        System.out.println(Arrays.toString(lineParts));
    }

    public static Date getZeroTimeDate(final Date fecha) {
        final Date res = fecha;
        final Calendar calendar = Calendar.getInstance();

        calendar.setTime(fecha);
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
