package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import play.api.libs.json.Json
import play.api.Logger

case class Library(name: String, status: String)
case class Sample(name: String, libraries: Seq[Library])
case class LabProject(name: String, status: String, samples: Seq[Sample])

object LabProjects {

  implicit val libraryFormat = Json.format[Library]
  implicit val sampleFormat = Json.format[Sample]
  implicit val labProjectFormat = Json.format[LabProject]

  def findAll() = {

    DB.withConnection { implicit connection =>

      val projectSQLResults = SQL("""
          	SELECT labprojects.name, labprojects.status, samples.name, libraries.name, libraries.status
          	FROM labprojects
              JOIN samples
          		ON labprojects.name = samples.project
              JOIN libraries
          		ON samples.name = libraries.sample
        	""").apply()

      def getLibraries(stream: Stream[SqlRow]): Seq[Library] = {
        stream.map(row => {
          val library = row[String]("libraries.name")
          val libraryStatus = row[String]("libraries.status")
          Library(library, libraryStatus)
        })
      }

      def getSamples(stream: Stream[SqlRow]): Seq[Sample] = {

        val samples = stream.groupBy(row => {
          val sample = row[String]("samples.name")
          sample
        })

        val result = samples.map {
          case (s: String, k: Stream[SqlRow]) => Sample(s, getLibraries(k).toList)
        }

        result.toSeq
      }

      def getProject(stream: Stream[SqlRow]): Seq[LabProject] = {

        val projects = stream.groupBy(row => {
          val projectName = row[String]("labprojects.name")
          val projectStatus = row[String]("labprojects.status")
          (projectName, projectStatus)
        })

        val result = projects.map {
          case ((projectName: String, projectStatus: String), k: Stream[SqlRow]) => LabProject(projectName, projectStatus, getSamples(k))
        }

        result.toSeq
      }

      getProject(projectSQLResults)
    }
  }
}

