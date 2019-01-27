package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CityListParserTest {

    @Test
    void getCitiesTest() throws Exception {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        final List<City> list = CityListParser.getCities(Wizard.host, "olga");
        assertFalse(list.isEmpty());
    }
}