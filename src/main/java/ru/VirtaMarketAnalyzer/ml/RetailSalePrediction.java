package ru.VirtaMarketAnalyzer.ml;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.RetailAnalytics;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by cobr123 on 15.01.2016.
 */
public final class RetailSalePrediction {
    private static final Logger logger = LoggerFactory.getLogger(RetailSalePrediction.class);

    private enum ATTR {
        WEALTH_INDEX, EDUCATION_INDEX, AVERAGE_SALARY, MARKET_INDEX, MARKET_VOLUME,
        LOCAL_PERCENT, LOCAL_PRICE, LOCAL_QUALITY, PRICE, SHOP_SIZE,
        TOWN_DISTRICT, DEPARTMENT_COUNT, BRAND, QUALITY, SELL_VOLUME,
        NOTORIETY, VISITORS_COUNT, SERVICE_LEVEL, SELLER_COUNT
    }

    public static void createPrediction(final String realm, final Map<String, List<RetailAnalytics>> retailAnalytics) throws IOException {
        final String baseDir = Utils.getDir() + Wizard.by_trade_at_cities + File.separator + realm + File.separator;
        System.out.println("stats.size() = " + retailAnalytics.size());
        for (final Map.Entry<String, List<RetailAnalytics>> entry : retailAnalytics.entrySet()) {
            logger.info("entry.getValue().size() = " + entry.getValue().size());
            logger.info(entry.getKey());
            try {
                final Instances trainingSet = createTrainingSet(entry.getValue());
                //
                final ArffSaver saver = new ArffSaver();
                saver.setInstances(trainingSet);
                saver.setFile(new File(baseDir + "weka" + entry.getKey() + ".arff"));
                saver.writeBatch();
                // Create a LinearRegression classifier
                final Classifier cModel = (Classifier) new LinearRegression();
                cModel.buildClassifier(trainingSet);
                // Print the result à la Weka explorer:
//                logger.info((cModel.toString());

                // Test the model
                final Evaluation eTest = new Evaluation(trainingSet);
                eTest.evaluateModel(cModel, trainingSet);

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

    private static Instances createTrainingSet(final List<RetailAnalytics> retailAnalytics) {
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
                districts.addElement("Окраина");
                districts.addElement("Пригород");

                attrs.addElement(new Attribute(attr.name(), districts));
            } else if (attr.ordinal() == ATTR.SERVICE_LEVEL.ordinal()) {
                final FastVector serviceLevel = new FastVector(5);
                serviceLevel.addElement("Элитный");
                serviceLevel.addElement("Высокий");
                serviceLevel.addElement("Средний");
                serviceLevel.addElement("Низкий");

                attrs.addElement(new Attribute(attr.name(), serviceLevel));
            } else if (attr.ordinal() == ATTR.SHOP_SIZE.ordinal()) {
                final FastVector sizes = new FastVector(5);
                sizes.addElement("100");
                sizes.addElement("500");
                sizes.addElement("1000");
                sizes.addElement("10000");
                sizes.addElement("100000");

                attrs.addElement(new Attribute(attr.name(), sizes));
            } else {
                attrs.addElement(new Attribute(attr.name()));
            }
        }
        final Instances trainingSet = new Instances("RetailSalePrediction", attrs, retailAnalytics.size());
        // Set class index (ATTR.SELL_VOLUME.ordinal())
        trainingSet.setClassIndex(ATTR.SELL_VOLUME.ordinal());

        for (final RetailAnalytics ra : retailAnalytics) {
            trainingSet.add(createInstance(attrs, ra));
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

        instance.setValue((Attribute) attrs.elementAt(ATTR.SHOP_SIZE.ordinal()), retailAnalytics.getShopSize() + "");
        instance.setValue((Attribute) attrs.elementAt(ATTR.TOWN_DISTRICT.ordinal()), retailAnalytics.getTownDistrict());
        instance.setValue((Attribute) attrs.elementAt(ATTR.DEPARTMENT_COUNT.ordinal()), retailAnalytics.getDepartmentCount());
        instance.setValue((Attribute) attrs.elementAt(ATTR.NOTORIETY.ordinal()), retailAnalytics.getNotoriety());
        instance.setValue((Attribute) attrs.elementAt(ATTR.VISITORS_COUNT.ordinal()), retailAnalytics.getVisitorsCount());
        instance.setValue((Attribute) attrs.elementAt(ATTR.SERVICE_LEVEL.ordinal()), retailAnalytics.getServiceLevel());
        instance.setValue((Attribute) attrs.elementAt(ATTR.SELLER_COUNT.ordinal()), retailAnalytics.getSellerCnt());

        instance.setValue((Attribute) attrs.elementAt(ATTR.BRAND.ordinal()), retailAnalytics.getBrand());
        instance.setValue((Attribute) attrs.elementAt(ATTR.PRICE.ordinal()), retailAnalytics.getPrice());
        instance.setValue((Attribute) attrs.elementAt(ATTR.QUALITY.ordinal()), retailAnalytics.getQuality());
        instance.setValue((Attribute) attrs.elementAt(ATTR.SELL_VOLUME.ordinal()), retailAnalytics.getSellVolume());
        return instance;
    }

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        Wizard.collectToJsonTradeAtCities("vera");
    }
}
