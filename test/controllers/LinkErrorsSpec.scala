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

import config.ApplicationConfig
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status._
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, status}

class LinkErrorsSpec extends VoaPropertyLinkingSpec with MockitoSugar {

  implicit val request = FakeRequest()
  val mockConfig = mock[ApplicationConfig]

  object TestLinkErrors extends LinkErrors(){
    override val config = mockConfig
  }

  class TestCase {
    val controller = TestLinkErrors

    when(mockConfig.pingdomToken).thenReturn(Some("token"))
    when(mockConfig.loggedInUser).thenReturn(Some("loggedInUser"))
    when(mockConfig.isAgentLoggedIn).thenReturn(Some("true"))
    when(mockConfig.analyticsToken).thenReturn("token")
    when(mockConfig.analyticsHost).thenReturn("host")
    when(mockConfig.bannerContent).thenReturn(None)

  }

  "manualVerificationRequired" must "show manual varification required page" in new TestCase {

    val res = controller.manualVerificationRequired()(request)

    status(res) mustBe OK
    contentAsString(res) must include("We were unable to link you to the requested property. We need to manually verify your request.")
  }

  "conflict" must "show conflict page" in new TestCase {

    val res = controller.conflict()(request)

    status(res) mustBe OK
    contentAsString(res) must include("unable to add this property to your customer record at this time as it has already been linked to another customer record")
  }


}
