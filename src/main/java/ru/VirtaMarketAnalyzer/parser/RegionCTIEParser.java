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
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by cobr123 on 16.03.2016.
 */
final public class RegionCTIEParser {
    private static final Logger logger = LoggerFactory.getLogger(RegionCTIEParser.class);

    public static void main(final String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        final String url = "http://virtonomics.com/olga/main/geo/regionENVD/";
        final List<Region> regions = new ArrayList<>();
        regions.add(new Region("2931", "2961", "Far East", 30));
        final List<Product> materials = ProductInitParser.getProducts(Wizard.host + "olga" + "/main/common/main_page/game_info/products/");
        logger.info(Utils.getPrettyGson(materials));
        final Map<String, List<RegionCTIE>> allRegionsCTIEList = getAllRegionsCTIEList(url, regions, materials);
        logger.info(Utils.getPrettyGson(allRegionsCTIEList));
        logger.info("\n" + materials.size() + " = " + allRegionsCTIEList.get("2961").size());
    }

    public static Map<String, List<RegionCTIE>> getAllRegionsCTIEList(final String url, final List<Region> regions, final List<Product> materials) throws IOException {
        return regions.stream().map(region -> {
            try {
                return getRegionCTIEList(url, region, materials);
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
        })
                .flatMap(Collection::stream)
                .collect(groupingBy(RegionCTIE::getRegionId));
    }

    public static List<RegionCTIE> getRegionCTIEList(final String url, final Region region, final List<Product> materials) throws IOException {
        final Document doc = Downloader.getDoc(url + region.getId());
        final Elements imgElems = doc.select("table.list > tbody > tr > td > img");
        return imgElems.stream().map(el -> {
            try {
                return getRegionCTIEList(el, region, materials);
            } catch (Exception e) {
                logger.info(url + region.getId());
                logger.error(e.getLocalizedMessage(), e);
            }
            return null;
        }).collect(Collectors.toList());
    }

    public static RegionCTIE getRegionCTIEList(final Element elem, final Region region, final List<Product> materials) throws Exception {
        final Optional<Product> product = materials.stream().filter(p -> p.getImgUrl().equalsIgnoreCase(elem.attr("src"))).findFirst();
        if (!product.isPresent()) {
            throw new Exception("Не найден продукт с изображением '" + elem.attr("src") + "'");
        }
        final String productId = product.get().getId();
        elem.parent().nextElementSibling().nextElementSibling().children().remove();
        final int rate = Utils.toInt(elem.parent().nextElementSibling().nextElementSibling().text());
        return new RegionCTIE(region.getId(), productId, rate);
    }
}
