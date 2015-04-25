package ru.VirtaMarketAnalyzer.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.File;
import java.io.IOException;

/**
 * Created by cobr123 on 24.04.2015.
 */
public final class ProductInitParser {
    public static void main(final String[] args) throws IOException {
        final File input = Downloader.get("http://virtonomica.ru/olga/main/common/main_page/game_info/trading");
        final Document doc = Jsoup.parse(input, "UTF-8", "http://virtonomica.ru/");

        final Elements links = doc.select("table[class=\"list\"] > tbody > tr > td > a");
        //System.out.println(list.outerHtml());
        for (final Element link : links) {
            System.out.println(link.attr("title"));
            final String[] parts = link.attr("href").split("/");
            System.out.println(parts[parts.length-1]);
        }
    }
}
