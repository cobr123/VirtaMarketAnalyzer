import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class CityParser {
    public static void main(final String[] args) throws IOException {
        //http://virtonomica.ru/olga/main/globalreport/marketing/by_trade_at_cities/370077/7060/7063/7076
        final File input = new File("d://test.html");
        final Document doc = Jsoup.parse(input, "WINDOWS-1251", "http://virtonomica.ru/");
        final Element table = doc.select("table[class=\"grid\"]").first();
        //System.out.println(table.outerHtml());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td > img").attr("src"));
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td > table > tbody > tr > td").eq(4).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td > table > tbody > tr:nth-child(3) > td").eq(4).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(2) > td").eq(0).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(2) > td").eq(1).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(3) > td").eq(0).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(3) > td").eq(1).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(4) > td").eq(0).html());
//        System.out.println(table.nextElementSibling().select("table > tbody > tr > td:nth-child(3) > table > tbody > tr:nth-child(4) > td").eq(1).html());

        final Element list = doc.select("table[class=\"list\"]").last();
        //System.out.println(list.outerHtml());
        final Elements bestInTown = list.select("table > tbody > tr");
        for (Element best : bestInTown) {
            System.out.println(best.select("tr > td:nth-child(1) > div:nth-child(2) > img").eq(0).attr("title"));
            best.select("tr > td:nth-child(1) > div:nth-child(2) > img").eq(0).remove();
            System.out.println(best.select("tr > td:nth-child(1) > div:nth-child(2)").html().replace("&nbsp;", " ").trim());
            System.out.println(Utils.toLong(best.select("tr > td").eq(1).html()));
            System.out.println(best.select("tr > td").eq(2).html());
            System.out.println(Utils.toLong(best.select("tr > td").eq(3).html()));
            System.out.println(Utils.toDouble(best.select("tr > td").eq(4).html()));
            System.out.println(Utils.toDouble(best.select("tr > td").eq(5).html()));
            System.out.println(Utils.toDouble(best.select("tr > td").eq(6).html()));
        }
    }
}
