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

class ProjectsActivity extends GDListActivity with NavigableListActivity with SmartActivity {
  val TAG = "ProjectsActivity"

  var adapter: ItemAdapter = null

  private var projects: List[Project] = Nil
  case class QueueWithPattern(q: RavelryQueue, pattern: Option[Pattern])
  private var queued: List[QueueWithPattern] = Nil
  private var minimalProjects: List[MinimalProjectish] = Nil
  private var tags: Set[String] = Set()

  var refreshButton: LoaderActionBarItem = null
  var numPending: Int = 0
  var queuePending: Int = 0
  var projectsPending: Int = 0
  var sortButton: ActionBarItem = null
  var actions: QuickActionGrid = null
  var filter: ProjectStatus = Unknown
  var fetchedQueue = false
  var fetchedProjects = false

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
    ActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "frogged"), "frogged projects", Frogged),
    ActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "tagged..."), "tagged projects", Unknown)
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
    actions = new QuickActionGrid(this)
    quickActions.foreach { qa => actions.addQuickAction(qa.action) }

    actions.setOnQuickActionClickListener(new Listener)
  }

  class Listener extends OnQuickActionClickListener{
    def onQuickActionClicked(widget: QuickActionWidget, position: Int) {
      filter = quickActions(position).filter
      updateTitle
      redraw()
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
    updateTitle

    if(!fetchedProjects || !fetchedQueue)
      maybeFetch()
  }

  private def maybeFetch() {
    val toParse = Data.get(ProjectsActivity.MINIMAL_PROJECTS, "[]")
    if(toParse == "[]") {
      refreshAll(FetchIfNeeded)
    } else {
      doParse(toParse)
    }
  }

  private def doParse(str: String) {
    restServiceConnection.parseRequest[MinimalProjectish](JsonParseRequest[MinimalProjectish](str,
    Parser.parseList[MinimalProjectish] _))(onMinimalProjectsLoaded)
  }

  def onMinimalProjectsLoaded(response: JsonParseResult[MinimalProjectish]) {
    minimalProjects = response.parsedVals
    tags = new collection.immutable.TreeSet[String]() ++ minimalProjects.flatMap { _.tags.getOrElse(Nil) }
    println("tags: %s".format(tags))
    fetchedProjects = true
    fetchedQueue = true
    redraw()
  }

  def redraw() {
    adapter = new ItemAdapter(this)

    val kept: List[MinimalProjectish] = 
      minimalProjects.filter { filterProjects }
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

    val useQueueOrder = kept.forall { _._actualStatus == Queued }

    val sorted: List[MinimalProjectish] =
      if(useQueueOrder)
        kept.collect { case q if q._actualStatus == Queued => q}.sortBy { _.order.getOrElse(999) }
      else
        kept.sortBy { _.uiName.toLowerCase }

    sorted.zipWithIndex.foreach { case (projectish, index) =>
      projectish match {
        case q if q._actualStatus == Queued =>
          val subtitle = if(!useQueueOrder) "in queue" else "queue item %s".format(index + 1)
          val item = q.imgUrl match {
              case Some(url) => new ThumbnailItem(q.uiName, subtitle, R.drawable.ic_gdcatalog, url)
              case None => new SubtitleItem(q.uiName, subtitle)
            }
          item.goesToWithData[QueuedProjectDetailsActivity, Id](Id(q.id))
          adapter.add(item)
        case p =>
          val subtitle = p._actualStatus match {
            case Finished => "finished"
            case InProgress => "in progress"
            case Frogged => "frogged"
            case Hibernated => "hibernating"
            case Unknown => "unknown"
          }

          val item = p.imgUrl match {
            case Some(url) => new ThumbnailItem(p.uiName, subtitle, R.drawable.ic_gdcatalog, url)
            case None => new SubtitleItem(p.uiName, subtitle)
          }
          item.goesToWithData[ProjectDetailsActivity, Id](Id(p.id))
          adapter.add(item)
      }
    }

    setListAdapter(adapter)
  }


  private def updateTitle {
    setTitle(filter match {
        case Hibernated => "hibernated"
        case InProgress => "in progress"
        case Unknown => "all projects"
        case Queued => "queued"
        case Frogged => "frogged"
        case Finished => "finished"
      })
  }

  private def refreshAll(policy: RefreshPolicy) {
    refreshButton.setLoading(true)
    numPending += 4
    queuePending += 2
    projectsPending += 2
    fetchedQueue = false
    fetchedProjects = false
    Data.currentUser.get.queuedProjects.render(policy, onQueueChanged)
    Data.currentUser.get.projects.render(policy, onProjectsChanged)
  }

  private def updatePendings(delta: Int) {
    numPending += delta
    if(numPending <= 0) {
      numPending = 0
      refreshButton.setLoading(false)
    }
  }

  private def onQueueChanged(queued: List[RavelryQueue], delta: Int) {
    queuePending += delta
    updatePendings(delta)

    if(queuePending <= 0) {
      queuePending = 0
      fetchedQueue = true
      val curTime = System.currentTimeMillis
      this.queued = queued.map { q =>
        QueueWithPattern(q, q.pattern)
      }
      info("time for onQueueChanged to map: %s".format(System.currentTimeMillis - curTime))
      updateItems
    }
  }

  private def onProjectsChanged(projects: List[Project], delta: Int) {
    info("got projects: %s".format(projects))
    projectsPending += delta
    updatePendings(delta)
    if(projectsPending <= 0) {
      projectsPending = 0
      fetchedProjects = true
      this.projects = projects
      updateItems
    }
  }

  private def filterProjects(projectish: MinimalProjectish): Boolean = projectish._actualStatus match {
    case Queued =>
      filter == Unknown || filter == Queued
    case x =>
      filter == Unknown || filter == x
  }

  private def updateItems {
    if(numPending > 0)
      return
    val allProjects: List[Either[QueueWithPattern, Project]] =
      ((queued.map { Left(_) }) ::: (projects.map { Right(_) }))
    val minimalProjects: List[MinimalProjectish] = allProjects.map { x =>
      x match {
        case Left(qwp) =>
          val photos: List[Photo] = qwp.pattern.map { _.photos.getOrElse(Nil) }.getOrElse(Nil)
          val photo = photos.headOption map { _.thumbnail_url }
          MinimalProjectish(qwp.q.id,
            photo,
            Some(qwp.q.sort_order),
            Some("Queued"),
            Some(Nil),
            qwp.q.uiName)
        case Right(p) =>
          MinimalProjectish(p.id,
            p.first_photo.map { _.thumbnail_url },
            None,
            Some(p.status.toString),
            p.tag_names,
            p.uiName)
      }
    }
    val saved = Parser.serializeList[MinimalProjectish](minimalProjects)
    Data.save(ProjectsActivity.MINIMAL_PROJECTS, saved)
    doParse(saved)
  }
}

object ProjectsActivity {
  val MINIMAL_PROJECTS = "minimal_projects"
}
