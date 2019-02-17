package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by cobr123 on 08.02.2019.
 */
public class TradeGuideParserTest {

    @Test
    void genTradeGuideTest() throws Exception {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));
        final String realm = "olga";
        final List<ProductCategory> productCategories = ProductInitParser.getTradeProductCategories(Wizard.host, realm);
        final List<TradeGuide> tradeGuides = TradeGuideParser.genTradeGuide(Wizard.host, realm, productCategories.get(0));
        assertFalse(tradeGuides.isEmpty());
        assertTrue(tradeGuides.stream().anyMatch(l -> !l.getTradeGuideProduct().isEmpty()));
    }
}
