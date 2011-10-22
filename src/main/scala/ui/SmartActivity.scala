package cldellow.ballero.ui

import cldellow.ballero.service.RestService
import cldellow.ballero.R

import android.app.Activity
import android.content._
import android.os._
import android.util.Log
import android.view.View
import android.widget._

trait SmartActivity extends Activity {// this: Activity =>
  def TAG: String
  def find[T](i: Int): T =
    findViewById(i).asInstanceOf[T]

  def findView(i: Int): View = find(i)
  def findLabel(i: Int): TextView = find(i)
  def findTextView(i: Int): TextView = find(i)
  def findProgressBar(i: Int): ProgressBar = find(i)
  def findButton(i: Int): Button = find(i)

  def toast(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
  }

  protected def info(s: String) { Log.i(TAG, s) }
  protected def warn(s: String) { Log.w(TAG, s) }
  protected def error(s: String) { Log.e(TAG, s) }

  def onRestServiceReady() {
  }

  private var boundRestServiceConnection = false
  lazy val restServiceConnection = {
    Log.i(TAG, "restServiceConnection accessed; binding")
    val x = new RestServiceConnection(this.onRestServiceReady)
    val result = bindService(new Intent(this, classOf[RestService]), x, Context.BIND_AUTO_CREATE)
    Log.i(TAG, "result of bind = %s".format(result))
    boundRestServiceConnection = true
    x
  }

  override def onStop() {
    super.onStop()
    if(boundRestServiceConnection) {
      unbindService(restServiceConnection)
    }
  }
}


// vim: set ts=2 sw=2 et:
