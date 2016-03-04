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
import org.testng.Assert.assertFalse
import org.testng.Assert.assertNotNull
import org.testng.Assert.assertSame
import org.testng.Assert.assertTrue
import org.threeten.bp.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH
import org.threeten.bp.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR
import org.threeten.bp.temporal.ChronoField.ALIGNED_WEEK_OF_MONTH
import org.threeten.bp.temporal.ChronoField.ALIGNED_WEEK_OF_YEAR
import org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH
import org.threeten.bp.temporal.ChronoField.DAY_OF_WEEK
import org.threeten.bp.temporal.ChronoField.DAY_OF_YEAR
import org.threeten.bp.temporal.ChronoField.EPOCH_DAY
import org.threeten.bp.temporal.ChronoField.ERA
import org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR
import org.threeten.bp.temporal.ChronoField.PROLEPTIC_MONTH
import org.threeten.bp.temporal.ChronoField.YEAR
import org.threeten.bp.temporal.ChronoField.YEAR_OF_ERA
import org.threeten.bp.temporal.ChronoUnit.CENTURIES
import org.threeten.bp.temporal.ChronoUnit.DAYS
import org.threeten.bp.temporal.ChronoUnit.DECADES
import org.threeten.bp.temporal.ChronoUnit.MILLENNIA
import org.threeten.bp.temporal.ChronoUnit.MONTHS
import org.threeten.bp.temporal.ChronoUnit.WEEKS
import org.threeten.bp.temporal.ChronoUnit.YEARS
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.ArrayList
import java.util.Arrays
import java.util.List
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.JulianFields
import org.threeten.bp.temporal.MockFieldNoValue
import org.threeten.bp.temporal.Temporal
import org.threeten.bp.temporal.TemporalAccessor
import org.threeten.bp.temporal.TemporalAdjuster
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.TemporalQueries
import org.threeten.bp.temporal.TemporalUnit

/**
  * Test LocalDate.
  */
@Test object TestLocalDate {
  private val OFFSET_PONE: ZoneOffset = ZoneOffset.ofHours(1)
  private val ZONE_PARIS: ZoneId = ZoneId.of("Europe/Paris")
  private val ZONE_GAZA: ZoneId = ZoneId.of("Asia/Gaza")
}

@Test class TestLocalDate extends AbstractDateTimeTest {
  private var TEST_2007_07_15: LocalDate = null
  private var MAX_VALID_EPOCHDAYS: Long = 0L
  private var MIN_VALID_EPOCHDAYS: Long = 0L
  private var MAX_DATE: LocalDate = null
  private var MIN_DATE: LocalDate = null
  private var MAX_INSTANT: Instant = null
  private var MIN_INSTANT: Instant = null

  @BeforeMethod def setUp(): Unit = {
    TEST_2007_07_15 = LocalDate.of(2007, 7, 15)
    val max: LocalDate = LocalDate.MAX
    val min: LocalDate = LocalDate.MIN
    MAX_VALID_EPOCHDAYS = max.toEpochDay
    MIN_VALID_EPOCHDAYS = min.toEpochDay
    MAX_DATE = max
    MIN_DATE = min
    MAX_INSTANT = max.atStartOfDay(ZoneOffset.UTC).toInstant
    MIN_INSTANT = min.atStartOfDay(ZoneOffset.UTC).toInstant
  }

  protected def samples: java.util.List[TemporalAccessor] = {
    val array: Array[TemporalAccessor] = Array(TEST_2007_07_15, LocalDate.MAX, LocalDate.MIN)
    Arrays.asList(array: _*)
  }

  protected def validFields: java.util.List[TemporalField] = {
    val array: Array[TemporalField] = Array(DAY_OF_WEEK, ALIGNED_DAY_OF_WEEK_IN_MONTH, ALIGNED_DAY_OF_WEEK_IN_YEAR, DAY_OF_MONTH, DAY_OF_YEAR, EPOCH_DAY, ALIGNED_WEEK_OF_MONTH, ALIGNED_WEEK_OF_YEAR, MONTH_OF_YEAR, PROLEPTIC_MONTH, YEAR_OF_ERA, YEAR, ERA, JulianFields.JULIAN_DAY, JulianFields.MODIFIED_JULIAN_DAY, JulianFields.RATA_DIE)
    Arrays.asList(array: _*)
  }

  protected def invalidFields: java.util.List[TemporalField] = {
    val list: java.util.List[TemporalField] = new java.util.ArrayList[TemporalField](Arrays.asList[TemporalField](ChronoField.values: _*))
    list.removeAll(validFields)
    list
  }

  @Test
  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def test_serialization(): Unit = {
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    val oos: ObjectOutputStream = new ObjectOutputStream(baos)
    oos.writeObject(TEST_2007_07_15)
    oos.close()
    val ois: ObjectInputStream = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray))
    assertEquals(ois.readObject, TEST_2007_07_15)
  }

  @Test def test_immutable(): Unit = {
    val cls: Class[LocalDate] = classOf[LocalDate]
    assertTrue(Modifier.isPublic(cls.getModifiers))
    assertTrue(Modifier.isFinal(cls.getModifiers))
    val fields: Array[Field] = cls.getDeclaredFields
    for (field <- fields) {
      if (!field.getName.contains("$")) {
        if (Modifier.isStatic(field.getModifiers)) {
          assertTrue(Modifier.isFinal(field.getModifiers), "Field:" + field.getName)
        }
        else {
          assertTrue(Modifier.isPrivate(field.getModifiers), "Field:" + field.getName)
          assertTrue(Modifier.isFinal(field.getModifiers), "Field:" + field.getName)
        }
      }
    }
  }

  private def check(test_2008_02_29: LocalDate, y: Int, m: Int, d: Int): Unit = {
    assertEquals(test_2008_02_29.getYear, y)
    assertEquals(test_2008_02_29.getMonth.getValue, m)
    assertEquals(test_2008_02_29.getDayOfMonth, d)
  }

  @Test def now(): Unit = {
    var expected: LocalDate = LocalDate.now(Clock.systemDefaultZone)
    var test: LocalDate = LocalDate.now

    {
      var i: Int = 0
      while (i < 100) {
        {
          if (expected == test) {
            return
          }
          expected = LocalDate.now(Clock.systemDefaultZone)
          test = LocalDate.now
        }
        {
          i += 1
          i - 1
        }
      }
    }
    assertEquals(test, expected)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_ZoneId_nullZoneId(): Unit = {
    LocalDate.now(null.asInstanceOf[ZoneId])
  }

  @Test def now_ZoneId(): Unit = {
    val zone: ZoneId = ZoneId.of("UTC+01:02:03")
    var expected: LocalDate = LocalDate.now(Clock.system(zone))
    var test: LocalDate = LocalDate.now(zone)

    {
      var i: Int = 0
      while (i < 100) {
        {
          if (expected == test) {
            return
          }
          expected = LocalDate.now(Clock.system(zone))
          test = LocalDate.now(zone)
        }
        {
          i += 1
          i - 1
        }
      }
    }
    assertEquals(test, expected)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_Clock_nullClock(): Unit = {
    LocalDate.now(null.asInstanceOf[Clock])
  }

  @Test def now_Clock_allSecsInDay_utc(): Unit = {
    {
      var i: Int = 0
      while (i < (2 * 24 * 60 * 60)) {
        {
          val instant: Instant = Instant.ofEpochSecond(i)
          val clock: Clock = Clock.fixed(instant, ZoneOffset.UTC)
          val test: LocalDate = LocalDate.now(clock)
          assertEquals(test.getYear, 1970)
          assertEquals(test.getMonth, Month.JANUARY)
          assertEquals(test.getDayOfMonth, if (i < 24 * 60 * 60) 1 else 2)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def now_Clock_allSecsInDay_offset(): Unit = {
    {
      var i: Int = 0
      while (i < (2 * 24 * 60 * 60)) {
        {
          val instant: Instant = Instant.ofEpochSecond(i)
          val clock: Clock = Clock.fixed(instant.minusSeconds(TestLocalDate.OFFSET_PONE.getTotalSeconds), TestLocalDate.OFFSET_PONE)
          val test: LocalDate = LocalDate.now(clock)
          assertEquals(test.getYear, 1970)
          assertEquals(test.getMonth, Month.JANUARY)
          assertEquals(test.getDayOfMonth, if (i < 24 * 60 * 60) 1 else 2)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def now_Clock_allSecsInDay_beforeEpoch(): Unit = {
    {
      var i: Int = -1
      while (i >= -(2 * 24 * 60 * 60)) {
        {
          val instant: Instant = Instant.ofEpochSecond(i)
          val clock: Clock = Clock.fixed(instant, ZoneOffset.UTC)
          val test: LocalDate = LocalDate.now(clock)
          assertEquals(test.getYear, 1969)
          assertEquals(test.getMonth, Month.DECEMBER)
          assertEquals(test.getDayOfMonth, if (i >= -24 * 60 * 60) 31 else 30)
        }
        {
          i -= 1
          i + 1
        }
      }
    }
  }

  @Test def now_Clock_maxYear(): Unit = {
    val clock: Clock = Clock.fixed(MAX_INSTANT, ZoneOffset.UTC)
    val test: LocalDate = LocalDate.now(clock)
    assertEquals(test, MAX_DATE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def now_Clock_tooBig(): Unit = {
    val clock: Clock = Clock.fixed(MAX_INSTANT.plusSeconds(24 * 60 * 60), ZoneOffset.UTC)
    LocalDate.now(clock)
  }

  @Test def now_Clock_minYear(): Unit = {
    val clock: Clock = Clock.fixed(MIN_INSTANT, ZoneOffset.UTC)
    val test: LocalDate = LocalDate.now(clock)
    assertEquals(test, MIN_DATE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def now_Clock_tooLow(): Unit = {
    val clock: Clock = Clock.fixed(MIN_INSTANT.minusNanos(1), ZoneOffset.UTC)
    LocalDate.now(clock)
  }

  @Test def factory_of_intsMonth(): Unit = {
    assertEquals(TEST_2007_07_15, LocalDate.of(2007, Month.JULY, 15))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_intsMonth_29febNonLeap(): Unit = {
    LocalDate.of(2007, Month.FEBRUARY, 29)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_intsMonth_31apr(): Unit = {
    LocalDate.of(2007, Month.APRIL, 31)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_intsMonth_dayTooLow(): Unit = {
    LocalDate.of(2007, Month.JANUARY, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_intsMonth_dayTooHigh(): Unit = {
    LocalDate.of(2007, Month.JANUARY, 32)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_of_intsMonth_nullMonth(): Unit = {
    LocalDate.of(2007, null, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_intsMonth_yearTooLow(): Unit = {
    LocalDate.of(Integer.MIN_VALUE, Month.JANUARY, 1)
  }

  @Test def factory_of_ints(): Unit = {
    check(TEST_2007_07_15, 2007, 7, 15)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_ints_29febNonLeap(): Unit = {
    LocalDate.of(2007, 2, 29)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_ints_31apr(): Unit = {
    LocalDate.of(2007, 4, 31)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_ints_dayTooLow(): Unit = {
    LocalDate.of(2007, 1, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_ints_dayTooHigh(): Unit = {
    LocalDate.of(2007, 1, 32)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_ints_monthTooLow(): Unit = {
    LocalDate.of(2007, 0, 1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_ints_monthTooHigh(): Unit = {
    LocalDate.of(2007, 13, 1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_of_ints_yearTooLow(): Unit = {
    LocalDate.of(Integer.MIN_VALUE, 1, 1)
  }

  @Test def factory_ofYearDay_ints_nonLeap(): Unit = {
    var date: LocalDate = LocalDate.of(2007, 1, 1)

    {
      var i: Int = 1
      while (i < 365) {
        {
          assertEquals(LocalDate.ofYearDay(2007, i), date)
          date = next(date)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def factory_ofYearDay_ints_leap(): Unit = {
    var date: LocalDate = LocalDate.of(2008, 1, 1)

    {
      var i: Int = 1
      while (i < 366) {
        {
          assertEquals(LocalDate.ofYearDay(2008, i), date)
          date = next(date)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofYearDay_ints_366nonLeap(): Unit = {
    LocalDate.ofYearDay(2007, 366)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofYearDay_ints_dayTooLow(): Unit = {
    LocalDate.ofYearDay(2007, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofYearDay_ints_dayTooHigh(): Unit = {
    LocalDate.ofYearDay(2007, 367)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofYearDay_ints_yearTooLow(): Unit = {
    LocalDate.ofYearDay(Integer.MIN_VALUE, 1)
  }

  private def next(date: LocalDate): LocalDate = {
    var _date = date
    val newDayOfMonth: Int = _date.getDayOfMonth + 1
    if (newDayOfMonth <= _date.getMonth.length(AbstractTest.isIsoLeap(_date.getYear))) {
      return _date.withDayOfMonth(newDayOfMonth)
    }
    _date = _date.withDayOfMonth(1)
    if (_date.getMonth eq Month.DECEMBER) {
      _date = _date.withYear(_date.getYear + 1)
    }
    _date.`with`(_date.getMonth.plus(1))
  }

  private def previous(date: LocalDate): LocalDate = {
    var _date = date
    val newDayOfMonth: Int = _date.getDayOfMonth - 1
    if (newDayOfMonth > 0) {
      return _date.withDayOfMonth(newDayOfMonth)
    }
    _date = _date.`with`(_date.getMonth.minus(1))
    if (_date.getMonth eq Month.DECEMBER) {
      _date = _date.withYear(_date.getYear - 1)
    }
    _date.withDayOfMonth(_date.getMonth.length(AbstractTest.isIsoLeap(_date.getYear)))
  }

  @Test def factory_ofEpochDay(): Unit = {
    val date_0000_01_01: Long = -678941 - 40587
    assertEquals(LocalDate.ofEpochDay(0), LocalDate.of(1970, 1, 1))
    assertEquals(LocalDate.ofEpochDay(date_0000_01_01), LocalDate.of(0, 1, 1))
    assertEquals(LocalDate.ofEpochDay(date_0000_01_01 - 1), LocalDate.of(-1, 12, 31))
    assertEquals(LocalDate.ofEpochDay(MAX_VALID_EPOCHDAYS), LocalDate.of(Year.MAX_VALUE, 12, 31))
    assertEquals(LocalDate.ofEpochDay(MIN_VALID_EPOCHDAYS), LocalDate.of(Year.MIN_VALUE, 1, 1))
    var test: LocalDate = LocalDate.of(0, 1, 1)
    
    {
      var i: Long = date_0000_01_01
      while (i < 700000) {
        {
          assertEquals(LocalDate.ofEpochDay(i), test)
          test = next(test)
        }
        {
          i += 1
          i - 1
        }
      }
    }
    test = LocalDate.of(0, 1, 1)
    
    {
      var i: Long = date_0000_01_01
      while (i > -2000000) {
        {
          assertEquals(LocalDate.ofEpochDay(i), test)
          test = previous(test)
        }
        {
          i -= 1
          i + 1
        }
      }
    }
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofEpochDay_aboveMax(): Unit = {
    LocalDate.ofEpochDay(MAX_VALID_EPOCHDAYS + 1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_ofEpochDay_belowMin(): Unit = {
    LocalDate.ofEpochDay(MIN_VALID_EPOCHDAYS - 1)
  }

  @Test def test_factory_CalendricalObject(): Unit = {
    assertEquals(LocalDate.from(LocalDate.of(2007, 7, 15)), LocalDate.of(2007, 7, 15))
    assertEquals(LocalDate.from(LocalDateTime.of(2007, 7, 15, 12, 30)), LocalDate.of(2007, 7, 15))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_factory_CalendricalObject_invalid_noDerive(): Unit = {
    LocalDate.from(LocalTime.of(12, 30))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_factory_CalendricalObject_null(): Unit = {
    LocalDate.from(null.asInstanceOf[TemporalAccessor])
  }

  @Test(dataProvider = "sampleToString") def factory_parse_validText(y: Int, m: Int, d: Int, parsable: String): Unit = {
    val t: LocalDate = LocalDate.parse(parsable)
    assertNotNull(t, parsable)
    assertEquals(t.getYear, y, parsable)
    assertEquals(t.getMonth.getValue, m, parsable)
    assertEquals(t.getDayOfMonth, d, parsable)
  }

  @DataProvider(name = "sampleBadParse") private[bp] def provider_sampleBadParse: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("2008/07/05"), Array("10000-01-01"), Array("2008-1-1"), Array("2008--01"), Array("ABCD-02-01"), Array("2008-AB-01"), Array("2008-02-AB"), Array("-0000-02-01"), Array("2008-02-01Z"), Array("2008-02-01+01:00"), Array("2008-02-01+01:00[Europe/Paris]"))
  }

  @Test(dataProvider = "sampleBadParse", expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_invalidText(unparsable: String): Unit = {
    LocalDate.parse(unparsable)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_illegalValue(): Unit = {
    LocalDate.parse("2008-06-32")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parse_invalidValue(): Unit = {
    LocalDate.parse("2008-06-31")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_nullText(): Unit = {
    LocalDate.parse(null.asInstanceOf[String])
  }

  @Test def factory_parse_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("u M d")
    val test: LocalDate = LocalDate.parse("2010 12 3", f)
    assertEquals(test, LocalDate.of(2010, 12, 3))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullText(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("u M d")
    LocalDate.parse(null.asInstanceOf[String], f)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_formatter_nullFormatter(): Unit = {
    LocalDate.parse("ANY", null)
  }

  @Test def test_get_TemporalField(): Unit = {
    val test: LocalDate = LocalDate.of(2008, 6, 30)
    assertEquals(test.get(YEAR), 2008)
    assertEquals(test.get(MONTH_OF_YEAR), 6)
    assertEquals(test.get(DAY_OF_MONTH), 30)
    assertEquals(test.get(DAY_OF_WEEK), 1)
    assertEquals(test.get(DAY_OF_YEAR), 182)
    assertEquals(test.get(YEAR_OF_ERA), 2008)
    assertEquals(test.get(ERA), 1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_TemporalField_tooBig(): Unit = {
    TEST_2007_07_15.get(EPOCH_DAY)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_get_TemporalField_null(): Unit = {
    TEST_2007_07_15.get(null.asInstanceOf[TemporalField])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_TemporalField_invalidField(): Unit = {
    TEST_2007_07_15.get(MockFieldNoValue.INSTANCE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_get_TemporalField_timeField(): Unit = {
    TEST_2007_07_15.get(ChronoField.AMPM_OF_DAY)
  }

  @Test def test_getLong_TemporalField(): Unit = {
    val test: LocalDate = LocalDate.of(2008, 6, 30)
    assertEquals(test.getLong(YEAR), 2008)
    assertEquals(test.getLong(MONTH_OF_YEAR), 6)
    assertEquals(test.getLong(DAY_OF_MONTH), 30)
    assertEquals(test.getLong(DAY_OF_WEEK), 1)
    assertEquals(test.getLong(DAY_OF_YEAR), 182)
    assertEquals(test.getLong(YEAR_OF_ERA), 2008)
    assertEquals(test.getLong(ERA), 1)
    assertEquals(test.getLong(PROLEPTIC_MONTH), 2008 * 12 + 6 - 1)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_getLong_TemporalField_null(): Unit = {
    TEST_2007_07_15.getLong(null.asInstanceOf[TemporalField])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_getLong_TemporalField_invalidField(): Unit = {
    TEST_2007_07_15.getLong(MockFieldNoValue.INSTANCE)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_getLong_TemporalField_timeField(): Unit = {
    TEST_2007_07_15.getLong(ChronoField.AMPM_OF_DAY)
  }

  @Test def test_query(): Unit = {
    assertEquals(TEST_2007_07_15.query(TemporalQueries.chronology), IsoChronology.INSTANCE)
    assertEquals(TEST_2007_07_15.query(TemporalQueries.localDate), TEST_2007_07_15)
    assertEquals(TEST_2007_07_15.query(TemporalQueries.localTime), null)
    assertEquals(TEST_2007_07_15.query(TemporalQueries.offset), null)
    assertEquals(TEST_2007_07_15.query(TemporalQueries.precision), ChronoUnit.DAYS)
    assertEquals(TEST_2007_07_15.query(TemporalQueries.zone), null)
    assertEquals(TEST_2007_07_15.query(TemporalQueries.zoneId), null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_query_null(): Unit = {
    TEST_2007_07_15.query(null)
  }

  @DataProvider(name = "sampleDates") private[bp] def provider_sampleDates: Array[Array[_ <: AnyRef]] = {
    Array[Array[_ <: AnyRef]](Array[Integer](2008, 7, 5), Array[Integer](2007, 7, 5), Array[Integer](2006, 7, 5), Array[Integer](2005, 7, 5), Array[Integer](2004, 1, 1), Array[Integer](-1, 1, 2))
  }

  @Test(dataProvider = "sampleDates") def test_get(y: Int, m: Int, d: Int): Unit = {
    val a: LocalDate = LocalDate.of(y, m, d)
    assertEquals(a.getYear, y)
    assertEquals(a.getMonth, Month.of(m))
    assertEquals(a.getDayOfMonth, d)
  }

  @Test(dataProvider = "sampleDates") def test_getDOY(y: Int, m: Int, d: Int): Unit = {
    val a: LocalDate = LocalDate.of(y, m, d)
    var total: Int = 0
    
    {
      var i: Int = 1
      while (i < m) {
        {
          total += Month.of(i).length(AbstractTest.isIsoLeap(y))
        }
        {
          i += 1
          i - 1
        }
      }
    }
    val doy: Int = total + d
    assertEquals(a.getDayOfYear, doy)
  }

  @Test def test_getDayOfWeek(): Unit = {
    var dow: DayOfWeek = DayOfWeek.MONDAY
    for (month <- Month.values) {
      val length: Int = month.length(false)
      
      {
        var i: Int = 1
        while (i <= length) {
          {
            val d: LocalDate = LocalDate.of(2007, month, i)
            assertSame(d.getDayOfWeek, dow)
            dow = dow.plus(1)
          }
          {
            i += 1
            i - 1
          }
        }
      }
    }
  }

  @Test def test_isLeapYear(): Unit = {
    assertEquals(LocalDate.of(1999, 1, 1).isLeapYear, false)
    assertEquals(LocalDate.of(2000, 1, 1).isLeapYear, true)
    assertEquals(LocalDate.of(2001, 1, 1).isLeapYear, false)
    assertEquals(LocalDate.of(2002, 1, 1).isLeapYear, false)
    assertEquals(LocalDate.of(2003, 1, 1).isLeapYear, false)
    assertEquals(LocalDate.of(2004, 1, 1).isLeapYear, true)
    assertEquals(LocalDate.of(2005, 1, 1).isLeapYear, false)
    assertEquals(LocalDate.of(1500, 1, 1).isLeapYear, false)
    assertEquals(LocalDate.of(1600, 1, 1).isLeapYear, true)
    assertEquals(LocalDate.of(1700, 1, 1).isLeapYear, false)
    assertEquals(LocalDate.of(1800, 1, 1).isLeapYear, false)
    assertEquals(LocalDate.of(1900, 1, 1).isLeapYear, false)
  }

  @Test def test_lengthOfMonth_notLeapYear(): Unit = {
    assertEquals(LocalDate.of(2007, 1, 1).lengthOfMonth, 31)
    assertEquals(LocalDate.of(2007, 2, 1).lengthOfMonth, 28)
    assertEquals(LocalDate.of(2007, 3, 1).lengthOfMonth, 31)
    assertEquals(LocalDate.of(2007, 4, 1).lengthOfMonth, 30)
    assertEquals(LocalDate.of(2007, 5, 1).lengthOfMonth, 31)
    assertEquals(LocalDate.of(2007, 6, 1).lengthOfMonth, 30)
    assertEquals(LocalDate.of(2007, 7, 1).lengthOfMonth, 31)
    assertEquals(LocalDate.of(2007, 8, 1).lengthOfMonth, 31)
    assertEquals(LocalDate.of(2007, 9, 1).lengthOfMonth, 30)
    assertEquals(LocalDate.of(2007, 10, 1).lengthOfMonth, 31)
    assertEquals(LocalDate.of(2007, 11, 1).lengthOfMonth, 30)
    assertEquals(LocalDate.of(2007, 12, 1).lengthOfMonth, 31)
  }

  @Test def test_lengthOfMonth_leapYear(): Unit = {
    assertEquals(LocalDate.of(2008, 1, 1).lengthOfMonth, 31)
    assertEquals(LocalDate.of(2008, 2, 1).lengthOfMonth, 29)
    assertEquals(LocalDate.of(2008, 3, 1).lengthOfMonth, 31)
    assertEquals(LocalDate.of(2008, 4, 1).lengthOfMonth, 30)
    assertEquals(LocalDate.of(2008, 5, 1).lengthOfMonth, 31)
    assertEquals(LocalDate.of(2008, 6, 1).lengthOfMonth, 30)
    assertEquals(LocalDate.of(2008, 7, 1).lengthOfMonth, 31)
    assertEquals(LocalDate.of(2008, 8, 1).lengthOfMonth, 31)
    assertEquals(LocalDate.of(2008, 9, 1).lengthOfMonth, 30)
    assertEquals(LocalDate.of(2008, 10, 1).lengthOfMonth, 31)
    assertEquals(LocalDate.of(2008, 11, 1).lengthOfMonth, 30)
    assertEquals(LocalDate.of(2008, 12, 1).lengthOfMonth, 31)
  }

  @Test def test_lengthOfYear(): Unit = {
    assertEquals(LocalDate.of(2007, 1, 1).lengthOfYear, 365)
    assertEquals(LocalDate.of(2008, 1, 1).lengthOfYear, 366)
  }

  @Test def test_with_adjustment(): Unit = {
    val sample: LocalDate = LocalDate.of(2012, 3, 4)
    val adjuster: TemporalAdjuster = new TemporalAdjuster() {
      def adjustInto(dateTime: Temporal): Temporal = {
        sample
      }
    }
    assertEquals(TEST_2007_07_15.`with`(adjuster), sample)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_with_adjustment_null(): Unit = {
    TEST_2007_07_15.`with`(null.asInstanceOf[TemporalAdjuster])
  }

  @Test def test_with_DateTimeField_long_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.`with`(YEAR, 2008)
    assertEquals(t, LocalDate.of(2008, 7, 15))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_with_DateTimeField_long_null(): Unit = {
    TEST_2007_07_15.`with`(null.asInstanceOf[TemporalField], 1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_with_DateTimeField_long_invalidField(): Unit = {
    TEST_2007_07_15.`with`(MockFieldNoValue.INSTANCE, 1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_with_DateTimeField_long_timeField(): Unit = {
    TEST_2007_07_15.`with`(ChronoField.AMPM_OF_DAY, 1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_with_DateTimeField_long_invalidValue(): Unit = {
    TEST_2007_07_15.`with`(ChronoField.DAY_OF_WEEK, -1)
  }

  @Test def test_withYear_int_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.withYear(2008)
    assertEquals(t, LocalDate.of(2008, 7, 15))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withYear_int_invalid(): Unit = {
    TEST_2007_07_15.withYear(Year.MIN_VALUE - 1)
  }

  @Test def test_withYear_int_adjustDay(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 2, 29).withYear(2007)
    val expected: LocalDate = LocalDate.of(2007, 2, 28)
    assertEquals(t, expected)
  }

  @Test def test_withMonth_int_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.withMonth(1)
    assertEquals(t, LocalDate.of(2007, 1, 15))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withMonth_int_invalid(): Unit = {
    TEST_2007_07_15.withMonth(13)
  }

  @Test def test_withMonth_int_adjustDay(): Unit = {
    val t: LocalDate = LocalDate.of(2007, 12, 31).withMonth(11)
    val expected: LocalDate = LocalDate.of(2007, 11, 30)
    assertEquals(t, expected)
  }

  @Test def test_withDayOfMonth_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.withDayOfMonth(1)
    assertEquals(t, LocalDate.of(2007, 7, 1))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfMonth_illegal(): Unit = {
    TEST_2007_07_15.withDayOfMonth(32)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfMonth_invalid(): Unit = {
    LocalDate.of(2007, 11, 30).withDayOfMonth(31)
  }

  @Test def test_withDayOfYear_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.withDayOfYear(33)
    assertEquals(t, LocalDate.of(2007, 2, 2))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfYear_illegal(): Unit = {
    TEST_2007_07_15.withDayOfYear(367)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_withDayOfYear_invalid(): Unit = {
    TEST_2007_07_15.withDayOfYear(366)
  }

  @Test def test_plus_Period_positiveMonths(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(7, ChronoUnit.MONTHS)
    val t: LocalDate = TEST_2007_07_15.plus(period)
    assertEquals(t, LocalDate.of(2008, 2, 15))
  }

  @Test def test_plus_Period_negativeDays(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(-25, ChronoUnit.DAYS)
    val t: LocalDate = TEST_2007_07_15.plus(period)
    assertEquals(t, LocalDate.of(2007, 6, 20))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plus_Period_timeNotAllowed(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(7, ChronoUnit.HOURS)
    TEST_2007_07_15.plus(period)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_plus_Period_null(): Unit = {
    TEST_2007_07_15.plus(null.asInstanceOf[MockSimplePeriod])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plus_Period_invalidTooLarge(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(1, ChronoUnit.YEARS)
    LocalDate.of(Year.MAX_VALUE, 1, 1).plus(period)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plus_Period_invalidTooSmall(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(-1, ChronoUnit.YEARS)
    LocalDate.of(Year.MIN_VALUE, 1, 1).plus(period)
  }

  @Test def test_plus_longPeriodUnit_positiveMonths(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plus(7, ChronoUnit.MONTHS)
    assertEquals(t, LocalDate.of(2008, 2, 15))
  }

  @Test def test_plus_longPeriodUnit_negativeDays(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plus(-25, ChronoUnit.DAYS)
    assertEquals(t, LocalDate.of(2007, 6, 20))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plus_longPeriodUnit_timeNotAllowed(): Unit = {
    TEST_2007_07_15.plus(7, ChronoUnit.HOURS)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_plus_longPeriodUnit_null(): Unit = {
    TEST_2007_07_15.plus(1, null.asInstanceOf[TemporalUnit])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plus_longPeriodUnit_invalidTooLarge(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 1, 1).plus(1, ChronoUnit.YEARS)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plus_longPeriodUnit_invalidTooSmall(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 1).plus(-1, ChronoUnit.YEARS)
  }

  @Test def test_plusYears_long_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusYears(1)
    assertEquals(t, LocalDate.of(2008, 7, 15))
  }

  @Test def test_plusYears_long_negative(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusYears(-1)
    assertEquals(t, LocalDate.of(2006, 7, 15))
  }

  @Test def test_plusYears_long_adjustDay(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 2, 29).plusYears(1)
    val expected: LocalDate = LocalDate.of(2009, 2, 28)
    assertEquals(t, expected)
  }

  @Test def test_plusYears_long_big(): Unit = {
    val years: Long = 20L + Year.MAX_VALUE
    val test: LocalDate = LocalDate.of(-40, 6, 1).plusYears(years)
    assertEquals(test, LocalDate.of((-40L + years).toInt, 6, 1))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_long_invalidTooLarge(): Unit = {
    val test: LocalDate = LocalDate.of(Year.MAX_VALUE, 6, 1)
    test.plusYears(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_long_invalidTooLargeMaxAddMax(): Unit = {
    val test: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 1)
    test.plusYears(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_long_invalidTooLargeMaxAddMin(): Unit = {
    val test: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 1)
    test.plusYears(Long.MinValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_long_invalidTooSmall_validInt(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 1).plusYears(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusYears_long_invalidTooSmall_invalidInt(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 1).plusYears(-10)
  }

  @Test def test_plusMonths_long_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusMonths(1)
    assertEquals(t, LocalDate.of(2007, 8, 15))
  }

  @Test def test_plusMonths_long_overYears(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusMonths(25)
    assertEquals(t, LocalDate.of(2009, 8, 15))
  }

  @Test def test_plusMonths_long_negative(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusMonths(-1)
    assertEquals(t, LocalDate.of(2007, 6, 15))
  }

  @Test def test_plusMonths_long_negativeAcrossYear(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusMonths(-7)
    assertEquals(t, LocalDate.of(2006, 12, 15))
  }

  @Test def test_plusMonths_long_negativeOverYears(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusMonths(-31)
    assertEquals(t, LocalDate.of(2004, 12, 15))
  }

  @Test def test_plusMonths_long_adjustDayFromLeapYear(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 2, 29).plusMonths(12)
    val expected: LocalDate = LocalDate.of(2009, 2, 28)
    assertEquals(t, expected)
  }

  @Test def test_plusMonths_long_adjustDayFromMonthLength(): Unit = {
    val t: LocalDate = LocalDate.of(2007, 3, 31).plusMonths(1)
    val expected: LocalDate = LocalDate.of(2007, 4, 30)
    assertEquals(t, expected)
  }

  @Test def test_plusMonths_long_big(): Unit = {
    val months: Long = 20L + Integer.MAX_VALUE
    val test: LocalDate = LocalDate.of(-40, 6, 1).plusMonths(months)
    assertEquals(test, LocalDate.of((-40L + months / 12).toInt, 6 + (months % 12).toInt, 1))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusMonths_long_invalidTooLarge(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 12, 1).plusMonths(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusMonths_long_invalidTooLargeMaxAddMax(): Unit = {
    val test: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 1)
    test.plusMonths(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusMonths_long_invalidTooLargeMaxAddMin(): Unit = {
    val test: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 1)
    test.plusMonths(Long.MinValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusMonths_long_invalidTooSmall(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 1).plusMonths(-1)
  }

  @Test def test_plusWeeks_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusWeeks(1)
    assertEquals(t, LocalDate.of(2007, 7, 22))
  }

  @Test def test_plusWeeks_overMonths(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusWeeks(9)
    assertEquals(t, LocalDate.of(2007, 9, 16))
  }

  @Test def test_plusWeeks_overYears(): Unit = {
    val t: LocalDate = LocalDate.of(2006, 7, 16).plusWeeks(52)
    assertEquals(t, TEST_2007_07_15)
  }

  @Test def test_plusWeeks_overLeapYears(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusYears(-1).plusWeeks(104)
    assertEquals(t, LocalDate.of(2008, 7, 12))
  }

  @Test def test_plusWeeks_negative(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusWeeks(-1)
    assertEquals(t, LocalDate.of(2007, 7, 8))
  }

  @Test def test_plusWeeks_negativeAcrossYear(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusWeeks(-28)
    assertEquals(t, LocalDate.of(2006, 12, 31))
  }

  @Test def test_plusWeeks_negativeOverYears(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusWeeks(-104)
    assertEquals(t, LocalDate.of(2005, 7, 17))
  }

  @Test def test_plusWeeks_maximum(): Unit = {
    val t: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 24).plusWeeks(1)
    val expected: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 31)
    assertEquals(t, expected)
  }

  @Test def test_plusWeeks_minimum(): Unit = {
    val t: LocalDate = LocalDate.of(Year.MIN_VALUE, 1, 8).plusWeeks(-1)
    val expected: LocalDate = LocalDate.of(Year.MIN_VALUE, 1, 1)
    assertEquals(t, expected)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusWeeks_invalidTooLarge(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 12, 25).plusWeeks(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusWeeks_invalidTooSmall(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 7).plusWeeks(-1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_plusWeeks_invalidMaxMinusMax(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 12, 25).plusWeeks(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_plusWeeks_invalidMaxMinusMin(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 12, 25).plusWeeks(Long.MinValue)
  }

  @Test def test_plusDays_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusDays(1)
    assertEquals(t, LocalDate.of(2007, 7, 16))
  }

  @Test def test_plusDays_overMonths(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusDays(62)
    assertEquals(t, LocalDate.of(2007, 9, 15))
  }

  @Test def test_plusDays_overYears(): Unit = {
    val t: LocalDate = LocalDate.of(2006, 7, 14).plusDays(366)
    assertEquals(t, TEST_2007_07_15)
  }

  @Test def test_plusDays_overLeapYears(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusYears(-1).plusDays(365 + 366)
    assertEquals(t, LocalDate.of(2008, 7, 15))
  }

  @Test def test_plusDays_negative(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusDays(-1)
    assertEquals(t, LocalDate.of(2007, 7, 14))
  }

  @Test def test_plusDays_negativeAcrossYear(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusDays(-196)
    assertEquals(t, LocalDate.of(2006, 12, 31))
  }

  @Test def test_plusDays_negativeOverYears(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusDays(-730)
    assertEquals(t, LocalDate.of(2005, 7, 15))
  }

  @Test def test_plusDays_maximum(): Unit = {
    val t: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 30).plusDays(1)
    val expected: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 31)
    assertEquals(t, expected)
  }

  @Test def test_plusDays_minimum(): Unit = {
    val t: LocalDate = LocalDate.of(Year.MIN_VALUE, 1, 2).plusDays(-1)
    val expected: LocalDate = LocalDate.of(Year.MIN_VALUE, 1, 1)
    assertEquals(t, expected)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusDays_invalidTooLarge(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 12, 31).plusDays(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_plusDays_invalidTooSmall(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 1).plusDays(-1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_plusDays_overflowTooLarge(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 12, 31).plusDays(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_plusDays_overflowTooSmall(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 1).plusDays(Long.MinValue)
  }

  @Test def test_minus_Period_positiveMonths(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(7, ChronoUnit.MONTHS)
    val t: LocalDate = TEST_2007_07_15.minus(period)
    assertEquals(t, LocalDate.of(2006, 12, 15))
  }

  @Test def test_minus_Period_negativeDays(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(-25, ChronoUnit.DAYS)
    val t: LocalDate = TEST_2007_07_15.minus(period)
    assertEquals(t, LocalDate.of(2007, 8, 9))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minus_Period_timeNotAllowed(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(7, ChronoUnit.HOURS)
    TEST_2007_07_15.minus(period)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_minus_Period_null(): Unit = {
    TEST_2007_07_15.minus(null.asInstanceOf[MockSimplePeriod])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minus_Period_invalidTooLarge(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(-1, ChronoUnit.YEARS)
    LocalDate.of(Year.MAX_VALUE, 1, 1).minus(period)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minus_Period_invalidTooSmall(): Unit = {
    val period: MockSimplePeriod = MockSimplePeriod.of(1, ChronoUnit.YEARS)
    LocalDate.of(Year.MIN_VALUE, 1, 1).minus(period)
  }

  @Test def test_minus_longPeriodUnit_positiveMonths(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minus(7, ChronoUnit.MONTHS)
    assertEquals(t, LocalDate.of(2006, 12, 15))
  }

  @Test def test_minus_longPeriodUnit_negativeDays(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minus(-25, ChronoUnit.DAYS)
    assertEquals(t, LocalDate.of(2007, 8, 9))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minus_longPeriodUnit_timeNotAllowed(): Unit = {
    TEST_2007_07_15.minus(7, ChronoUnit.HOURS)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_minus_longPeriodUnit_null(): Unit = {
    TEST_2007_07_15.minus(1, null.asInstanceOf[TemporalUnit])
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minus_longPeriodUnit_invalidTooLarge(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 1, 1).minus(-1, ChronoUnit.YEARS)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minus_longPeriodUnit_invalidTooSmall(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 1).minus(1, ChronoUnit.YEARS)
  }

  @Test def test_minusYears_long_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusYears(1)
    assertEquals(t, LocalDate.of(2006, 7, 15))
  }

  @Test def test_minusYears_long_negative(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusYears(-1)
    assertEquals(t, LocalDate.of(2008, 7, 15))
  }

  @Test def test_minusYears_long_adjustDay(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 2, 29).minusYears(1)
    val expected: LocalDate = LocalDate.of(2007, 2, 28)
    assertEquals(t, expected)
  }

  @Test def test_minusYears_long_big(): Unit = {
    val years: Long = 20L + Year.MAX_VALUE
    val test: LocalDate = LocalDate.of(40, 6, 1).minusYears(years)
    assertEquals(test, LocalDate.of((40L - years).toInt, 6, 1))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_long_invalidTooLarge(): Unit = {
    val test: LocalDate = LocalDate.of(Year.MAX_VALUE, 6, 1)
    test.minusYears(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_long_invalidTooLargeMaxAddMax(): Unit = {
    val test: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 1)
    test.minusYears(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_long_invalidTooLargeMaxAddMin(): Unit = {
    val test: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 1)
    test.minusYears(Long.MinValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusYears_long_invalidTooSmall(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 1).minusYears(1)
  }

  @Test def test_minusMonths_long_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusMonths(1)
    assertEquals(t, LocalDate.of(2007, 6, 15))
  }

  @Test def test_minusMonths_long_overYears(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusMonths(25)
    assertEquals(t, LocalDate.of(2005, 6, 15))
  }

  @Test def test_minusMonths_long_negative(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusMonths(-1)
    assertEquals(t, LocalDate.of(2007, 8, 15))
  }

  @Test def test_minusMonths_long_negativeAcrossYear(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusMonths(-7)
    assertEquals(t, LocalDate.of(2008, 2, 15))
  }

  @Test def test_minusMonths_long_negativeOverYears(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusMonths(-31)
    assertEquals(t, LocalDate.of(2010, 2, 15))
  }

  @Test def test_minusMonths_long_adjustDayFromLeapYear(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 2, 29).minusMonths(12)
    val expected: LocalDate = LocalDate.of(2007, 2, 28)
    assertEquals(t, expected)
  }

  @Test def test_minusMonths_long_adjustDayFromMonthLength(): Unit = {
    val t: LocalDate = LocalDate.of(2007, 3, 31).minusMonths(1)
    val expected: LocalDate = LocalDate.of(2007, 2, 28)
    assertEquals(t, expected)
  }

  @Test def test_minusMonths_long_big(): Unit = {
    val months: Long = 20L + Integer.MAX_VALUE
    val test: LocalDate = LocalDate.of(40, 6, 1).minusMonths(months)
    assertEquals(test, LocalDate.of((40L - months / 12).toInt, 6 - (months % 12).toInt, 1))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusMonths_long_invalidTooLarge(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 12, 1).minusMonths(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusMonths_long_invalidTooLargeMaxAddMax(): Unit = {
    val test: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 1)
    test.minusMonths(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusMonths_long_invalidTooLargeMaxAddMin(): Unit = {
    val test: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 1)
    test.minusMonths(Long.MinValue)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusMonths_long_invalidTooSmall(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 1).minusMonths(1)
  }

  @Test def test_minusWeeks_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusWeeks(1)
    assertEquals(t, LocalDate.of(2007, 7, 8))
  }

  @Test def test_minusWeeks_overMonths(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusWeeks(9)
    assertEquals(t, LocalDate.of(2007, 5, 13))
  }

  @Test def test_minusWeeks_overYears(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 7, 13).minusWeeks(52)
    assertEquals(t, TEST_2007_07_15)
  }

  @Test def test_minusWeeks_overLeapYears(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusYears(-1).minusWeeks(104)
    assertEquals(t, LocalDate.of(2006, 7, 18))
  }

  @Test def test_minusWeeks_negative(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusWeeks(-1)
    assertEquals(t, LocalDate.of(2007, 7, 22))
  }

  @Test def test_minusWeeks_negativeAcrossYear(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusWeeks(-28)
    assertEquals(t, LocalDate.of(2008, 1, 27))
  }

  @Test def test_minusWeeks_negativeOverYears(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusWeeks(-104)
    assertEquals(t, LocalDate.of(2009, 7, 12))
  }

  @Test def test_minusWeeks_maximum(): Unit = {
    val t: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 24).minusWeeks(-1)
    val expected: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 31)
    assertEquals(t, expected)
  }

  @Test def test_minusWeeks_minimum(): Unit = {
    val t: LocalDate = LocalDate.of(Year.MIN_VALUE, 1, 8).minusWeeks(1)
    val expected: LocalDate = LocalDate.of(Year.MIN_VALUE, 1, 1)
    assertEquals(t, expected)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusWeeks_invalidTooLarge(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 12, 25).minusWeeks(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusWeeks_invalidTooSmall(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 7).minusWeeks(1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_minusWeeks_invalidMaxMinusMax(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 12, 25).minusWeeks(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_minusWeeks_invalidMaxMinusMin(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 12, 25).minusWeeks(Long.MinValue)
  }

  @Test def test_minusDays_normal(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusDays(1)
    assertEquals(t, LocalDate.of(2007, 7, 14))
  }

  @Test def test_minusDays_overMonths(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusDays(62)
    assertEquals(t, LocalDate.of(2007, 5, 14))
  }

  @Test def test_minusDays_overYears(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 7, 16).minusDays(367)
    assertEquals(t, TEST_2007_07_15)
  }

  @Test def test_minusDays_overLeapYears(): Unit = {
    val t: LocalDate = TEST_2007_07_15.plusYears(2).minusDays(365 + 366)
    assertEquals(t, TEST_2007_07_15)
  }

  @Test def test_minusDays_negative(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusDays(-1)
    assertEquals(t, LocalDate.of(2007, 7, 16))
  }

  @Test def test_minusDays_negativeAcrossYear(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusDays(-169)
    assertEquals(t, LocalDate.of(2007, 12, 31))
  }

  @Test def test_minusDays_negativeOverYears(): Unit = {
    val t: LocalDate = TEST_2007_07_15.minusDays(-731)
    assertEquals(t, LocalDate.of(2009, 7, 15))
  }

  @Test def test_minusDays_maximum(): Unit = {
    val t: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 30).minusDays(-1)
    val expected: LocalDate = LocalDate.of(Year.MAX_VALUE, 12, 31)
    assertEquals(t, expected)
  }

  @Test def test_minusDays_minimum(): Unit = {
    val t: LocalDate = LocalDate.of(Year.MIN_VALUE, 1, 2).minusDays(1)
    val expected: LocalDate = LocalDate.of(Year.MIN_VALUE, 1, 1)
    assertEquals(t, expected)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusDays_invalidTooLarge(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 12, 31).minusDays(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_minusDays_invalidTooSmall(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 1).minusDays(1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_minusDays_overflowTooLarge(): Unit = {
    LocalDate.of(Year.MAX_VALUE, 12, 31).minusDays(Long.MinValue)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_minusDays_overflowTooSmall(): Unit = {
    LocalDate.of(Year.MIN_VALUE, 1, 1).minusDays(Long.MaxValue)
  }

  @DataProvider(name = "until") private[bp] def provider_until: Array[Array[Any]] = {
    Array[Array[Any]](Array("2012-06-30", "2012-06-30", DAYS, 0), Array("2012-06-30", "2012-06-30", WEEKS, 0), Array("2012-06-30", "2012-06-30", MONTHS, 0), Array("2012-06-30", "2012-06-30", YEARS, 0), Array("2012-06-30", "2012-06-30", DECADES, 0), Array("2012-06-30", "2012-06-30", CENTURIES, 0), Array("2012-06-30", "2012-06-30", MILLENNIA, 0), Array("2012-06-30", "2012-07-01", DAYS, 1), Array("2012-06-30", "2012-07-01", WEEKS, 0), Array("2012-06-30", "2012-07-01", MONTHS, 0), Array("2012-06-30", "2012-07-01", YEARS, 0), Array("2012-06-30", "2012-07-01", DECADES, 0), Array("2012-06-30", "2012-07-01", CENTURIES, 0), Array("2012-06-30", "2012-07-01", MILLENNIA, 0), Array("2012-06-30", "2012-07-07", DAYS, 7), Array("2012-06-30", "2012-07-07", WEEKS, 1), Array("2012-06-30", "2012-07-07", MONTHS, 0), Array("2012-06-30", "2012-07-07", YEARS, 0), Array("2012-06-30", "2012-07-07", DECADES, 0), Array("2012-06-30", "2012-07-07", CENTURIES, 0), Array("2012-06-30", "2012-07-07", MILLENNIA, 0), Array("2012-06-30", "2012-07-29", MONTHS, 0), Array("2012-06-30", "2012-07-30", MONTHS, 1), Array("2012-06-30", "2012-07-31", MONTHS, 1))
  }

  @Test(dataProvider = "until") def test_until(startStr: String, endStr: String, unit: TemporalUnit, expected: Long): Unit = {
    val start: LocalDate = LocalDate.parse(startStr)
    val end: LocalDate = LocalDate.parse(endStr)
    assertEquals(start.until(end, unit), expected)
    assertEquals(end.until(start, unit), -expected)
  }

  @Test def test_atTime_LocalTime(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    assertEquals(t.atTime(LocalTime.of(11, 30)), LocalDateTime.of(2008, 6, 30, 11, 30))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_atTime_LocalTime_null(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(null.asInstanceOf[LocalTime])
  }

  @Test def test_atTime_int_int(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    assertEquals(t.atTime(11, 30), LocalDateTime.of(2008, 6, 30, 11, 30))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_hourTooSmall(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(-1, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_hourTooBig(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(24, 30)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_minuteTooSmall(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(11, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_minuteTooBig(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(11, 60)
  }

  @Test def test_atTime_int_int_int(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    assertEquals(t.atTime(11, 30, 40), LocalDateTime.of(2008, 6, 30, 11, 30, 40))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_hourTooSmall(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(-1, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_hourTooBig(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(24, 30, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_minuteTooSmall(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(11, -1, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_minuteTooBig(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(11, 60, 40)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_secondTooSmall(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(11, 30, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_secondTooBig(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(11, 30, 60)
  }

  @Test def test_atTime_int_int_int_int(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    assertEquals(t.atTime(11, 30, 40, 50), LocalDateTime.of(2008, 6, 30, 11, 30, 40, 50))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_int_hourTooSmall(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(-1, 30, 40, 50)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_int_hourTooBig(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(24, 30, 40, 50)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_int_minuteTooSmall(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(11, -1, 40, 50)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_int_minuteTooBig(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(11, 60, 40, 50)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_int_secondTooSmall(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(11, 30, -1, 50)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_int_secondTooBig(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(11, 30, 60, 50)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_int_nanoTooSmall(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(11, 30, 40, -1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_atTime_int_int_int_int_nanoTooBig(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atTime(11, 30, 40, 1000000000)
  }

  @Test def test_atStartOfDay(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    assertEquals(t.atStartOfDay(TestLocalDate.ZONE_PARIS), ZonedDateTime.of(LocalDateTime.of(2008, 6, 30, 0, 0), TestLocalDate.ZONE_PARIS))
  }

  @Test def test_atStartOfDay_dstGap(): Unit = {
    val t: LocalDate = LocalDate.of(2007, 4, 1)
    assertEquals(t.atStartOfDay(TestLocalDate.ZONE_GAZA), ZonedDateTime.of(LocalDateTime.of(2007, 4, 1, 1, 0), TestLocalDate.ZONE_GAZA))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_atStartOfDay_nullTimeZone(): Unit = {
    val t: LocalDate = LocalDate.of(2008, 6, 30)
    t.atStartOfDay(null.asInstanceOf[ZoneId])
  }

  @Test def test_toEpochDay(): Unit = {
    val date_0000_01_01: Long = -678941 - 40587
    var test: LocalDate = LocalDate.of(0, 1, 1)
    
    {
      var i: Long = date_0000_01_01
      while (i < 700000) {
        {
          assertEquals(test.toEpochDay, i)
          test = next(test)
        }
        {
          i += 1
          i - 1
        }
      }
    }
    test = LocalDate.of(0, 1, 1)
    
    {
      var i: Long = date_0000_01_01
      while (i > -2000000) {
        {
          assertEquals(test.toEpochDay, i)
          test = previous(test)
        }
        {
          i -= 1
          i + 1
        }
      }
    }
    assertEquals(LocalDate.of(1858, 11, 17).toEpochDay, -40587)
    assertEquals(LocalDate.of(1, 1, 1).toEpochDay, -678575 - 40587)
    assertEquals(LocalDate.of(1995, 9, 27).toEpochDay, 49987 - 40587)
    assertEquals(LocalDate.of(1970, 1, 1).toEpochDay, 0)
    assertEquals(LocalDate.of(-1, 12, 31).toEpochDay, -678942 - 40587)
  }

  @Test def test_comparisons(): Unit = {
    doTest_comparisons_LocalDate(LocalDate.of(Year.MIN_VALUE, 1, 1), LocalDate.of(Year.MIN_VALUE, 12, 31), LocalDate.of(-1, 1, 1), LocalDate.of(-1, 12, 31), LocalDate.of(0, 1, 1), LocalDate.of(0, 12, 31), LocalDate.of(1, 1, 1), LocalDate.of(1, 12, 31), LocalDate.of(2006, 1, 1), LocalDate.of(2006, 12, 31), LocalDate.of(2007, 1, 1), LocalDate.of(2007, 12, 31), LocalDate.of(2008, 1, 1), LocalDate.of(2008, 2, 29), LocalDate.of(2008, 12, 31), LocalDate.of(Year.MAX_VALUE, 1, 1), LocalDate.of(Year.MAX_VALUE, 12, 31))
  }

  private def doTest_comparisons_LocalDate(localDates: LocalDate*): Unit = {
    {
      var i: Int = 0
      while (i < localDates.length) {
        {
          val a: LocalDate = localDates(i)
          
          {
            var j: Int = 0
            while (j < localDates.length) {
              {
                val b: LocalDate = localDates(j)
                if (i < j) {
                  assertTrue(a.compareTo(b) < 0, a + " <=> " + b)
                  assertEquals(a.isBefore(b), true, a + " <=> " + b)
                  assertEquals(a.isAfter(b), false, a + " <=> " + b)
                  assertEquals(a == b, false, a + " <=> " + b)
                }
                else if (i > j) {
                  assertTrue(a.compareTo(b) > 0, a + " <=> " + b)
                  assertEquals(a.isBefore(b), false, a + " <=> " + b)
                  assertEquals(a.isAfter(b), true, a + " <=> " + b)
                  assertEquals(a == b, false, a + " <=> " + b)
                }
                else {
                  assertEquals(a.compareTo(b), 0, a + " <=> " + b)
                  assertEquals(a.isBefore(b), false, a + " <=> " + b)
                  assertEquals(a.isAfter(b), false, a + " <=> " + b)
                  assertEquals(a == b, true, a + " <=> " + b)
                }
              }
              {
                j += 1
                j - 1
              }
            }
          }
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_compareTo_ObjectNull(): Unit = {
    TEST_2007_07_15.compareTo(null)
  }

  @Test def test_isBefore(): Unit = {
    assertTrue(TEST_2007_07_15.isBefore(LocalDate.of(2007, 7, 16)))
    assertFalse(TEST_2007_07_15.isBefore(LocalDate.of(2007, 7, 14)))
    assertFalse(TEST_2007_07_15.isBefore(TEST_2007_07_15))
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isBefore_ObjectNull(): Unit = {
    TEST_2007_07_15.isBefore(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isAfter_ObjectNull(): Unit = {
    TEST_2007_07_15.isAfter(null)
  }

  @Test def test_isAfter(): Unit = {
    assertTrue(TEST_2007_07_15.isAfter(LocalDate.of(2007, 7, 14)))
    assertFalse(TEST_2007_07_15.isAfter(LocalDate.of(2007, 7, 16)))
    assertFalse(TEST_2007_07_15.isAfter(TEST_2007_07_15))
  }

  @Test(dataProvider = "sampleDates") def test_equals_true(y: Int, m: Int, d: Int): Unit = {
    val a: LocalDate = LocalDate.of(y, m, d)
    val b: LocalDate = LocalDate.of(y, m, d)
    assertEquals(a == b, true)
  }

  @Test(dataProvider = "sampleDates") def test_equals_false_year_differs(y: Int, m: Int, d: Int): Unit = {
    val a: LocalDate = LocalDate.of(y, m, d)
    val b: LocalDate = LocalDate.of(y + 1, m, d)
    assertEquals(a == b, false)
  }

  @Test(dataProvider = "sampleDates") def test_equals_false_month_differs(y: Int, m: Int, d: Int): Unit = {
    val a: LocalDate = LocalDate.of(y, m, d)
    val b: LocalDate = LocalDate.of(y, m + 1, d)
    assertEquals(a == b, false)
  }

  @Test(dataProvider = "sampleDates") def test_equals_false_day_differs(y: Int, m: Int, d: Int): Unit = {
    val a: LocalDate = LocalDate.of(y, m, d)
    val b: LocalDate = LocalDate.of(y, m, d + 1)
    assertEquals(a == b, false)
  }

  @Test def test_equals_itself_true(): Unit = {
    assertEquals(TEST_2007_07_15 == TEST_2007_07_15, true)
  }

  @Test def test_equals_string_false(): Unit = {
    assertEquals(TEST_2007_07_15 == "2007-07-15", false)
  }

  @Test def test_equals_null_false(): Unit = {
    assertEquals(TEST_2007_07_15 == null, false)
  }

  @Test(dataProvider = "sampleDates") def test_hashCode(y: Int, m: Int, d: Int): Unit = {
    val a: LocalDate = LocalDate.of(y, m, d)
    assertEquals(a.hashCode, a.hashCode)
    val b: LocalDate = LocalDate.of(y, m, d)
    assertEquals(a.hashCode, b.hashCode)
  }

  @DataProvider(name = "sampleToString") private[bp] def provider_sampleToString: Array[Array[Any]] = {
    Array[Array[Any]](Array(2008, 7, 5, "2008-07-05"), Array(2007, 12, 31, "2007-12-31"), Array(999, 12, 31, "0999-12-31"), Array(-1, 1, 2, "-0001-01-02"), Array(9999, 12, 31, "9999-12-31"), Array(-9999, 12, 31, "-9999-12-31"), Array(10000, 1, 1, "+10000-01-01"), Array(-10000, 1, 1, "-10000-01-01"), Array(12345678, 1, 1, "+12345678-01-01"), Array(-12345678, 1, 1, "-12345678-01-01"))
  }

  @Test(dataProvider = "sampleToString") def test_toString(y: Int, m: Int, d: Int, expected: String): Unit = {
    val t: LocalDate = LocalDate.of(y, m, d)
    val str: String = t.toString
    assertEquals(str, expected)
  }

  @Test def test_format_formatter(): Unit = {
    val f: DateTimeFormatter = DateTimeFormatter.ofPattern("y M d")
    val t: String = LocalDate.of(2010, 12, 3).format(f)
    assertEquals(t, "2010 12 3")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_format_formatter_null(): Unit = {
    LocalDate.of(2010, 12, 3).format(null)
  }
}