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
package java.time

import org.testng.Assert.assertEquals
import java.time.Month.DECEMBER
import java.time.Month.JANUARY
import java.time.Month.JUNE
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.util.ArrayList
import java.util.Arrays
import java.util.List
import java.util.Locale
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.time.chrono.IsoChronology
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.JulianFields
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalField
import java.time.temporal.TemporalQueries

/**
  * Test Month.
  */
@Test object TestMonth {
  private val MAX_LENGTH: Int = 12
}

@Test class TestMonth extends AbstractDateTimeTest {
  protected def samples: java.util.List[TemporalAccessor] = {
    val array: Array[TemporalAccessor] = Array(JANUARY, JUNE, DECEMBER)
    Arrays.asList(array: _*)
  }

  protected def validFields: java.util.List[TemporalField] = {
    val array: Array[TemporalField] = Array(MONTH_OF_YEAR)
    Arrays.asList(array: _*)
  }

  protected def invalidFields: java.util.List[TemporalField] = {
    val list: java.util.List[TemporalField] = new java.util.ArrayList[TemporalField](Arrays.asList[TemporalField](ChronoField.values: _*))
    list.removeAll(validFields)
    list.add(JulianFields.JULIAN_DAY)
    list.add(JulianFields.MODIFIED_JULIAN_DAY)
    list.add(JulianFields.RATA_DIE)
    list
  }

  @Test def test_factory_int_singleton(): Unit = {
    {
      var i: Int = 1
      while (i <= TestMonth.MAX_LENGTH) {
        {
          val test: Month = Month.of(i)
          assertEquals(test.getValue, i)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_int_tooLow(): Unit = {
    Month.of(0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_int_tooHigh(): Unit = {
    Month.of(13)
  }

  @Test def test_factory_CalendricalObject(): Unit = {
    assertEquals(Month.from(LocalDate.of(2011, 6, 6)), JUNE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_CalendricalObject_invalid_noDerive(): Unit = {
    Month.from(LocalTime.of(12, 30))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_CalendricalObject_null(): Unit = {
    Month.from(null.asInstanceOf[TemporalAccessor])
  }

  @Test def test_get_TemporalField(): Unit = {
    assertEquals(Month.JULY.get(ChronoField.MONTH_OF_YEAR), 7)
  }

  @Test def test_getLong_TemporalField(): Unit = {
    assertEquals(Month.JULY.getLong(ChronoField.MONTH_OF_YEAR), 7)
  }

  @Test def test_query(): Unit = {
    assertEquals(Month.JUNE.query(TemporalQueries.chronology), IsoChronology.INSTANCE)
    assertEquals(Month.JUNE.query(TemporalQueries.localDate), null)
    assertEquals(Month.JUNE.query(TemporalQueries.localTime), null)
    assertEquals(Month.JUNE.query(TemporalQueries.offset), null)
    assertEquals(Month.JUNE.query(TemporalQueries.precision), ChronoUnit.MONTHS)
    assertEquals(Month.JUNE.query(TemporalQueries.zone), null)
    assertEquals(Month.JUNE.query(TemporalQueries.zoneId), null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_query_null(): Unit = {
    Month.JUNE.query(null)
  }

  @Test def test_getDisplayName(): Unit = {
    assertEquals(Month.JANUARY.getDisplayName(TextStyle.SHORT, Locale.US), "Jan")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_getDisplayName_nullStyle(): Unit = {
    Month.JANUARY.getDisplayName(null, Locale.US)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_getDisplayName_nullLocale(): Unit = {
    Month.JANUARY.getDisplayName(TextStyle.FULL, null)
  }

  @DataProvider(name = "plus") private[time] def data_plus: Array[Array[Int]] = {
    Array[Array[Int]](Array(1, -13, 12), Array(1, -12, 1), Array(1, -11, 2), Array(1, -10, 3), Array(1, -9, 4), Array(1, -8, 5), Array(1, -7, 6), Array(1, -6, 7), Array(1, -5, 8), Array(1, -4, 9), Array(1, -3, 10), Array(1, -2, 11), Array(1, -1, 12), Array(1, 0, 1), Array(1, 1, 2), Array(1, 2, 3), Array(1, 3, 4), Array(1, 4, 5), Array(1, 5, 6), Array(1, 6, 7), Array(1, 7, 8), Array(1, 8, 9), Array(1, 9, 10), Array(1, 10, 11), Array(1, 11, 12), Array(1, 12, 1), Array(1, 13, 2), Array(1, 1, 2), Array(2, 1, 3), Array(3, 1, 4), Array(4, 1, 5), Array(5, 1, 6), Array(6, 1, 7), Array(7, 1, 8), Array(8, 1, 9), Array(9, 1, 10), Array(10, 1, 11), Array(11, 1, 12), Array(12, 1, 1), Array(1, -1, 12), Array(2, -1, 1), Array(3, -1, 2), Array(4, -1, 3), Array(5, -1, 4), Array(6, -1, 5), Array(7, -1, 6), Array(8, -1, 7), Array(9, -1, 8), Array(10, -1, 9), Array(11, -1, 10), Array(12, -1, 11))
  }

  @Test(dataProvider = "plus") def test_plus_long(base: Int, amount: Long, expected: Int): Unit = {
    assertEquals(Month.of(base).plus(amount), Month.of(expected))
  }

  @DataProvider(name = "minus") private[time] def data_minus: Array[Array[Int]] = {
    Array[Array[Int]](Array(1, -13, 2), Array(1, -12, 1), Array(1, -11, 12), Array(1, -10, 11), Array(1, -9, 10), Array(1, -8, 9), Array(1, -7, 8), Array(1, -6, 7), Array(1, -5, 6), Array(1, -4, 5), Array(1, -3, 4), Array(1, -2, 3), Array(1, -1, 2), Array(1, 0, 1), Array(1, 1, 12), Array(1, 2, 11), Array(1, 3, 10), Array(1, 4, 9), Array(1, 5, 8), Array(1, 6, 7), Array(1, 7, 6), Array(1, 8, 5), Array(1, 9, 4), Array(1, 10, 3), Array(1, 11, 2), Array(1, 12, 1), Array(1, 13, 12))
  }

  @Test(dataProvider = "minus") def test_minus_long(base: Int, amount: Long, expected: Int): Unit = {
    assertEquals(Month.of(base).minus(amount), Month.of(expected))
  }

  @Test def test_length_boolean_notLeapYear(): Unit = {
    assertEquals(Month.JANUARY.length(false), 31)
    assertEquals(Month.FEBRUARY.length(false), 28)
    assertEquals(Month.MARCH.length(false), 31)
    assertEquals(Month.APRIL.length(false), 30)
    assertEquals(Month.MAY.length(false), 31)
    assertEquals(Month.JUNE.length(false), 30)
    assertEquals(Month.JULY.length(false), 31)
    assertEquals(Month.AUGUST.length(false), 31)
    assertEquals(Month.SEPTEMBER.length(false), 30)
    assertEquals(Month.OCTOBER.length(false), 31)
    assertEquals(Month.NOVEMBER.length(false), 30)
    assertEquals(Month.DECEMBER.length(false), 31)
  }

  @Test def test_length_boolean_leapYear(): Unit = {
    assertEquals(Month.JANUARY.length(true), 31)
    assertEquals(Month.FEBRUARY.length(true), 29)
    assertEquals(Month.MARCH.length(true), 31)
    assertEquals(Month.APRIL.length(true), 30)
    assertEquals(Month.MAY.length(true), 31)
    assertEquals(Month.JUNE.length(true), 30)
    assertEquals(Month.JULY.length(true), 31)
    assertEquals(Month.AUGUST.length(true), 31)
    assertEquals(Month.SEPTEMBER.length(true), 30)
    assertEquals(Month.OCTOBER.length(true), 31)
    assertEquals(Month.NOVEMBER.length(true), 30)
    assertEquals(Month.DECEMBER.length(true), 31)
  }

  @Test def test_minLength(): Unit = {
    assertEquals(Month.JANUARY.minLength, 31)
    assertEquals(Month.FEBRUARY.minLength, 28)
    assertEquals(Month.MARCH.minLength, 31)
    assertEquals(Month.APRIL.minLength, 30)
    assertEquals(Month.MAY.minLength, 31)
    assertEquals(Month.JUNE.minLength, 30)
    assertEquals(Month.JULY.minLength, 31)
    assertEquals(Month.AUGUST.minLength, 31)
    assertEquals(Month.SEPTEMBER.minLength, 30)
    assertEquals(Month.OCTOBER.minLength, 31)
    assertEquals(Month.NOVEMBER.minLength, 30)
    assertEquals(Month.DECEMBER.minLength, 31)
  }

  @Test def test_maxLength(): Unit = {
    assertEquals(Month.JANUARY.maxLength, 31)
    assertEquals(Month.FEBRUARY.maxLength, 29)
    assertEquals(Month.MARCH.maxLength, 31)
    assertEquals(Month.APRIL.maxLength, 30)
    assertEquals(Month.MAY.maxLength, 31)
    assertEquals(Month.JUNE.maxLength, 30)
    assertEquals(Month.JULY.maxLength, 31)
    assertEquals(Month.AUGUST.maxLength, 31)
    assertEquals(Month.SEPTEMBER.maxLength, 30)
    assertEquals(Month.OCTOBER.maxLength, 31)
    assertEquals(Month.NOVEMBER.maxLength, 30)
    assertEquals(Month.DECEMBER.maxLength, 31)
  }

  @Test def test_firstDayOfYear_notLeapYear(): Unit = {
    assertEquals(Month.JANUARY.firstDayOfYear(false), 1)
    assertEquals(Month.FEBRUARY.firstDayOfYear(false), 1 + 31)
    assertEquals(Month.MARCH.firstDayOfYear(false), 1 + 31 + 28)
    assertEquals(Month.APRIL.firstDayOfYear(false), 1 + 31 + 28 + 31)
    assertEquals(Month.MAY.firstDayOfYear(false), 1 + 31 + 28 + 31 + 30)
    assertEquals(Month.JUNE.firstDayOfYear(false), 1 + 31 + 28 + 31 + 30 + 31)
    assertEquals(Month.JULY.firstDayOfYear(false), 1 + 31 + 28 + 31 + 30 + 31 + 30)
    assertEquals(Month.AUGUST.firstDayOfYear(false), 1 + 31 + 28 + 31 + 30 + 31 + 30 + 31)
    assertEquals(Month.SEPTEMBER.firstDayOfYear(false), 1 + 31 + 28 + 31 + 30 + 31 + 30 + 31 + 31)
    assertEquals(Month.OCTOBER.firstDayOfYear(false), 1 + 31 + 28 + 31 + 30 + 31 + 30 + 31 + 31 + 30)
    assertEquals(Month.NOVEMBER.firstDayOfYear(false), 1 + 31 + 28 + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31)
    assertEquals(Month.DECEMBER.firstDayOfYear(false), 1 + 31 + 28 + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31 + 30)
  }

  @Test def test_firstDayOfYear_leapYear(): Unit = {
    assertEquals(Month.JANUARY.firstDayOfYear(true), 1)
    assertEquals(Month.FEBRUARY.firstDayOfYear(true), 1 + 31)
    assertEquals(Month.MARCH.firstDayOfYear(true), 1 + 31 + 29)
    assertEquals(Month.APRIL.firstDayOfYear(true), 1 + 31 + 29 + 31)
    assertEquals(Month.MAY.firstDayOfYear(true), 1 + 31 + 29 + 31 + 30)
    assertEquals(Month.JUNE.firstDayOfYear(true), 1 + 31 + 29 + 31 + 30 + 31)
    assertEquals(Month.JULY.firstDayOfYear(true), 1 + 31 + 29 + 31 + 30 + 31 + 30)
    assertEquals(Month.AUGUST.firstDayOfYear(true), 1 + 31 + 29 + 31 + 30 + 31 + 30 + 31)
    assertEquals(Month.SEPTEMBER.firstDayOfYear(true), 1 + 31 + 29 + 31 + 30 + 31 + 30 + 31 + 31)
    assertEquals(Month.OCTOBER.firstDayOfYear(true), 1 + 31 + 29 + 31 + 30 + 31 + 30 + 31 + 31 + 30)
    assertEquals(Month.NOVEMBER.firstDayOfYear(true), 1 + 31 + 29 + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31)
    assertEquals(Month.DECEMBER.firstDayOfYear(true), 1 + 31 + 29 + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31 + 30)
  }

  @Test def test_firstMonthOfQuarter(): Unit = {
    assertEquals(Month.JANUARY.firstMonthOfQuarter, Month.JANUARY)
    assertEquals(Month.FEBRUARY.firstMonthOfQuarter, Month.JANUARY)
    assertEquals(Month.MARCH.firstMonthOfQuarter, Month.JANUARY)
    assertEquals(Month.APRIL.firstMonthOfQuarter, Month.APRIL)
    assertEquals(Month.MAY.firstMonthOfQuarter, Month.APRIL)
    assertEquals(Month.JUNE.firstMonthOfQuarter, Month.APRIL)
    assertEquals(Month.JULY.firstMonthOfQuarter, Month.JULY)
    assertEquals(Month.AUGUST.firstMonthOfQuarter, Month.JULY)
    assertEquals(Month.SEPTEMBER.firstMonthOfQuarter, Month.JULY)
    assertEquals(Month.OCTOBER.firstMonthOfQuarter, Month.OCTOBER)
    assertEquals(Month.NOVEMBER.firstMonthOfQuarter, Month.OCTOBER)
    assertEquals(Month.DECEMBER.firstMonthOfQuarter, Month.OCTOBER)
  }

  @Test def test_toString(): Unit = {
    assertEquals(Month.JANUARY.toString, "JANUARY")
    assertEquals(Month.FEBRUARY.toString, "FEBRUARY")
    assertEquals(Month.MARCH.toString, "MARCH")
    assertEquals(Month.APRIL.toString, "APRIL")
    assertEquals(Month.MAY.toString, "MAY")
    assertEquals(Month.JUNE.toString, "JUNE")
    assertEquals(Month.JULY.toString, "JULY")
    assertEquals(Month.AUGUST.toString, "AUGUST")
    assertEquals(Month.SEPTEMBER.toString, "SEPTEMBER")
    assertEquals(Month.OCTOBER.toString, "OCTOBER")
    assertEquals(Month.NOVEMBER.toString, "NOVEMBER")
    assertEquals(Month.DECEMBER.toString, "DECEMBER")
  }

  @Test def test_enum(): Unit = {
    assertEquals(Month.valueOf("JANUARY"), Month.JANUARY)
    assertEquals(Month.values(0), Month.JANUARY)
  }
}