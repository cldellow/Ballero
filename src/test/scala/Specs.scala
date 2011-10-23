package cldellow.ballero.test;
import cldellow.ballero.data._

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec

import org.json._

case class OneString(foo: String)
case class TwoStringsDefault(foo: String, bar: String = "foo")
case class StringInt(foo: String, bar: Int)
case class ListString(foo: List[String])
case class ListCaseClass(foo: List[OneString])
case class CaseClassWithCaseClass(foo: OneString)
case class OptionalString(foo: Option[String])

class Specs extends Spec with ShouldMatchers {
  describe("json parser") {
    it("can parse a case class with 1 string") {
      val x: OneString = Parser.parse[OneString]("""{ "foo" : "bar" }""")
      assert(x === OneString("bar"))
    }
    it("can parse a case class with 1 string, and 1 defualt argument") {
      val x: TwoStringsDefault = Parser.parse[TwoStringsDefault]("""{ "foo" : "bar" }""")
      assert(x === TwoStringsDefault("bar", "foo"))
    }
    it("can parse a case class with list of strings") {
      val x: ListString = Parser.parse[ListString]("""{ "foo" : ["bar", "baz"] }""")
      assert(x === ListString(List("bar", "baz")))
    }
    it("can parse a case class with list of case classes") {
      val x: ListCaseClass = Parser.parse[ListCaseClass]("""{ "foo" : [ { "foo" : "bar" } ] }""")
      assert(x === ListCaseClass(List(OneString("bar"))))
    }
    it("can parse a case class with a case class") {
      val x: CaseClassWithCaseClass = Parser.parse[CaseClassWithCaseClass](
        """{ "foo" : { "foo" : "bar" } }""")
      assert(x === CaseClassWithCaseClass(OneString("bar")))
    }
    it("can parse a case class with a string/int") {
      val x: StringInt = Parser.parse[StringInt](
        """{ "foo" : "foo", "bar": 3 }""")
      assert(x === StringInt("foo", 3))
    }
    it("can parse a case class with an optional string") {
      val x: OptionalString = Parser.parse[OptionalString]("{}")
      assert(x == OptionalString(None))
      val y: OptionalString = Parser.parse[OptionalString]("""{"foo" : "bar"}""")
      assert(y == OptionalString(Some("bar")))
    }
    it("can parse the Google result") {
      val x: GoogleResponse = Parser.parse[GoogleResponse](Inputs.GoogleResponse1)
    }
    it("can parse the Google Waterloo result") {
      val x: GoogleResponse = Parser.parse[GoogleResponse](Inputs.GoogleResponse2)
    }
    it("can parse the empty Google result") {
      val x: GoogleResponse = Parser.parse[GoogleResponse]("""{   "results" : [],   "status" : "ZERO_RESULTS"}""")
      println(x.results)
      println(x.status)
    }
  }
}
