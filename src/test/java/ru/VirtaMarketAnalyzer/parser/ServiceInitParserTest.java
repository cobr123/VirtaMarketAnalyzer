package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.UnitType;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceInitParserTest {

    @Test
    void getServiceUnitTypesTest() throws Exception {
        final List<UnitType> unitTypesRu = ServiceInitParser.getServiceUnitTypes(Wizard.host, "olga");
        final List<UnitType> unitTypesEn = ServiceInitParser.getServiceUnitTypes(Wizard.host_en, "olga");
        assertFalse(unitTypesRu.isEmpty());
        assertFalse(unitTypesEn.isEmpty());
        assertEquals(unitTypesRu.size(), unitTypesEn.size());
        assertEquals(unitTypesRu.size(), 8);
        assertFalse(unitTypesRu.get(0).getSpecializations().isEmpty());
        assertFalse(unitTypesEn.get(0).getSpecializations().isEmpty());
        assertFalse(unitTypesRu.get(0).getSpecializations().get(0).getRawMaterials().isEmpty());
        assertFalse(unitTypesEn.get(0).getSpecializations().get(0).getRawMaterials().isEmpty());
    }
}