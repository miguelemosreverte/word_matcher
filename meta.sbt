ThisBuild / organization := "evolution"
ThisBuild / organizationName := "Evolution"
ThisBuild / organizationHomepage := Some(url("http://evolution.com"))

ThisBuild / homepage := None
ThisBuild / startYear := Some(2021)

ThisBuild / developers ++=
  Developer("miguelemosreverte", "Miguel Lemos", "miguelemosreverte@gmail.com", url("https://github.com/miguelemosreverte")) ::
    Nil

ThisBuild / scmInfo :=
  Some(
    ScmInfo(
      browseUrl = url("https://github.com/miguelemosreverte/evolution"),
      connection = "https://github.com/miguelemosreverte/evolution.git",
      devConnection = Some("git@github.com:miguelemosreverte/evolution.git")
    )
  )

ThisBuild / licenses := Nil