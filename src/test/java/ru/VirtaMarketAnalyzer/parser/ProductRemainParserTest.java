package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductRemain;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProductRemainParserTest {

    @Test
    void getRemainsTest() throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));
        final String realm = "olga";
        final List<Product> materials = ProductInitParser.getManufactureProducts(Wizard.host, realm);
        final Map<String, List<ProductRemain>> productRemains = ProductRemainParser.getRemains(Wizard.host, realm, materials);
        assertFalse(productRemains.isEmpty());
        assertTrue(productRemains.size() <= materials.size());
        assertTrue(productRemains.values().stream().anyMatch(l -> !l.isEmpty()));
    }
}