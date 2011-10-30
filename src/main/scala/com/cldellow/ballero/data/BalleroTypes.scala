package com.cldellow.ballero.data

import android.content._
import android.util.Log
import com.cldellow.ballero.ui.SmartActivity
import com.cldellow.ballero.service._
import java.util.concurrent.atomic._

sealed trait RefreshPolicy
case object ForceNetwork extends RefreshPolicy
case object FetchIfNeeded extends RefreshPolicy
case object ForceDisk extends RefreshPolicy

class NetworkResource[T <: Product](val url: UrlInput, val array: Boolean = true)(implicit mf: Manifest[T]) {
  def getAgeFromKey(ageName: String)(implicit a: SmartActivity): Long =
    (System.currentTimeMillis - Data.get(ageName, "0").toLong) / 1000

  def getAge(implicit a: SmartActivity): Long = getAgeFromKey(ageName)


  protected def fromString(string: String): List[T] =
    if(array)
      Parser.parseList[T](string)
    else
      List(Parser.parse[T](string))

  def get(implicit a: SmartActivity): List[T] = Parser.parseList[T](Data.get(name, "[]"))
  final def ageName: String = "%s_age".format(name)
  def name: String = url.cacheName

  def canNetwork = true
  def getUrl = url.base.replace("{user}", Data.currentUser.map { _.name }.getOrElse(""))

  def stale(implicit a: SmartActivity): Boolean = getAge > 3600

  def render(refreshPolicy: RefreshPolicy, callback: (List[T], Boolean) => Unit)(implicit a: SmartActivity) {
    val doNetwork = (refreshPolicy == ForceNetwork ||
      (refreshPolicy == FetchIfNeeded && stale)) && canNetwork

    callback(get, doNetwork)

    if(doNetwork) {
      a.restServiceConnection.request(
        RestRequest(getUrl)) { response =>
          Log.i("NETWORK_RESOURCE", "got %s".format(response.body))

          response.statusCode match {
            case OK =>
              val newValues = fromString(response.body)
              val saving = Parser.serializeList(newValues)(mf)
              Log.i("NETWORK_RESOURCE", "saving %s".format(saving))
              Data.save(name, saving)
              callback(newValues, false)
            case _ =>
              a.networkError(response)
              callback(get, false)
          }
      }
    }
  }
}

class SignedNetworkResource[T <: Product](url: UrlInput, array: Boolean = true)(implicit mf: Manifest[T]) 
extends NetworkResource[T](url, array) {
  override def canNetwork = Data.currentUser map { _.hasToken } getOrElse false
  override def getUrl = Data.currentUser map { _.sign(url) } get
}

/** Convenience class to map from one domain object to another -- useful for flattening
    responses.*/
class TransformedNetworkResource[From <: Product, To <: Product] (in: NetworkResource[From], mapper: (From => List[To]))
  (implicit toMf: Manifest[To], fromMf: Manifest[From]) extends NetworkResource[To](in.url) {
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
    in.get flatMap mapper
}

/** Convenience class to map asynchronously from one domain object to another */
class QueueNetworkResource(in: NetworkResource[SimpleQueuedProject]) extends
NetworkResource[RavelryQueue](UrlInput("http://example.com/",Map(), "delete_me")) {
  override def canNetwork = Data.currentUser map { _.hasToken } getOrElse false


  override def get(implicit a: SmartActivity): List[RavelryQueue] =
    in.get.map { id => RavelryApi.makeQueueDetailsResource(id.id) }.flatMap { _.get }

  override def render(refreshPolicy: RefreshPolicy, callback: (List[RavelryQueue], Boolean) => Unit)(implicit a: SmartActivity) {
    val doNetwork = (refreshPolicy == ForceNetwork ||
      (refreshPolicy == FetchIfNeeded && (in.stale || get.length != in.get.length))) && canNetwork

    callback(get, doNetwork)

    if(doNetwork) {
      in.render(refreshPolicy, fetchAll(callback))
    }

    def fetchAll(callback: (List[RavelryQueue], Boolean) => Unit)(ids: List[SimpleQueuedProject], pending: Boolean) {
      val counter = new AtomicInteger(ids.length)
      ids.foreach { id =>
        val resource = RavelryApi.makeQueueDetailsResource(id.id)

        id.pattern_id.map { pattern_id =>
          counter.getAndIncrement

          val patternResource = RavelryApi.makePatternDetailsResource(pattern_id)
          patternResource.render(refreshPolicy, { (items, pending) =>
            if(!pending) {
              val newValue = counter.getAndDecrement

              if(newValue == 1)
                callback(get, false)
            }
          })
        }

        resource.render(refreshPolicy, { (items, pending) =>
          if(!pending) {
            val newValue = counter.getAndDecrement

            if(newValue == 1)
              callback(get, false)
          }
        })
      }
    }
  }
}

case class User(name: String, oauth_token: Option[OAuthCredential]) {
  def hasToken = oauth_token.isDefined
  def sign(url: UrlInput): String =
    Crypto.sign(url.base.replace("{user}", name), url.params, oauth_token.get.auth_token, oauth_token.get.signing_key)


  private val _needleResource = 
    new NetworkResource[Needle](RavelryApi.needleList)

  private val _queueResource =
    new TransformedNetworkResource[QueuedProjects, SimpleQueuedProject](
      new SignedNetworkResource[QueuedProjects](RavelryApi.queueList, false),
      { qp => qp.queued_projects })

  private val _projectResource =
    new TransformedNetworkResource[SimpleProjects, Project](
      new SignedNetworkResource[SimpleProjects](RavelryApi.projectList, false),
      { sp => sp.projects })

  private val _queuedProjectsResource =
    new QueueNetworkResource(_queueResource)



  def queue: NetworkResource[SimpleQueuedProject] = _queueResource
  def queuedProjects: NetworkResource[RavelryQueue] = _queuedProjectsResource
  def projects: NetworkResource[Project] = _projectResource
  def needles: NetworkResource[Needle] = _needleResource

}
case class Users(users: List[User])

object Data {
  private val balleroKey = "_ballero"
  private val usersKey = "users"
  private val balleroRevKey = "balleroRev"
  val balleroRev = 2

  var currentUser: Option[User] = None

  def save(key: String, value: String)(implicit context: Context) = {
    val editor = getUserPreferences.edit
    editor.putString("%s_age".format(key), System.currentTimeMillis.toString)
    editor.putString(key, value)
    //Log.i("DATA", "saving key %s with value %s".format(key, value))
    editor.commit
  }

  def get(key: String, default: String)(implicit context: Context): String = {
   val rv = getUserPreferences.getString(key, default)
   //Log.i("DATA", "asked for key %s, returning default? %s".format(key, default == rv))
   rv
  }

  private var sanityChecked = false
  private def getUserPreferences(implicit context: Context) = {
    if(!sanityChecked) {
      sanityChecked = true
      // If the Ballero data serialization has changed between revs, blow away all cached
      // data and force network reloads.
      val prefs = getGlobalPreferences
      val oldInt = prefs.getInt(balleroRevKey, 0)
      if(oldInt != balleroRev) {
        Log.i("DATA", "Ballero serialization formats changed; clearing user cache")
        users.foreach { user =>
          context.getSharedPreferences(user.name, 0).edit().clear().commit()
        }

        prefs.edit.putInt(balleroRevKey, balleroRev).commit
      }
    }

    context.getSharedPreferences(currentUser.get.name, 0)
  }
  private def getGlobalPreferences(implicit context: Context) =
    context.getSharedPreferences(balleroKey, 0)

  def users(implicit context: Context): List[User] = {
    val prefs = getGlobalPreferences(context)
    val storedValue = prefs.getString(usersKey, """{ "users" : [] }""")
    Parser.parse[Users](storedValue).users
  }

  def saveUser(user: User)(implicit context: Context) {
    val serialized = Parser.serialize(Users(user :: (users(context) filter { _.name != user.name })))
    //Log.i("DATA", "saving users: %s".format(serialized))
    getGlobalPreferences.edit.putString(usersKey, serialized).commit
  }
}

