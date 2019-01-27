package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CityParserTest {

//    @Test
//    void collectByTradeAtCitiesTest() throws IOException {
//        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
//        final String realm = "olga";
//        final String url = Wizard.host + realm + "/main/geo/countrydutylist/";
//        final List<Country> countries = new ArrayList<>();
//        countries.add(new Country("2931", "Россия"));
//        final List<City> cities = new ArrayList<>();
//        final City city = new City("3010", "3023", "3055", "Николаев", 10, 0, 0, 0, 0, null);
//        cities.add(city);
//        final List<Product> products = ProductInitParser.getTradingProducts(Wizard.host, realm);
//        final List<Region> regions = CityInitParser.getRegions(Wizard.host, realm);
//        final Map<String, List<CountryDutyList>> countriesDutyList = CountryDutyListParser.getAllCountryDutyList(Wizard.host, realm, countries);
//        final List<TradeAtCity> stats = CityParser.collectByTradeAtCities(Wizard.host, realm, cities, products.get(0), countriesDutyList, regions);
//        assertFalse(stats.isEmpty());
//    }
}