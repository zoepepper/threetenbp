/*
 * Copyright (c) 2007-present, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.threeten.bp

import org.threeten.bp.DayOfWeek.MONDAY
import org.threeten.bp.DayOfWeek.TUESDAY
import org.threeten.bp.Month.AUGUST
import org.threeten.bp.Month.FEBRUARY
import org.threeten.bp.Month.MARCH
import org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH
import org.threeten.bp.temporal.ChronoUnit.DAYS
import org.threeten.bp.temporal.ChronoUnit.HOURS
import org.threeten.bp.temporal.ChronoUnit.MINUTES
import org.threeten.bp.temporal.TemporalAdjusters.dayOfWeekInMonth
import org.threeten.bp.temporal.TemporalAdjusters.firstInMonth
import org.threeten.bp.temporal.TemporalAdjusters.lastDayOfMonth
import org.threeten.bp.temporal.TemporalAdjusters.next
import org.threeten.bp.temporal.TemporalAdjusters.nextOrSame
import org.threeten.bp.zone.ZoneOffsetTransition

/** Test the fluency of the whole API. */
object FluentAPIChecker {
  @SuppressWarnings(Array("unused")) def main(args: Array[String]): Unit = {
    val clock: Clock = Clock.systemDefaultZone
    val tod: LocalTime = LocalTime.now(clock)
    tod.plusHours(6).plusMinutes(2)
    tod.plus(6, HOURS).plus(2, MINUTES)
    var date: LocalDate = null
    date = LocalDate.now(clock).plusDays(3)
    date = LocalDate.now(clock).plus(3, DAYS)
    date = LocalDate.now(Clock.systemDefaultZone).plus(3, DAYS)
    date = LocalDate.of(2007, 3, 20)
    date = LocalDate.of(2007, MARCH, 20)
    date = Year.of(2007).atMonth(3).atDay(20)
    date = Year.of(2007).atMonth(MARCH).atDay(20)
    date = date.`with`(lastDayOfMonth)
    date = date.`with`(next(MONDAY))
    date = date.`with`(nextOrSame(MONDAY))
    date = date.`with`(dayOfWeekInMonth(2, TUESDAY))
    date = date.`with`(firstInMonth(MONDAY))
    date = date.`with`(Year.of(2009))
    date = date.`with`(Month.of(6))
    date = date.`with`(AUGUST)
    val d2: Period = Period.ofDays(3)
    System.out.println(d2)
    tod.withHour(12).withMinute(30)
    var md: MonthDay = MonthDay.of(FEBRUARY, 4)
    md = md.`with`(MARCH)
    DAY_OF_MONTH.range.getMaximum
    date.getMonth.maxLength
    date.range(DAY_OF_MONTH).getMaximum
    FEBRUARY.maxLength
    var dow: DayOfWeek = MONDAY
    dow = dow.plus(1)
    val offset: ZoneOffset = ZoneOffset.ofHours(1)
    val paris: ZoneId = ZoneId.of("Europe/Paris")
    import scala.collection.JavaConversions._
    for (trans <- paris.getRules.getTransitions) {
      System.out.println("Paris transition: " + trans)
    }
    System.out.println("Summer time Paris starts: " + paris.getRules.getTransitionRules.get(0))
    System.out.println("Summer time Paris ends: " + paris.getRules.getTransitionRules.get(1))
    val ldt: LocalDateTime = date.atTime(tod)
    val zdt1: ZonedDateTime = date.atStartOfDay(paris)
    val zdt2: ZonedDateTime = date.atTime(12, 0).atZone(paris)

    {
      val year: Year = Year.of(2002)
      val sixNationsMonth: YearMonth = year.atMonth(FEBRUARY)
      val englandWales: LocalDate = sixNationsMonth.atDay(12)
      val engWal: LocalDate = Year.of(2009).atMonth(FEBRUARY).atDay(12)
    }
    val tickingClock: Clock = Clock.tickSeconds(paris)

    var i: Int = 0
    while (i < 20) {
      System.out.println(LocalTime.now(tickingClock))
      try Thread.sleep(500)
      catch {
        case ex: InterruptedException =>
      }
      i += 1
    }
  }
}