package cldellow.ballero.ui

import cldellow.ballero.R
import cldellow.ballero.service._

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

class MainActivity extends GDListActivity with SmartActivity {
  val TAG = "MainActivity"

  def createTextItem(stringId: Int, klass: Class[_]): TextItem = {
    val textItem: TextItem = new TextItem(getString(stringId))
    textItem.setTag(klass)
    textItem
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val adapter = new ItemAdapter(this)
    adapter.add(createTextItem(R.string.find_lys, classOf[FindLysActivity]));
    adapter.add(createTextItem(R.string.add_ravelry_account, classOf[AddRavelryAccountActivity]));
    setListAdapter(adapter);
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val textItem: TextItem = l.getAdapter().getItem(position).asInstanceOf[TextItem]

    val intent: Intent = new Intent(this, textItem.getTag.asInstanceOf[Class[_]])
    intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, textItem.text);
    startActivity(intent)
  }

  def findLysClick(view: View) {
    setContentView(R.layout.findlys)
  }

}
