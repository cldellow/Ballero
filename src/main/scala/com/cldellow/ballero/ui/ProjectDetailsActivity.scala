package com.cldellow.ballero.ui

import com.cldellow.ballero.R
import com.cldellow.ballero.data._
import android.graphics._
import scala.collection.JavaConversions._
import android.app.Activity
import android.content._
import android.location._
import android.os.Bundle
import android.util.Log
import android.view._
import android.widget.TextView
import greendroid.app._
import greendroid.widget._

class ProjectDetailsActivity extends GDActivity with SmartActivity {
  val TAG = "ProjectDetailsActivity"

  var currentId: Int = 0

  lazy val layoutMakeFor = findView(R.id.layoutMakeFor)
  lazy val yarnLayout = findView(R.id.yarnLayout)
  lazy val needleDetails = findLabel(R.id.lblNeedleDetails)
  lazy val yarnName = findLabel(R.id.lblYarnName)
  lazy val yarnAmount = findLabel(R.id.lblYarnAmount)
  lazy val yarnColor = findLabel(R.id.lblYarnColor)
  lazy val notesValue = findLabel(R.id.lblNotesValue)
  lazy val madeForValue = findLabel(R.id.lblMadeForValue)
  lazy val progressBar = findProgressBar(R.id.progressBar)
  lazy val progressBarLoading = findProgressBar(R.id.progressBarLoading)
  lazy val linearLayout = findView(R.id.linearLayout)
  lazy val status = findLabel(R.id.lblStatus)
  lazy val patternName = findLabel(R.id.lblPatternName)
  lazy val craftName = findLabel(R.id.lblCraftName)
  lazy val imageView = findAsyncImageView(R.id.image_view)

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    currentId = getParams[Id].get.id


  }

  def onProjectLoaded(projects: List[Project], pending: Boolean) {
    projects.headOption.map { project =>
      setTitle(project.uiName)
      patternName.setVisibility(View.GONE)

      project.pattern_name.foreach { name =>
        patternName.setVisibility(View.VISIBLE)
        patternName.setText(name)
      }

      imageView.setVisibility(View.GONE)
      project.photos.foreach { photos =>
        photos.headOption.foreach { photo =>
          photo.square_url.foreach { url =>
            imageView.setVisibility(View.VISIBLE)
            imageView.setUrl(url)
          }
        }
      }
      status.setText(project.status.human)
      project.craft_name.foreach { name => craftName.setText(name) }

      if(project.progress.isDefined) {
        progressBar.setVisibility(View.VISIBLE)
        progressBar.setMax(100)
        progressBar.setProgress(project.progress.get)
      } else {
        progressBar.setVisibility(View.GONE)
      }

      var notes = project.notes.getOrElse("")
      if(notes.trim=="")
        notesValue.setText("(no notes)")
      else
        notesValue.setText(notes)

      var madeFor = project.made_for.getOrElse("")
      if(madeFor.trim == "")
        madeFor = "(no one)"

      if(madeFor == "(no one)") {
        layoutMakeFor.setVisibility(View.GONE)
      }
      else {
        layoutMakeFor.setVisibility(View.VISIBLE)
        madeForValue.setText(madeFor)
      }

      yarnLayout.setVisibility(View.GONE)
      project.packs.foreach { packs =>
        packs.foreach { pack =>
          yarnLayout.setVisibility(View.VISIBLE)
          pack.yarn_name.foreach { name => yarnName.setText(name) }
          pack.colorway.foreach { colorway => yarnColor.setText(colorway) }
          pack.skeins.foreach { skeins =>
            yarnAmount.setText("%s skeins".format(skeins))
            pack.total_grams.foreach { grams =>
              pack.total_yards.foreach { yards =>
                yarnAmount.setText("%s skeins (%s g / %s yards)".format(skeins, grams, yards))
              }
            }
          }
        }
      }

      project.needle_sizes.foreach { needle_sizes =>
        val needles = needle_sizes.map { needle =>
          needle.name
        }.flatten.mkString("\n")
        needleDetails.setText(needles)
      }
    }
    progressBarLoading.setVisibility(View.GONE)
    linearLayout.setVisibility(View.VISIBLE)
  }

  override def createLayout(): Int = {
    return R.layout.project_details
  }

  override def onResume() {
    super.onResume()
    progressBarLoading.setVisibility(View.VISIBLE)
    linearLayout.setVisibility(View.GONE)
    RavelryApi.makeProjectDetailsResource(currentId).render(FetchIfNeeded, onProjectLoaded)
  }
}
