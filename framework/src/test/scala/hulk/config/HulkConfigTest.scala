package hulk.config

import hulk.config.versioning.{AcceptHeaderVersioning, Versioning}
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
}
