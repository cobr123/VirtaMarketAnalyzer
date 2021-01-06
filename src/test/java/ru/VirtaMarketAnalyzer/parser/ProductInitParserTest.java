package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductCategory;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductInitParserTest {
    private static final Logger logger = LoggerFactory.getLogger(ProductInitParserTest.class);

    @Test
    void getTradingProductsTest() throws IOException {
        final String realm = "olga";
        final List<Product> listProduct = ProductInitParser.getTradingProducts(Wizard.host, realm);
        final List<ProductCategory> listProductCategory = ProductInitParser.getTradeProductCategories(Wizard.host, realm);
        assertFalse(listProduct.isEmpty());
        assertFalse(listProductCategory.isEmpty());
        assertTrue(listProduct.size() > listProductCategory.size());
    }

    @Test
    void getTradingProductsCompareTest() throws IOException {
        final List<Product> olgaProducts = ProductInitParser.getTradingProducts(Wizard.host, "olga");
        assertFalse(olgaProducts.isEmpty());
        final List<Product> fastProducts = ProductInitParser.getTradingProducts(Wizard.host, "fast");
        assertFalse(olgaProducts.isEmpty());
        if (olgaProducts.size() <= fastProducts.size()) {
            logger.error("olgaProducts.size({}) <= fastProducts.size({})", olgaProducts.size(), fastProducts.size());
        }
        assertFalse(olgaProducts.size() <= fastProducts.size());

        final List<Product> veraProducts = ProductInitParser.getTradingProducts(Wizard.host, "vera");
        assertFalse(veraProducts.isEmpty());
        if (olgaProducts.size() == veraProducts.size()) {
            logger.error("olgaProducts.size({}) == veraProducts.size({})", olgaProducts.size(), veraProducts.size());
        }
        assertFalse(olgaProducts.size() == veraProducts.size());
        assertFalse(fastProducts.size() == veraProducts.size());
    }

    @Test
    void getManufactureProductsTest() throws IOException {
        final String realm = "olga";
        final List<Product> listProduct = ProductInitParser.getManufactureProducts(Wizard.host, realm);
        final List<ProductCategory> listProductCategory = ProductInitParser.getManufactureProductCategories(Wizard.host, realm);
        assertFalse(listProduct.isEmpty());
        assertFalse(listProductCategory.isEmpty());
        assertTrue(listProduct.size() > listProductCategory.size());
    }

}