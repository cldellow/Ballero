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
import android.widget.AdapterView._

class PhotoViewerActivity extends GDActivity with SmartActivity {
  val TAG = "PhotoViewerActivity"
  lazy val imageView = findAsyncImageView(R.id.image_view)
  lazy val gallery = findGallery(R.id.gallery)

  var photoIntent: PhotoIntent = null
  var imageUrls: List[String] = Nil

  var numPendings = 0
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    photoIntent = getParams[PhotoIntent].get

    if(photoIntent.name == "project") {
      RavelryApi.makeProjectDetailsResource(photoIntent.id).render(FetchIfNeeded, onProjectChanged)
    } else if(photoIntent.name == "pattern") {
      RavelryApi.makePatternDetailsResource(photoIntent.id).render(FetchIfNeeded, onPatternChanged)
    }
  }

  def updatePendings(delta: Int) {
    numPendings += delta
  }

  def onProjectChanged(projects: List[Project], delta: Int) {
    updatePendings(delta)

    setTitle(projects.head.uiName)
    setImages(projects.headOption.flatMap { _.photos }.getOrElse(Nil).take(3))
  }

  def onPatternChanged(patterns: List[Pattern], delta: Int) {
    updatePendings(delta)
    setTitle(patterns.head.name)
    setImages(patterns.headOption.flatMap { _.photos }.getOrElse(Nil).take(3))
  }

  def setImages(photos: List[Photo]) {
    imageUrls = photos.map { p => p.medium_url }
    val smallUrls = photos.map { p => p.square_url.getOrElse(p.thumbnail_url) }
    val imageAdapter = new AsyncImageViewAdapter(this, smallUrls.toArray)
    info("small: %s".format(smallUrls))
    info("big: %s".format(imageUrls))
    gallery.setAdapter(imageAdapter)
    gallery.setSelection(photoIntent.index)
    setImage(photoIntent.index)

    gallery.setOnItemClickListener(new OnItemClickListener() {
      override def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {
        setImage(position)
      }
    })
  }

  def setImage(index: Int) {
    imageView.setUrl(imageUrls(index))
  }


  override def onResume() {
    super.onResume()
    gallery.setOnItemClickListener(new OnItemClickListener() {
      override def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {
      }
    })
  }

  override def createLayout(): Int = {
    return R.layout.photo_viewer_activity
  }
}

// vim: set ts=2 sw=2 et:
