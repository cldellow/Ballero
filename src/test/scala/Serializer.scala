package com.cldellow.ballero.test

import com.cldellow.ballero.data._

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec

import org.json._

class Serializer extends Spec with ShouldMatchers {
  describe("json serializer") {
    it("can serialize a case class with string") {
      val out = Parser.serialize(OneString("bar"))
      assert(out === """{"foo":"bar"}""")
    }
    it("can serialize a case class with an optional string present") {
      val out = Parser.serialize(OptionalString(Some("bar")))
      assert(out === """{"foo":"bar"}""")
    }
    it("can serialize a case class with an optional string absent") {
      val out = Parser.serialize(OptionalString(None))
      assert(out === """{}""")
    }
    it("can serialize a case class with a string/int") {
      val out = Parser.serialize(StringInt("foo", 3))
      assert(out === """{"foo":"foo","bar":3}""")
    }
    it("can serialize a case class with an empty list of string") {
      val out = Parser.serialize(ListString(Nil))
      assert(out === """{"foo":[]}""")
    }
    it("can serialize a case class with a list of string") {
      val out = Parser.serialize(ListString(List("foo", "bar")))
      assert(out === """{"foo":["foo","bar"]}""")
    }
    it("can serialize a case class with a case class") {
      val out = Parser.serialize(CaseClassWithCaseClass(OneString("baz")))
      assert(out === """{"foo":{"foo":"baz"}}""")
    }
    it("can serialize a case class with a list of case class") {
      val out = Parser.serialize(ListCaseClass(List(OneString("baz"), OneString("luhrman"))))
      assert(out === """{"foo":[{"foo":"baz"},{"foo":"luhrman"}]}""")
    }
    it("can serialize a case class with a boolean") {
      val out = Parser.serialize(CaseBoolean(true))
      assert(out === """{"foo":true}""")
      val out2 = Parser.serialize(CaseBoolean(false))
      assert(out2 === """{"foo":false}""")
    }
  }
}
