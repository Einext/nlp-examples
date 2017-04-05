import java.io.{BufferedOutputStream, File, FileOutputStream}

import play.api.libs.json.{JsObject, JsString}

import scala.io.Source


object DataPrep {

  def main(args: Array[String]): Unit = {

    val base = "/Volumes/SONY/Data/IMDB-review"

    val output = new BufferedOutputStream(new FileOutputStream(new File("output.json")))

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
