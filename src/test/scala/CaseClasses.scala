package com.cldellow.ballero.test

case class CaseBoolean(foo: Boolean)
case class OneString(foo: String)
case class StringInt(bar: Int, foo: String)
case class ListString(foo: List[String])
case class ListCaseClass(foo: List[OneString])
case class OptionListCaseClass(foo: Option[List[OneString]])
case class CaseClassWithCaseClass(foo: OneString)
case class OptionalString(foo: Option[String])

