package ru.VirtaMarketAnalyzer.parser;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        final DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        final String dateStr = df.format(new Date());
        final String realm = "olga";
        final String url = Wizard.host + realm + "/main/globalreport/technology_target_market/total/" + dateStr + "/ask";
        final List<TechLvl> techIdAsks = getAskTech(url);
        logger.info(Utils.getPrettyGson(techIdAsks));
        logger.info("techIdAsks.size() = {}", techIdAsks.size());
    }

    private static List<TechLvl> getAskTech(final String url) throws IOException {
        final Document doc = Downloader.getDoc(url);
        final Elements asks = doc.select("table.list > tbody > tr > td > a:not(:contains(--))");

        //http://virtonomica.ru/olga/main/globalreport/technology/2423/16/target_market_summary/21-03-2016/ask
        return asks.stream().map(ask -> {
            final Matcher matcher = tech_lvl_pattern.matcher(ask.attr("href"));
            if (matcher.find()) {
                System.out.println();
                final String techID = matcher.group(1);
                final int lvl = Utils.toInt(matcher.group(2));
                return new TechLvl(techID, lvl);
            }
            return null;
        }).collect(toList());
    }
}
