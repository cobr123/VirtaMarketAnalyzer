package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductCategory;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductInitParserTest {

    @Test
    void getTradingProductsTest() throws IOException {
        final String realm = "olga";
        final List<Product> list = ProductInitParser.getTradingProducts(Wizard.host, realm);
        assertFalse(list.isEmpty());
    }

    @Test
    void getManufactureProductsTest() throws IOException {
        final String realm = "olga";
        final List<Product> list = ProductInitParser.getManufactureProducts(Wizard.host, realm);
        assertFalse(list.isEmpty());
    }

    @Test
    void getTradeProductCategoriesTest() throws IOException {
        final String realm = "olga";
        final List<ProductCategory> list = ProductInitParser.getTradeProductCategories(Wizard.host, realm);
        assertFalse(list.isEmpty());
    }

    @Test
    void getManufactureProductCategoriesTest() throws IOException {
        final String realm = "olga";
        final List<ProductCategory> list = ProductInitParser.getManufactureProductCategories(Wizard.host, realm);
        assertFalse(list.isEmpty());
    }
}