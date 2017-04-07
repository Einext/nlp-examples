package com.einext

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


object NLPQA {

  private val logger = LoggerFactory.getLogger(getClass)
  private val lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz",
    "-maxLength", "80", "-retainTmpSubcategories")


  def parseQuestion(statement:String):Unit = {
    /* Extracting relationship for question answer system */
    val tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "")
    val tokenizer = tokenizerFactory.getTokenizer(new StringReader(statement))
    val wordList = tokenizer.tokenize()
    val parseTree = lp.apply(wordList)

    val tlp = lp.treebankLanguagePack()
    val gsf = tlp.grammaticalStructureFactory()
    val gs = gsf.newGrammaticalStructure(parseTree)

    val tdl = gs.typedDependenciesCCprocessed()
    //println(tdl)

    tdl.foreach{dependency =>
      val govr = dependency.gov().originalText()
      val rel = dependency.reln().getLongName
      val dep = dependency.dep().originalText()
      println(s"QA --- Governor word: [$govr] Relation: [$rel] Dependent Word: [$dep]")
    }
  }


  def main(args: Array[String]): Unit = {
    while(1> 0){
      val statement = io.StdIn.readLine("> ")
      parseQuestion(statement)
    }


  }
}
