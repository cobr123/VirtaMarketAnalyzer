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
import ru.VirtaMarketAnalyzer.ml.js.LinearRegressionToJson;
import ru.VirtaMarketAnalyzer.parser.ProductInitParser;
import ru.VirtaMarketAnalyzer.publish.GitHubPublisher;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.meta.RandomCommittee;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVSaver;

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
                .map(fileVersion -> {
                    try {
                        final TradeAtCity[] arr = new GsonBuilder().create().fromJson(fileVersion.getContent(), TradeAtCity[].class);
                        return Stream.of(arr)
                                .peek(ra -> ra.setDate(fileVersion.getDate()))
                                .collect(toList());
                    } catch (final Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream);
    }

    public static Set<TradeAtCity> getAllTradeAtCity(final Git git, final String fileNameStartWith, final String realm, final String productID) throws IOException, GitAPIException {
        return getAllVersionsTradeAtCity(git, fileNameStartWith, realm)
                .filter(ra -> productID.equals(ra.getProductId()))
                .collect(toSet());
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

    public static final Map<String, List<ProductRemain>> cacheProductRemain = new HashMap<>();

    public static List<ProductRemain> getAllVersionsProductRemain(
            final Git git,
            final String fileNameStartWith,
            final String realm
    ) {
        final String key = realm + "|" + fileNameStartWith;
        if (!cacheProductRemain.containsKey(key)) {
            final List<ProductRemain> list = getAllVersions(git, Wizard.industry, fileNameStartWith, Optional.of(realm))
                    .map(fileVersion -> {
                        try {
                            final ProductRemain[] arr = new GsonBuilder().create().fromJson(fileVersion.getContent(), ProductRemain[].class);
                            return Stream.of(arr)
                                    .peek(ra -> ra.setDate(fileVersion.getDate()))
                                    .collect(toList());
                        } catch (final Exception e) {
                            logger.error(e.getLocalizedMessage(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .collect(toList());
            cacheProductRemain.put(key, list);
        }
        return cacheProductRemain.get(key);
    }

    public static Set<ProductRemain> getAllProductRemains(final Git git, final String fileNameStartWith, final String realm, final String productID) throws IOException, GitAPIException {
        return getAllVersionsProductRemain(git, fileNameStartWith, realm)
                .stream()
                .filter(ra -> productID.equals(ra.getProductID()))
                .collect(toSet());
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
                .map(file -> {
                    try {
                        return GitHubPublisher.getAllVersions(git, dirName + "/" + realm.orElse(new File(file.getParent()).getName()) + "/" + file.getName());
                    } catch (final Exception e) {
                        logger.error(e.getLocalizedMessage(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream);
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

    public static LinearRegressionSummary createCommonPrediction(final String productID) throws IOException, GitAPIException {
        logger.info("productID = {}", productID);
        final Set<RetailAnalytics> set = getAllRetailAnalytics(RETAIL_ANALYTICS_ + productID)
                .filter(ra -> productID.isEmpty() || ra.getProductId().equals(productID))
                //.filter(ra -> ra.getShopSize() == 100 || ra.getShopSize() == 500 || ra.getShopSize() == 1_000 || ra.getShopSize() == 10_000 || ra.getShopSize() == 100_000)
//                .filter(ra -> ra.getShopSize() > 0)
//                .filter(ra -> ra.getSellVolumeNumber() > 0)
//                .filter(ra -> ra.getDemography() > 0)
//                .filter(ra -> ra.getMarketIdx().isEmpty() || ra.getMarketIdx().equals("E"))
                .collect(toSet());
        logger.info("set.size() = {}", set.size());

        if (!set.isEmpty()) {
            //группируем аналитику по товарам и сохраняем
//            final Map<String, List<RetailAnalytics>> retailAnalyticsHist = set.parallelStream()
//                    .filter(ra -> ra.getNotoriety() >= 100)
//                    .collect(Collectors.groupingBy(RetailAnalytics::getProductId));

//            final ExclusionStrategy es = new HistAnalytExclStrat();
//            for (final Map.Entry<String, List<RetailAnalytics>> entry : retailAnalyticsHist.entrySet()) {
//                final String fileNamePath = GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator
//                        + RetailSalePrediction.RETAIL_ANALYTICS_HIST + File.separator + entry.getKey() + ".json";
//                Utils.writeToGson(fileNamePath, squeeze(entry.getValue()), es);
//            }
            final Set<String> productIds = set.parallelStream().map(RetailAnalytics::getProductId).collect(Collectors.toSet());
            final Set<String> productCategories = set.parallelStream().map(RetailAnalytics::getProductCategory).collect(Collectors.toSet());
            try {
                logger.info("createTrainingSet");
                final Instances trainingSet = createTrainingSet(set, productIds, productCategories);

//                final Standardize standardize = new Standardize();
//                standardize.setInputFormat(trainingSetRaw);
//                final Instances trainingSet = Filter.useFilter(trainingSetRaw, standardize);

                logger.info("ArffSaver");
                final ArffSaver saver = new ArffSaver();
                saver.setInstances(trainingSet);
                saver.setFile(new File(Utils.getDir() + WEKA + File.separator + "common_" + productID + ".arff"));
                saver.writeBatch();

                logger.info("CSVSaver");
                final CSVSaver saverCsv = new CSVSaver();
                saverCsv.setInstances(trainingSet);
                saverCsv.setFile(new File(Utils.getDir() + WEKA + File.separator + "common_" + productID + ".csv"));
                saverCsv.writeBatch();
//                final File file = new File(GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + WEKA + File.separator + "common.arff");
//                file.delete();

                final LinearRegressionSummary summary = trainLinearRegression(trainingSet, productID);
//                trainRandomCommittee(trainingSet);
//                trainDecisionTable(trainingSet);
//                trainMultilayerPerceptron(trainingSet);

//                trainRandomForest(trainingSet);
//                trainRandomTree(trainingSet);
//                trainLibSvm(trainingSet);
//                logger.info("begin trainJ48BySet");
//                trainJ48BySet(trainingSet);
//                logger.info("end trainJ48BySet");
//
//                logger.info("begin trainJ48CrossValidation");
//                trainJ48CrossValidation(trainingSet);
//                logger.info("end trainJ48CrossValidation");

                //запоминаем дату обновления данных
                final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                Utils.writeToGson(GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + "updateDate.json", new UpdateDate(df.format(new Date())));

                return summary;
            } catch (final Exception e) {
                logger.info("productID = {}", productID);
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return null;
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

    public static void trainRandomCommittee(final Instances trainingSet) throws Exception {
        logger.info("Create a classifier");
        final RandomTree classifier = new RandomTree();
        classifier.setKValue(0);
        classifier.setMaxDepth(0);
        classifier.setMinNum(0.001);
        classifier.setAllowUnclassifiedInstances(false);
        classifier.setNumFolds(0);

        final RandomCommittee tree = new RandomCommittee();
        tree.setClassifier(classifier);
        tree.setNumIterations(10);
        tree.buildClassifier(trainingSet);

        logger.info("Test the model");
        final Evaluation eval = new Evaluation(trainingSet);
//        eval.crossValidateModel(tree, trainingSet, 10, new Random(1));
        eval.evaluateModel(tree, trainingSet);

        // Print the result à la Weka explorer:
        logger.info(eval.toSummaryString());
        logger.info(tree.toString());
        logger.info(eval.toMatrixString());
        logger.info(eval.toClassDetailsString());
        logger.info(eval.toCumulativeMarginDistributionString());

//        logger.info("coefficients");
//        for(int i = 0; i < tree.coefficients().length; ++i){
//            logger.info("{} | {}", trainingSet.attribute(i).name(), tree.coefficients()[i]);
//        }

//        try {
//            final File file = new File(GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + "prediction_set_script.js");
//            FileUtils.writeStringToFile(file, ClassifierToJs.compress(ClassifierToJs.toSource(tree, "predictCommonBySet")), "UTF-8");
//        } catch (final Exception e) {
//            logger.error(e.getLocalizedMessage(), e);
//        }
    }

    public static LinearRegressionSummary trainLinearRegression(final Instances trainingSet, final String productID) throws Exception {
        logger.info("Create a classifier");
        final LinearRegression tree = new LinearRegression();
        tree.setEliminateColinearAttributes(true);
        tree.buildClassifier(trainingSet);

        logger.info("Test the model");
        final Evaluation eval = new Evaluation(trainingSet);
//        eval.crossValidateModel(tree, trainingSet, 10, new Random(1));
        eval.evaluateModel(tree, trainingSet);

        // Print the result à la Weka explorer:
//        logger.info(eval.toSummaryString());
//        logger.info(tree.toString());
//        logger.info(LinearRegressionToJson.toJson(tree));
        final LinearRegressionSummary summary = new LinearRegressionSummary(
                productID
                , eval.correlationCoefficient()
                , eval.meanAbsoluteError()
                , eval.rootMeanSquaredError()
                , eval.relativeAbsoluteError()
                , eval.rootRelativeSquaredError()
                , eval.numInstances()
        );
        try {
            final File file = new File(Utils.getDir() + WEKA + File.separator + "coefficients" + File.separator + productID + ".json");
            FileUtils.writeStringToFile(file, LinearRegressionToJson.toJson(tree, summary), "UTF-8");
        } catch (final Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        try {
            final File file = new File(Utils.getDir() + WEKA + File.separator + "coefficients" + File.separator + productID + ".summary.txt");
            FileUtils.writeStringToFile(file, eval.toSummaryString(), "UTF-8");
        } catch (final Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        try {
            final File file = new File(Utils.getDir() + WEKA + File.separator + "coefficients" + File.separator + productID + ".formula.txt");
            FileUtils.writeStringToFile(file, tree.toString(), "UTF-8");
        } catch (final Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return summary;
    }

    public static void trainDecisionTable(final Instances trainingSet) throws Exception {
        // Create a classifier
        final DecisionTable tree = new DecisionTable();
        tree.buildClassifier(trainingSet);

        // Test the model
        final Evaluation eval = new Evaluation(trainingSet);
//        eval.crossValidateModel(tree, trainingSet, 10, new Random(1));
        eval.evaluateModel(tree, trainingSet);

        // Print the result à la Weka explorer:
        logger.info(eval.toSummaryString());
        logger.info(tree.toString());

//        try {
//            final File file = new File(GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + "prediction_set_script.js");
//            FileUtils.writeStringToFile(file, ClassifierToJs.compress(ClassifierToJs.toSource(tree, "predictCommonBySet")), "UTF-8");
//        } catch (final Exception e) {
//            logger.error(e.getLocalizedMessage(), e);
//        }
    }

    public static void trainMultilayerPerceptron(final Instances trainingSet) throws Exception {
        // Create a classifier
        final MultilayerPerceptron tree = new MultilayerPerceptron();
        tree.buildClassifier(trainingSet);

        // Test the model
        final Evaluation eval = new Evaluation(trainingSet);
//        eval.crossValidateModel(tree, trainingSet, 10, new Random(1));
        eval.evaluateModel(tree, trainingSet);

        // Print the result à la Weka explorer:
        logger.info(eval.toSummaryString());
        logger.info(eval.toMatrixString());
        logger.info(tree.toString());
    }

    public static void trainRandomTree(final Instances trainingSet) throws Exception {
        // Create a classifier
        final RandomTree tree = new RandomTree();
        tree.buildClassifier(trainingSet);

        // Test the model
        final Evaluation eval = new Evaluation(trainingSet);
        eval.evaluateModel(tree, trainingSet);

        // Print the result à la Weka explorer:
        logger.info(eval.toSummaryString());
        logger.info(eval.toMatrixString());
        logger.info(tree.toString());
    }

    public static void trainRandomForest(final Instances trainingSet) throws Exception {
        // Create a classifier
        final RandomForest tree = new RandomForest();
        tree.buildClassifier(trainingSet);

        // Test the model
        final Evaluation eval = new Evaluation(trainingSet);
//        eval.crossValidateModel(tree, trainingSet, 10, new Random(1));
        eval.evaluateModel(tree, trainingSet);

        // Print the result à la Weka explorer:
        logger.info(eval.toSummaryString());
        logger.info(eval.toMatrixString());
        logger.info(tree.toString());
    }

    public static void trainLibSvm(final Instances trainingSet) throws Exception {
        // Create a classifier
        final LibSVM tree = new LibSVM();
        tree.buildClassifier(trainingSet);

        // Test the model
        final Evaluation eval = new Evaluation(trainingSet);
        eval.crossValidateModel(tree, trainingSet, 10, new Random(1));
        //eval.evaluateModel(tree, trainingSet);

        // Print the result à la Weka explorer:
        logger.info(eval.toSummaryString());

    }

    public static void trainJ48BySet(final Instances trainingSet) throws Exception {
        // Create a classifier
        final J48 tree = new J48();
        tree.setMinNumObj(1);
        //tree.setConfidenceFactor(0.5f);
        tree.setReducedErrorPruning(true);
        //tree.setDebug(true);
        //
        tree.buildClassifier(trainingSet);
//        ClassifierToJs.saveModel(tree, GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + "prediction_set_script.model");

        // Test the model
        final Evaluation eval = new Evaluation(trainingSet);
        eval.crossValidateModel(tree, trainingSet, 10, new Random(1));
        //eval.evaluateModel(tree, trainingSet);

        // Print the result à la Weka explorer:
        logger.info(eval.toSummaryString());
//        FileUtils.writeStringToFile(new File(GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + "prediction_set_summary.txt"), eval.toSummaryString());

//        try {
//            final File file = new File(GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + "prediction_set_script.js");
//            FileUtils.writeStringToFile(file, ClassifierToJs.compress(ClassifierToJs.toSource(tree, "predictCommonBySet")), "UTF-8");
//        } catch (final Exception e) {
//            logger.error(e.getLocalizedMessage(), e);
//        }
    }

    public static void trainJ48CrossValidation(final Instances trainingSet) throws Exception {
        // Create a classifier
        final J48 tree = new J48();
        tree.setMinNumObj(1);
        //tree.setConfidenceFactor(0.5f);
        tree.setReducedErrorPruning(true);
//        tree.setDebug(true);

        //evaluate j48 with cross validation
        final Evaluation eval = new Evaluation(trainingSet);

        //first supply the classifier
        //then the training data
        //number of folds
        //random seed
        eval.crossValidateModel(tree, trainingSet, 10, new Random(new Date().getTime()));
        logger.info(eval.toSummaryString());
        Utils.writeFile(GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + "prediction_cv_summary.txt", eval.toSummaryString());

        tree.buildClassifier(trainingSet);
//                logger.info(tree.graph());

//        try {
//            final File file = new File(GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + "prediction_cv_script.js");
//            FileUtils.writeStringToFile(file, ClassifierToJs.compress(ClassifierToJs.toSource(tree, "predictCommonByCV")), "UTF-8");
//        } catch (final Exception e) {
//            logger.error(e.getLocalizedMessage(), e);
//        }
    }

    public static void createPrediction(final String realm, final Map<String, Set<RetailAnalytics>> retailAnalytics, final Set<String> productIds, final Set<String> productCategories) throws IOException {
        final String baseDir = Utils.getDir() + Wizard.by_trade_at_cities + File.separator + realm + File.separator;
        logger.info("stats.size() = " + retailAnalytics.size());
        for (final Map.Entry<String, Set<RetailAnalytics>> entry : retailAnalytics.entrySet()) {
            logger.info(entry.getKey());
            logger.info("entry.getValue().size() = " + entry.getValue().size());
            if (entry.getValue().isEmpty()) {
                continue;
            }

            try {
                final Instances trainingSet = createTrainingSet(entry.getValue(), productIds, productCategories);
                //
                final ArffSaver saver = new ArffSaver();
                saver.setInstances(trainingSet);
                saver.setFile(new File(baseDir + WEKA + File.separator + entry.getKey() + ".arff"));
                saver.writeBatch();
                // Create a classifier
                final J48 tree = new J48();
                tree.buildClassifier(trainingSet);
//                ClassifierToJs.saveModel(tree, baseDir + "weka" + File.separator + "java" + File.separator + entry.getKey() + ".model");

//                try {
//                FileUtils.writeStringToFile(new File(baseDir + "weka" + File.separator + "js" + File.separator + entry.getKey() + ".js"), ClassifierToJs.toSource(tree, "PredictProd" + entry.getKey()), "UTF-8");
//                } catch (final Exception e) {
//                    logger.error(e.getLocalizedMessage(), e);
//                }
                // Print the result à la Weka explorer:
//                logger.info((cModel.toString());

                // TestWeka the model
                final Evaluation eTest = new Evaluation(trainingSet);
                eTest.crossValidateModel(tree, trainingSet, 10, new Random(1));
                //eTest.evaluateModel(tree, trainingSet);

                // Print the result à la Weka explorer:
                logger.info(eTest.toSummaryString());

                // Specify that the instance belong to the training set
                // in order to inherit from the set description
//                Instance iUse = createInstance(3198, 9669, 5, 0, 1, 0);
//                iUse.setDataset(trainingSet);

                // Get the likelihood of each classes
//                double[] fDistribution = cModel.distributionForInstance(iUse);
//                logger.info(fDistribution[0]);
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private static Instances createTrainingSet(final Set<RetailAnalytics> retailAnalytics, final Set<String> productIds, final Set<String> productCategories) {
        final FastVector attrs = new FastVector(ATTR.values().length);
        for (final ATTR attr : ATTR.values()) {
            if (attr.ordinal() == ATTR.MARKET_INDEX.ordinal()) {
                final FastVector marketIndex = new FastVector(8);
                marketIndex.addElement("AAA");
                marketIndex.addElement("AA");
                marketIndex.addElement("A");
                marketIndex.addElement("B");
                marketIndex.addElement("C");
                marketIndex.addElement("D");
                marketIndex.addElement("E");
                marketIndex.addElement("");

                attrs.addElement(new Attribute(attr.name(), marketIndex));
            } else if (attr.ordinal() == ATTR.TOWN_DISTRICT.ordinal()) {
                final FastVector districts = new FastVector(5);
                districts.addElement("Фешенебельный район");
                districts.addElement("Центр города");
                districts.addElement("Спальный район");
                districts.addElement("Пригород");
                districts.addElement("Окраина");
                //заправки
                districts.addElement("");

                attrs.addElement(new Attribute(attr.name(), districts));
            } else if (attr.ordinal() == ATTR.SERVICE_LEVEL.ordinal()) {
                final FastVector serviceLevel = new FastVector(6);
                serviceLevel.addElement("Элитный");
                serviceLevel.addElement("Очень высокий");
                serviceLevel.addElement("Высокий");
                serviceLevel.addElement("Нормальный");
                serviceLevel.addElement("Низкий");
                serviceLevel.addElement("Очень низкий");

                attrs.addElement(new Attribute(attr.name(), serviceLevel));
            } else if (attr.ordinal() == ATTR.SHOP_SIZE.ordinal()) {
                final FastVector sizes = new FastVector(5);
                //магазины
                sizes.addElement("100");
                sizes.addElement("500");
                sizes.addElement("1000");
                sizes.addElement("10000");
                sizes.addElement("100000");
                //заправки
                sizes.addElement("1");
                sizes.addElement("2");
                sizes.addElement("3");
                sizes.addElement("4");
                sizes.addElement("5");

                attrs.addElement(new Attribute(attr.name(), sizes));
            } else if (attr.ordinal() == ATTR.PRODUCT_ID.ordinal()) {
                final FastVector fv = new FastVector(productIds.size());
                productIds.forEach(fv::addElement);
                attrs.addElement(new Attribute(attr.name(), fv));
//            } else if (attr.ordinal() == ATTR.PRODUCT_CATEGORY.ordinal()) {
//                final FastVector fv = new FastVector(productCategories.size());
//                productCategories.forEach(fv::addElement);
//                attrs.addElement(new Attribute(attr.name(), fv));
            } else if (attr.ordinal() == ATTR.DEPARTMENT_COUNT.ordinal()) {
                final int maxDepCnt = retailAnalytics.parallelStream().max(Comparator.comparingInt(RetailAnalytics::getDepartmentCount)).get().getDepartmentCount();
                logger.info("maxDepCnt =  {}", maxDepCnt);
                final FastVector fv = new FastVector(maxDepCnt);
                for (int i = 1; i <= maxDepCnt; ++i) {
                    fv.addElement(i + "");
                }
                attrs.addElement(new Attribute(attr.name(), fv));
            } else if (attr.ordinal() == ATTR.SELLER_COUNT.ordinal()) {
                final int maxSellerCnt = retailAnalytics.parallelStream().max(Comparator.comparingInt(RetailAnalytics::getSellerCnt)).get().getSellerCnt();
                logger.info("maxSellerCnt =  {}", maxSellerCnt);
                final FastVector fv = new FastVector(maxSellerCnt + 1);
                for (int i = 0; i <= maxSellerCnt; ++i) {
                    fv.addElement(i + "");
                }
                attrs.addElement(new Attribute(attr.name(), fv));
            } else if (attr.ordinal() == ATTR.VISITORS_COUNT.ordinal()) {
                final FastVector values = new FastVector(numbers.length * words.length + 2);
                retailAnalytics.stream()
                        .map(RetailAnalytics::getVisitorsCount)
                        .distinct()
                        .forEach(values::addElement);

//                values.addElement("менее 50");
//                values.addElement("около 50");
//                for (final String number : numbers) {
//                    for (final String word : words) {
//                        values.addElement(word + " " + number);
//                    }
//                }

                attrs.addElement(new Attribute(attr.name(), values));
            } else {
                attrs.addElement(new Attribute(attr.name()));
            }
        }
        final Instances trainingSet = new Instances("RetailSalePrediction", attrs, retailAnalytics.size());
        // Set class index (ATTR.SELL_VOLUME.ordinal())
//        trainingSet.setClassIndex(ATTR.SELL_VOLUME_NUMBER.ordinal());
        trainingSet.setClassIndex(ATTR.PRICE.ordinal());

        for (final RetailAnalytics ra : retailAnalytics) {
            try {
                trainingSet.add(createInstance(attrs, ra));
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return trainingSet;
    }

    private static Instance createInstance(final FastVector attrs, final RetailAnalytics retailAnalytics) {
        final Instance instance = new Instance(ATTR.values().length);
        instance.setValue((Attribute) attrs.elementAt(ATTR.WEALTH_INDEX.ordinal()), retailAnalytics.getWealthIndex());
        instance.setValue((Attribute) attrs.elementAt(ATTR.EDUCATION_INDEX.ordinal()), retailAnalytics.getEducationIndex());
        instance.setValue((Attribute) attrs.elementAt(ATTR.AVERAGE_SALARY.ordinal()), retailAnalytics.getAverageSalary());
        instance.setValue((Attribute) attrs.elementAt(ATTR.MARKET_INDEX.ordinal()), retailAnalytics.getMarketIdx());
        instance.setValue((Attribute) attrs.elementAt(ATTR.MARKET_VOLUME.ordinal()), retailAnalytics.getMarketVolume());

        instance.setValue((Attribute) attrs.elementAt(ATTR.LOCAL_PERCENT.ordinal()), retailAnalytics.getLocalPercent());
        instance.setValue((Attribute) attrs.elementAt(ATTR.LOCAL_PRICE.ordinal()), retailAnalytics.getLocalPrice());
        instance.setValue((Attribute) attrs.elementAt(ATTR.LOCAL_QUALITY.ordinal()), retailAnalytics.getLocalQuality());

        try {
            instance.setValue((Attribute) attrs.elementAt(ATTR.PRODUCT_ID.ordinal()), retailAnalytics.getProductId());
        } catch (final Exception e) {
            logger.info("retailAnalytics.getProductId() = '{}'", retailAnalytics.getProductId());
            throw e;
        }
//        try {
//            instance.setValue((Attribute) attrs.elementAt(ATTR.PRODUCT_CATEGORY.ordinal()), retailAnalytics.getProductCategory());
//        } catch (final Exception e) {
//            logger.info("retailAnalytics.getProductCategory() = '{}'", retailAnalytics.getProductCategory());
//            throw e;
//        }
        try {
            instance.setValue((Attribute) attrs.elementAt(ATTR.SHOP_SIZE.ordinal()), retailAnalytics.getShopSize() + "");
        } catch (final Exception e) {
            logger.info("retailAnalytics.getShopSize() = '{}', productID = {}", retailAnalytics.getShopSize(), retailAnalytics.getProductId());
            throw e;
        }
        try {
            instance.setValue((Attribute) attrs.elementAt(ATTR.TOWN_DISTRICT.ordinal()), retailAnalytics.getTownDistrict());
        } catch (final Exception e) {
            logger.info("retailAnalytics.getTownDistrict() = '{}'", retailAnalytics.getTownDistrict());
            throw e;
        }
        try {
            instance.setValue((Attribute) attrs.elementAt(ATTR.DEPARTMENT_COUNT.ordinal()), retailAnalytics.getDepartmentCount() + "");
        } catch (final Exception e) {
            logger.info("retailAnalytics.getDepartmentCount() = '{}'", retailAnalytics.getDepartmentCount());
            throw e;
        }
        instance.setValue((Attribute) attrs.elementAt(ATTR.NOTORIETY.ordinal()), retailAnalytics.getNotoriety());
        try {
            instance.setValue((Attribute) attrs.elementAt(ATTR.VISITORS_COUNT.ordinal()), retailAnalytics.getVisitorsCount());
        } catch (final Exception e) {
            logger.info("retailAnalytics.getVisitorsCount() = '{}'", retailAnalytics.getVisitorsCount());
            throw e;
        }
        try {
            instance.setValue((Attribute) attrs.elementAt(ATTR.SERVICE_LEVEL.ordinal()), retailAnalytics.getServiceLevel());
        } catch (final Exception e) {
            logger.info("retailAnalytics.getServiceLevel() = '{}'", retailAnalytics.getServiceLevel());
            throw e;
        }
        try {
            instance.setValue((Attribute) attrs.elementAt(ATTR.SELLER_COUNT.ordinal()), retailAnalytics.getSellerCnt() + "");
        } catch (final Exception e) {
            logger.info("retailAnalytics.getSellerCnt() = '{}'", retailAnalytics.getSellerCnt());
            throw e;
        }

        instance.setValue((Attribute) attrs.elementAt(ATTR.BRAND.ordinal()), retailAnalytics.getBrand());
        instance.setValue((Attribute) attrs.elementAt(ATTR.PRICE.ordinal()), retailAnalytics.getPrice());
        instance.setValue((Attribute) attrs.elementAt(ATTR.QUALITY.ordinal()), retailAnalytics.getQuality());
        instance.setValue((Attribute) attrs.elementAt(ATTR.SELL_VOLUME_NUMBER.ordinal()), retailAnalytics.getSellVolumeNumber());
        instance.setValue((Attribute) attrs.elementAt(ATTR.DEMOGRAPHY.ordinal()), retailAnalytics.getDemography());

        return instance;
    }

    public static void main(String[] args) throws Exception {
//        createCommonPrediction("");
//        createCommonPrediction("1490");
//        createCommonPrediction("422705");
        final List<Product> products = ProductInitParser.getTradingProducts(Wizard.host, "olga");
        final List<LinearRegressionSummary> summaries = new ArrayList<>();
        for (int i = 0, productsSize = products.size(); i < productsSize; i++) {
            logger.info("{}/{}", i, productsSize);
            summaries.add(RetailSalePrediction.createCommonPrediction(products.get(i).getId()));
        }
        Utils.writeToGson(Utils.getDir() + WEKA + File.separator + "summaries.json", summaries);
    }
}
