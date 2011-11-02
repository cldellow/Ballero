package com.cldellow.ballero.ui

import com.google.android.maps._
import com.cldellow.ballero.data._

class BalloonOverlayItem(point: GeoPoint, val shop: LocalYarnStore)
extends OverlayItem(point, shop.name, "") {
  override def getSnippet: String = List[Option[String]](shop.address, shop.zip).flatten.mkString("\n")
}

// vim: set ts=2 sw=2 et:
