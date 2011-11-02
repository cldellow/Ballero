package com.cldellow.ballero.service

import java.net.URLEncoder

case class JsonParseRequest[T](data: String, parseFunc: String => List[T])
case class JsonParseResult[T](parsedVals: List[T])

