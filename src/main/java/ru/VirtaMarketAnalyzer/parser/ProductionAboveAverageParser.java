package ru.VirtaMarketAnalyzer.parser;

import one.util.streamex.StreamEx;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
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
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));

        final String host = Wizard.host;
        final String realm = "olga";

//        final List<Product> products = new ArrayList<>();
//        //продукт
//        products.add(ProductInitParser.getManufactureProduct(host, realm, "422703"));
//        products.add(ProductInitParser.getManufactureProduct(host, realm, "422704"));
//        products.add(ProductInitParser.getManufactureProduct(host, realm, "422705"));
//        products.add(ProductInitParser.getManufactureProduct(host, realm, "422706"));
//        products.add(ProductInitParser.getManufactureProduct(host, realm, "422707"));
//        final List<ProductHistory> productHistory = ProductHistoryParser.getHistory(host + realm + "/main/globalreport/product_history/", products);
//        //ингридиенты для поиска остатков
//        products.add(ProductInitParser.getManufactureProduct(host, realm, "1467"));
//        products.add(ProductInitParser.getManufactureProduct(host, realm, "1466"));
//        products.add(ProductInitParser.getManufactureProduct(host, realm, "1471"));
//        products.add(ProductInitParser.getManufactureProduct(host, realm, "1483"));

        final List<Product> products = ProductInitParser.getManufactureProducts(host, realm);
        final List<ProductHistory> productHistory = ProductHistoryParser.getHistory(host + realm + "/main/globalreport/product_history/", products);

        final Map<String, List<ProductRemain>> productRemains = ProductRemainParser.getRemains(host + realm + "/main/globalreport/marketing/by_products/", products);
        //System.out.println(Utils.getPrettyGson(productRemains));

        logger.info("getManufactures");
        final List<Manufacture> manufactures = ManufactureListParser.getManufactures(host + realm + "/main/common/main_page/game_info/industry/");
        logger.info("getProductRecipes");
        final Map<String, List<ProductRecipe>> productRecipes = ProductRecipeParser.getProductRecipes(host, realm, manufactures);
        logger.info("calc");
//        logger.info(Utils.getPrettyGson(calc(host, realm, productHistory, productRemains, productRecipes)));
        logger.info(Utils.getPrettyGson(calc(host, realm, productHistory, productRemains, productRecipes).size()));
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
    ) throws IOException {
        final List<TechLvl> techLvls = TechMarketAskParser.getTech(host, realm);

        return productHistory.parallelStream()
                .map(ph -> calcByProduct(techLvls, ph, productRemains, productRecipes.get(ph.getProductID())))
                .filter(paa -> paa != null)
                .flatMap(Collection::parallelStream)
                .filter(paa -> paa != null)
                .collect(toList());
    }

    public static List<ProductionAboveAverage> calcByProduct(
            final List<TechLvl> techLvls
            , final ProductHistory productHistory
            , final Map<String, List<ProductRemain>> productRemains
            , final List<ProductRecipe> productRecipes
    ) {
        if (productRecipes == null) {
            return null;
        }
        return productRecipes.stream()
                .map(pr -> calcByMaxTech(techLvls, productHistory, productRemains, pr))
                .filter(paa -> paa != null)
                .flatMap(Collection::stream)
                .filter(paa -> paa != null)
                .collect(toList());
    }

    public static List<ProductionAboveAverage> calcByMaxTech(
            final List<TechLvl> techLvls
            , final ProductHistory productHistory
            , final Map<String, List<ProductRemain>> productRemains
            , final ProductRecipe productRecipe
    ) {
        final int maxTechLvl = techLvls.stream()
                .filter(tl -> productRecipe.getManufactureID().equals(tl.getTechId()))
                .filter(tl -> tl.getPrice() > 0)
                .max((o1, o2) -> (o1.getLvl() > o2.getLvl()) ? 1 : -1)
                .orElse(new TechLvl("", 2, 0.0))
                .getLvl();

        return IntStream.rangeClosed(1, maxTechLvl)
                .mapToObj(lvl -> calcByRecipe(productHistory, productRemains, productRecipe, lvl))
                .filter(paa -> paa != null)
                .flatMap(Collection::stream)
                .filter(paa -> paa != null)
                .collect(toList());
    }

    public static List<ProductionAboveAverage> calcByRecipe(
            final ProductHistory productHistory
            , final Map<String, List<ProductRemain>> productRemains
            , final ProductRecipe productRecipe
            , final double techLvl
    ) {
        if (productRecipe.getInputProducts() == null || productRecipe.getInputProducts().size() == 0) {
            return null;
        }
        final double work_quant = 1000.0;
        final double koef = (productRecipe.getResultProducts().get(0).getProdBaseQty() * work_quant) / productRecipe.getResultProducts().get(0).getResultQty();
        //пробуем 50 лучших по соотношению цена/качество
        final List<List<ProductRemain>> materials = new ArrayList<>();
        for (final ManufactureIngredient inputProduct : productRecipe.getInputProducts()) {
            //logger.info("inputProduct.getProductID() == {}", inputProduct.getProductID());
            final List<ProductRemain> remains = productRemains.getOrDefault(inputProduct.getProductID(), new ArrayList<>())
                    .stream()
                    .filter(r -> r.getRemain() > 0)
                    .filter(r -> r.getQuality() >= inputProduct.getMinQuality())
                    .filter(r -> r.getMaxOrderType() == ProductRemain.MaxOrderType.U || r.getMaxOrder() >= inputProduct.getQty() * koef)
                    .filter(r -> r.getRemain() >= inputProduct.getQty() * koef)
                    .sorted((o1, o2) -> (o1.getPrice() / o1.getQuality() > o2.getPrice() / o2.getQuality()) ? 1 : -1)
                    .limit((productRecipe.getInputProducts().size() <= 3) ? 50 : 3)
                    .collect(Collectors.toList());

            materials.add(remains);
        }
//        logger.info("materials.size() = {}", materials.size());
//        logger.info("StreamEx.cartesianProduct = {}", StreamEx.cartesianProduct(materials).count());
//        logger.info("Utils.ofCombinations = {}", Utils.ofCombinations(materials, ArrayList::new).count());
        //оставляем по 3 лучших для каждого уровня технологии для каждого товара
        return StreamEx.cartesianProduct(materials)
                .map(mats -> calcResult(productRecipe, mats, techLvl))
                .flatMap(Collection::stream)
                .filter(paa -> paa.getQuality() >= productHistory.getQuality())
                .filter(paa -> paa.getProductID().equals(productHistory.getProductID()))
                .sorted((o1, o2) -> (o1.getCost() / o1.getQuality() > o2.getCost() / o2.getQuality()) ? 1 : -1)
                .limit(3)
                .collect(toList());
    }

    public static List<ProductionAboveAverage> calcResult(
            final ProductRecipe productRecipe,
            final List<ProductRemain> materials,
            final double techLvl
    ) {
        //logger.info("tech = {}", techLvl);
        final List<ProductionAboveAverage> result = new ArrayList<>();
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
            ingQual[i] = materials.get(i).getQuality();
            ingPrice[i] = materials.get(i).getPrice();
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

        final double work_quant = 10000.0;
        final double work_salary = 300.0;

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
            ));
        } else {
            for (int i = 1; i < productRecipe.getResultProducts().size(); ++i) {
                result.add(new ProductionAboveAverage(
                        productRecipe.getManufactureID()
                        , productRecipe.getSpecialization()
                        , productRecipe.getResultProducts().get(i).getProductID()
                        , Math.round(prodQuantity[i])
                        , Math.round(prodQual[i] * 100.0) / 100.0
                        , Math.round(exps / prodQuantity[i] * 100.0) / 100.0
                        , materials
                        , techLvl
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
