package ru.VirtaMarketAnalyzer.parser;

import org.junit.jupiter.api.Test;
import ru.VirtaMarketAnalyzer.data.Manufacture;
import ru.VirtaMarketAnalyzer.data.ProductRecipe;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

class ProductRecipeParserTest {

    @Test
    void getProductRecipesTest() throws IOException {
        final String host = Wizard.host;
        final String realm = "olga";
        //Лекарственное пчеловодство
        final Manufacture manufacture = ManufactureListParser.getManufacture(host, realm, "423140");
        final List<Manufacture> manufactures = new ArrayList<>();
        manufactures.add(manufacture);
        final Map<String, List<ProductRecipe>> recipes = ProductRecipeParser.getProductRecipes(host, realm, manufactures);
        //Модификатор качества для меда -20%
        assertEquals(0, recipes.get("423151").get(0).getResultProducts().stream().filter(p -> p.getProductID().equals("423151")).collect(toList()).get(0).getQualityBonusPercent());
        assertEquals(0, recipes.get("423151").get(1).getResultProducts().stream().filter(p -> p.getProductID().equals("423151")).collect(toList()).get(0).getQualityBonusPercent());
        assertEquals(-20, recipes.get("423151").get(2).getResultProducts().stream().filter(p -> p.getProductID().equals("423151")).collect(toList()).get(0).getQualityBonusPercent());
        assertEquals(3, recipes.get("423151").size());
        //Модификатор качества для маточного молочка 0%
        assertEquals(0, recipes.get("423153").get(0).getResultProducts().stream().filter(p -> p.getProductID().equals("423153")).collect(toList()).get(0).getQualityBonusPercent());
        assertEquals(1, recipes.get("423153").size());
        assertEquals(1, recipes.get("423153").get(0).getResultProducts().stream().filter(p -> p.getProductID().equals("423153")).collect(toList()).size());
    }
}