package com.cldellow.ballero.data

import android.content._
import android.database.sqlite._
import android.util.Log
import com.cldellow.ballero.ui.SmartActivity
import com.cldellow.ballero.service._
import java.util.concurrent.atomic._

object Constants {
  val DB_VERSION = 4
}

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

  protected var _cachedGet: Option[List[T]] = None
  def get(implicit a: SmartActivity): List[T] = {
    if(_cachedGet.isDefined)
      _cachedGet.get
    else {
      _cachedGet = Some(
        if(!array) 
          Parser.parseList[T](Data.get(name, "[]"))
        else {
          val keys = Parser.parseList[String](Data.get(name, "[]"))
          keys.flatMap { key => Parser.parseList[T](Data.get(name + "_item_" + key, "[]")) }
        })
      _cachedGet.get
    }
  }
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
          //Log.i("NETWORK_RESOURCE", "got %s".format(response.body))

          response.statusCode match {
            case OK =>
              val newValues = fromString(response.body)
              _cachedGet = Some(newValues)

              // If it's an array, serialize the list of keys to the primary name
              // and each item to its own key.
              if(!array) {
                val saving = Parser.serializeList(newValues)(mf)
                Log.i("NETWORK_RESOURCE", "saving %s".format(saving))
                Data.save(name, saving)
              } else if(array) {
                val keys = newValues.map { _.asInstanceOf[Key] }
                val savedKeys = Parser.serializeList[String](keys.map { _.key })
                Data.save(name, savedKeys)

                newValues.foreach { key =>
                  val saved = Parser.serializeList[T](List(key))
                  Data.save(name + "_item_" + key.asInstanceOf[Key].key, saved)
                }
              }
              callback(newValues, false)
            case _ =>
              a.networkError(response)
              callback(get, false)
          }
      }
    }
  }
}

object NetworkResource {
  def get[T <: Product](key: String): Option[T] = {
    None
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


  override def get(implicit a: SmartActivity): List[RavelryQueue] = {
    if(_cachedGet.isDefined)
      _cachedGet.get
    else {
      _cachedGet = Some(in.get.map { id => RavelryApi.makeQueueDetailsResource(id.id) }.flatMap { _.get })
      _cachedGet.get
    }
  }

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

              if(newValue == 1) {
                _cachedGet = None
                callback(get, false)
              }
            }
          })
        }

        resource.render(refreshPolicy, { (items, pending) =>
          if(!pending) {
            val newValue = counter.getAndDecrement

            if(newValue == 1) { 
              _cachedGet = None
              callback(get, false)
            }
          }
        })
      }
    }
  }
}

case class User(name: String, oauth_token: Option[OAuthCredential]) {
  def hasToken = oauth_token.isDefined

  def uiPrefix = "uipref_"
  def uiPref(key: String, default: String)(implicit context: Context): String = {
    Data.getPreferencesForUser(name).getString("uipref_" + key, default)
  }

  def setUiPref(key: String, value: String)(implicit context: Context) {
    Data.getPreferencesForUser(name).edit().putString("uipref_" + key, value).commit
  }

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

class DataHelper(context: Context) extends SQLiteOpenHelper(context, "ballero", null, Constants.DB_VERSION) {
  override def onCreate(db: SQLiteDatabase) {
    db.execSQL("CREATE TABLE data (namespace text, key text, value text, primary key(namespace, key))")

  }

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE data")
    onCreate(db)
  }
}

object Data {
  private val balleroKey = "_ballero"
  private val usersKey = "users"
  private val balleroRevKey = "balleroRev"
  val balleroRev = 2
  var newUser = false

  var currentUser: Option[User] = None

  var _databaseHelper: DataHelper = null
  def getHelper(implicit context: Context): DataHelper = {
    if(_databaseHelper == null)
      _databaseHelper = new DataHelper(context)

    _databaseHelper
  }

  def getDatabase(implicit context: Context): SQLiteDatabase = {
    getHelper.getWritableDatabase
  }

  def save(key: String, value: String)(implicit context: Context) = {
    val db = getDatabase
    val username = Data.currentUser.get.name
    db.delete("data", "namespace = ? and key = ?", List(username, key).toArray)
    db.delete("data", "namespace = ? and key = ?", List(username, key + "_age").toArray)

    val contentValues = new ContentValues()
    contentValues.put("namespace", username)
    contentValues.put("key", key)
    contentValues.put("value", value)
    db.insert("data", null, contentValues)

    contentValues.put("key", key + "_age")
    contentValues.put("value", System.currentTimeMillis.toString)
    db.insert("data", null, contentValues)
  }

  def get(key: String, default: String)(implicit context: Context): String = {
   val username = Data.currentUser.get.name
   val cursor = getDatabase.query("data", List("value").toArray,
     "namespace = ? AND key = ?", List(username, key).toArray, null, null, null, null)
   val rv = if(cursor.getCount == 0) {
     cursor.close()
     default
   } else {
     cursor.moveToFirst
     val rv = cursor.getString(0)
     cursor.close()
     rv
   }

   rv
  }

  private var sanityChecked = false

  def getPreferencesForUser(user: String)(implicit context: Context) = {
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

    context.getSharedPreferences(user, 0)
  }

  def getUserPreferences(implicit context: Context) = {
    getPreferencesForUser(currentUser.get.name)
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
    getGlobalPreferences.edit.putString(usersKey, serialized).commit
  }

  def deleteUser(name: String)(implicit context: Context) {
    val serialized = Parser.serialize(Users(users(context) filter { _.name != name }))
    getGlobalPreferences.edit.putString(usersKey, serialized).commit
  }
}

