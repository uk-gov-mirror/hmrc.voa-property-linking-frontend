/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.agent

import binders.pagination.PaginationParameters
import binders.propertylinks.GetPropertyLinksParameters
import controllers.VoaPropertyLinkingSpec
import models.propertyrepresentation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import tests.AllMocks

import scala.concurrent.Future

class ManageAgentControllerSpec extends VoaPropertyLinkingSpec with MockitoSugar with AllMocks {

  private val mockManageAgentPage = mock[views.html.propertyrepresentation.manage.manageAgent]
  private val mockRemoveAgentFromOrganisationPage =
    mock[views.html.propertyrepresentation.manage.removeAgentFromOrganisation]
  private val mockUnassignAgentFromPropertyPage =
    mock[views.html.propertyrepresentation.manage.unassignAgentFromProperty]
  private val mockAddAgentToAllPropertyPage =
    mock[views.html.propertyrepresentation.manage.addAgentToAllProperties]
  private val mockConfirmAddAgentToAllProperty =
    mock[views.html.propertyrepresentation.manage.confirmAddAgentToAllProperties]
  private val mockUnassignAgentFromAllProperties =
    mock[views.html.propertyrepresentation.manage.unassignAgentFromAllProperties]
  private val mockConfirmUnassignAgentFromAllProperties =
    mock[views.html.propertyrepresentation.manage.confirmUnassignAgentFromAllProperties]
  private val mockConfirmRemoveAgentFromOrganisation =
    mock[views.html.propertyrepresentation.manage.confirmRemoveAgentFromOrganisation]
  private val mockMyAgentsPage = mock[views.html.propertyrepresentation.manage.myAgents]
  private val mockManageAgentSimplePage = mock[views.html.propertyrepresentation.manage.manageAgentSimpleProperties]

  val testController = new ManageAgentController(
    errorHandler = mockCustomErrorHandler,
    authenticated = preAuthenticatedActionBuilders(userIsAgent = false),
    agentRelationshipService = mockAgentRelationshipService,
    manageAgentPage = mockManageAgentPage,
    myAgentsPage = mockMyAgentsPage,
    removeAgentFromOrganisation = mockRemoveAgentFromOrganisationPage,
    unassignAgentFromProperty = mockUnassignAgentFromPropertyPage,
    addAgentToAllProperties = mockAddAgentToAllPropertyPage,
    confirmAddAgentToAllProperties = mockConfirmAddAgentToAllProperty,
    unassignAgentFromAllProperties = mockUnassignAgentFromAllProperties,
    confirmUnassignAgentFromAllProperties = mockConfirmUnassignAgentFromAllProperties,
    confirmRemoveAgentFromOrganisation = mockConfirmRemoveAgentFromOrganisation,
    mockManageAgentSimplePage
  )

  "showAgents" should "show the manage agent page" in {
    val propertyLinksCount = 1
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any()))
      .thenReturn(Future.successful(organisationsAgentsList.copy(agents = List(agentSummary.copy(propertyCount = 0)))))
    when(mockAgentRelationshipService.getMyOrganisationPropertyLinksCount()(any()))
      .thenReturn(Future.successful(propertyLinksCount))
    when(mockMyAgentsPage.apply(any(), any())(any(), any(), any()))
      .thenReturn(Html(""))
    val res = testController.showAgents()(FakeRequest())
    status(res) mustBe OK
  }

  "manageAgent" should "show the manage agent page" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any()))
      .thenReturn(Future.successful(organisationsAgentsList.copy(agents = List(agentSummary.copy(propertyCount = 0)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithNoAuthorisations))
    when(mockRemoveAgentFromOrganisationPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html(""))
    val res = testController.manageAgent(None)(FakeRequest())
    status(res) mustBe OK
  }

  "manageAgentProperties" should "return the correct manage agent page with property links" in {
    val agentCode = 1
    when(mockAgentRelationshipService.getMyAgentPropertyLinks(any(), any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithTwoAuthorisation))
    when(mockAgentRelationshipService.getAgentNameAndAddress(any())(any()))
      .thenReturn(Future.successful(Some(agentDetails)))
    when(mockManageAgentSimplePage.apply(any(), any(), any())(any(), any(), any()))
      .thenReturn(Html(""))
    val res = testController.manageAgentProperties(agentCode, GetPropertyLinksParameters(), PaginationParameters())(
      FakeRequest())
    status(res) mustBe OK

  }

  "getManageAgentPage" should "return the correct manage agent page when org has one agent but no property links" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any()))
      .thenReturn(Future.successful(organisationsAgentsList.copy(agents = List(agentSummary.copy(propertyCount = 0)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithNoAuthorisations))
    when(mockRemoveAgentFromOrganisationPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has Zero PropertyLinks"))

    val res = testController.getManageAgentPage(None)(FakeRequest())
    res.futureValue.isDefined mustBe true
    res.futureValue.get.toString() mustBe "IP has Zero PropertyLinks"

  }

  "getManageAgentPage" should "return the correct manage agent page when org has one agent and one property link (agent not assigned)" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any()))
      .thenReturn(Future.successful(organisationsAgentsList.copy(agents = List(agentSummary.copy(propertyCount = 0)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithOneAuthorisation))
    when(mockManageAgentPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has One PropertyLink - agent not assigned"))

    val res = testController.getManageAgentPage(None)(FakeRequest())
    res.futureValue.isDefined mustBe true
    res.futureValue.get.toString() mustBe "IP has One PropertyLink - agent not assigned"
  }

  "getManageAgentPage" should "return the correct manage agent page when org has one agent and one property link (agent assigned)" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any()))
      .thenReturn(Future.successful(organisationsAgentsList.copy(agents = List(agentSummary.copy(propertyCount = 1)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithOneAuthorisation))
    when(mockUnassignAgentFromPropertyPage.apply(any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has One PropertyLink - agent assigned"))

    val res = testController.getManageAgentPage(None)(FakeRequest())
    res.futureValue.isDefined mustBe true
    res.futureValue.get.toString() mustBe "IP has One PropertyLink - agent assigned"
  }

  "getManageAgentPage" should "return the correct manage agent page when org has one agent and one property link (agent assigned) - agentCodeProvided" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any())).thenReturn(
      Future.successful(organisationsAgentsList.copy(
        agents = List(agentSummary.copy(propertyCount = 1, representativeCode = agentCode)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithOneAuthorisation))
    when(mockUnassignAgentFromPropertyPage.apply(any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has One PropertyLink - agent assigned"))

    val res = testController.getManageAgentPage(Some(agentCode))(FakeRequest())
    res.futureValue.isDefined mustBe true
    res.futureValue.get.toString() mustBe "IP has One PropertyLink - agent assigned"
  }

  "getManageAgentPage" should "return the correct manage agent page when org has one agent and more than one property links (agent not assigned)" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any()))
      .thenReturn(Future.successful(organisationsAgentsList.copy(agents = List(agentSummary.copy(propertyCount = 0)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithOneAuthorisation))
    when(mockManageAgentPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has more than one PropertyLinks - agent not assigned"))

    val res = testController.getManageAgentPage(None)(FakeRequest())
    res.futureValue.isDefined mustBe true
    res.futureValue.get.toString() mustBe "IP has more than one PropertyLinks - agent not assigned"
  }

  "getManageAgentPage" should "return the correct manage agent page when org has one agent and more than one property links (agent assigned to some)" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any()))
      .thenReturn(Future.successful(organisationsAgentsList.copy(agents = List(agentSummary.copy(propertyCount = 1)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithTwoAuthorisation))
    when(mockManageAgentPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has more than one PropertyLinks - agent assigned to some"))

    val res = testController.getManageAgentPage(None)(FakeRequest())
    res.futureValue.isDefined mustBe true
    res.futureValue.get.toString() mustBe "IP has more than one PropertyLinks - agent assigned to some"
  }

  "getManageAgentPage" should "return the correct manage agent page when org has one agent and more than one property links (agent assigned to all)" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any()))
      .thenReturn(Future.successful(organisationsAgentsList.copy(agents = List(agentSummary.copy(propertyCount = 2)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithTwoAuthorisation))
    when(mockManageAgentPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has more than one PropertyLinks - agent assigned to all"))

    val res = testController.getManageAgentPage(None)(FakeRequest())
    res.futureValue.isDefined mustBe true
    res.futureValue.get.toString() mustBe "IP has more than one PropertyLinks - agent assigned to all"
  }

  "getManageAgentPage" should "return None for an invalid propertyLinks and agent details combination" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any()))
      .thenReturn(Future.successful(organisationsAgentsList.copy(agents = List(agentSummary.copy(propertyCount = 9)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithTwoAuthorisation))

    val res = testController.getManageAgentPage(None)(FakeRequest())
    res.futureValue mustBe None
  }

  "submitManageAgent" should "return 400 Bad Request when an invalid manage property option is submitted " in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any())).thenReturn(
      Future.successful(organisationsAgentsList.copy(
        agents = List(agentSummary.copy(propertyCount = 1, representativeCode = agentCode)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithOneAuthorisation))
    when(mockUnassignAgentFromPropertyPage.apply(any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has One PropertyLink - agent assigned"))

    val res = testController.submitManageAgent(agentCode)(
      FakeRequest().withFormUrlEncodedBody("manageAgentOption" -> "BLAH", "agentCode" -> s"$agentCode"))
    status(res) mustBe BAD_REQUEST
  }

  "submitManageAgent" should "return 400 Bad Request when agentCode is not submitted " in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any())).thenReturn(
      Future.successful(organisationsAgentsList.copy(
        agents = List(agentSummary.copy(propertyCount = 1, representativeCode = agentCode)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithOneAuthorisation))
    when(mockUnassignAgentFromPropertyPage.apply(any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has One PropertyLink - agent assigned"))

    val res = testController.submitManageAgent(agentCode)(
      FakeRequest().withFormUrlEncodedBody("manageAgentOption" -> "unassignFromAllProperties"))
    status(res) mustBe BAD_REQUEST
  }

  "submitManageAgent" should "return 200 Ok when IP chooses to appoint agent to all properties" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any())).thenReturn(
      Future.successful(organisationsAgentsList.copy(
        agents = List(agentSummary.copy(propertyCount = 1, representativeCode = agentCode)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithTwoAuthsAgentAssignedToOne))
    when(mockManageAgentPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has more than one PropertyLink - agent assigned to some"))
    when(mockAddAgentToAllPropertyPage.apply(any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("addAgentToAllProperty"))

    val res = testController.submitManageAgent(agentCode)(
      FakeRequest()
        .withFormUrlEncodedBody("manageAgentOption" -> s"${AssignToAllProperties.name}", "agentName" -> "Agent Org"))

    status(res) mustBe OK
  }

  "submitManageAgent" should "return 303 Redirect when IP chooses to appoint agent to some properties" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any())).thenReturn(
      Future.successful(organisationsAgentsList.copy(
        agents = List(agentSummary.copy(propertyCount = 1, representativeCode = agentCode)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithTwoAuthsAgentAssignedToOne))
    when(mockManageAgentPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has more than one PropertyLink - agent assigned to some"))

    val res = testController.submitManageAgent(agentCode)(
      FakeRequest()
        .withFormUrlEncodedBody("manageAgentOption" -> s"${AssignToSomeProperties.name}", "agentName" -> "Agent Org"))

    status(res) mustBe SEE_OTHER
    redirectLocation(res) mustBe
      Some(
        "/business-rates-property-linking/my-organisation/appoint/properties?page=1&pageSize=15&" +
          "agentCode=12345&agentAppointed=BOTH&backLink=%2F" +
          "business-rates-property-linking%2Fmy-organisation%2Fmanage-agent%3FagentCode%3D12345")
  }

  "submitManageAgent" should "return 200 Ok when IP chooses to unassigned agent from all properties" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any())).thenReturn(
      Future.successful(organisationsAgentsList.copy(
        agents = List(agentSummary.copy(propertyCount = 1, representativeCode = agentCode)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithTwoAuthsAgentAssignedToOne))
    when(mockManageAgentPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has more than one PropertyLink - agent assigned to some"))
    when(mockUnassignAgentFromAllProperties.apply(any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("unassignAgentFromAllProperties"))

    val res = testController.submitManageAgent(agentCode)(FakeRequest()
      .withFormUrlEncodedBody("manageAgentOption" -> s"${UnassignFromAllProperties.name}", "agentName" -> "Agent Org"))

    status(res) mustBe OK
  }

  "submitManageAgent" should "return 303 Redirect when IP chooses to unassign agent from some properties" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any())).thenReturn(
      Future.successful(organisationsAgentsList.copy(
        agents = List(agentSummary.copy(propertyCount = 1, representativeCode = agentCode)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithTwoAuthsAgentAssignedToOne))
    when(mockManageAgentPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has more than one PropertyLink - agent assigned to some"))

    val res = testController.submitManageAgent(agentCode)(FakeRequest()
      .withFormUrlEncodedBody("manageAgentOption" -> s"${UnassignFromSomeProperties.name}", "agentName" -> "Agent Org"))

    status(res) mustBe SEE_OTHER
    redirectLocation(res) mustBe
      Some(
        "/business-rates-property-linking/my-organisation/revoke/properties?page=1&pageSize=15" +
          "&agentCode=12345")
  }

  "submitManageAgent" should "return 200 Ok when IP chooses to remove agent from account" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any())).thenReturn(
      Future.successful(organisationsAgentsList.copy(
        agents = List(agentSummary.copy(propertyCount = 1, representativeCode = agentCode)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithTwoAuthsAgentAssignedToOne))
    when(mockManageAgentPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has more than one PropertyLink - agent assigned to some"))
    when(mockRemoveAgentFromOrganisationPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("removeAgentFromOrganisation"))

    val res = testController.submitManageAgent(agentCode)(
      FakeRequest()
        .withFormUrlEncodedBody("manageAgentOption" -> s"${RemoveFromYourAccount.name}", "agentName" -> "Agent Org"))

    status(res) mustBe OK
  }

  "submitManageAgent" should "return 200 Ok when IP chooses to appoint agent to only property" in {
    when(mockAgentRelationshipService.getMyOrganisationAgents()(any())).thenReturn(
      Future.successful(organisationsAgentsList.copy(
        agents = List(agentSummary.copy(propertyCount = 1, representativeCode = agentCode)))))
    when(mockAgentRelationshipService.getMyOrganisationsPropertyLinks(any(), any())(any()))
      .thenReturn(Future.successful(ownerAuthResultWithTwoAuthsAgentAssignedToOne))
    when(mockManageAgentPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("IP has more than one PropertyLink - agent assigned to some"))
    when(mockAddAgentToAllPropertyPage.apply(any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("addAgentToAllProperty"))

    val res = testController.submitManageAgent(agentCode)(
      FakeRequest()
        .withFormUrlEncodedBody("manageAgentOption" -> s"${AssignToYourProperty.name}", "agentName" -> "Agent Org"))

    status(res) mustBe OK
  }

  "assignAgentToAll" should "return 400 Bad Request when invalid form submitted" in {
    when(mockAddAgentToAllPropertyPage.apply(any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("addAgentToAllProperty"))

    val res = testController.assignAgentToAll(agentCode, "Some agent org")(
      FakeRequest().withFormUrlEncodedBody("agentCode" -> s"$agentCode"))

    status(res) mustBe BAD_REQUEST
  }

  "assignAgentToAll" should "return 200 Ok a valid form is submitted" in {
    when(mockAgentRelationshipService.assignAgent(any())(any()))
      .thenReturn(Future.successful(AgentAppointmentChangesResponse("some-id")))
    when(mockConfirmAddAgentToAllProperty.apply(any())(any(), any(), any()))
      .thenReturn(Html("confirmAddAgentToAllProperty"))

    val res = testController.assignAgentToAll(agentCode, "Some agent org")(
      FakeRequest()
        .withFormUrlEncodedBody("agentCode" -> s"$agentCode", "scope" -> s"${AppointmentScope.ALL_PROPERTIES}"))

    status(res) mustBe OK
  }

  "unassignAgentFromAll" should "return 400 Bad Request when invalid form submitted" in {
    when(mockUnassignAgentFromAllProperties.apply(any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("unassignAgentFromAllProperties"))

    val res = testController.unassignAgentFromAll(agentCode, "Some agent org")(
      FakeRequest().withFormUrlEncodedBody("agentCode" -> s"$agentCode"))

    status(res) mustBe BAD_REQUEST
  }

  "unassignAgentFromAll" should "return 200 Ok a valid form is submitted" in {
    when(mockAgentRelationshipService.unassignAgent(any())(any()))
      .thenReturn(Future.successful(AgentAppointmentChangesResponse("some-id")))
    when(mockConfirmUnassignAgentFromAllProperties.apply(any(), any())(any(), any(), any()))
      .thenReturn(Html("confirmUnassignAgentFromAllProperties"))

    val res = testController.unassignAgentFromAll(agentCode, "Some agent org")(
      FakeRequest()
        .withFormUrlEncodedBody("agentCode" -> s"$agentCode", "scope" -> s"${AppointmentScope.ALL_PROPERTIES}"))

    status(res) mustBe OK
  }

  "showRemoveAgentFromIpOrganisation" should "return 200 Ok" in {
    when(mockRemoveAgentFromOrganisationPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("removeAgentFromOrganisationPage"))

    val res = testController.showRemoveAgentFromIpOrganisation(agentCode, "Some agent org")(FakeRequest())

    status(res) mustBe OK
  }

  "removeAgentFromIpOrganisation" should "return 400 Bad Request when invalid form submitted" in {
    when(mockRemoveAgentFromOrganisationPage.apply(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(Html("removeAgentFromOrganisationPage"))

    val res = testController.removeAgentFromIpOrganisation(agentCode, "Some agent org", "some-back-link")(
      FakeRequest().withFormUrlEncodedBody("agentCode" -> s"$agentCode"))

    status(res) mustBe BAD_REQUEST
  }

  "removeAgentFromIpOrganisation" should "return 200 Ok a valid form is submitted" in {
    when(mockAgentRelationshipService.removeAgentFromOrganisation(any())(any()))
      .thenReturn(Future.successful(AgentAppointmentChangesResponse("some-id")))
    when(mockConfirmRemoveAgentFromOrganisation.apply(any())(any(), any(), any()))
      .thenReturn(Html("confirmRemoveAgentFromOrganisation"))

    val res = testController.removeAgentFromIpOrganisation(agentCode, "Some agent org", "back-link")(
      FakeRequest()
        .withFormUrlEncodedBody("agentCode" -> s"$agentCode", "scope" -> s"${AppointmentScope.RELATIONSHIP}"))

    status(res) mustBe OK
  }

}
