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

package auth

import config.{ApplicationConfig, VPLAuthConnector}
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.{Actions, AuthContext, GovernmentGateway}

import scala.concurrent.Future

object GGAction extends Actions {
  private def authenticatedBy = AuthenticatedBy(GovernmentGatewayProvider, GGConfidence)
  override protected def authConnector = VPLAuthConnector

  def apply(body: AuthContext => Request[AnyContent] => Result) = authenticatedBy(body)
  def async(body: AuthContext => Request[AnyContent] => Future[Result]) = authenticatedBy.async(body)
}

object GovernmentGatewayProvider extends GovernmentGateway with ServicesConfig {
  private val continueUrl = ApplicationConfig.baseUrl + controllers.routes.Dashboard.home().url
  private val ggUrl = baseUrl("government-gateway")

  override def login = s"${ApplicationConfig.ggSignInUrl}?continue=$continueUrl&accountType=organisation"
}
