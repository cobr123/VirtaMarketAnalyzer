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

/**
 * Created by cobr123 on 20.03.16.
 */
final public class TechMarketAskParser {
    private static final Logger logger = LoggerFactory.getLogger(TechMarketAskParser.class);

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));

        final DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        final String dateStr = df.format(new Date());
        final String realm = "olga";
        final String url = Wizard.host + realm + "/main/globalreport/technology_target_market/total/" + dateStr + "/ask";
        final List<TechLvl> techIdAsks = getAskTech(url);
        logger.info(Utils.getPrettyGson(asks));
    }

    private static List<TechLvl> getAskTech(final String url) throws IOException {
        final Document doc = Downloader.getDoc(url );
        final Elements asks = doc.select("table.list > tbody > tr > td > a[text!=\"--\"]");

        return asks.stream().map(ask -> ask.attr('href'))
    }
}
