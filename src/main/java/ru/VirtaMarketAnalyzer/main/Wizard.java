package ru.VirtaMarketAnalyzer.main;

import com.google.gson.Gson;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.TradeAtCity;
import ru.VirtaMarketAnalyzer.parser.CityInitParser;
import ru.VirtaMarketAnalyzer.parser.CityListParser;
import ru.VirtaMarketAnalyzer.parser.CityParser;
import ru.VirtaMarketAnalyzer.parser.ProductInitParser;
import ru.VirtaMarketAnalyzer.siteGenerator.GenHtml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Wizard {
    public static void main(String[] args) throws IOException {
        final List<String> realms = new ArrayList<>();
        realms.add("olga");
//        realms.add("vera");
//        realms.add("lien");
//        realms.add("anna");
        for (final String realm : realms) {
            collectToJson(realm);
        }
        //публикуем на сайте
    }

    public static void collectToJson(final String realm) throws IOException {
        final String baseDir = Utils.getDir() + realm + File.separator;
        //получаем список доступных городов
        final List<City> cities = CityInitParser.getCities("http://virtonomica.ru/" + realm + "/main/globalreport/marketing/by_trade_at_cities/");
        //заполняем уровень богатства городов
        CityListParser.fillWealthIndex("http://virtonomica.ru/" + realm + "/main/geo/citylist/", cities);
        //получаем список доступных розничных товаров
        final List<Product> products = ProductInitParser.getProducts("http://virtonomica.ru/" + realm + "/main/common/main_page/game_info/trading/");
        //собираем данные продаж товаров в городах
        final Map<String, List<TradeAtCity>> stats = CityParser.collectByTradeAtCities("http://virtonomica.ru/" + realm + "/main/globalreport/marketing/by_trade_at_cities/", cities, products);
        //сохраняем их в json
        for (final String key : stats.keySet()) {
            final List<TradeAtCity> list = stats.get(key);
            final Gson gson = new Gson();
            Utils.log(baseDir + "tradeAtCity_" + key + ".json");
            Utils.writeFile(baseDir + "tradeAtCity_" + key + ".json", gson.toJson(list));
        }
        //создаем html-страницы
        final String index = GenHtml.createIndexHtml(realm, cities, products);
        Utils.writeFile(baseDir + "index.html", index);
    }
}
