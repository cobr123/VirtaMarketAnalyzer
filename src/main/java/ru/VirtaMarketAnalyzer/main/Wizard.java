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
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static ru.VirtaMarketAnalyzer.ml.RetailSalePrediction.TRADE_AT_CITY_;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class Wizard {
    private static final Logger logger = LoggerFactory.getLogger(Wizard.class);
    public static final String host = "https://virtonomica.ru/";
    public static final String host_en = "https://virtonomics.com/";
    public static final String industry = "industry";
    public static final String by_trade_at_cities = "by_trade_at_cities";
    public static final String by_service = "by_service";
    public static final String countrydutylist = "countrydutylist";
    public static final String tech = "tech";
    public static final String retail_trends = "retail_trends";


    public static void main(String[] args) throws IOException, GitAPIException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));

        final List<String> realms = new ArrayList<>();
        realms.add("lien");
        realms.add("mary");
        realms.add("anna");
        realms.add("fast");
        realms.add("olga");
        realms.add("vera");
        for (final String realm : realms) {
            collectToJsonTradeAtCities(realm);
            collectToJsonIndustries(realm);
            collectToJsonTech(realm);
        }
//        final File localPathFile = new File(GitHubPublisher.localPath);
//        if (localPathFile.exists()) {
//            FileUtils.deleteDirectory(localPathFile);
//        }
        //публикуем на сайте
        GitHubPublisher.publishRetail(realms);
        //собираем данные со всех реалмов и продуктов
//        RetailSalePrediction.createCommonPrediction();
        //публикуем на сайте
//        GitHubPublisher.publishPredictions();
    }

    private static void collectToJsonTech(final String realm) throws IOException {
        final String baseDir = Utils.getDir() + tech + File.separator + realm + File.separator;

        //типы подразделений для технологий
        final List<TechUnitType> techList = TechListParser.getTechUnitTypes(Wizard.host, realm);
        Utils.writeToGson(baseDir + "unit_types.json", techList);
        final List<TechUnitType> techList_en = TechListParser.getTechUnitTypes(Wizard.host_en, realm);
        Utils.writeToGson(baseDir + "unit_types_en.json", techList_en);
        //спрос на технологии без предложений
        final List<TechLicenseLvl> licenseAskWoBid = TechMarketAskParser.getLicenseAskWoBid(Wizard.host, realm);
        Utils.writeToGson(baseDir + "license_ask_wo_bid.json", licenseAskWoBid);
        //цены на технологии
        final List<TechLvl> techLvls = TechMarketAskParser.getTech(host, realm);
        Utils.writeToGson(baseDir + "technology_market.json", techLvls);
        //запоминаем дату обновления данных
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Utils.writeToGson(baseDir + "updateDate.json", new UpdateDate(df.format(new Date())));
    }

    public static void saveImg(final String imgUrl) {
        try {
            final URL imgFullUrl = new URL(host + imgUrl);
            final File imgFile = new File(Utils.getDir() + imgUrl.replace("/", File.separator));
            FileUtils.copyURLToFile(imgFullUrl, imgFile);
        } catch (final Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public static void saveProductImg(final List<Product> products) {
        products.parallelStream()
                .forEach(product -> {
                    saveImg(product.getImgUrl());
                    saveImg(product.getImgUrl().replace("/img/products/", "/img/products/16/"));
                });
    }

    public static void saveUnitTypeImg(final List<UnitType> unitTypes) {
        unitTypes.parallelStream()
                .forEach(unitType -> {
                    saveImg(unitType.getImgUrl());
                });
    }

    public static void collectToJsonTradeAtCities(final String realm) throws IOException, GitAPIException {
        final String baseDir = Utils.getDir() + by_trade_at_cities + File.separator + realm + File.separator;
        final String serviceBaseDir = Utils.getDir() + by_service + File.separator + realm + File.separator;

        final File baseDirFile = new File(baseDir);
        if (baseDirFile.exists()) {
            logger.info("удаляем {}", baseDirFile.getAbsolutePath());
            FileUtils.deleteDirectory(baseDirFile);
        }
        final File serviceBaseDirFile = new File(serviceBaseDir);
        if (serviceBaseDirFile.exists()) {
            logger.info("удаляем {}", serviceBaseDirFile.getAbsolutePath());
            FileUtils.deleteDirectory(serviceBaseDirFile);
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
        logger.info("cities.size() = {}, realm = {}", cities.size(), realm);

        logger.info("получаем список доступных розничных товаров");
        final List<Product> products = ProductInitParser.getTradingProducts(host, realm);
        Utils.writeToGson(baseDir + "products.json", products);
        final List<Product> products_en = ProductInitParser.getTradingProducts(host_en, realm);
        Utils.writeToGson(baseDir + "products_en.json", products_en);
        logger.info("products.size() = {}, realm = {}", products.size(), realm);
        saveProductImg(products);

        logger.info("получаем список доступных сервисов");
        final List<UnitType> unitTypes = ServiceInitParser.getServiceUnitTypes(host, realm);
        Utils.writeToGson(serviceBaseDir + "service_unit_types.json", unitTypes);
        final List<UnitType> unitTypes_en = ServiceInitParser.getServiceUnitTypes(host_en, realm);
        Utils.writeToGson(serviceBaseDir + "service_unit_types_en.json", unitTypes_en);
        logger.info("service_unit_types.size() = {}, realm = {}", unitTypes.size(), realm);
        saveUnitTypeImg(unitTypes);
        logger.info("собираем данные о стоимости аренды в городах");
        final List<RentAtCity> rents = RentAtCityParser.getUnitTypeRent(Wizard.host, realm, cities);
        Utils.writeToGson(baseDir + "rent.json", rents);

        final Calendar today = Calendar.getInstance();
        if ("olga".equalsIgnoreCase(realm) && (today.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
        } else if ("anna".equalsIgnoreCase(realm) && today.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
        } else if ("mary".equalsIgnoreCase(realm) && today.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
        } else if ("lien".equalsIgnoreCase(realm) && today.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
        } else if ("vera".equalsIgnoreCase(realm) && (today.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY || today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
        } else if ("fast".equalsIgnoreCase(realm)) {
        } else {
            return;
        }

        logger.info("получаем список доступных розничных категорий товаров");
        final List<ProductCategory> product_categories = ProductInitParser.getTradeProductCategories(host, realm);
        Utils.writeToGson(baseDir + "product_categories.json", product_categories);
        final List<ProductCategory> product_categories_en = ProductInitParser.getTradeProductCategories(host_en, realm);
        Utils.writeToGson(baseDir + "product_categories_en.json", product_categories_en);

        logger.info("группируем таможенные пошлины по странам");
        final List<Product> materials = ProductInitParser.getManufactureProducts(host, realm);
        final Map<String, List<CountryDutyList>> countriesDutyList = CountryDutyListParser.getAllCountryDutyList(host + realm + "/main/geo/countrydutylist/", countries, materials);
        for (final Map.Entry<String, List<CountryDutyList>> entry : countriesDutyList.entrySet()) {
            Utils.writeToGson(baseDir + countrydutylist + File.separator + entry.getKey() + ".json", entry.getValue());
        }
        logger.info("собираем данные продаж товаров в городах");
        final Map<String, List<TradeAtCity>> stats = CityParser.collectByTradeAtCities(host, realm, cities, products, countriesDutyList, regions);
        //сохраняем их в json
        for (final Map.Entry<String, List<TradeAtCity>> entry : stats.entrySet()) {
            Utils.writeToGson(baseDir + "tradeAtCity_" + entry.getKey() + ".json", entry.getValue());
        }
        logger.info("запоминаем дату обновления данных");
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Utils.writeToGson(baseDir + "updateDate.json", new UpdateDate(df.format(new Date())));

        logger.info("собираем данные из магазинов");
        final List<Shop> shops = TopRetailParser.getShopList(realm, stats, products);
        logger.info("группируем данные из магазинов по товарам и сохраняем с дополнительной аналитикой");
        final Map<String, List<RetailAnalytics>> retailAnalytics = PrepareAnalitics.getRetailAnalitincsByProducts(shops, stats, products);
        for (final Map.Entry<String, List<RetailAnalytics>> entry : retailAnalytics.entrySet()) {
            Utils.writeToGsonZip(baseDir + RetailSalePrediction.RETAIL_ANALYTICS_ + entry.getKey() + ".json", entry.getValue());
        }
        logger.info("группируем данные о сервисах по городам");
        for (final UnitType ut : unitTypes) {
            final List<ServiceAtCity> serviceAtCity = ServiceAtCityParser.get(host, realm, cities, ut, regions, rents);
            Utils.writeToGson(serviceBaseDir + "serviceAtCity_" + ut.getId() + ".json", serviceAtCity);
        }
        for (final UnitType ut : unitTypes_en) {
            final List<ServiceAtCity> serviceAtCity_en = ServiceAtCityParser.get(host_en, realm, cities_en, ut, regions_en, rents);
            Utils.writeToGson(serviceBaseDir + "serviceAtCity_" + ut.getId() + "_en.json", serviceAtCity_en);
        }
        logger.info("запоминаем дату обновления данных");
        Utils.writeToGson(serviceBaseDir + "updateDate.json", new UpdateDate(df.format(new Date())));

//        ищем формулу для объема продаж в рознице
//        RetailSalePrediction.createPrediction(realm, retailAnalytics, products);
        logger.info("обновляем тренды");
        updateAllRetailTrends(realm);
    }

    public static void updateAllRetailTrends(final String realm) throws IOException, GitAPIException {
        final String baseDir = Utils.getDir() + by_trade_at_cities + File.separator + realm + File.separator;
        final Set<TradeAtCity> set = RetailSalePrediction.getAllTradeAtCity(TRADE_AT_CITY_, realm);
        logger.info("updateAllRetailAnalytics.size() = {}", set.size());

        //группируем аналитику по товарам и сохраняем
        final Map<String, List<TradeAtCity>> tradeAtCityByProduct = set.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(TradeAtCity::getProductId));

        for (final Map.Entry<String, List<TradeAtCity>> entry : tradeAtCityByProduct.entrySet()) {
            final String fileNamePath = baseDir + retail_trends + File.separator + entry.getKey() + ".json";
            Utils.writeToGsonZip(fileNamePath, getRetailTrends(entry.getValue()));
        }
    }

    private static List<RetailTrend> getRetailTrends(final List<TradeAtCity> list) {
        return list.stream()
                .collect(Collectors.groupingBy((tac) -> RetailTrend.dateFormat.format(tac.getDate())))
                .entrySet().stream()
                .map(e -> e.getValue().stream()
                        .map(RetailTrend::new)
                        .reduce((f1, f2) -> new RetailTrend(
                                (f1.getLocalPrice() + f2.getLocalPrice()) / 2.0,
                                (f1.getLocalQuality() + f2.getLocalQuality()) / 2.0,
                                (f1.getShopPrice() + f2.getShopPrice()) / 2.0,
                                (f1.getShopQuality() + f2.getShopQuality()) / 2.0,
                                f1.getDate(),
                                f1.getVolume() + f2.getVolume(),
                                (f1.getLocalMarketVolumeSum() + f2.getLocalMarketVolumeSum()) / 2.0,
                                (f1.getShopMarketVolumeSum() + f2.getShopMarketVolumeSum()) / 2.0,
                                (f1.getLocalMarketVolumeSumTotal() + f2.getLocalMarketVolumeSumTotal()) / 2.0,
                                (f1.getShopMarketVolumeSumTotal() + f2.getShopMarketVolumeSumTotal()) / 2.0,
                                (f1.getPercentMarketVolumeSum() + f2.getPercentMarketVolumeSum()) / 2.0,
                                (f1.getPercentMarketVolumeSumTotal() + f2.getPercentMarketVolumeSumTotal()) / 2.0
                        )))
                .map(Optional::get)
                .sorted(Comparator.comparing(RetailTrend::getDate))
                .collect(Collectors.toList());
    }

    public static void collectToJsonIndustries(final String realm) throws IOException {
        final String baseDir = Utils.getDir() + industry + File.separator + realm + File.separator;

        logger.info("собираем рецепты производства товаров и материалов");
        final List<Manufacture> manufactures = ManufactureListParser.getManufactures(host, realm);
        Utils.writeToGson(baseDir + "manufactures.json", manufactures);
        final Map<String, List<ProductRecipe>> productRecipes = ProductRecipeParser.getProductRecipes(host, realm, manufactures);
        //сохраняем их в json
        for (final Map.Entry<String, List<ProductRecipe>> entry : productRecipes.entrySet()) {
            Utils.writeToGson(baseDir + "recipe_" + entry.getKey() + ".json", entry.getValue());
        }
        final List<Manufacture> manufactures_en = ManufactureListParser.getManufactures(host_en, realm);
        Utils.writeToGson(baseDir + "manufactures_en.json", manufactures_en);
        final Map<String, List<ProductRecipe>> productRecipes_en = ProductRecipeParser.getProductRecipes(host_en, realm, manufactures_en);
        //сохраняем их в json
        for (final Map.Entry<String, List<ProductRecipe>> entry : productRecipes_en.entrySet()) {
            Utils.writeToGson(baseDir + "recipe_" + entry.getKey() + "_en.json", entry.getValue());
        }
        logger.info("получаем список всех доступных товаров и материалов");
        final List<Product> materials = ProductInitParser.getManufactureProducts(host, realm);
        Utils.writeToGson(baseDir + "materials.json", materials);
        final List<Product> materials_en = ProductInitParser.getManufactureProducts(host_en, realm);
        Utils.writeToGson(baseDir + "materials_en.json", materials_en);
        logger.info("materials.size() = {}, realm = {}", materials.size(), realm);
        saveProductImg(materials);
        logger.info("materials img saved");
        //страны
        final List<Country> countries = CityInitParser.getCountries(host + realm + "/main/common/main_page/game_info/world/");
        //регионы
        final List<Region> regions = CityInitParser.getRegions(host + realm + "/main/geo/regionlist/", countries);
        logger.info("группируем ставки енвд по регионам");
        final Map<String, List<RegionCTIE>> allRegionsCTIEList = RegionCTIEParser.getAllRegionsCTIEList(host + realm + "/main/geo/regionENVD/", regions, materials);
        for (final Map.Entry<String, List<RegionCTIE>> entry : allRegionsCTIEList.entrySet()) {
            Utils.writeToGson(baseDir + "region_ctie" + File.separator + entry.getKey() + ".json", entry.getValue());
        }
        logger.info("собираем данные о доступных товарах на оптовом рынке");
        final Map<String, List<ProductRemain>> productRemains = ProductRemainParser.getRemains(host + realm + "/main/globalreport/marketing/by_products/", materials);
        //сохраняем их в json
        for (final Map.Entry<String, List<ProductRemain>> entry : productRemains.entrySet()) {
            Utils.writeToGson(baseDir + "product_remains_" + entry.getKey() + ".json", entry.getValue());
        }
        logger.info("собираем данные о среднем качестве товаров");
        final List<ProductHistory> productHistory = ProductHistoryParser.getHistory(host + realm + "/main/globalreport/product_history/", materials);
        Utils.writeToGson(baseDir + "product_history.json", productHistory);
        logger.info("собираем товары которые можно произвести с качеством выше среднего");
        final List<ProductionAboveAverage> productionAboveAverage = ProductionAboveAverageParser.calc(host, realm, productHistory, productRemains, productRecipes, manufactures);
        final List<ProductionAboveAverage> productionAboveAverage_en = ProductionAboveAverageParser.calc(host, realm, productHistory, productRemains, productRecipes_en, manufactures);
        logger.info("productionAboveAverage.size = {}", productionAboveAverage.size());
        Utils.writeToGsonZip(baseDir + "production_above_average.json", productionAboveAverage);
        Utils.writeToGsonZip(baseDir + "production_above_average_en.json", productionAboveAverage_en);
        logger.info("запоминаем дату обновления данных");
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Utils.writeToGson(baseDir + "updateDate.json", new UpdateDate(df.format(new Date())));
    }
}