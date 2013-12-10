package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.libs.json.Json

case class Project(name: String, status: String)

object Projects {

  implicit val projectFormat = Json.format[Project]

  def projectNamesToProjectMap: Map[String, Project] = {
    findAll().map(f => (f.name, f)).toMap
  }

  val simple = {
    get[String]("name") ~
      get[String]("status") map {
        case name ~ status => {
          Project(name, status)
        }
      }
  }

  def findAll(): Seq[Project] = {
    DB.withConnection { implicit connection =>
      SQL("select * from projects").as(Projects.simple *)
    }
  }

  def findAllOnGoing(): Seq[Project] = {
    DB.withConnection { implicit connection =>
      SQL("select * from projects where status='ongoing'").as(Projects.simple *)
    }
  }

  def find(name: String): Seq[Project] = {
    DB.withConnection { implicit connection =>
      SQL("select * from projects where name={name}").on('name -> name).as(Projects.simple *)
    }
  }

  def create(project: Project): Unit = {
    DB.withConnection { implicit connection =>
      SQL("insert into projects (name, status) values ({name}, {status})").on(
        'name -> project.name,
        'status -> project.status.toString()).executeInsert()
    }
  }

  def update(id: String, status: String): Unit = {
    DB.withConnection { implicit connection =>
      SQL("update projects set status={status} where name={name} ").on(
        'name -> id,
        'status -> status.toString()).executeInsert()
    }
  }

}

