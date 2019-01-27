package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.Country;
import ru.VirtaMarketAnalyzer.data.Region;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CityInitParserTest {

    @Test
    void getRegionsTest() throws IOException {
        final List<Region> list = CityInitParser.getRegions(Wizard.host, "olga");
        assertFalse(list.isEmpty());
    }

    @Test
    void getCountriesTest() throws IOException {
        final List<Country> list = CityInitParser.getCountries(Wizard.host, "olga");
        assertFalse(list.isEmpty());
    }
}