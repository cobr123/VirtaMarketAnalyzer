package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductionForRetail;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductionForRetailParserTest {

    @Test
    void genProductionForRetailTest() throws Exception {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));
        final String realm = "olga";
        final List<Product> products = ProductInitParser.getTradingProducts(Wizard.host, realm);
        final List<ProductionForRetail> productionForRetail = ProductionForRetailParser.genProductionForRetail(Wizard.host, realm, products.get(0));
        assertFalse(productionForRetail.isEmpty());
    }
}