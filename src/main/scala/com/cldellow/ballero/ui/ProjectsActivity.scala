package com.cldellow.ballero.ui

import com.cldellow.ballero.R
import com.cldellow.ballero.service._
import com.cldellow.ballero.data._
import scala.xml.XML

import scala.collection.JavaConversions._
import android.app._
import android.content._
import android.location._
import android.graphics._
import android.graphics.drawable._
import android.os.Bundle
import android.util.Log
import android.view._
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
  private var tags: List[String] = Nil
  private var filterTags: List[String] = Nil

  var numPending: Int = 0
  var queuePending: Int = 0
  var projectsPending: Int = 0
  var filterButton: ActionBarItem = null
  var sortButton: ActionBarItem = null
  var filterActions: QuickActionGrid = null
  var sortActions: QuickActionGrid = null
  var filter: ProjectStatus = Unknown
  var fetchedQueue = false
  var fetchedProjects = false
  var intentLoaded = false

  case class FilterActionItem(action: QuickAction, label: String, filter: ProjectStatus)

  def d(id: Int): Drawable = {
    val d = this.getResources().getDrawable(id)
    d
  }


  sealed trait SortType
  case object SortAlphabetically extends SortType
  case class SortActionItem(action: QuickAction, label: String, sort: SortType)
  lazy val sortQuickActions = List(
    SortActionItem(new QuickAction(d(R.drawable.ic_menu_sort_alphabetically), "sort alpha"), "alphabetically",
    SortAlphabetically)
  )

  lazy val filterQuickActions = List(
    FilterActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "all"), "all projects", Unknown),
    FilterActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "WIPs"), "in progress projects", InProgress),
    FilterActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "queued"), "queued projects", Queued),
    FilterActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "finished"), "finished projects", Finished),
    FilterActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "zzz"), "hibernating projects", Hibernated),
    FilterActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "frogged"), "frogged projects", Frogged),
    FilterActionItem(new QuickAction(d(R.drawable.gd_action_bar_compose), "tagged..."), "tagged projects", Unknown)
  )

  override def onPause{
    super.onPause
    Data.currentUser.get.setUiPref("projects_filter", filter.toString)
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    adapter = new ItemAdapter(this)
    setListAdapter(adapter)
    filterButton = addActionBarItem(Type.Search, R.id.action_bar_locate)
    sortButton = addActionBarItem(Type.SortAlphabetically, R.id.action_bar_sort)

    ensureLayout()

    filterActions = new QuickActionGrid(this)
    filterQuickActions.foreach { qa => filterActions.addQuickAction(qa.action) }
    filterActions.setOnQuickActionClickListener(new FilterListener)

    sortActions = new QuickActionGrid(this)
    sortQuickActions.foreach { qa => sortActions.addQuickAction(qa.action) }
    sortActions.setOnQuickActionClickListener(new SortListener)
  }

  class SortListener extends OnQuickActionClickListener{
    def onQuickActionClicked(widget: QuickActionWidget, position: Int) {
    }
  }


  class FilterListener extends OnQuickActionClickListener{
    def onQuickActionClicked(widget: QuickActionWidget, position: Int) {
      if(position == 6) { // tagged...
        filter = Unknown
        showTagDialog()
      } else {
        filter = filterQuickActions(position).filter
        filterTags = Nil
        updateTitle
        redraw()
      }
    }
  }

  val PROGRESS_DIALOG = 0
  var progressDialog: ProgressDialog = null
  override def onCreateDialog(id: Int): Dialog = {
    id match {
      case PROGRESS_DIALOG =>
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading project and queue lists, this could be a while.")
        progressDialog.setIndeterminate(true)
        progressDialog
      case _ => null
    }
  }

  def showTagDialog() {
    val builder: AlertDialog.Builder = new AlertDialog.Builder(this)
    val items = tags.map { _.asInstanceOf[CharSequence] }.toArray
    val setFilterTags = filterTags.toSet
    val checkedList = tags.map { tag => setFilterTags.contains(tag) }
    val checked = checkedList.toArray
    val selected: collection.mutable.Set[Int] = collection.mutable.Set() ++
      checkedList.zipWithIndex.filter { _._1 }.map { _._2}

    builder.setTitle("Select tags")
    builder.setMultiChoiceItems(
      items,
      checked,
      new DialogInterface.OnMultiChoiceClickListener() {
        def onClick(dialog: DialogInterface, item: Int, isSelected: Boolean) {
          if(isSelected)
            selected += item
          else
            selected -= item
        }
      }
    )
    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, item: Int) {
        val newTags = selected.map { tags(_) }.toList
        info("new tags: %s".format(newTags))
        filterTags = newTags
        updateTitle
        redraw()
      }
    });
    val alert: AlertDialog = builder.create()
    alert.show()
  }

  override def onHandleActionBarItemClick(item: ActionBarItem, position: Int): Boolean =
    item.getItemId match {
      case R.id.action_bar_sort =>
        sortActions.show(item.getItemView)
        true
      case R.id.action_bar_locate =>
        filterActions.show(item.getItemView)
        true
      case _ =>
        true
    }

  override def createLayout: Int = R.layout.projects_activity

  override def onResume {
    super.onResume

    var savedFilter = Data.currentUser.get.uiPref("projects_filter", "Unknown")
    filter = ProjectStatus(savedFilter)

    if(!intentLoaded) {
      intentLoaded = true
      val projectIntent = getParams[ProjectsIntent]
      if(projectIntent.isDefined) {
        filter = Unknown
        filterTags = projectIntent.get.tags
      }
    }

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
    tags = (Set() ++ minimalProjects.flatMap { _.tags.getOrElse(Nil) }).toList.sorted
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
        case Unknown => 
          if (filterTags.isEmpty)
            "all projects"
          else
            "tagged: %s".format(filterTags.mkString(", "))
        case Queued => "queued"
        case Frogged => "frogged"
        case Finished => "finished"
      })
  }

  private def refreshAll(policy: RefreshPolicy) {
    showDialog(PROGRESS_DIALOG)
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
      dismissDialog(PROGRESS_DIALOG)
      progressDialog = null
    }
  }

  private def onQueueChanged(queued: List[RavelryQueue], delta: Int) {
    queuePending += delta
    updatePendings(delta)

    if(queuePending <= 0) {
      queuePending = 0
      fetchedQueue = true
      if(progressDialog != null)
        progressDialog.incrementProgressBy(1)
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
      if(progressDialog != null)
        progressDialog.incrementProgressBy(1)
      this.projects = projects
      updateItems
    }
  }

  private def filterTags(projectish: MinimalProjectish): Boolean = {
    val tags = projectish.tags.getOrElse(Nil)
    filterTags.forall{ reqd => tags.contains(reqd) }
  }

  private def filterProjects(projectish: MinimalProjectish): Boolean = 
    if(filter == Unknown && !filterTags.isEmpty) {
      filterTags(projectish)
    } else
      projectish._actualStatus match {
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

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val inflater: MenuInflater = getMenuInflater()
    inflater.inflate(R.menu.projects_menu, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.refresh => refreshAll(ForceNetwork)
      case R.id.notebook =>
        val intent = new Intent(this, classOf[RavellerHomeActivity])
        startActivity(intent)
      case _ =>
    }
    true
  }
}

object ProjectsActivity {
  val MINIMAL_PROJECTS = "minimal_projects"
}
