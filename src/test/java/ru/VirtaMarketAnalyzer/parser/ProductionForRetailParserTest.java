package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductionForRetail;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductionForRetailParserTest {
    private static final Logger logger = LoggerFactory.getLogger(ProductionForRetailParserTest.class);

    @Test
    void genProductionForRetailTest() throws Exception {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));
        final String realm = "olga";
        final Product product = ProductInitParser.getTradingProduct(Wizard.host, realm, "422897");
        final List<ProductionForRetail> productionsForRetail = ProductionForRetailParser.genProductionForRetail(Wizard.host, realm, product);
        assertFalse(productionsForRetail.isEmpty());
        final ProductionForRetail productionForRetail = productionsForRetail.get(productionsForRetail.size() - 1);
        logger.info(Utils.getPrettyGson(productionForRetail));
        logger.info("https://virtonomica.ru/{}/main/globalreport/marketing?geo={}&product_id={}#by-trade-at-cities", realm, productionForRetail.getGeo(), productionForRetail.getProductID());
        logger.info("size = {}", productionsForRetail.size());
    }
}