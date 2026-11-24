/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.ui.specs

import org.scalatest.*
import org.scalatest.concurrent.Eventually
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.*
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.selenium.webdriver.{Browser, ScreenshotOnFailure}
import uk.gov.hmrc.ui.pages.DisaReturnsService

import java.time.LocalDate
import scala.util.control.NonFatal

trait BaseSpec
    extends AnyFeatureSpec
    with GivenWhenThen
    with Matchers
    with Eventually
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Browser
    with ScreenshotOnFailure
    with DisaReturnsService {

  protected def testZReferences: Seq[String]

  private val declarationPeriodDate = "2026-08-17"
  private val reportingPeriod       = LocalDate.parse(declarationPeriodDate).minusMonths(1)
  private val taxYearStart          =
    if (reportingPeriod.getMonthValue < 4) {
      reportingPeriod.getYear - 1
    } else {
      reportingPeriod.getYear
    }
  private val taxYear               = s"$taxYearStart-${(taxYearStart + 1).toString.takeRight(2)}"

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    clearMonthlyReturns(testZReferences)
    resetClock(testZReferences)
  }

  override def beforeEach(): Unit = startBrowser()

  override def afterEach(): Unit = quitBrowser()

  protected def monthlyReturnScenario(name: String, zReference: String)(test: String => Unit): Unit =
    Scenario(name) {
      val zReferences = Seq(zReference)
      try {
        clearMonthlyReturns(zReferences)
        withClue(s"Failed to set the submission clock for $zReference: ") {
          setSubmissionsClock(declarationPeriodDate, zReferences).status shouldBe 204
        }
        test(zReference)
        // The confirmation page is shown before background processing finishes.
        awaitMonthlyReturnProcessing(zReference)
        clearMonthlyReturns(zReferences)
      } catch {
        case NonFatal(error) =>
          try resetClock(zReferences)
          catch { case NonFatal(resetError) => error.addSuppressed(resetError) }
          throw error
      }
      resetClock(zReferences)
    }

  private def clearMonthlyReturns(zReferences: Seq[String]): Unit = {
    withClue(s"Failed to clear backend monthly returns for ${zReferences.mkString(", ")}: ") {
      deleteBackendMonthlyReturnsRequest(zReferences).status shouldBe 204
    }
    withClue(s"Failed to clear submission monthly returns for ${zReferences.mkString(", ")}: ") {
      deleteSubmissionMonthlyReturnsRequest(zReferences).status shouldBe 204
    }
  }

  private def resetClock(zReferences: Seq[String]): Unit =
    withClue(s"Failed to reset submission overrides for ${zReferences.mkString(", ")}: ") {
      resetSubmissionOverrides(zReferences).status shouldBe 204
    }

  private def awaitMonthlyReturnProcessing(zReference: String): Unit = {
    val path       = s"/monthly/$zReference/$taxYear/${reportingPeriod.getMonthValue}"
    val poll       = interval(Span(1, Seconds))
    val submission = eventually(timeout(Span(6, Minutes)), poll) {
      val response      = getSubmissionMonthlyReturn(path)
      withClue(s"Reading submission monthly return $path: ") {
        response.status shouldBe 200
      }
      val monthlyReturn = Json.parse(response.body)
      withClue(s"Waiting for NPS declaration of $path: ") {
        (monthlyReturn \ "npsDeclaredOn").asOpt[String] should not be empty
      }
      monthlyReturn
    }

    if (!(submission \ "nilReturn").as[Boolean]) {
      val references    = (submission \ "submissions").as[Seq[JsValue]].map(file => (file \ "reference").as[String])
      val authorization = backendAuthorization(zReference)

      references should not be empty
      eventually(timeout(Span(30, Seconds)), poll) {
        val response  = getBackendMonthlyReturn(path, authorization)
        withClue(s"Reading backend monthly return $path: ") {
          response.status shouldBe 200
        }
        val uploads   = (Json.parse(response.body) \ "fileUploads").as[Seq[JsValue]]
        val submitted = uploads.collect {
          case file if (file \ "status").as[String] == "SUBMITTED" => (file \ "reference").as[String]
        }
        withClue(s"Waiting for backend file transfer of $path: ") {
          submitted should contain allElementsOf references
        }
      }
    }
  }
}
