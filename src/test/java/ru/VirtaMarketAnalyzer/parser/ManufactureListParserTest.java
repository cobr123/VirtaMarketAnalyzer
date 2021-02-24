package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.Manufacture;
import ru.VirtaMarketAnalyzer.data.ProductRecipe;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ManufactureListParserTest {

    @Test
    void getManufacturesTest() throws IOException {
        final String realm = "olga";
        final List<Manufacture> manufactures = ManufactureListParser.getManufactures(Wizard.host, realm);
        assertFalse(manufactures.isEmpty());
        final Map<String, List<ProductRecipe>> productRecipes = ProductRecipeParser.getProductRecipes(Wizard.host, realm, manufactures);
        assertFalse(productRecipes.isEmpty());
    }
}