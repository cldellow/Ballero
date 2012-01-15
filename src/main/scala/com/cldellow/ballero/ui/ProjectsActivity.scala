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
  private var queued: List[SimpleQueuedProject] = Nil
  private var minimalProjects: List[MinimalProjectish] = Nil
  private var tags: List[String] = Nil
  private var filterTags: List[String] = Nil

  var numPending: Int = 0
  var queuePending: Int = 0
  var projectsPending: Int = 0
  var filterButton: ActionBarItem = null
  var sortButton: ActionBarItem = null
  var filterActions: QuickActionGrid = null
  var projectSortActions: QuickActionGrid = null
  var queueSortActions: QuickActionGrid = null
  var filter: ProjectStatus = Unknown
  var projectSort: SortType = SortAlphabetically
  var queueSort: SortType = SortAlphabetically
  var fetchedQueue = false
  var fetchedProjects = false
  var intentLoaded = false

  case class FilterActionItem(action: QuickAction, label: String, filter: ProjectStatus)

  def d(id: Int): Drawable = {
    val d = this.getResources().getDrawable(id)
    d
  }


  sealed trait SortType
  object SortType {
    def apply(s: String): SortType = s match {
      case "SortHappiness" => SortHappiness
      case "SortStarted" => SortStarted
      case "SortCompleted" => SortCompleted
      case "SortProgress" => SortProgress
      case _ => SortAlphabetically
    }
  }

  case object SortAlphabetically extends SortType
  case object SortHappiness extends SortType
  case object SortStarted extends SortType
  case object SortCompleted extends SortType
  case object SortProgress extends SortType

  case class SortActionItem(action: QuickAction, label: String, sort: SortType, drawableId: Int)
  lazy val projectSortQuickActions = List(
    SortActionItem(new QuickAction(d(R.drawable.small_ic_menu_sort_alphabetically), "alpha"), "alphabetically",
    SortAlphabetically, R.drawable.gd_action_bar_sort_alpha),
    SortActionItem(new QuickAction(d(R.drawable.small_ic_menu_emoticons), "happiness"), "happiness", SortHappiness,
    R.drawable.small_ic_menu_emoticons),
    SortActionItem(new QuickAction(d(R.drawable.small_ic_menu_day), "started"), "started date", SortStarted,
    R.drawable.small_ic_menu_day),
    SortActionItem(new QuickAction(d(R.drawable.small_ic_menu_month), "completed"), "completed date", SortCompleted,
    R.drawable.small_ic_menu_month),
    SortActionItem(new QuickAction(d(R.drawable.small_ic_menu_sort_by_size), "% done"), "percent done", SortProgress,
    R.drawable.gd_action_bar_sort_by_size)
  )

  lazy val queueSortQuickActions = List(
    SortActionItem(new QuickAction(d(R.drawable.small_ic_menu_sort_alphabetically), "alpha"), "alphabetically",
    SortAlphabetically, R.drawable.gd_action_bar_sort_alpha),
    SortActionItem(new QuickAction(d(R.drawable.small_ic_menu_day), "added"), "added date", SortStarted,
    R.drawable.small_ic_menu_day),
    SortActionItem(new QuickAction(d(R.drawable.small_ic_menu_sort_by_size), "queue order"), "queue order", SortProgress,
    R.drawable.gd_action_bar_sort_by_size)
  )


  lazy val filterQuickActions = List(
    FilterActionItem(new QuickAction(d(R.drawable.small_ic_menu_agenda), "all"), "all projects", Unknown),
    FilterActionItem(new QuickAction(d(R.drawable.small_ic_menu_agenda), "WIPs"), "in progress projects", InProgress),
    FilterActionItem(new QuickAction(d(R.drawable.small_ic_menu_agenda), "queued"), "queued projects", Queued),
    FilterActionItem(new QuickAction(d(R.drawable.small_ic_menu_agenda), "finished"), "finished projects", Finished),
    FilterActionItem(new QuickAction(d(R.drawable.small_ic_menu_agenda), "zzz"), "hibernating projects", Hibernated),
    FilterActionItem(new QuickAction(d(R.drawable.small_ic_menu_agenda), "frogged"), "frogged projects", Frogged),
    FilterActionItem(new QuickAction(d(R.drawable.small_ic_menu_agenda), "tagged..."), "tagged projects", Unknown)
  )

  override def onPause{
    super.onPause
    Data.currentUser.get.setUiPref("projects_filter", filter.toString)
    Data.currentUser.get.setUiPref("projects_sort", projectSort.toString)
    Data.currentUser.get.setUiPref("queue_sort", queueSort.toString)
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

    projectSortActions = new QuickActionGrid(this)
    projectSortQuickActions.foreach { qa => projectSortActions.addQuickAction(qa.action) }
    projectSortActions.setOnQuickActionClickListener(new ProjectSortListener)

    queueSortActions = new QuickActionGrid(this)
    queueSortQuickActions.foreach { qa => queueSortActions.addQuickAction(qa.action) }
    queueSortActions.setOnQuickActionClickListener(new QueueSortListener)
  }

  val BLACK_CF: ColorFilter = new LightingColorFilter(Color.BLACK, Color.BLACK)
  val WHITE_CF: ColorFilter = new LightingColorFilter(Color.WHITE, Color.WHITE)
  def db(id: Int): Drawable = {
    val d = this.getResources().getDrawable(id).mutate()
    d.setColorFilter(WHITE_CF)
    d
  }

  class ProjectSortListener extends OnQuickActionClickListener{
    def onQuickActionClicked(widget: QuickActionWidget, position: Int) {
      projectSort = projectSortQuickActions(position).sort
      updateSortButton
      redraw()
    }
  }

  class QueueSortListener extends OnQuickActionClickListener{
    def onQuickActionClicked(widget: QuickActionWidget, position: Int) {
      queueSort = queueSortQuickActions(position).sort
      updateSortButton
      redraw()
    }
  }

  def updateSortButton {
    if(useQueueOrder) {
      val drawableId = queueSortQuickActions.filter { _.sort == queueSort }.head.drawableId
      sortButton.setDrawable(db(drawableId))
    } else {
      val drawableId = projectSortQuickActions.filter { _.sort == projectSort }.head.drawableId
      sortButton.setDrawable(db(drawableId))
    }
  }


  class FilterListener extends OnQuickActionClickListener{
    def onQuickActionClicked(widget: QuickActionWidget, position: Int) {
      if(position == 6) { // tagged...
        filter = Unknown
        updateSortButton
        showTagDialog()
      } else {
        filter = filterQuickActions(position).filter
        filterTags = Nil
        updateTitle
        updateSortButton
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
        if(useQueueOrder)
          queueSortActions.show(item.getItemView)
        else
          projectSortActions.show(item.getItemView)
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

    var savedProjectSort = Data.currentUser.get.uiPref("projects_sort", "SortAlphabetically")
    projectSort = SortType(savedProjectSort)
    var savedQueueSort = Data.currentUser.get.uiPref("queue_sort", "SortProgress")
    queueSort = SortType(savedQueueSort)
    updateSortButton
    

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
    tags = (Set() ++ minimalProjects.flatMap { _.t.getOrElse(Nil) }).toList.sorted
    println("tags: %s".format(tags))
    fetchedProjects = true
    fetchedQueue = true
    redraw()
  }

  var _dt = new java.util.Date
  var _now = new java.util.Date
  var _yesterday = new java.util.Date(_now.getTime() - (86400 * 1000))
  def fmtDate(t: Int): String = {
    if(t == 0)
      return "never"

    _dt.setTime(t.toLong * 1000)
    if(_dt.getYear == _now.getYear && _dt.getMonth == _now.getMonth && _dt.getDate == _now.getDate)
      "today"
    else if(_dt.getYear == _yesterday.getYear && _dt.getMonth == _yesterday.getMonth && _dt.getDate == _yesterday.getDate)
      "yesterday"
    else
      "%s %s, %s".format(
        _dt.getMonth match {
          case 0 => "Jan"
          case 1 => "Feb"
          case 2 => "Mar"
          case 3 => "Apr"
          case 4 => "May"
          case 5 => "June"
          case 6 => "July"
          case 7 => "Aug"
          case 8 => "Sept"
          case 9 => "Oct"
          case 10 => "Nov"
          case 11 => "Dec"
        }, _dt.getDate, _dt.getYear + 1900)
  }

  def useQueueOrder = filter == Queued

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


    val sorted: List[MinimalProjectish] =
      if(useQueueOrder) {
        val collected = kept.collect { case q if q._actualStatus == Queued => q}
        queueSort match {
          case SortAlphabetically =>
            collected.sortBy { _.n }
          case SortStarted =>
            collected.sortBy { -_.c.getOrElse(0) }
          case _ =>
            collected.sortBy { _.o.getOrElse(999) }
          }
      } else {
        projectSort match {
          case SortAlphabetically =>
            kept.sortBy { _.n.toLowerCase }
          case SortHappiness =>
            kept.sortBy { item =>
              if(item._actualStatus == Queued) 1 else -item.r.getOrElse(0)
            }
          case SortProgress =>
            kept.sortBy { -_.p.getOrElse(0) }
          case SortStarted =>
            kept.sortBy { item => if(item._actualStatus == Queued) 1 else -item.c.getOrElse(0) }
          case SortCompleted =>
            kept.sortBy { -_.f.getOrElse(0) }
        }
      }

    sorted.zipWithIndex.foreach { case (projectish, index) =>
      projectish match {
        case q if q._actualStatus == Queued =>
          val subtitle =
            projectSort match {
              case _ =>
                if(!useQueueOrder)
                  "in queue"
                else
                  queueSort match {
                    case SortStarted =>
                      "added %s, #%s".format(fmtDate(q.c.getOrElse(0)), q.o.getOrElse(0))
                    case _ =>
                      "queue item %s".format(q.o.getOrElse(0))
                  }
            }

          val item = q.img match {
              case Some(url) => new ThumbnailItem(q.n, subtitle, R.drawable.ic_gdcatalog, url)
              case None => new SubtitleItem(q.n, subtitle)
            }
          item.goesToWithData[QueuedProjectDetailsActivity, Id](Id(q.id))
          adapter.add(item)
        case p =>
          val subtitle =
            projectSort match {
              case SortStarted|SortCompleted =>
                (if(projectSort == SortStarted) "%s, started %s" else "%s, completed %s").format(
                  (p._actualStatus: @unchecked) match {
                    case Finished => "finished"
                    case InProgress => "in progress"
                    case Frogged => "frogged"
                    case Hibernated => "hibernating"
                    case Unknown => "unknown"
                  }, fmtDate(if(projectSort == SortStarted) p.c.getOrElse(0) else p.f.getOrElse(0)))
              case SortHappiness =>
                "%s, %s".format(
                  (p._actualStatus: @unchecked) match {
                    case Finished => "finished"
                    case InProgress => "in progress"
                    case Frogged => "frogged"
                    case Hibernated => "hibernating"
                    case Unknown => "unknown"
                  }, p.r.getOrElse(5) match {
                    case 5 => "not rated"
                    case 0 => "ugh"
                    case 1 => "meh"
                    case 2 => "it's ok"
                    case 3 => "like it"
                    case 4 => "love it"
                  })
              case SortProgress =>
                "%s, %s%% complete".format(
                  (p._actualStatus: @unchecked) match {
                    case Finished => "finished"
                    case InProgress => "in progress"
                    case Frogged => "frogged"
                    case Hibernated => "hibernating"
                    case Unknown => "unknown"
                  }, p.p.getOrElse(0))
              case _ =>
                (p._actualStatus: @unchecked) match {
                  case Finished => "finished"
                  case InProgress => "in progress"
                  case Frogged => "frogged"
                  case Hibernated => "hibernating"
                  case Unknown => "unknown"
                }
            }

          val item = p.img match {
            case Some(url) => new ThumbnailItem(p.n, subtitle, R.drawable.ic_gdcatalog, url)
            case None => new SubtitleItem(p.n, subtitle)
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
    Data.currentUser.get.queue.render(policy, onQueueChanged)
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

  private def onQueueChanged(queued: List[SimpleQueuedProject], delta: Int) {
    queuePending += delta
    updatePendings(delta)

    if(queuePending <= 0) {
      queuePending = 0
      fetchedQueue = true
      if(progressDialog != null)
        progressDialog.incrementProgressBy(1)
      val curTime = System.currentTimeMillis
      this.queued = queued
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
    val tags = projectish.t.getOrElse(Nil)
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
    val allProjects: List[Either[SimpleQueuedProject, Project]] =
      ((queued.map { Left(_) }) ::: (projects.map { Right(_) }))
    val minimalProjects: List[MinimalProjectish] = allProjects.map { x =>
      x match {
        case Left(qwp) =>
          val photo = qwp.best_photo.map { _.thumbnail_url }
          MinimalProjectish(
            c = qwp._createdAtInt,
            f = None,
            id = qwp.id,
            img = photo,
            p = None,
            r = None,
            o = Some(qwp.sort_order),
            s = Some("Queued"),
            t = Some(Nil),
            n = qwp.uiName)
        case Right(p) =>
          MinimalProjectish(
            c = p._startedOnInt,
            f = p._completedOnInt,
            p = p.progress,
            r = p.rating,
            id = p.id,
            img = p.first_photo.map { _.thumbnail_url },
            o = None,
            s = Some(p.status.toString),
            t = p.tag_names,
            n = p.uiName)
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
