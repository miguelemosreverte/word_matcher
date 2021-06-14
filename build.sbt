lazy val `word_matcher` =
  (project in file("."))
    .aggregate(`domain`, `application`, `infrastructure`)
    .settings(
      assembly / mainClass := Some("leapfin.lemos.word_matcher.Main"),
      assembly / assemblyJarName := "word_matcher.jar"
    )
    .dependsOn(`application`)

lazy val `domain` = project in file(".") / "modules" / "domain"
lazy val `application` =
  (project in file(
    "."
  ) / "modules" / "application" dependsOn (`domain` % "compile->compile;test->test", `infrastructure`))

lazy val `infrastructure` = project in file(".") / "modules" / "infrastructure"

ThisBuild / Test / testOptions += Tests.Argument("-oD")
