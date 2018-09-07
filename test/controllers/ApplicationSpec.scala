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
import junit.framework.TestCase
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status._
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.{contentAsString, status}
import utils.StubGgAction

class ApplicationSpec extends VoaPropertyLinkingSpec with MockitoSugar {

  implicit val request = FakeRequest()
  val mockConfig = mock[ApplicationConfig]

  object TestApplication extends Application(StubGgAction)(messageApi,mockConfig)

  class TestCase {
    val controller = TestApplication

    when(mockConfig.pingdomToken).thenReturn(Some("token"))
    when(mockConfig.loggedInUser).thenReturn(Some("loggedInUser"))
    when(mockConfig.isAgentLoggedIn).thenReturn(Some("true"))
    when(mockConfig.analyticsToken).thenReturn("token")
    when(mockConfig.analyticsHost).thenReturn("host")
    when(mockConfig.bannerContent).thenReturn(None)

  }

  "addUserToGG" must "show add user to GG page" in new TestCase {
    val res = controller.addUserToGG()(request)

    status(res) mustBe OK
    contentAsString(res) must include("Add a user")
    contentAsString(res) must include(routes.Application.manageBusinessTaxAccount.url)
  }

  "manageBusinessTaxAccount" must "redirect to business tax account url" in new TestCase {

    when(mockConfig.businessTaxAccountUrl(anyString())).thenReturn("test-url")

    val res = controller.manageBusinessTaxAccount()(request)

    status(res) mustBe SEE_OTHER
    Helpers.redirectLocation(res) mustBe Some("test-url")
  }

//  "start" must "show start page" in new TestCase {
//
//    val res = controller.start()(request)
//
//    status(res) mustBe OK
//    contentAsString(res) must include("Register to use this service")
//  }

  "logOut" must "redirect to start url" in new TestCase {
    val res = controller.logOut()(request)

    status(res) mustBe SEE_OTHER
    Helpers.redirectLocation(res) mustBe Some(routes.Application.start.url)
  }

  "contactUs" must "show contact us page" in new TestCase {
    val res = controller.contactUs()(request)

    status(res) mustBe OK
    contentAsString(res) must include("We’re unable to continue with this request")
    contentAsString(res) must include("Please contact us")
  }

  "invalidAccountType" must "show invalid account type page" in new TestCase {
    val res = controller.invalidAccountType()(request)

    status(res) mustBe UNAUTHORIZED
    contentAsString(res) must include("You can’t use that Government Gateway account to register for this service")
  }

}
