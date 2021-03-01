package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Manufacture;
import ru.VirtaMarketAnalyzer.data.ProductRecipe;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

class ProductRecipeParserTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductRecipeParserTest.class);

    @Test
    void getProductRecipesTest() throws IOException {
        final String host = Wizard.host;
        final String realm = "olga";
        //Лекарственное пчеловодство
        final Manufacture manufacture = ManufactureListParser.getManufacture(host, realm, "423140");
        final List<Manufacture> manufactures = new ArrayList<>();
        manufactures.add(manufacture);
        final Map<String, List<ProductRecipe>> recipes = ProductRecipeParser.getProductRecipes(host, realm, manufactures);
        assertEquals("Лекарственное пчеловодство", recipes.get("423153").get(0).getSpecialization());
        assertEquals(0.3, recipes.get("423153").get(0).getInputProducts().stream().filter(p -> p.getProductID().equals("423139")).collect(toList()).get(0).getQty());
        assertEquals(0.0, recipes.get("423153").get(0).getInputProducts().stream().filter(p -> p.getProductID().equals("423139")).collect(toList()).get(0).getMinQuality());
        // 10 маточного молочка
        assertEquals(10.0, recipes.get("423153").get(0).getResultProducts().stream().filter(p -> p.getProductID().equals("423153")).collect(toList()).get(0).getResultQty());
        assertEquals(50.0, recipes.get("423153").get(0).getResultProducts().stream().filter(p -> p.getProductID().equals("423153")).collect(toList()).get(0).getProdBaseQty());
        //Модификатор качества для маточного молочка 0%
        assertEquals(0.0, recipes.get("423153").get(0).getResultProducts().stream().filter(p -> p.getProductID().equals("423153")).collect(toList()).get(0).getQualityBonusPercent());
        // 15 меда
        assertEquals(15.0, recipes.get("423153").get(0).getResultProducts().stream().filter(p -> p.getProductID().equals("423151")).collect(toList()).get(0).getResultQty());
        assertEquals(75.0, recipes.get("423153").get(0).getResultProducts().stream().filter(p -> p.getProductID().equals("423151")).collect(toList()).get(0).getProdBaseQty());
        //Модификатор качества для меда -20%
        assertEquals(-20.0, recipes.get("423153").get(0).getResultProducts().stream().filter(p -> p.getProductID().equals("423151")).collect(toList()).get(0).getQualityBonusPercent());

        assertEquals(1, recipes.get("423153").size());
        assertEquals(5.0, recipes.get("423153").get(0).getEquipmentPerWorker());
        assertEquals(0.1, recipes.get("423153").get(0).getEnergyConsumption());
        assertEquals("423138", recipes.get("423153").get(0).getEquipment().getId());
        assertEquals(1, recipes.get("423153").get(0).getInputProducts().size());
        assertEquals(2, recipes.get("423153").get(0).getResultProducts().size());
        assertEquals(1, recipes.get("423153").get(0).getResultProducts().stream().filter(p -> p.getProductID().equals("423153")).collect(toList()).size());
        assertEquals(1, recipes.get("423153").get(0).getResultProducts().stream().filter(p -> p.getProductID().equals("423151")).collect(toList()).size());

        assertFalse(Utils.getGson(recipes.get("423153")).isEmpty());
    }

    @Test
    void getGsonTest() throws IOException {
        final String host = Wizard.host;
        final String realm = "olga";
        final List<Manufacture> manufactures = ManufactureListParser.getManufactures(host, realm);
        assertFalse(manufactures.isEmpty());
        final Map<String, List<ProductRecipe>> productRecipes = ProductRecipeParser.getProductRecipes(host, realm, manufactures);
        assertFalse(productRecipes.isEmpty());

        for (final Map.Entry<String, List<ProductRecipe>> entry : productRecipes.entrySet()) {
            try {
                assertFalse(Utils.getGson(entry.getValue()).isEmpty());
            } catch (final Exception e) {
                final Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
                logger.error("entry.getKey = {}\n{}", entry.getKey(), gson.toJson(entry.getValue()));
                throw e;
            }
        }
    }

}