package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
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
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));
        final String realm = "olga";
        final List<UnitType> unitTypes = ServiceInitParser.getServiceUnitTypes(Wizard.host, realm);
        logger.info("unitType {}", unitTypes.get(0).getCaption());
        final List<ServiceGuide> serviceGuides = ServiceGuideParser.genServiceGuide(Wizard.host, realm, unitTypes.get(0));
        assertFalse(serviceGuides.isEmpty());
        assertTrue(serviceGuides.stream().anyMatch(l -> !l.getServiceGuideProducts().isEmpty()));
        final ServiceGuide serviceGuide = serviceGuides.get(0);
        logger.info(Utils.getPrettyGson(serviceGuide));
        logger.info("https://virtonomica.ru/{}/main/globalreport/marketing?geo={}&unit_type_id={}#by-service", realm, serviceGuide.getGeo(), unitTypes.get(0).getId());
        logger.info(Utils.getPrettyGson(serviceGuides.stream().filter(a -> a.getServiceSpecId().equals("359933")).findFirst().get()));
        logger.info("size = {}", serviceGuides.size());
    }
}
