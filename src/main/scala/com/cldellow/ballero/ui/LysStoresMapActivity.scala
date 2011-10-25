package com.cldellow.ballero.ui

import com.cldellow.ballero.R
import com.cldellow.ballero.data._
import com.cldellow.ballero.service._

import android.graphics._
import scala.collection.JavaConversions._
import android.app.Activity
import android.content._
import android.location._
import android.os.Bundle
import android.util.Log
import android.view._
import android.widget.TextView
import greendroid.app._
import greendroid.widget._
import com.google.android.maps._

class LysStoresMapActivity extends GDMapActivity with SmartActivity {
  val TAG = "FindLysActivity"
  var mapView: MapView = null
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setActionBarContentView(R.layout.lys_stores_map);

    mapView = find(R.id.map_view)
    mapView.setBuiltInZoomControls(true)
    val controller = mapView.getController
    val locationString = getIntent().getStringExtra(UiConstants.Location)
    val location = Parser.parse[GoogleLocation](locationString)
    controller.setCenter(new GeoPoint((location.lat * 1e6).toInt, (location.lng * 1e6).toInt))
    controller.setZoom(14)

    query(location.lat, location.lng)
  }

  override def isRouteDisplayed = false

  def query(lat: BigDecimal, lng: BigDecimal) {
    restServiceConnection.request(
      RestRequest(
        Keys.appsign("http://api.ravelry.com/shops/search.json",
          Map("lat" -> lat.toString, "units" -> "km", "lng" -> lng.toString, "shop_type_id" -> "1",
            "radius" -> "40")))) { response =>
      info("resp length: %s".format(response.body.length))
      val shops = Parser.parse[ShopResponse](response.body)
      info(shops.toString)
    }
  }
}
