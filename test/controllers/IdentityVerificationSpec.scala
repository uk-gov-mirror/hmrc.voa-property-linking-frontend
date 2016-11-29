/*
 * Copyright 2016 HM Revenue & Customs
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
import models.{Address, GroupAccount, IndividualDetails}
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import utils._

class IdentityVerificationSpec extends ControllerSpec {

  private object TestIdentityVerification extends IdentityVerification {
    override val individuals = StubIndividualAccountConnector
    override val groups = StubGroupAccountConnector
    override val userDetails = StubUserDetails
    override val auth = StubAuthConnector
    override val ggAction = StubGGAction
    override val identityVerification = StubIdentityVerification
    override val keystore = StubKeystore
  }

  val request = FakeRequest()
  private def requestWithJourneyId(id: String) = request.withSession("journeyId" -> id)
  StubKeystore.stubIndividualDetails(IndividualDetails("fname", "lname", "aa@aa.aa", "123", None))

  "Successfully verifying identity when the group does not have a CCA account" must
    "display the successful iv confirmation page, and not create an individual account" in {
    StubUserDetails.stubGroupId("groupwithoutaccount")
    StubIdentityVerification.stubSuccessfulJourney("successfuljourney")

    val res = TestIdentityVerification.withRestoredSession()(requestWithJourneyId("successfuljourney"))
    status(res) mustBe OK

    val content = contentAsString(res)
    val html = Jsoup.parse(content)
    html.select("h1.heading-confirmation span.tick").html must equal ("✔") withClue "Page did not contain success summary"
    html.select("a.button[href=/property-linking/create-group]").size must equal (1) withClue "Page did not contain link to create group account"

    implicit val hc = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))

    await(StubIndividualAccountConnector.get()) mustBe Nil
  }

  "Successfully verifying identity when the group does have a CCA account" must "display a confirmation page, and create the individual account" in {
    StubAuthConnector.stubInternalId("individualwithoutaccount")
    StubUserDetails.stubGroupId("groupwithaccount")
    StubGroupAccountConnector.stubAccount(GroupAccount("groupwithaccount", "", Address("", "", "", ""), "", "", false, None))
    StubIdentityVerification.stubSuccessfulJourney("anothersuccess")

    val res = TestIdentityVerification.withRestoredSession()(requestWithJourneyId("anothersuccess"))
    status(res) mustBe OK

    val html = Jsoup.parse(contentAsString(res))
    html.select("h1.heading-confirmation span.tick").html must equal ("✔") withClue "Page did not contain success summary"
    html.select("a.button[href=/property-linking/home").size must equal (1) withClue "Page did not contain dashboard link"

    implicit val hc = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))

    await(StubIndividualAccountConnector.get("individualwithoutaccount")) must not be None
  }

  "Manually navigating to the iv success page after failing identity verification" must "return a 401 Unauthorised response" in {
    StubIdentityVerification.stubFailedJourney("somejourneyid")

    val res = TestIdentityVerification.withRestoredSession()(request.withSession("journey-id" -> "somejourneyid"))
    status(res) mustBe UNAUTHORIZED
  }
}
