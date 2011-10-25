package com.cldellow.ballero.ui

import com.cldellow.ballero.R
import com.cldellow.ballero.service._
import com.cldellow.ballero.data._

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

  private def createTextItem(stringId: Int, klass: Class[_]): TextItem = createTextItem(getString(stringId), klass)

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val adapter = new ItemAdapter(this)

    adapter.add(createTextItem(R.string.find_lys, classOf[FindLysActivity]));

    Data.users.sortBy { _.name }.foreach { user => 
      adapter.add(createTextItem(user.name, classOf[RavellerHomeActivity]))
    }

    adapter.add(createTextItem(R.string.add_ravelry_account, classOf[AddRavelryAccountActivity]));

    Data.users.foreach { user => info("got user: %s".format(user)) }
    setListAdapter(adapter);

  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val textItem: TextItem = l.getAdapter().getItem(position).asInstanceOf[TextItem]

    //TODO: make this less of a hack
    val klazz = textItem.getTag.asInstanceOf[Class[_]]
    if(klazz == classOf[RavellerHomeActivity])
      Data.currentUser = Some(Data.users.find { _.name == textItem.text }.get)

    val intent: Intent = new Intent(this, textItem.getTag.asInstanceOf[Class[_]])
    intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, textItem.text);
    intent.putExtra(UiConstants.ExtraText, textItem.text);
    startActivity(intent)
  }

  def findLysClick(view: View) {
    setContentView(R.layout.findlys)
  }

}
