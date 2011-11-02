/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.cldellow.ballero.ui

import android.content._
import android.graphics._
import android.text._
import android.view._
import android.widget.FrameLayout.LayoutParams
import android.widget._
import android.util._
import android.net.Uri

import com.google.android.maps.OverlayItem

import com.cldellow.ballero.R
/**
 * A view representing a MapView marker information balloon.
 * <p>
 * This class has a number of Android resource dependencies:
 * <ul>
 * <li>drawable/balloon_overlay_bg_selector.xml</li>
 * <li>drawable/balloon_overlay_close.png</li>
 * <li>drawable/balloon_overlay_focused.9.png</li>
 * <li>drawable/balloon_overlay_unfocused.9.png</li>
 * <li>layout/balloon_map_overlay.xml</li>
 * </ul>
 * </p>
 * 
 * @author Jeff Gilfelt
 *
 */
class BalloonOverlayView(context: Context, balloonBottomOffset: Int)
extends FrameLayout(context) {
  private val WRAP_CONTENT = -2

  // phone number
  // address
  // 


  setPadding(10, 0, 10, balloonBottomOffset);
  private val layout = new LinearLayout(context);
  private var item: Option[BalloonOverlayItem] = None
  layout.setVisibility(View.VISIBLE)

  private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
  private val v: View = inflater.inflate(R.layout.balloon_overlay, layout)

  private val title = v.findViewById(R.id.balloon_item_title).asInstanceOf[TextView]
  private val snippet = v.findViewById(R.id.balloon_item_snippet).asInstanceOf[TextView]
  private val twitter = v.findViewById(R.id.balloon_item_twitter).asInstanceOf[TextView]
  private val phone = v.findViewById(R.id.balloon_item_phone).asInstanceOf[TextView]


  private val close = v.findViewById(R.id.close_img_button).asInstanceOf[ImageView]
  close.setOnClickListener(new View.OnClickListener() {
    def onClick(v: View) {
      layout.setVisibility(View.GONE)
    }
  })

  val params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
  params.gravity = Gravity.NO_GRAVITY;

  addView(layout, params);

  def urlClick(v: View) {
    item foreach { item =>
      item.shop.url.foreach { url =>
        if(url != "") {
          context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
      }
    }
  }

  def cleanTwitter(twit: Option[String]): Option[String] = {
    val clean = twit.map { twit =>
      val indexOf = twit.lastIndexOf("/")

      if(indexOf >= 0)
        twit.substring(indexOf + 1)
      else
        twit
    }

    if(clean.isDefined && clean.get.trim != "")
      Some(clean.get.trim)
    else
      None
  }

  def twitterClick(v: View) {
    item map { item =>
      cleanTwitter(item.shop.twitter_id).map { twit =>
        new Intent(Intent.ACTION_VIEW,
          Uri.parse("https://mobile.twitter.com/" + twit))
      } map { context.startActivity(_) }
    }
  }


  def phoneClick(v: View) {
    item map { item =>
      item.shop.phone.map { phone =>
        new Intent(Intent.ACTION_CALL, Uri.parse("tel:%s".format(
          phone.replace("(", "")
            .replace(")", "")
            .replace("-", ""))))
        } map { context.startActivity(_) }
    }
  }
  /**
   * Sets the view data from a given overlay item.
   * 
   * @param item - The overlay item containing the relevant view data 
   * (title and snippet). 
   */
  def setData(item: BalloonOverlayItem) {
    this.item = Some(item)
    layout.setVisibility(View.VISIBLE)
    if (item.getTitle() != null) {
      title.setVisibility(View.VISIBLE)
      val hasUrl = item.shop.url != ""
      if(hasUrl) {
        title.setTextColor(Color.BLUE)
        title.setText(Html.fromHtml("<span><u>%s</u></span>".format(item.getTitle())))
      } else {
        title.setTextColor(Color.BLACK)
        title.setText(Html.fromHtml("<span>%s</span>".format(item.getTitle())))
      }
    } else {
      title.setVisibility(View.GONE)
    }

    if(item.shop.phone.isDefined) {
      phone.setVisibility(View.VISIBLE)
      phone.setText(item.shop.phone.get)
    }
    else
      phone.setVisibility(View.GONE)

    if (item.getSnippet() != null) {
      snippet.setVisibility(View.VISIBLE)
      snippet.setText(item.getSnippet())
    } else {
      snippet.setVisibility(View.GONE)
    }

    val twit = cleanTwitter(item.shop.twitter_id)
    if (twit.isDefined) {
      twitter.setVisibility(View.VISIBLE)
      twitter.setText(Html.fromHtml("<span><u>@%s</u></span>".format(twit.get)))
    } else {
      twitter.setVisibility(View.GONE)
    }
  }

}
