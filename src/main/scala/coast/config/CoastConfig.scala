package coast.config

import akka.http.ServerSettings

/**
  * Created by reweber on 19/12/2015
  */
trait CoastConfig {
  def interface: Option[String]
  def port: Option[Int]
  def asyncParallelism: Option[Int]
  def serverSettings: Option[ServerSettings]
}
