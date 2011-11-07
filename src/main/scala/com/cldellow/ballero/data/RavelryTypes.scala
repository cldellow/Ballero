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

case class Id(id: Int)

trait Key {
  def key: String
}

trait IdKey extends Key {
  def key = id.toString
  def id: Int
}

case class SimpleQueuedProject(
  id: Int,
  notes: Option[String],
  pattern_id: Option[Int]
) extends IdKey

case class QueuedProjects (queued_projects: List[SimpleQueuedProject])

case class RavelryQueueProjectWrapper(queued_project: RavelryQueue)

case class PatternWrapper(pattern: Pattern)
case class ProjectWrapper(project: Project)

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
  yarn_weight_description: Option[String]
) extends IdKey
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
  make_for: Option[String],
  name: String,
  var pattern: Option[Pattern],
  pattern_id: Option[Int],
  pattern_name: Option[String],
  sort_order: Int
)  extends Projectish with IdKey {
  def uiName: String = pattern.map { _.name }.getOrElse(pattern_name.getOrElse(name))
}


case class SimpleProjects (projects: List[Project])
case class Photo (
  id: Int,
  medium_url: String,
  small_url: String,
  square_url: Option[String],
  thumbnail_url: String
)

sealed trait ProjectStatus {
  def human: String
}

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
case object Hibernated extends ProjectStatus { def human = "zzz" }
case object Queued extends ProjectStatus { def human = "in queue" }
case object Frogged extends ProjectStatus { def human = "frogged" }
case object InProgress extends ProjectStatus { def human = "in progress" }
case object Finished extends ProjectStatus { def human = "finished" }
case object Unknown extends ProjectStatus { def human = "unknown" }

case class MinimalProjectish(
  id: Int,
  imgUrl: Option[String],
  order: Option[Int],
  status: Option[String],
  uiName: String
) {
  lazy val _actualStatus: ProjectStatus = ProjectStatus(status.getOrElse(""))
}

trait Projectish {
  def uiName: String
}

/*
"packs": [{
            "yarn_id": 69628,
            "yarn_weight": {
                "name": "Lace",
                "min_gauge": null,
                "wpi": null,
                "crochet_gauge": "",
                "ply": "2",
                "knit_gauge": "32-34",
                "max_gauge": null
            },
            "total_grams": 100,
            "colorway": "Dark blues",
            "shop_id": null,
            "prefer_metric_weight": true,
            "prefer_metric_length": false,
            "ounces_per_skein": 3.53,
            "dye_lot": "",
            "skeins": 1.0,
            "id": 19389993,
            "shop_name": "KW Knitters Guild",
            "grams_per_skein": 100,
            "color_family_id": 9,
            "total_meters": 699.5,
            "stash_id": 5470401,
            "yarn": {
                "permalink": "handmaiden-fine-yarn-marrakesh",
                "yarn_company_id": 426,
                "name": "Marrakesh",
                "id": 69628,
                "yarn_company_name": "Handmaiden Fine Yarn"
            },
            "total_ounces": 3.53,
            "personal_name": null,
            "meters_per_skein": 699.5,
            "yarn_name": "Handmaiden Fine Yarn Marrakesh",
            "yards_per_skein": 765.0,
            "total_yards": 765.0
        }],
        */

case class Yarn(
  id: Int,
  name: Option[String],
  permalink: Option[String],
  yarn_company_name: Option[String]
)

case class YarnPack (
  colorway: Option[String],
  skeins: Option[BigDecimal],
  total_grams: Option[Int],
  total_yards: Option[BigDecimal],
  yarn: Option[Yarn],
  yarn_id: Option[Int],
  yarn_name: Option[String]
)
case class Project (
  completed: Option[String],
  craft_name: Option[String],
  first_photo: Option[Photo],
  id: Int,
  made_for: Option[String],
  name: Option[String],
  needle_sizes: Option[List[RavelryNeedle]],
  notes: Option[String],
  packs: Option[List[YarnPack]],
  pattern_id: Option[Int],
  pattern_name: Option[String],
  permalink: String,
  photos: Option[List[Photo]],
  progress: Option[Int],
  rating: Option[Int],
  started: Option[String],
  /* "In progress", "Finished" */
  status_name: Option[String],
  tag_names: Option[List[String]]
) extends Projectish with IdKey {
  /* TODO: parse the other statuses */
  def status: ProjectStatus = status_name.getOrElse("In progress") match {
    case "Hibernating" => Hibernated
    case "Frogged" => Frogged
    case "In progress" => InProgress
    case "Finished" => Finished
    case _ => Unknown
  }

  def uiName: String = name.getOrElse { pattern_name.getOrElse("bugbug: no name!") }
}

/**
   "hook": "E",
            "metric": 3.5,
            "us": "4 ",
            "us_steel": "00",
            "name": "US 4  - 3.5 mm",
            "knitting": true,
            "id": 4,
            "crochet": false
*/
case class RavelryNeedle(
  crochet: Boolean,
  hook: Option[String],
  knitting: Boolean,
  metric: Option[BigDecimal],
  name: Option[String],
  us: Option[String],
  us_steel: Option[String]
)

case class Needle(
  comment: String,
  gaugeMetric: BigDecimal,
  /** Seriously, US. WTF. */
  gaugeUSString: Option[String],
  id: Int,
  kind: String,
  lengthDecimal: Option[BigDecimal],
  lengthString: Option[String]
) extends IdKey

case class LocalYarnStore(
  address: Option[String],
  city: String,
  latitude: BigDecimal,
  longitude: BigDecimal,
  name: String,
  phone: Option[String],
  shop_email: Option[String],
  site: Option[String],
  twitter_id: Option[String],
  url: Option[String],
  zip: Option[String]
)

case class ShopResponse(
  shops: List[LocalYarnStore]
)
