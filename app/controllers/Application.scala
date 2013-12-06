package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import models.Projects
import models.Project
import models.OnGoing
import models.Errored
import models.AnalysisFinished
import models.Delivered
import models.PiperProjects
import models.PiperProject

object Application extends Controller {

  def index = Action {
    val piperProjectStatuses = PiperProjects.findAll.map(x => (x, checkPiperJobStatus(x.logpath)))
    Ok(views.html.index("Projects: " + Projects.findAll.toString + piperProjectStatuses.toString))
  }

  def registerProject(id: String) = Action {
    Projects.create(Project(id, OnGoing))
    Ok("Added id:" + id)
  }

  def allProjects() = Action {
    Ok(Projects.findAll.toString)
  }

  def projectStatus(id: String) = Action {
    val projectMap = Projects.projectNamesToProjectMap
    if (projectMap.contains(id))
      Ok("id: " + id + " was found, with status: " + projectMap(id).status)
    else
      BadRequest("id: " + id + " not found.")
  }

  private def updateStatusInProject(id: String, status: models.Status) = {
    val projectMap = Projects.projectNamesToProjectMap
    if (projectMap.contains(id)) {
      Projects.update(id, status)
      Ok("id: " + id + " was found, with updated status to: " + status)
    } else
      BadRequest("id: " + id + " not found.")
  }

  def errorInProject(id: String) = Action {
    updateStatusInProject(id, Errored)
  }

  def analysisFinishedInProject(id: String) = Action {
    updateStatusInProject(id, AnalysisFinished)
  }

  def deliveredProject(id: String) = Action {
    updateStatusInProject(id, Delivered)
  }

  def associatePiperLogWithProject(id: String, log: String) = Action {
    if (!PiperProjects.findAll.contains("id")) {
      PiperProjects.create(PiperProject(id, log))
      Ok("id: " + id + " was associated with log file: " + log)
    } else
      BadRequest("id: " + id + " already associated with a log file.")
  }

  def updatePiperLogWithProject(id: String, log: String) = Action {
    if (PiperProjects.findAll.map(f => f.name).contains(id)) {
      PiperProjects.update(id, log)
      Ok("id: " + id + " was associated with log file: " + log)
    } else
      BadRequest("id: " + id + " not found.")
  }

  //@TODO Move this to a better place
  sealed trait JobStatus
  case class JobStatusContainer(pending: Int = 0, running: Int = 0, finished: Int = 0, errored: Int = 0) extends JobStatus
  case object UnknownJobStatus extends JobStatus

  def checkPiperJobStatus(log: String): JobStatus = {
    
    import scala.sys.process.ProcessIO
    import scala.sys.process.Process

    val cmd = """ssh milou cat """ + log + """ | grep "QGraph.*Run""""
    val pb = Process(cmd)
    val Pattern = """.*\s(\d+)\sPend.*\s(\d+)\sRun.*\s(\d+)\sFail.*\s(\d+)\sDone.*""".r

    try {
      val result = pb.!!.split("\n").last

      result match {
        case Pattern(pending, run, failed, done) =>
          JobStatusContainer(pending.toInt, run.toInt, done.toInt, failed.toInt)
        case _ => UnknownJobStatus
      }
    } catch {
      case e: Exception => {
        Logger.error("Exception thrown when checking piper status. Message: " + e.getMessage())
        UnknownJobStatus
      }
    }
  }

}