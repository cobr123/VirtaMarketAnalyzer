package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.TechLvl;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TechMarketAskParserTest {

    @Test
    void getTechTest() throws IOException {
        //https://virtonomica.ru/olga/main/globalreport/technology_market/total
        //Завод по производству кухонных плит
        final List<TechLvl> techLvls = TechMarketAskParser.getTech(Wizard.host, "olga", "373215");
        assertFalse(techLvls.isEmpty());
    }
}