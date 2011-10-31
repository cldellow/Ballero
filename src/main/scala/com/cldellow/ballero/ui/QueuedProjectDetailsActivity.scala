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

class QueuedProjectDetailsActivity extends GDActivity with SmartActivity {
  val TAG = "QueuedProjectDetailsActivity"
  var currentId: Int = 0

  lazy val notesValue = findLabel(R.id.lblNotesValue)
  lazy val makeForValue = findLabel(R.id.lblMakeForValue)
  lazy val patternName = findLabel(R.id.lblPatternName)
  lazy val imageView = findAsyncImageView(R.id.image_view)
  lazy val layoutMakeFor = findView(R.id.layoutMakeFor)

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setTitle("queued project details")
    currentId = getParams[Id].get.id

  }

  override def createLayout(): Int = {
      return R.layout.queued_project_details
  }


  override def onResume() {
    super.onResume()

    val ravelryQueue = RavelryApi.makeQueueDetailsResource(currentId).get.headOption
    imageView.setVisibility(View.GONE)
    ravelryQueue map { q =>

      var makeFor = q.make_for
      if(makeFor.trim == "") makeFor = "(no one)"

      if(makeFor == "(no one)") {
        layoutMakeFor.setVisibility(View.GONE)
      } else {
        makeForValue.setText(makeFor)
        layoutMakeFor.setVisibility(View.VISIBLE)
      }

      q.pattern_name.map { pn => patternName.setText(pn) }


      q.pattern_id.foreach { id =>
        val patternDetails = RavelryApi.makePatternDetailsResource(id).get.headOption
        patternDetails.foreach { pattern =>
          pattern.photos.getOrElse(Nil).headOption.foreach { photo =>
            info("PHOTO: " + photo.toString)
            imageView.setVisibility(View.VISIBLE)
            imageView.setUrl(photo.small_url)
          }
        }
      }
    }

    Data.currentUser.get.queue.get.filter { _.id == currentId }.map { q =>
      var text = q.notes.getOrElse("")
      if(text.trim == "") text = "(no notes)"
      notesValue.setText(text)
    }
  }

}
