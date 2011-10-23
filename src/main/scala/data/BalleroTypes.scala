package cldellow.ballero.data

import android.content._

case class User(name: String, oauth_token: Option[String])
case class Users(users: List[User])

object Data {
  private val balleroKey = "_ballero"
  private val usersKey = "users"

  private def getPreferences(implicit context: Context) =
    context.getSharedPreferences(balleroKey, 0)

  def users(implicit context: Context): List[User] = {
    val prefs = getPreferences(context)
    val storedValue = prefs.getString(usersKey, """{ "users" : [{ "name" : "cldellow" }] }""")
    Parser.parse[Users](storedValue).users
  }
}
// vim: set ts=2 sw=2 et:
