package cldellow.ballero.ui

import android.content._
import android.os._
import android.util.Log
import cldellow.ballero.service.RestService

class RestServiceConnection(f: => Unit) extends ServiceConnection {
  val TAG = "RestServiceConnection"

  private var _boundService: RestService#LocalBinder = null
  def boundService = _boundService
  def onServiceConnected(className: ComponentName, service: IBinder) {
    Log.i(TAG, "onServiceConnected")
    _boundService = service.asInstanceOf[RestService#LocalBinder]
  }

  def onServiceDisconnected(className: ComponentName) {
    Log.i(TAG, "onServiceDisconnected")
    error("RestServiceConnection.onServiceDisconnected called")
  }
}


// vim: set ts=2 sw=2 et:
