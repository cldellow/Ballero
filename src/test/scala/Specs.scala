import cldellow.ballero
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec

import org.json._

class Specs extends Spec with ShouldMatchers {
  describe("json parser") {
    it("can parse an object") {
      val jsonObject = new JSONObject("""{ "foo": "bar" }""")
    }
  }
}
