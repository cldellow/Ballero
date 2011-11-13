package com.cldellow.ballero.ui

import com.cldellow.ballero.R
import greendroid.widget.ActionBarItem.Type
import com.cldellow.ballero.data._
import android.graphics._
import scala.collection.JavaConversions._
import android.app._
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
import android.widget.AdapterView._

abstract class ProjectishActivity extends GDActivity with SmartActivity {
  def isProject: Boolean
  def patternId: Option[Int]
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
  lazy val linearLayout = findView(R.id.linearLayout)
  lazy val status = findLabel(R.id.lblStatus)
  lazy val patternName = findLabel(R.id.lblPatternName)
  lazy val btnEditNotes = findButton(R.id.btnEditNotes)
  lazy val btnTakePhoto = findButton(R.id.btnTakePhoto)
  //lazy val imageView = findAsyncImageView(R.id.image_view)

  var currentId: Int = 0

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    ensureLayout()
    currentId = getParams[Id].get.id
  }

  override def onResume() {
    super.onResume()
    val outer = this
    gallery.setOnItemClickListener(new OnItemClickListener() {
      override def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {
        val intent: Intent = new Intent(outer, classOf[PhotoViewerActivity])
        intent.putExtra("com.cldellow.params",
          Parser.serialize(PhotoIntent(
            if(isProject)
              currentId
            else
              patternId.get
            ,
            position,
            if(isProject)
              "project"
            else
              "pattern")))
        startActivity(intent)
      }
    })
  }


  override def createLayout(): Int = {
    return R.layout.project_details
  }


  def startedOnClick(v: View) {}
  def completedOnClick(v: View) {}
  def madeForClick(v: View) {}
  def statusClick(v: View) {}
  def btnTakePhotoClick(v: View) {}

  override def onPause() {
    super.onPause()
    linearLayout.setVisibility(View.GONE)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val inflater: MenuInflater = getMenuInflater()
    inflater.inflate(R.menu.project_details_menu, menu)
    true
  }

  def refreshAll(policy: RefreshPolicy) {
    doFetch(policy)
    showDialog(PROGRESS_DIALOG)
  }

  def dismissProgressDialog() {
    dismissDialog(PROGRESS_DIALOG)
    progressDialog = null
  }

  /** 10, don't clash with IDs for editing in project details */
  val PROGRESS_DIALOG = 10
  var progressDialog: ProgressDialog = null
  override def onCreateDialog(id: Int): Dialog = {
    id match {
      case PROGRESS_DIALOG =>
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Downloading details...")
        progressDialog.setIndeterminate(true)
        progressDialog
      case _ => null
    }
  }


  def doFetch(policy: RefreshPolicy): Unit

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.refresh => refreshAll(ForceNetwork)
      case R.id.notebook =>
        val intent = new Intent(this, classOf[RavellerHomeActivity])
        startActivity(intent)
      case R.id.projects =>
        val intent = new Intent(this, classOf[ProjectsActivity])
        startActivity(intent)
      case _ =>
    }
    true
  }

}

// vim: set ts=2 sw=2 et:
