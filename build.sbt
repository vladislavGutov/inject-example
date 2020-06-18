name := "inject-blog"

version := "0.1"

scalaVersion := "2.13.2"

libraryDependencies += "org.typelevel"     %% "cats-core"            % "2.1.1"
libraryDependencies += "org.typelevel"     %% "cats-effect"          % "2.1.3"
libraryDependencies += "io.chrisdavenport" %% "log4cats-slf4j"       % "1.1.1"
libraryDependencies += "io.circe"          %% "circe-core"           % "0.13.0"
libraryDependencies += "io.circe"          %% "circe-parser"         % "0.13.0"
libraryDependencies += "io.circe"          %% "circe-generic"        % "0.13.0"
libraryDependencies += "io.circe"          %% "circe-generic-extras" % "0.13.0"

scalacOptions ++= Seq(
  "-language:higherKinds"
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)