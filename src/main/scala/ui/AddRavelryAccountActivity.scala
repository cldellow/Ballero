package cldellow.ballero.ui

import cldellow.ballero.R

import scala.collection.JavaConversions._
import android.app.Activity
import android.content.Context
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
  }

}
