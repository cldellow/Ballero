package com.cldellow.ballero.ui

import com.cldellow.ballero.R
import com.cldellow.ballero.data._
import com.cldellow.ballero.service._

import android.graphics._
import scala.collection.JavaConversions._
import android.app.Activity
import android.content._
import android.content.res._
import android.graphics.drawable._
import android.location._
import android.os.Bundle
import android.util.Log
import android.view._
import android.widget.TextView
import greendroid.app._
import greendroid.graphics.drawable._
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

  private def makeDetails(shop: LocalYarnStore): String = {
    import shop._
    List[Option[String]](Some(address), Some(zip), shop_email, Some(phone), twitter_id map { "@" + _ }, Some(url))
      .flatten.mkString("\n")
  }

  private def handleResponse(shops: ShopResponse) {
    if (shops.shops.isEmpty) {
      toast("Sorry, no nearby stores!")
      return
    }

    val overlays = mapView.getOverlays
    val drawable = getResources().getDrawable(R.drawable.marker)

    val basicOverlay = new StoreOverlay(drawable, mapView)
  //Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
//HelloItemizedOverlay itemizedoverlay = new HelloItemizedOverlay(drawable, this);
    shops.shops.foreach { shop =>
      val point = new GeoPoint((shop.latitude * 1e6).toInt, (shop.longitude * 1e6).toInt)
      val overlayItem = new BalloonOverlayItem(point, shop)
      basicOverlay.addOverlay(overlayItem)
    }

    overlays.add(basicOverlay)
    mapView.invalidate()
  }

  def query(lat: BigDecimal, lng: BigDecimal) {
    restServiceConnection.request(
      RestRequest(
        Keys.appsign("http://api.ravelry.com/shops/search.json",
          Map("lat" -> lat.toString, "units" -> "km", "lng" -> lng.toString, "shop_type_id" -> "1",
            "radius" -> "40")))) { response =>
      val shops = Parser.parse[ShopResponse](response.body)
      handleResponse(shops)
    }
  }
}
