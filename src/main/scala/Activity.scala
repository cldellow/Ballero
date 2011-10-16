package cldellow.ballero

import scala.collection.JavaConversions._
import android.app.Activity
import android.content.Context
import android.location._
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView

class MainActivity extends Activity {
  val TAG = "MainActivity"
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
    /*
    setContentView(new TextView(this) {
      setText("hello, world")
    })
    */
  }

  def findLysClick(view: View) {
    setContentView(R.layout.findlys)
    val locationManager = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]

    val locationListener = new MyLocationListener()
    val providers  = locationManager.getAllProviders().toList
    Log.e(TAG, providers.toString)

    //val criteria: Criteria = new Criteria()
    //criteria.setAccuracy(Criteria.ACCURACY_COARSE)
    //val provider: String = locationManager.getBestProvider(criteria, true)
    val provider = "gps"
    val mostRecentLocation: Location = locationManager.getLastKnownLocation(provider)
    if(mostRecentLocation!=null) {
      Log.e(TAG, "got mostRecentLocation")
      val latid=mostRecentLocation.getLatitude()
      val longid=mostRecentLocation.getLongitude()
      Log.e(TAG, "lat: %s, long: %s".format(latid, longid))
    }
    locationManager.requestLocationUpdates(provider, 1, 0, locationListener)
  }

  private class MyLocationListener extends LocationListener {
    def onLocationChanged(loc: Location) {
      Log.e(TAG, "onLocationChanged fired")
      if (loc != null) {
        val latid = loc.getLatitude()
        val longid = loc.getLongitude()
        val accuracyd = loc.getAccuracy()
        val providershown: String = loc.getProvider()
        Log.e(TAG, "onLocationChanged: lat=%s, long=%s, acc=%s, prov=%s".format(latid, longid, accuracyd,
        providershown))
        // accuracy.setText("Location Acquired. Accuracy:"
        //+ Double.toString(accuracyd) + "m\nProvider: "+providershown);
      }
    }

    def onProviderDisabled(name: String) {}
    def onProviderEnabled(name: String) {}
    def onStatusChanged(name: String, int: Int, bundle: Bundle) {}
  }
}
