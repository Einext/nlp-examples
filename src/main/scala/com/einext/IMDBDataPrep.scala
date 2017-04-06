package com.einext

import java.io.{BufferedOutputStream, File, FileOutputStream}

import org.slf4j.LoggerFactory
import play.api.libs.json.{JsObject, JsString}

import scala.io.Source



/*
*  Download IMDB curated data from http://ai.stanford.edu/~amaas/data/sentiment/aclImdb_v1.tar.gz
*  Unzip it a location of your choice and pass the location as command line argument
*
* */

object IMDBDataPrep {

  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    if(args.length < 1){
      println("Specify the directory that contains the IMDB dataset. Link to download http://ai.stanford.edu/~amaas/data/sentiment/aclImdb_v1.tar.gz")
      System.exit(1)
    }

    val base = args(0)
    logger.info(s"Location of IMDB dataset: $base")

    val outputFile = new File("output/imdb-comments.json")

    outputFile.toPath.getParent.toFile.mkdirs()

    val output = new BufferedOutputStream(new FileOutputStream(outputFile))
    logger.info(s"Output will be stored in ${outputFile.getAbsolutePath}")

    for (label <- Array("test", "train"); sentiment <- Array("pos", "neg")) {
      val d = new File(s"$base/$label/$sentiment")
      if(d.exists() && d.isDirectory){
        val files = d
                      .listFiles()
                      .filter(_.isFile)
                      .toList
        files.foreach{f =>
          val bs = Source.fromFile(f)
          val content = bs.getLines().mkString
          bs.close()

          val js = JsObject(Seq(
            "label" -> JsString(label),
            "sentiment" -> JsString(sentiment),
            "name" -> JsString(f.getName),
            "content" -> JsString(content)
          ))
          output.write((js.toString() + "\n").getBytes)
        }
      }else{
        println(d.getPath + " is not a valid directory")
      }

    }
    output.flush()
    output.close()


  }

}
