package cldellow.ballero.service

import java.net.URLEncoder

sealed trait HttpVerb
case object GET extends HttpVerb
case object POST extends HttpVerb
case class RestRequest(url: String, verb: HttpVerb = GET, params: Map[String, String] = Map()) {
  def getParams: String =
    if(params.isEmpty)
      ""
    else
      "?%s".format(
        params.map { case(k, v) => "%s=%s".format(k, URLEncoder.encode(v, "UTF-8")) }
          .mkString("&"))
}

case class RestResponse(status: Int, body: String) {
}


// vim: set ts=2 sw=2 et:
