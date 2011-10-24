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

