package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.TechAskBid;
import ru.VirtaMarketAnalyzer.data.TechLvl;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.scrapper.Downloader;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * Created by cobr123 on 20.03.16.
 */
final public class TechMarketAskParser {
    private static final Logger logger = LoggerFactory.getLogger(TechMarketAskParser.class);
    private static final Pattern tech_lvl_pattern = Pattern.compile("/globalreport/technology/(\\d+)/(\\d+)/target_market_summary/");

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String dateStr = df.format(new Date());
        final String realm = "olga";
        final String url1 = Wizard.host + realm + "/main/globalreport/technology_target_market/total";
        final List<TechLvl> techIdAsks = getAskTech(url1);
//        logger.info(Utils.getPrettyGson(techIdAsks));
        logger.info("techIdAsks.size() = {}", techIdAsks.size());

        for (final TechLvl techIdAsk : techIdAsks) {
            //http://virtonomica.ru/olga/main/globalreport/technology/2427/31/target_market_summary/2016-03-21/ask
            final String url2 = Wizard.host + realm + "/main/globalreport/technology/" + techIdAsk.getTechId() + "/" + techIdAsk.getLvl() + "/target_market_summary/" + dateStr + "/ask";
//            logger.info("url2 = {}", url2);
            final List<TechAskBid> techAsks = getTechAskBids(url2);
//            logger.info(Utils.getPrettyGson(techAsks));
//            logger.info("techAsks.size() = {}", techAsks.size());

            //http://virtonomica.ru/olga/main/globalreport/technology/2427/31/target_market_summary/2016-03-21/bid
            final String url3 = Wizard.host + realm + "/main/globalreport/technology/" + techIdAsk.getTechId() + "/" + techIdAsk.getLvl() + "/target_market_summary/" + dateStr + "/bid";
//            logger.info("url3 = {}", url3);
            final List<TechAskBid> techBids = getTechAskBids(url3);
//            logger.info(Utils.getPrettyGson(techBids));
//            logger.info("techBids.size() = {}", techBids.size());
//            break;
        }
    }

    private static List<TechAskBid> getTechAskBids(final String url) throws IOException {
        final Document doc = Downloader.getDoc(url);
        final Elements priceAndQty = doc.select("table[class=list] > tbody > tr[class]");

        return priceAndQty.stream().map(paq -> {
            final double price = Utils.toDouble(paq.select("> td:eq(0)").text());
            final int quantity = Utils.toInt(paq.select("> td:eq(1)").text());
            return new TechAskBid(price, quantity);
        }).collect(toList());
    }

    private static List<TechLvl> getAskTech(final String url) throws IOException {
        final Document doc = Downloader.getDoc(url);
        final Elements asks = doc.select("table.list > tbody > tr > td > a:not(:contains(--))");

        //http://virtonomica.ru/olga/main/globalreport/technology/2423/16/target_market_summary/21-03-2016/ask
        return asks.stream().map(ask -> {
            final Matcher matcher = tech_lvl_pattern.matcher(ask.attr("href"));
            if (matcher.find()) {
                final String techID = matcher.group(1);
                final int lvl = Utils.toInt(matcher.group(2));
                return new TechLvl(techID, lvl);
            }
            return null;
        }).collect(toList());
    }
}
