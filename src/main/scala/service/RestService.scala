package cldellow.ballero.service

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
/*
 new DownloadImageTask().execute("http://example.com/image.png");
}

private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
     protected Bitmap doInBackground(String... urls) {
         return loadImageFromNetwork(urls[0]);
     }

     protected void onPostExecute(Bitmap result) {
         mImageView.setImageBitmap(result);
     }
 }
 */
  private class IntTask(f: Integer => Unit) extends IntTaskBase {
    def doInBackground1(args: Array[String]): Integer = {
      3
    }

    override def onPostExecute(i: Integer) = f(i)
  }

  private class RestRequestTask(f: RestResponse => Unit) extends RestRequestTaskBase {
    var responseCode: Int = 0
    var responseCodeMessage: String = ""
    var body: String = ""

    def doInBackground1(args: Array[RestRequest]): RestResponse = {
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
      RestResponse(responseCode, body)
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
          body = scala.io.Source.fromInputStream(inputStream).getLines.reduceLeft { _ + _ }

          // Closing the input stream will trigger connection release
          inputStream.close();
        }
      } catch {
        case e: ClientProtocolException =>
          client.getConnectionManager().shutdown()
          e.printStackTrace()
        case e: IOException =>
          client.getConnectionManager().shutdown()
          e.printStackTrace()
      }
  }

    override def onPostExecute(r: RestResponse) = f(r)
  }

  class LocalBinder extends Binder {
    //def getService: RestService = RestService.this
    def getNumber(callback: Integer => Unit) {
      new IntTask(callback).execute()
    }

    def request(request: RestRequest)(callback: RestResponse => Unit) {
      new RestRequestTask(callback).execute(request)
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
