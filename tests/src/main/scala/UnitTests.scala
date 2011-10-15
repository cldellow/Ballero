package cldellow.ballero.tests

import junit.framework.Assert._
import _root_.android.test.AndroidTestCase

class UnitTests extends AndroidTestCase {
  def testPackageIsCorrect {
    assertEquals("cldellow.ballero", getContext.getPackageName)
  }
}