package com.cldellow.ballero.ui

import com.cldellow.ballero.R
import com.cldellow.ballero.service._
import com.cldellow.ballero.data._
import scala.xml.XML

import scala.collection.JavaConversions._
import android.app.Activity
import android.content._
import android.location._
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget._
import greendroid.app._
import greendroid.widget.ActionBarItem.Type
import greendroid.graphics.drawable._
import greendroid.widget._
import greendroid.widget.item._

class RavellerHomeActivity extends GDListActivity with SmartActivity {
  val TAG = "RavellerHomeActivity"

  var adapter: ItemAdapter = null

  var needlesItem: SubtitleItem = null
  var projectsItem: SubtitleItem = null

  var refreshButton: LoaderActionBarItem = null

  var numPending: Int = 0

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    adapter = new ItemAdapter(this)

    needlesItem = goesTo[NeedlesActivity, SubtitleItem](new SubtitleItem("needles", "", true))
    projectsItem = goesTo[NeedlesActivity, SubtitleItem](new SubtitleItem("projects", "", true))
    adapter.add(projectsItem)
    adapter.add(needlesItem)

    setListAdapter(adapter)
    refreshButton = addActionBarItem(Type.Refresh, R.id.action_bar_refresh).asInstanceOf[LoaderActionBarItem]
  }

  override def onHandleActionBarItemClick(item: ActionBarItem, position: Int): Boolean =
    item.getItemId match {
      case R.id.action_bar_refresh =>
        refreshAll(ForceNetwork)
        true
      case _ =>
        true
    }

  override def onResume {
    super.onResume
    refreshAll(FetchIfNeeded)
  }

  private var projectsPending = 0
  private def refreshAll(policy: RefreshPolicy) {
    refreshButton.setLoading(true)
    numPending += 3
    projectsPending += 2
    Data.currentUser.get.needles.render(policy, onNeedlesChanged)
    Data.currentUser.get.queue.render(policy, onQueueChanged)
    Data.currentUser.get.projects.render(policy, onProjectsChanged)
  }

  private def updatePendings(pending: Boolean) {
    if(!pending) {
      numPending -= 1
      if(numPending <= 0) {
        numPending = 0
        refreshButton.setLoading(false)
      }
    }
  }

  private var queued: List[Id] = Nil
  private var projects: List[Project] = Nil

  private def onQueueChanged(queued: List[Id], pending: Boolean) {
    updatePendings(pending)
    if(!pending)
      projectsPending -= 1

    this.queued = queued
    updateProjectsItem

  }

  private def onProjectsChanged(projects: List[Project], pending: Boolean) {
    updatePendings(pending)

    if(!pending)
      projectsPending -= 1

    this.projects = projects
    updateProjectsItem
  }

  private def updateProjectsItem {
    val subtitle = 
      if(queued.isEmpty && projects.isEmpty)
        "no projects"
      else {
      val queue = if(queued.isEmpty) "queue empty" else "%s queued".format(queued.length)
      val projectStrings = projects.filter { proj => proj.status == InProgress || proj.status == Finished }.groupBy { _.status_name }.map {
      case (status, items) => "%s %s".format(items.length, status.toLowerCase) }

      (projectStrings.toList ::: List(queue)).mkString(", ")
    }

    projectsItem.subtitle = subtitle
    projectsItem.inProgress = projectsPending > 0

    onContentChanged
  }

  private def onNeedlesChanged(needles: List[Needle], pending: Boolean) {
    updatePendings(pending)

    val subtitle = if(needles.isEmpty) "no needles" else needles.groupBy { _.kind }
      .map { case(kind, needles) => "%s %s".format(needles.length, kind) }
      .mkString(", ")

    needlesItem.subtitle = subtitle
    needlesItem.inProgress = pending

    onContentChanged
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val textItem: Item = l.getAdapter().getItem(position).asInstanceOf[Item]

    val intent: Intent = new Intent(this, textItem.getTag.asInstanceOf[Class[_]])
    startActivity(intent)
  }

}
