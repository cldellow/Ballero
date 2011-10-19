package cldellow.ballero

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

class AddRavelryAccountActivity extends GDActivity {
  val TAG = "AddRavelryAccountActivity"
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
  }

  override def createLayout(): Int = {
      return R.layout.addravelry
  }

  def loginClick(v: View) {
     val progressBar = findViewById(R.id.progressBar).asInstanceOf[ProgressBar]
     progressBar.setVisibility(View.VISIBLE)


  }

}
