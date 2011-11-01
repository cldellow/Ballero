package com.cldellow.ballero.ui

import com.cldellow.ballero.R
import com.cldellow.ballero.service._
import com.cldellow.ballero.data._

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

class AddRavelryAccountActivity extends GDActivity with SmartActivity {
  val TAG = "AddRavelryAccountActivity"
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setTitle("add a ravelry account")
  }

  override def createLayout(): Int = {
      return R.layout.addravelry
  }

  lazy val btnLogin = findButton(R.id.btnLogin)
  lazy val txtPassword = findTextView(R.id.txtPassword)
  lazy val txtUsername = findTextView(R.id.txtUsername)
  lazy val progressBar = findProgressBar(R.id.progressBar)
  lazy val controls = List(btnLogin, txtPassword, txtUsername, progressBar)

  def disableControls {
    progressBar.setVisibility(View.VISIBLE)
    controls.foreach { _.setEnabled(false) }
    btnLogin.setText("verifying...")
  }

  def enableControls {
    progressBar.setVisibility(View.INVISIBLE)
    controls.foreach { _.setEnabled(true) }
    btnLogin.setText("login")
  }

  def badLogin() {
    enableControls
    longToast("Incorrect username or password.")
  }

  def goodLogin(username: String, auth_token: String, signing_key: String) {
    longToast("Great! Hang on a sec, admiring your knitting.")

    val newUser = User(username, Some(OAuthCredential(auth_token, signing_key)))
    Data.newUser = true
    Data.currentUser = Some(newUser)
    Data.saveUser(newUser)
    finish()
  }


  def attemptLogin(username: String, password: String) {
    info("user: %s".format(username))
    info("password: %s".format(password))
    val appSigned = Crypto.appsign("http://api.ravelry.com/authenticate.json",
      Map("credentials" -> Crypto.aes256("%s:%s".format(username, password))))
    info("request: %s".format(appSigned))
    val request = RestRequest(appSigned)
    restServiceConnection.request(request) { response =>
      info("got ersponse: %s".format(response))
      val authResponse = Parser.parse[AuthResponse](response.body)
      if(authResponse.auth_token.isEmpty || authResponse.signing_key.isEmpty)
        badLogin()
      else
        goodLogin(username, authResponse.auth_token.get, authResponse.signing_key.get)
    }
  }


  def loginClick(v: View) {
    if(txtUsername.getText.length == 0) {
      toast("Please enter your username.")
      return
    }

    if(txtPassword.getText.length == 0) {
      toast("Please enter your password.")
      return
    }

    disableControls
    attemptLogin(txtUsername.getText.toString, txtPassword.getText.toString)
  }

}
