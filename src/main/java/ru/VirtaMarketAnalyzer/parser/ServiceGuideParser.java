package ru.VirtaMarketAnalyzer.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by cobr123 on 17.02.2019.
 */
final public class ServiceGuideParser {
    private static final Logger logger = LoggerFactory.getLogger(ServiceGuideParser.class);

    /**
     * Создает розничный гид по одной категории товаров для всех городов
     */
    public static List<ServiceGuide> genServiceGuide(
            final String host,
            final String realm,
            final UnitType serviceType
    ) throws Exception {
        final List<City> cities = CityListParser.getCities(host, realm, false);
        final List<Product> products = ProductInitParser.getServiceProducts(host, realm, serviceType);
        final Map<String, List<ProductRemain>> productRemains = ProductRemainParser.getRemains(host, realm, products);

        return cities.parallelStream()
                .map(city -> {
                    try {
                        return genServiceGuide(host, realm, city, serviceType, products, productRemains);
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Создает розничный гид по одной категории товаров для одного города.
     * Только если хотя бы один продукт прибыльный.
     */
    public static List<ServiceGuide> genServiceGuide(
            final String host,
            final String realm,
            final City city,
            final UnitType serviceType,
            final List<Product> products,
            final Map<String, List<ProductRemain>> productRemains
    ) throws Exception {
        final List<ServiceGuide> serviceGuides = new ArrayList<>();
        final Set<String> tradingProductsId = products.stream().map(Product::getId).collect(Collectors.toSet());
        final List<RentAtCity> rents = RentAtCityParser.getUnitTypeRent(host, realm, city.getId());
        final ServiceAtCity stat = ServiceAtCityParser.get(host, realm, city, serviceType, null, tradingProductsId, rents);

        for (final UnitTypeSpec unitTypeSpec : serviceType.getSpecializations()) {
            final List<ServiceGuideProduct> serviceGuideProducts = new ArrayList<>();

            for (final RawMaterial rawMaterial : unitTypeSpec.getRawMaterials()) {
                serviceGuideProducts.add(genServiceGuideProduct(
                        host, realm, rawMaterial,
                        stat, stat.getRetailBySpec().get(unitTypeSpec.getCaption()).get(rawMaterial.getId()),
                        productRemains.getOrDefault(rawMaterial.getId(), new ArrayList<>())
                ));
            }
            final ServiceGuide serviceGuide = new ServiceGuide(host, realm, unitTypeSpec.getId(), city, serviceGuideProducts, Math.round(stat.getVolume() * 0.1));
            if (serviceGuide.getIncomeAfterTaxSum() > 0) {
                serviceGuides.add(serviceGuide);
            }
        }
        return serviceGuides;
    }

    /**
     * Считает прибыльность товара для одного города.
     * Максимальный объем продаж 10% рынка.
     */
    public static ServiceGuideProduct genServiceGuideProduct(
            final String host,
            final String realm,
            final RawMaterial rawMaterial,
            final ServiceAtCity stat,
            final ServiceSpecRetail serviceSpecRetail,
            final List<ProductRemain> productRemains
    ) throws Exception {
        final List<String> suppliersUnitIds = new ArrayList<>();
        final List<ProductRemain> productRemainsFiltered = productRemains.stream()
                .filter(pr -> pr.getRemainByMaxOrderType() > 0 && pr.getQuality() >= serviceSpecRetail.getLocalQuality())
                .sorted(Comparator.comparingDouble(o -> o.getPrice() / o.getQuality()))
                .collect(Collectors.toList());
        final long maxVolume = Math.round(stat.getVolume() * 0.1 * rawMaterial.getQuantity());
        double quality = 0;
        double buyPrice = 0;
        long volume = 0;
        for (final ProductRemain pr : productRemainsFiltered) {
            suppliersUnitIds.add(pr.getUnitID());
            final double maxProductRemainVolume = Math.min(pr.getRemainByMaxOrderType(), maxVolume - volume);
            final double priceWithDuty = CountryDutyListParser.addDuty(host, realm, pr.getCountryId(), stat.getCountryId(), pr.getProductID(), pr.getPrice());
            final double transportCost = Utils.repeatOnErr(() -> CountryDutyListParser.getTransportCost(host, realm, pr.getTownId(), stat.getTownId(), pr.getProductID()));
            quality = merge(quality, volume, pr.getQuality(), maxProductRemainVolume);
            buyPrice = merge(buyPrice, volume, priceWithDuty + transportCost, maxProductRemainVolume);
            volume += maxProductRemainVolume;
            if (volume >= maxVolume) {
                break;
            }
        }
        double sellPrice = serviceSpecRetail.getLocalPrice();
        if (quality - 30.0 > serviceSpecRetail.getLocalQuality()) {
            sellPrice = Utils.round2(serviceSpecRetail.getLocalPrice() * 2.5);
        } else if (quality - 20.0 > serviceSpecRetail.getLocalQuality()) {
            sellPrice = Utils.round2(serviceSpecRetail.getLocalPrice() * 2.0);
        } else if (quality - 10.0 > serviceSpecRetail.getLocalQuality()) {
            sellPrice = Utils.round2(serviceSpecRetail.getLocalPrice() * 1.5);
        }
        return new ServiceGuideProduct(rawMaterial.getId(), rawMaterial.getQuantity(), quality, buyPrice, sellPrice, volume, suppliersUnitIds);
    }

    private static double merge(double quality1, double volume1, double quality2, double volume2) {
        return Utils.round2((quality1 * volume1) / (volume1 + volume2) + (quality2 * volume2) / (volume1 + volume2));
    }

}
