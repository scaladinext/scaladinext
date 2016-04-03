// publish related
sonatypeProfileName := "com.github.scaladinext"

publishMavenStyle := true

publishArtifact in Test := false

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/scaladinext/scaladinext</url>
    <licenses>
      <license>
        <name>BSD-style</name>
        <url>http://www.opensource.org/licenses/bsd-license.php</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>https://github.com/scaladinext/scaladinext.git</url>
      <connection>https://github.com/scaladinext/scaladinext.git</connection>
    </scm>
    <developers>
      <developer>
        <id>scaladinext</id>
        <name>scaladinext</name>
        <url>https://github.com/scaladinext</url>
      </developer>
    </developers>)