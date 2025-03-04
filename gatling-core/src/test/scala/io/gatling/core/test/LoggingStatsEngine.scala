/*
 * Copyright 2011-2024 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.core.test

import java.util.concurrent.ConcurrentLinkedDeque

import io.gatling.commons.stats.Status
import io.gatling.core.session.GroupBlock
import io.gatling.core.stats.StatsEngine

import org.apache.pekko.actor.ActorRef

sealed trait StatsEngineMessage

final case class LogResponse(
    scenario: String,
    groups: List[String],
    requestName: String,
    startTimestamp: Long,
    endTimestamp: Long,
    status: Status,
    responseCode: Option[String],
    message: Option[String]
) extends StatsEngineMessage

final case class LogGroupEnd(scenario: String, group: GroupBlock, exitTimestamp: Long) extends StatsEngineMessage

final case class LogCrash(scenario: String, groups: List[String], requestName: String, error: String) extends StatsEngineMessage

class LoggingStatsEngine extends StatsEngine {
  private[test] val msgQueue = new ConcurrentLinkedDeque[Any]

  override def start(): Unit = {}

  override def stop(controller: ActorRef, exception: Option[Exception]): Unit = {}

  override def logUserStart(scenario: String): Unit = {}

  override def logUserEnd(scenario: String): Unit = {}

  override def logResponse(
      scenario: String,
      groups: List[String],
      requestName: String,
      startTimestamp: Long,
      endTimestamp: Long,
      status: Status,
      responseCode: Option[String],
      message: Option[String]
  ): Unit =
    msgQueue.addLast(LogResponse(scenario, groups, requestName, startTimestamp, endTimestamp, status, responseCode, message))

  override def logGroupEnd(scenario: String, groupBlock: GroupBlock, exitTimestamp: Long): Unit =
    msgQueue.addLast(LogGroupEnd(scenario, groupBlock, exitTimestamp))

  override def logCrash(scenario: String, groups: List[String], requestName: String, error: String): Unit =
    msgQueue.addLast(LogCrash(scenario, groups, requestName, error))
}
