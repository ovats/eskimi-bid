name := "eskimi-bid"

version := "0.1"

scalaVersion := "2.13.6"

//TODO I had to comment this line, otherwise project will not work, can't build it.
//idePackagePrefix := Some("com.eskimi.samplebid")

lazy val akkaVersion                    = "2.6.5"
lazy val akkaHttpVersion                = "10.2.0"
lazy val akkaHttpJsonSerializersVersion = "1.34.0"
lazy val pureConfigVersion              = "0.17.1"
lazy val logbackVersion                 = "1.2.10"
lazy val scalaTestVersion               = "3.2.9"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed"     % akkaVersion,
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-jackson"    % akkaHttpJsonSerializersVersion,
  // For managing conf files
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
  // Log dependencies
  "ch.qos.logback" % "logback-classic" % logbackVersion % Runtime,
  // Unit test dependencies
  "org.scalatest"     %% "scalatest"                % scalaTestVersion,
  "org.scalatest"     %% "scalatest-flatspec"       % scalaTestVersion % Test,
  "org.scalatest"     %% "scalatest-shouldmatchers" % scalaTestVersion % Test,
  "com.typesafe.akka" %% "akka-testkit"             % akkaVersion      % Test,
  "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion  % Test,
)
