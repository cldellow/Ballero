package com.cldellow.ballero.data

import android.content._
import android.util.Log
import com.cldellow.ballero.ui.SmartActivity
import com.cldellow.ballero.service.{RestRequest, RestResponse}

sealed trait RefreshPolicy
case object ForceNetwork extends RefreshPolicy
case object FetchIfNeeded extends RefreshPolicy
case object ForceDisk extends RefreshPolicy

class NetworkResource[T <: Product](val url: String, val array: Boolean = true)(implicit mf: Manifest[T]) {
  def getAge(implicit a: SmartActivity): Long =
    (System.currentTimeMillis - Data.get(ageName, "0").toLong) / 1000

  protected def fromString(string: String): List[T] =
    if(array)
      Parser.parseList[T](string)
    else
      List(Parser.parse[T](string))

  def get(implicit a: SmartActivity): List[T] = Parser.parseList[T](Data.get(name, "[]"))
  final def ageName: String = "%s_age".format(name)
  final def name: String = mf.erasure.getSimpleName.toLowerCase

  def canNetwork = true
  def getUrl = url

  def render(refreshPolicy: RefreshPolicy, callback: (List[T], Boolean) => Unit)(implicit a: SmartActivity) {
    val doNetwork = (refreshPolicy == ForceNetwork ||
      (refreshPolicy == FetchIfNeeded && getAge > 3600)) && canNetwork

    callback(get, doNetwork)

    if(doNetwork) {
      a.restServiceConnection.request(
        RestRequest(getUrl)) { response =>
          Log.i("NETWORK_RESOURCE", "got %s".format(response.body))
          val newValues = fromString(response.body)
          val saving = Parser.serializeList(newValues)(mf)
          Log.i("NETWORK_RESOURCE", "saving %s".format(saving))
          Data.save(name, saving)
          callback(newValues, false)
      }
    }
  }
}

class SignedNetworkResource[T <: Product](url: String, array: Boolean = true)(implicit mf: Manifest[T]) 
extends NetworkResource[T](url, array) {
  override def canNetwork = Data.currentUser map { _.oauth_token.isDefined } getOrElse false
  override def getUrl = Data.currentUser map { user =>
    val withUserUrl = url.replace("{user}", user.name)
    Crypto.sign(withUserUrl, Map(), user.oauth_token.get.auth_token, user.oauth_token.get.signing_key)
  } get
}

/** Convenience class to map from one domain object to another -- useful for flattening
    responses.*/
class TransformedNetworkResource[From <: Product, To <: Product](in: NetworkResource[From],
  mapper: (From => List[To]))(implicit toMf: Manifest[To], fromMf: Manifest[From]) extends
NetworkResource[To](in.url) {
  override def getUrl = in.getUrl
  override def canNetwork = in.canNetwork

  override def render(refreshPolicy: RefreshPolicy, callback: (List[To], Boolean) => Unit)(implicit a: SmartActivity) {
    // We'd like to invoke the delegated render and perform the transformation.
    in.render(refreshPolicy, transformer(callback))
  }

  private def transformer(callback: (List[To], Boolean) => Unit)(results: List[From], pending: Boolean)
  {
    callback(results flatMap mapper, pending)
  }

  override def get(implicit a: SmartActivity): List[To] =
    Parser.parseList[From](Data.get(name, "[]")) flatMap mapper
}

case class User(name: String, oauth_token: Option[OAuthCredential]) {
  private val _needleResource = 
    new NetworkResource[Needle]("http://rav.cldellow.com:8080/rav/people/%s/needles".format(name))

  private val _queueResource =
    new TransformedNetworkResource[QueuedProjects, Id](
      new SignedNetworkResource[QueuedProjects]("http://api.ravelry.com/people/{user}/queue/list.json", false),
      { qp => qp.queued_projects })

  private val _projectResource =
    new TransformedNetworkResource[SimpleProjects, Project](
      new SignedNetworkResource[SimpleProjects]("http://api.ravelry.com/projects/{user}/list.json", false),
      { sp => sp.projects })


  def queue: NetworkResource[Id] = _queueResource
  def projects: NetworkResource[Project] = _projectResource
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
    val storedValue = prefs.getString(usersKey, """{ "users" : [] }""")
    Parser.parse[Users](storedValue).users
  }

  def saveUser(user: User)(implicit context: Context) {
    val serialized = Parser.serialize(Users(user :: (users(context) filter { _.name != user.name })))
    Log.i("DATA", "saving users: %s".format(serialized))
    getGlobalPreferences.edit.putString(usersKey, serialized).commit
  }
}

