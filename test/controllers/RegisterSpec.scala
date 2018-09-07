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

import org.scalatest.Matchers.convertToAnyShouldWrapper
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.{header, status}
import play.api.test.Helpers._
import utils.StubGgAction

class RegisterSpec extends VoaPropertyLinkingSpec {

  implicit val request = FakeRequest()

  object TestRegister extends Register(StubGgAction)

  "continue" should "return the correct map including accountType" in {
    val testAccountType = "testAccountType"
    TestRegister.continue(testAccountType) shouldBe  Map("accountType" -> Seq(testAccountType), "continue" -> Seq(routes.Dashboard.home().url), "origin" -> Seq("voa"))
  }

  "show" should "redirect to correct location and have the correct query string parameter" in {
    val eventualResult = TestRegister.show()(request)

    status(eventualResult) mustBe SEE_OTHER
    header("location", eventualResult).getOrElse("").endsWith("/government-gateway-registration-frontend?accountType=organisation&continue=%2Fbusiness-rates-property-linking%2Fhome&origin=voa") shouldBe true
  }


}
