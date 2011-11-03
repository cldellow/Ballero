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


  protected def fromString(string: String): List[T] = {
    val currentTime = System.currentTimeMillis
    val rv = if(array)
      Parser.parseList[T](string)
    else
      List(Parser.parse[T](string))
    Log.i("NETWORK_RESOURCE", "Parse took %s ms".format(System.currentTimeMillis - currentTime))
    rv
  }

  protected var _cachedGet: Option[List[T]] = None
  private def get(callback: (List[T], Int) => Unit, delta: Int)(implicit a: SmartActivity) {
    if(_cachedGet.isDefined) {
      callback(_cachedGet.get, delta)
    } else {
      a.restServiceConnection.parseRequest(JsonParseRequest[T](Data.get(name, "[]"), Parser.parseList[T])){ response =>
        _cachedGet = Some(response.parsedVals)
        callback(response.parsedVals, delta)
      }
    }
  }

  final def ageName: String = "%s_age".format(name)
  def name: String = url.cacheName

  def canNetwork = true
  def getUrl =
    Crypto.appsign(
      (url.base.replace("{user}", Data.currentUser.map { _.name }.getOrElse(""))),
      url.params)

  def stale(implicit a: SmartActivity): Boolean = getAge > 3600

  def render(refreshPolicy: RefreshPolicy, callback: (List[T], Int) => Unit)(implicit a: SmartActivity) {
    val doNetwork = (refreshPolicy == ForceNetwork ||
      (refreshPolicy == FetchIfNeeded && stale)) && canNetwork

    val delta = if (doNetwork) -1 else -2
    get(callback, delta)
    if(doNetwork) {
      val restRequest = RestRequest[T](getUrl, parseFunc = fromString)
      a.restServiceConnection.request(restRequest) { response =>
          //Log.i("NETWORK_RESOURCE", "got %s".format(response.body))

          response.statusCode match {
            case OK =>
              val newValues = response.parsedVals
              _cachedGet = Some(newValues)

              val saving = Parser.serializeList(newValues)(mf)
              Log.i("NETWORK_RESOURCE", "saving %s".format(saving))
              Data.save(name, saving)
            callback(newValues, -1)
            case _ =>
              Log.e("NETWORK_RESOURCE", "Failed request: %s".format(restRequest))
              Log.e("NETWORK_RESOURCE", "Response: %s".format(response))
              a.networkError(response)
              get(callback, -1)
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

  override def render(refreshPolicy: RefreshPolicy, callback: (List[To], Int) => Unit)(implicit a: SmartActivity) {
    // We'd like to invoke the delegated render and perform the transformation.
    in.render(refreshPolicy, transformer(callback))
  }

  private def transformer(callback: (List[To], Int) => Unit)(results: List[From], delta: Int)
  {
    callback(results flatMap mapper, delta)
  }
}

/** Convenience class to map asynchronously from one domain object to another */
class QueueNetworkResource(in: NetworkResource[SimpleQueuedProject]) extends
NetworkResource[RavelryQueue](UrlInput("http://example.com/",Map(), "delete_me")) {
  override def canNetwork = Data.currentUser map { _.hasToken } getOrElse false


  private def get(callback: (List[RavelryQueue], Int) => Unit, delta: Int)(implicit a: SmartActivity) {
    if(_cachedGet.isDefined)
      callback(_cachedGet.get, delta)
    else
      in.render(ForceDisk, fetchAll(ForceDisk, callback, delta))
  }

  /** rawDelta is the delta the original caller is expecting to see */
  private def fetchAll(policy: RefreshPolicy, callback: (List[RavelryQueue], Int) => Unit, rawDelta: Int)(ids:
  List[SimpleQueuedProject], delta: Int)(implicit a: SmartActivity) {
    val counter = new AtomicInteger(ids.length * 2)
    import scala.collection.JavaConversions._
    val patternMap: collection.mutable.ConcurrentMap[Int, Pattern] = 
      new java.util.concurrent.ConcurrentHashMap[Int, Pattern]
    val queueMap: collection.mutable.ConcurrentMap[Int, RavelryQueue] = 
      new java.util.concurrent.ConcurrentHashMap[Int, RavelryQueue]
    val syncObject = new Object

    def fire() {
      if(policy != ForceDisk)
        Data.save(ageName, System.currentTimeMillis.toString)
      val ravelryQueue = queueMap.values.toList
      ravelryQueue.foreach { q =>
        q.pattern_id.foreach { id =>
          q.pattern = patternMap.get(id)
        }
      }
      callback(ravelryQueue, rawDelta)
    }
    ids.foreach { id =>
      val resource = RavelryApi.makeQueueDetailsResource(id.id)

      id.pattern_id.map { pattern_id =>
        counter.addAndGet(2)

        val patternResource = RavelryApi.makePatternDetailsResource(pattern_id)
        patternResource.render(policy, { (items, pending) =>
          val contains = items.exists { item => patternMap.contains(item.id) }
          items.foreach { item => patternMap(item.id) = item }

          val newValue = counter.addAndGet(pending)

          if(newValue == 0) {
            fire()
          }
        })
      }

      resource.render(policy, { (items, pending) =>
        val contains = items.exists { item => queueMap.contains(item.id) }
        items.foreach { item => queueMap(item.id) = item }

        val newValue = counter.addAndGet(pending)

        if(newValue == 0) { 
          fire()
        }
      })
    }
  }

  override def render(refreshPolicy: RefreshPolicy, callback: (List[RavelryQueue], Int) => Unit)(implicit a: SmartActivity) {
    val doNetwork = (refreshPolicy == ForceNetwork ||
      (refreshPolicy == FetchIfNeeded && (in.stale || stale))) && canNetwork


    val delta = if(doNetwork) -1 else -2
    get(callback, delta)

    if(doNetwork) {
      in.render(refreshPolicy, fetchAll(refreshPolicy, callback, delta))
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
    val db = getDatabase
    db.delete("data", "namespace = ?", List(name).toArray)

    val serialized = Parser.serialize(Users(users(context) filter { _.name != name }))
    getGlobalPreferences.edit.putString(usersKey, serialized).commit
  }
}

