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

package uk.gov.hmrc.ui.pages

import org.openqa.selenium.By
import uk.gov.hmrc.selenium.webdriver.Driver
import uk.gov.hmrc.ui.conf.TestConfiguration

object AuthLoginPage extends BasePage {
  override val pageUrl: String = TestConfiguration.url("auth-login-stub") + "/gg-sign-in"

  private val redirectionUrlById: By = By.id("redirectionUrl")
  private val affinityGroupById: By  = By.id("affinityGroupSelect")
  private val authSubmitById: By     = By.id("submit-top")
  private val baseUrl: String        = TestConfiguration.url("disa-returns-frontend")

  private def loadPage: this.type = {
    get(pageUrl)
    verifyPageLoaded()
    this
  }

  private def submitAuthPage(): Unit = click(authSubmitById)

  private def submitAuth(redirectionUrl: String): Unit = {
    loadPage
    sendKeys(redirectionUrlById, s"$baseUrl$redirectionUrl")
    selectByVisibleText(affinityGroupById, "Organisation")
    submitAuthPage()
  }

  private def submitAuthWithEnrollmentInfo(
    redirectionUrl: String,
    enrolmentKey: String,
    IdentifierName: String,
    IdentifierValue: String
  ): Unit = {
    loadPage
    sendKeys(redirectionUrlById, s"$baseUrl$redirectionUrl")
    selectByVisibleText(affinityGroupById, "Organisation")
    sendKeys(By.id("enrolment[0].name"), enrolmentKey)
    sendKeys(By.id("input-0-0-name"), IdentifierName)
    sendKeys(By.id("input-0-0-value"), IdentifierValue)
    submitAuthPage()
  }

  private def submitAuthWithGroupId(
    redirectionUrl: String,
    groupId: String
  ): Unit = {
    loadPage
    sendKeys(redirectionUrlById, s"$baseUrl$redirectionUrl")
    selectByVisibleText(affinityGroupById, "Organisation")
    sendKeys(By.id("groupIdentifier"), groupId)
    submitAuthPage()
  }

  def launchAuthPage(redirectionUrl: String): Unit = {
    loadPage
    sendKeys(redirectionUrlById, s"$baseUrl$redirectionUrl")
    selectByVisibleText(affinityGroupById, "Organisation")

  }

  def loginAsAFreshUser(redirectionUrl: String): Unit =
    submitAuth(redirectionUrl)

  def loginAsEnrolledUser(
    redirectionUrl: String,
    enrolmentKey: String,
    IdentifierName: String,
    IdentifierValue: String
  ): Unit =
    submitAuthWithEnrollmentInfo(redirectionUrl, enrolmentKey, IdentifierName, IdentifierValue)

  def loginAsPendingEnrollmentUser(redirectionUrl: String, groupId: String): Unit =
    submitAuthWithGroupId(redirectionUrl, groupId)

  def enterCredId(credId: String): Unit =
    Driver.instance.findElement(By.id("authorityId")).sendKeys(credId)

}
