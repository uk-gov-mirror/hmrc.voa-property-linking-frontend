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
import play.api.test.Helpers.{redirectLocation, status}

class LoginSpec extends VoaPropertyLinkingSpec with MockitoSugar{

  implicit val request = FakeRequest()
  val mockConfig = mock[ApplicationConfig]

  object TestLogin extends Login(mockConfig)

  "show" must "redirect to login page" in {
    when(mockConfig.ggSignInUrl).thenReturn("test-url")
    when(mockConfig.ggContinueUrl).thenReturn("continue-url")

    val res = TestLogin.show()(request)

    status(res) mustBe SEE_OTHER
    redirectLocation(res) mustBe Some("test-url?continue=continue-url&origin=voa")
  }

}
