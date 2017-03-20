package ru.VirtaMarketAnalyzer.ml;

//import org.apache.spark.SparkConf;
//import org.apache.spark.api.java.JavaSparkContext;
//import scala.Tuple2;
//
//import org.apache.spark.api.java.JavaDoubleRDD;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.api.java.function.Function;
//import org.apache.spark.mllib.linalg.Vectors;
//import org.apache.spark.mllib.regression.LabeledPoint;
//import org.apache.spark.mllib.regression.LinearRegressionModel;
//import org.apache.spark.mllib.regression.LinearRegressionWithSGD;
//
///**
// * Created by r.tabulov on 28.02.2017.
// */
//public class TestSpark {
//    public static void main(String[] args) {
//        System.setProperty("hadoop.home.dir", "d:\\winutil\\");
//        SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local[2]");
//        JavaSparkContext sc = new JavaSparkContext(conf);
//        // Load and parse the data
//        String path = "d:\\tmp\\weka\\common_2016.02.12.csv";
//        JavaRDD<String> data = sc.textFile(path);
//        JavaRDD<LabeledPoint> parsedData = data.map(
//                new Function<String, LabeledPoint>() {
//                    public LabeledPoint call(String line) {
//                        String[] parts = line.split(",");
//                        String[] features = parts[1].split(" ");
//                        double[] v = new double[features.length];
//                        for (int i = 0; i < features.length - 1; i++) {
//                            v[i] = Double.parseDouble(features[i]);
//                        }
//                        return new LabeledPoint(Double.parseDouble(parts[0]), Vectors.dense(v));
//                    }
//                }
//        );
//        parsedData.cache();
//
//// Building the model
//        int numIterations = 100;
//        double stepSize = 0.00000001;
//        final LinearRegressionModel model =
//                LinearRegressionWithSGD.train(JavaRDD.toRDD(parsedData), numIterations, stepSize);
//
//// Evaluate model on training examples and compute training error
//        JavaRDD<Tuple2<Double, Double>> valuesAndPreds = parsedData.map(
//                new Function<LabeledPoint, Tuple2<Double, Double>>() {
//                    public Tuple2<Double, Double> call(LabeledPoint point) {
//                        double prediction = model.predict(point.features());
//                        return new Tuple2<>(prediction, point.label());
//                    }
//                }
//        );
//        double MSE = new JavaDoubleRDD(valuesAndPreds.map(
//                new Function<Tuple2<Double, Double>, Object>() {
//                    public Object call(Tuple2<Double, Double> pair) {
//                        return Math.pow(pair._1() - pair._2(), 2.0);
//                    }
//                }
//        ).rdd()).mean();
//        System.out.println("training Mean Squared Error = " + MSE);
//
//// Save and load model
//        model.save(sc.sc(), "target/tmp/javaLinearRegressionWithSGDModel");
//        LinearRegressionModel sameModel = LinearRegressionModel.load(sc.sc(),
//                "target/tmp/javaLinearRegressionWithSGDModel");
//    }
//}
