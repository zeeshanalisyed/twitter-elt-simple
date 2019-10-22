package org.examples.hstream

import org.apache.log4j.Logger
import org.apache.log4j.BasicConfigurator;
import org.apache.spark.sql.SparkSession

object TweetMetrics {
  val logger = Logger.getLogger(getClass.getName)
  // generating low level model e.g. pos, nes, neu afterward high level models will come into game
  val dataFile: String = sys.env.get("TWITTER_DATA_FILE").map(_.toString).getOrElse("")
  def calculatePercentage(ratio: Float, percentile: Int = 100): Float = ratio*100
  def calculateRatio(t: Float, m: Float): Float = t/m 

  def main(args: Array[String]): Unit = {
    BasicConfigurator.configure();
    
    val ss = SparkSession.builder.appName("TweetMetrics").master("local[2]").getOrCreate()
    val tweets = ss.read.textFile(this.dataFile).cache()
    // just to keep it simple
    val pos = tweets.filter(_.contains("Positive"))
    val nes = tweets.filter(_.contains("Negative"))
    val neu = tweets.filter(_.contains("Neutral"))
    val total: Float = tweets.count;

    // Calculate Ratio
    val positiveRatio: Float = this.calculateRatio(pos.count: Float, total)
    val negativeRatio: Float = this.calculateRatio(nes.count: Float, total)
    val neutralRatio: Float = this.calculateRatio(neu.count: Float, total)
    // Calculate Percentile
    val positivePercent: Float = this.calculatePercentage(positiveRatio)
    val negativePercent: Float = this.calculatePercentage(negativeRatio)
    val neutralPercent: Float = this.calculatePercentage(neutralRatio)
    ss.stop();
    logger.info("Data Classifier running for 3 Classes Positive, Negative, Neutral")
    logger.info(s"How many percent of people talk about possitive stuff $positivePercent%")
    logger.info(s"How many percent of people talk about negitive stuff $negativePercent%")
    logger.info(s"How many percent of people are neutral $neutralPercent%")

  }
}