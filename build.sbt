import Dependencies._

lazy val root = project.in(file("."))
  .settings(allSettings)
  .settings(noPublishSettings)
  .aggregate(core)
  .dependsOn(core)

lazy val allSettings = Seq.concat(
  buildSettings,
  baseSettings,
  publishSettings
)

lazy val buildSettings = Seq(
  organization := "com.github.tkrs",
  name := "gcs-scala",
  scalaVersion := Ver.`scala2.12`,
  crossScalaVersions := Seq(Ver.`scala2.11`, Ver.`scala2.12`)
)

lazy val baseSettings = Seq(
  libraryDependencies ++= Seq(Pkg.googleCloudStorage, Pkg.catsCore) ++ Pkg.forTest,
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-unchecked",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused-import",
    "-Xfuture",
    "-Xlint"
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

lazy val noPublishSettings = Seq(
  publish := ((): Unit),
  publishLocal := ((): Unit),
  publishArtifact := false
)

lazy val core = project.in(file("core"))
  .settings(allSettings)
  .settings(
    description := "gcs-scala core",
    moduleName := "gcs-scala-core",
    name := "core"
  )

lazy val example = project.in(file("example"))
  .settings(allSettings)
  .settings(noPublishSettings)
  .settings(
    description := "gcs-scala example",
    moduleName := "gcs-scala-example",
    name := "example"
  )
  .dependsOn(core)
