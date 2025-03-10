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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by cobr123 on 20.03.16.
 */
final public class TechMarketAskParser {
    private static final Logger logger = LoggerFactory.getLogger(TechMarketAskParser.class);
    private static final Pattern tech_lvl_pattern = Pattern.compile("/globalreport/technology/(\\d+)/(\\d+)/target_market_summary/");

    public static void main(String[] args) throws IOException {
        final String realm = "olga";
        final List<TechLicenseLvl> askWoBidTechLvl = getLicenseAskWoBid(Wizard.host, realm);
//        logger.info(Utils.getPrettyGson(askWoBidTechLvl));
        for (final TechLicenseLvl tl : askWoBidTechLvl) {
            //https://virtonomica.ru/olga/main/globalreport/technology/2423/10/target_market_summary/21-03-2016/ask
            if (tl.getTechId().equals("2423") && tl.getLvl() == 10) {
                logger.info(Utils.getPrettyGson(tl));
            }
            //https://virtonomica.ru/olga/main/globalreport/technology/423140/2/target_market_summary/21-03-2016/ask
            if (tl.getTechId().equals("423140") && tl.getLvl() == 2) {
                logger.info(Utils.getPrettyGson(tl));
            }
            //https://virtonomica.ru/olga/main/globalreport/technology/1906/7/target_market_summary/21-03-2016/ask
            if (tl.getTechId().equals("1906") && tl.getLvl() == 7) {
                logger.info(Utils.getPrettyGson(tl));
            }
            break;
        }
        logger.info("askWoBidTechLvl.size() = {}", askWoBidTechLvl.size());
        final List<TechUnitType> techList = TechListParser.getTechUnitTypes(Wizard.host, realm);
        logger.info("techList.size() = {}", techList.size());
        System.out.println(Utils.getPrettyGson(getTech(Wizard.host, realm, techList)));
    }

    public static List<TechLicenseLvl> getLicenseAskWoBid(final String host, final String realm) throws IOException {
        final List<TechLicenseOffer> techLicenseOffers = getTechLicenseOffers(host, realm);
//        logger.info(Utils.getPrettyGson(techLicenseOffers));
        logger.info("techLicenseOffers.size() = {}, realm = {}", techLicenseOffers.size(), realm);

        final List<TechLicenseLvl> licenseAskWoBid = techLicenseOffers.stream()
                .filter(o -> o.getAskCount() > 0)
                .map(o -> {
                    try {
                        final List<TechLicenseLvlPrice> prices = getLicenseLvlPrices(host, realm, o.getUnitTypeID(), o.getLevel());
                        final List<TechLicenseAskBid> techAsks = prices.stream().filter(p -> p.getAskCount() > 0).map(p -> new TechLicenseAskBid(p.getPrice(), p.getAskCount())).collect(Collectors.toList());
                        final List<TechLicenseAskBid> techBids = prices.stream().filter(p -> p.getBidCount() > 0).map(p -> new TechLicenseAskBid(p.getPrice(), p.getBidCount())).collect(Collectors.toList());
                        if (!techAsks.isEmpty() && techBids.isEmpty()) {
                            return new TechLicenseLvl(o.getUnitTypeID(), o.getLevel(), Collections.emptyList());
                        } else {
                            final List<TechLicenseAskBid> tmp = getAskWoBid(techAsks, techBids);
                            if (!tmp.isEmpty()) {
                                return new TechLicenseLvl(o.getUnitTypeID(), o.getLevel(), tmp);
                            } else {
                                return null;
                            }
                        }
                    } catch (final IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        logger.info("licenseAskWoBid.size() = {}, realm = {}", licenseAskWoBid.size(), realm);
        return licenseAskWoBid;
    }

    private static List<TechLicenseLvlPrice> getLicenseLvlPrices(final String host, final String realm, final String unitTypeID, final int level) throws IOException {
        final String url = host + "api/" + realm + "/main/technology/license/stat?unit_type=" + unitTypeID + "&level=" + level + "&format=json";
        try {
            final String json = Downloader.getJson(url);
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<TechLicenseLvlPrice[]>() {
            }.getType();
            final TechLicenseLvlPrice[] arr = gson.fromJson(json, mapType);

            return Stream.of(arr)
                    .collect(Collectors.toList());
        } catch (final IOException e) {
            logger.error("url = {}", url);
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    private static List<TechLicenseAskBid> getAskWoBid(final List<TechLicenseAskBid> asks, final List<TechLicenseAskBid> bids) {
        //найти спрос без предложения (для цены спроса нет такой же или меньше цены предложения )
        //найти спрос без достаточного предложения (для количества спроса нет такого же или больше количества предложения с такой же или меньшей ценой)
        return asks.stream()
                .map(ask -> {
                    final int bidQtySum = bids.stream().filter(bid -> ask.getPrice() >= bid.getPrice()).mapToInt(TechLicenseAskBid::getQuantity).sum();
                    final boolean bidWithGreaterPriceExist = bids.stream().anyMatch(bid -> ask.getPrice() >= bid.getPrice());
                    if (ask.getQuantity() > bidQtySum) {
                        return new TechLicenseAskBid(ask.getPrice(), ask.getQuantity() - bidQtySum);
                    } else if (!bidWithGreaterPriceExist) {
                        return ask;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<TechLicenseOffer> getTechLicenseOffers(final String host, final String realm) throws IOException {
        final String url = host + "api/" + realm + "/main/technology/license/offers?type=ask&format=json&wrap=0";
        try {
            final String json = Downloader.getJson(url);
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<TechLicenseOffer[]>() {
            }.getType();
            final TechLicenseOffer[] arr = gson.fromJson(json, mapType);

            return Stream.of(arr)
                    .collect(Collectors.toList());
        } catch (final IOException e) {
            logger.error("url = {}", url);
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    public static List<TechLvl> getTech(final String host, final String realm, final List<TechUnitType> techList) throws IOException {
        return techList.stream()
                .map(tl -> {
                    try {
                        return getTech(host, realm, tl.getId());
                    } catch (final Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public static List<TechLvl> getTech(final String host, final String realm, final String unit_type_id) throws IOException {
        final String url = host + "api/" + realm + "/main/unittype/technologies?app=virtonomica&format=json&ajax=1&id=" + unit_type_id + "&wrap=0";
        try {
            final String json = Downloader.getJson(url);
            final Gson gson = new Gson();
            final Type mapType = new TypeToken<TechReport[]>() {
            }.getType();
            final TechReport[] arr = gson.fromJson(json, mapType);

            return Stream.of(arr)
                    .map(row -> new TechLvl(row.getUnitTypeID(), row.getLevel(), row.getPrice()))
                    .collect(Collectors.toList());
        } catch (final IOException e) {
            logger.error("url = {}", url);
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }
}
