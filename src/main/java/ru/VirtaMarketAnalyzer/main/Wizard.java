package ru.VirtaMarketAnalyzer.main;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.ml.PrepareAnalitics;
import ru.VirtaMarketAnalyzer.ml.RetailSalePrediction;
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
    public static final String host_en = "http://virtonomica.com/";
    public static final String industry = "industry";
    public static final String by_trade_at_cities = "by_trade_at_cities";
    public static final String by_service = "by_service";


    public static void main(String[] args) throws IOException, GitAPIException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));

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
        final File localPathFile = new File(GitHubPublisher.localPath);
        if (localPathFile.exists()) {
            FileUtils.deleteDirectory(localPathFile);
        }
        //публикуем на сайте
        GitHubPublisher.publishRetail(realms);
        //собираем данные со всех реалмов и продуктов
        RetailSalePrediction.createCommonPrediction();
        //публикуем на сайте
        GitHubPublisher.publishPredictions();
    }

    public static void collectToJsonTradeAtCities(final String realm) throws IOException {
        final String baseDir = Utils.getDir() + by_trade_at_cities + File.separator + realm + File.separator;
        final String serviceBaseDir = Utils.getDir() + by_service + File.separator + realm + File.separator;

        final Calendar today = Calendar.getInstance();
        final File baseDirFile = new File(baseDir);
        if ("olga".equalsIgnoreCase(realm) && (today.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
        } else if ("anna".equalsIgnoreCase(realm) && today.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
        } else if ("mary".equalsIgnoreCase(realm) && today.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
        } else if ("lien".equalsIgnoreCase(realm) && today.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
        } else if ("vera".equalsIgnoreCase(realm) && (today.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
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
        final List<Country> countries_en = CityInitParser.getCountries(host_en + realm + "/main/common/main_page/game_info/world/");
        Utils.writeToGson(baseDir + "countries_en.json", countries_en);
        //регионы
        final List<Region> regions = CityInitParser.getRegions(host + realm + "/main/geo/regionlist/", countries);
        Utils.writeToGson(baseDir + "regions.json", regions);
        final List<Region> regions_en = CityInitParser.getRegions(host_en + realm + "/main/geo/regionlist/", countries);
        Utils.writeToGson(baseDir + "regions_en.json", regions_en);
        //города и уровень богатства городов
        final List<City> cities = CityListParser.fillWealthIndex(host + realm + "/main/geo/citylist/", regions);
        Utils.writeToGson(baseDir + "cities.json", cities);
        final List<City> cities_en = CityListParser.fillWealthIndex(host_en + realm + "/main/geo/citylist/", regions);
        Utils.writeToGson(baseDir + "cities_en.json", cities_en);
        logger.info("cities.size() = {}", cities.size());
        //получаем список доступных розничных товаров
        final List<Product> products = ProductInitParser.getProducts(host + realm + "/main/common/main_page/game_info/trading/");
        Utils.writeToGson(baseDir + "products.json", products);
        final List<Product> products_en = ProductInitParser.getProducts(host_en + realm + "/main/common/main_page/game_info/trading/");
        Utils.writeToGson(baseDir + "products_en.json", products_en);
        logger.info("products.size() = {}", products.size());
        //получаем список доступных розничных категорий товаров
        final List<ProductCategory> product_categories = ProductInitParser.getProductCategories(products);
        Utils.writeToGson(baseDir + "product_categories.json", product_categories);
        final List<ProductCategory> product_categories_en = ProductInitParser.getProductCategories(products_en);
        Utils.writeToGson(baseDir + "product_categories_en.json", product_categories_en);
        //собираем данные продаж товаров в городах
        final Map<String, List<TradeAtCity>> stats = CityParser.collectByTradeAtCities(host + realm + "/main/globalreport/marketing/by_trade_at_cities/", cities, products);
        //сохраняем их в json
        for (final Map.Entry<String, List<TradeAtCity>> entry : stats.entrySet()) {
            Utils.writeToGson(baseDir + "tradeAtCity_" + entry.getKey() + ".json", entry.getValue());
        }
        //запоминаем дату обновления данных
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Utils.writeToGson(baseDir + "updateDate.json", new UpdateDate(df.format(new Date())));
        //собираем данные из магазинов
        final List<Shop> shops = TopRetailParser.getShopList(host, realm, cities, products);
        //группируем данные из магазинов по товарам и сохраняем с дополнительной аналитикой
        final Map<String, List<RetailAnalytics>> retailAnalytics = PrepareAnalitics.getRetailAnalitincsByProducts(shops, stats, products);
        for (final Map.Entry<String, List<RetailAnalytics>> entry : retailAnalytics.entrySet()) {
            Utils.writeToGson(baseDir + RetailSalePrediction.RETAIL_ANALYTICS_ + entry.getKey() + ".json", entry.getValue());
        }
        //сервисы
        final List<UnitType> unitTypes = ServiceInitParser.getServiceUnitTypes(host, realm);
        Utils.writeToGson(serviceBaseDir + "service_unit_types.json", unitTypes);
        final List<UnitType> unitTypes_en = ServiceInitParser.getServiceUnitTypes(host_en, realm);
        Utils.writeToGson(serviceBaseDir + "service_unit_types_en.json", unitTypes_en);
        //данные о сервисах по городам
        for (final UnitType ut : unitTypes) {
            final List<ServiceAtCity> serviceAtCity = ServiceAtCityParser.get(host, realm, cities, ut.getId());
            Utils.writeToGson(serviceBaseDir + "serviceAtCity_" + ut.getId() + ".json", serviceAtCity);
        }

        //ищем формулу для объема продаж в рознице
//        RetailSalePrediction.createPrediction(realm, retailAnalytics, products);
    }

    public static void collectToJsonIndustries(final String realm) throws IOException {
        final String baseDir = Utils.getDir() + industry + File.separator + realm + File.separator;
        //собираем рецепты производства товаров и материалов
        final List<Manufacture> manufactures = ManufactureListParser.getManufactures(host + realm + "/main/common/main_page/game_info/industry/");
        Utils.writeToGson(baseDir + "manufactures.json", manufactures);
        final Map<String, List<ProductRecipe>> productRecipes = ProductRecipeParser.getProductRecipes(host + realm + "/main/industry/unit_type/info/", manufactures);
        //сохраняем их в json
        for (final Map.Entry<String, List<ProductRecipe>> entry : productRecipes.entrySet()) {
            Utils.writeToGson(baseDir + "recipe_" + entry.getKey() + ".json", entry.getValue());
        }
        final List<Manufacture> manufactures_en = ManufactureListParser.getManufactures(host_en + realm + "/main/common/main_page/game_info/industry/");
        Utils.writeToGson(baseDir + "manufactures_en.json", manufactures_en);
        final Map<String, List<ProductRecipe>> productRecipes_en = ProductRecipeParser.getProductRecipes(host_en + realm + "/main/industry/unit_type/info/", manufactures_en);
        //сохраняем их в json
        for (final Map.Entry<String, List<ProductRecipe>> entry : productRecipes_en.entrySet()) {
            Utils.writeToGson(baseDir + "recipe_" + entry.getKey() + "_en.json", entry.getValue());
        }
        //получаем список всех доступных товаров и материалов
        final List<Product> materials = ProductInitParser.getProducts(host + realm + "/main/common/main_page/game_info/products/");
        Utils.writeToGson(baseDir + "materials.json", materials);
        final List<Product> materials_en = ProductInitParser.getProducts(host_en + realm + "/main/common/main_page/game_info/products/");
        Utils.writeToGson(baseDir + "materials_en.json", materials_en);
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