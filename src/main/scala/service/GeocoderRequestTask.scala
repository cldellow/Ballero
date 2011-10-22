package cldellow.ballero.service

import cldellow.ballero.ui._
import scala.collection.JavaConversions._
import android.content.Context
import android.location._

/*
class GeocoderRequestTask(f: Address => Unit)(context: SmartActivity) extends GeocoderRequestTaskBase {
  val geocoder = new Geocoder(context)
  def doInBackground1(input: Array[Either[String, Location]]): Address = input(0) match {
    case Left(geocode) =>
      null
    case Right(reverseGeocode) =>
      geocoder.getFromLocation(reverseGeocode.getLatitude, reverseGeocode.getLongitude, 1).head
  }

  override def onPostExecute(addr: Address) = f(addr)
}
*/

// vim: set ts=2 sw=2 et:
