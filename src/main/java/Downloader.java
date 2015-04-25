import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class Downloader {
    public static void main(final String[] args) throws IOException {
        final Document doc = Jsoup.connect("http://virtonomica.ru/olga/main/geo/citylist/331858").get();
        Utils.writeFile("d://citylist.html", doc.outerHtml());
    }

}
