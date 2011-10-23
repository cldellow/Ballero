package cldellow.ballero.ui

import cldellow.ballero.R
import cldellow.ballero.data._

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
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setActionBarContentView(R.layout.lys_stores_map);

    val mapView: MapView = find(R.id.map_view)
    mapView.setBuiltInZoomControls(true)
    val controller = mapView.getController
    val locationString = getIntent().getStringExtra(UiConstants.Location)
    val location = Parser.parse[GoogleLocation](locationString)
    controller.setCenter(new GeoPoint((location.lat * 1e6).toInt, (location.lng * 1e6).toInt))
    controller.setZoom(14)

  }

  override def isRouteDisplayed = false
}
