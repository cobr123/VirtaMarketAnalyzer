package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cobr123 on 18.05.2015.
 */
final public class ProductRecipeParser {
    private static final Logger logger = LoggerFactory.getLogger(ProductRecipeParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
//        final Document doc = Downloader.getDoc(Wizard.host + "olga/main/industry/unit_type/info/15751");
//        final Document doc = Downloader.getDoc(Wizard.host + "olga/main/industry/unit_type/info/422209");
//        final Document doc = Downloader.getDoc(Wizard.host + "olga/main/industry/unit_type/info/2425");
//        final Document doc = Downloader.getDoc(Wizard.host + "olga/main/industry/unit_type/info/2417");
        final String host = Wizard.host;
        final String realm = "olga";
        final String url = host + realm + "/main/industry/unit_type/info/";
        final List<Manufacture> manufactures = new ArrayList<>();
//        manufactures.add(new Manufacture("423140", "manufactureCategory", "caption"));
//        manufactures.add(new Manufacture("2425", "manufactureCategory", "caption"));
        manufactures.add(new Manufacture("2438", "manufactureCategory", "caption"));

        final Map<String, List<ProductRecipe>> result = getProductRecipes(host, realm, manufactures);
        logger.info(Utils.getPrettyGson(result));
    }

    public static Map<String, List<ProductRecipe>> getProductRecipes(final String host, final String realm, final List<Manufacture> manufactures) throws IOException {
        final List<ProductRecipe> recipes = getRecipes(host, realm, manufactures);
        //иногда один продукт можно получить разными способами
        final Map<String, List<ProductRecipe>> productRecipes = new HashMap<>();
        for (final ProductRecipe recipe : recipes) {
            for (final ManufactureResult result : recipe.getResultProducts()) {
                if (!productRecipes.containsKey(result.getProductID())) {
                    productRecipes.put(result.getProductID(), new ArrayList<>());
                }
                productRecipes.get(result.getProductID()).add(recipe);
            }
        }
        return productRecipes;
    }

    public static List<ProductRecipe> getRecipes(final String host, final String realm, final List<Manufacture> manufactures) throws IOException {
        final List<ProductRecipe> recipes = new ArrayList<>();

        for (final Manufacture manufacture : manufactures) {
            try {
                final Document doc = Downloader.getDoc(host + realm + "/main/industry/unit_type/info/" + manufacture.getId());

                final String manufactureCategory = doc.select("table.infoblock > tbody > tr > td:nth-child(2) > a").text();
                manufacture.setManufactureCategory(manufactureCategory);

                final Element lastTableRow = doc.select("table.grid > tbody > tr:nth-child(3)").last();
                final Elements rows = doc.select("table.grid > tbody > tr[class]");
                if (rows.size() > 0) {
                    //количество товаров производимых 1 человеком
                    final String minWorkerQty = Utils.getFirstBySep(lastTableRow.select("> td:nth-child(2)").text(), " ");
                    final String minEquipQty = Utils.getLastBySep(lastTableRow.select("> td:nth-child(2)").text(), " ");
                    final double equipmentPerWorker = Utils.toDouble(minEquipQty) / Utils.toDouble(minWorkerQty);
//                logger.info("minWorkerQty = {}", minWorkerQty);
                    //System.out.println(list.outerHtml());
                    int minProdQtyCellIdx = 3;
                    for (final Element row : rows) {
                        if (!row.select("> td:nth-child(1) > b").text().isEmpty()) {
                            final String specialization = row.select("td:nth-child(1) > b").text();
//                        logger.info("specialization = {}", specialization);
                            final List<ManufactureIngredient> inputProducts = new ArrayList<>();
                            //td:nth-child(3) > table > tbody > tr > td:nth-child(1) > table > tbody > tr:nth-child(1) > td > a:nth-child(1) > img
                            //td:nth-child(3) > table > tbody > tr > td:nth-child(2) > table > tbody > tr:nth-child(1) > td > a:nth-child(1) > img
                            final Elements ings = row.select("> td:nth-child(3) > table > tbody > tr > td > table > tbody > tr:nth-child(1) > td > a:nth-child(1) > img");
                            for (final Element ing : ings) {
                                final String productID = Utils.getLastBySep(ing.parent().attr("href"), "/");
                                final String minQuality = Utils.clearNumber(ing.parent().parent().parent().nextElementSibling().child(0).select("> div > nobr > b").text());
                                ing.parent().parent().parent().nextElementSibling().child(0).children().remove();
                                final String qty = ing.parent().parent().parent().nextElementSibling().child(0).text();
                                inputProducts.add(new ManufactureIngredient(productID, Utils.toDouble(qty), Utils.toDouble(minQuality)));
                            }

                            final List<ManufactureResult> resultProducts = new ArrayList<>();
                            final Elements results = row.select("> td:nth-child(4) > table > tbody > tr > td > table > tbody > tr:nth-child(1) > td > a:nth-child(1)");
                            int resultIdx = 0;
                            for (final Element result : results) {
                                final String minProdQty = lastTableRow.select("> td").eq(minProdQtyCellIdx).select("> nobr").text();
//                            logger.info("minProdQty = {}", minProdQty);
                                final Double prodBaseQty = Utils.toDouble(minProdQty) / Utils.toDouble(minWorkerQty);
//                            logger.info("prodBaseQty = {}", prodBaseQty);

                                final String resultID = Utils.getLastBySep(result.attr("href"), "/");
                                result.parent().parent().nextElementSibling().child(0).children().remove();
                                final String resultQty = result.parent().parent().nextElementSibling().child(0).text();
//                            logger.info("resultQty = {}", Utils.toDouble(resultQty));

                                String qualityBonus = row.select("> td:nth-child(5)").text();
                                if (results.size() > 1) {
                                    final Element bonusTD = row.select("> td:nth-child(5) > table > tbody > tr").eq(resultIdx).select("> td").first();
                                    bonusTD.children().remove();
                                    qualityBonus = bonusTD.text();
                                }
                                final ManufactureResult manufactureResult = new ManufactureResult(resultID, prodBaseQty, Utils.toDouble(resultQty), Utils.toDouble(qualityBonus));
                                resultProducts.add(manufactureResult);
                                ++resultIdx;
                                ++minProdQtyCellIdx;
                            }

                            final Element equipElem = row.select(" > td:nth-child(2) > a:nth-child(1) > img").first();
                            Product equipment = null;
                            //если не "склад"
                            if (!"2011".equals(manufacture.getId())) {
                                equipment = getProduct(host, realm, equipElem);
                            }

                            final ProductRecipe recipe = new ProductRecipe(manufacture.getId(), specialization, equipment, equipmentPerWorker, inputProducts, resultProducts);
                            recipes.add(recipe);
                        }
                    }
                }
            } catch (final Exception e) {
                logger.error(host + realm + "/main/industry/unit_type/info/" + manufacture.getId());
                throw e;
            }
        }
        return recipes;
    }

    private static Product getProduct(final String host, final String realm, final Element equipElem) throws IOException {
        final String id = Utils.getLastBySep(equipElem.parent().attr("href"), "/");
        return ProductInitParser.getManufactureProduct(host, realm, id);
    }
}