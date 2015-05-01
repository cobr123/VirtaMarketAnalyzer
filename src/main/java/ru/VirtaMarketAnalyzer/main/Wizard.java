package ru.VirtaMarketAnalyzer.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.parser.CityInitParser;
import ru.VirtaMarketAnalyzer.parser.CityListParser;
import ru.VirtaMarketAnalyzer.parser.CityParser;
import ru.VirtaMarketAnalyzer.parser.ProductInitParser;

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

    public static void main(String[] args) throws IOException {
        final List<String> realms = new ArrayList<>();
        realms.add("lien");
        realms.add("olga");
        realms.add("vera");
        realms.add("anna");
        realms.add("mary");
        for (final String realm : realms) {
            collectToJson(realm);
        }
        //публикуем на сайте
    }

    public static void collectToJson(final String realm) throws IOException {
        final String baseDir = Utils.getDir() + realm + File.separator;
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
}
