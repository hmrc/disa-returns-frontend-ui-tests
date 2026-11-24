/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.ui.pages

import play.api.libs.json.*
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import play.api.libs.ws.StandaloneWSResponse
import uk.gov.hmrc.api.conf.TestEnvironment
import uk.gov.hmrc.apitestrunner.http.HttpClient
import uk.gov.hmrc.ui.conf.TestConfiguration

import scala.concurrent.Await
import scala.concurrent.duration.*

trait DisaReturnsService extends HttpClient {

  private lazy val disaReturnsHost: String           = TestEnvironment.url("disa-returns")
  private lazy val disaReturnsBackendHost: String    = TestEnvironment.url("disa-returns-backend")
  private lazy val disaReturnsSubmissionHost: String = TestEnvironment.url("disa-returns-submission")
  private lazy val disaReturnsPath: String           = "/monthly"
  private lazy val disaReturnsCallbackPath: String   = "/callback/monthly"
  private lazy val disaReturnsBase: String           = s"$disaReturnsHost$disaReturnsPath"

  def postSubmission(
    isaManagerReference: String,
    taxYear: String,
    headers: Map[String, String],
    ndString: String = "",
    month: String
  ): StandaloneWSResponse =
    Await.result(
      mkRequest(s"$disaReturnsBase/$isaManagerReference/$taxYear/$month")
        .withHttpHeaders(headers.toSeq: _*)
        .post(ndString),
      10.seconds
    )

  private def postJson(url: String, body: JsObject): StandaloneWSResponse =
    Await.result(
      mkRequest(url).addHttpHeaders("Content-Type" -> "application/json").post(Json.stringify(body)),
      10.seconds
    )

  def deleteBackendMonthlyReturnsRequest(zReferences: Seq[String]): StandaloneWSResponse =
    postJson(
      s"$disaReturnsBackendHost/test-only/monthly-returns/cleanup",
      Json.obj("zReferences" -> zReferences)
    )

  def deleteSubmissionMonthlyReturnsRequest(zReferences: Seq[String]): StandaloneWSResponse =
    postJson(
      s"$disaReturnsSubmissionHost/test-only/monthly-returns/cleanup",
      Json.obj("zReferences" -> zReferences)
    )

  def getSubmissionMonthlyReturn(path: String): StandaloneWSResponse =
    Await.result(
      mkRequest(s"$disaReturnsSubmissionHost$path")
        .addHttpHeaders(
          "Authorization" -> TestConfiguration.envConfig.getString("services.disa-returns-submission.internalAuthToken")
        )
        .get(),
      10.seconds
    )

  def getBackendMonthlyReturn(path: String, authorization: String): StandaloneWSResponse =
    Await.result(
      mkRequest(s"$disaReturnsBackendHost$path").addHttpHeaders("Authorization" -> authorization).get(),
      10.seconds
    )

  def backendAuthorization(zReference: String): String = {
    val response = postJson(
      TestEnvironment.url("auth-login-api"),
      Json.obj(
        "credId"             -> s"000000000000${zReference.drop(1)}",
        "affinityGroup"      -> "Organisation",
        "credentialStrength" -> "strong",
        "credentialRole"     -> "admin",
        "enrolments"         -> Json.arr(
          Json.obj(
            "key"         -> "HMRC-DISA-ORG",
            "identifiers" -> Json.arr(Json.obj("key" -> "ZREF", "value" -> zReference)),
            "state"       -> "Activated"
          )
        )
      )
    )
    require(response.status == 201, s"Failed to get backend authorization for $zReference: HTTP ${response.status}")
    response.header("Authorization").getOrElse(throw new IllegalStateException("Auth login returned no bearer token"))
  }

  def postDeclaration(
    isaManagerReference: String,
    taxYear: String,
    month: String,
    headers: Map[String, String],
    nilReturn: Boolean
  ): StandaloneWSResponse = {

    val body = Json.stringify(Json.obj("nilReturn" -> nilReturn))
    Await.result(
      mkRequest(s"$disaReturnsBase/$isaManagerReference/$taxYear/$month/declaration")
        .withHttpHeaders(headers.toSeq: _*)
        .post(body),
      10.seconds
    )
  }

  def getReportingResultsSummary(
    isaManagerReference: String,
    taxYear: String,
    month: String,
    headers: Map[String, String]
  ): StandaloneWSResponse =
    Await.result(
      mkRequest(
        s"$disaReturnsBase/$isaManagerReference/$taxYear/$month/results/summary"
      )
        .withHttpHeaders(headers.toSeq: _*)
        .get(),
      10.seconds
    )

  def getReconciliationReport(
    isaManagerReference: String,
    taxYear: String,
    month: String,
    page: Int,
    headers: Map[String, String]
  ): StandaloneWSResponse =
    Await.result(
      mkRequest(
        s"$disaReturnsBase/$isaManagerReference/$taxYear/$month/results?page=$page"
      )
        .withHttpHeaders(headers.toSeq: _*)
        .get(),
      10.seconds
    )

  def setSubmissionsClock(date: String, zReferences: Seq[String]): StandaloneWSResponse =
    Await.result(
      mkRequest(s"$disaReturnsSubmissionHost/test-only/overrides")
        .addHttpHeaders("Content-Type" -> "application/json")
        .put(
          Json.stringify(
            Json.obj(
              "zReferences"     -> zReferences,
              "clock"           -> Json.obj("date" -> date),
              "reportingWindow" -> JsNull
            )
          )
        ),
      10.seconds
    )
  def resetSubmissionOverrides(zReferences: Seq[String]): StandaloneWSResponse          =
    postJson(
      s"$disaReturnsSubmissionHost/test-only/overrides/delete",
      Json.obj("zReferences" -> zReferences)
    )

  def makeReturnSummaryCallback(
    isaManagerReference: String,
    taxYear: String,
    month: String,
    totalRecords: Int,
    headers: Map[String, String]
  ): StandaloneWSResponse = {
    val payload =
      s"""
         |{
         |  "totalRecords": $totalRecords
         |}
         |""".stripMargin
    Await.result(
      mkRequest(s"$disaReturnsHost$disaReturnsCallbackPath/$isaManagerReference/$taxYear/$month")
        .withHttpHeaders(headers.toSeq: _*)
        .post(payload),
      10.seconds
    )
  }
}
