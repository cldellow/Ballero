package com.cldellow.ballero.ui

import com.cldellow.ballero.R
import com.cldellow.ballero.service._
import com.cldellow.ballero.data._
import scala.xml.XML

import scala.collection.JavaConversions._
import android.app.Activity
import android.content._
import android.location._
import android.graphics._
import android.graphics.drawable._
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget._
import greendroid.app._
import greendroid.widget.QuickActionWidget._
import greendroid.widget.ActionBarItem.Type
import greendroid.graphics.drawable._
import greendroid.widget._
import greendroid.widget.item._

class StashListActivity extends GDListActivity with NavigableListActivity with SmartActivity {
  val TAG = "StashListActivity"

  var adapter: ItemAdapter = null

  var fetchedYarns = false
  var refreshButton: LoaderActionBarItem = null
  var numPending: Int = 0
  var minimalStash: List[MinimalStashedYarn] = Nil
  var _stashedYarns: List[SentinelStashedYarn] = Nil
  var sortButton: ActionBarItem = null

  override def onPause{
    super.onPause
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    adapter = new ItemAdapter(this)
    setListAdapter(adapter)
    refreshButton = addActionBarItem(Type.Refresh, R.id.action_bar_refresh).asInstanceOf[LoaderActionBarItem]

    ensureLayout()
  }

  override def onHandleActionBarItemClick(item: ActionBarItem, position: Int): Boolean =
    item.getItemId match {
      case R.id.action_bar_refresh =>
        refreshAll(ForceNetwork)
        true
      case _ =>
        true
    }

  override def createLayout: Int = R.layout.projects_activity

  override def onResume {
    super.onResume

    updateTitle

    if(!fetchedYarns)
      maybeFetch()
  }

  private def maybeFetch() {
    val toParse = Data.get(StashListActivity.MINIMAL_STASH, "[]")
    if(toParse == "[]") {
      refreshAll(FetchIfNeeded)
    } else {
      doParse(toParse)
    }
  }

  private def doParse(str: String) {
    restServiceConnection.parseRequest[MinimalStashedYarn](JsonParseRequest[MinimalStashedYarn](str,
    Parser.parseList[MinimalStashedYarn]))(onMinimalStashedYarnsLoaded)
  }

  def onMinimalStashedYarnsLoaded(response: JsonParseResult[MinimalStashedYarn]) {
    minimalStash = response.parsedVals
    redraw()
  }

  def redraw() {
    adapter = new ItemAdapter(this)

    val kept: List[MinimalStashedYarn] = minimalStash

    setListAdapter(adapter)
  }


  private def updateTitle {
    setTitle("stash list")
  }

  private def refreshAll(policy: RefreshPolicy) {
    refreshButton.setLoading(true)
    numPending += 2
    fetchedYarns = false
    Data.currentUser.get.stash.render(policy, onYarnsChanged(policy))
  }

  private def updatePendings(delta: Int) {
    numPending += delta
    if(numPending <= 0) {
      numPending = 0
      refreshButton.setLoading(false)
    }
  }

  private def onYarnsChanged(policy: RefreshPolicy)(stashed: List[SentinelStashedYarn], delta: Int) {
    fetchedYarns = true
    stashed.foreach { stash =>
      numPending += 2
      RavelryApi.makeStashDetailsResource(stash.id).render(policy, onStashDetailsChanged)
    }
    _stashedYarns = stashed
    updatePendings(delta)
  }

  private def onStashDetailsChanged(stashedDetails: List[StashedYarn], delta: Int) {
    updatePendings(delta)

  }
}

object StashListActivity {
  val MINIMAL_STASH = "minimal_stash"
}
