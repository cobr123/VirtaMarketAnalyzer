package ru.VirtaMarketAnalyzer.main;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.parser.*;
import ru.VirtaMarketAnalyzer.publish.GitHubPublisher;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


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
    public static final String product_remains_trends = "product_remains_trends";
    public static final String trade_guide = "trade_guide";
    public static final String service_guide = "service_guide";
    //public static final String retail_production_guide = "retail_production_guide";
    public static final String by_product_category_id = "by_product_category_id";
    public static final String by_service_id = "by_service_id";
    public static final String CITY_ELECTRICITY_TARIFF = "city_electricity_tariff";
    public static final List<String> realms = Arrays.asList("nika", "mary", "anna", "fast", "olga", "vera", "lien");


    public static void main(String[] args) throws Exception {
        checkLoginRequired();
        final List<String> parsedRealms = new ArrayList<>();
        for (final String realm : Wizard.realms) {
            try {
                if (isParseNeedToday(realm)) {
                    collectToJsonTradeAtCities(realm);
                    collectToJsonIndustries(realm);
                    collectToJsonTech(realm);
                    parsedRealms.add(realm);
                }
            } catch (final Exception e) {
                //видимо на этом реалме пересчета в этот день не было
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        //публикуем на сайте
        GitHubPublisher.publishRetail(parsedRealms);
    }

    private static void checkLoginRequired() throws Exception {
        final List<Product> olgaProducts = ProductInitParser.getTradingProducts(Wizard.host, "olga");
        final List<Product> fastProducts = ProductInitParser.getTradingProducts(Wizard.host, "fast");
        if (olgaProducts.size() <= fastProducts.size()) {
            logger.error("login required! olgaProducts.size({}) <= fastProducts.size({})", olgaProducts.size(), fastProducts.size());
            throw new Exception("login required!");
        }
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
        final List<TechLvl> techLvls = TechMarketAskParser.getTech(host, realm, techList);
        Utils.writeToGson(baseDir + "technology_market.json", techLvls);
        //запоминаем дату обновления данных
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Utils.writeToGson(baseDir + "updateDate.json", new UpdateDate(df.format(new Date())));
    }

    public static void saveImg(final String imgUrl) {
        saveImg(imgUrl, imgUrl);
    }

    public static void saveImg(final String imgUrl, final String imgPath) {
        try {
            final File imgFile = new File(Utils.getDir() + imgPath.replace("/", File.separator));
            if (!imgFile.exists()) {
                final URL imgFullUrl = new URL(host + imgUrl);
                FileUtils.copyURLToFile(imgFullUrl, imgFile);
            }
        } catch (final Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public static void saveProductImg(final List<Product> products) {
        products.parallelStream()
                .forEach(product -> {
                    saveImg(product.getImgUrl().replace("/img/products/", "/i/app/virtonomica/product/svg/"), product.getImgUrl());
                });
    }

    public static void saveUnitTypeImg(final List<UnitType> unitTypes) {
        unitTypes.parallelStream()
                .forEach(unitType -> saveImg(unitType.getImgUrl()));
    }

    private static boolean todayIs(final int dayOfWeek) {
        final Calendar today = Calendar.getInstance();
        return today.get(Calendar.DAY_OF_WEEK) == dayOfWeek;
    }

    public static boolean isParseNeedToday(final String realm) {
        if ("mary".equalsIgnoreCase(realm) && todayIs(Calendar.MONDAY)) {
        } else if ("anna".equalsIgnoreCase(realm) && todayIs(Calendar.TUESDAY)) {
        } else if ("olga".equalsIgnoreCase(realm) && todayIs(Calendar.WEDNESDAY)) {
        } else if ("vera".equalsIgnoreCase(realm) && todayIs(Calendar.THURSDAY)) {
        } else if ("lien".equalsIgnoreCase(realm) && todayIs(Calendar.FRIDAY)) {
        } else if ("nika".equalsIgnoreCase(realm) && todayIs(Calendar.FRIDAY)) {
        } else if ("olga".equalsIgnoreCase(realm) && todayIs(Calendar.SUNDAY)) {
        } else if ("fast".equalsIgnoreCase(realm)) {
        } else {
            return false;
        }
        return true;
    }

    public static void collectToJsonTradeAtCities(final String realm) throws Exception {
        final String baseDir = Utils.getDir() + by_trade_at_cities + File.separator + realm + File.separator;
        final String serviceBaseDir = Utils.getDir() + by_service + File.separator + realm + File.separator;
        final String tradeGuideBaseDir = Utils.getDir() + trade_guide + File.separator + realm + File.separator;
        final String serviceGuideBaseDir = Utils.getDir() + service_guide + File.separator + realm + File.separator;
        //final String retailProductionGuideBaseDir = Utils.getDir() + retail_production_guide + File.separator + realm + File.separator;

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
        final List<Country> countries = CityInitParser.getCountries(host, realm);
        Utils.writeToGson(baseDir + "countries.json", countries);
        final List<Country> countries_en = CityInitParser.getCountries(host_en, realm);
        Utils.writeToGson(baseDir + "countries_en.json", countries_en);
        logger.info("countries.size() = {}, realm = {}", countries.size(), realm);
        //регионы
        final List<Region> regions = CityInitParser.getRegions(host, realm);
        Utils.writeToGson(baseDir + "regions.json", regions);
        final List<Region> regions_en = CityInitParser.getRegions(host_en, realm);
        Utils.writeToGson(baseDir + "regions_en.json", regions_en);
        logger.info("regions.size() = {}, realm = {}", regions.size(), realm);
        //города и уровень богатства городов
        final List<City> cities = Utils.repeatOnErr(() -> CityListParser.getCities(host, realm));
        Utils.writeToGson(baseDir + "cities.json", cities);
        final List<City> cities_en = CityListParser.getCities(host_en, realm);
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
        logger.info("service_unit_types.size() = {}, realm = {}", unitTypes.size(), realm);
        final List<UnitType> unitTypes_en = ServiceInitParser.getServiceUnitTypes(host_en, realm);
        Utils.writeToGson(serviceBaseDir + "service_unit_types_en.json", unitTypes_en);
        logger.info("service_unit_types_en.size() = {}, realm = {}", unitTypes_en.size(), realm);
        saveUnitTypeImg(unitTypes);
        logger.info("собираем данные о стоимости аренды в городах");
        final List<RentAtCity> rents = RentAtCityParser.getUnitTypeRent(Wizard.host, realm, cities);
        Utils.writeToGson(baseDir + "rent.json", rents);
        logger.info("rents.size() = {}, realm = {}", rents.size(), realm);

        logger.info("получаем список доступных розничных категорий товаров");
        final List<ProductCategory> product_categories = ProductInitParser.getTradeProductCategories(host, realm);
        Utils.writeToGson(baseDir + "product_categories.json", product_categories);
        final List<ProductCategory> product_categories_en = ProductInitParser.getTradeProductCategories(host_en, realm);
        Utils.writeToGson(baseDir + "product_categories_en.json", product_categories_en);
        logger.info("product_categories.size() = {}, realm = {}", product_categories.size(), realm);

        logger.info("группируем таможенные пошлины по странам");
        final Map<String, List<CountryDutyList>> countriesDutyList = CountryDutyListParser.getAllCountryDutyList(host, realm, countries);
        for (final Map.Entry<String, List<CountryDutyList>> entry : countriesDutyList.entrySet()) {
            Utils.writeToGson(baseDir + countrydutylist + File.separator + entry.getKey() + ".json", entry.getValue());
        }
        logger.info("идеальное время выполнения (все города для розницы одного продукта): {} секунд, итого: {}", cities.size() / Downloader.permitsPerSecond, DurationFormatUtils.formatDurationHMS(Math.round(cities.size() / Downloader.permitsPerSecond * products.size() * 1000.0)));
        for (int i = 0; i < products.size(); i++) {
            final StopWatch watch = new StopWatch();
            watch.start();
            final Product product = products.get(i);
            logger.info("{} / {} собираем данные продаж товаров в городах, {}", i + 1, products.size(), product.getCaption());
            logger.info("{}{}/main/globalreport/marketing?product_id={}#by-trade-at-cities", host, realm, product.getId());
            final List<TradeAtCity> stats = CityParser.collectByTradeAtCities(host, realm, cities, product);
            Utils.writeToGson(baseDir + "tradeAtCity_" + product.getId() + ".json", stats);
            watch.stop();
            logger.info("время выполнения: {}", watch.toString());
//            logger.info("собираем данные из магазинов");
//            final List<Shop> shops = TopRetailParser.getShopList(host, realm, stats, product);
//            logger.info("shops.size() = {}", shops.size());
//            logger.info("группируем данные из магазинов по товарам и сохраняем с дополнительной аналитикой");
//            final List<RetailAnalytics> retailAnalytics = PrepareAnalitics.getRetailAnalitincsByProducts(shops, stats, product, cities);
//            logger.info("retailAnalytics.size() = {}", retailAnalytics.size());
//            Utils.writeToGsonZip(baseDir + RetailSalePrediction.RETAIL_ANALYTICS_ + product.getId() + ".json", retailAnalytics);
        }
        logger.info("группируем данные о сервисах по городам");
        for (final UnitType ut : unitTypes) {
            final List<ServiceAtCity> serviceAtCity = ServiceAtCityParser.get(host, realm, cities, ut, regions, rents);
            Utils.writeToGson(serviceBaseDir + "serviceAtCity_" + ut.getId() + ".json", serviceAtCity);
            logger.info("{}{}/main/globalreport/marketing?unit_type_id={}#by-service", host, realm, ut.getId());
        }
        for (final UnitType ut : unitTypes_en) {
            final List<ServiceAtCity> serviceAtCity_en = ServiceAtCityParser.get(host_en, realm, cities_en, ut, regions_en, rents);
            Utils.writeToGson(serviceBaseDir + "serviceAtCity_" + ut.getId() + "_en.json", serviceAtCity_en);
            logger.info("{}{}/main/globalreport/marketing?unit_type_id={}#by-service", host_en, realm, ut.getId());
        }
        logger.info("генерируем торговый гид, {}", realm);
        final List<ProductCategory> productCategories = ProductInitParser.getTradeProductCategories(host, realm);
        for (int i = 0; i < productCategories.size(); i++) {
            final ProductCategory productCategory = productCategories.get(i);
            logger.info("{} / {}, {}", i + 1, productCategories.size(), productCategory.getCaption());
            final List<TradeGuide> tradeGuides = TradeGuideParser.genTradeGuide(host, realm, productCategory);
            Utils.writeToGsonZip(tradeGuideBaseDir + by_product_category_id + File.separator + productCategory.getId() + ".json", tradeGuides);
        }
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        if (!"fast".equalsIgnoreCase(realm)) {
            logger.info("генерируем гид по сервисам, {}", realm);
            for (int i = 0; i < unitTypes.size(); i++) {
                final UnitType unitType = unitTypes.get(i);
                logger.info("{} / {}, {}", i + 1, unitTypes.size(), unitType.getCaption());
                final List<ServiceGuide> serviceGuides = ServiceGuideParser.genServiceGuide(host, realm, unitType);
                Utils.writeToGsonZip(serviceGuideBaseDir + by_service_id + File.separator + unitType.getId() + ".json", serviceGuides);
            }
            Utils.writeToGson(serviceGuideBaseDir + "updateDate.json", new UpdateDate(df.format(new Date())));
        }
//        logger.info("генерируем производственный гид для розницы, {}", realm);
//        for (int i = 0; i < productCategories.size(); i++) {
//            final ProductCategory productCategory = productCategories.get(i);
//            logger.info("{} / {}, {}", i + 1, productCategories.size(), productCategory.getCaption());
//            final List<ProductionForRetail> retailProductionGuides = ProductionForRetailParser.genProductionForRetailByProductCategory(host, realm, productCategory);
//            Utils.writeToGsonZip(retailProductionGuideBaseDir + by_product_category_id + File.separator + productCategory.getId() + ".json", retailProductionGuides);
//        }

//        ищем формулу для объема продаж в рознице
//        RetailSalePrediction.createPrediction(realm, retailAnalytics, products);
        logger.info("запоминаем дату обновления данных");
        Utils.writeToGson(baseDir + "updateDate.json", new UpdateDate(df.format(new Date())));
        Utils.writeToGson(serviceBaseDir + "updateDate.json", new UpdateDate(df.format(new Date())));
        Utils.writeToGson(tradeGuideBaseDir + "updateDate.json", new UpdateDate(df.format(new Date())));
//        Utils.writeToGson(retailProductionGuideBaseDir + "updateDate.json", new UpdateDate(df.format(new Date())));
    }


    public static void collectToJsonIndustries(final String realm) throws Exception {
        final String baseDir = Utils.getDir() + industry + File.separator + realm + File.separator;

        logger.info("собираем рецепты производства товаров и материалов");
        final List<Manufacture> manufactures = ManufactureListParser.getManufactures(host, realm);
        Utils.writeToGson(baseDir + "manufactures.json", manufactures);
        logger.info("manufactures.size() = {}, realm = {}", manufactures.size(), realm);
        final Map<String, List<ProductRecipe>> productRecipes = ProductRecipeParser.getProductRecipes(host, realm, manufactures);
        logger.info("productRecipes.size() = {}, realm = {}", productRecipes.size(), realm);
        //сохраняем их в json
        for (final Map.Entry<String, List<ProductRecipe>> entry : productRecipes.entrySet()) {
            Utils.writeToGson(baseDir + "recipe_" + entry.getKey() + ".json", entry.getValue());
        }
        final List<Manufacture> manufactures_en = ManufactureListParser.getManufactures(host_en, realm);
        Utils.writeToGson(baseDir + "manufactures_en.json", manufactures_en);
        logger.info("manufactures_en.size() = {}, realm = {}", manufactures_en.size(), realm);
        final Map<String, List<ProductRecipe>> productRecipes_en = ProductRecipeParser.getProductRecipes(host_en, realm, manufactures_en);
        logger.info("productRecipes_en.size() = {}, realm = {}", productRecipes_en.size(), realm);
        //сохраняем их в json
        for (final Map.Entry<String, List<ProductRecipe>> entry : productRecipes_en.entrySet()) {
            Utils.writeToGson(baseDir + "recipe_" + entry.getKey() + "_en.json", entry.getValue());
        }
        logger.info("получаем список всех доступных товаров и материалов");
        final List<Product> materials = ProductInitParser.getManufactureProducts(host, realm);
        logger.info("materials.size() = {}, realm = {}", materials.size(), realm);
        Utils.writeToGson(baseDir + "materials.json", materials);
        final List<Product> materials_en = ProductInitParser.getManufactureProducts(host_en, realm);
        logger.info("materials_en.size() = {}, realm = {}", materials_en.size(), realm);
        Utils.writeToGson(baseDir + "materials_en.json", materials_en);
        saveProductImg(materials);
        logger.info("materials img saved");
        //страны
        //final List<Country> countries = CityInitParser.getCountries(host, realm);
        //регионы
        final List<Region> regions = CityInitParser.getRegions(host, realm);
        //города
        final List<City> cities = CityListParser.getCities(host, realm);

        logger.info("группируем ставки енвд по регионам");
        final Map<String, List<RegionCTIE>> allRegionsCTIEList = RegionCTIEParser.getAllRegionsCTIEList(host, realm, regions);
        for (final Map.Entry<String, List<RegionCTIE>> entry : allRegionsCTIEList.entrySet()) {
            Utils.writeToGson(baseDir + "region_ctie" + File.separator + entry.getKey() + ".json", entry.getValue());
        }
        logger.info("группируем тарифы на электроэнергию по товарам");
        final Map<String, List<CityElectricityTariff>> cityElectricityTariffList = CityElectricityTariffParser.getAllCityElectricityTariffList(host, realm, cities);
        for (final Map.Entry<String, List<CityElectricityTariff>> entry : cityElectricityTariffList.entrySet()) {
            Utils.writeToGson(baseDir + CITY_ELECTRICITY_TARIFF + File.separator + entry.getKey() + ".json", entry.getValue());
        }
        logger.info("собираем данные о доступных товарах на оптовом рынке");
        final Map<String, List<ProductRemain>> productRemains = ProductRemainParser.getRemains(host, realm, materials);
        //сохраняем их в json
        for (final Map.Entry<String, List<ProductRemain>> entry : productRemains.entrySet()) {
            Utils.writeToGson(baseDir + "product_remains_" + entry.getKey() + ".json", entry.getValue());
        }
        if (!"fast".equalsIgnoreCase(realm)) {
            logger.info("собираем данные о среднем качестве товаров");
            final List<ProductHistory> productHistory = ProductHistoryParser.getHistory(host, realm, materials);
            Utils.writeToGson(baseDir + "product_history.json", productHistory);
            logger.info("собираем товары которые можно произвести с качеством выше среднего");
            final List<TechUnitType> techList = TechListParser.getTechUnitTypes(Wizard.host, realm);
            final List<ProductionAboveAverage> productionAboveAverage = ProductionAboveAverageParser.calc(host, realm, productHistory, productRemains, productRecipes, manufactures, techList);
            final List<ProductionAboveAverage> productionAboveAverage_en = ProductionAboveAverageParser.calc(host, realm, productHistory, productRemains, productRecipes_en, manufactures, techList);
            logger.info("productionAboveAverage.size = {}, realm = {}", productionAboveAverage.size(), realm);
            logger.info("productionAboveAverage_en.size = {}, realm = {}", productionAboveAverage_en.size(), realm);
            Utils.writeToGsonZip(baseDir + "production_above_average.json", productionAboveAverage);
            Utils.writeToGsonZip(baseDir + "production_above_average_en.json", productionAboveAverage_en);
            logger.info("запоминаем дату обновления данных");
            final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            Utils.writeToGson(baseDir + "production_above_average_updateDate.json", new UpdateDate(df.format(new Date())));
        }
        logger.info("запоминаем дату обновления данных");
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Utils.writeToGson(baseDir + "updateDate.json", new UpdateDate(df.format(new Date())));
    }


}