package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.annotations.SerializedName;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 26.02.16.
 */
public final class ServiceAtCityParser {
    private static final Logger logger = LoggerFactory.getLogger(ServiceAtCityParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{ISO8601} [%t] %p %c %x - %m%n")));
        final String host = Wizard.host;
        final String realm = "olga";
        final City city = new City("3010", "3023", "3025", "Николаев", 10, 0, 0);
        final List<City> cities = new ArrayList<>();
        cities.add(city);

        final List<UnitTypeSpec> specializations = new ArrayList<>();
        final List<RawMaterial> rawMaterials = new ArrayList<>();
        specializations.add(new UnitTypeSpec("Фитнес", ProductInitParser.getTradingProduct(host, realm, "15337"), rawMaterials));
        final UnitType service = new UnitType("348207", "Фитнес-центр", "", specializations);
        final List<RentAtCity> rentAtCity = RentAtCityParser.getUnitTypeRent(Wizard.host, realm, cities);
        final List<ServiceAtCity> serviceAtCity = get(host, realm, cities, service, null,rentAtCity);
        logger.info(Utils.getPrettyGson(serviceAtCity));

//        final List<UnitTypeSpec> specializations = new ArrayList<>();
//        final List<RawMaterial> rawMaterials = new ArrayList<>();
//        rawMaterials.add(new RawMaterial("", "", "15742", "Картофель", 1));
//        rawMaterials.add(new RawMaterial("", "", "15747", "Масло", 1));
//        rawMaterials.add(new RawMaterial("", "", "1506", "Хлеб", 1));
//        rawMaterials.add(new RawMaterial("", "", "1490", "Мясо", 1));
//        rawMaterials.add(new RawMaterial("", "", "1503", "Прохладительные напитки", 1));
//        specializations.add(new UnitTypeSpec("Фастфуд", new Product("","","373198","Ресторанное оборудование"), rawMaterials));
//        final UnitType service = new UnitType("373265", "Ресторан", "", specializations);
//        final List<ServiceAtCity> serviceAtCity = get(Wizard.host, realm, cities, service, null);
//        logger.info(Utils.getPrettyGson(serviceAtCity));
    }

    private static ServiceSpecRetail addRetailStatByProduct(final String host, final String realm, final String productID, final City city) {
        try {
            final TradeAtCity tac = CityParser.get(Wizard.host, realm, city, ProductInitParser.getTradingProduct(host, realm, productID), null, null).build();
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
            ,final List<RentAtCity> rents
    ) throws IOException {
        final String fullUrl = host + realm + "/main/globalreport/marketing/by_service/" + service.getId() + "/" + city.getCountryId() + "/" + city.getRegionId() + "/" + city.getId();
        final Document doc = Downloader.getDoc(fullUrl);
        final Element table = doc.select("table.grid").first();

        if (table == null) {
            Downloader.invalidateCache(fullUrl);
            throw new IOException("На странице '" + fullUrl + "' не найдена таблица с классом grid");
        }

        final double marketDevelopmentIndex = Utils.toDouble(table.select(" > tbody > tr > td:nth-child(1) > b").text());
        final long volume = Utils.toLong(table.select(" > tbody > tr > td:nth-child(2) > b").text());
        final int subdivisionsCnt = Utils.toInt(table.select(" > tbody > tr > td:nth-child(3) > b").text());
        final long companiesCnt = Utils.toLong(table.select(" > tbody > tr > td:nth-child(4) > b").text());
        final double price = Utils.toDouble(table.select(" > tbody > tr > td:nth-child(5) > b").text());
        final Map<String, Double> percentBySpec = new HashMap<>();
        table.nextElementSibling().select(" > tbody > tr:nth-child(2) > td:nth-child(2) > table > tbody > tr > td:nth-child(3)").stream()
                .forEach(element -> {
                    final String key = element.text();
                    final Double val = Utils.toDouble(element.nextElementSibling().nextElementSibling().text());
                    percentBySpec.put(key, val);
                });

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
                , volume
                , price
                , subdivisionsCnt
                , companiesCnt
                , marketDevelopmentIndex
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
            ,final List<RentAtCity> rents
    ) throws IOException {
        //получаем список доступных розничных товаров
        final Set<String> tradingProductsId = ProductInitParser.getTradingProducts(host, realm).stream().map(Product::getId).collect(Collectors.toSet());

        return cities.parallelStream().map(city -> {
            try {
                return get(host, realm, city, service, regions, tradingProductsId, rents);
            } catch (final IOException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
        }).collect(Collectors.toList());
    }
}
