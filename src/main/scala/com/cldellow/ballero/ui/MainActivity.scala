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
import android.view._
import android.widget._
import greendroid.app._
import greendroid.widget._
import greendroid.widget.item._
import greendroid.widget.itemview._

class MainActivity extends GDListActivity with NavigableListActivity with SmartActivity {
  val TAG = "MainActivity"

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    registerForContextMenu(getListView())

    setTitle("Ballero")
    rebuildOptions

  }

  override def onResume() {
    super.onResume
    if(Data.newUser && Data.currentUser.isDefined) {
      Data.newUser = false
      val intent = new Intent(this, classOf[RavellerHomeActivity])
      intent.putExtra(ActionBarActivity.GD_ACTION_BAR_TITLE, Data.currentUser.get.name)
      startActivity(intent)
    }
  }

  def rebuildOptions {
    val adapter = new ItemAdapter(this)
    adapter.add(new TextItem("find a local yarn store").goesTo[FindLysActivity])
    Data.users.sortBy { _.name }.foreach { user => 
      adapter.add(new TextItem(user.name).goesToWithData[RavellerHomeActivity, User](user))
    }
    adapter.add(new TextItem("link a ravelry account").goesTo[AddRavelryAccountActivity])
    Data.users.foreach { user => info("got user: %s".format(user)) }
    setListAdapter(adapter);
  }

  var longClickUser: Option[String] = None
  override def onCreateContextMenu (menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
    val infoItem = menuInfo.asInstanceOf[AdapterView.AdapterContextMenuInfo]
    val text = infoItem.targetView.asInstanceOf[TextItemView].getText.toString
    if(text == "find a local yarn store" || text == "link a ravelry account")
      return

    longClickUser = Some(text)
    menu.setHeaderTitle("%s options".format(text))
    menu.add(0, 0, 0, "Remove account")
  }

  override def onContextItemSelected(item: MenuItem): Boolean = {
    if(!longClickUser.isDefined)
      return false

    item.getItemId() match {
      case 0 =>
        toast("Deleted data for %s".format(longClickUser.get))
        Data.deleteUser(longClickUser.get)
        longClickUser = None
        rebuildOptions
        true
      case _ => false
    }
  }
}
