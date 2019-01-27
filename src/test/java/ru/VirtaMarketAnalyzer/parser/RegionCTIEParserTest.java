package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.Region;
import ru.VirtaMarketAnalyzer.data.RegionCTIE;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegionCTIEParserTest {

    @Test
    void getAllRegionsCTIEListTest() {
        final List<Region> regions = new ArrayList<>();
        regions.add(new Region("2931", "2961", "Far East", 30));
        final Map<String, List<RegionCTIE>> allRegionsCTIEList = RegionCTIEParser.getAllRegionsCTIEList(Wizard.host, "olga", regions);
        assertFalse(allRegionsCTIEList.isEmpty());
        assertFalse(allRegionsCTIEList.get("2961").isEmpty());
    }
}