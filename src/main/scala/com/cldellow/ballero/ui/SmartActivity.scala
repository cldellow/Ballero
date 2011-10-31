package com.cldellow.ballero.ui

import com.cldellow.ballero.service._
import com.cldellow.ballero.data._
import com.cldellow.ballero.R

import org.json.JSONObject
import java.net.URLEncoder
import android.app.Activity
import android.content._
import android.location._
import android.os._
import android.util.Log
import android.view.View
import android.widget._
import java.util.Locale
import greendroid.widget._
import greendroid.widget.item._

trait SmartActivity extends Activity {// this: Activity =>
  def TAG: String
  def find[T](i: Int): T =
    findViewById(i).asInstanceOf[T]

  def findView(i: Int): View = find(i)
  def findAsyncImageView(i: Int): AsyncImageView = find(i)
  def findLabel(i: Int): TextView = find(i)
  def findTextView(i: Int): TextView = find(i)
  def findProgressBar(i: Int): ProgressBar = find(i)
  def findButton(i: Int): Button = find(i)

  def toast(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
  }

  def longToast(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_LONG).show()
  }

  protected def getParams[P <: Product](implicit mf: Manifest[P]): Option[P] = {
    val intent = getIntent()
    val bundle = intent.getExtras

    if(bundle == null)
      None
    else {
      val params = bundle.getString("com.cldellow.params")
      if(params == null)
        None
      else
        Some(Parser.parse[P](params))
    }
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
      boundRestServiceConnection = false
      unbindService(restServiceConnection)
    }
  }

  implicit def implicitContext: SmartActivity = this
  protected def geocode(loc: Location)(callback: Option[Address] => Unit) {
    restServiceConnection.request(
      RestRequest("http://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&sensor=true".format(loc.getLatitude,
      loc.getLongitude))) { handleGeocodeResponse(callback) }
  }

  private def handleGeocodeResponse(callback: Option[Address] => Unit)(restResponse: RestResponse) {
    val response = Parser.parse[GoogleResponse](restResponse.body)

    callback(response.results match {
      case Nil => None
      case x :: xs =>
        val address = new Address(Locale.getDefault)
        address.setLongitude(x.geometry.location.lng.toDouble)
        address.setLatitude(x.geometry.location.lat.toDouble)
        x.address_components.find { _.types.contains("locality") }.map { city =>
          address.setLocality(city.long_name)
        }
        x.address_components.find { _.types.contains("administrative_area_level_1") }.map { state =>
          address.setAdminArea(state.long_name)
        }

        if(address.getLocality != null && address.getAdminArea != null)
          Some(address)
        else
          None
    })
  }

  protected def geocode(str: String)(callback: Option[Address] => Unit) {
    restServiceConnection.request(
      RestRequest("http://maps.googleapis.com/maps/api/geocode/json?address=%s&sensor=true"
        .format(URLEncoder.encode(str, "UTF-8")))) { handleGeocodeResponse(callback) }
  }

  protected def createTextItem(string: String, klass: Class[_]): TextItem = {
    val textItem = new TextItem(string)
    textItem.setTag(klass)
    textItem
  }

  /** Display a warning when we can't access network resources; max once warning per 10
      minutes. */
  def networkError(request: RestResponse) {
    if(SmartActivity.canToast) {
      SmartActivity.canToast = false
      toast("Oops, couldn't access the Internet.")
    }
  }

  implicit def item2richitem[T <: Item](item: T): RichItem[T] = new RichItem(item)
}

case class NavHint(clazz: Class[_], params: Option[String])
class RichItem[T <: Item](val item: T) {
  def goesTo[A <: Activity](implicit mfA: Manifest[A]): T = {
    goesTo(None)
  }

  def goesToWithData[A <: Activity, P <: Product](info: P)(implicit mfA: Manifest[A], mfP: Manifest[P]): T = {
    goesTo[P, A](Some(info))
  }



  private def goesTo[P <: Product, A <: Activity](info: Option[P])(implicit mfP: Manifest[P], mfA: Manifest[A]): T = {
    val serialized = info map { Parser.serialize(_) }
    item.setTag(NavHint(mfA.erasure, serialized))
    item
  }
}

object SmartActivity {
  var canToast = true
}

// vim: set ts=2 sw=2 et:
