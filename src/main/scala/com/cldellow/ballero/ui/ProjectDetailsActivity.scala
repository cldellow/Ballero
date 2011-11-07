package com.cldellow.ballero.ui

import com.cldellow.ballero.R
import greendroid.widget.ActionBarItem.Type
import com.cldellow.ballero.data._
import android.graphics._
import scala.collection.JavaConversions._
import android.app.Activity
import android.content._
import android.location._
import android.os.Bundle
import android.util.Log
import android.view._
import android.widget._
import greendroid.app._
import se.fnord.android.layout._
import greendroid.widget._
import greendroid.widget.item._

class ProjectDetailsActivity extends GDActivity with SmartActivity {
  val TAG = "ProjectDetailsActivity"

  var currentId: Int = 0

  lazy val yarnLayout = findView(R.id.yarnLayout)
  lazy val lblCompletedOnValue = findLabel(R.id.lblCompletedOnValue)
  lazy val lblStartedOnValue = findLabel(R.id.lblStartedOnValue)
  lazy val listViewYarn = findLinearListView(R.id.listViewYarn)
  lazy val imageViewHappiness = findImageView(R.id.imageHappiness)
  lazy val gallery = findGallery(R.id.gallery)
  lazy val needleLayout = findView(R.id.needleLayout)
  lazy val tagsContentLayout = findViewGroup(R.id.tagsContentLayout)
  lazy val tagsLayout = findView(R.id.tagsLayout)
  lazy val needleDetails = findLabel(R.id.lblNeedleDetails)
  lazy val notesValue = findLabel(R.id.lblNotesValue)
  lazy val madeForValue = findLabel(R.id.lblMadeForValue)
  lazy val progressBar = findProgressBar(R.id.progressBar)
  lazy val progressBarLoading = findProgressBar(R.id.progressBarLoading)
  lazy val linearLayout = findView(R.id.linearLayout)
  lazy val status = findLabel(R.id.lblStatus)
  lazy val patternName = findLabel(R.id.lblPatternName)
  //lazy val imageView = findAsyncImageView(R.id.image_view)

  var refreshButton: LoaderActionBarItem = null


  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    currentId = getParams[Id].get.id
    refreshButton = addActionBarItem(Type.Refresh, R.id.action_bar_refresh).asInstanceOf[LoaderActionBarItem]
  }

  override def onHandleActionBarItemClick(item: ActionBarItem, position: Int): Boolean =
    item.getItemId match {
      case R.id.action_bar_refresh =>
        fetch(ForceNetwork)
        true
      case _ =>
        true
    }

  var pending = 0
  def onProjectLoaded(projects: List[Project], delta: Int) {
    pending += delta
    if(pending <= 0) {
      pending = 0
      refreshButton.setLoading(false)
    }

    projects.headOption.map { project =>
      setTitle(project.uiName)
      patternName.setVisibility(View.GONE)

      project.pattern_name.foreach { name =>
        patternName.setVisibility(View.VISIBLE)
        patternName.setText(name)
      }

      val imageUrls = project.photos.map { photos =>
        photos.map { photo =>
          photo.square_url.getOrElse(photo.thumbnail_url)
        }
      }.getOrElse(Nil)

      if(imageUrls.length > 0) {
        val imageAdapter = new AsyncImageViewAdapter(this, imageUrls.toArray)
        gallery.setAdapter(imageAdapter)
        gallery.setSelection(imageUrls.length / 2)
        gallery.setVisibility(View.VISIBLE)
      } else {
        gallery.setVisibility(View.GONE)
      }
      status.setText(project.status.human)

      // TODO: set default happiness to grey unknown
      project.rating.foreach { happiness =>
        val img = happiness match {
          case 0 => getResources.getDrawable(R.drawable.rating0)
          case 1 => getResources.getDrawable(R.drawable.rating1)
          case 2 => getResources.getDrawable(R.drawable.rating2)
          case 3 => getResources.getDrawable(R.drawable.rating3)
          case 4 => getResources.getDrawable(R.drawable.rating4)
        }
        imageViewHappiness.setImageDrawable(img)
      }

      progressBar.setVisibility(View.VISIBLE)
      progressBar.setMax(100)
      progressBar.setProgress(0)

      if(project.progress.isDefined) {
        progressBar.setProgress(project.progress.get)
      }

      var notes = project.notes.getOrElse("")
      if(notes.trim=="")
        notesValue.setText("(no notes)")
      else
        notesValue.setText(notes)

      var madeFor = project.made_for.getOrElse("")
      if(madeFor.trim == "")
        madeFor = "unknown"

      madeForValue.setText(madeFor)

      yarnLayout.setVisibility(View.GONE)
      val adapter: ItemAdapter = new ItemAdapter(this)
      project.packs.foreach { packs =>

        packs.foreach { pack =>
          yarnLayout.setVisibility(View.VISIBLE)
          val title = pack.yarn.flatMap { _.yarn_company_name }.getOrElse("Unknown brand")
          val subtitle = pack.yarn.flatMap { _.name }.getOrElse("Unknown yarn")
          val subtitle2 = List[Option[String]](
            pack.colorway, pack.skeins.map { x => "%s skeins".format(if(x == x.toInt) x.toInt else x) },
            pack.total_grams.map { x => "%s g".format(x) },
            pack.total_yards.map { x => "%s yards".format(x.toInt) }).flatten.filter { _ != "" }.mkString(", ")

          adapter.add(new SubtitleItem2(title, subtitle, subtitle2))
        }
      }
      listViewYarn.setAdapter(adapter)
      listViewYarn.invalidate


      lblCompletedOnValue.setText("unknown")
      project.completed.map { c => lblCompletedOnValue.setText(c) }

      lblStartedOnValue.setText("unknown")
      project.started.map { c => lblStartedOnValue.setText(c) }
      needleLayout.setVisibility(View.GONE)
      project.needle_sizes.foreach { needle_sizes =>
        val needles = needle_sizes.map { needle =>
          needle.name
        }.flatten.mkString("\n")
        if(needles != "") {
          needleLayout.setVisibility(View.VISIBLE)
          needleDetails.setText(needles)
        }
      }

      tagsLayout.setVisibility(View.GONE)
      tagsContentLayout.removeAllViews()
      project.tag_names.foreach { tag_names =>
        tag_names.foreach { tag_name =>
          tagsLayout.setVisibility(View.VISIBLE)
          val textView = new TextView(this)
          textView.setVisibility(View.VISIBLE)
          textView.setText(tag_name)
          textView.setBackgroundColor(Color.LTGRAY)
          textView.setPadding(10,2,10,2)
          textView.setTextColor(Color.BLACK)
          textView.setSingleLine(true)
          tagsContentLayout.addView(textView, new PredicateLayout.LayoutParams(10,4))
        }
      }
    }
    progressBarLoading.setVisibility(View.GONE)
    linearLayout.setVisibility(View.VISIBLE)
  }

  override def createLayout(): Int = {
    return R.layout.project_details
  }

  override def onPause() {
    super.onPause()
    progressBarLoading.setVisibility(View.VISIBLE)
    linearLayout.setVisibility(View.GONE)
  }

  override def onResume() {
    super.onResume()
    progressBarLoading.setVisibility(View.VISIBLE)
    linearLayout.setVisibility(View.GONE)
    fetch(FetchIfNeeded)
  }

  def fetch(policy: RefreshPolicy) {
    pending += 2
    refreshButton.setLoading(true)
    RavelryApi.makeProjectDetailsResource(currentId).render(policy, onProjectLoaded)
  }
}
