package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Created by cobr123 on 08.02.2019.
 */
public class TradeGuideParserTest {

    @Test
    void collectByTradeAtCitiesTest() throws Exception {
        final String realm = "olga";
        final List<ProductCategory> productCategories = ProductInitParser.getTradeProductCategories(Wizard.host, realm);
        final List<TradeGuide> tradeGuides = TradeGuideParser.genTradeGuide(Wizard.host, realm, productCategories.get(0));
        assertFalse(tradeGuides.isEmpty());
    }
}
