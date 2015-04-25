import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class CityInitParser {
    public static void main(final String[] args) throws IOException {
        //http://virtonomica.ru/olga/main/globalreport/marketing/by_trade_at_cities/
        final File input = new File("d://by_trade_at_cities.html");
        final Document doc = Jsoup.parse(input, "WINDOWS-1251", "http://virtonomica.ru/");

        final Elements options = doc.select("option[class=\"geocombo f-mx\"]");
        //System.out.println(list.outerHtml());
        for (Element opt : options) {
            System.out.println(opt.text());
            System.out.println(opt.attr("value"));
        }
    }
}
