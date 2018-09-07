/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers

import auth.{GgAction, GovernmentGatewayProvider}
import connectors.identityVerificationProxy.IdentityVerificationProxyConnector
import models.registration._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalacheck.Arbitrary._
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Request
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PersonalDetailsSessionRepository
import resources._
import services.RegistrationService
import services.iv.{IdentityVerificationService, IvService}
import uk.gov.hmrc.auth.core.{Admin, AffinityGroup}
import utils._

import scala.concurrent.Future

  class IdentityVerificationSpec extends VoaPropertyLinkingSpec with MockitoSugar {

  "Successfully verifying identity when an organisation does not have a CCA account" must
    "register and enrol the user then redirect to the registration success page" in new TestCase {
    StubVplAuthConnector.stubExternalId("externalId")
    StubVplAuthConnector.stubGroupId("groupwithoutaccount")
    StubVplAuthConnector.stubUserDetails("externalId", testOrganisationInfo)
    StubIdentityVerification.stubSuccessfulJourney("successfuljourney")
    when(mockRegistrationService.create(any(), any())(any())(any(), any())).thenReturn(Future.successful(EnrolmentSuccess(1l)))

    val res = TestIdentityVerification.success()(requestWithJourneyId("successfuljourney"))
    status(res) mustBe SEE_OTHER
    redirectLocation(res) mustBe Some(controllers.registration.routes.RegistrationController.success(1l).url)
  }

  "Successfully verifying identity when an individual does not have a CCA account" must
    "register and enrol the user then redirect to the registration success page" in new TestCase {
    override lazy val mockSessionRepoOrgDetails = mockSessionRepoIndDetails
    StubVplAuthConnector.stubExternalId("externalId")
    StubVplAuthConnector.stubGroupId("groupwithoutaccount")
    StubVplAuthConnector.stubUserDetails("externalId", testIndividualInfo)
    StubIdentityVerification.stubSuccessfulJourney("successfuljourney")
    when(mockRegistrationService.create(any(), any())(any())(any(), any())).thenReturn(Future.successful(EnrolmentSuccess(1l)))

    val res = TestIdentityVerification.success()(requestWithJourneyId("successfuljourney"))
    status(res) mustBe SEE_OTHER
    redirectLocation(res) mustBe Some(controllers.registration.routes.RegistrationController.success(1l).url)
  }

  "Successfully verifying identity" must
    "return internal server error when the registration or enrolment fails" in new TestCase {
    StubVplAuthConnector.stubExternalId("externalId")
    StubVplAuthConnector.stubGroupId("groupwithoutaccount")
    StubVplAuthConnector.stubUserDetails("externalId", testOrganisationInfo)
    StubIdentityVerification.stubSuccessfulJourney("successfuljourney")
    when(mockRegistrationService.create(any(), any())(any())(any(), any())).thenReturn(Future.successful(EnrolmentFailure))
//    when(mockGovernmentGatewayProvider.redirectToLogin(any())).thenReturn( Future.successful(Redirect("redirect-url", Map("continue" -> Seq("test"), "origin" -> Seq("test-voa")))))
//    when(mockGgAction.provider).thenReturn(mockGovernmentGatewayProvider)


    val res = TestIdentityVerification.success()(requestWithJourneyId("successfuljourney"))

    status(res) mustBe INTERNAL_SERVER_ERROR
  }

  "Manually navigating to the iv success page after failing identity verification" must "return a 401 Unauthorised response" in new TestCase {
    StubIdentityVerification.stubFailedJourney("somejourneyid")

    val res = TestIdentityVerification.success()(request.withSession("journey-id" -> "somejourneyid"))
    status(res) mustBe UNAUTHORIZED
  }

  "Navigating to the iv failed page" must "return the failed page" in new TestCase {
    val res = TestIdentityVerification.fail()(request)
    status(res) mustBe OK
    contentAsString(res) must include("Identity verification failed")
  }

  "Navigating to restoreSession" must "redirect to the iv success page" in new TestCase {
    val res = TestIdentityVerification.restoreSession()(request)
    status(res) mustBe SEE_OTHER
    redirectLocation(res) mustBe Some(routes.IdentityVerification.success.url)
  }

//  "startIv" must "redirect to the iv start page when iv is enabled" in new TestCase {
//
////    val adminUser = AdminUser {}
//
//
//    when(mockSessionRepoOrgDetails.get[AdminUser]).thenReturn(Future.successful(None))
//    when(identityVerificationService)
//
//    val res = TestIdentityVerification.startIv()(request)
//    status(res) mustBe SEE_OTHER
//    redirectLocation(res) mustBe Some(routes.IdentityVerification.success.url)
//  }


  trait TestCase {
    lazy val mockSessionRepoOrgDetails = {
      val f = mock[PersonalDetailsSessionRepository]
      when(f.start(any())(any(), any())
      ).thenReturn(Future.successful(()))
      when(f.saveOrUpdate(any())(any(), any())
      ).thenReturn(Future.successful(()))
      when(f.get[AdminOrganisationAccountDetails](any(), any())).thenReturn(Future.successful(arbitrary[AdminOrganisationAccountDetails].sample))
      f
    }

    lazy val mockSessionRepoIndDetails = {
      val f = mock[PersonalDetailsSessionRepository]
      when(f.start(any())(any(), any())
      ).thenReturn(Future.successful(()))
      when(f.saveOrUpdate(any())(any(), any())
      ).thenReturn(Future.successful(()))
      when(f.get[IndividualUserAccountDetails](any(), any())).thenReturn(Future.successful(arbitrary[IndividualUserAccountDetails].sample))
      f
    }

    def testOrganisationInfo = UserInfo(firstName = Some("Bob"),
      lastName = Some("Smith"),
      email = "bob@smith.com",
      postcode = Some("AB12 3CD"),
      groupIdentifier = "GroupIdenfifier",
      affinityGroup = AffinityGroup.Organisation,
      gatewayId = "",
      credentialRole = Admin)

    def testIndividualInfo = UserInfo(firstName = Some("Bob"),
      lastName = Some("Smith"),
      email = "bob@smith.com",
      postcode = Some("AB12 3CD"),
      groupIdentifier = "GroupIdenfifier",
      affinityGroup = AffinityGroup.Individual,
      gatewayId = "",
      credentialRole = Admin)

    lazy val mockRegistrationService = mock[RegistrationService]
    //    lazy val mockIdentityVerificationService = mock[IdentityVerificationService]
    lazy val mockGgAction = mock[GgAction]
    lazy val mockGovernmentGatewayProvider = mock[GovernmentGatewayProvider]

    lazy val stubIdentityVerificationServiceEnrolmentOrg = new IvService(StubVplAuthConnector, mockRegistrationService, mockSessionRepoOrgDetails, app.injector.instanceOf[IdentityVerificationProxyConnector], applicationConfig)

    object TestIdentityVerification extends IdentityVerification(StubGgAction, StubIdentityVerification, StubAddresses,
      StubIndividualAccountConnector, stubIdentityVerificationServiceEnrolmentOrg, StubGroupAccountConnector,
      StubVplAuthConnector, mockSessionRepoOrgDetails)

    val request = FakeRequest()

    def requestWithJourneyId(id: String) = request.withSession("journeyId" -> id)
  }
}
