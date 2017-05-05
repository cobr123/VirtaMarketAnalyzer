package ru.VirtaMarketAnalyzer.parser;

import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by cobr123 on 20.03.16.
 */
final public class TechMarketAskParser {
    private static final Logger logger = LoggerFactory.getLogger(TechMarketAskParser.class);
    private static final Pattern tech_lvl_pattern = Pattern.compile("/globalreport/technology/(\\d+)/(\\d+)/target_market_summary/");

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));

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
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String dateStr = df.format(new Date());

        final String url1 = host + realm + "/main/globalreport/technology_target_market/total";
        final List<TechLicenseLvl> techIdAsks = getAskTechLicense(url1);
//        logger.info(Utils.getPrettyGson(techIdAsks));
        logger.info("techIdAsks.size() = {}, realm = {}", techIdAsks.size(), realm);

        final List<TechLicenseLvl> licenseAskWoBid = new ArrayList<>();
        for (final TechLicenseLvl techIdAsk : techIdAsks) {
            //https://virtonomica.ru/olga/main/globalreport/technology/2427/31/target_market_summary/2016-03-21/ask
            final String url2 = host + realm + "/main/globalreport/technology/" + techIdAsk.getTechId() + "/" + techIdAsk.getLvl() + "/target_market_summary/" + dateStr + "/ask";
//            logger.info("url2 = {}", url2);
            final List<TechLicenseAskBid> techAsks = getTechLicenseAskBids(url2);
//            logger.info(Utils.getPrettyGson(techAsks));
//            logger.info("techAsks.size() = {}", techAsks.size());

            //https://virtonomica.ru/olga/main/globalreport/technology/2427/31/target_market_summary/2016-03-21/bid
            final String url3 = host + realm + "/main/globalreport/technology/" + techIdAsk.getTechId() + "/" + techIdAsk.getLvl() + "/target_market_summary/" + dateStr + "/bid";
//            logger.info("url3 = {}", url3);
            final List<TechLicenseAskBid> techBids = getTechLicenseAskBids(url3);
//            logger.info(Utils.getPrettyGson(techBids));
//            logger.info("techBids.size() = {}", techBids.size());
            if (techAsks.size() > 0 && techBids.size() == 0) {
                licenseAskWoBid.add(new TechLicenseLvl(techIdAsk, Collections.emptyList()));
            } else {
                final List<TechLicenseAskBid> tmp = getAskWoBid(techAsks, techBids);
                if (tmp.size() > 0) {
                    licenseAskWoBid.add(new TechLicenseLvl(techIdAsk, tmp));
                }
            }
//            throw new IOException("test");
        }
        logger.info("licenseAskWoBid.size() = {}, realm = {}", licenseAskWoBid.size(), realm);
        return licenseAskWoBid;
    }

    private static List<TechLicenseAskBid> getAskWoBid(final List<TechLicenseAskBid> asks, final List<TechLicenseAskBid> bids) {
        //найти спрос без предложения (для цены спроса нет такой же или меньше цены предложения )
        //найти спрос без достаточного предложения (для количества спроса нет такого же или больше количества предложения с такой же или меньшей ценой)
        return asks.stream()
                .map(ask -> {
                    final int bidQtySum = bids.stream().filter(bid -> ask.getPrice() >= bid.getPrice()).mapToInt(TechLicenseAskBid::getQuantity).sum();
                    final boolean bidWithGreaterPriceExist = bids.stream().filter(bid -> ask.getPrice() >= bid.getPrice()).findAny().isPresent();
                    if (ask.getQuantity() > bidQtySum) {
                        return new TechLicenseAskBid(ask.getPrice(), ask.getQuantity() - bidQtySum);
                    } else if (!bidWithGreaterPriceExist) {
                        return ask;
                    }
                    return null;
                })
                .filter(ask -> ask != null)
                .collect(Collectors.toList());
    }

    private static List<TechLicenseAskBid> getTechLicenseAskBids(final String url) throws IOException {
        final int maxTryCnt = 3;
        for (int tryCnt = 1; tryCnt <= maxTryCnt; ++tryCnt) {
            final Document doc = Downloader.getDoc(url);
            //итоги
            final Element table = doc.select("table.list").first();
            if (table == null) {
                Downloader.invalidateCache(url);
                logger.error("На странице '" + url + "' не найдена таблица с классом list");
                Downloader.waitSecond(3);
                continue;
            }
            //сами ставки\предложения
            final Elements priceAndQty = doc.select("table.list > tbody > tr[class]");

            return priceAndQty.stream().map(paq -> {
                final double price = Utils.toDouble(paq.select("> td:eq(0)").text());
                final int quantity = Utils.toInt(paq.select("> td:eq(1)").text());
                return new TechLicenseAskBid(price, quantity);
            }).collect(toList());
        }
        return null;
    }

    private static List<TechLicenseLvl> getAskTechLicense(final String url) throws IOException {
        final int maxTryCnt = 3;
//        logger.info(url);
//        Downloader.invalidateCache(url);
        for (int tryCnt = 1; tryCnt <= maxTryCnt; ++tryCnt) {
            final Document doc = Downloader.getDoc(url);
            final Element table = doc.select("table.list").first();
            if (table == null) {
                Downloader.invalidateCache(url);
                logger.error("На странице '" + url + "' не найдена таблица с классом list");
                Downloader.waitSecond(3);
                continue;
            }
            final Element footer = doc.select("div.metro_footer").first();
            if (footer == null) {
                Downloader.invalidateCache(url);
                logger.error("На странице '" + url + "' не найден div.metro_footer");
                Downloader.waitSecond(3);
                continue;
            }
            final Elements asks = doc.select("table.list > tbody > tr > td > a:not(:contains(--))");

            //https://virtonomica.ru/olga/main/globalreport/technology/2423/16/target_market_summary/21-03-2016/ask
            return asks.stream().map(ask -> {
                final Matcher matcher = tech_lvl_pattern.matcher(ask.attr("href"));
                if (matcher.find()) {
                    final String techID = matcher.group(1);
                    final int lvl = Utils.toInt(matcher.group(2));
                    return new TechLicenseLvl(techID, lvl);
                }
                return null;
            }).collect(toList());
        }
        return null;
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
                .collect(toList());
    }

    public static List<TechLvl> getTech(final String host, final String realm, final String unit_type_id) throws IOException {
        final String url = host + "api/" + realm + "/main/technology/report/unittype?unit_type_id=" + unit_type_id + "&format=json&wrap=0";
        final String fileToSave = Utils.getDir() + Downloader.getCrearedUrl(host + "api/" + realm + "/main/technology/report/unittype/", null) + unit_type_id + ".json";
        //logger.info(fileToSave);
        FileUtils.copyURLToFile(new URL(url), new File(fileToSave));

        final TechReport[] arr = new GsonBuilder().create().fromJson(Utils.readFile(fileToSave), TechReport[].class);

        return Stream.of(arr)
                .map(row -> new TechLvl(row.getUnitTypeID(), row.getLevel(), row.getPrice()))
                .collect(toList());
    }
}
