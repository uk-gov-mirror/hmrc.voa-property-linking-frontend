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

package controllers.test

import java.time.Instant
import java.util.UUID
import javax.inject.Inject

import actions.AuthenticatedAction
import connectors.{ExternalId, GroupAccounts, IndividualAccounts}
import controllers.PropertyLinkingController
import models.{GroupAccount, UpdatedOrganisationAccount}
import models.test.TestUserDetails
import play.api.libs.json.Json
import services.{EnrolmentService, Failure, Success}

import scala.util.Random

class TestController @Inject()(
                                authenticated: AuthenticatedAction,
                                enrolmentService: EnrolmentService,
                                individualAccounts: IndividualAccounts,
                                groups: GroupAccounts
                              ) extends PropertyLinkingController {

  def getUserDetails() = authenticated { implicit request =>
    Ok(Json.toJson(if (request.organisationAccount.isAgent) {
      TestUserDetails(
        personId = request.individualAccount.individualId,
        organisationId = request.organisationAccount.id,
        organisationName = request.organisationAccount.companyName,
        governmentGatewayGroupId = request.organisationAccount.groupId,
        governmentGatewayExternalId = request.individualAccount.externalId,
        agentCode = Some(request.organisationAccount.agentCode))
    } else {
      TestUserDetails(personId = request.individualAccount.individualId,
        organisationId = request.organisationAccount.id,
        organisationName = request.organisationAccount.companyName,
        governmentGatewayGroupId = request.organisationAccount.groupId,
        governmentGatewayExternalId = request.individualAccount.externalId,
        agentCode = None)
    }))
  }

  def deEnrol() = authenticated { implicit request =>
    enrolmentService
      .deEnrolUser(request.individualAccount.individualId)
      .map {
        case Success => Ok("Successful")
        case Failure => Ok("Failure")
      }
  }

  def delete = authenticated { implicit request =>
    val externalId = UUID.randomUUID().toString
    for {
      _ <- individualAccounts.update(request.individualAccount.copy(externalId = externalId))
      _ <- groups.update(request.organisationAccount.id, create(request.organisationAccount, externalId))
    } yield Ok("Successful")
  }

  private def create(group: GroupAccount, externalId: String): UpdatedOrganisationAccount =
    UpdatedOrganisationAccount(
      Random.nextString(40),
      group.addressId,
      group.isAgent,
      group.companyName,
      group.email,
      group.phone,
      Instant.now(),
      externalId
    )
}
