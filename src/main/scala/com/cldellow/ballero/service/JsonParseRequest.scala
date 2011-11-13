package com.cldellow.ballero.service

import java.net.URLEncoder

case class JsonParseRequest[T](data: String,
  dataBytes: Array[Byte],
  dataBytesSize: Int,
  parseFunc: String => List[T],
  parseBytesFunc: (Array[Byte], Int) => List[T])

case class JsonParseResult[T](parsedVals: List[T])

object JsonParseRequest {
  def apply[T](data: String, parseFunc: String => List[T]): JsonParseRequest[T] =
    JsonParseRequest[T](data, null, 0, parseFunc, null)

  def apply[T](data: Array[Byte], size: Int, parseFunc: (Array[Byte], Int) => List[T]): JsonParseRequest[T] =
    JsonParseRequest[T](null, data, size, null, parseFunc)

  def apply[T](data: Array[Byte], size: Int, parseFunc: ParseFuncs[T]): JsonParseRequest[T] =
    JsonParseRequest[T](null, data, size, parseFunc.fromString, parseFunc.fromBytes)
}
