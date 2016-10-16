package ru.VirtaMarketAnalyzer.parser;

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

/**
 * Created by cobr123 on 16.10.2016.
 */
public final class ProductionAboveAverageParser {
    private static final Logger logger = LoggerFactory.getLogger(ProductionAboveAverageParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));

        final String realm = "olga";
        final List<Product> products = new ArrayList<>();
        //продукт
        products.add(new Product("", "", "1482", ""));
        final List<ProductHistory> productHistory = ProductHistoryParser.getHistory(Wizard.host + realm + "/main/globalreport/product_history/", products);
        //ингридиенты для поиска остатков
        products.add(new Product("", "", "1493", ""));
        products.add(new Product("", "", "1484", ""));
        final Map<String, List<ProductRemain>> productRemains = ProductRemainParser.getRemains(Wizard.host + realm + "/main/globalreport/marketing/by_products/", products);
        //System.out.println(Utils.getPrettyGson(productRemains));

        final List<Manufacture> manufactures = ManufactureListParser.getManufactures(Wizard.host + realm + "/main/common/main_page/game_info/industry/");
        final Map<String, List<ProductRecipe>> productRecipes = ProductRecipeParser.getProductRecipes(Wizard.host + realm + "/main/industry/unit_type/info/", manufactures);

        System.out.println(Utils.getPrettyGson(calc(Wizard.host, realm, productHistory, productRemains, productRecipes)));
    }

    public static List<ProductionAboveAverage> calc(
            final String host
            , final String realm
            , final List<ProductHistory> productHistory
            , final Map<String, List<ProductRemain>> productRemains
            , final Map<String, List<ProductRecipe>> productRecipes
    ) {
        return productHistory.parallelStream()
                .map(ph -> calc(host, realm, ph, productRemains, productRecipes.get(ph.getProductID())))
                .flatMap(Collection::parallelStream)
                .filter(paa -> paa != null)
                .collect(Collectors.toList());
    }

    public static List<ProductionAboveAverage> calc(
            final String host
            , final String realm
            , final ProductHistory productHistory
            , final Map<String, List<ProductRemain>> productRemains
            , final List<ProductRecipe> productRecipes
    ) {
        return productRecipes.stream()
                .map(pr -> calc(host, realm, productHistory, productRemains, pr))
                .flatMap(Collection::stream)
                .filter(paa -> paa != null)
                .collect(Collectors.toList());
    }

    public static List<ProductionAboveAverage> calc(
            final String host
            , final String realm
            , final ProductHistory productHistory
            , final Map<String, List<ProductRemain>> productRemains
            , final ProductRecipe productRecipe
    ) {
        final List<ProductionAboveAverage> list = new ArrayList<>();
        for (int lvl = 1; lvl <= 30; ++lvl) {
            final List<ProductionAboveAverage> paa = calc(host, realm, productHistory, productRemains, productRecipe, lvl);
            list.addAll(paa);
        }
        return list;
    }

    public static List<ProductionAboveAverage> calc(
            final String host
            , final String realm
            , final ProductHistory productHistory
            , final Map<String, List<ProductRemain>> productRemains
            , final ProductRecipe productRecipe
            , final int techLvl
    ) {
        final List<ProductionAboveAverage> list = new ArrayList<>();
        if (productRecipe.getInputProducts() == null || productRecipe.getInputProducts().size() == 0) {
            return list;
        }
        //пробуем 10 лучших по соотношению цена/качество
        for(int idx = 0; idx < 5; ++ idx) {
            final List<ProductRemain> materials = new ArrayList<>();
            for (final ManufactureIngredient inputProduct : productRecipe.getInputProducts()) {
                //logger.info("inputProduct.getProductID() == {}", inputProduct.getProductID());
                final List<ProductRemain> remains = productRemains.getOrDefault(inputProduct.getProductID(), new ArrayList<>())
                        .stream()
                        .filter(r -> r.getMaxOrderType() == ProductRemain.MaxOrderType.U || r.getMaxOrder() >= inputProduct.getQty())
                        .filter(r -> r.getRemain() >= inputProduct.getQty())
                        .filter(r -> r.getQuality() >= inputProduct.getMinQuality())
                        .collect(Collectors.toList());

                if (remains.size() <= idx) {
                    //logger.info("remains.size() <= {}", idx);
                    return list;
                }
                remains.sort((o1, o2) -> (o1.getPrice() / o1.getQuality() > o2.getPrice() / o2.getQuality()) ? 1 : -1);
                materials.add(remains.get(idx));
                //logger.info("remains.get(0).getQuality() = {}, .getPrice() = {}", remains.get(0).getQuality(), remains.get(0).getPrice());
                //logger.info("remains.get({}).getQuality() = {}, .getPrice() = {}", remains.size()-1, remains.get(remains.size()-1).getQuality(), remains.get(remains.size()-1).getPrice());
            }
            list.add(calc(host, realm, productHistory, materials, productRecipe, techLvl));
        }
        return list;
    }

    public static ProductionAboveAverage calc(
            final String host
            , final String realm
            , final ProductHistory productHistory
            , final List<ProductRemain> materials
            , final ProductRecipe productRecipe
            , final int techLvl
    ) {
        final List<ManufactureCalcResult> manufactureCalcResults = calcResult(productRecipe, materials, techLvl);

        for (final ManufactureCalcResult manufactureCalcResult : manufactureCalcResults) {
            //logger.info("productHistory.getQuality() = {}", productHistory.getQuality());
            //logger.info("manufactureCalcResult.getQuality() = {}", manufactureCalcResult.getQuality());
            if (manufactureCalcResult.getQuality() >= productHistory.getQuality()) {
                return new ProductionAboveAverage(
                        productRecipe.getManufactureID()
                        ,productRecipe.getSpecialization()
                        ,manufactureCalcResult.getProductID()
                        ,manufactureCalcResult.getVolume()
                        ,manufactureCalcResult.getQuality()
                        ,manufactureCalcResult.getCost()
                        ,materials
                        ,techLvl
                );
            }
        }
        return null;
    }

    public static List<ManufactureCalcResult> calcResult(final ProductRecipe productRecipe, final List<ProductRemain> materials, final double techLvl) {
        //logger.info("tech = {}", techLvl);
        final List<ManufactureCalcResult> result = new ArrayList<>();
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
        final List<Double> ingQual = new ArrayList<>();
        final List<Double> ingPrice = new ArrayList<>();
        final List<Double> ingTotalPrice = new ArrayList<>();
        final List<Double> ingBaseQty = new ArrayList<>();
        double ingTotalCost = 0.0;

        productRecipe.getInputProducts()
                .stream()
                .forEach(ing -> ingBaseQty.add(ing.getQty()));

        materials.stream()
                .forEach(material -> {
                    ingQual.add(material.getQuality());
                    ingPrice.add(material.getPrice());
                });
        final int num = ingQual.size();
        final double eff = 1.0;
        //количество товаров производимых 1 человеком
        final double prodBaseQuan = productRecipe.getResultProducts().get(0).getProdBaseQty();
        //var prodbase_quan2  = recipe.rp[1].pbq || 0;
        //итоговое количество товара за единицу производства
        final double resultQty = productRecipe.getResultProducts().get(0).getResultQty();

        final double work_quant = 10000.0;
        final double work_salary = 300.0;

        //квалификация работников
        //var PersonalQual = Math.pow(tech, 0.8);
        //$("#PersonalQual", this).text(PersonalQual.toFixed(2));

        //качество станков
//        final Double equipQual = Math.pow(techLvl, 1.2);
        //$("#EquipQuan", this).text(EquipQual.toFixed(2));
//        result.equipQual = equipQual.toFixed(2);

        final List<Double> ingQuantity = new ArrayList<>();
        //количество ингридиентов
        for (int i = 0; i < num; i++) {
            ingQuantity.add(ingBaseQty.get(i) / resultQty * prodBaseQuan * work_quant * Math.pow(1.05, techLvl - 1.0) * eff);
//            result.materials[i].ingQty = Math.round(ingQuantity[i]);
            //console.log('ingQuantity[i] = ' + ingQuantity[i]);
        }
        //цена ингридиентов
        for (int i = 0; i < num; i++) {
            if (ingPrice.get(i) > 0) {
                ingTotalPrice.add(ingQuantity.get(i) * ingPrice.get(i));
            } else {
                ingTotalPrice.add(0.0);
            }
        }
        //общая цена ингридиентов
        for (int i = 0; i < num; i++) {
            ingTotalCost += ingTotalPrice.get(i);
        }
        //объем выпускаемой продукции
        final double prodQuantity = work_quant * prodBaseQuan * Math.pow(1.05, techLvl - 1.0) * eff;
//        result.quantity = Math.round(prodQuantity);

        //итоговое качество ингридиентов
        double ingTotalQual = 0.0;
        double ingTotalQty = 0.0;
        for (int i = 0; i < num; i++) {
            ingTotalQual += ingQual.get(i) * ingBaseQty.get(i);
            ingTotalQty += ingBaseQty.get(i);
        }
        ingTotalQual = ingTotalQual / ingTotalQty * eff;

        //качество товара
        double prodQual = Math.pow(ingTotalQual, 0.5) * Math.pow(techLvl, 0.65);
        //console.log('prodQual = ' + prodQual);
        //ограничение качества (по технологии)
        if (prodQual > Math.pow(techLvl, 1.3)) {
            prodQual = Math.pow(techLvl, 1.3);
        }
        if (prodQual < 1) {
            prodQual = 1.0;
        }
        //бонус к качеству
        prodQual = prodQual * (1.0 + productRecipe.getResultProducts().get(0).getQualityBonusPercent() / 100.0);
        //$("#prodQual", this).text( prodQual.toFixed(2) ) ;
//        result.quality = prodQual.toFixed(2);

        //себестоимость
        final double zp = work_salary * work_quant;
        final double exps = ingTotalCost + zp + zp * 0.1;
        //$("#Cost", this).text( "$" + commaSeparateNumber((exps / prodQuantity).toFixed(2)) );
//        result.cost = (exps / prodQuantity).toFixed(2);
        result.add(new ManufactureCalcResult(
                productRecipe.getResultProducts().get(0).getProductID()
                ,Math.round(prodQuantity)
                ,Math.round(prodQual * 100.0) / 100.0
                ,exps / prodQuantity
                ));

        //прибыль
//        final double profit = (Sale_Price * prodQuantity) - exps;
        //$("#profit", this).text( "$" + commaSeparateNumber(profit.toFixed(2)) );
//        result.profit = profit.toFixed(2);
        return result;
    }
}
