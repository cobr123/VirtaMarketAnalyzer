import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class GdsInitParser {
    public static void main(final String[] args) throws IOException {
        //http://virtonomica.ru/olga/main/common/main_page/game_info/trading
        final File input = new File("d://trading.html");
        final Document doc = Jsoup.parse(input, "WINDOWS-1251", "http://virtonomica.ru/");

        final Elements links = doc.select("table[class=\"list\"] > tbody > tr > td > a");
        //System.out.println(list.outerHtml());
        for (final Element link : links) {
            System.out.println(link.attr("title"));
            final String[] parts = link.attr("href").split("/");
            System.out.println(parts[parts.length-1]);
        }
    }
}
