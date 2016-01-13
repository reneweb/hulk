package hulk.config

import akka.http.ServerSettings

/**
  * Created by reweber on 19/12/2015
  */
trait HulkConfig {
  def versioning: Option[Versioning] = None

  def interface: Option[String] = None
  def port: Option[Int] = None
  def asyncParallelism: Option[Int] = None
  def serverSettings: Option[ServerSettings] = None
}

trait Versioning

private[hulk] case class PathVersioning() extends Versioning
private[hulk] case class AcceptHeaderVersioning(versionRegex: String = "v[0-9]+") extends Versioning
private[hulk] case class AcceptVersionHeaderVersioning() extends Versioning

object Versioning {
  def path = new PathVersioning()
  def acceptHeader = new AcceptHeaderVersioning()
  def acceptHeader(versionRegex: String) = new AcceptHeaderVersioning(versionRegex)
  def acceptVersionHeader = new AcceptVersionHeaderVersioning()
}


