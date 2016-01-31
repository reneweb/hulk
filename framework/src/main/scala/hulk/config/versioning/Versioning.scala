package hulk.config.versioning

/**
  * Created by reweber on 31/01/2016
  */
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
