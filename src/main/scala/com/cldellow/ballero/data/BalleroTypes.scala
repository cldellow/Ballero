package com.cldellow.ballero.data

import android.content._
import android.util.Log
import com.cldellow.ballero.ui.SmartActivity
import com.cldellow.ballero.service.{RestRequest, RestResponse}

sealed trait RefreshPolicy
case object ForceNetwork extends RefreshPolicy
case object FetchIfNeeded extends RefreshPolicy
case object ForceDisk extends RefreshPolicy

class NetworkResource[T](val url: String)(implicit mf: Manifest[T]) {
  def getAge(implicit a: SmartActivity): Long =
    (System.currentTimeMillis - Data.get(ageName, "0").toLong) / 1000

  def get(implicit a: SmartActivity): List[T] = Parser.parseList[T](Data.get(name, "[]"))
  final def ageName: String = "%s_age".format(name)
  final def name: String = mf.erasure.getSimpleName.toLowerCase

  def render(refreshPolicy: RefreshPolicy, callback: (List[T], Boolean) => Unit)(implicit a: SmartActivity) {
    val doNetwork = refreshPolicy == ForceNetwork ||
      (refreshPolicy == FetchIfNeeded && getAge > 3600)

    callback(get, doNetwork)

    if(doNetwork) {
      a.restServiceConnection.request(
        RestRequest(url)) { response =>
          val newValues = Parser.parseList[T](response.body)(mf)
          val saving = Parser.serializeList(newValues)(mf)
          Log.i("NETWORK_RESOURCE", "saving %s".format(saving))
          Data.save(name, saving)
          callback(newValues, false)
      }
    }
  }
}

case class User(name: String, oauth_token: Option[String]) {
  private val _needleResource = 
    new NetworkResource[Needle]("http://rav.cldellow.com:8080/rav/people/%s/needles".format(name))
  def needles: NetworkResource[Needle] = _needleResource

}
case class Users(users: List[User])

object Data {
  private val balleroKey = "_ballero"
  private val usersKey = "users"

  var currentUser: Option[User] = None

  def save(key: String, value: String)(implicit context: Context) = {
    val editor = getUserPreferences.edit
    editor.putString("%s_age".format(key), System.currentTimeMillis.toString)
    editor.putString(key, value)
    editor.commit
  }

  def get(key: String, default: String)(implicit context: Context): String = 
   getUserPreferences.getString(key, default)

  private def getUserPreferences(implicit context: Context) =
    context.getSharedPreferences(currentUser.get.name, 0)
  private def getGlobalPreferences(implicit context: Context) =
    context.getSharedPreferences(balleroKey, 0)

  def users(implicit context: Context): List[User] = {
    val prefs = getGlobalPreferences(context)
    val storedValue = prefs.getString(usersKey, """{ "users" : [{ "name" : "cldellow" }, { "name" : "jkdellow" }] }""")
    Parser.parse[Users](storedValue).users
  }
}
// vim: set ts=2 sw=2 et:
