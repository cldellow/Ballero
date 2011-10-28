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

class ProjectsActivity extends GDListActivity with SmartActivity {
  val TAG = "ProjectsActivity"

  var adapter: ItemAdapter = null

  private var projects: List[Project] = Nil
  private var queued: List[RavelryQueue] = Nil

  var refreshButton: LoaderActionBarItem = null
  var numPending: Int = 0

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    adapter = new ItemAdapter(this)
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

  private def refreshAll(policy: RefreshPolicy) {
    refreshButton.setLoading(true)
    numPending += 2
    Data.currentUser.get.queuedProjects.render(policy, onQueueChanged)
    Data.currentUser.get.projects.render(policy, onProjectsChanged)
  }

  private def onQueueChanged(queued: List[RavelryQueue], pending: Boolean) {
    this.queued = queued
    updatePendings(pending)
    println("new queue")
    println(queued)
    updateItems
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

  private def onProjectsChanged(projects: List[Project], pending: Boolean) {
    updatePendings(pending)
    this.projects = projects
    updateItems
  }

  private def updateItems {
    adapter = new ItemAdapter(this)

    (queued ::: projects).sortBy { _.uiName }.foreach { projectish =>
      projectish match {
        case q: RavelryQueue =>
          val subtitle = "in queue"
          val item = q.pattern_id.map { id => RavelryApi.makePatternDetailsResource(id).get }
            .getOrElse(Nil).flatMap { pattern => pattern.photos.getOrElse(Nil)
            } match {
              case photo :: xs => new ThumbnailItem(q.uiName, subtitle, R.drawable.ic_gdcatalog, photo.thumbnail_url)
              case Nil => new SubtitleItem(q.uiName, subtitle)
            }
          adapter.add(item)
        case p: Project =>
          val subtitle = p.status match {
            case Finished => "finished"
            case InProgress => "in progress"
            case Unknown => "unknown"
          }

          val item = p.first_photo match {
            case Some(photo) => new ThumbnailItem(p.uiName, subtitle, R.drawable.ic_gdcatalog, photo.thumbnail_url)
            case None => new SubtitleItem(p.uiName, subtitle)
          }
          adapter.add(item)
      }
    }

    setListAdapter(adapter)
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    /*
    val textItem: Item = l.getAdapter().getItem(position).asInstanceOf[Item]
    val intent: Intent = new Intent(this, textItem.getTag.asInstanceOf[Class[_]])
    startActivity(intent)
    */
  }
}
