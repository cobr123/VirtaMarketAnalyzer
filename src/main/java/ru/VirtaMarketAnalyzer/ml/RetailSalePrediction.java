package ru.VirtaMarketAnalyzer.ml;

import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.*;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.parser.ProductInitParser;
import ru.VirtaMarketAnalyzer.publish.GitHubPublisher;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created by cobr123 on 15.01.2016.
 */
public final class RetailSalePrediction {
    private static final Logger logger = LoggerFactory.getLogger(RetailSalePrediction.class);

    public static final String predict_retail_sales = "predict_retail_sales";

    public static final String[] numbers = new String[]{
            "100", "200", "300", "500",
            "1 000", "2 000", "3 000", "5 000",
            "10 000", "20 000", "30 000", "50 000",
            "100 000", "200 000", "300 000", "500 000",
            "1 000 000", "2 000 000", "3 000 000", "5 000 000",
            "10 000 000", "20 000 000", "30 000 000", "50 000 000",
            "100 000 000", "200 000 000", "300 000 000", "500 000 000",
            "1 000 000 000", "2 000 000 000", "3 000 000 000", "5 000 000 000"
    };
    public static final String[] words = new String[]{"около", "более"};
    public static final String RETAIL_ANALYTICS_ = "retail_analytics_";
    public static final String TRADE_AT_CITY_ = "tradeAtCity_";
    public static final String PRODUCT_REMAINS_ = "product_remains_";
    public static final String RETAIL_ANALYTICS_HIST = "retail_analytics_hist";
    public static final String WEKA = "weka";

    public enum ATTR {
        WEALTH_INDEX, EDUCATION_INDEX, AVERAGE_SALARY,
        MARKET_INDEX, MARKET_VOLUME, LOCAL_PERCENT,
        LOCAL_PRICE, LOCAL_QUALITY,
        SHOP_SIZE, TOWN_DISTRICT, DEPARTMENT_COUNT,
        BRAND, QUALITY, NOTORIETY, VISITORS_COUNT,
        SERVICE_LEVEL, SELLER_COUNT, PRODUCT_ID, //PRODUCT_CATEGORY,
        SELL_VOLUME_NUMBER, DEMOGRAPHY,
        //последний для автоподстановки при открытии в weka
        PRICE;

        public String getFunctionName() {
            final StringBuilder sb = new StringBuilder();
            sb.append("get");
            boolean capitalize = true;
            for (int i = 0; i < this.name().length(); ++i) {
                if (this.name().charAt(i) == '_') {
                    capitalize = true;
                    continue;
                }
                if (capitalize) {
                    capitalize = false;
                    sb.append((this.name().charAt(i) + "").toUpperCase());
                } else {
                    sb.append((this.name().charAt(i) + "").toLowerCase());
                }
            }
            return sb.toString();
        }
    }

    public static List<Product> getAllProducts(final File dir) throws IOException {
        final Set<Product> set = new HashSet<>();
        for (final File realmDir : dir.listFiles()) {
            if (realmDir.isDirectory()) {
                for (final File file : realmDir.listFiles()) {
                    if (file.isFile() && file.getName().equals("products.json")) {
                        final String text = FileUtils.readFileToString(file, "UTF-8");
                        final Product[] arr = new GsonBuilder().create().fromJson(text, Product[].class);
                        Collections.addAll(set, arr);
                    }
                }
            }
        }
        return new ArrayList<>(set);
    }

    public static List<Product> getAllRealmProducts(final File realmDir) throws IOException {
        final Set<Product> set = new HashSet<>();
        for (final File file : realmDir.listFiles()) {
            if (file.isFile() && file.getName().equals("products.json")) {
                final String text = FileUtils.readFileToString(file, "UTF-8");
                final Product[] arr = new GsonBuilder().create().fromJson(text, Product[].class);
                Collections.addAll(set, arr);
            }
        }
        return new ArrayList<>(set);
    }

    public static List<Product> getAllRealmMaterials(final File realmDir) throws IOException {
        final Set<Product> set = new HashSet<>();
        for (final File file : realmDir.listFiles()) {
            if (file.isFile() && file.getName().equals("materials.json")) {
                final String text = FileUtils.readFileToString(file, "UTF-8");
                final Product[] arr = new GsonBuilder().create().fromJson(text, Product[].class);
                Collections.addAll(set, arr);
            }
        }
        return new ArrayList<>(set);
    }

    public static Stream<TradeAtCity> getAllVersionsTradeAtCity(
            final Git git,
            final String fileNameStartWith,
            final String realm
    ) {
        return getAllVersions(git, Wizard.by_trade_at_cities, fileNameStartWith, Optional.of(realm))
                .flatMap(fileVersion -> {
                    try {
                        final TradeAtCity[] arr = new GsonBuilder().create().fromJson(fileVersion.getContent(), TradeAtCity[].class);
                        return Stream.of(arr)
                                .peek(ra -> ra.setDate(fileVersion.getDate()));
                    } catch (final Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                        return Stream.empty();
                    }
                });
    }

    public static Stream<TradeAtCity> getAllTradeAtCity(final Git git, final String fileNameStartWith, final String realm, final String productID) {
        return getAllVersionsTradeAtCity(git, fileNameStartWith, realm)
                .filter(ra -> productID.equals(ra.getProductId()));
    }

    public static Set<TradeAtCity> getAllTradeAtCity(final String fileNameStartWith, final String realm) throws IOException, GitAPIException {
        return getAllVersions(Wizard.by_trade_at_cities, fileNameStartWith, Optional.of(realm))
                .map(fileVersion -> {
                    try {
                        final TradeAtCity[] arr = new GsonBuilder().create().fromJson(fileVersion.getContent(), TradeAtCity[].class);
                        return Stream.of(arr)
                                .filter(ra -> ra.getProductId() != null)
                                .peek(ra -> ra.setDate(fileVersion.getDate()))
                                .collect(toList());
                    } catch (final Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toSet());
    }

    public static Stream<ProductRemain> getAllVersionsProductRemain(
            final Git git,
            final String fileNameStartWith,
            final String realm
    ) {
        return getAllVersions(git, Wizard.industry, fileNameStartWith, Optional.of(realm))
                .flatMap(fileVersion -> {
                    try {
                        final ProductRemain[] arr = new GsonBuilder().create().fromJson(fileVersion.getContent(), ProductRemain[].class);
                        return Stream.of(arr)
                                .peek(ra -> ra.setDate(fileVersion.getDate()));
                    } catch (final Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                        return Stream.empty();
                    }
                });
    }

    public static Stream<ProductRemain> getAllProductRemains(final Git git, final String fileNameStartWith, final String realm, final String productID) {
        return getAllVersionsProductRemain(git, fileNameStartWith, realm)
                .filter(ra -> productID.equals(ra.getProductID()));
    }

    public static Set<ProductRemain> getAllProductRemains(final String fileNameStartWith, final String realm) throws IOException, GitAPIException {
        return getAllVersions(Wizard.industry, fileNameStartWith, Optional.of(realm))
                .map(fileVersion -> {
                    try {
                        final ProductRemain[] arr = new GsonBuilder().create().fromJson(fileVersion.getContent(), ProductRemain[].class);
                        return Stream.of(arr)
                                .filter(ra -> ra.getProductID() != null)
                                .peek(ra -> ra.setDate(fileVersion.getDate()))
                                .collect(toList());
                    } catch (final Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(toSet());
    }

    public static Git fetchAndHardReset() throws GitAPIException, IOException {
        final Git git = GitHubPublisher.getRepo();
        logger.info("git fetch");
        git.fetch().call();
        logger.info("git fetch finished");
        logger.info("git reset");
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
        logger.info("git reset finished");
        return git;
    }

    public static Stream<FileVersion> getAllVersions(final String dirName, final String fileNameStartWith, final Optional<String> realm) throws IOException, GitAPIException {
        final Git git = fetchAndHardReset();
        return getAllVersions(git, dirName, fileNameStartWith, realm);
    }

    public static Stream<FileVersion> getAllVersions(
            final Git git,
            final String dirName,
            final String fileNameStartWith,
            final Optional<String> realm
    ) {
        final File dir = new File(GitHubPublisher.localPath + dirName + File.separator);
        logger.trace("dir = {}", dir.getAbsoluteFile());
        if (dir.listFiles() == null) {
            return Stream.empty();
        }

        return Stream.of(dir.listFiles())
                .filter(File::isDirectory)
                .filter(realmDir -> !realm.isPresent() || realmDir.getName().equals(realm.get()))
                .map(File::listFiles)
                .flatMap(Stream::of)
                .filter(File::isFile)
                .filter(f -> f.getName().startsWith(fileNameStartWith))
                .flatMap(file -> {
                    try {
                        return GitHubPublisher.getAllVersions(git, dirName + "/" + realm.orElse(new File(file.getParent()).getName()) + "/" + file.getName());
                    } catch (final Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                        return Stream.empty();
                    }
                });
    }

    public static Stream<RetailAnalytics> getAllRetailAnalytics(final String fileNameStartWith) throws IOException, GitAPIException {
        final Stream<RetailAnalytics> stream = getAllVersions(Wizard.by_trade_at_cities, fileNameStartWith, Optional.empty())
                .map(fileVersion -> {
                    try {
                        final RetailAnalytics[] arr = new GsonBuilder().create().fromJson(fileVersion.getContent(), RetailAnalytics[].class);
                        return Stream.of(arr)
                                .filter(ra -> ra.getProductId() != null)
                                .peek(ra -> ra.setDate(fileVersion.getDate()))
                                .collect(toList());
                    } catch (final Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .parallel();
        logger.info("getAllRetailAnalytics done");
        return stream;
    }

    /**
     * Оставляет не более 50 элементов для каждого:
     * индекса рынка
     * , уровня благосостояния
     * , объёма рынка
     */
    private static List<RetailAnalytics> squeeze(final List<RetailAnalytics> list) {
        final Map<String, List<RetailAnalytics>> map = list.stream()
                .collect(Collectors.groupingBy(RetailAnalytics::getMarketIdx));

        final Comparator<RetailAnalytics> comparator = new RetailAnalyticsHistCompare();
        final int maxCnt = 50;
        final List<RetailAnalytics> result = new ArrayList<>();
        for (final Map.Entry<String, List<RetailAnalytics>> entry : map.entrySet()) {
            final List<RetailAnalytics> tmp = entry.getValue();
            if (tmp.size() > maxCnt) {
                result.addAll(groupByWealthIndex(tmp, comparator, maxCnt));
            } else {
                result.addAll(tmp);
            }
        }
        return result;
    }

    private static List<RetailAnalytics> groupByWealthIndex(final List<RetailAnalytics> list, final Comparator<RetailAnalytics> comparator, final int maxCnt) {
        final Map<Long, List<RetailAnalytics>> map = list.stream()
                .collect(Collectors.groupingBy(RetailAnalytics::getWealthIndexRounded));

        final List<RetailAnalytics> result = new ArrayList<>();
        for (final Map.Entry<Long, List<RetailAnalytics>> entry : map.entrySet()) {
            final List<RetailAnalytics> tmp = entry.getValue();
            if (tmp.size() > maxCnt) {
                result.addAll(groupByMarketVolume(tmp, comparator, maxCnt));
            } else {
                result.addAll(tmp);
            }
        }
        return result;
    }

    private static List<RetailAnalytics> groupByMarketVolume(final List<RetailAnalytics> list, final Comparator<RetailAnalytics> comparator, final int maxCnt) {
        final Map<Long, List<RetailAnalytics>> map = list.stream()
                .collect(Collectors.groupingBy(RetailAnalytics::getMarketVolume));

        final List<RetailAnalytics> result = new ArrayList<>();
        for (final Map.Entry<Long, List<RetailAnalytics>> entry : map.entrySet()) {
            final List<RetailAnalytics> tmp = entry.getValue();
            if (tmp.size() > maxCnt) {
                tmp.sort(comparator);
                result.addAll(tmp.subList(0, maxCnt));
            } else {
                result.addAll(tmp);
            }
        }
        return result;
    }

}
