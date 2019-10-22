package org.zlogjs.hstream

import java.io._
import scala.io.Source
import scala.reflect.runtime.universe._
import java.nio.charset.Charset
import java.util.Properties
import org.apache.log4j.Logger

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}

import edu.stanford.nlp.coref.CorefCoreAnnotations
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.ling.CoreAnnotations.{NamedEntityTagAnnotation, LemmaAnnotation, PartOfSpeechAnnotation, SentencesAnnotation, TextAnnotation, TokensAnnotation}
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.util.CoreMap

import scala.collection.JavaConverters._

object Configs {
  def getConfigs(key: String): String = sys.env.get(key).map(_.toString).getOrElse("")
}

object TwitterStream {
  @transient lazy val logger = Logger.getLogger(getClass.getName)
  val dataFile: String = Configs.getConfigs("TWITTER_DATA_FILE")
  var tweetCounter: Int = 0;
  val consumerToken = ConsumerToken(key = Configs.getConfigs("TCKEY"), secret = Configs.getConfigs("TCSEC"))
  val accessToken = AccessToken(key = Configs.getConfigs("TTKEY"), secret = Configs.getConfigs("TTSEC"))  

  // val restClient = TwitterRestClient(consumerToken, accessToken)

  val client = TwitterStreamingClient(consumerToken=consumerToken, accessToken=accessToken)

  val props: Properties = new Properties()
  props.put("annotators", "tokenize, ssplit, parse, sentiment")
  
  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  def printTweetText: PartialFunction[StreamingMessage, Unit] = {
      case tweet: Tweet => println(tweet.text)
  }
  def tokenizeTweetText: PartialFunction[StreamingMessage, Unit] = {
      
      case tweet: Tweet => {
        val document: Annotation = new Annotation(tweet.text)
        pipeline.annotate(document)
        val sentences: List[CoreMap] = document.get(classOf[SentencesAnnotation]).asScala.toList

        (for {
          sentence: CoreMap <- sentences
          token: CoreLabel <- sentence.get(classOf[TokensAnnotation]).asScala.toList
          word: String = token.get(classOf[TextAnnotation])
    
        } yield (word, token)) foreach(t => println("word: " + t._1 + " token: " +  t._2))
      }
  }
  def getSentiment(sentiment: Int): String = sentiment match {
    case x if x == 0 || x == 1 => "Negative"
    case 2 => "Neutral"
    case x if x == 3 || x == 4 => "Positive"
  }
  def printType[T](x: T)(implicit tag: TypeTag[T]): Unit = println(tag.tpe.toString) 
  def npAnalysisTweetText: PartialFunction[StreamingMessage, Unit] = {
      
      case tweet: Tweet => {
        val document: Annotation = new Annotation(tweet.text)
        pipeline.annotate(document)
        val sentences: List[CoreMap] = document.get(classOf[SentencesAnnotation]).asScala.toList

         // Check if positive sentiment sentence is truly positive
         sentences
          .map(sentence => (sentence, sentence.get(classOf[SentimentAnnotatedTree])))
          .map { case (sentence, tree) => (sentence.toString, this.getSentiment(RNNCoreAnnotations.getPredictedClass(tree))) }
          .foreach { line =>
            println(line)
            this.tweetCounter = 1 + this.tweetCounter;
            val wp = new BufferedWriter(new FileWriter(dataFile, true))
            wp.write(line.toString)
            wp.write("\r\n")
            wp.flush
            wp.close
          }
      }
  }
  def main(args: Array[String]): Unit = {
    client.sampleStatuses(stall_warnings = true)(this.npAnalysisTweetText)
  }
}