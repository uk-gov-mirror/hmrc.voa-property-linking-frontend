/*
 * Copyright 2017 HM Revenue & Customs
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

package connectors.propertyLinking

import connectors._
import models._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.JsValue
import serialization.JsonFormats._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.{ExecutionContext, Future}

class PropertyLinkConnector(http: HttpGet with HttpPut with HttpPost)(implicit ec: ExecutionContext)
  extends ServicesConfig {
  lazy val baseUrl: String = baseUrl("property-representations") + s"/property-linking"

  def get(organisationId: Int, linkId: Int)(implicit hc: HeaderCarrier): Future[Option[DetailedPropertyLink]] = {
    linkedProperties(organisationId).map( links => links.find(_.linkId == linkId) )
  }

  def linkToProperty(property: Property, organisationId: Int, individualId: Int,
                     capacityDeclaration: CapacityDeclaration, submissionId: String, basis: LinkBasis,
                     fileInfo: Option[FileInfo])
                    (implicit hc: HeaderCarrier): Future[Unit] = {
    val url = baseUrl + s"/property-links"
    val request = PropertyLinkRequest(property.uarn, organisationId, individualId, Capacity.fromDeclaration(capacityDeclaration),
      DateTime.now, basis, fileInfo.toSeq, submissionId)
    http.POST[PropertyLinkRequest, HttpResponse](url, request) map { _ => () }
  }

  def linkedProperties(organisationId: Int)(implicit hc: HeaderCarrier): Future[Seq[DetailedPropertyLink]] = {
    val url = baseUrl + s"/property-links/$organisationId"
    val tmp = http.GET[JsValue](url)
    val output = tmp.map(x => {
      Logger.info(x.toString())
      x.as[Seq[DetailedPropertyLink]]
    })
    output
  }

  def assessments(linkId: Int)(implicit hc: HeaderCarrier): Future[Seq[Assessment]] = {
    val url = baseUrl + s"/dashboard/assessments/$linkId"
    http.GET[Seq[Assessment]](url)
  }
}
