package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by cobr123 on 17.02.2019.
 */
public class ServiceGuideParserTest {
    private static final Logger logger = LoggerFactory.getLogger(ServiceGuideParserTest.class);

    @Test
    void genServiceGuideTest() throws Exception {
        final String realm = "olga";
        final UnitType unitType = ServiceInitParser.getServiceUnitTypes(Wizard.host, realm).get(0);
        logger.info("unitType {}", unitType.getCaption());
        final List<ServiceGuide> serviceGuides = ServiceGuideParser.genServiceGuide(Wizard.host, realm, unitType);
        assertFalse(serviceGuides.isEmpty());
        assertTrue(serviceGuides.stream().anyMatch(l -> !l.getServiceGuideProducts().isEmpty()));
        final ServiceGuide serviceGuide = serviceGuides.get(0);
        logger.info(Utils.getPrettyGson(serviceGuide));
        logger.info("https://virtonomica.ru/{}/main/globalreport/marketing?geo={}&unit_type_id={}#by-service", realm, serviceGuide.getGeo(), unitType.getId());
        logger.info(Utils.getPrettyGson(serviceGuides.stream().filter(a -> a.getServiceSpecId().equals("359933")).findFirst().get()));
        logger.info("size = {}", serviceGuides.size());
    }
}
