name := "inject-blog"

version := "0.1"

scalaVersion := "2.13.2"

libraryDependencies += "org.typelevel"     %% "cats-core"      % "2.1.1"
libraryDependencies += "org.typelevel"     %% "cats-effect"    % "2.1.3"
libraryDependencies += "io.chrisdavenport" %% "log4cats-slf4j" % "1.1.1"

scalacOptions ++= Seq(
  "-language:higherKinds"
)