package com.cldellow.ballero.test

case class CaseBoolean(foo: Boolean)
case class OneString(foo: String)
case class TwoStringsDefault(foo: String, bar: String = "foo")
case class StringInt(foo: String, bar: Int)
case class ListString(foo: List[String])
case class ListCaseClass(foo: List[OneString])
case class CaseClassWithCaseClass(foo: OneString)
case class OptionalString(foo: Option[String])

