package hulk.config

import akka.http.scaladsl.settings.ServerSettings
import com.codahale.metrics.MetricRegistry
import hulk.config.versioning.Versioning

/**
  * Created by reweber on 19/12/2015
  */
trait HulkConfig {
  def versioning: Option[Versioning] = None

  def interface: Option[String] = None
  def port: Option[Int] = None
  def asyncParallelism: Option[Int] = None
  def serverSettings: Option[ServerSettings] = None

  def metricRegistry: Option[MetricRegistry] = None
}


