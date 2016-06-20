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
package java.time.format

import org.scalatest.testng.TestNGSuite
import org.testng.Assert.assertEquals
import java.time.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH
import java.time.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR
import java.time.temporal.ChronoField.ALIGNED_WEEK_OF_MONTH
import java.time.temporal.ChronoField.ALIGNED_WEEK_OF_YEAR
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.DAY_OF_WEEK
import java.time.temporal.ChronoField.DAY_OF_YEAR
import java.time.temporal.ChronoField.EPOCH_DAY
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.time.temporal.ChronoField.PROLEPTIC_MONTH
import java.time.temporal.ChronoField.YEAR
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.chrono.IsoChronology
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalField
import java.time.temporal.TemporalQuery

/** Test. */
object TestDateTimeBuilderCombinations {
  private val PARIS: ZoneId = ZoneId.of("Europe/Paris")

  private val localDateFrom = new TemporalQuery[LocalDate]() {
    def queryFrom(temporal: TemporalAccessor): LocalDate = LocalDate.from(temporal)
  }
}

class TestDateTimeBuilderCombinations extends TestNGSuite {
  import TestDateTimeBuilderCombinations.localDateFrom

  @DataProvider(name = "combine") private[format] def data_combine: Array[Array[Any]] = {
    Array[Array[Any]](Array(YEAR, 2012, MONTH_OF_YEAR, 6, DAY_OF_MONTH, 3, null, null, localDateFrom, LocalDate.of(2012, 6, 3)), Array(PROLEPTIC_MONTH, 2012 * 12 + 6 - 1, DAY_OF_MONTH, 3, null, null, null, null, localDateFrom, LocalDate.of(2012, 6, 3)), Array(YEAR, 2012, ALIGNED_WEEK_OF_YEAR, 6, DAY_OF_WEEK, 3, null, null, localDateFrom, LocalDate.of(2012, 2, 8)), Array(YEAR, 2012, DAY_OF_YEAR, 155, null, null, null, null, localDateFrom, LocalDate.of(2012, 6, 3)), Array(EPOCH_DAY, 12, null, null, null, null, null, null, localDateFrom, LocalDate.of(1970, 1, 13)))
  }

  @Test(dataProvider = "combine") def test_derive(field1: TemporalField, value1: Number, field2: TemporalField, value2: Number, field3: TemporalField, value3: Number, field4: TemporalField, value4: Number, query: TemporalQuery[Any], expectedVal: AnyRef): Unit = {
    val builder: DateTimeBuilder = new DateTimeBuilder(field1, value1.longValue)
    builder.chrono = IsoChronology.INSTANCE
    if (field2 != null) {
      builder.addFieldValue(field2, value2.longValue)
    }
    if (field3 != null) {
      builder.addFieldValue(field3, value3.longValue)
    }
    if (field4 != null) {
      builder.addFieldValue(field4, value4.longValue)
    }
    builder.resolve(ResolverStyle.SMART, null)
    assertEquals(builder.build(query), expectedVal)
  }

  @DataProvider(name = "normalized") private[format] def data_normalized: Array[Array[Any]] = {
    Array[Array[Any]](Array(YEAR, 2127, null, null, null, null, YEAR, 2127), Array(MONTH_OF_YEAR, 12, null, null, null, null, MONTH_OF_YEAR, 12), Array(DAY_OF_YEAR, 127, null, null, null, null, DAY_OF_YEAR, 127), Array(DAY_OF_MONTH, 23, null, null, null, null, DAY_OF_MONTH, 23), Array(DAY_OF_WEEK, 127, null, null, null, null, DAY_OF_WEEK, 127L), Array(ALIGNED_WEEK_OF_YEAR, 23, null, null, null, null, ALIGNED_WEEK_OF_YEAR, 23), Array(ALIGNED_DAY_OF_WEEK_IN_YEAR, 4, null, null, null, null, ALIGNED_DAY_OF_WEEK_IN_YEAR, 4L), Array(ALIGNED_WEEK_OF_MONTH, 4, null, null, null, null, ALIGNED_WEEK_OF_MONTH, 4), Array(ALIGNED_DAY_OF_WEEK_IN_MONTH, 3, null, null, null, null, ALIGNED_DAY_OF_WEEK_IN_MONTH, 3), Array(PROLEPTIC_MONTH, 15, null, null, null, null, PROLEPTIC_MONTH, null), Array(PROLEPTIC_MONTH, 1971 * 12 + 4 - 1, null, null, null, null, YEAR, 1971), Array(PROLEPTIC_MONTH, 1971 * 12 + 4 - 1, null, null, null, null, MONTH_OF_YEAR, 4))
  }

  @Test(dataProvider = "normalized") def test_normalized(field1: TemporalField, value1: Number, field2: TemporalField, value2: Number, field3: TemporalField, value3: Number, query: TemporalField, expectedVal: Number): Unit = {
    val builder: DateTimeBuilder = new DateTimeBuilder(field1, value1.longValue)
    builder.chrono = IsoChronology.INSTANCE
    if (field2 != null) {
      builder.addFieldValue(field2, value2.longValue)
    }
    if (field3 != null) {
      builder.addFieldValue(field3, value3.longValue)
    }
    builder.resolve(ResolverStyle.SMART, null)
    if (expectedVal != null) {
      assertEquals(builder.getLong(query), expectedVal.longValue)
    }
    else {
      assertEquals(builder.isSupported(query), false)
    }
  }

  @Test def test_parse_ZDT_withZone(): Unit = {
    val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(TestDateTimeBuilderCombinations.PARIS)
    val acc: TemporalAccessor = fmt.parse("2014-06-30 01:02:03")
    assertEquals(ZonedDateTime.from(acc), ZonedDateTime.of(2014, 6, 30, 1, 2, 3, 0, TestDateTimeBuilderCombinations.PARIS))
  }

  @Test def test_parse_Instant_withZone(): Unit = {
    val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(TestDateTimeBuilderCombinations.PARIS)
    val acc: TemporalAccessor = fmt.parse("2014-06-30 01:02:03")
    assertEquals(Instant.from(acc), ZonedDateTime.of(2014, 6, 30, 1, 2, 3, 0, TestDateTimeBuilderCombinations.PARIS).toInstant)
  }
}
