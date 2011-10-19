package cldellow.ballero

import scala.collection.JavaConversions._
import android.app.Activity
import android.content._
import android.location._
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget._
import greendroid.app._
import greendroid.widget._
import greendroid.widget.item._

class MainActivity extends GDListActivity {
  val TAG = "MainActivity"

  def createTextItem(stringId: Int, klass: Class[_]): TextItem = {
    val textItem: TextItem = new TextItem(getString(stringId))
    textItem.setTag(klass)
    textItem
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val adapter = new ItemAdapter(this)
    adapter.add(createTextItem(R.string.find_lys, classOf[FindLysActivity]));
    adapter.add(createTextItem(R.string.add_ravelry_account, classOf[AddRavelryAccountActivity]));
    /*
    adapter.add(createTextItem(R.string.tweaked_item_view_label, TweakedItemViewActivity.class));
    adapter.add(createTextItem(R.string.segmented_label, SegmentedActivity.class));
    adapter.add(createTextItem(R.string.action_bar_activity_label, ActionBarActivity.class));
    adapter.add(createTextItem(R.string.quick_action_label, QuickActionActivity.class));
    adapter.add(createTextItem(R.string.simple_async_image_view_label, SimpleAsyncImageViewActivity.class));
    adapter.add(createTextItem(R.string.async_image_view_list_view_label, AsyncImageViewListActivity.class));
    adapter.add(createTextItem(R.string.map_pin_drawable_label, MapPinMapActivity.class));
    adapter.add(createTextItem(R.string.paged_view_label, PagedViewActivity.class));
*/

    setListAdapter(adapter);
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val textItem: TextItem = l.getAdapter().getItem(position).asInstanceOf[TextItem]

    val intent: Intent = new Intent(this, textItem.getTag.asInstanceOf[Class[_]])
    intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, textItem.text);
    startActivity(intent)
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
