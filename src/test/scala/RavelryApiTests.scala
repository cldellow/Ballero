package cldellow.ballero.test

import cldellow.ballero.data._

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.coriander.oauth._

import org.json._

class RavelryApiTest extends Spec with ShouldMatchers {
  describe("ravelry API") {
    it("should list stores") {
      //http://maps.google.com/maps?q=waterloo,+on&hl=en&ll=43.463137,-80.51734&spn=0.042923,0.090723&sll=37.0625,-95.677068&sspn=47.617464,92.900391&vpsrc=6&hnear=Waterloo,+Waterloo+Regional+Municipality,+Ontario,+Canada&t=m&z=14
      println(Keys.appsign(
        "http://api.ravelry.com/shops/search.json",
        Map("lat" -> "43.463137", "shop_type_id" -> "1", "radius" -> "100",  "lng" -> "-80.51734")))
    }
  }
}
