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

class ProjectDetailsActivity extends ProjectishActivity {
  val TAG = "ProjectDetailsActivity"

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

    yarnRequirementsLayout.setVisibility(View.GONE)
    myYarnLayout.setVisibility(View.GONE)
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
