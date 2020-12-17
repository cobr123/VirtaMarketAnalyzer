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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ProductionForRetailParserTest {
    private static final Logger logger = LoggerFactory.getLogger(ProductionForRetailParserTest.class);

    @Test
    void genProductionForRetailTest() throws Exception {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));
        final String realm = "olga";
        final Product product = ProductInitParser.getTradingProduct(Wizard.host, realm, "422897");
        final List<ProductionForRetail> productionsForRetail = ProductionForRetailParser.genProductionForRetailByProduct(Wizard.host, realm, product)
                .stream()
                .sorted(Comparator.comparingDouble(ProductionForRetail::getIncomeAfterTax).reversed())
                .collect(Collectors.toList());
        assertFalse(productionsForRetail.isEmpty());
        final ProductionForRetail productionForRetail1 = productionsForRetail.get(0);
        logger.info(Utils.getPrettyGson(productionForRetail1));
        logger.info("https://virtonomica.ru/{}/main/globalreport/marketing?geo={}&product_id={}#by-trade-at-cities", realm, productionForRetail1.getGeo(), productionForRetail1.getProductID());
        final ProductionForRetail productionForRetail2 = productionsForRetail.get(productionsForRetail.size() - 1);
        logger.info(Utils.getPrettyGson(productionForRetail2));
        logger.info("https://virtonomica.ru/{}/main/globalreport/marketing?geo={}&product_id={}#by-trade-at-cities", realm, productionForRetail2.getGeo(), productionForRetail2.getProductID());
        logger.info("size = {}", productionsForRetail.size());
    }
}