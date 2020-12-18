package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TopRetailParserTest {

    @Test
    void getShopListTest() throws Exception {
        final String realm = "olga";
        final String host = Wizard.host;
        final Product product = ProductInitParser.getTradingProduct(host, realm, "423863");
        final List<City> cities = new ArrayList<>();
        final City city = new City("2931", "2932", "2933", "Москва", 10, 0, 0, 0, 0, null);
        cities.add(city);
        final List<TradeAtCity> stats = CityParser.collectByTradeAtCities(host, realm, cities, product);
        assertFalse(stats.isEmpty());
        assertTrue(stats.stream().anyMatch(l -> !l.getMajorSellInCityList().isEmpty()));
        //заправки
        final List<Shop> shops = TopRetailParser.getShopList(host, realm, stats, product);
        assertFalse(shops.isEmpty());
    }
}