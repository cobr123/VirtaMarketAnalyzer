package ru.VirtaMarketAnalyzer.main;

import com.google.gson.Gson;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.TradeAtCity;
import ru.VirtaMarketAnalyzer.parser.CityInitParser;
import ru.VirtaMarketAnalyzer.parser.CityListParser;
import ru.VirtaMarketAnalyzer.parser.CityParser;
import ru.VirtaMarketAnalyzer.parser.ProductInitParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    }

    public static void collectToJson(final String realm) throws IOException {
        //получаем список доступных городов
        final List<City> cities = CityInitParser.getCities("http://virtonomica.ru/" + realm + "/main/globalreport/marketing/by_trade_at_cities/");
        //заполняем уровень богатства городов
        CityListParser.fillWealthIndex("http://virtonomica.ru/" + realm + "/main/geo/citylist/", cities);
        //получаем список доступных розничных товаров
        final List<Product> products = ProductInitParser.getProducts("http://virtonomica.ru/" + realm + "/main/common/main_page/game_info/trading/");
        //собираем данные продаж товаров в городах
        final List<TradeAtCity> stat = CityParser.collectByTradeAtCities("http://virtonomica.ru/" + realm + "/main/globalreport/marketing/by_trade_at_cities/", cities, products);
        final Gson gson = new Gson();
        Utils.log(Utils.getDir() + realm + File.separator + "tradeAtCity.json");
        Utils.writeFile(Utils.getDir() + realm + File.separator + "tradeAtCity.json", gson.toJson(stat));
        //создаем html-страницы
        //публикуем на сайте
    }
}
