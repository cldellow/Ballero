package cldellow.ballero.ui

import android.content.Intent
import android.net.Uri
import greendroid.app.GDApplication

class BalleroApplication extends GDApplication {
    override def getHomeActivityClass: Class[_] = classOf[MainActivity]

    override def getMainApplicationIntent: Intent = {
      new Intent(Intent.ACTION_VIEW, Uri.parse("http://github.com/cldellow/Ballero"))
    }
}
