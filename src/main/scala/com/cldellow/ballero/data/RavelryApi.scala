package com.cldellow.ballero.data

case class UrlInput(base: String, params: Map[String, String], cacheName: String)

object RavelryApi {
  val root = "http://api.ravelry.com"
  def queueDetails(id: Int) =
    UrlInput("%s/people/{user}/queue/%s.json".format(root, id), Map(), "ravelryqueue_%s".format(id))
  def projectList = UrlInput("%s/projects/{user}/list.json".format(root), Map(), "projects")
  def queueList = UrlInput("%s/people/{user}/queue/list.json".format(root), Map(), "queue")
  def needleList = UrlInput("http://rav.cldellow.com:8080/rav/people/{user}/needles", Map(), "needles")
  def friendsList = UrlInput("%s/people/{user}/friends/list.json".format(root), Map(), "friends")
  def patternDetails(id: Int) = UrlInput("%s/patterns/%s.json".format(root, id), Map(), "pattern_%s".format(id))
}

// vim: set ts=2 sw=2 et:
