package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.ProductHistory;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductHistoryParserTest {

    @Test
    void getHistoryTest() throws IOException {
        final String host = Wizard.host;
        final String realm = "olga";
        final List<Product> products = new ArrayList<>();
        products.add(ProductInitParser.getManufactureProduct(host, realm, "1482"));
        final List<ProductHistory> list = ProductHistoryParser.getHistory(host, realm, products);
        assertFalse(list.isEmpty());
    }
}