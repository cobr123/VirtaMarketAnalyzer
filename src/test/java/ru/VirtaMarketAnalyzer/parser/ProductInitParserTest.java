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
        final List<Product> listProduct = ProductInitParser.getTradingProducts(Wizard.host, realm);
        final List<ProductCategory> listProductCategory = ProductInitParser.getTradeProductCategories(Wizard.host, realm);
        assertFalse(listProduct.isEmpty());
        assertFalse(listProductCategory.isEmpty());
        assertTrue(listProduct.size() > listProductCategory.size());
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