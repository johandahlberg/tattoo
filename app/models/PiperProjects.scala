package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class PiperProject(name: String, logpath: String)

object PiperProjects {

  val simple = {
    get[String]("name") ~
      get[String]("logpath") map {
        case name ~ logpath => {
          PiperProject(name, logpath)
        }
      }
  }

  def findAll(): Seq[PiperProject] = {
    DB.withConnection { implicit connection =>
      SQL("select * from piperprojects").as(PiperProjects.simple *)
    }
  }

  def create(project: PiperProject): Unit = {
    DB.withConnection { implicit connection =>
      SQL("insert into piperprojects (name, logpath) values ({name}, {logpath})").on(
        'name -> project.name,
        'logpath -> project.logpath).executeInsert()
    }
  }

  def update(id: String, logpath: String): Unit = {
    DB.withConnection { implicit connection =>
      SQL("update piperprojects set logpath={logpath} where name={name} ").on(
        'name -> id,
        'logpath -> logpath).executeInsert()
    }
  }
}

