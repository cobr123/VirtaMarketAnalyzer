package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CityParserTest {

    @Test
    void collectByTradeAtCitiesTest() throws IOException {
        final String realm = "olga";
        final List<City> cities = new ArrayList<>();
        final City city = new City("2931", "2932", "2933", "Москва", 10, 0, 0, 0, 0, null);
        cities.add(city);
        final List<Product> products = ProductInitParser.getTradingProducts(Wizard.host, realm);
        final List<TradeAtCity> stats = CityParser.collectByTradeAtCities(Wizard.host, realm, cities, products.get(0));
        assertFalse(stats.isEmpty());
    }

    @Test
    void collectByTradeAtCitiesTest2() throws IOException {
        final String realm = "vera";
        final List<City> cities = new ArrayList<>();
        final City city = new City("423255", "423256", "423257", "Москва", 10, 0, 0, 0, 0, null);
        cities.add(city);
        final Product product = ProductInitParser.getTradingProduct(Wizard.host, realm, "422717");
        final List<TradeAtCity> stats = CityParser.collectByTradeAtCities(Wizard.host, realm, cities, product);
        assertFalse(stats.isEmpty());
    }
}