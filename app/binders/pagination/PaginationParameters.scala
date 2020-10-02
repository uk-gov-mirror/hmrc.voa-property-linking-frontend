/*
 * Copyright 2020 HM Revenue & Customs
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

package binders.pagination

import binders.validation.ValidationUtils
import binders.{Params, ValidationResult}
import play.api.Logger
import play.api.mvc.QueryStringBindable
import utils.Cats
import utils.QueryParamUtils.toQueryString

case class PaginationParameters(
      startPoint: Int = 1,
      pageSize: Int = 15,
      requestTotalRowCount: Boolean = true
) {
  def previous = this.copy(startPoint = startPoint - pageSize)
  def next = this.copy(startPoint = startPoint + pageSize)

  override val toString = s"startPoint=$startPoint&pageSize=$pageSize&requestTotalRowCount=$requestTotalRowCount"

}

object PaginationParameters {
  implicit def queryStringBindable(
        implicit intBinder: QueryStringBindable[Int],
        booleanBinder: QueryStringBindable[Boolean]): QueryStringBindable[PaginationParameters] =
    new QueryStringBindable[PaginationParameters] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, PaginationParameters]] =
        for {
          startPoint           <- intBinder.bind("startPoint", params)
          pageSize             <- intBinder.bind("pageSize", params)
          requestTotalRowCount <- booleanBinder.bind("requestTotalRowCount", params)
        } yield {
          (startPoint, pageSize, requestTotalRowCount) match {
            case (Right(sp), Right(ps), Right(rtrc)) => Right(PaginationParameters(sp, ps, rtrc))
            case _                                   => Left("Unable to bind PaginationParams")
          }
        }

      override def unbind(key: String, value: PaginationParameters): String = s"$value"
    }
}
