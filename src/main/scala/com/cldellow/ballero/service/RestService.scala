package com.cldellow.ballero.service

import android.app._
import android.os._
import android.util.Log
import android.content._

import java.io.{InputStream, IOException}

import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.entity._
import org.apache.http.client.methods._
import org.apache.http.entity._
import org.apache.http.impl.client._
import org.apache.http.message._
import org.apache.http.params._
import org.apache.http.protocol._

class RestService extends Service {
  private val TAG = "RestService"

  private class JsonParseTask[T](f: JsonParseResult[T] => Unit) extends JsonParseTaskBase {
    def doInBackground1(args: Array[JsonParseRequest[_]]): JsonParseResult[_] = {
      val request = args(0)
      val tm = System.currentTimeMillis
      val parsedValues = request.parseFunc(request.data).map { _.asInstanceOf[T] }
      Log.i("REST", "parse of %s chars took %s ms".format(request.data.length, System.currentTimeMillis - tm))
      JsonParseResult[T](parsedValues)
    }

    override def onPostExecute(r: JsonParseResult[_]) = f(r.asInstanceOf[JsonParseResult[T]])
  }

  private class RestRequestTask[T](f: RestResponse[T] => Unit) extends RestRequestTaskBase {
    var responseCode: Int = 0
    var responseCodeMessage: String = ""
    var body: String = ""

    def doInBackground1(args: Array[RestRequest[_]]): RestResponse[_] = {
      // do something
      val request = args(0)
      request.verb match {
        case GET =>
          val httpRequest = new HttpGet(request.url + request.getParams)
          //addHeaderParams(request)
          executeRequest(httpRequest)
        case POST => error("not supported")
          /*
          HttpPost request = new HttpPost(url);
          request = (HttpPost) addHeaderParams(request);
          request = (HttpPost) addBodyParams(request);
          executeRequest(request, url);
          */
      }

      val parsedVals =
        if(responseCode == 200)
          request.parseFunc(body)
        else
          Nil
      RestResponse(responseCode, body, responseCodeMessage, parsedVals)
    }

    private def executeRequest(request: HttpUriRequest) {
      val client = new DefaultHttpClient
      val params = client.getParams

      // Setting 30 second timeouts
      HttpConnectionParams.setConnectionTimeout(params, 30 * 1000)
      HttpConnectionParams.setSoTimeout(params, 30 * 1000)

      try {
        val httpResponse: HttpResponse = client.execute(request)
        responseCode = httpResponse.getStatusLine().getStatusCode()
        responseCodeMessage = httpResponse.getStatusLine().getReasonPhrase()

        val entity: HttpEntity = httpResponse.getEntity()

        if (entity != null) {

          val inputStream: InputStream = entity.getContent
          val stringBuilder = new StringBuilder(2048)
          scala.io.Source.fromInputStream(inputStream).getLines.foreach { line =>
            stringBuilder.append(line)
          }
          body = stringBuilder.toString

          // Closing the input stream will trigger connection release
          inputStream.close();
        }
      } 
      catch {
        case e: java.net.UnknownHostException =>
          e.printStackTrace
          responseCodeMessage = e.toString
          client.getConnectionManager().shutdown()
          client.getConnectionManager().shutdown()
        case e: ClientProtocolException =>
          client.getConnectionManager().shutdown()
          responseCodeMessage = e.toString
          e.printStackTrace()
        case e: IOException =>
          client.getConnectionManager().shutdown()
          responseCodeMessage = e.toString
          e.printStackTrace()
      }
    }

    override def onPostExecute(r: RestResponse[_]) = f(r.asInstanceOf[RestResponse[T]])
  }

  class LocalBinder extends Binder {
    def request[T](request: RestRequest[T])(callback: RestResponse[T] => Unit) {
      new RestRequestTask(callback).execute(request)
    }

    def parseRequest[T](request: JsonParseRequest[T])(callback: JsonParseResult[T] => Unit) {
      new JsonParseTask(callback).execute(request)
    }
  }

  private val binder: IBinder = new LocalBinder()
  override def onBind(intent: Intent): IBinder = {
    binder
  }

  override def onDestroy {
    Log.i(TAG, "onDestroy called")
  }

}

// vim: set ts=2 sw=2 et:
