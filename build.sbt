lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.github.tkrs",
      scalaVersion := "2.12.2",
      version      := "0.0.1-SNAPSHOT",
      crossScalaVersions := Seq("2.11.11", "2.12.2")
    )),
    publishSettings,
    name := "gcs-scala",
    libraryDependencies ++= Seq(
      "com.google.cloud" % "google-cloud-storage" % "1.2.3",
      "org.typelevel" %% "cats" % "0.9.0",
      "org.scalatest" %% "scalatest" % "3.0.3" % "test"
    ),
    scalacOptions := Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-unchecked",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-Xfuture",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused-import",
      "-Ydelambdafy:method"
    ),
    scalacOptions in (Compile, console) ~= (_ filterNot (_ == "-Ywarn-unused-import"))
  )

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  homepage := Some(url("https://github.com/tkrs/gcs-scala")),
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/tkrs/gcs-scala"),
      "scm:git:git@github.com:tkrs/gcs-scala.git"
    )
  ),
  pomExtra :=
    <developers>
      <developer>
        <id>tkrs</id>
        <name>Takeru Sato</name>
        <url>https://github.com/tkrs</url>
      </developer>
    </developers>,
  pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toCharArray)
) ++ credentialSettings

lazy val credentialSettings = Seq(
  credentials ++= (for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
)

// lazy val noPublishSettings = Seq(
//   publish := (),
//   publishLocal := (),
//   publishArtifact := false
// )
