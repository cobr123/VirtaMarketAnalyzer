package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.UnitType;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceInitParserTest {

    @Test
    void getServiceUnitTypesTest() throws Exception {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));
        final List<UnitType> unitTypesRu = ServiceInitParser.getServiceUnitTypes(Wizard.host, "olga");
        final List<UnitType> unitTypesEn = ServiceInitParser.getServiceUnitTypes(Wizard.host_en, "olga");
        assertFalse(unitTypesRu.isEmpty());
        assertFalse(unitTypesEn.isEmpty());
        assertEquals(unitTypesRu.size(), unitTypesEn.size());
        assertFalse(unitTypesRu.get(0).getSpecializations().isEmpty());
        assertFalse(unitTypesEn.get(0).getSpecializations().isEmpty());
        assertFalse(unitTypesRu.get(0).getSpecializations().get(0).getRawMaterials().isEmpty());
        assertFalse(unitTypesEn.get(0).getSpecializations().get(0).getRawMaterials().isEmpty());
    }
}