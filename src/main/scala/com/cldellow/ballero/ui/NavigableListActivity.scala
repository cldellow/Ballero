package com.cldellow.ballero.ui

import com.cldellow.ballero.service._
import com.cldellow.ballero.data._
import com.cldellow.ballero.R

import org.json.JSONObject
import java.net.URLEncoder
import android.app.Activity
import android.content._
import android.location._
import android.os._
import android.util.Log
import android.view.View
import android.widget._
import java.util.Locale
import greendroid.app._
import greendroid.widget._
import greendroid.widget.item._

trait NavigableListActivity { this: GDListActivity =>
  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val item: Item = l.getAdapter().getItem(position).asInstanceOf[Item]

    val hints = item.getTag.asInstanceOf[NavHint]
    val intent: Intent = new Intent(this, hints.clazz)
    if(hints.params.isDefined)
      intent.putExtra("com.cldellow.params", hints.params.get)

    startActivity(intent)
  }
}

// vim: set ts=2 sw=2 et:
