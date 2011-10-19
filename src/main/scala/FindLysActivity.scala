package cldellow.ballero

import scala.collection.JavaConversions._
import android.app.Activity
import android.content.Context
import android.location._
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import greendroid.app._
import greendroid.widget._

class FindLysActivity extends GDListActivity {
  val TAG = "FindLysActivity"
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val adapter = new ItemAdapter(this)
    setListAdapter(adapter);
  }
}
