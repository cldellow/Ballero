package cldellow.ballero

import android.app.Activity
import android.view.View
import android.widget._

trait SmartActivity { this: Activity =>
  def find[T](i: Int): T =
    findViewById(i).asInstanceOf[T]

  def findView(i: Int): View = find(i)
  def findTextView(i: Int): TextView = find(i)
  def findProgressBar(i: Int): ProgressBar = find(i)
  def findButton(i: Int): Button = find(i)

  def toast(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
  }
}


// vim: set ts=2 sw=2 et:
