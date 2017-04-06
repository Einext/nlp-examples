package com.einext

import java.io.{BufferedWriter, FileWriter}

import org.slf4j.LoggerFactory
import play.api.libs.json.Json

import scala.io.Source
import scala.util.Random


/*
* Download the wordnet data file (click on link WNdb-3.0.tar.gz) https://wordnet.princeton.edu/wordnet/download/current-version/
* Parsing Doc http://wordnet.princeton.edu/wordnet/man/wndb.5WN.html
*
* */

object WordNet {



  private val logger = LoggerFactory.getLogger(getClass)

  def getRecords(fileName: String): Array[String] = {
    Source
      .fromFile(fileName)
      .bufferedReader
      .lines
      .toArray
      .map(_.toString)
      .filter { line => !line.startsWith("  ") && line.nonEmpty }
  }


  def main(args: Array[String]): Unit = {

    //Base path must contain the index files and data files. See above for the download links
    val baseDir = args(0)
    logger.info(s"Path: $baseDir")


    val lemmaData = Array("adj", "adv", "noun", "verb")
      .flatMap(pos => getRecords(s"$baseDir/data.$pos"))
    .map{line =>
      val offset = line.split(" ")(0)
      val glossary = line.split("\\|")(1)
      offset -> glossary
    }.toMap[String, String]

    println(s"Total no of records in LemmaData: ${lemmaData.size}")

    val OffSetPattern = """(\d{8})""".r

    val lemmaIndex = Array("adj", "adv", "noun", "verb")
      .flatMap(pos => getRecords(s"$baseDir/index.$pos"))
      .map { line =>
        val tokens = line.split(" ")
        val lemma = tokens(0).replace("-", " ")
        val pos = tokens(1)
        val synset_cnt = tokens(2).toInt
        val synset_offsets = OffSetPattern.findAllIn(line).toList
        val glossaries = synset_offsets.map(offset => lemmaData .get(offset)).filter(_.isDefined).map(_.get)
        Lemma(lemma, pos, glossaries)
      }

    println(s"Total no of records in lemmaIndex: ${lemmaIndex.size}")

    val threshold = 100.0 / lemmaIndex.length

    lemmaIndex.filter(_ => Random.nextDouble() <= threshold).foreach(println)

    implicit val LemmaFormat = Json.format[Lemma]

    lemmaIndex.take(10).map(l => Json.toJson(l).toString()).foreach(println)

    val writer = new BufferedWriter(new FileWriter("output/dictionary.json"))

    lemmaIndex.foreach(e => writer.write(Json.toJson(e) + "\n"))

  }

}


case class Lemma(w: String, pos: String, glos: List[String])



