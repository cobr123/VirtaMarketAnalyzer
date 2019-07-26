package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 26.02.16.
 */
public final class ServiceAtCityParser {
    private static final Logger logger = LoggerFactory.getLogger(ServiceAtCityParser.class);

    private static ServiceSpecRetail addRetailStatByProduct(final String host, final String realm, final String productID, final City city) {
        try {
            final TradeAtCity tac = CityParser.get(Wizard.host, realm, city, ProductInitParser.getTradingProduct(host, realm, productID)).build();
            return new ServiceSpecRetail(tac.getLocalPrice(), tac.getLocalQuality(), tac.getShopPrice(), tac.getShopQuality());
        } catch (final Exception e) {
            logger.error("Ошибка: ", e);
        }
        return null;
    }

    private static Map<String, ServiceSpecRetail> getRetailStat(final String host, final String realm
            , final UnitTypeSpec spec
            , final City city
            , final Set<String> tradingProductsId
    ) {
        final Map<String, ServiceSpecRetail> stat = new HashMap<>();
        if (tradingProductsId.contains(spec.getEquipment().getId())) {
            stat.put(spec.getEquipment().getId(), addRetailStatByProduct(host, realm, spec.getEquipment().getId(), city));
        }

        spec.getRawMaterials()
                .stream()
                .forEach(mat -> {
                    if (tradingProductsId.contains(mat.getId())) {
                        stat.put(mat.getId(), addRetailStatByProduct(host, realm, mat.getId(), city));
                    } else {
                        //на реалме fast в рознице нет товара 'Прохладительные напитки', а в расходниках сервиса есть
                        stat.put(mat.getId(), new ServiceSpecRetail(0, 0, 0, 0));
//                        logger.error("tradingProductsId не содержит id '" + mat.getId() + "', realm = " + realm);
                    }
                });
        return stat;
    }

    private static ServiceSpecRetail calcBySpec(final String realm, final UnitTypeSpec spec, final Map<String, ServiceSpecRetail> stat) {
        try {
//            logger.info("spec.getRawMaterials().size() = {}", spec.getRawMaterials().size());
//            logger.info("stat.containsKey(spec.getEquipment()) = {}", stat.containsKey(spec.getEquipment().getId()));
            if (spec.getRawMaterials().size() > 0) {
                final double localPrice = spec.getRawMaterials()
                        .stream()
                        .mapToDouble(mat -> stat.get(mat.getId()).getLocalPrice() * mat.getQuantity())
                        .sum();
                final double localQuality = spec.getRawMaterials()
                        .stream()
                        .mapToDouble(mat -> stat.get(mat.getId()).getLocalQuality())
                        .average().orElse(0);
                final double shopPrice = spec.getRawMaterials()
                        .stream()
                        .mapToDouble(mat -> stat.get(mat.getId()).getShopPrice() * mat.getQuantity())
                        .sum();
                final double shopQuality = spec.getRawMaterials()
                        .stream()
                        .mapToDouble(mat -> stat.get(mat.getId()).getShopQuality())
                        .average().orElse(0);
                return new ServiceSpecRetail(localPrice, localQuality, shopPrice, shopQuality);
            } else if (stat.containsKey(spec.getEquipment().getId())) {
                final ServiceSpecRetail serviceSpecRetail = stat.get(spec.getEquipment().getId());
                final double localPrice = serviceSpecRetail.getLocalPrice();
                final double localQuality = serviceSpecRetail.getLocalQuality();
                final double shopPrice = serviceSpecRetail.getShopPrice();
                final double shopQuality = serviceSpecRetail.getShopQuality();
                return new ServiceSpecRetail(localPrice, localQuality, shopPrice, shopQuality);
            }
        } catch (final Exception e) {
            logger.error("stat.size() = " + stat.size());
            for (final RawMaterial mat : spec.getRawMaterials()) {
                logger.error("('" + mat.getCaption() + "', stat.containsKey('" + mat.getId() + "') = " + stat.containsKey(mat.getId()));
            }
            logger.error("spec.getCaption() = " + spec.getCaption() + ", spec.getRawMaterials().size() = " + spec.getRawMaterials().size() + ", realm = " + realm, e);
        }
        return new ServiceSpecRetail(0, 0, 0, 0);
    }

    public static ServiceAtCity get(
            final String host
            , final String realm
            , final City city
            , final UnitType service
            , final List<Region> regions
            , final Set<String> tradingProductsId
            , final List<RentAtCity> rents
    ) throws IOException {
        final ServiceMetrics serviceMetrics = getServiceMetrics(host, realm, city, service);
        if (serviceMetrics == null) {
            return null;
        }
        final Map<String, Double> percentBySpec = getPercentBySpec(host, realm, city, service);

        final double incomeTaxRate = (regions == null) ? 0 : regions.stream().filter(r -> r.getId().equals(city.getRegionId())).findFirst().get().getIncomeTaxRate();

        final Map<String, Map<String, ServiceSpecRetail>> retailBySpec = new HashMap<>();
        final Map<String, ServiceSpecRetail> retailCalcBySpec = new HashMap<>();
        service.getSpecializations().forEach(spec -> {
            final Map<String, ServiceSpecRetail> stat = getRetailStat(host, realm, spec, city, tradingProductsId);
            retailCalcBySpec.put(spec.getCaption(), calcBySpec(realm, spec, stat));
            //в retailBySpec только расходники, оборудование не включаем
            stat.remove(spec.getEquipment().getId());
            retailBySpec.put(spec.getCaption(), stat);
        });

        final double areaRent = rents.stream()
                .filter(r -> r.getCityId().equalsIgnoreCase(city.getId()))
                .filter(r -> r.getUnitTypeImgSrc().equalsIgnoreCase(service.getUnitTypeImgSrc()))
                .findAny()
                .get()
                .getAreaRent();

        return new ServiceAtCity(city.getCountryId()
                , city.getRegionId()
                , city.getId()
                , serviceMetrics.getSales()
                , serviceMetrics.getPrice()
                , serviceMetrics.getUnitCount()
                , serviceMetrics.getCompanyCount()
                , Utils.round2(serviceMetrics.getRevenuePerRetail() * 100.0)
                , percentBySpec
                , city.getWealthIndex()
                , incomeTaxRate
                , retailBySpec
                , retailCalcBySpec
                , areaRent
        );
    }

    public static List<ServiceAtCity> get(
            final String host
            , final String realm
            , final List<City> cities
            , final UnitType service
            , final List<Region> regions
            , final List<RentAtCity> rents
    ) throws IOException {
        //получаем список доступных розничных товаров
        final Set<String> tradingProductsId = ProductInitParser.getTradingProducts(host, realm).stream().map(Product::getId).collect(Collectors.toSet());

        return cities.parallelStream().map(city -> {
            try {
                return get(host, realm, city, service, regions, tradingProductsId, rents);
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static ServiceMetrics getServiceMetrics(final String host, final String realm, final City city, final UnitType service) throws IOException {
        //TODO: &produce_id=422835
        final String url = host + "api/" + realm + "/main/marketing/report/service/metrics?geo=" + city.getGeo() + "&unit_type_id=" + service.getId();
        try {
            final String json = Downloader.getJson(url);
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Object>>() {
            }.getType();
            final Map<String, Object> mapOfMetrics = gson.fromJson(json, mapType);

            final String name = mapOfMetrics.get("name").toString();
            final String symbol = mapOfMetrics.get("symbol").toString();
            final String specialization = mapOfMetrics.get("specialization").toString();

            if (mapOfMetrics.get("turn_id") == null) {
                logger.trace("Не найден turn_id. {}&format=debug", url);
                logger.trace("{}{}/main/globalreport/marketing?geo={}&unit_type_id={}#by-service", host, realm, city.getGeo(), service.getId());
                return new ServiceMetrics("", 0, 0, 0, 0, 0, name, symbol, specialization);
            } else {
                final String turnId = mapOfMetrics.get("turn_id").toString();
                final double price = Double.parseDouble(mapOfMetrics.get("price").toString());
                final long sales = Long.parseLong(mapOfMetrics.get("sales").toString());
                final int unitCount = Integer.parseInt(mapOfMetrics.get("unit_count").toString());
                final int companyCount = Integer.parseInt(mapOfMetrics.get("company_count").toString());
                final double revenuePerRetail = Double.parseDouble(mapOfMetrics.get("revenue_per_retail").toString());

                return new ServiceMetrics(turnId, price, sales, unitCount, companyCount, revenuePerRetail, name, symbol, specialization);
            }
        } catch (final Exception e) {
            logger.error(url + "&format=debug");
            throw e;
        }
    }

    private static Map<String, Double> getPercentBySpec(final String host, final String realm, final City city, final UnitType service) throws IOException {
        final Map<String, Double> percentBySpec = new HashMap<>();
        //TODO: &produce_id=422835
        final String url = host + "api/" + realm + "/main/marketing/report/service/specializations?geo=" + city.getGeo() + "&unit_type_id=" + service.getId();
        try {
            final String json = Downloader.getJson(url);
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {
            }.getType();
            final Map<String, Map<String, Object>> mapOfMetrics = gson.fromJson(json, mapType);

            for (final String idx : mapOfMetrics.keySet()) {
                final Map<String, Object> metrics = mapOfMetrics.get(idx);

                final String caption = metrics.get("name").toString();
                final double marketPerc = Double.parseDouble(metrics.get("market_size").toString());

                percentBySpec.put(caption, marketPerc);
            }
        } catch (final Exception e) {
            logger.error(url + "&format=debug");
            throw e;
        }
        return percentBySpec;
    }
}
