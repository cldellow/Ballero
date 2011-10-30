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

class ProjectsActivity extends GDListActivity with SmartActivity {
  val TAG = "ProjectsActivity"

  var adapter: ItemAdapter = null

  private var projects: List[Project] = Nil
  case class QueueWithPattern(q: RavelryQueue, pattern: Option[Pattern])
  private var queued: List[QueueWithPattern] = Nil

  var refreshButton: LoaderActionBarItem = null
  var numPending: Int = 0
  var sortButton: ActionBarItem = null
  var actions: QuickActionBar = null
  var filter: ProjectStatus = Unknown

  case class ActionItem(action: QuickAction, label: String, filter: ProjectStatus)

  val BLACK_CF: ColorFilter = new LightingColorFilter(Color.BLACK, Color.BLACK)
  def d(id: Int): Drawable = {
    val d = this.getResources().getDrawable(id)
    d.setColorFilter(BLACK_CF)
    d
  }


  lazy val quickActions = List(
    ActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "all"), "all projects", Unknown),
    ActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "WIPs"), "in progress projects", InProgress),
    ActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "queued"), "queued projects", Queued),
    ActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "finished"), "finished projects", Finished),
    ActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "zzz"), "hibernating projects", Hibernated),
    ActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "frogged"), "frogged projects", Frogged)
  )

  override def onPause{
    super.onPause
    Data.currentUser.get.setUiPref("projects_filter", filter.toString)
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    adapter = new ItemAdapter(this)
    setListAdapter(adapter)
    refreshButton = addActionBarItem(Type.Refresh, R.id.action_bar_refresh).asInstanceOf[LoaderActionBarItem]
    sortButton = addActionBarItem(Type.Export, R.id.action_bar_locate)

    ensureLayout()
    actions = new QuickActionBar(this)
    quickActions.foreach { qa => actions.addQuickAction(qa.action) }

    actions.setOnQuickActionClickListener(new Listener)
  }

  class Listener extends OnQuickActionClickListener{
    def onQuickActionClicked(widget: QuickActionWidget, position: Int) {
      filter = quickActions(position).filter
      updateItems
    }
  }

  override def onHandleActionBarItemClick(item: ActionBarItem, position: Int): Boolean =
    item.getItemId match {
      case R.id.action_bar_refresh =>
        refreshAll(ForceNetwork)
        true
      case R.id.action_bar_locate =>
        actions.show(item.getItemView)
        true
      case _ =>
        true
    }

  override def createLayout: Int = R.layout.projects_activity

  override def onResume {
    super.onResume

    var savedFilter = Data.currentUser.get.uiPref("projects_filter", "Unknown")
    filter = ProjectStatus(savedFilter)

    refreshAll(FetchIfNeeded)
  }

  private def refreshAll(policy: RefreshPolicy) {
    refreshButton.setLoading(true)
    numPending += 2
    Data.currentUser.get.queuedProjects.render(policy, onQueueChanged)
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

  private def onQueueChanged(queued: List[RavelryQueue], pending: Boolean) {
    this.queued = queued.map { q =>
      QueueWithPattern(q, q.pattern_id.map { id =>
        RavelryApi.makePatternDetailsResource(id).get }.getOrElse(Nil).headOption)
    }
    updatePendings(pending)
    println("new queue")
    println(queued)
    updateItems
  }

  private def onProjectsChanged(projects: List[Project], pending: Boolean) {
    updatePendings(pending)
    this.projects = projects
    updateItems
  }

  private def filterProjects(projectish: Either[QueueWithPattern, Project]): Boolean = projectish match {
    case Left(q) =>
      filter == Unknown || filter == Queued
    case Right(p) =>
      filter == Unknown ||
      filter == p.status
  }

  private def updateItems {
    adapter = new ItemAdapter(this)

    val kept: List[Either[QueueWithPattern, Project]] =
      ((queued.map { Left(_) }) ::: (projects.map { Right(_) })).filter { filterProjects }
    if(filter != Unknown)
      adapter.add(new SeparatorItem(filter match {
        case Hibernated => "hibernating projects"
        case Queued => "queued projects"
        case Finished => "finished projects"
        case Frogged => "frogged projects"
        case InProgress => "in progress projects"
        // just to avoid match warning - never shows up
        case Unknown => "all projects"
      }))
    if(kept.isEmpty) {
      adapter.add(new TextItem("no projects found"))
    }

    val useQueueOrder = kept.forall { _.isLeft }
    val sorted =
      if(useQueueOrder)
        kept.collect { case Left(q) => Left(q)}.sortBy { -_.left.get.q.sort_order }
      else
        kept.sortBy { x => x match { case Left(q) => q.q.uiName case Right(p) => p.uiName } }

    sorted.zipWithIndex.foreach { case (projectish, index) =>
      projectish match {
        case Left(q) =>
          val subtitle = if(!useQueueOrder) "in queue" else "queue item %s".format(index + 1)
          val photos: List[Photo] = q.pattern.map { _.photos.getOrElse(Nil) }.getOrElse(Nil)
          val item = photos match {
              case photo :: xs => new ThumbnailItem(q.q.uiName, subtitle, R.drawable.ic_gdcatalog, photo.thumbnail_url)
              case Nil => new SubtitleItem(q.q.uiName, subtitle)
            }
          adapter.add(item)
        case Right(p) =>
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
  }
}
