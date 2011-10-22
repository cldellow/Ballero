package cldellow.ballero.ui

import cldellow.ballero.R
import scala.collection.JavaConversions._
import android.app.Activity
import android.content.Context
import android.location._
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import greendroid.app._
import greendroid.widget._

class FindLysActivity extends GDActivity with SmartActivity {
  val TAG = "FindLysActivity"
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
  }

  override def onResume() {
    super.onResume()
    hunt()
  }

  lazy val btnTryAgain = findButton(R.id.btnTryAgain)
  lazy val btnFindStores = findButton(R.id.btnFindStores)
  lazy val btnFindCity = findButton(R.id.btnFindCity)
  lazy val txtCityName = findTextView(R.id.txtCityName)
  lazy val lblFound = findLabel(R.id.lblFound)
  lazy val lblSearchingLocation = findLabel(R.id.lblSearchingLocation)
  lazy val progressBar = findProgressBar(R.id.progressBar)

  lazy val controls = List(lblFound, btnFindStores)


  override def createLayout(): Int = {
      return R.layout.findlys
  }

  private lazy val locationManager = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
  private lazy val locationListener = new MyLocationListener()

  private def hunt() {
    val providers  = locationManager.getAllProviders().toList
    Log.e(TAG, providers.toString)

    val provider = "gps"
    val mostRecentLocation: Location = locationManager.getLastKnownLocation(provider)

    if(mostRecentLocation != null)
      newLocation(mostRecentLocation)

    locationManager.requestLocationUpdates(provider, 60000, 100, locationListener)
  }

  private def newLocation(loc: Location) {
      Log.e(TAG, "got mostRecentLocation")
      val latid = loc.getLatitude()
      val longid = loc.getLongitude()
      Log.e(TAG, "lat: %s, long: %s".format(latid, longid))

      // Ask the Geocoder where that is, show the spinner and set text to smth
      // appropriate
      progressBar.setVisibility(View.VISIBLE)
      lblSearchingLocation.setText("resolving your address")
      geocode(loc) { response =>
        toast("got a response %s".format(response.toString))
      }
  }

  private class MyLocationListener extends LocationListener {
    def disableUpdates {
      locationManager.removeUpdates(locationListener)
    }

    def onLocationChanged(loc: Location) {
      Log.e(TAG, "onLocationChanged fired")
      disableUpdates

      progressBar.setVisibility(View.GONE)
      if (loc != null) {
        lblSearchingLocation.setText("found you!")
        newLocation(loc)
      } else {
        lblSearchingLocation.setText("uh oh, something broke")
        btnTryAgain.setVisibility(View.VISIBLE)
      }


    }

    def onProviderDisabled(name: String) {}
    def onProviderEnabled(name: String) {}
    def onStatusChanged(name: String, int: Int, bundle: Bundle) {}
  }

}
