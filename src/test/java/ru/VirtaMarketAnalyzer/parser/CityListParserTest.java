package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CityListParserTest {

    @Test
    void getCitiesTest() throws Exception {
        final List<City> list = CityListParser.getCities(Wizard.host, "olga");
        assertFalse(list.isEmpty());
    }
}