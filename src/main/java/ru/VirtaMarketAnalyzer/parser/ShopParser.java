package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.Shop;
import ru.VirtaMarketAnalyzer.data.ShopProduct;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 16.01.16.
 */
public final class ShopParser {
    private static final Logger logger = LoggerFactory.getLogger(ShopParser.class);

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
//        final String url = "http://virtonomica.ru/olga/main/unit/view/5788675";
        final String url = "http://virtonomica.ru/mary/main/unit/view/3943258";
//        Downloader.invalidateCache(url);
        final List<City> cities = new ArrayList<>();
        cities.add(new City("3010", "3023", "7073", "Херсон", 0.0, 0.0, 0.0));
        final List<Product> products = new ArrayList<>();
        products.add(new Product("категория", "/img/products/bourbon.gif", "123", "Бурбон"));
        products.add(new Product("категория", "/img/products/gps.gif", "123", "GPS-навигаторы"));
        System.out.println(Utils.getPrettyGson(parse(url, cities, products)));
    }

    public static Shop parse(final String url, final List<City> cities, final List<Product> products) throws IOException {
        final Document doc = Downloader.getDoc(url);
        final Map<String, List<Product>> productsByImgSrc = products.stream().collect(Collectors.groupingBy(Product::getImgUrl));

        final String countryId = Utils.getLastFromUrl(doc.select("table.infoblock > tbody > tr:nth-child(1) > td:nth-child(2) > a:nth-child(1)").attr("href"));
        final String regionId = Utils.getLastFromUrl(doc.select("table.infoblock > tbody > tr:nth-child(1) > td:nth-child(2) > a:nth-child(2)").attr("href"));
        doc.select("table.infoblock > tbody > tr:nth-child(1) > td:nth-child(2)").first().children().remove();
        final String dyrtyCaption = doc.select("table.infoblock > tbody > tr:nth-child(1) > td:nth-child(2)").text();
        final String dyrtyCaptionReplaced = dyrtyCaption.replaceFirst("\\([^\\)]*\\)$", "").trim()
                .replace("San Diego", "Сан-Диего")
                .replace("Indianapolis", "Индианаполис")
                .replace("San Luis Potosí", "Сан-Луис-Потоси")
                .replace("San Luis Potosi", "Сан-Луис-Потоси")
                .replace("León", "Леон")
                .replace("Filadelfia", "Филадельфия")
                .replace("Tartu", "Тарту")
                .replace("Belfast", "Белфаст")
                .replace("Lisburn", "Лисберн")
                .replace("Leeds", "Лидс")
                .replace("Coventry", "Ковентри")
                .replace("Bamako", "Бамако")
                .replace("Los Angeles", "Лос-Анджелес")
                .replace("Hartford", "Хартфорд")
                .replace("Rotterdam", "Роттердам")
                .replace("Andijon", "Андижан")
                .replace("Almere", "Алмере")
                .replace("Moscow", "Москва")
                .replace("Lipetsk", "Липецк")
                .replace("Perm", "Пермь")
                .replace("Ufa", "Уфа")
                .replace("Omsk", "Омск")
                .replace("Vladivostok", "Владивосток")
                .replace("Nizhni Novgorod", "Нижний Новгород")
                .replace("Харків", "Харьков")
                .replace("Львів", "Львов")
                .replace("Novosibirsk", "Новосибирск")
                .replace("Khabarovsk", "Хабаровск")
                .replace("Hongkong", "Гонконг")
                .replace("Nankin", "Нанкин")
                .replace("Amsterdam", "Амстердам")
                .replace("Beijing", "Пекин")
                .replace("Singapore", "Сингапур")
                .replace("Chengdu", "Чэнду")
                .replace("Shimkent", "Шымкент")
                .replace("Guangzhou", "Гуанчжоу")
                .replace("Shanghai", "Шанхай")
                .replace("Seoul", "Сеул")
                .replace("Hanoi", "Ханой")
                .replace("Bangkok", "Бангкок")
                .replace("Hoshimin", "Хошимин")
                .replace("San Antonio", "Сан-Антонио")
                .replace("Santiago de Cuba", "Сантьяго-де-Куба")
                .replace("Pretoria", "Претория")
                .replace("Aguascalientes", "Агуаскальентес")
                .replace("Phoenix", "Финикс")
                .replace("Vilnius", "Вильнюс")
                .replace("Camaguey", "Камагуэй")
                .replace("Klaipeda", "Клайпеда")
                .replace("Buenos Aires", "Буэнос Айрес")
                .replace("Bergen", "Берген")
                .replace("Ecatepec de Morelos", "Экатепек-де-Морелос")
                .replace("Narva", "Нарва")
                .replace("Madrid", "Мадрид")
                .replace("Liepaja", "Лиепая")
                .replace("Київ", "Киев")
                .replace("San Miguel de Tucuman", "Сан-Мигель-де-Тукуман")
                .replace("Kuala-Lumpur", "Куала-Лумпур")
                .replace("Davao", "Давао")
                .replace("Saltillo", "Сальтильо")
                .replace("Guadalajara", "Гвадалахара")
                .replace("Kuala-Lumpur", "Куала-Лумпур")
                .replace("Surabaya", "Сурабая")
                .replace("Jakarta", "Джакарта")
                .replace("Xi'an", "Сиань")
                .replace("Manila", "Манила")
                .replace("ChiangMai", "Чиенгмай")
                .replace("Jacksonville", "Джексонвиль")
                .replace("Memphis", "Мемфис")
                .replace("Washington", "Вашингтон")
                .replace("Charlotte", "Шарлотт")
                .replace("Bristol", "Бристоль")
                .replace("Guadalupe", "Гуадалупе")
                .replace("New York", "Нью-Йорк")
                .replace("Tijuana", "Тихуана")
                .replace("Saint-Étienne", "Сент-Этьен")
                .replace("Dortmund", "Дортмунд")
                .replace("Montpellier", "Монпелье")
                .replace("Las Palmas de Gran Canaria", "Лас-Пальмас-де-Гран-Канария")
                .replace("Sevilla", "Севилья")
                .replace("Gao", "Гао")
                .replace("Samarqand", "Самарканд")
                .replace("Abu Dhabi", "Абу-Даби")
                .replace("Stuttgart", "Штутгарт")
                .replace("La Plata", "Ла-Плата")
                .replace("Gaziantep", "Газиантеп")
                .replace("Houston", "Хьюстон")
                .replace("Haikou", "Хайкоу")
                .replace("Пинар-дель-Рио", "Pinar del Rio")
                .replace("Осло", "Oslo")
                .replace("Rhodes", "Родос");
        String townId;
        try {
            townId = cities.stream()
                    .filter(c -> c.getCountryId().equals(countryId))
//                    .filter(c -> c.getRegionId().equals(regionId))
                    .filter(c -> c.getCaption().equals(dyrtyCaptionReplaced))
                    .findFirst().get().getId();
        } catch (final Exception e) {
            logger.info(url);
            cities.stream()
                    .filter(c -> c.getCountryId().equals(countryId))
//                    .filter(c -> c.getRegionId().equals(regionId))
                    .forEach(c -> logger.info(".replace(\"{}\", \"{}\")", c.getCaption(), dyrtyCaptionReplaced));
            logger.info("'dyrtyCaption = {}'", dyrtyCaptionReplaced);
            throw e;
        }
        final int shopSize = Utils.toInt(doc.select("table.infoblock > tbody > tr:nth-child(3) > td:nth-child(2)").text());
        final String townDistrict = doc.select("table.infoblock > tbody > tr:nth-child(2) > td:nth-child(2)").text();
        final int departmentCount = Utils.doubleToInt(Utils.toDouble(doc.select("table.infoblock > tbody > tr:nth-child(4) > td:nth-child(2)").text()));
        final double notoriety = Utils.toDouble(doc.select("table.infoblock > tbody > tr:nth-child(5) > td:nth-child(2)").text());
        final String visitorsCount = doc.select("table.infoblock > tbody > tr:nth-child(6) > td:nth-child(2)").text().trim();
        final String serviceLevel = doc.select("table.infoblock > tbody > tr:nth-child(7) > td:nth-child(2)").text();

        final List<ShopProduct> shopProducts = new ArrayList<>();
        final Elements rows = doc.select("table[class=\"grid\"] > tbody > tr[class]");
        for (final Element row : rows) {
            try {
                if ("не изв.".equalsIgnoreCase(row.select("> td:nth-child(3)").first().text())) {
                    continue;
                }
                if (row.select("> td:nth-child(1) > img").first().attr("src").contains("/brand/")) {
                    continue;
                }
                final String productId = productsByImgSrc.get(row.select("> td:eq(0) > img").first().attr("src")).get(0).getId();
                final String sellVolume = row.select("> td").eq(1).text().trim();
                final double quality = Utils.toDouble(row.select("> td").eq(2).text());
                final double brand = Utils.toDouble(row.select("> td").eq(3).text());
                final double price = Utils.toDouble(row.select("> td").eq(4).text());
                final double marketShare = Utils.toDouble(row.select("> td").eq(5).text());

                final ShopProduct shopProduct = new ShopProduct(productId, sellVolume, price, quality, brand, marketShare);
                shopProducts.add(shopProduct);
            } catch (final Exception e) {
                logger.info("url = {}", url);
                logger.info("rows.size() = {}", rows.size());
                logger.error(row.outerHtml());
                logger.error(e.getLocalizedMessage(), e);
            }
        }

        return new Shop(countryId, regionId, townId, shopSize, townDistrict, departmentCount, notoriety,
                visitorsCount, serviceLevel, shopProducts);
    }
}
