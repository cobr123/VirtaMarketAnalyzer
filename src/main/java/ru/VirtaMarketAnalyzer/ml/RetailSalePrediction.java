package ru.VirtaMarketAnalyzer.ml;

import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.Product;
import ru.VirtaMarketAnalyzer.data.RetailAnalytics;
import ru.VirtaMarketAnalyzer.data.UpdateDate;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import ru.VirtaMarketAnalyzer.ml.js.ClassifierToJs;
import ru.VirtaMarketAnalyzer.publish.GitHubPublisher;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
    public static final String WEKA = "weka";

    public enum ATTR {
        WEALTH_INDEX, EDUCATION_INDEX, AVERAGE_SALARY,
        MARKET_INDEX, MARKET_VOLUME, LOCAL_PERCENT,
        LOCAL_PRICE, LOCAL_QUALITY, PRICE,
        SHOP_SIZE, TOWN_DISTRICT, DEPARTMENT_COUNT,
        BRAND, QUALITY, NOTORIETY, VISITORS_COUNT,
        SERVICE_LEVEL, SELLER_COUNT, PRODUCT_ID,
        //последний для автоподстановки при открытии в weka
        SELL_VOLUME;

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

    public static void createCommonPrediction() throws IOException, GitAPIException {
        final Set<RetailAnalytics> set = new HashSet<>();
        final File dir = new File(Utils.getDir() + Wizard.by_trade_at_cities + File.separator);
        final Git git = GitHubPublisher.getRepo();
        for (final File realmDir : dir.listFiles()) {
            for (final File file : realmDir.listFiles()) {
                if (file.getName().startsWith(RETAIL_ANALYTICS_)) {
                    final List<String> list = GitHubPublisher.getAllVersions(git, Wizard.by_trade_at_cities + "/" + realmDir.getName() + "/" + file.getName());
                    final String productId = Utils.getLastBySep(FilenameUtils.removeExtension(file.getName()), "_");
                    for (final String str : list) {
                        try {
                            final RetailAnalytics[] arr = new GsonBuilder().create().fromJson(str, RetailAnalytics[].class);
                            for (final RetailAnalytics ra : arr) {
                                set.add(RetailAnalytics.fillProductId(productId, ra));
                            }
                        } catch (final Exception e) {
                            logger.error(e.getLocalizedMessage(), e);
                        }
                    }
                }
            }
        }

        if (!set.isEmpty()) {
            logger.info("set.size() = " + set.size());
            final Set<String> productIds = set.parallelStream().map(RetailAnalytics::getProductId).collect(Collectors.toSet());
            try {
                final Instances trainingSet = createTrainingSet(set, productIds);
                //
                final ArffSaver saver = new ArffSaver();
                saver.setInstances(trainingSet);
                saver.setFile(new File(GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + WEKA + File.separator + "common.arff"));
                saver.writeBatch();

                trainJ48BySet(trainingSet);
                trainJ48CrossValidation(trainingSet);

                //запоминаем дату обновления данных
                final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                Utils.writeToGson(GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + "updateDate.json", new UpdateDate(df.format(new Date())));
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }

    public static void trainJ48BySet(final Instances trainingSet) throws Exception {
        // Create a classifier
        final J48 tree = new J48();
        tree.buildClassifier(trainingSet);

        // Test the model
        final Evaluation eval = new Evaluation(trainingSet);
        eval.evaluateModel(tree, trainingSet);

        // Print the result à la Weka explorer:
        logger.info(eval.toSummaryString());

        try {
            final File file = new File(GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + "prediction_set_script.js");
            FileUtils.writeStringToFile(file, ClassifierToJs.compress(ClassifierToJs.toSource(tree, "predictCommonBySet")), "UTF-8");
        } catch (final Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public static void trainJ48CrossValidation(final Instances trainingSet) throws Exception {
        // Create a classifier
        final J48 tree = new J48();
        tree.setUnpruned(true);        // using an unpruned J48

        //evaluate j48 with cross validation
        final Evaluation eval = new Evaluation(trainingSet);

        //first supply the classifier
        //then the training data
        //number of folds
        //random seed
        eval.crossValidateModel(tree, trainingSet, 10, new Random(new Date().getTime()));
        logger.info(eval.toSummaryString());


        tree.buildClassifier(trainingSet);
//                logger.info(tree.graph());

        try {
            final File file = new File(GitHubPublisher.localPath + RetailSalePrediction.predict_retail_sales + File.separator + "prediction_cv_script.js");
            FileUtils.writeStringToFile(file, ClassifierToJs.compress(ClassifierToJs.toSource(tree, "predictCommonByCV")), "UTF-8");
        } catch (final Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public static void createPrediction(final String realm, final Map<String, Set<RetailAnalytics>> retailAnalytics, final Set<String> productIds) throws IOException {
        final String baseDir = Utils.getDir() + Wizard.by_trade_at_cities + File.separator + realm + File.separator;
        logger.info("stats.size() = " + retailAnalytics.size());
        for (final Map.Entry<String, Set<RetailAnalytics>> entry : retailAnalytics.entrySet()) {
            logger.info(entry.getKey());
            logger.info("entry.getValue().size() = " + entry.getValue().size());
            if (entry.getValue().isEmpty()) {
                continue;
            }

            try {
                final Instances trainingSet = createTrainingSet(entry.getValue(), productIds);
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

                // Test the model
                final Evaluation eTest = new Evaluation(trainingSet);
                eTest.evaluateModel(tree, trainingSet);

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

    private static Instances createTrainingSet(final Set<RetailAnalytics> retailAnalytics, final Set<String> productIds) {
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
                sizes.addElement("100");
                sizes.addElement("500");
                sizes.addElement("1000");
                sizes.addElement("10000");
                sizes.addElement("100000");

                attrs.addElement(new Attribute(attr.name(), sizes));
            } else if (attr.ordinal() == ATTR.PRODUCT_ID.ordinal()) {
                final FastVector fv = new FastVector(productIds.size());
                productIds.forEach(fv::addElement);
                attrs.addElement(new Attribute(attr.name(), fv));
            } else if (attr.ordinal() == ATTR.SELL_VOLUME.ordinal() || attr.ordinal() == ATTR.VISITORS_COUNT.ordinal()) {
                final FastVector values = new FastVector(numbers.length * words.length + 2);
                values.addElement("менее 50");
                values.addElement("около 50");
                for (final String number : numbers) {
                    for (final String word : words) {
                        values.addElement(word + " " + number);
                    }
                }

                attrs.addElement(new Attribute(attr.name(), values));
            } else {
                attrs.addElement(new Attribute(attr.name()));
            }
        }
        final Instances trainingSet = new Instances("RetailSalePrediction", attrs, retailAnalytics.size());
        // Set class index (ATTR.SELL_VOLUME.ordinal())
        trainingSet.setClassIndex(ATTR.SELL_VOLUME.ordinal());

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
        instance.setValue((Attribute) attrs.elementAt(ATTR.SHOP_SIZE.ordinal()), retailAnalytics.getShopSize() + "");
        try {
            instance.setValue((Attribute) attrs.elementAt(ATTR.TOWN_DISTRICT.ordinal()), retailAnalytics.getTownDistrict());
        } catch (final Exception e) {
            logger.info("retailAnalytics.getTownDistrict() = '{}'", retailAnalytics.getTownDistrict());
            throw e;
        }
        instance.setValue((Attribute) attrs.elementAt(ATTR.DEPARTMENT_COUNT.ordinal()), retailAnalytics.getDepartmentCount());
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
        instance.setValue((Attribute) attrs.elementAt(ATTR.SELLER_COUNT.ordinal()), retailAnalytics.getSellerCnt());

        instance.setValue((Attribute) attrs.elementAt(ATTR.BRAND.ordinal()), retailAnalytics.getBrand());
        instance.setValue((Attribute) attrs.elementAt(ATTR.PRICE.ordinal()), retailAnalytics.getPrice());
        instance.setValue((Attribute) attrs.elementAt(ATTR.QUALITY.ordinal()), retailAnalytics.getQuality());
        try {
            instance.setValue((Attribute) attrs.elementAt(ATTR.SELL_VOLUME.ordinal()), retailAnalytics.getSellVolume());
        } catch (final Exception e) {
            logger.info("retailAnalytics.getSellVolume() = '{}'", retailAnalytics.getSellVolume());
            throw e;
        }
        return instance;
    }

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));

        createCommonPrediction();
    }
}
