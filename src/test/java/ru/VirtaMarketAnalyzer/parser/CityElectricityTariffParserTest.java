package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.CityElectricityTariff;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CityElectricityTariffParserTest {

    @Test
    void getAllCityElectricityTariffListTest() throws IOException {
        final String realm = "olga";
        final List<City> cities = new ArrayList<>();
        final City city = new City("3010", "3023", "3055", "Николаев", 10, 0, 0, 0, 0, null);
        cities.add(city);
        final Map<String, List<CityElectricityTariff>> allCityElectricityTariffList = CityElectricityTariffParser.getAllCityElectricityTariffList(Wizard.host, realm, cities);
        assertFalse(allCityElectricityTariffList.isEmpty());
        assertFalse(allCityElectricityTariffList.get("3868").isEmpty());
    }
}