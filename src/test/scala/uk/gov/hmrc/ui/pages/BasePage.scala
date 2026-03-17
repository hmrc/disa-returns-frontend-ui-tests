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

import org.openqa.selenium.support.ui.{ExpectedConditions, FluentWait, Wait}
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.selenium.component.PageObject
import uk.gov.hmrc.selenium.webdriver.Driver
import uk.gov.hmrc.ui.conf.TestConfiguration

import java.time.Duration

trait BasePage extends PageObject with Matchers {
  val pageUrl: String
  val baseUrl: String           = TestConfiguration.url("disa-registration-frontend")
  val signInButtonClassName: By = By.partialLinkText("Sign in")
  val saveAndContinueButton: By = By.xpath("//button[contains(text(),'Save and continue')]")
  val continueButton: By        = By.xpath("//a[contains(text(),'Continue')]")
  val signOutButton: By         = By.xpath("//a[contains(text(),'Sign out')]")
  val pageHeader: By            = By.xpath("//h1")

  def verifyPageUrl(): Boolean =
    getCurrentUrl == pageUrl

  def verifyPageTitle(pageTitle: String, url: String): Boolean = {
    verifyPageLoaded(url)
    val actualTitle = getTitle
    if (actualTitle != pageTitle) {
      println(s"[Title Mismatch] Expected: '$pageTitle' | Actual: '$actualTitle'")
      false
    } else {
      true
    }
  }

  def verifyPageLoadedWithHeader(pageHeaderText: String, url: String): Boolean = {
    verifyPageLoaded(url)
    val actualHeader = getText(pageHeader)
    if (actualHeader != pageHeaderText) {
      println(s"[Header Mismatch] Expected: '$pageHeaderText' | Actual: '$actualHeader'")
      false
    } else {
      true
    }
  }

  private def fluentWait: Wait[WebDriver] = new FluentWait[WebDriver](Driver.instance)
    .withTimeout(Duration.ofSeconds(2))
    .pollingEvery(Duration.ofMillis(200))

  def verifyPageLoaded(url: String = this.pageUrl): Unit = fluentWait.until(ExpectedConditions.urlToBe(url))

  def navigateTo(url: String): Unit = {
    Driver.instance.get(url)
    verifyPageLoaded(url)
  }

  def goTo(page: BasePage): Unit = navigateTo(page.pageUrl)

  def clickOnByPartialLinkText(partialLinkText: By): Unit = {
    verifyPageLoaded()
    click(partialLinkText)
  }

  def isElementPresent(locator: By): Boolean =
    Driver.instance.findElements(locator).size() > 0

  def clickSaveAndContinue(): Unit =
    click(saveAndContinueButton)

  def clickContinue(): Unit =
    click(continueButton)

  def signOut(): Unit =
    click(signOutButton)
}
