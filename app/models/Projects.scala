package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

sealed trait Status
case object OnGoing extends Status { override def toString = "ongoing" }
case object Errored extends Status { override def toString = "error" }
case object AnalysisFinished extends Status { override def toString = "analysisfinished" }
case object Delivered extends Status { override def toString = "delivered" }

case class Project(name: String, status: Status)

object Projects {

  def projectNamesToProjectMap: Map[String, Project] = {
    findAll().map(f => (f.name, f)).toMap
  }

  def string2Status(s: String): Status = {
    s match {
      case "ongoing" => OnGoing
      case "error" => Errored
      case "analysisfinished" => AnalysisFinished
      case "delivered" => Delivered
    }
  }

  val simple = {
    get[String]("name") ~
      get[String]("status") map {
        case name ~ status => {
          Project(name, string2Status(status))
        }
      }
  }

  def findAll(): Seq[Project] = {
    DB.withConnection { implicit connection =>
      SQL("select * from projects").as(Projects.simple *)
    }
  }

  def create(project: Project): Unit = {
    DB.withConnection { implicit connection =>
      SQL("insert into projects (name, status) values ({name}, {status})").on(
        'name -> project.name,
        'status -> project.status.toString()).executeInsert()
    }
  }

  def update(id: String, status: Status): Unit = {
    DB.withConnection { implicit connection =>
      SQL("update projects set status={status} where name={name} ").on(
        'name -> id,
        'status -> status.toString()).executeInsert()
    }
  }

}

