package ru.VirtaMarketAnalyzer.main;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.ml.RetailSalePrediction;
import ru.VirtaMarketAnalyzer.publish.GitHubPublisher;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static ru.VirtaMarketAnalyzer.ml.RetailSalePrediction.PRODUCT_REMAINS_;
import static ru.VirtaMarketAnalyzer.ml.RetailSalePrediction.TRADE_AT_CITY_;
import static ru.VirtaMarketAnalyzer.publish.GitHubPublisher.getRepo;

/**
 * Created by cobr123 on 29.09.2017.
 */
final public class TrendUpdater {
    private static final Logger logger = LoggerFactory.getLogger(TrendUpdater.class);

    public static void main(String[] args) throws IOException, GitAPIException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %C{1} %x - %m%n")));

        updateTrends();
    }

    public static void updateTrends() throws IOException, GitAPIException {
        //обновляем
        getRepo();

        for (final String realm : Wizard.realms) {
            updateTrends(realm);
        }
        //публикуем на сайте
        GitHubPublisher.publishTrends();
    }

    private static void updateTrends(final String realm) throws IOException, GitAPIException {
//        if ("olga".equalsIgnoreCase(realm) && (todayIs(Calendar.WEDNESDAY) || todayIs(Calendar.SATURDAY))) {
//        } else if ("anna".equalsIgnoreCase(realm) && todayIs(Calendar.TUESDAY)) {
//        } else if ("mary".equalsIgnoreCase(realm) && todayIs(Calendar.MONDAY)) {
//        } else if (("lien".equalsIgnoreCase(realm) || "nika".equalsIgnoreCase(realm)) && todayIs(Calendar.FRIDAY)) {
//        } else if ("vera".equalsIgnoreCase(realm) && (todayIs(Calendar.THURSDAY) || todayIs(Calendar.SUNDAY))) {
//        } else if ("fast".equalsIgnoreCase(realm)) {
//        } else {
//            return;
//        }
        logger.info("обновляем тренды, {}", realm);
        updateAllRetailTrends(realm);
        updateAllProductRemainTrends(realm);
    }

    public static void updateAllRetailTrends(final String realm) throws IOException, GitAPIException {
        final String baseDir = GitHubPublisher.localPath + Wizard.by_trade_at_cities + File.separator + realm + File.separator;
        final Set<TradeAtCity> set = RetailSalePrediction.getAllTradeAtCity(TRADE_AT_CITY_, realm);
        logger.info("updateAllRetailAnalytics.size() = {}, {}", set.size(), realm);

        //группируем аналитику по товарам и сохраняем
        final Map<String, List<TradeAtCity>> tradeAtCityByProduct = set.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(TradeAtCity::getProductId));

        for (final Map.Entry<String, List<TradeAtCity>> entry : tradeAtCityByProduct.entrySet()) {
            final String fileNamePath = baseDir + Wizard.retail_trends + File.separator + entry.getKey() + ".json";
            Utils.writeToGsonZip(fileNamePath, getRetailTrends(entry.getValue()));
        }
        logger.info("запоминаем дату обновления данных");
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Utils.writeToGson(baseDir + Wizard.retail_trends + File.separator + "updateDate.json", new UpdateDate(df.format(new Date())));
    }

    private static List<RetailTrend> getRetailTrends(final List<TradeAtCity> list) {
        return list.stream()
                .collect(Collectors.groupingBy((tac) -> RetailTrend.dateFormat.format(tac.getDate())))
                .entrySet().stream()
                .map(e -> getWeighedRetailTrend(groupByTown(e.getValue())))
                .sorted(Comparator.comparing(RetailTrend::getDate))
                .collect(Collectors.toList());
    }

    private static List<TradeAtCity> groupByTown(final List<TradeAtCity> list) {
        //проверяем что для одного продукта в одном городе только одна запись на дату
        return list.stream()
                .collect(Collectors.groupingBy(TradeAtCity::getTownId))
                .entrySet().stream()
                .map(e -> e.getValue().stream()
                        .reduce((f1, f2) -> {
                            //logger.info("reduce, productID = {}, town = {}, date = {}", f1.getProductId(), f1.getTownCaption(), f1.getDate());
                            if (f1.getVolume() > f2.getVolume()) {
                                return f1;
                            } else {
                                return f2;
                            }
                        }))
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static RetailTrend getWeighedRetailTrend(final List<TradeAtCity> tradeAtCityList) {
        //данные по одному продукту на одну дату
        final Date date = tradeAtCityList.get(0).getDate();
        final double volume = tradeAtCityList.stream().mapToDouble(TradeAtCity::getVolume).sum();
        //=SUMPRODUCT(A2:A3,B2:B3)/SUM(B2:B3)
        final double localPrice = tradeAtCityList.stream()
                .mapToDouble(tac -> tac.getLocalPrice() * tac.getVolume() / volume).sum();
        final double localQuality = tradeAtCityList.stream()
                .mapToDouble(tac -> tac.getLocalQuality() * tac.getVolume() / volume).sum();
        final double shopPrice = tradeAtCityList.stream()
                .mapToDouble(tac -> tac.getShopPrice() * tac.getVolume() / volume).sum();
        final double shopQuality = tradeAtCityList.stream()
                .mapToDouble(tac -> tac.getShopQuality() * tac.getVolume() / volume).sum();

        return new RetailTrend(
                Utils.round2(localPrice),
                Utils.round2(localQuality),
                Utils.round2(shopPrice),
                Utils.round2(shopQuality),
                date,
                volume
        );
    }

    public static void updateAllProductRemainTrends(final String realm) throws IOException, GitAPIException {
        final String baseDir = GitHubPublisher.localPath + Wizard.industry + File.separator + realm + File.separator;
        final Set<ProductRemain> set = RetailSalePrediction.getAllProductRemains(PRODUCT_REMAINS_, realm);
        logger.info("updateAllProductRemainTrends.size() = {}, {}", set.size(), realm);

        //группируем аналитику по товарам и сохраняем
        final Map<String, List<ProductRemain>> productRemainByProduct = set.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(ProductRemain::getProductID));

        for (final Map.Entry<String, List<ProductRemain>> entry : productRemainByProduct.entrySet()) {
            final String fileNamePath = baseDir + Wizard.product_remains_trends + File.separator + entry.getKey() + ".json";
            Utils.writeToGsonZip(fileNamePath, getProductRemainTrends(entry.getValue()));
        }
        logger.info("запоминаем дату обновления данных");
        final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Utils.writeToGson(baseDir + Wizard.product_remains_trends + File.separator + "updateDate.json", new UpdateDate(df.format(new Date())));
    }

    private static List<ProductRemainTrend> getProductRemainTrends(final List<ProductRemain> list) {
        return list.stream()
                .collect(Collectors.groupingBy((pr) -> RetailTrend.dateFormat.format(pr.getDate())))
                .entrySet().stream()
                .map(e -> getWeighedProductRemainTrend(groupByUnit(e.getValue())))
                .sorted(Comparator.comparing(ProductRemainTrend::getDate))
                .collect(Collectors.toList());
    }

    private static List<ProductRemain> groupByUnit(final List<ProductRemain> list) {
        //проверяем что для одного продукта в одном подразделении только одна запись на дату
        return list.stream()
                .collect(Collectors.groupingBy(ProductRemain::getUnitID))
                .entrySet().stream()
                .map(e -> e.getValue().stream()
                        .reduce((f1, f2) -> {
                            if (f1.getRemain() > f2.getRemain()) {
                                return f1;
                            } else {
                                return f2;
                            }
                        }))
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static ProductRemainTrend getWeighedProductRemainTrend(final List<ProductRemain> productRemain) {
        //данные по одному продукту на одну дату
        final Date date = productRemain.get(0).getDate();
        final List<ProductRemain> productRemainFiltered = productRemain.stream()
                .filter(pr -> pr.getRemain() > 0)
                .filter(pr -> pr.getRemain() != Long.MAX_VALUE)
                .collect(Collectors.toList());
        ;
        final double remain = productRemainFiltered.stream()
                .mapToDouble(ProductRemain::getRemainByMaxOrderType)
                .sum();
        //=SUMPRODUCT(A2:A3,B2:B3)/SUM(B2:B3)
        final double quality = productRemainFiltered.stream()
                .mapToDouble(pr -> pr.getQuality() * pr.getRemainByMaxOrderType() / remain)
                .sum();
        final double price = productRemainFiltered.stream()
                .mapToDouble(pr -> pr.getPrice() * pr.getRemainByMaxOrderType() / remain)
                .sum();

        //меньше 5% общего объема группируем в одну запись
        final List<ProductRemain> productRemainOthersFiltered = productRemainFiltered.stream()
                .filter(pr -> pr.getRemainByMaxOrderType() <= remain * 0.05)
                .collect(Collectors.toList());
        ;
        final double totalOthers = productRemainOthersFiltered.stream()
                .mapToDouble(ProductRemain::getTotal)
                .sum();
        final double remainOthers = productRemainOthersFiltered.stream()
                .mapToDouble(ProductRemain::getRemainByMaxOrderType)
                .sum();
        final double qualityOthers = productRemainOthersFiltered.stream()
                .mapToDouble(pr -> pr.getQuality() * pr.getRemainByMaxOrderType() / remainOthers)
                .sum();
        final double priceOthers = productRemainOthersFiltered.stream()
                .mapToDouble(pr -> pr.getPrice() * pr.getRemainByMaxOrderType() / remainOthers)
                .sum();
        final ProductRemainTrendSup others = new ProductRemainTrendSup("", ""
                , totalOthers, remainOthers
                , Utils.round2(qualityOthers), Utils.round2(priceOthers)
                , ProductRemain.MaxOrderType.L, remainOthers);

        final List<ProductRemainTrendSup> sup = productRemainFiltered.stream()
                .filter(pr -> pr.getRemainByMaxOrderType() > remain * 0.05)
                .map(pr -> new ProductRemainTrendSup(
                        pr.getCompanyName()
                        , pr.getUnitID()
                        , pr.getTotal()
                        , pr.getRemain()
                        , pr.getQuality()
                        , pr.getPrice()
                        , pr.getMaxOrderType()
                        , pr.getRemainByMaxOrderType()
                ))
                .collect(Collectors.toList());
        sup.add(others);

        return new ProductRemainTrend(
                remain,
                Utils.round2(quality),
                Utils.round2(price),
                date,
                sup
        );
    }
}
