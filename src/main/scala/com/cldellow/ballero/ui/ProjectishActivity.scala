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

abstract class ProjectishActivity extends GDActivity with SmartActivity {
  lazy val yarnLayout = findView(R.id.yarnLayout)
  lazy val myYarnLayout = findView(R.id.myYarnLayout)
  lazy val yarnRequirementsLayout = findView(R.id.yarnRequirementsLayout)
  lazy val lblYarns = findLabel(R.id.lblYarns)
  lazy val lblYarnSize = findLabel(R.id.lblYarnSize)
  lazy val lblYarnYardage = findLabel(R.id.lblYarnYardage)
  lazy val lblCompletedOn = findLabel(R.id.lblCompletedOn)
  lazy val lblCompletedOnValue = findLabel(R.id.lblCompletedOnValue)
  lazy val lblStartedOnValue = findLabel(R.id.lblStartedOnValue)
  lazy val lblStartedOn = findLabel(R.id.lblStartedOn)
  lazy val listViewYarn = findLinearListView(R.id.listViewYarn)
  lazy val listViewMyYarn = findLinearListView(R.id.listViewMyYarn)
  lazy val lblHappiness = findLabel(R.id.lblHappiness)
  lazy val imageViewHappiness = findImageView(R.id.imageHappiness)
  lazy val gallery = findGallery(R.id.gallery)
  lazy val needleLayout = findView(R.id.needleLayout)
  lazy val tagsContentLayout = findViewGroup(R.id.tagsContentLayout)
  lazy val tagsLayout = findView(R.id.tagsLayout)
  lazy val needleDetails = findLabel(R.id.lblNeedleDetails)
  lazy val notesValue = findLabel(R.id.lblNotesValue)
  lazy val lblMadeFor = findLabel(R.id.lblMadeFor)
  lazy val madeForValue = findLabel(R.id.lblMadeForValue)
  lazy val progressBar = findProgressBar(R.id.progressBar)
  lazy val progressBarLoading = findProgressBar(R.id.progressBarLoading)
  lazy val linearLayout = findView(R.id.linearLayout)
  lazy val status = findLabel(R.id.lblStatus)
  lazy val patternName = findLabel(R.id.lblPatternName)
  //lazy val imageView = findAsyncImageView(R.id.image_view)

  var refreshButton: LoaderActionBarItem = null
  var currentId: Int = 0

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    currentId = getParams[Id].get.id
    refreshButton = addActionBarItem(Type.Refresh, R.id.action_bar_refresh).asInstanceOf[LoaderActionBarItem]
  }


  override def createLayout(): Int = {
    return R.layout.project_details
  }


  override def onPause() {
    super.onPause()
    progressBarLoading.setVisibility(View.VISIBLE)
    linearLayout.setVisibility(View.GONE)
  }

}

// vim: set ts=2 sw=2 et:
