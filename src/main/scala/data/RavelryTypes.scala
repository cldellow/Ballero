package cldellow.ballero.data

import xml.{XML, NodeSeq}

case class Needle(
  comment: String,
  gaugeMetric: BigDecimal,
  gaugeUS: Option[BigDecimal],
  kind: String,
  lengthDecimal: Option[BigDecimal],
  lengthString: Option[String]
)

object NeedleHelpers {
  def parse(xml: NodeSeq): List[Needle] = {
    val needleTable = (xml \\ "table") filter { node => (node \ "@id").text == "needle_details" }
    println(needleTable)
    Nil
  }
}


// vim: set ts=2 sw=2 et:
