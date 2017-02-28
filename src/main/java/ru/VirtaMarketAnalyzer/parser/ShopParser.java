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
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 16.01.16.
 */
public final class ShopParser {
    private static final Logger logger = LoggerFactory.getLogger(ShopParser.class);
    private static final Set<String> oneTryErrorUrl = new HashSet<>();

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
//        final String url = Wizard.host + "olga/main/unit/view/5788675";
        final String host = Wizard.host;
        final String realm = "mary";
        final String url = host + realm + "/main/unit/view/3943258";
//        Downloader.invalidateCache(url);
        final List<City> cities = new ArrayList<>();
        cities.add(new City("422653", "422655", "422682", "Вашингтон", 0.0, 0.0, 0.0, 0));
        final List<Product> products = new ArrayList<>();
        products.add(ProductInitParser.getTradingProduct(host, realm, "422547"));
        products.add(ProductInitParser.getTradingProduct(host, realm, "3838"));
        final Map<String, List<Product>> productsByImgSrc = products.stream().collect(Collectors.groupingBy(Product::getImgUrl));
        System.out.println(Utils.getPrettyGson(parse(realm, url, cities, productsByImgSrc, "Вашингтон")));
    }

    public static Shop parse(final String realm, final String url, final List<City> cities, final Map<String, List<Product>> productsByImgSrc, final String cityCaption) throws Exception {
        final Document doc = Downloader.getDoc(url);

        final String countryId = Utils.getLastFromUrl(doc.select("table.infoblock > tbody > tr:nth-child(1) > td:nth-child(2) > a:nth-child(1)").attr("href"));
        final String regionId = Utils.getLastFromUrl(doc.select("table.infoblock > tbody > tr:nth-child(1) > td:nth-child(2) > a:nth-child(2)").attr("href"));

        String townId = "";
        if (!cityCaption.isEmpty()) {
            final Optional<City> city = cities.stream()
                    .filter(c -> c.getCountryId().equals(countryId))
                    .filter(c -> c.getCaption().equals(cityCaption))
                    .findFirst();

            if (city.isPresent()) {
                townId = city.get().getId();
            }
        }
        if (townId.isEmpty()) {
            final Element tmpFirst = doc.select("table.infoblock > tbody > tr:nth-child(1) > td:nth-child(2)").first();
            if (tmpFirst != null && tmpFirst.children() != null) {
                tmpFirst.children().remove();
            }
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
                    .replace("Pinar del Rio", "Пинар-дель-Рио")
                    .replace("Oslo", "Осло")
                    .replace("Erevan", "Ереван")
                    .replace("Havana", "Гавана")
                    .replace("Cape Town", "Кейптаун")
                    .replace("Abidjan", "Абиджан")
                    .replace("Abiyán", "Абиджан")
                    .replace("Mexico City", "Мехико")
                    .replace("Bonn", "Бонн")
                    .replace("Colonia", "Кёльн")
                    .replace("Caracas", "Каракас")
                    .replace("Valencia (Ve)", "Валенсия (Ve)")
                    .replace("Groningen", "Гронинген")
                    .replace("Bouake", "Буаке")
                    .replace("Iokogama", "Иокогама")
                    .replace("Inchon", "Инчхон")
                    .replace("Fukuoka", "Фукуока")
                    .replace("Medan", "Медан")
                    .replace("Asahikawa", "Асахикава")
                    .replace("Kano", "Кано")
                    .replace("Riad", "Эр-Рияд")
                    .replace("Cologne", "Кёльн")
                    .replace("Francfort-sur-le-Main", "Франкфурт")
                    .replace("Berlin", "Берлин")
                    .replace("Leipzig", "Лейпциг")
                    .replace("Konya", "Конья")
                    .replace("Stavanger", "Ставангер")
                    .replace("Larissa", "Ларисса")
                    .replace("Istanbul", "Стамбул")
                    .replace("Jeddah", "Джидда")
                    .replace("Dammam", "Даммам")
                    .replace("Medina", "Медина")
                    .replace("Dubai", "Дубай")
                    .replace("Kuwait City", "Эль-Кувейт")
                    .replace("Mecca", "Мекка")
                    .replace("Sapporo", "Саппоро")
                    .replace("Bloemfontein", "Блумфонтейн")
                    .replace("Johannesburg", "Йоханнесбург")
                    .replace("Strasbourg", "Страсбург")
                    .replace("Лієпая", "Лиепая")
                    .replace("Запоріжжя", "Запорожье")
                    .replace("Rhodes", "Родос");
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
                        .forEach(c -> logger.info(".replace(\"{}\", \"{}\")", dyrtyCaptionReplaced, c.getCaption()));
                logger.info("'dyrtyCaption = {}'", dyrtyCaptionReplaced);
                throw e;
            }
        }
        return parse(realm, "", countryId, regionId, townId, url, productsByImgSrc);
    }

    public static Shop parse(final String realm, final String productId
            , final String countryId, final String regionId, final String townId
            , final String url
            , final Map<String, List<Product>> productsByImgSrc
    ) throws Exception {
        Document doc = null;
        if (oneTryErrorUrl.contains(url)) {
            return null;
        }
        try {
            final int maxTriesCnt = 1;
            doc = Downloader.getDoc(url, maxTriesCnt);
        } catch (final Exception e) {
            oneTryErrorUrl.add(url);
            logger.error("url = https://virtonomica.ru/{}/main/globalreport/marketing/by_trade_at_cities/{}/{}/{}/{}", realm, productId, countryId, regionId, townId);
            logger.error(e.getLocalizedMessage());
            return null;
        }
        try {
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
                    final String prodId = productsByImgSrc.get(row.select("> td:eq(0) > img").first().attr("src")).get(0).getId();
                    final String sellVolume = row.select("> td").eq(1).text().trim();
                    final double quality = Utils.toDouble(row.select("> td").eq(2).text());
                    final double brand = Utils.toDouble(row.select("> td").eq(3).text());
                    final double price = Utils.toDouble(row.select("> td").eq(4).text());
                    final double marketShare = Utils.toDouble(row.select("> td").eq(5).text());

                    final ShopProduct shopProduct = new ShopProduct(prodId, sellVolume, price, quality, brand, marketShare);
                    shopProducts.add(shopProduct);
                } catch (final Exception e) {
                    logger.info("url = {}", url);
                    logger.info("rows.size() = {}", rows.size());
                    logger.error(row.outerHtml());
                    logger.error(e.getLocalizedMessage(), e);
                }
            }

            final String unitImage = doc.select("#unitImage > img").attr("src");

            if (unitImage.startsWith("/img/v2/units/fuel_")) {
                //заправки
                int shopSize = Utils.toInt(unitImage.substring("/img/v2/units/fuel_".length(), "/img/v2/units/fuel_".length() + 1));

                final String townDistrict = "";
                final int departmentCount = 1;
                final double notoriety = Utils.toDouble(doc.select("table.infoblock > tbody > tr:nth-child(3) > td:nth-child(2)").text());
                final String visitorsCount = doc.select("table.infoblock > tbody > tr:nth-child(4) > td:nth-child(2)").text().trim();
                final String serviceLevel = doc.select("table.infoblock > tbody > tr:nth-child(5) > td:nth-child(2)").text();

                return new Shop(countryId, regionId, townId, shopSize, townDistrict, departmentCount, notoriety,
                        visitorsCount, serviceLevel, shopProducts);
            } else if (unitImage.startsWith("/img/v2/units/shop_")) {
                //магазины
                final int shopSize = Utils.toInt(doc.select("table.infoblock > tbody > tr:nth-child(3) > td:nth-child(2)").text());
                final String townDistrict = doc.select("table.infoblock > tbody > tr:nth-child(2) > td:nth-child(2)").text();
                final int departmentCount = Utils.doubleToInt(Utils.toDouble(doc.select("table.infoblock > tbody > tr:nth-child(4) > td:nth-child(2)").text()));
                final double notoriety = Utils.toDouble(doc.select("table.infoblock > tbody > tr:nth-child(5) > td:nth-child(2)").text());
                final String visitorsCount = doc.select("table.infoblock > tbody > tr:nth-child(6) > td:nth-child(2)").text().trim();
                final String serviceLevel = doc.select("table.infoblock > tbody > tr:nth-child(7) > td:nth-child(2)").text();

                return new Shop(countryId, regionId, townId, shopSize, townDistrict, departmentCount, notoriety,
                        visitorsCount, serviceLevel, shopProducts);
            } else {
                logger.error("Неизвестный тип юнита, unitImage = {}, url = {}. Возможно еще идет пересчет.", unitImage, url);
                return null;
            }
        } catch (final Exception e) {
            logger.error("url = {}", url);
            throw e;
        }
    }
}
