package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TopRetailParserTest {

    @Test
    void getShopListTest() throws Exception {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));
        final String realm = "olga";
        final String host = Wizard.host;
        final Product product = ProductInitParser.getTradingProduct(host, realm, "423863");
        final List<Region> regions = CityInitParser.getRegions(host, realm);
        final List<Country> countries = new ArrayList<>();
        countries.add(new Country("2931", "Россия"));
        final List<City> cities = new ArrayList<>();
        final City city = new City("2931", "2932", "2933", "Москва", 10, 0, 0, 0, 0, null);
        cities.add(city);
        final Map<String, List<CountryDutyList>> countriesDutyList = CountryDutyListParser.getAllCountryDutyList(host, realm, countries);
        final List<TradeAtCity> stats = CityParser.collectByTradeAtCities(host, realm, cities, product, countriesDutyList, regions);
        assertFalse(stats.isEmpty());
        assertTrue(stats.stream().anyMatch(l -> !l.getMajorSellInCityList().isEmpty()));
        //заправки
        final List<Shop> shops = TopRetailParser.getShopList(host, realm, stats, product);
        assertFalse(shops.isEmpty());
    }
}