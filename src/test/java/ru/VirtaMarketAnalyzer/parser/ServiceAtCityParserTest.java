package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceAtCityParserTest {

    @Test
    void getTest() throws IOException {
        final String host = Wizard.host;
        final String realm = "olga";
        final City city = new City("3010", "3023", "3025", "Николаев", 10, 0, 0, 0, 0, null);
        final List<City> cities = new ArrayList<>();
        cities.add(city);

        final List<UnitTypeSpec> specializations = new ArrayList<>();
        final List<RawMaterial> rawMaterials = new ArrayList<>();
        specializations.add(new UnitTypeSpec("348214", "Фитнес", ProductInitParser.getTradingProduct(host, realm, "15337"), rawMaterials));
        final UnitType service = new UnitType("348207", "Фитнес-центр", "", specializations);
        final List<RentAtCity> rentAtCity = RentAtCityParser.getUnitTypeRent(Wizard.host, realm, cities);
        final List<ServiceAtCity> serviceAtCity = ServiceAtCityParser.get(host, realm, cities, service, null, rentAtCity);
        assertFalse(serviceAtCity.isEmpty());
    }

    @Test
    void getEmptyTurnIdTest() throws IOException {
        //Не найден turn_id
        final String host = Wizard.host;
        final String realm = "anna";
        final City city = new City("2931", "2961", "424013", "Масейо", 10, 0, 0, 0, 0, null);
        final List<City> cities = new ArrayList<>();
        cities.add(city);

        final UnitType service = new UnitType("422825", "Авторемонтная мастерская", "", new ArrayList<>());
        final List<RentAtCity> rentAtCity = RentAtCityParser.getUnitTypeRent(Wizard.host, realm, cities);
        final List<ServiceAtCity> serviceAtCity = ServiceAtCityParser.get(host, realm, cities, service, null, rentAtCity);
        assertFalse(serviceAtCity.isEmpty());
    }
}