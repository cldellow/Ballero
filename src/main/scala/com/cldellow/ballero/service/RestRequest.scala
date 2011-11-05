package com.cldellow.ballero.service

import java.net.URLEncoder

sealed trait HttpVerb
case object GET extends HttpVerb
case object POST extends HttpVerb
case class RestRequest[T](url: String, verb: HttpVerb = GET, params: Map[String, String] = Map(),
  parseFunc: String => List[T]) {
  def getParams: String =
    if(params.isEmpty)
      ""
    else
      "?%s".format(
        params.map { case(k, v) => "%s=%s".format(k, URLEncoder.encode(v, "UTF-8")) }
          .mkString("&"))
}

sealed abstract class StatusCode(val code: Int)

case object OK extends StatusCode(200)
case class UnknownCode(override val code: Int) extends StatusCode(code)

object StatusCode {
  def apply(code: Int): StatusCode = code match {
    case 200 => OK
    case _ => UnknownCode(code)
  }
}

case class RestResponse[T](status: Int, body: String, statusMessage: String, var parsedVals: List[T]) {
  lazy val statusCode: StatusCode = StatusCode(status)
}


// vim: set ts=2 sw=2 et:
