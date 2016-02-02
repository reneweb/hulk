package hulk.config

import akka.actor.ActorSystem
import akka.http.ServerSettings
import com.codahale.metrics.MetricRegistry
import hulk.config.versioning.{AcceptHeaderVersioning, Versioning}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

/**
  * Created by reweber on 02/02/2016
  */
class HulkConfigTest extends Specification {

  "HulkConfig#versioning" should {
    "return some versioning if set" >> {
      val acceptHeaderVersioning = Some(new AcceptHeaderVersioning())

      val config = new HulkConfig {
        override def versioning: Option[Versioning] = acceptHeaderVersioning
      }

      config.versioning must equalTo(acceptHeaderVersioning)
      config.versioning.get must equalTo(AcceptHeaderVersioning("v[0-9]+"))
    }
    "return none if not set" >> {
      val config = new HulkConfig {
        override def versioning: Option[Versioning] = None
      }

      config.versioning must equalTo(None)
    }
  }

  "HulkConfig#interface" should {
    "return some interface if set" >> {
      val i = Some("localhost")

      val config = new HulkConfig {
        override def interface: Option[String] = i
      }

      config.interface must equalTo(i)
    }
    "return none if not set" >> {
      val config = new HulkConfig {
        override def interface: Option[String] = None
      }

      config.interface must equalTo(None)
    }
  }

  "HulkConfig#port" should {
    "return some interface if set" >> {
      val i = Some(1000)

      val config = new HulkConfig {
        override def port: Option[Int] = i
      }

      config.port must equalTo(i)
    }
    "return none if not set" >> {
      val config = new HulkConfig {
        override def port: Option[Int] = None
      }

      config.port must equalTo(None)
    }
  }

  "HulkConfig#asyncParallelism" should {
    "return some interface if set" >> {
      val i = Some(5)

      val config = new HulkConfig {
        override def asyncParallelism: Option[Int] = i
      }

      config.asyncParallelism must equalTo(i)
    }
    "return none if not set" >> {
      val config = new HulkConfig {
        override def asyncParallelism: Option[Int] = None
      }

      config.asyncParallelism must equalTo(None)
    }
  }

  "HulkConfig#serverSettings" should {
    "return some interface if set" >> {
      val i = Some(ServerSettings(None)(ActorSystem()))

      val config = new HulkConfig {
        override def serverSettings: Option[ServerSettings] = i
      }

      config.serverSettings must equalTo(i)
    }
    "return none if not set" >> {
      val config = new HulkConfig {
        override def serverSettings: Option[ServerSettings] = None
      }

      config.serverSettings must equalTo(None)
    }
  }

  "HulkConfig#metricRegistry" should {
    "return some metricRegistry if set" >> {
      val i = Some(new MetricRegistry)

      val config = new HulkConfig {
        override def metricRegistry: Option[MetricRegistry] = i
      }

      config.metricRegistry must equalTo(i)
    }
    "return none if not set" >> {
      val config = new HulkConfig {
        override def metricRegistry: Option[MetricRegistry] = None
      }

      config.metricRegistry must equalTo(None)
    }
  }
}
