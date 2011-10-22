package cldellow.ballero.ui

import cldellow.ballero.service._
import cldellow.ballero.R

import org.json.JSONObject
import android.app.Activity
import android.content._
import android.location._
import android.os._
import android.util.Log
import android.view.View
import android.widget._
import java.util.Locale

trait SmartActivity extends Activity {// this: Activity =>
  def TAG: String
  def find[T](i: Int): T =
    findViewById(i).asInstanceOf[T]

  def findView(i: Int): View = find(i)
  def findLabel(i: Int): TextView = find(i)
  def findTextView(i: Int): TextView = find(i)
  def findProgressBar(i: Int): ProgressBar = find(i)
  def findButton(i: Int): Button = find(i)

  def toast(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
  }

  protected def info(s: String) { Log.i(TAG, s) }
  protected def warn(s: String) { Log.w(TAG, s) }
  protected def error(s: String) { Log.e(TAG, s) }

  private var boundRestServiceConnection = false
  lazy val restServiceConnection = {
    Log.i(TAG, "restServiceConnection accessed; binding")
    val x = new RestServiceConnection()
    val result = bindService(new Intent(this, classOf[RestService]), x, Context.BIND_AUTO_CREATE)
    Log.i(TAG, "result of bind = %s".format(result))
    boundRestServiceConnection = true
    x
  }

  override def onStop() {
    super.onStop()
    if(boundRestServiceConnection) {
      unbindService(restServiceConnection)
    }
  }

  protected def geocode(loc: Location)(callback: Address => Unit) {
    //geocode(Right(loc))(callback)
    restServiceConnection.request(
      RestRequest("http://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&sensor=true".format(loc.getLatitude,
      loc.getLongitude))) { restResponse =>

      info("got response: %s".format(restResponse))
      val address = new Address(Locale.getDefault)

      val response = new JSONObject(restResponse.body)
      info("parsed: %s".format(response))

      callback(address)
    }
  }

  /*
  protected def geocode(loc: Location)(callback: Address => Unit) { geocode(Right(loc))(callback) }
  protected def geocode(loc: String)(callback: Address => Unit) { geocode(Left(loc))(callback) }
  protected def geocode(loc: Either[String, Location])(callback: Address => Unit) {
    new GeocoderRequestTask(callback)(this).execute(loc)
  }
  */
}


// vim: set ts=2 sw=2 et:
