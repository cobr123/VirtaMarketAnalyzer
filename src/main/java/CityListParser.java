import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class CityListParser {
    public static void main(final String[] args) throws IOException {
        //http://virtonomica.ru/olga/main/geo/citylist/331858
        final File input = new File("d://citylist.html");
        final Document doc = Jsoup.parse(input, "WINDOWS-1251", "http://virtonomica.ru/");
        final Element table = doc.select("table[class=\"grid\"]").last();
        //System.out.println(list.outerHtml());
        final Elements towns = table.select("table > tbody > tr");
        for (Element town : towns) {
            final String[] parts = town.select("tr > td:nth-child(1) > a").eq(0).attr("href").split("/");
            System.out.println(parts[parts.length-1]);
            System.out.println(Utils.toDouble(town.select("tr > td:nth-child(6)").html()));
        }
    }

}
