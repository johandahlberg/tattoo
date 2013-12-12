package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import models.Projects
import models.Project
import models.PiperProjects
import play.api.libs.json.Json
import models.PiperProject
import Projects.projectFormat
import models.PiperProjects.piperProjectFormat
import models.LabProjects.labProjectFormat
import models.ProjectConstants
import models.LabProjects

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("???"))
  }
  
  def registerProject(id: String) = Action {
    Projects.create(Project(id, "OnGoing"))
    Ok("Added id:" + id)
  }

  def allProjects() = Action {
    Ok(Json.toJson(Projects.findAll))
  }
  
  def allLabProjects() = Action {
    Ok(Json.toJson(LabProjects.findAll))
  }

  def onGoingProjects = Action {
    Ok(Json.toJson(Projects.findProjectWithStatus(ProjectConstants.ONGOING)))
  }

  def analysisFinishedProjects = Action {
    Ok(Json.toJson(Projects.findProjectWithStatus(ProjectConstants.ANALYSIS_FINISHED)))
  }

  def deliveredProjects = Action {
    Ok(Json.toJson(Projects.findProjectWithStatus(ProjectConstants.DELIVERED)))
  }
  
  def errorProjects = Action {
    Ok(Json.toJson(Projects.findProjectWithStatus(ProjectConstants.ERROR)))
  }

  def projectStatus(id: String) = Action {
    val projectMap = Projects.projectNamesToProjectMap
    if (projectMap.contains(id))
      Ok(Json.toJson(projectMap(id)))
    else
      BadRequest("id: " + id + " not found.")
  }

  private def updateStatusInProject(id: String, status: String) = {
    val projectMap = Projects.projectNamesToProjectMap
    if (projectMap.contains(id)) {
      Projects.update(id, status)
      val matching = Projects.find(id)
      Ok(Json.toJson(matching))
    } else
      BadRequest("id: " + id + " not found.")
  }

  def onGoingInProject(id: String) = Action {
    updateStatusInProject(id, ProjectConstants.ONGOING)
  }

  def errorInProject(id: String) = Action {
    updateStatusInProject(id, ProjectConstants.ERROR)
  }

  def analysisFinishedInProject(id: String) = Action {
    updateStatusInProject(id, ProjectConstants.ANALYSIS_FINISHED)
  }

  def deliveredProject(id: String) = Action {
    updateStatusInProject(id, ProjectConstants.DELIVERED)
  }

  def associatePiperLogWithProject(id: String, log: String) = Action {
    if (!PiperProjects.findAll.contains("id")) {
      PiperProjects.create(PiperProject(id, log))
      val matching = PiperProjects.find(id)
      Ok(Json.toJson(matching))
    } else
      BadRequest("id: " + id + " already associated with a log file.")
  }

  def updatePiperLogWithProject(id: String, log: String) = Action {
    if (PiperProjects.findAll.map(f => f.name).contains(id)) {
      PiperProjects.update(id, log)
      val matching = PiperProjects.find(id)
      Ok(Json.toJson(matching))
    } else
      BadRequest("id: " + id + " not found.")
  }

  def getPiperStatusOfProject(id: String) = Action {
    val project = PiperProjects.find(id)
    assert(project.size == 1, "project: " + project)
    val jobStatus = checkPiperJobStatus(project(0).logpath)
    if (jobStatus.isDefined)
      Ok(Json.toJson(jobStatus))
    else
      Ok(Json.toJson("unknown"))
  }

  //@TODO Move this to a better place
  implicit val jobStatusFormat = Json.format[JobStatusContainer]
  case class JobStatusContainer(pending: Int = 0, running: Int = 0, finished: Int = 0, errored: Int = 0)

  def checkPiperJobStatus(log: String): Option[JobStatusContainer] = {

    import scala.sys.process.ProcessIO
    import scala.sys.process.Process

    val cmd = """ssh biologin cat """ + log + """ | grep "QGraph.*Run""""
    val pb = Process(cmd)
    val Pattern = """.*\s(\d+)\sPend.*\s(\d+)\sRun.*\s(\d+)\sFail.*\s(\d+)\sDone.*""".r

    try {
      val result = pb.!!.split("\n").last

      result match {
        case Pattern(pending, run, failed, done) => {
          val container = JobStatusContainer(pending.toInt, run.toInt, done.toInt, failed.toInt)
          Logger.info("Found job status: " + container + " for log: " + log)
          Some(container)
        }
          
        case _ => None
      }
    } catch {
      case e: Exception => {
        Logger.error("Exception thrown when checking piper status. Message: " + e.getMessage())
        None
      }
    }
  }

}