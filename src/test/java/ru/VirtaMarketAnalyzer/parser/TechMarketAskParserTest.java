package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.TechLicenseLvl;
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

    @Test
    void getLicenseAskWoBidTest() throws IOException {
        final List<TechLicenseLvl> licenseAskWoBid = TechMarketAskParser.getLicenseAskWoBid(Wizard.host, "olga");
        assertFalse(licenseAskWoBid.isEmpty());
    }
}