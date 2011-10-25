package com.cldellow.ballero.ui

import android.content._
import android.os._
import android.util.Log
import com.cldellow.ballero.service._

class RestServiceConnection() extends ServiceConnection {
  val TAG = "RestServiceConnection"

  private var _boundService: RestService#LocalBinder = null

  private var _stack: List[(RestRequest, RestResponse => Unit)] = Nil

  /** If the request came in on activity load, we may not have a functioninty rest service connection, so queue up the
   * request.
   *
   * Long term, may want to enforce a max # of outstanding requests anyway.
   */
  def request(restRequest: RestRequest)(callback: RestResponse => Unit) {
    if(_boundService != null)
      _boundService.request(restRequest)(callback)
    else {
      _stack = (restRequest, callback) :: _stack
    }
  }

  def onServiceConnected(className: ComponentName, service: IBinder) {
    Log.i(TAG, "onServiceConnected")
    _boundService = service.asInstanceOf[RestService#LocalBinder]

    // flush the queue
    val oldQueue = _stack
    _stack = Nil
    oldQueue.foreach { x => request(x._1)(x._2) }
  }

  def onServiceDisconnected(className: ComponentName) {
    Log.i(TAG, "onServiceDisconnected")
    error("RestServiceConnection.onServiceDisconnected called")
  }
}


// vim: set ts=2 sw=2 et:
