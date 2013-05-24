import sbt._
import Keys._
import play.Project._
import com.typesafe.config._

object ApplicationBuild extends Build {

  // val conf = play.api.Configuration.load(new File("."))
  val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()
  val appName         = "panel"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm
  )


  def customLessEntryPoints(base: File): PathFinder = (
    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "bootstrap.less") +++
    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "responsive.less") +++
    (base / "app" / "assets" / "stylesheets" * "*.less")
  )
  
  val main = play.Project(appName, appVersion, appDependencies).settings(
    lessEntryPoints <<= baseDirectory(customLessEntryPoints),
    coffeescriptOptions := Seq("native", "/usr/local/bin/coffee -p")
  ) dependsOn (
    RootProject(uri(conf.getString("affogato.repository")))
  )

}
