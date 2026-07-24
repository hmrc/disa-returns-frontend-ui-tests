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

import uk.gov.hmrc.ui.pages.*

class FileUploadSpec extends BaseSpec {

  Feature("ISA manager logs in and submits a monthly report") {

    Scenario("1. ISA manager logs in and submits a monthly report with js enabled on browser") {

      Given("the ISA manager logs in as an already enrolled organisation User")
      AuthLoginPage.loginAsEnrolledUser("/monthly-report-submission", "HMRC-DISA-ORG", "ZREF", "Z1023")

      Then("The 'Organisation is Enrolled' page is displayed")
      MonthlyReportSubmissionPage.verifyPageTitle(
        MonthlyReportSubmissionPage.pageTitle,
        MonthlyReportSubmissionPage.pageUrl
      ) shouldBe true

      When(
        "the user clicks on the Yes radio button and then clicks on save and continue button on 'Monthly Report Submission' page"
      )
      MonthlyReportSubmissionPage.clickRadioButton("Yes - I am uploading a report")
      MonthlyReportSubmissionPage.clickSaveAndContinue()

      Then("the user is navigated to the 'File Upload' page")
      FileUploadPage.verifyPageTitle(
        FileUploadPage.pageTitle,
        FileUploadPage.pageUrl
      ) shouldBe true

      When("User selects a valid file upload")
      FileUploadPage.chooseFileAndUploadFile("first")

      And("User clicks Continue button")
      FileUploadPage.clickContinue()

      And("I wait for the file to be uploaded")
      FileUploadPage.thenWaitForXSeconds(10)

      Then("the user clicks on the No radio button and then clicks on continue button on 'file uploaded' page")
      UploadedReportFilesPage.clickRadioButton("No")
      UploadedReportFilesPage.clickContinue()

      Then("the user is navigated to the 'Check Your Answers' page")
      CheckYourAnswersPage.verifyPageTitle(
        CheckYourAnswersPage.pageTitle,
        CheckYourAnswersPage.pageUrl
      ) shouldBe true

      Then("the user clicks on save and continue button on 'Check your answers' page")
      CheckYourAnswersPage.clickSaveAndContinue()

      Then("the user is navigated to the 'Declaration' page")
      DeclarationPage.verifyPageTitle(
        DeclarationPage.pageTitle,
        DeclarationPage.pageUrl
      ) shouldBe true

      Then("the user clicks on save and continue button on 'Declaration' page")
      DeclarationPage.clickAgreeAndSubmit()

      Then("the user is navigated to the 'Submission Complete' page")
      SubmissionCompletePage.verifyPageTitle(
        SubmissionCompletePage.pageTitle,
        SubmissionCompletePage.pageUrl
      ) shouldBe true

    }
    Scenario("2. ISA manager logs in and submits a nil report") {

      Given("the ISA manager logs in as an already enrolled organisation User")
      AuthLoginPage.loginAsEnrolledUser("/monthly-report-submission", "HMRC-DISA-ORG", "ZREF", "Z1133")

      Then("The 'Organisation is Enrolled' page is displayed")
      MonthlyReportSubmissionPage.verifyPageTitle(
        MonthlyReportSubmissionPage.pageTitle,
        MonthlyReportSubmissionPage.pageUrl
      ) shouldBe true

      When(
        "the user clicks on the No radio button and then clicks on save and continue button on 'Monthly Report Submission' page"
      )
      MonthlyReportSubmissionPage.clickRadioButton("No - I have a nil report")
      MonthlyReportSubmissionPage.clickSaveAndContinue()

      Then("the user is navigated to the 'Check Your Answers' page")
      CheckYourAnswersPage.verifyPageTitle(
        CheckYourAnswersPage.pageTitle,
        CheckYourAnswersPage.pageUrl
      ) shouldBe true

      Then("the user clicks on save and continue button on 'Check your answers' page")
      CheckYourAnswersPage.clickSaveAndContinue()

      Then("the user is navigated to the 'Declaration' page")
      DeclarationPage.verifyPageTitle(
        DeclarationPage.pageTitleTwo,
        DeclarationPage.pageUrl
      ) shouldBe true

      Then("the user clicks on save and continue button on 'Declaration' page")
      DeclarationPage.clickAgreeAndSubmit()

      Then("the user is navigated to the 'Submission Complete' page")
      SubmissionCompletePage.verifyPageTitle(
        SubmissionCompletePage.pageTitle,
        SubmissionCompletePage.pageUrl
      ) shouldBe true

    }
  }
}
