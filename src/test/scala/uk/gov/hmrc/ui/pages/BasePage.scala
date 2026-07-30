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

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions, FluentWait, Wait, WebDriverWait}
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.selenium.component.PageObject
import uk.gov.hmrc.selenium.webdriver.Driver
import uk.gov.hmrc.ui.conf.TestConfiguration
import java.util
import scala.jdk.CollectionConverters.CollectionHasAsScala
import java.time.Duration

trait BasePage extends PageObject with Matchers {
  val pageUrl: String
  val baseUrl: String           = TestConfiguration.url("disa-returns-frontend")
  val signInButtonClassName: By = By.partialLinkText("Sign in")
  val saveAndContinueButton: By = By.xpath("//button[contains(text(),'Save and continue')]")
  val continueButton: By        = By.xpath("//button[contains(text(),'Continue')]")
  val agreeAndSubmit: By        = By.xpath("//button[contains(text(),'I agree - submit')]")
  val signOutButton: By         = By.xpath("//a[contains(text(),'Sign out')]")
  val pageHeader: By            = By.xpath("//h1")
  val usrDir: String            = System.getProperty("user.dir") + "/src/test/resources/testData/"
  var filePath                  = ""

  def uploadFilesToBrowser(fileSeq: String, elementID: String): Unit = {
    fileSeq match {
      case "first"  => filePath = usrDir + "isa-open-valid.xlsx"
      case "second" => filePath = usrDir + "isa-open-empty.xlsx"
    }

    Driver.instance.findElement(By.id(elementID)).sendKeys(filePath)
    Driver.instance.findElement(By.id(elementID)).isEnabled
  }

  def waitFor[T](condition: ExpectedCondition[T]): T = {
    val wait = new WebDriverWait(Driver.instance, Duration.ofSeconds(10))
    wait.until(condition)
  }

  def waitForVisible(by: By): Unit = waitFor(elementToBeClickable(by))

  def secondsWait(secs: Int): Unit = Thread.sleep(secs.*(1000))

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

  def verifyPageTitleAndUrl(pageTitle: String, url: String): Boolean = {
    verifyUrlWithIds(url)
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

  def verifyUrlWithIds(url: String = this.pageUrl): Unit = fluentWait.until(ExpectedConditions.urlContains(url))

  def navigateTo(url: String): Unit = {
    Driver.instance.get(url)
    verifyPageLoaded(url)
  }

  def find(by: By): WebElement = {
    fluentWait.until(ExpectedConditions.presenceOfElementLocated(by))
    Driver.instance.findElement(by)
  }

  def disableJavaScript()(implicit driver: WebDriver): Unit =
    driver match {
      case chromeDriver: ChromeDriver =>
        val params = new util.HashMap[String, AnyRef]()

        params.put(
          "value",
          java.lang.Boolean.valueOf(true)
        )

        chromeDriver.executeCdpCommand(
          "Emulation.setScriptExecutionDisabled",
          params
        )

      case other =>
        throw new RuntimeException(
          s"Disabling JavaScript is only supported for ChromeDriver. Current driver: ${other.getClass.getName}"
        )
    }

  def goTo(page: BasePage): Unit = navigateTo(page.pageUrl)

  def clickOnByPartialLinkText(partialLinkText: String): Unit =
    click(By.partialLinkText(partialLinkText))

  def clickOnByLinkText(LinkText: String): Unit =
    click(By.ByLinkText(LinkText))

  def clickRadioButton(text: String): Unit =
    Driver.instance.findElements(By.tagName("label")).asScala.filter(_.getText.trim == text).head.click()

  def isElementPresent(locator: By): Boolean =
    Driver.instance.findElements(locator).size() > 0

  def clickSaveAndContinue(): Unit =
    click(saveAndContinueButton)

  def clickContinue(): Unit =
    click(continueButton)

  def clickAgreeAndSubmit(): Unit =
    click(agreeAndSubmit)

  def signOut(): Unit =
    click(signOutButton)
}
