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
import greendroid.widget.item._

class MainActivity extends GDListActivity with NavigableListActivity with SmartActivity {
  val TAG = "MainActivity"

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setTitle("Ballero")
    val adapter = new ItemAdapter(this)

    adapter.add(new TextItem("find a local yarn store").goesTo[FindLysActivity])

    Data.users.sortBy { _.name }.foreach { user => 
      adapter.add(new TextItem(user.name).goesToWithData[RavellerHomeActivity, User](user))
    }

    adapter.add(new TextItem("link a ravelry account").goesTo[AddRavelryAccountActivity])

    Data.users.foreach { user => info("got user: %s".format(user)) }
    setListAdapter(adapter);

  }
}
