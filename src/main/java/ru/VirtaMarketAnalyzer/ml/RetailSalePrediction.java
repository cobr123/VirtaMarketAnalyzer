package ru.VirtaMarketAnalyzer.ml;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.City;
import ru.VirtaMarketAnalyzer.data.MajorSellInCity;
import ru.VirtaMarketAnalyzer.data.TradeAtCity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cobr123 on 15.01.2016.
 */
public final class RetailSalePrediction {
    private static final Logger logger = LoggerFactory.getLogger(RetailSalePrediction.class);

    private enum ATTR {
        WEALTH_INDEX, EDUCATION_INDEX, AVERAGE_SALARY, MARKET_INDEX, MARKET_VOLUME, LOCAL_PERCENT, LOCAL_PRICE, LOCAL_QUALITY, SHOP_PERCENT, SHOP_PRICE, SHOP_QUALITY, MAJOR_PRICE, MAJOR_SHOP_SIZE, MAJOR_TOWN_DISTRICT, MAJOR_BRAND, MAJOR_QUALITY, MAJOR_SELL_VOLUME
    }

    public static void createPrediction(final String realm, final Map<String, List<TradeAtCity>> stats) {
        System.out.println("stats.size() = " + stats.size());
        for (final Map.Entry<String, List<TradeAtCity>> entry : stats.entrySet()) {
            logger.info("entry.getValue().size() = " + entry.getValue().size());
            logger.info(entry.getKey());
            try {
                final Instances trainingSet = createTrainingSet(entry.getValue());
                //
                final ArffSaver saver = new ArffSaver();
                saver.setInstances(trainingSet);
                saver.setFile(new File("d:\\weka\\" + realm + "\\" + entry.getKey() + ".arff"));
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

    private static Instances createTrainingSet(final List<TradeAtCity> tradeAtCities) {
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
            } else if (attr.ordinal() == ATTR.MAJOR_TOWN_DISTRICT.ordinal()) {
                final FastVector districts = new FastVector(5);
                districts.addElement("Фешенебельный район");
                districts.addElement("Центр города");
                districts.addElement("Спальный район");
                districts.addElement("Окраина");
                districts.addElement("Пригород");

                attrs.addElement(new Attribute(attr.name(), districts));
            } else if (attr.ordinal() == ATTR.MAJOR_SHOP_SIZE.ordinal()) {
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
        final Instances trainingSet = new Instances("RetailSalePrediction", attrs, tradeAtCities.size());
        // Set class index (ATTR.MAJOR_SELL_VOLUME.ordinal())
        trainingSet.setClassIndex(ATTR.MAJOR_SELL_VOLUME.ordinal());

        for (final TradeAtCity tradeAtCity : tradeAtCities) {
            for (final MajorSellInCity majorSellInCity : tradeAtCity.getMajorSellInCityList()) {
                trainingSet.add(createInstance(attrs, tradeAtCity, majorSellInCity));
            }
        }
        return trainingSet;
    }

    private static Instance createInstance(final FastVector attrs, final TradeAtCity tradeAtCity, final MajorSellInCity majorSellInCity) {
        final Instance instance = new Instance(ATTR.values().length);
        instance.setValue((Attribute) attrs.elementAt(ATTR.WEALTH_INDEX.ordinal()), tradeAtCity.getWealthIndex());
        instance.setValue((Attribute) attrs.elementAt(ATTR.EDUCATION_INDEX.ordinal()), tradeAtCity.getEducationIndex());
        instance.setValue((Attribute) attrs.elementAt(ATTR.AVERAGE_SALARY.ordinal()), tradeAtCity.getAverageSalary());
        instance.setValue((Attribute) attrs.elementAt(ATTR.MARKET_INDEX.ordinal()), tradeAtCity.getMarketIdx());
        instance.setValue((Attribute) attrs.elementAt(ATTR.MARKET_VOLUME.ordinal()), tradeAtCity.getVolume());

        instance.setValue((Attribute) attrs.elementAt(ATTR.LOCAL_PERCENT.ordinal()), tradeAtCity.getLocalPercent());
        instance.setValue((Attribute) attrs.elementAt(ATTR.LOCAL_PRICE.ordinal()), tradeAtCity.getLocalPrice());
        instance.setValue((Attribute) attrs.elementAt(ATTR.LOCAL_QUALITY.ordinal()), tradeAtCity.getLocalQuality());

        instance.setValue((Attribute) attrs.elementAt(ATTR.SHOP_PERCENT.ordinal()), tradeAtCity.getShopBrand());
        instance.setValue((Attribute) attrs.elementAt(ATTR.SHOP_PRICE.ordinal()), tradeAtCity.getShopPrice());
        instance.setValue((Attribute) attrs.elementAt(ATTR.SHOP_QUALITY.ordinal()), tradeAtCity.getShopQuality());

        instance.setValue((Attribute) attrs.elementAt(ATTR.MAJOR_SHOP_SIZE.ordinal()), majorSellInCity.getShopSize() + "");
        instance.setValue((Attribute) attrs.elementAt(ATTR.MAJOR_TOWN_DISTRICT.ordinal()), majorSellInCity.getTownDistrict());
        instance.setValue((Attribute) attrs.elementAt(ATTR.MAJOR_BRAND.ordinal()), majorSellInCity.getBrand());
        instance.setValue((Attribute) attrs.elementAt(ATTR.MAJOR_PRICE.ordinal()), majorSellInCity.getPrice());
        instance.setValue((Attribute) attrs.elementAt(ATTR.MAJOR_QUALITY.ordinal()), majorSellInCity.getQuality());
        instance.setValue((Attribute) attrs.elementAt(ATTR.MAJOR_SELL_VOLUME.ordinal()), majorSellInCity.getSellVolume());
        return instance;
    }

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));
        Wizard.collectToJsonTradeAtCities("vera");
    }
}
