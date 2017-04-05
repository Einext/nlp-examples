import java.io.{PrintWriter, StringReader}
import java.util.{Properties, List => JList}

import edu.stanford.nlp.ie.crf.CRFClassifier
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.parser.lexparser.LexicalizedParser
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.process.{CoreLabelTokenFactory, PTBTokenizer}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.util.CoreMap
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object HelloNLP {

  private val logger = LoggerFactory.getLogger(getClass)
  private val lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
    "-maxLength", "80", "-retainTmpSubcategories")
  private val pros = new Properties()
  pros.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, ner, sentiment")
  val pipeline = new StanfordCoreNLP(pros)


  def main(args: Array[String]): Unit = {

    var text = Source.fromFile("input/sentiment-input.txt").getLines().mkString
    val annotation = new Annotation(text)
    pipeline.annotate(annotation)

    /* Annotations available in annotation object*/
    annotation
      .keySet()
      .foreach(c => println(s"Annotations class: ${c.getName}"))


    val sentences = annotation.get(classOf[SentencesAnnotation])

    /* Find sentences and sentiment score*/
    val sentimentText = Array("Very Negative", "Negative", "Neutral", "Positive", "Very Positive")
    sentences
      .map{case(sentence:CoreMap) =>
        (sentence, sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree]))
      }.foreach{case(sentence, tree) =>
        val score = RNNCoreAnnotations.getPredictedClass(tree)
        println(s"Sentiment ---  sentence: $sentence  [${sentimentText(score)}]")
      }


    /* Find NER - named entity relationship */
    val model = "edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz"
    val classifier = CRFClassifier.getClassifierNoExceptions(model)

    classifier
      .classify(text)
      .asInstanceOf[JList[JList[CoreLabel]]]
      .foreach { coreLabels =>
        coreLabels.foreach {case(coreLabel:CoreLabel) =>
          val word:String = coreLabel.word()
          val category:String = coreLabel.get(classOf[AnswerAnnotation])
          println(s"NER ----- word: $word, category: $category")
        }
      }


    /* Find lemmas */
    val lemmas = ArrayBuffer.empty[String]
    sentences
      .foreach{sentence =>
          sentence
            .get(classOf[TokensAnnotation])
            .foreach(word => lemmas.append(word.get(classOf[LemmaAnnotation])))
      }

    lemmas.foreach{lemma =>
      println(s"Lemma --- $lemma")
    }

    /* Parts of speech */
    sentences
      .foreach{sentence =>
        sentence
          .get(classOf[TokensAnnotation])
          .foreach{token =>
            val word = token.get(classOf[TextAnnotation])
            val pos = token.get(classOf[PartOfSpeechAnnotation])
            println(s"POS -- $word [$pos]")
          }
      }

    /* Extracting relationship for question answer system */
    val question = "Who is the 32nd president of the United States?"
    val tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "")
    val tokenizer = tokenizerFactory.getTokenizer(new StringReader(question))
    val wordList = tokenizer.tokenize()
    val parseTree = lp.apply(wordList)

    val tlp = lp.treebankLanguagePack()
    val gsf = tlp.grammaticalStructureFactory()
    val gs = gsf.newGrammaticalStructure(parseTree)

    val tdl = gs.typedDependenciesCCprocessed()
    //println(tdl)

    tdl.foreach{dependency =>
      println(s"QA --- Governor word: [${dependency.gov().originalText()}] Relation: [${dependency.reln().getLongName}] Dependent Word: [${dependency.dep().originalText()}]")
    }


    /* Print json representation of an annotation */
    pipeline.jsonPrint(annotation, new PrintWriter(System.out))

    /* Print timing of function calls */
    println(pipeline.timingInformation())
  }
}
