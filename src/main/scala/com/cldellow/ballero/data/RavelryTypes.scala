package com.cldellow.ballero.data

import xml.{XML, NodeSeq}

case class AuthResponse (
  auth_token: Option[String],
  signing_key: Option[String]
)

case class OAuthCredential (
  auth_token: String,
  signing_key: String
)

case class Id (id: Int)
case class QueuedProjects (queued_projects: List[Id])
case class SimpleProjects (projects: List[Project])
case class Photo (
  id: Int,
  medium_url: String,
  small_url: String,
  square_url: String,
  thumbnail_url: String
)

sealed trait ProjectStatus
case object InProgress extends ProjectStatus
case object Finished extends ProjectStatus
case object Unknown extends ProjectStatus

case class Project (
  first_photo: Option[Photo],
  id: Int,
  made_for: String,
  name: String,
  pattern_id: Option[Int],
  pattern_name: String,
  permalink: String,
  progress: Option[Int],
  /* "In progress", "Finished" */
  status_name: String
) {
  def status: ProjectStatus = status_name match {
    case "In progress" => InProgress
    case "Finished" => Finished
    case _ => Unknown
  }
}

case class Needle(
  comment: String,
  gaugeMetric: BigDecimal,
  gaugeUS: Option[BigDecimal],
  kind: String,
  lengthDecimal: Option[BigDecimal],
  lengthString: Option[String]
)

case class LocalYarnStore(
  address: String,
  city: String,
  latitude: BigDecimal,
  longitude: BigDecimal,
  name: String,
  phone: String,
  shop_email: Option[String],
  site: String,
  twitter_id: Option[String],
  url: String,
  zip: String
)

case class ShopResponse(
  shops: List[LocalYarnStore]
)
