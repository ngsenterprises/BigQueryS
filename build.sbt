name := "BigQueryS"

version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies ++= {
  val akkaV       = "2.4.16"//"2.4.3"
  val scalaTestV  = "3.0.0"//"2.2.6"
  Seq(
    "org.apache.commons" % "commons-io" % "1.3.2",
    "com.typesafe.akka" % "akka-actor_2.12" % akkaV,
    "com.google.apis"         %  "google-api-services-bigquery" % "v2-rev355-1.22.0",
    "com.google.oauth-client" %  "google-oauth-client"          % "1.22.0",
    "com.google.oauth-client" %  "google-oauth-client-jetty"    % "1.22.0",
    "com.google.http-client"  %  "google-http-client-jackson2"  % "1.22.0",
    "ch.qos.logback"          %  "logback-classic"              % "1.2.3"   % Test,
    "org.scalatest"           %% "scalatest"                    % "3.0.4"   % Test,
    "com.github.seratch"      % "bigquery4s_2.12"               % "0.6",
    "com.google.cloud"        % "google-cloud-bigquery"         % "0.26.0-beta",
    "com.typesafe.akka"       % "akka-testkit_2.12"             % "2.5.6" % "test",
    "com.typesafe"            % "config"                        % "1.3.2"
  )
}