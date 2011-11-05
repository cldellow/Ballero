package com.cldellow.ballero.ui

import android.content._
import android.os._
import android.util.Log
import com.cldellow.ballero.service._

class RestServiceConnection() extends ServiceConnection {
  val TAG = "RestServiceConnection"

  private var _boundService: RestService#LocalBinder = null

  case class Pair[T](request: RestRequest[T], callback: RestResponse[T] => Unit)
  case class JsonPair[T](request: JsonParseRequest[T], callback: JsonParseResult[T] => Unit)
  private var _stack: List[Pair[_]] = Nil
  private var _jsonStack: List[JsonPair[_]] = Nil

  /** If the request came in on activity load, we may not have a functioninty rest service connection, so queue up the
   * request.
   *
   * Long term, may want to enforce a max # of outstanding requests anyway.
   */
  var permittedConnections = 4
  var parseSlots = 1
  def request[T](restRequest: RestRequest[T])(callback: RestResponse[T] => Unit) {
    _stack = Pair[T](restRequest, callback) :: _stack
    if(_boundService != null)
      pumpRequests()
  }

  def parseRequest[T](request: JsonParseRequest[T])(callback: JsonParseResult[T] => Unit) {
    _jsonStack = JsonPair[T](request, callback) :: _jsonStack
    if(_boundService != null)
      pumpParseRequests()
  }

  def freeConnection() = {
    permittedConnections = permittedConnections + 1
  }

  def freeParseRequest() = {
    parseSlots = parseSlots + 1
  }

  /** Centralized limit on how many outstanding network connections we permit. */
  def pumpRequests() {
    if(_stack.isEmpty)
      return

    if(permittedConnections == 0) {
      Log.i("REST", "queuing connection")
      return
    }

    val newConnection = _stack.head
    _stack = _stack.tail
    permittedConnections = permittedConnections - 1

    println("new connection is " + newConnection)
    Log.i("REST", "Outgoing: " + newConnection.request.toString)
    newConnection match {
      case x: Pair[_] =>
        _boundService.request(x.request){ response =>
          Log.i("REST", "freeing connection")
          freeConnection()
          pumpRequests()
          if(response.status != 200) {
            response.parsedVals = Nil
            x.callback(response)
          } else {
            parseRequest(JsonParseRequest(response.body, x.request.parseFunc)) { parseResponse => 
              response.parsedVals = parseResponse.parsedVals
              x.callback(response)
            }
          }
        }
    }
  }

  def onServiceConnected(className: ComponentName, service: IBinder) {
    Log.i(TAG, "onServiceConnected")
    _boundService = service.asInstanceOf[RestService#LocalBinder]

    // flush the queue
    pumpRequests()
    pumpParseRequests()
  }

  def pumpParseRequests() {
    if(_jsonStack.isEmpty)
      return

    if(parseSlots == 0) {
      Log.i("REST", "queuing parse")
      return
    }

    val newConnection = _jsonStack.head
    _jsonStack = _jsonStack.tail
    parseSlots = parseSlots - 1

    newConnection match {
      case x: JsonPair[_] =>
        _boundService.parseRequest(x.request){ response =>
          freeParseRequest()
          pumpParseRequests()
          x.callback(response)
        }
    }
  }


  def onServiceDisconnected(className: ComponentName) {
    Log.i(TAG, "onServiceDisconnected")
    error("RestServiceConnection.onServiceDisconnected called")
  }
}


// vim: set ts=2 sw=2 et:
