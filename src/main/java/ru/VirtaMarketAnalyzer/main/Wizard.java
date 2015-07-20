package ru.VirtaMarketAnalyzer.main;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.parser.*;
import ru.VirtaMarketAnalyzer.publish.GitHubPublisher;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Wizard {
    private static final Logger logger = LoggerFactory.getLogger(Wizard.class);
    public static final String host = "http://virtonomica.ru/";
    public static final String industry = "industry";
    public static final String by_trade_at_cities = "by_trade_at_cities";


    public static void main(String[] args) throws IOException, GitAPIException {
        BasicConfigurator.configure();

        final List<String> realms = new ArrayList<>();
        realms.add("olga");
        realms.add("vera");
        realms.add("anna");
        realms.add("mary");
        realms.add("lien");
        for (final String realm : realms) {
            collectToJsonTradeAtCities(realm);
            collectToJsonIndustries(realm);
        }
        //публикуем на сайте
        GitHubPublisher.publish(realms);
    }

    public static void collectToJsonTradeAtCities(final String realm) throws IOException {
        final String baseDir = Utils.getDir() + by_trade_at_cities + File.separator + realm + File.separator;

        final Calendar today = Calendar.getInstance();
        final File baseDirFile = new File(baseDir);
        if (today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && "vera".equalsIgnoreCase(realm)) {
        } else if (today.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && "olga".equalsIgnoreCase(realm)) {
        } else if (today.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY && "anna".equalsIgnoreCase(realm)) {
        } else if (today.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY && "mary".equalsIgnoreCase(realm)) {
        } else if (today.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY && "lien".equalsIgnoreCase(realm)) {
        } else {
            if (baseDirFile.exists()) {
                logger.info("удаляем {}", baseDirFile.getAbsolutePath());
                FileUtils.deleteDirectory(baseDirFile);
            }
            return;
        }
        //страны
        final List<Country> countries = CityInitParser.getCountries(host + realm + "/main/common/main_page/game_info/world/");
        Utils.writeToGson(baseDir + "countries.json", countries);
        //регионы
        final List<Region> regions = CityInitParser.getRegions(host + realm + "/main/geo/regionlist/", countries);
        Utils.writeToGson(baseDir + "regions.json", regions);
        //города и уровень богатства городов
        final List<City> cities = CityListParser.fillWealthIndex(host + realm + "/main/geo/citylist/", regions);
        Utils.writeToGson(baseDir + "cities.json", cities);
        logger.info("cities.size() = {}", cities.size());
        //получаем список доступных розничных товаров
        final List<Product> products = ProductInitParser.getProducts(host + realm + "/main/common/main_page/game_info/trading/");
        Utils.writeToGson(baseDir + "products.json", products);
        logger.info("products.size() = {}", products.size());
        //получаем список доступных розничных категорий товаров
        final List<ProductCategory> product_categories = ProductInitParser.getProductCategories(products);
        Utils.writeToGson(baseDir + "product_categories.json", product_categories);
        //собираем данные продаж товаров в городах
        final Map<String, List<TradeAtCity>> stats = CityParser.collectByTradeAtCities(host + realm + "/main/globalreport/marketing/by_trade_at_cities/", cities, products);
        //сохраняем их в json
        for (final Map.Entry<String, List<TradeAtCity>> entry : stats.entrySet()) {
            Utils.writeToGson(baseDir + "tradeAtCity_" + entry.getKey() + ".json", entry.getValue());
        }
        //запоминаем дату обновления данных
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Utils.writeToGson(baseDir + "updateDate.json", new UpdateDate(df.format(new Date())));
    }

    public static void collectToJsonIndustries(final String realm) throws IOException {
        final String baseDir = Utils.getDir() + industry + File.separator + realm + File.separator;
        //собираем рецепты производства товаров и материалов
        final List<Manufacture> manufactures = ManufactureListParser.getManufactures(host + realm + "/main/common/main_page/game_info/industry/");
        final List<ProductRecipe> recipes = ProductRecipeParser.getRecipes(host + realm + "/main/industry/unit_type/info/", manufactures);
        Utils.writeToGson(baseDir + "manufactures.json", manufactures);
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
        //сохраняем их в json
        for (final Map.Entry<String, List<ProductRecipe>> entry : productRecipes.entrySet()) {
            Utils.writeToGson(baseDir + "recipe_" + entry.getKey() + ".json", entry.getValue());
        }
        //получаем список всех доступных товаров и материалов
        final List<Product> materials = ProductInitParser.getProducts(host + realm + "/main/common/main_page/game_info/products/");
        Utils.writeToGson(baseDir + "materials.json", materials);
        logger.info("materials.size() = {}", materials.size());
        //собираем данные о доступных товарах на оптовом рынке
        final Map<String, List<ProductRemain>> productRemains = ProductRemainParser.getRemains(host + realm + "/main/globalreport/marketing/by_products/", materials);
        //сохраняем их в json
        for (final Map.Entry<String, List<ProductRemain>> entry : productRemains.entrySet()) {
            Utils.writeToGson(baseDir + "product_remains_" + entry.getKey() + ".json", entry.getValue());
        }
        //запоминаем дату обновления данных
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Utils.writeToGson(baseDir + "updateDate.json", new UpdateDate(df.format(new Date())));
    }
}
