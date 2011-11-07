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
  def stashList = UrlInput("%s/people/{user}/stash/list.json".format(root), Map(), "stash")
  def projectDetails(id: Int) = UrlInput("%s/projects/{user}/%s.json".format(root, id), Map(), "project_%s".format(id))
  def stashDetails(id: Int) = UrlInput("%s/people/{user}/stash/%s.json".format(root, id), Map(), "stash_%s".format(id))
  def patternDetails(id: Int) = UrlInput("%s/patterns/%s.json".format(root, id), Map(), "pattern_%s".format(id))

  def makeQueueDetailsResource(id: Int): NetworkResource[RavelryQueue] =
    new TransformedNetworkResource[RavelryQueueProjectWrapper, RavelryQueue](
      new SignedNetworkResource[RavelryQueueProjectWrapper](RavelryApi.queueDetails(id), false),
      { qp => List(qp.queued_project) })

  def makePatternDetailsResource(id: Int): NetworkResource[Pattern] =
    new TransformedNetworkResource[PatternWrapper, Pattern](
      new NetworkResource[PatternWrapper](RavelryApi.patternDetails(id), false),
      { qp => List(qp.pattern) })

  def makeStashDetailsResource(id: Int): NetworkResource[StashedYarn] =
    new TransformedNetworkResource[StashedYarnWrapper, StashedYarn](
      new SignedNetworkResource[StashedYarnWrapper](RavelryApi.stashDetails(id), false),
      { qp => List(qp.stash) })


  def makeProjectDetailsResource(id: Int): NetworkResource[Project] =
    new TransformedNetworkResource[ProjectWrapper, Project](
      new SignedNetworkResource[ProjectWrapper](RavelryApi.projectDetails(id), false),
      { qp => List(qp.project) })


}

// vim: set ts=2 sw=2 et:
