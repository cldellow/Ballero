package cldellow.ballero.ui

import cldellow.ballero.R
import cldellow.ballero.service._
import cldellow.ballero.data._
import scala.xml.XML

import scala.collection.JavaConversions._
import android.app.Activity
import android.content._
import android.location._
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget._
import greendroid.app._
import greendroid.widget._
import greendroid.widget.item._

class RavellerHomeActivity extends GDListActivity with SmartActivity {
  val TAG = "RavellerHomeActivity"

  var adapter: ItemAdapter = null

  var needlesItem: Item = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    adapter = new ItemAdapter(this)

    needlesItem = new ProgressItem("needles", true)
    adapter.add(needlesItem)
    /*
    restServiceConnection.request(
      RestRequest("http://rav.cldellow.com:8080/rav/people/%s/needles.json".format(Data.currentUser.get.name))
    )(onNeedlesDownloaded)
    */
    setListAdapter(adapter)

  }

  override def onResume {
    super.onResume
    Data.currentUser.get.needles.render(true, onNeedlesChanged)
  }

  private def onNeedlesChanged(needles: List[Needle], pending: Boolean) {
    /*
    needles.groupBy { _.kind }.foreach { case (kind, needles) =>
      adapter.add(new SeparatorItem("%s %s".format(needles.length, kind)))
      needles.foreach { needle =>
        adapter.add(new TextItem("%s (%s)".format(needle.gaugeMetric, needle.comment)))
      }
    }
    setListAdapter(adapter)
    */
    adapter.remove(needlesItem)

    val subtitle = if(needles.isEmpty) "no needles" else needles.groupBy { _.kind }
      .map { case(kind, needles) => "%s %s".format(needles.length, kind) }
      .mkString(", ")

    println("pending: %s".format(pending))
    needlesItem = new SubtitleItem("needles", subtitle, pending)
    adapter.add(needlesItem)
    onContentChanged()
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val textItem: TextItem = l.getAdapter().getItem(position).asInstanceOf[TextItem]

    val intent: Intent = new Intent(this, textItem.getTag.asInstanceOf[Class[_]])
    intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, textItem.text);
    intent.putExtra(UiConstants.ExtraText, textItem.text);
    startActivity(intent)
  }

}
