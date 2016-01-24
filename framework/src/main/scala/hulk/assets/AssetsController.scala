package hulk.assets

import java.nio.file.Paths

import hulk.http.response.Binary
import hulk.http.{NotFound, Ok, Action}

import scala.io.{Codec, Source}
import scala.util.Try

/**
  * Created by reweber on 23/01/2016
  */
class AssetsController {

  def get(path: Option[String] = None) = Action { request =>
    val assetOpt = request.requestParams.get("file")
    assetOpt.flatMap { asset =>
      val sourceOpt = Try(path.map(p => Source.fromFile(Paths.get(p, asset).toString))).toOption.flatten

      sourceOpt.map { source =>
        val reader = source.reader()
        val bytes = Iterator.continually(reader.read).takeWhile(_ != -1).map(_.toByte).toArray
        Ok[Binary](bytes)
      }
    }.getOrElse(NotFound())
  }
}
