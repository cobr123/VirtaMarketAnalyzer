package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.VirtaMarketAnalyzer.data.Country;
import ru.VirtaMarketAnalyzer.data.CountryDutyList;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by cobr123 on 16.03.2016.
 */
final public class CountryDutyListParser {
    private static final Logger logger = LoggerFactory.getLogger(CountryDutyListParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        final String url = "http://virtonomica.ru/olga/main/geo/countrydutylist/";
        final List<Country> countries = new ArrayList<>();
        countries.add(new Country("3054", "Азербайджан"));
        final List<Product> products = new ArrayList<>();
        products.add(new Product("", "/img/products/diamonds.gif", "1460", "Алмазы"));
        logger.info(Utils.getPrettyGson(getAllCountryDutyList(url, countries, products)));
    }

    public static Map<String, List<CountryDutyList>> getAllCountryDutyList(final String url, final List<Country> countries, final List<Product> products) throws IOException {
        return countries.stream().map(country -> {
            try {
                return getCountryDutyList(url, country, products);
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
        })
                .flatMap(Collection::stream)
                .filter(elem -> elem != null)
                .collect(groupingBy(CountryDutyList::getCountryId));
    }

    public static List<CountryDutyList> getCountryDutyList(final String url, final Country country, final List<Product> products) throws IOException {
        final Document doc = Downloader.getDoc(url + country.getId());
        final Elements imgElems = doc.select("table.list > tbody > tr > td:nth-child(1) > img");
        return imgElems.stream().map(el -> {
            try {
                return getCountryDutyList(el, country, products);
            } catch (Exception e) {
                logger.info(url + country.getId());
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
        }).collect(Collectors.toList());
    }

    public static CountryDutyList getCountryDutyList(final Element elem, final Country country, final List<Product> products) throws Exception {
        final String countryId = country.getId();
        final Optional<Product> product = products.stream().filter(p -> p.getImgUrl().equalsIgnoreCase(elem.attr("src"))).findFirst();
        if (!product.isPresent()) {
            throw new Exception("Не найден продукт с изображением '" + elem.attr("src") + "'");
        }
        final String productId = product.get().getId();
        elem.parent().nextElementSibling().nextElementSibling().children().remove();
        final int exportTaxPercent = Utils.toInt(elem.parent().nextElementSibling().nextElementSibling().text());
        elem.parent().nextElementSibling().nextElementSibling().nextElementSibling().children().remove();
        final int importTaxPercent = Utils.toInt(elem.parent().nextElementSibling().nextElementSibling().nextElementSibling().text());
        final double indicativePrice = Utils.toDouble(elem.parent().nextElementSibling().nextElementSibling().nextElementSibling().nextElementSibling().text());
        return new CountryDutyList(countryId, productId, exportTaxPercent, importTaxPercent, indicativePrice);
    }
}
