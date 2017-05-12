package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.Region;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class CityListParser {
    private static final Logger logger = LoggerFactory.getLogger(CityListParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));

        final Document doc = Downloader.getDoc(Wizard.host + "olga/main/geo/citylist/331858");
        final Element table = doc.select("table[class=\"grid\"]").last();
        //System.out.println(list.outerHtml());
        final Elements towns = table.select("table > tbody > tr");
        for (Element town : towns) {
            final String[] parts = town.select("tr > td:nth-child(1) > a").eq(0).attr("href").split("/");
            logger.info(parts[parts.length - 1]);
            logger.info("" + Utils.toDouble(town.select("tr > td:nth-child(6)").html()));
        }
    }

    public static List<City> fillWealthIndex(final String host, final String realm, final List<Region> regions) throws IOException {
        final List<City> cities = new ArrayList<>();
        for (final Region region : regions) {
            getWealthIndex(host, realm, region, cities);
        }
        return cities;
    }

    public static void getWealthIndex(final String host, final String realm, final Region region, final List<City> cities) throws IOException {
        final Document doc = Downloader.getDoc(host + realm + "/main/geo/citylist/" + region.getId());
        final Element table = doc.select("table[class=\"grid\"]").last();
//        System.out.println(table.outerHtml());
        final Elements towns = table.select("table > tbody > tr");
        towns.stream().filter(town -> !town.select("tr > td:nth-child(1) > a").isEmpty()).forEach(town -> {
            try {
                final String[] parts = town.select("tr > td:nth-child(1) > a").eq(0).attr("href").split("/");
                final String caption = town.select("tr > td:nth-child(1) > a").eq(0).text();
                final String id = parts[parts.length - 1];
                final String population = town.select("tr > td:nth-child(2)").text();
                final String averageSalary = town.select("tr > td:nth-child(3)").text();
                final String educationIndex = town.select("tr > td:nth-child(5)").text();
                final String wealthIndex = town.select("tr > td:nth-child(6)").html();
                final String demography = town.select("tr > td:nth-child(7)").text()
                        .replace("ths.", "")
                        .replace("тыс. чел.", "")
                        .replaceAll("\\s+", "");

                final Document docCity = Downloader.getDoc(host + realm + "/main/geo/city/" + id);
                final String bonusHtml = cleanPreserveLineBreaks(docCity.select("#mainContent > div:nth-child(8) > table > tbody > tr:nth-child(2) > td:nth-child(3) > div").html().replaceAll("<div>","<br>")).trim();
                final List<String> mayoralBonuses = Stream.of(bonusHtml.split("\\n"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

                cities.add(new City(region.getCountryId(), region.getId(), id, caption
                        , Utils.toDouble(wealthIndex)
                        , Utils.toDouble(educationIndex)
                        , Utils.toDouble(averageSalary)
                        , Utils.toInt(demography)
                        , Utils.toInt(population)
                        , mayoralBonuses
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static String cleanPreserveLineBreaks(final String bodyHtml) {
        // get pretty printed html with preserved br and p tags
        final String prettyPrintedBodyFragment = Jsoup.clean(bodyHtml, "", Whitelist.none().addTags("br", "p"), new Document.OutputSettings().prettyPrint(true));
        // get plain text with preserved line breaks by disabled prettyPrint
        final String cleaned = Jsoup.clean(prettyPrintedBodyFragment, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        return cleaned;
    }
}