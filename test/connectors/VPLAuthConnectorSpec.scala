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

package connectors

import controllers.VoaPropertyLinkingSpec
import models.registration.{UserDetails, UserInfo}
import play.api.libs.json.{JsValue, Json, OFormat}
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.User
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.StubServicesConfig

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class VPLAuthConnectorSpec extends VoaPropertyLinkingSpec {

  implicit val userInfoFormat = Json.format[UserInfo]
  implicit val userDetailsLinkFormat = Json.format[UserDetailsLink]
  implicit val externalIdFormat = Json.format[ExternalId]

  implicit val hc = HeaderCarrier()

  class Setup {
    val connector = new VPLAuthConnector(StubServicesConfig, mockWSHttp) {
      override val serviceUrl: String = "tst-url/"
    }
  }

  "getUserId" must "returns cred id" in new Setup {
    mockHttpGET[JsValue]("tst-url/auth/authority", Future.successful(Json.parse("""{"credId": "cred-id"}""")))

    whenReady(connector.getUserId)(_ mustBe "cred-id")
  }

//  "getUserDetails" must "returns user details" in new Setup {
//    private val userDetailsLink = UserDetailsLink("user-details-service/user", "user-id")
//    private val externalId = ExternalId("external-id")
//    private val userInfo = UserInfo(Some("firstName"), Some("lastName"), "email@email.com", Some("AA11AA"), "group-identifier", "gatewayId", Organisation, User)
//
////    mockHttpGET[JsValue]("tst-url/auth/authority", Future.successful(Json.parse("""{"ids": "user-id", "userDetailsLink": "user-details-service/user"}""")))
//    mockHttpGET[JsValue]("tst-url/auth/authority", Future.successful(Json.toJson(userDetailsLink)))
//    mockHttpGET[JsValue]("tst-url/user-id", Future.successful(Json.toJson(externalId)))
//    mockHttpGET[JsValue]("user-details-service/user", Future.successful(Json.toJson(userInfo)))
//
//    whenReady(connector.getUserDetails)(_ mustBe UserDetails(externalId.externalId, userInfo))
//  }

}
