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

import org.testng.Assert.assertEquals
import org.testng.Assert.assertSame
import org.threeten.bp.DayOfWeek.MONDAY
import org.threeten.bp.DayOfWeek.SUNDAY
import org.threeten.bp.DayOfWeek.WEDNESDAY
import org.threeten.bp.temporal.ChronoField.DAY_OF_WEEK
import java.util.Arrays
import java.util.Locale
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.JulianFields
import org.threeten.bp.temporal.Temporal
import org.threeten.bp.temporal.TemporalAccessor
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.TemporalQueries

/**
  * Test DayOfWeek.
  */
@Test class TestDayOfWeek extends AbstractDateTimeTest {
  @BeforeMethod def setUp(): Unit = {}

  protected def samples: java.util.List[TemporalAccessor] = {
    val array: Array[TemporalAccessor] = Array(MONDAY, WEDNESDAY, SUNDAY)
    Arrays.asList(array: _*)
  }

  protected def validFields: java.util.List[TemporalField] = {
    val array: Array[TemporalField] = Array(DAY_OF_WEEK)
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
    var i: Int = 1
    while (i <= 7) {
      val test: DayOfWeek = DayOfWeek.of(i)
      assertEquals(test.getValue, i)
      assertSame(DayOfWeek.of(i), test)
      i += 1
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_int_valueTooLow(): Unit = {
    DayOfWeek.of(0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_int_valueTooHigh(): Unit = {
    DayOfWeek.of(8)
  }

  @Test def test_factory_CalendricalObject(): Unit = {
    assertEquals(DayOfWeek.from(LocalDate.of(2011, 6, 6)), DayOfWeek.MONDAY)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_CalendricalObject_invalid_noDerive(): Unit = {
    DayOfWeek.from(LocalTime.of(12, 30))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_CalendricalObject_null(): Unit = {
    DayOfWeek.from(null.asInstanceOf[TemporalAccessor])
  }

  @Test def test_get_TemporalField(): Unit = {
    assertEquals(DayOfWeek.WEDNESDAY.getLong(ChronoField.DAY_OF_WEEK), 3)
  }

  @Test def test_getLong_TemporalField(): Unit = {
    assertEquals(DayOfWeek.WEDNESDAY.getLong(ChronoField.DAY_OF_WEEK), 3)
  }

  @Test def test_query(): Unit = {
    assertEquals(DayOfWeek.FRIDAY.query(TemporalQueries.chronology), null)
    assertEquals(DayOfWeek.FRIDAY.query(TemporalQueries.localDate), null)
    assertEquals(DayOfWeek.FRIDAY.query(TemporalQueries.localTime), null)
    assertEquals(DayOfWeek.FRIDAY.query(TemporalQueries.offset), null)
    assertEquals(DayOfWeek.FRIDAY.query(TemporalQueries.precision), ChronoUnit.DAYS)
    assertEquals(DayOfWeek.FRIDAY.query(TemporalQueries.zone), null)
    assertEquals(DayOfWeek.FRIDAY.query(TemporalQueries.zoneId), null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_query_null(): Unit = {
    DayOfWeek.FRIDAY.query(null)
  }

  @Test def test_getDisplayName(): Unit = {
    assertEquals(DayOfWeek.MONDAY.getDisplayName(TextStyle.SHORT, Locale.US), "Mon")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_getDisplayName_nullStyle(): Unit = {
    DayOfWeek.MONDAY.getDisplayName(null, Locale.US)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_getDisplayName_nullLocale(): Unit = {
    DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL, null)
  }

  @DataProvider(name = "plus") private[bp] def data_plus: Array[Array[Any]] = {
    Array[Array[Any]](Array(1, -8, 7), Array(1, -7, 1), Array(1, -6, 2), Array(1, -5, 3), Array(1, -4, 4), Array(1, -3, 5), Array(1, -2, 6), Array(1, -1, 7), Array(1, 0, 1), Array(1, 1, 2), Array(1, 2, 3), Array(1, 3, 4), Array(1, 4, 5), Array(1, 5, 6), Array(1, 6, 7), Array(1, 7, 1), Array(1, 8, 2), Array(1, 1, 2), Array(2, 1, 3), Array(3, 1, 4), Array(4, 1, 5), Array(5, 1, 6), Array(6, 1, 7), Array(7, 1, 1), Array(1, -1, 7), Array(2, -1, 1), Array(3, -1, 2), Array(4, -1, 3), Array(5, -1, 4), Array(6, -1, 5), Array(7, -1, 6))
  }

  @Test(dataProvider = "plus") def test_plus_long(base: Int, amount: Long, expected: Int): Unit = {
    assertEquals(DayOfWeek.of(base).plus(amount), DayOfWeek.of(expected))
  }

  @DataProvider(name = "minus") private[bp] def data_minus: Array[Array[Any]] = {
    Array[Array[Any]](Array(1, -8, 2), Array(1, -7, 1), Array(1, -6, 7), Array(1, -5, 6), Array(1, -4, 5), Array(1, -3, 4), Array(1, -2, 3), Array(1, -1, 2), Array(1, 0, 1), Array(1, 1, 7), Array(1, 2, 6), Array(1, 3, 5), Array(1, 4, 4), Array(1, 5, 3), Array(1, 6, 2), Array(1, 7, 1), Array(1, 8, 7))
  }

  @Test(dataProvider = "minus") def test_minus_long(base: Int, amount: Long, expected: Int): Unit = {
    assertEquals(DayOfWeek.of(base).minus(amount), DayOfWeek.of(expected))
  }

  @Test def test_adjustInto(): Unit = {
    assertEquals(DayOfWeek.MONDAY.adjustInto(LocalDate.of(2012, 9, 2)), LocalDate.of(2012, 8, 27))
    assertEquals(DayOfWeek.MONDAY.adjustInto(LocalDate.of(2012, 9, 3)), LocalDate.of(2012, 9, 3))
    assertEquals(DayOfWeek.MONDAY.adjustInto(LocalDate.of(2012, 9, 4)), LocalDate.of(2012, 9, 3))
    assertEquals(DayOfWeek.MONDAY.adjustInto(LocalDate.of(2012, 9, 10)), LocalDate.of(2012, 9, 10))
    assertEquals(DayOfWeek.MONDAY.adjustInto(LocalDate.of(2012, 9, 11)), LocalDate.of(2012, 9, 10))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_adjustInto_null(): Unit = {
    DayOfWeek.MONDAY.adjustInto(null.asInstanceOf[Temporal])
  }

  @Test def test_toString(): Unit = {
    assertEquals(DayOfWeek.MONDAY.toString, "MONDAY")
    assertEquals(DayOfWeek.TUESDAY.toString, "TUESDAY")
    assertEquals(DayOfWeek.WEDNESDAY.toString, "WEDNESDAY")
    assertEquals(DayOfWeek.THURSDAY.toString, "THURSDAY")
    assertEquals(DayOfWeek.FRIDAY.toString, "FRIDAY")
    assertEquals(DayOfWeek.SATURDAY.toString, "SATURDAY")
    assertEquals(DayOfWeek.SUNDAY.toString, "SUNDAY")
  }

  @Test def test_enum(): Unit = {
    assertEquals(DayOfWeek.valueOf("MONDAY"), DayOfWeek.MONDAY)
    assertEquals(DayOfWeek.values(0), DayOfWeek.MONDAY)
  }
}