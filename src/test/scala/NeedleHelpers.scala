package cldellow.ballero.test

import cldellow.ballero.data._

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import xml.XML

import org.json._

class NeedleHelpers extends Spec with ShouldMatchers {
  describe("needle helpers") {
    it("can deserialize Jenn's needles") {
      val url = new java.net.URL("http://www.ravelry.com/people/jkdellow/needles/details")
      val urlConn = url.openConnection
      val cookies = List("foo" -> "bar")
      //urlConn.setRequestProperty("Cookie", cookies.map { case (a, b) => "%s=%s".format(a,b) }.mkString("; "))
      urlConn.setRequestProperty("Cookie", """radvid=96deca9e41f33327706e902c6d3f17f9; version=1;__qca=P0-957073867-1318593937328; captchaid=96deca9e41f33327706e902c6d3f17f9;rsigned=BAgiLTE3MzUzNzc6ZDY5MWI4MGY3OGYxMmJmOTg0NmVjZGUwNWQ1NGQ0OGI%3D--24ede0168ed3ee7be127bc7cd4b51ce3066eaa69;last_read_markers=;ravelrys_pocketses=BAh7DDoQX2NzcmZfdG9rZW4iMVV1SHpCQ08xeFJMZ3h6bW9BanpzRkdqZ3FrSENNeHVEZE9iZE9oYUFDVjg9Og9zZXNzaW9uX2lkIiUwMGVjZTQyMmVkMGVjYjBmZWJkOGZmMjk0ZjE3NjFmOToUbGFzdF9zZWVuX2NoZWNrSXU6CVRpbWUN9uYbgNAQZIYGOh9AbWFyc2hhbF93aXRoX3V0Y19jb2VyY2lvbkYiDHVzZXJfaWRpA9F6GjoOdWFfbG9nZ2VkVCIKZmxhc2hJQzonQWN0aW9uQ29udHJvbGxlcjo6Rmxhc2g6OkZsYXNoSGFzaHsGOgtsb2NrZWRGBjoKQHVzZWR7BjsMRjoOcGFnZXZpZXdzWxAiEnRvcGljOjE4NzI5ODMiEnRvcGljOjE4NzY2NDgiEnRvcGljOjE3OTI4MTAiEnRvcGljOjEzNjU5OTgiEnRvcGljOjEzNjU5OTgiEnRvcGljOjE4NzI5ODMiEnRvcGljOjEzNjY0NTMiEnRvcGljOjEzNjU5OTgiEnRvcGljOjE1NjE1MjgiEnRvcGljOjE1NTc3NzQiEnRvcGljOjE1ODk3Mjc%3D--df5da8ee503340584b67d609120773e1e69cc8ac;timezone=-14400; __utma=67132946.2066823632.1318593929.1319400239.1319405516.49; __utmb=67132946.35.10.1319405516;__utmc=67132946; __utmz=67132946.1318593929.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)""")
      urlConn.connect
      val inputStream = urlConn.getInputStream
      val input = io.Source.fromInputStream(inputStream).getLines.reduceLeft { _ + _ }
      inputStream.close


      println(input)
      val needles = NeedleHelpers.parse(XML.loadString(input))
    }
  }
}
