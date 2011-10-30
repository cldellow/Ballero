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

case class SimpleQueuedProject(
  id: Int,
  pattern_id: Option[Int]
)
case class QueuedProjects (queued_projects: List[SimpleQueuedProject])

case class RavelryQueueProjectWrapper(queued_project: RavelryQueue)

case class PatternWrapper(pattern: Pattern)

case class Pattern(
  difficulty_average: BigDecimal,
  gauge: Option[BigDecimal],
  gauge_description: Option[String],
  gauge_divisor: Option[Int],
  gauge_pattern: Option[String],
  id: Int,
  name: String,
  notes: Option[String],
  permalink: String,
  photos: Option[List[Photo]],
  price: Option[BigDecimal],
  row_gauge: Option[BigDecimal],
  yardage: Option[Int],
  yarn_weight_description: String
)
/*
        "pattern": {
            "gauge_description": "20 stitches and 32 rows = 4 inches in stockinette stitch",
            "row_gauge": 32.0,
            "price": 6.5,
            "permalink": "kleio",
            "name": "Kleio",
            "yarn_weight_description": "Fingering / 4 ply (14 wpi)",
            "notes" : "Blah",
            "gauge" : 20.0,
            "gauge_divisor": 4,
            "yardage": 740,
            "rating_average": 4.77611940298507,
            "difficulty_average": 4.35483870967742,


            //not parsed
            "gauge_pattern": "stockinette stitch",
            "queued_projects_count": 767,
            "yardage_max": null,
            "downloadable": true,
            "difficulty_count": 62,
            "pattern_needle_sizes": [{
                "us": "5 ",
                "hook": "F",
                "metric": 3.75,
                "name": "US 5  - 3.75 mm",
                "us_steel": null,
                "id": 5,
                "knitting": true,
                "crochet": false
            }],
            "published": "2011/09/01",
            "projects_count": 174,
            "url": "",
            "yardage_description": "740 yards",
            "id": 250441,
            "free": false,
            "rating_count": 67,
            "pdf_url": "",
            "favorites_count": 2296,
            "comments_count": 11
        },
        */

        /* not parsed -- detailed queue
        "yarn_id": null,
        "pattern_name": "Kleio by Rosemary (Romi) Hill",
        "pattern_id": 250441,
        "created_at": "2011/10/26 09:02:06 -0400",
        "sort_order": 4,
        "skeins": null,
        "finish_by": null,
        "user_id": 1758876,
        "start_on": null,
        "name": "Kleio by Rosemary (Romi) Hill",
        "queued_stashes": [],
        "yarn_name": ""
        */
case class RavelryQueue(
  id: Int,
  make_for: String,
  name: String,
  pattern: Option[Pattern],
  pattern_id: Option[Int],
  pattern_name: Option[String],
  sort_order: Int
)  extends Projectish {
  def uiName: String = pattern.map { _.name }.getOrElse(pattern_name.getOrElse(name))
}


case class SimpleProjects (projects: List[Project])
case class Photo (
  id: Int,
  medium_url: String,
  small_url: String,
  square_url: String,
  thumbnail_url: String
)

sealed trait ProjectStatus
object ProjectStatus {
  def apply(str: String): ProjectStatus = str match {
    case "Hibernated" => Hibernated
    case "Queued" => Queued
    case "Frogged" => Frogged
    case "InProgress" => InProgress
    case "Finished" => Finished
    case _ => Unknown
  }
}
case object Hibernated extends ProjectStatus
case object Queued extends ProjectStatus
case object Frogged extends ProjectStatus
case object InProgress extends ProjectStatus
case object Finished extends ProjectStatus
case object Unknown extends ProjectStatus

trait Projectish {
  def uiName: String
}

case class Project (
  first_photo: Option[Photo],
  id: Int,
  made_for: String,
  name: String,
  pattern_id: Option[Int],
  pattern_name: Option[String],
  permalink: String,
  progress: Option[Int],
  /* "In progress", "Finished" */
  status_name: String
) extends Projectish{
  /* TODO: parse the other statuses */
  def status: ProjectStatus = status_name match {
    case "In progress" => InProgress
    case "Finished" => Finished
    case _ => Unknown
  }

  def uiName: String = name
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
  site: Option[String],
  twitter_id: Option[String],
  url: String,
  zip: String
)

case class ShopResponse(
  shops: List[LocalYarnStore]
)
