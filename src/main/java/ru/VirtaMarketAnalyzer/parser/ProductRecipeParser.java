package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cobr123 on 18.05.2015.
 */
final public class ProductRecipeParser {
    private static final Logger logger = LoggerFactory.getLogger(ProductRecipeParser.class);

    public static void main(final String[] args) throws IOException {
//        final Document doc = Downloader.getDoc(Wizard.host + "olga/main/industry/unit_type/info/15751");
//        final Document doc = Downloader.getDoc(Wizard.host + "olga/main/industry/unit_type/info/422209");
//        final Document doc = Downloader.getDoc(Wizard.host + "olga/main/industry/unit_type/info/2425");
//        final Document doc = Downloader.getDoc(Wizard.host + "olga/main/industry/unit_type/info/2417");
        final String host = Wizard.host;
        final String realm = "olga";
        final String url = host + realm + "/main/industry/unit_type/info/";
        final List<Manufacture> manufactures = new ArrayList<>();
//        manufactures.add(new Manufacture("423140", "manufactureCategory", "caption"));
//        manufactures.add(new Manufacture("2425", "manufactureCategory", "caption"));
        manufactures.add(new Manufacture("2404", "manufactureCategory", "caption", new ArrayList<>()));

        final Map<String, List<ProductRecipe>> result = getProductRecipes(host, realm, manufactures);
        logger.info(Utils.getPrettyGson(result));
    }

    public static Map<String, List<ProductRecipe>> getProductRecipes(final String host, final String realm, final List<Manufacture> manufactures) throws IOException {
        final List<ProductRecipe> recipes = getRecipes(host, realm, manufactures);
        //иногда один продукт можно получить разными способами
        final Map<String, List<ProductRecipe>> productRecipes = new HashMap<>();
        for (final ProductRecipe recipe : recipes) {
            for (final ManufactureResult result : recipe.getResultProducts()) {
                if (!productRecipes.containsKey(result.getProductID())) {
                    productRecipes.put(result.getProductID(), new ArrayList<>());
                }
                productRecipes.get(result.getProductID()).add(recipe);
            }
        }
        return productRecipes;
    }

    public static List<ProductRecipe> getRecipes(final String host, final String realm, final List<Manufacture> manufactures) throws IOException {
        final String lang = (Wizard.host.equals(host) ? "ru" : "en");
        final List<ProductRecipe> recipes = new ArrayList<>();

        for (final Manufacture manufacture : manufactures) {
            final String url = host + "api/" + realm + "/main/unittype/produce?id=" + manufacture.getId() + "&lang=" + lang;

            try {
                final String json = Downloader.getJson(url);
                final Gson gson = new Gson();
                final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
                }.getType();
                final Map<String, Map<String, Object>> mapOfProductRecipes = gson.fromJson(json, mapType);

                for (final Map.Entry<String, Map<String, Object>> entry : mapOfProductRecipes.entrySet()) {
                    final Map<String, Object> productRecipe = entry.getValue();

                    final String specialization = productRecipe.get("name").toString();

                    //количество товаров производимых 1 человеком
                    final int workerQty = manufacture.getSizes().get(0).getWorkplacesCount();
                    if (workerQty > 0) {
                        final int equipQty = manufacture.getSizes().get(0).getMaxEquipment();
                        final double equipmentPerWorker = (double) equipQty / (double) workerQty;

                        Product equipment = null;
                        //если не "склад"
                        if (!"2011".equals(manufacture.getId())) {
                            equipment = getProduct(host, realm, productRecipe.get("equipment_product_id").toString());
                            final double energyConsumption = Double.parseDouble(productRecipe.get("energy_per_equipment").toString()) * (double) equipQty;

                            final Map<String, Map<String, Object>> output = (Map<String, Map<String, Object>>) productRecipe.get("output");
                            if (!output.isEmpty() && !output.containsKey("")) {
                                Map<String, Map<String, Object>> input = new HashMap<>();
                                // проверка для шахт
                                if (productRecipe.get("input") instanceof Map) {
                                    input = (Map<String, Map<String, Object>>) productRecipe.get("input");
                                }
                                final ProductRecipe recipe = new ProductRecipe(
                                        manufacture.getId(),
                                        specialization,
                                        equipment,
                                        equipmentPerWorker,
                                        energyConsumption,
                                        getManufactureIngredient(input),
                                        getManufactureResult(manufacture, output)
                                );
                                recipes.add(recipe);
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                logger.error(url + "&format=debug");
                throw e;
            }
        }
        return recipes;
    }

    private static List<ManufactureIngredient> getManufactureIngredient(final Map<String, Map<String, Object>> input) {
        final List<ManufactureIngredient> inputProducts = new ArrayList<>();

        for (final Map.Entry<String, Map<String, Object>> entry : input.entrySet()) {
            final Map<String, Object> recipeInput = entry.getValue();

            final String productID = recipeInput.get("id").toString();
            final double minQuality = Utils.toDouble(recipeInput.get("quality").toString());
            final double qty = Utils.toDouble(recipeInput.get("qty").toString());

            inputProducts.add(new ManufactureIngredient(productID, Utils.round2(qty), Utils.round2(minQuality)));
        }
        return inputProducts;
    }

    private static List<ManufactureResult> getManufactureResult(final Manufacture manufacture, final Map<String, Map<String, Object>> output) {
        final List<ManufactureResult> resultProducts = new ArrayList<>();

        for (final Map.Entry<String, Map<String, Object>> entry : output.entrySet()) {
            final Map<String, Object> recipeOutput = entry.getValue();

            final String resultID = recipeOutput.get("id").toString();
            final double resultQty = Utils.toDouble(recipeOutput.get("qty").toString());
            final double prodBaseQty = resultQty * (double) manufacture.getSizes().get(0).getMaxEquipment();

            final double quality = Utils.toDouble(recipeOutput.get("quality").toString());
            final double qualityBonus = quality * 100.0 - 100.0;

            final ManufactureResult manufactureResult = new ManufactureResult(resultID, prodBaseQty, resultQty, qualityBonus);
            resultProducts.add(manufactureResult);
        }
        return resultProducts;
    }

    private static Product getProduct(final String host, final String realm, final String equipID) throws IOException {
        return ProductInitParser.getManufactureProduct(host, realm, equipID);
    }

    public static List<Product> getProductFromRecipes(final String host, final String realm, final List<ProductRecipe> productRecipes) throws IOException {
        final Map<String, Product> map = new HashMap<>();
        for (final ProductRecipe productRecipe : productRecipes) {
            for (final ManufactureResult manufactureResult : productRecipe.getResultProducts()) {
                if (!map.containsKey(manufactureResult.getProductID())) {
                    map.put(manufactureResult.getProductID(), ProductInitParser.getManufactureProduct(host, realm, manufactureResult.getProductID()));
                }
            }
            for (final ManufactureIngredient manufactureIngredient : productRecipe.getInputProducts()) {
                if (!map.containsKey(manufactureIngredient.getProductID())) {
                    map.put(manufactureIngredient.getProductID(), ProductInitParser.getManufactureProduct(host, realm, manufactureIngredient.getProductID()));
                }
            }
        }
        return new ArrayList<>(map.values());
    }
}