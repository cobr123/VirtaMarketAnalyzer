import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Utils {
    static void writeFile(final String path, final String content)
            throws IOException {
        FileOutputStream fop = null;

        try {
            final File file = new File(path);
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            // get the content in bytes
            final byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static String clearNumber(final String text) {
        return text.replace("$", "").replace("ед.", "").replace("менее", "").replace("около", "").replace("более", "").replaceAll("\\s+", "").trim();
    }

    public static double toDouble(final String text) {
        final String clear = clearNumber(text);
        if (clear.isEmpty()) {
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
}
