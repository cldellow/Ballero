package com.cldellow.ballero.ui

import com.cldellow.ballero.R
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

class FindLysActivity extends GDActivity with SmartActivity {
  val TAG = "FindLysActivity"
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setTitle("find a local yarn store")
  }

  val klazz = classOf[LysStoresMapActivity]
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

  def btnFindStoresClick(view: View) {
    if(currentAddress.isEmpty) {
      toast("Uh oh, don't know where you are.")
      return
    }

    val intent: Intent = new Intent(this, classOf[LysStoresMapActivity])
    intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, "local yarn stores")
    currentAddress map { currentAddress =>
      intent.putExtra(UiConstants.Location,
      """{ "lat": %s, "lng": %s }""".format(currentAddress.getLatitude, currentAddress.getLongitude))
    }
    startActivity(intent)
  }

  def btnFindCityClick(view: View) {
    val cityName = txtCityName.getText.toString
    if(cityName == "enter city here" || cityName.length == 0) {
      toast("Enter a city name.")
      return
    }

    locationListener.disableUpdates
    progressBar.setVisibility(View.VISIBLE)
    lblSearchingLocation.setText("searching for city")
    lblSearchingLocation.setVisibility(View.VISIBLE)

    geocode(cityName)(geocodeCallback(false))
  }


  def txtCityNameClick(view: View) {
    info("txtCityNameClick called")
    if(txtCityName.getText.toString == "enter city here") {
      info("enter city here")
      txtCityName.setText("")
      txtCityName.setTextColor(Color.BLACK)
    }
  }

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
    btnFindStores.setVisibility(View.GONE)
  }

  private var currentAddress: Option[Address] = None

  private def geocodeCallback(fromGPS: Boolean)(address: Option[Address]) {
    progressBar.setVisibility(View.GONE)
    address match {
      case None =>
        progressBar.setVisibility(View.GONE)
        if(fromGPS)
          lblSearchingLocation.setText("couldn't find where you are")
        else
          lblSearchingLocation.setText("couldn't find that city")
      case Some(address) =>
        lblSearchingLocation.setVisibility(View.GONE)
        lblFound.setText("%s, %s".format(address.getLocality, address.getAdminArea))
        lblFound.setVisibility(View.VISIBLE)
        btnFindStores.setVisibility(View.VISIBLE)
    }
    currentAddress = address
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
      geocode(loc)(geocodeCallback(true))
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
