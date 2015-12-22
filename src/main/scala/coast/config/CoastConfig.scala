package coast.config

import akka.http.ServerSettings

/**
  * Created by reweber on 19/12/2015
  */
trait CoastConfig {
  def interface: Option[String] = None
  def port: Option[Int] = None
  def asyncParallelism: Option[Int] = None
  def serverSettings: Option[ServerSettings] = None
}
