package ru.VirtaMarketAnalyzer.parser;

import one.util.streamex.StreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Created by cobr123 on 16.10.2016.
 */
public final class ProductionAboveAverageParser {
    private static final Logger logger = LoggerFactory.getLogger(ProductionAboveAverageParser.class);

    public static void main(final String[] args) throws IOException {
        final String host = Wizard.host;
        final String realm = "olga";

        final List<Product> productsForHistory = new ArrayList<>();
        //продукт
        productsForHistory.add(ProductInitParser.getManufactureProduct(host, realm, "422718"));
        final List<ProductHistory> productHistory = ProductHistoryParser.getHistory(host, realm, productsForHistory);
        //ингридиенты для поиска остатков
        final List<Product> productsForRemains = new ArrayList<>();
        productsForRemains.add(ProductInitParser.getManufactureProduct(host, realm, "370073"));
        productsForRemains.add(ProductInitParser.getManufactureProduct(host, realm, "1477"));
        productsForRemains.add(ProductInitParser.getManufactureProduct(host, realm, "1476"));
        productsForRemains.add(ProductInitParser.getManufactureProduct(host, realm, "1481"));

//        final List<Product> products = ProductInitParser.getManufactureProducts(host, realm);
//        final List<ProductHistory> productHistory = ProductHistoryParser.getHistory(host + realm + "/main/globalreport/product_history/", products);

        final Map<String, List<ProductRemain>> productRemains = ProductRemainParser.getRemains(host, realm, productsForRemains);
        //System.out.println(Utils.getPrettyGson(productRemains));

        logger.info("getManufactures");
        final List<Manufacture> manufactures = ManufactureListParser.getManufactures(host, realm);
        logger.info("getProductRecipes");
        final Map<String, List<ProductRecipe>> productRecipes = ProductRecipeParser.getProductRecipes(host, realm, manufactures);
        logger.info("calc");
//        logger.info(Utils.getPrettyGson(calc(host, realm, productHistory, productRemains, productRecipes)));
        final List<TechUnitType> techList = TechListParser.getTechUnitTypes(Wizard.host, realm);
        logger.info(Utils.getPrettyGson(calc(host, realm, productHistory, productRemains, productRecipes, manufactures, techList)));
//        final long total = ProductInitParser.getManufactureProductCategories(host, realm)
//                .stream()
//                .mapToLong(pc -> {
//                    try {
//                        final List<ProductHistory> productHistory = ProductHistoryParser.getHistory(host + realm + "/main/globalreport/product_history/"
//                                , products.stream()
//                                        .filter(p -> p.getProductCategoryID().equals(pc.getId()))
//                                        .collect(Collectors.toList())
//                        );
//                        logger.info("calc");
//                        logger.info(pc.getCaption());
//                        final long size = calc(host, realm, productHistory, productRemains, productRecipes).size();
//                        logger.info("" + size);
//                        return size;
//                    } catch (final Exception e) {
//                        logger.error(e.getLocalizedMessage(), e);
//                        return 0;
//                    }
//                })
//                .sum();
//        logger.info("total = {}", total);
    }

    public static List<ProductionAboveAverage> calc(
            final String host
            , final String realm
            , final List<ProductHistory> productHistory
            , final Map<String, List<ProductRemain>> productRemains
            , final Map<String, List<ProductRecipe>> productRecipes
            , final List<Manufacture> manufactures
            , final List<TechUnitType> techList
    ) throws IOException {
        final List<TechLvl> techLvls = TechMarketAskParser.getTech(host, realm, techList);
//        productRecipes.values().stream()
//                .flatMap(Collection::stream)
//                .mapToInt(pr -> pr.getInputProducts().size())
//                .boxed()
//                .sorted(reverseOrder())
//                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
//                .entrySet().stream()
//                .forEachOrdered(entry -> logger.info("топ по количеству ингридиентов - {}: {} шт.", entry.getKey(), entry.getValue()));
        for (final Map.Entry<String, List<ProductRemain>> entry : productRemains.entrySet()) {
            final String key = entry.getKey();
            final List<ProductRemain> sortedList = entry.getValue();
            sortedList.sort((o1, o2) -> (o1.getPrice() / o1.getQuality() > o2.getPrice() / o2.getQuality()) ? 1 : -1);
            productRemains.put(key, sortedList);
        }
        return productHistory.parallelStream()
                .filter(ph -> productRecipes.containsKey(ph.getProductID()))
                .map(ph -> calcByProduct(host, realm, techLvls, ph, productRemains, productRecipes.get(ph.getProductID()), manufactures))
                .flatMap(Collection::stream)
                .collect(toList());
    }

    public static List<ProductionAboveAverage> calcByProduct(
            final String host
            , final String realm
            , final List<TechLvl> techLvls
            , final ProductHistory productHistory
            , final Map<String, List<ProductRemain>> productRemains
            , final List<ProductRecipe> productRecipes
            , final List<Manufacture> manufactures
    ) {
        return productRecipes.stream()
                .filter(pr -> pr.getInputProducts() != null && !pr.getInputProducts().isEmpty())
                .map(pr -> calcByMaxTech(host, realm, techLvls, productHistory, productRemains, pr, manufactures))
                .flatMap(Collection::stream)
                .collect(toList());
    }

    public static List<ProductionAboveAverage> calcByMaxTech(
            final String host
            , final String realm
            , final List<TechLvl> techLvls
            , final ProductHistory productHistory
            , final Map<String, List<ProductRemain>> productRemains
            , final ProductRecipe productRecipe
            , final List<Manufacture> manufactures
    ) {
        final int maxTechLvl = techLvls.stream()
                .filter(tl -> productRecipe.getManufactureID().equals(tl.getTechId()))
                .filter(tl -> tl.getPrice() > 0)
                .max((o1, o2) -> (o1.getLvl() > o2.getLvl()) ? 1 : -1)
                .orElse(new TechLvl("", 2, 0.0))
                .getLvl();

        final List<List<ProductRemain>> materials = getProductRemain(productRecipe, productRemains);

        return IntStream.rangeClosed(1, maxTechLvl)
                .mapToObj(lvl -> calcByRecipe(host, realm, productHistory, materials, productRecipe, lvl, manufactures, productRemains))
                .flatMap(Collection::stream)
                .collect(toList());
    }

    public static List<List<ProductRemain>> getProductRemain(final ProductRecipe productRecipe, final Map<String, List<ProductRemain>> productRemains) {
        final List<List<ProductRemain>> materials = new ArrayList<>();
        final double work_quant = 1000.0;
        final double koef = (productRecipe.getResultProducts().get(0).getProdBaseQty() * work_quant) / productRecipe.getResultProducts().get(0).getResultQty();

        for (final ManufactureIngredient inputProduct : productRecipe.getInputProducts()) {
            final List<ProductRemain> remains = productRemains.getOrDefault(inputProduct.getProductID(), new ArrayList<>())
                    .stream()
                    .filter(r -> r.getQuality() >= inputProduct.getMinQuality())
                    .filter(r -> r.getRemainByMaxOrderType() >= inputProduct.getQty() * koef)
                    .collect(Collectors.toList());

            materials.add(remains);
        }
        return materials;
    }

    public static List<ProductionAboveAverage> calcByRecipe(
            final String host
            , final String realm
            , final ProductHistory productHistory
            , final List<List<ProductRemain>> materials
            , final ProductRecipe productRecipe
            , final double techLvl
            , final List<Manufacture> manufactures
            , final Map<String, List<ProductRemain>> productRemains
    ) {
//        final double playerQuality = Utils.calcPlayerQualityForTech(techLvl);
//        final double workersQuality = Utils.calcWorkersQualityForTech(techLvl);
//        final double optimalTop1 = Utils.calcMaxTop1(playerQuality, workersQuality) * 0.75;
        final long maxWorkplacesCount = manufactures.stream()
                .filter(m -> m.getId().equals(productRecipe.getManufactureID()))
                .mapToInt(m -> m.getSizes().stream().mapToInt(ManufactureSize::getWorkplacesCount).max().orElse(0))
                .max()
                .orElse(0);
//        logger.info("materials.size() = {}", materials.size());
//        logger.info("StreamEx.cartesianProduct = {}", StreamEx.cartesianProduct(materials).count());
        //оставляем по 3 лучших для каждого уровня технологии для каждого товара
        return StreamEx.cartesianProduct(materials)
                .limit(100_000)
                .map(mats -> {
                    try {
                        return calcResult(host, realm, productRecipe, mats, techLvl, maxWorkplacesCount, productRemains, null);
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(paa -> paa.getQuality() > productHistory.getQuality())
                .filter(paa -> paa.getProductID().equals(productHistory.getProductID()))
                .sorted((o1, o2) -> (o1.getCost() / o1.getQuality() > o2.getCost() / o2.getQuality()) ? 1 : -1)
                .limit(3)
                .collect(toList());
    }

    public static List<ProductionAboveAverage> calcResult(
            final String host,
            final String realm,
            final ProductRecipe productRecipe,
            final List<ProductRemain> materials,
            final double techLvl,
            final long maxWorkplacesCount,
            final Map<String, List<ProductRemain>> productRemains,
            final City productionCity
    ) throws Exception {
        //logger.info("tech = {}", techLvl);
        final List<ProductionAboveAverage> result = new ArrayList<>(productRecipe.getResultProducts().size());
//        var result = {
//                spec:recipe.s
//                , manufactureID:recipe.i
//                , tech:tech
//                , quality:0
//                , quantity:0
//                , cost:0
//                , profit:0
//                , equipQual:0
//                , equipId:recipe.e.i
//                , materials:materials
//                , productID:recipe.rp[0].pi
//        };
        final double[] ingQual = new double[materials.size()];
        final double[] ingPrice = new double[materials.size()];
        final double[] ingTotalPrice = new double[materials.size()];
        final double[] ingBaseQty = new double[materials.size()];

        for (int i = 0; i < productRecipe.getInputProducts().size(); ++i) {
            ingBaseQty[i] = productRecipe.getInputProducts().get(i).getQty();
        }
        for (int i = 0; i < materials.size(); ++i) {
            final ProductRemain material = materials.get(i);
            ingQual[i] = material.getQuality();
            ingPrice[i] = (productionCity == null) ? material.getPrice() : CountryDutyListParser.addDutyAndTransportCost(
                    host, realm,
                    material.getCountryId(), productionCity.getCountryId(),
                    material.getTownId(), productionCity.getId(),
                    material.getProductID(),
                    material.getPrice()
            );
        }

        final int num = ingQual.length;
        final double eff = 1.0;
        //количество товаров производимых 1 человеком
        final double[] prodBaseQuan = new double[productRecipe.getResultProducts().size()];
        for (int i = 0; i < productRecipe.getResultProducts().size(); ++i) {
            prodBaseQuan[i] = productRecipe.getResultProducts().get(i).getProdBaseQty();
        }
        //var prodbase_quan2  = recipe.rp[1].pbq || 0;
        //итоговое количество товара за единицу производства
        final double[] resultQty = new double[productRecipe.getResultProducts().size()];
        for (int i = 0; i < productRecipe.getResultProducts().size(); ++i) {
            resultQty[i] = productRecipe.getResultProducts().get(i).getResultQty();
        }

        final double work_quant = 1000.0;
        final double work_salary = (productionCity == null) ? 300.0 : productionCity.getAverageSalary();

        //квалификация работников
        //var PersonalQual = Math.pow(tech, 0.8);
        //$("#PersonalQual", this).text(PersonalQual.toFixed(2));

        //качество станков
//        final Double equipQual = Math.pow(techLvl, 1.2);
        //$("#EquipQuan", this).text(EquipQual.toFixed(2));
//        result.equipQual = equipQual.toFixed(2);

        final double[] ingQuantity = new double[num];
        //количество ингридиентов
        for (int i = 0; i < num; i++) {
            ingQuantity[i] = ingBaseQty[i] / resultQty[0] * prodBaseQuan[0] * work_quant * Math.pow(1.05, techLvl - 1.0) * eff;
//            result.materials[i].ingQty = Math.round(ingQuantity[i]);
            //console.log('ingQuantity[i] = ' + ingQuantity[i]);
        }
        //цена ингридиентов
        for (int i = 0; i < num; i++) {
            if (ingPrice[i] > 0) {
                ingTotalPrice[i] = ingQuantity[i] * ingPrice[i];
            } else {
                ingTotalPrice[i] = 0.0;
            }
        }
        double ingTotalCost = 0.0;
        //общая цена ингридиентов
        for (int i = 0; i < num; i++) {
            ingTotalCost += ingTotalPrice[i];
        }
        //объем выпускаемой продукции
        final double[] prodQuantity = new double[productRecipe.getResultProducts().size()];
        for (int i = 0; i < productRecipe.getResultProducts().size(); ++i) {
            prodQuantity[i] = work_quant * prodBaseQuan[i] * Math.pow(1.05, techLvl - 1.0) * eff;
        }
//        result.quantity = Math.round(prodQuantity);

        //итоговое качество ингридиентов
        double ingTotalQual = 0.0;
        double ingTotalQty = 0.0;
        for (int i = 0; i < num; i++) {
            ingTotalQual += ingQual[i] * ingBaseQty[i];
            ingTotalQty += ingBaseQty[i];
        }
        ingTotalQual = ingTotalQual / ingTotalQty * eff;

        //качество товара
        final double[] prodQual = new double[productRecipe.getResultProducts().size()];

        prodQual[0] = Math.pow(ingTotalQual, 0.5) * Math.pow(techLvl, 0.65);

        //ограничение качества (по технологии)
        if (prodQual[0] > Math.pow(techLvl, 1.3)) {
            prodQual[0] = Math.pow(techLvl, 1.3);
        }
        if (prodQual[0] < 1) {
            prodQual[0] = 1.0;
        }
        //бонус к качеству
        prodQual[0] = prodQual[0] * (1.0 + productRecipe.getResultProducts().get(0).getQualityBonusPercent() / 100.0);

        for (int i = 1; i < productRecipe.getResultProducts().size(); ++i) {
            prodQual[i] = Math.pow(ingTotalQual, 0.5) * Math.pow(techLvl, 0.65) * (1.0 + productRecipe.getResultProducts().get(i).getQualityBonusPercent() / 100.0);

            //ограничение качества (по технологии)
            if (prodQual[i] > Math.pow(techLvl, 1.3)) {
                prodQual[i] = Math.pow(techLvl, 1.3);
            }
            if (prodQual[i] < 1) {
                prodQual[i] = 1.0;
            }
        }
        //себестоимость
        final double zp = work_salary * work_quant;
        final double exps = ingTotalCost + zp + zp * 0.1;
        //$("#Cost", this).text( "$" + commaSeparateNumber((exps / prodQuantity).toFixed(2)) );
//        result.cost = (exps / prodQuantity).toFixed(2);
        result.add(new ProductionAboveAverage(
                productRecipe.getManufactureID()
                , productRecipe.getSpecialization()
                , productRecipe.getResultProducts().get(0).getProductID()
                , Math.round(prodQuantity[0])
                , Math.round(prodQual[0] * 100.0) / 100.0
                , Math.round(exps / prodQuantity[0] * 100.0) / 100.0
                , materials
                , techLvl
                , maxWorkplacesCount
                , productRemains.getOrDefault(productRecipe.getResultProducts().get(0).getProductID(), new ArrayList<>())
                .stream()
                .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[0] / prodQual[0])
        ));
        if (productRecipe.getResultProducts().size() == 3) {
            //Нефтеперегонка
            //Бензин Нормаль-80 - 35%
            result.add(new ProductionAboveAverage(
                    productRecipe.getManufactureID()
                    , productRecipe.getSpecialization()
                    , productRecipe.getResultProducts().get(0).getProductID()
                    , Math.round(prodQuantity[0])
                    , Math.round(prodQual[0] * 100.0) / 100.0
                    , Math.round(exps / prodQuantity[0] * 0.35 * 100.0) / 100.0
                    , materials
                    , techLvl
                    , maxWorkplacesCount
                    , productRemains.getOrDefault(productRecipe.getResultProducts().get(0).getProductID(), new ArrayList<>())
                    .stream()
                    .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[0] / prodQual[0])
            ));
            //Дизельное топливо - 30%
            result.add(new ProductionAboveAverage(
                    productRecipe.getManufactureID()
                    , productRecipe.getSpecialization()
                    , productRecipe.getResultProducts().get(1).getProductID()
                    , Math.round(prodQuantity[1])
                    , Math.round(prodQual[1] * 100.0) / 100.0
                    , Math.round(exps / prodQuantity[1] * 0.30 * 100.0) / 100.0
                    , materials
                    , techLvl
                    , maxWorkplacesCount
                    , productRemains.getOrDefault(productRecipe.getResultProducts().get(1).getProductID(), new ArrayList<>())
                    .stream()
                    .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[1] / prodQual[1])
            ));
            //Мазут             - 35%
            result.add(new ProductionAboveAverage(
                    productRecipe.getManufactureID()
                    , productRecipe.getSpecialization()
                    , productRecipe.getResultProducts().get(2).getProductID()
                    , Math.round(prodQuantity[2])
                    , Math.round(prodQual[2] * 100.0) / 100.0
                    , Math.round(exps / prodQuantity[2] * 0.35 * 100.0) / 100.0
                    , materials
                    , techLvl
                    , maxWorkplacesCount
                    , productRemains.getOrDefault(productRecipe.getResultProducts().get(2).getProductID(), new ArrayList<>())
                    .stream()
                    .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[2] / prodQual[2])
            ));
        } else if (productRecipe.getResultProducts().size() == 4) {
            //Ректификация нефти
            //Бензин Нормаль-80 - 35%
            result.add(new ProductionAboveAverage(
                    productRecipe.getManufactureID()
                    , productRecipe.getSpecialization()
                    , productRecipe.getResultProducts().get(0).getProductID()
                    , Math.round(prodQuantity[0])
                    , Math.round(prodQual[0] * 100.0) / 100.0
                    , Math.round(exps / prodQuantity[0] * 0.35 * 100.0) / 100.0
                    , materials
                    , techLvl
                    , maxWorkplacesCount
                    , productRemains.getOrDefault(productRecipe.getResultProducts().get(0).getProductID(), new ArrayList<>())
                    .stream()
                    .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[0] / prodQual[0])
            ));
            //Бензин Регуляр-92 - 32%
            result.add(new ProductionAboveAverage(
                    productRecipe.getManufactureID()
                    , productRecipe.getSpecialization()
                    , productRecipe.getResultProducts().get(1).getProductID()
                    , Math.round(prodQuantity[1])
                    , Math.round(prodQual[1] * 100.0) / 100.0
                    , Math.round(exps / prodQuantity[1] * 0.32 * 100.0) / 100.0
                    , materials
                    , techLvl
                    , maxWorkplacesCount
                    , productRemains.getOrDefault(productRecipe.getResultProducts().get(1).getProductID(), new ArrayList<>())
                    .stream()
                    .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[1] / prodQual[1])
            ));
            //Дизельное топливо - 23%
            result.add(new ProductionAboveAverage(
                    productRecipe.getManufactureID()
                    , productRecipe.getSpecialization()
                    , productRecipe.getResultProducts().get(2).getProductID()
                    , Math.round(prodQuantity[2])
                    , Math.round(prodQual[2] * 100.0) / 100.0
                    , Math.round(exps / prodQuantity[2] * 0.23 * 100.0) / 100.0
                    , materials
                    , techLvl
                    , maxWorkplacesCount
                    , productRemains.getOrDefault(productRecipe.getResultProducts().get(2).getProductID(), new ArrayList<>())
                    .stream()
                    .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[2] / prodQual[2])
            ));
            //Мазут             - 10%
            result.add(new ProductionAboveAverage(
                    productRecipe.getManufactureID()
                    , productRecipe.getSpecialization()
                    , productRecipe.getResultProducts().get(3).getProductID()
                    , Math.round(prodQuantity[3])
                    , Math.round(prodQual[3] * 100.0) / 100.0
                    , Math.round(exps / prodQuantity[3] * 0.10 * 100.0) / 100.0
                    , materials
                    , techLvl
                    , maxWorkplacesCount
                    , productRemains.getOrDefault(productRecipe.getResultProducts().get(3).getProductID(), new ArrayList<>())
                    .stream()
                    .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[3] / prodQual[3])
            ));
        } else if (productRecipe.getResultProducts().size() == 5) {
            //Каталитический крекинг нефти
            //Бензин Нормаль-80 - 7%
            result.add(new ProductionAboveAverage(
                    productRecipe.getManufactureID()
                    , productRecipe.getSpecialization()
                    , productRecipe.getResultProducts().get(0).getProductID()
                    , Math.round(prodQuantity[0])
                    , Math.round(prodQual[0] * 100.0) / 100.0
                    , Math.round(exps / prodQuantity[0] * 0.07 * 100.0) / 100.0
                    , materials
                    , techLvl
                    , maxWorkplacesCount
                    , productRemains.getOrDefault(productRecipe.getResultProducts().get(0).getProductID(), new ArrayList<>())
                    .stream()
                    .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[0] / prodQual[0])
            ));
            //Бензин Премиум-95 - 35%
            result.add(new ProductionAboveAverage(
                    productRecipe.getManufactureID()
                    , productRecipe.getSpecialization()
                    , productRecipe.getResultProducts().get(1).getProductID()
                    , Math.round(prodQuantity[1])
                    , Math.round(prodQual[1] * 100.0) / 100.0
                    , Math.round(exps / prodQuantity[1] * 0.35 * 100.0) / 100.0
                    , materials
                    , techLvl
                    , maxWorkplacesCount
                    , productRemains.getOrDefault(productRecipe.getResultProducts().get(1).getProductID(), new ArrayList<>())
                    .stream()
                    .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[1] / prodQual[1])
            ));
            //Бензин Регуляр-92 - 51%
            result.add(new ProductionAboveAverage(
                    productRecipe.getManufactureID()
                    , productRecipe.getSpecialization()
                    , productRecipe.getResultProducts().get(2).getProductID()
                    , Math.round(prodQuantity[2])
                    , Math.round(prodQual[2] * 100.0) / 100.0
                    , Math.round(exps / prodQuantity[2] * 0.51 * 100.0) / 100.0
                    , materials
                    , techLvl
                    , maxWorkplacesCount
                    , productRemains.getOrDefault(productRecipe.getResultProducts().get(2).getProductID(), new ArrayList<>())
                    .stream()
                    .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[2] / prodQual[2])
            ));
            //Дизельное топливо - 6%
            result.add(new ProductionAboveAverage(
                    productRecipe.getManufactureID()
                    , productRecipe.getSpecialization()
                    , productRecipe.getResultProducts().get(3).getProductID()
                    , Math.round(prodQuantity[3])
                    , Math.round(prodQual[3] * 100.0) / 100.0
                    , Math.round(exps / prodQuantity[3] * 0.06 * 100.0) / 100.0
                    , materials
                    , techLvl
                    , maxWorkplacesCount
                    , productRemains.getOrDefault(productRecipe.getResultProducts().get(3).getProductID(), new ArrayList<>())
                    .stream()
                    .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[3] / prodQual[3])
            ));
            //Мазут             - 1%
            result.add(new ProductionAboveAverage(
                    productRecipe.getManufactureID()
                    , productRecipe.getSpecialization()
                    , productRecipe.getResultProducts().get(4).getProductID()
                    , Math.round(prodQuantity[4])
                    , Math.round(prodQual[4] * 100.0) / 100.0
                    , Math.round(exps / prodQuantity[4] * 0.01 * 100.0) / 100.0
                    , materials
                    , techLvl
                    , maxWorkplacesCount
                    , productRemains.getOrDefault(productRecipe.getResultProducts().get(4).getProductID(), new ArrayList<>())
                    .stream()
                    .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[4] / prodQual[4])
            ));
        } else {
            for (int i = 1; i < productRecipe.getResultProducts().size(); ++i) {
                final int tmpIdx = i;
                result.add(new ProductionAboveAverage(
                        productRecipe.getManufactureID()
                        , productRecipe.getSpecialization()
                        , productRecipe.getResultProducts().get(i).getProductID()
                        , Math.round(prodQuantity[i])
                        , Math.round(prodQual[i] * 100.0) / 100.0
                        , Math.round(exps / prodQuantity[i] * 100.0) / 100.0
                        , materials
                        , techLvl
                        , maxWorkplacesCount
                        , productRemains.getOrDefault(productRecipe.getResultProducts().get(tmpIdx).getProductID(), new ArrayList<>())
                        .stream()
                        .noneMatch(r -> r.getRemain() >= 0 && r.getPrice() / r.getQuality() <= exps / prodQuantity[tmpIdx] / prodQual[tmpIdx])
                ));
            }
        }

        //прибыль
//        final double profit = (Sale_Price * prodQuantity) - exps;
        //$("#profit", this).text( "$" + commaSeparateNumber(profit.toFixed(2)) );
//        result.profit = profit.toFixed(2);
        return result;
    }
}
