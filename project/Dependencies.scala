import sbt.*

object Dependencies {

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "ui-test-runner"  % "0.54.0" % Test,
    "uk.gov.hmrc" %% "api-test-runner" % "0.10.0" % Test
  )

}
