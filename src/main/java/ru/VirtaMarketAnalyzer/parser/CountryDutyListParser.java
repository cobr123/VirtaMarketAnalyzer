package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.Region;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cobr123 on 16.03.2016.
 */
final public class CountryDutyListParser {
    private static final Logger logger = LoggerFactory.getLogger(CountryDutyListParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        final String url = "http://virtonomica.ru/olga/main/geo/countrydutylist/";
        final List<Region> regions = new ArrayList<>();
        regions.add(new Region("", "3054", "", 0.0));
        logger.info(Utils.getPrettyGson(getCountryDutyList(url, regions)));
    }

    public static List<CountryDutyList> getCountryDutyList(final String url, final List<Region> regions) throws IOException {
        return regions.stream().map(region -> {
            return getCountryDuty(url + region.getId());
        }).collect(toList());
    }

    public static CountryDutyList getCountryDuty(final String url) throws IOException {
        final Document doc = Downloader.getDoc(url);
        final Element table = doc.select("table[class=\"grid\"]").last();
//        System.out.println(table.outerHtml());
        final Elements towns = table.select("table > tbody > tr");
        towns.stream().filter(town -> !town.select("tr > td:nth-child(1) > a").isEmpty()).forEach(town -> {
            final String[] parts = town.select("tr > td:nth-child(1) > a").eq(0).attr("href").split("/");
            final String caption = town.select("tr > td:nth-child(1) > a").eq(0).text();
            final String id = parts[parts.length - 1];
            final String averageSalary = town.select("tr > td:nth-child(3)").text();
            final String educationIndex = town.select("tr > td:nth-child(5)").text();
            final String wealthIndex = town.select("tr > td:nth-child(6)").html();
            cities.add(new City(region.getCountryId(), region.getId(), id, caption, Utils.toDouble(wealthIndex), Utils.toDouble(educationIndex), Utils.toDouble(averageSalary)));
        });
    }
}
