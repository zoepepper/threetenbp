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

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.Assert.fail
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.DAY_OF_WEEK
import java.time.temporal.ChronoField.DAY_OF_YEAR
import java.time.temporal.ChronoField.HOUR_OF_DAY
import java.time.temporal.ChronoField.MINUTE_OF_HOUR
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.time.temporal.ChronoField.NANO_OF_SECOND
import java.time.temporal.ChronoField.OFFSET_SECONDS
import java.time.temporal.ChronoField.SECOND_OF_MINUTE
import java.time.temporal.ChronoField.YEAR
import java.text.ParsePosition
import java.util.HashMap
import java.util.Iterator
import java.util.Locale
import java.util.Map
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Year
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import java.time.chrono.Chronology
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalField
import java.time.temporal.TemporalQueries
import java.time.temporal.TemporalQuery

/** Test DateTimeFormatters. */
@Test object TestDateTimeFormatters {

  private[format] class MockAccessor extends TemporalAccessor {
    private[format] var fields: java.util.Map[TemporalField, Long] = new java.util.HashMap[TemporalField, Long]
    private[format] var zoneId: ZoneId = null

    private[format] def setFields(dt: LocalDate): Unit = {
      if (dt != null) {
        fields.put(YEAR, dt.getYear.toLong)
        fields.put(MONTH_OF_YEAR, dt.getMonthValue.toLong)
        fields.put(DAY_OF_MONTH, dt.getDayOfMonth.toLong)
        fields.put(DAY_OF_YEAR, dt.getDayOfYear.toLong)
        fields.put(DAY_OF_WEEK, dt.getDayOfWeek.getValue.toLong)
        fields.put(IsoFields.WEEK_BASED_YEAR, dt.getLong(IsoFields.WEEK_BASED_YEAR))
        fields.put(IsoFields.WEEK_OF_WEEK_BASED_YEAR, dt.getLong(IsoFields.WEEK_OF_WEEK_BASED_YEAR))
      }
    }

    private[format] def setFields(dt: LocalDateTime): Unit = {
      if (dt != null) {
        fields.put(YEAR, dt.getYear.toLong)
        fields.put(MONTH_OF_YEAR, dt.getMonthValue.toLong)
        fields.put(DAY_OF_MONTH, dt.getDayOfMonth.toLong)
        fields.put(DAY_OF_YEAR, dt.getDayOfYear.toLong)
        fields.put(DAY_OF_WEEK, dt.getDayOfWeek.getValue.toLong)
        fields.put(IsoFields.WEEK_BASED_YEAR, dt.getLong(IsoFields.WEEK_BASED_YEAR))
        fields.put(IsoFields.WEEK_OF_WEEK_BASED_YEAR, dt.getLong(IsoFields.WEEK_OF_WEEK_BASED_YEAR))
        fields.put(HOUR_OF_DAY, dt.getHour.toLong)
        fields.put(MINUTE_OF_HOUR, dt.getMinute.toLong)
        fields.put(SECOND_OF_MINUTE, dt.getSecond.toLong)
        fields.put(NANO_OF_SECOND, dt.getNano.toLong)
      }
    }

    private[format] def setOffset(offsetId: String): Unit = {
      if (offsetId != null) {
        this.fields.put(OFFSET_SECONDS, ZoneOffset.of(offsetId).getTotalSeconds.toLong)
      }
    }

    private[format] def setZone(zoneId: String): Unit = {
      if (zoneId != null) {
        this.zoneId = ZoneId.of(zoneId)
      }
    }

    def isSupported(field: TemporalField): Boolean = {
      fields.containsKey(field)
    }

    def getLong(field: TemporalField): Long = {
      try {
        fields.get(field)
      }
      catch {
        case ex: NullPointerException =>
          throw new DateTimeException("Field missing: " + field)
      }
    }

    @SuppressWarnings(Array("unchecked")) override def query[R >: Null](query: TemporalQuery[R]): R = {
      if (query eq TemporalQueries.zoneId) {
        return zoneId.asInstanceOf[R]
      }
      super.query(query)
    }

    override def toString: String = fields + (if (zoneId != null) " " + zoneId else "")
  }

  private[format] class Expected private[format]() {
    private[format] var fieldValues: java.util.Map[TemporalField, Long] = new java.util.HashMap[TemporalField, Long]
    private[format] var zone: ZoneId = null
    private[format] var chrono: Chronology = null

    private[format] def this(field1: TemporalField, value1: Long, field2: TemporalField, value2: Long) = {
      this()
      fieldValues.put(field1, value1)
      fieldValues.put(field2, value2)
    }

    private[format] def add(offset: ZoneOffset): Unit = {
      fieldValues.put(OFFSET_SECONDS, offset.getTotalSeconds.toLong)
    }
  }

}

@Test class TestDateTimeFormatters {
  @BeforeMethod def setUp(): Unit = {
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_print_nullCalendrical(): Unit = {
    DateTimeFormatter.ISO_DATE.format(null.asInstanceOf[TemporalAccessor])
  }

  @Test def test_pattern_String(): Unit = {
    val test: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu")
    assertEquals(test.toString, "Value(DayOfMonth)' 'Text(MonthOfYear,SHORT)' 'Value(Year,4,19,EXCEEDS_PAD)")
    assertEquals(test.getLocale, Locale.getDefault)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_pattern_String_invalid(): Unit = {
    DateTimeFormatter.ofPattern("p")
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_pattern_String_null(): Unit = {
    DateTimeFormatter.ofPattern(null)
  }

  @Test def test_pattern_StringLocale(): Unit = {
    val test: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.UK)
    assertEquals(test.toString, "Value(DayOfMonth)' 'Text(MonthOfYear,SHORT)' 'Value(Year,4,19,EXCEEDS_PAD)")
    assertEquals(test.getLocale, Locale.UK)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException])) def test_pattern_StringLocale_invalid(): Unit = {
    DateTimeFormatter.ofPattern("p", Locale.UK)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_pattern_StringLocale_nullPattern(): Unit = {
    DateTimeFormatter.ofPattern(null, Locale.UK)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_pattern_StringLocale_nullLocale(): Unit = {
    DateTimeFormatter.ofPattern("yyyy", null)
  }

  @DataProvider(name = "sample_isoLocalDate") private[format] def provider_sample_isoLocalDate: Array[Array[Any]] = {
    Array[Array[Any]](Array(2008, null, null, null, null, null, classOf[DateTimeException]), Array(null, 6, null, null, null, null, classOf[DateTimeException]), Array(null, null, 30, null, null, null, classOf[DateTimeException]), Array(null, null, null, "+01:00", null, null, classOf[DateTimeException]), Array(null, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, null, null, null, null, classOf[DateTimeException]), Array(null, 6, 30, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, null, null, "2008-06-30", null), Array(2008, 6, 30, "+01:00", null, "2008-06-30", null), Array(2008, 6, 30, "+01:00", "Europe/Paris", "2008-06-30", null), Array(2008, 6, 30, null, "Europe/Paris", "2008-06-30", null), Array(123456, 6, 30, null, null, "+123456-06-30", null))
  }

  @Test(dataProvider = "sample_isoLocalDate") def test_print_isoLocalDate(year: Integer, month: Integer, day: Integer, offsetId: String, zoneId: String, expected: String, expectedEx: Class[_]): Unit = {
    val test: TemporalAccessor = buildAccessor(year, month, day, null, null, null, null, offsetId, zoneId)
    if (expectedEx == null) {
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(test), expected)
    }
    else {
      try {
        DateTimeFormatter.ISO_LOCAL_DATE.format(test)
        fail
      }
      catch {
        case ex: Exception =>
          assertTrue(expectedEx.isInstance(ex))
      }
    }
  }

  @Test(dataProvider = "sample_isoLocalDate") def test_parse_isoLocalDate(year: Integer, month: Integer, day: Integer, offsetId: String, zoneId: String, input: String, invalid: Class[_]): Unit = {
    if (input != null) {
      val expected: TestDateTimeFormatters.Expected = createDate(year, month, day)
      assertParseMatch(DateTimeFormatter.ISO_LOCAL_DATE.parseUnresolved(input, new ParsePosition(0)), expected)
    }
  }

  @Test def test_parse_isoLocalDate_999999999(): Unit = {
    val expected: TestDateTimeFormatters.Expected = createDate(999999999, 8, 6)
    assertParseMatch(DateTimeFormatter.ISO_LOCAL_DATE.parseUnresolved("+999999999-08-06", new ParsePosition(0)), expected)
    assertEquals(LocalDate.parse("+999999999-08-06"), LocalDate.of(999999999, 8, 6))
  }

  @Test def test_parse_isoLocalDate_1000000000(): Unit = {
    val expected: TestDateTimeFormatters.Expected = createDate(1000000000, 8, 6)
    assertParseMatch(DateTimeFormatter.ISO_LOCAL_DATE.parseUnresolved("+1000000000-08-06", new ParsePosition(0)), expected)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_parse_isoLocalDate_1000000000_failedCreate(): Unit = {
    LocalDate.parse("+1000000000-08-06")
  }

  @Test def test_parse_isoLocalDate_M999999999(): Unit = {
    val expected: TestDateTimeFormatters.Expected = createDate(-999999999, 8, 6)
    assertParseMatch(DateTimeFormatter.ISO_LOCAL_DATE.parseUnresolved("-999999999-08-06", new ParsePosition(0)), expected)
    assertEquals(LocalDate.parse("-999999999-08-06"), LocalDate.of(-999999999, 8, 6))
  }

  @Test def test_parse_isoLocalDate_M1000000000(): Unit = {
    val expected: TestDateTimeFormatters.Expected = createDate(-1000000000, 8, 6)
    assertParseMatch(DateTimeFormatter.ISO_LOCAL_DATE.parseUnresolved("-1000000000-08-06", new ParsePosition(0)), expected)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_parse_isoLocalDate_M1000000000_failedCreate(): Unit = {
    LocalDate.parse("-1000000000-08-06")
  }

  @DataProvider(name = "sample_isoOffsetDate") private[format] def provider_sample_isoOffsetDate: Array[Array[Any]] = {
    Array[Array[Any]](Array(2008, null, null, null, null, null, classOf[DateTimeException]), Array(null, 6, null, null, null, null, classOf[DateTimeException]), Array(null, null, 30, null, null, null, classOf[DateTimeException]), Array(null, null, null, "+01:00", null, null, classOf[DateTimeException]), Array(null, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, null, null, null, null, classOf[DateTimeException]), Array(null, 6, 30, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, "+01:00", null, "2008-06-30+01:00", null), Array(2008, 6, 30, "+01:00", "Europe/Paris", "2008-06-30+01:00", null), Array(2008, 6, 30, null, "Europe/Paris", null, classOf[DateTimeException]), Array(123456, 6, 30, "+01:00", null, "+123456-06-30+01:00", null))
  }

  @Test(dataProvider = "sample_isoOffsetDate") def test_print_isoOffsetDate(year: Integer, month: Integer, day: Integer, offsetId: String, zoneId: String, expected: String, expectedEx: Class[_]): Unit = {
    val test: TemporalAccessor = buildAccessor(year, month, day, null, null, null, null, offsetId, zoneId)
    if (expectedEx == null) {
      assertEquals(DateTimeFormatter.ISO_OFFSET_DATE.format(test), expected)
    }
    else {
      try {
        DateTimeFormatter.ISO_OFFSET_DATE.format(test)
        fail
      }
      catch {
        case ex: Exception =>
          assertTrue(expectedEx.isInstance(ex))
      }
    }
  }

  @Test(dataProvider = "sample_isoOffsetDate") def test_parse_isoOffsetDate(year: Integer, month: Integer, day: Integer, offsetId: String, zoneId: String, input: String, invalid: Class[_]): Unit = {
    if (input != null) {
      val expected: TestDateTimeFormatters.Expected = createDate(year, month, day)
      buildCalendrical(expected, offsetId, null)
      assertParseMatch(DateTimeFormatter.ISO_OFFSET_DATE.parseUnresolved(input, new ParsePosition(0)), expected)
    }
  }

  @DataProvider(name = "sample_isoDate") private[format] def provider_sample_isoDate: Array[Array[Any]] = {
    Array[Array[Any]](Array(2008, null, null, null, null, null, classOf[DateTimeException]), Array(null, 6, null, null, null, null, classOf[DateTimeException]), Array(null, null, 30, null, null, null, classOf[DateTimeException]), Array(null, null, null, "+01:00", null, null, classOf[DateTimeException]), Array(null, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, null, null, null, null, classOf[DateTimeException]), Array(null, 6, 30, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, null, null, "2008-06-30", null), Array(2008, 6, 30, "+01:00", null, "2008-06-30+01:00", null), Array(2008, 6, 30, "+01:00", "Europe/Paris", "2008-06-30+01:00", null), Array(2008, 6, 30, null, "Europe/Paris", "2008-06-30", null), Array(123456, 6, 30, "+01:00", "Europe/Paris", "+123456-06-30+01:00", null))
  }

  @Test(dataProvider = "sample_isoDate") def test_print_isoDate(year: Integer, month: Integer, day: Integer, offsetId: String, zoneId: String, expected: String, expectedEx: Class[_]): Unit = {
    val test: TemporalAccessor = buildAccessor(year, month, day, null, null, null, null, offsetId, zoneId)
    if (expectedEx == null) {
      assertEquals(DateTimeFormatter.ISO_DATE.format(test), expected)
    }
    else {
      try {
        DateTimeFormatter.ISO_DATE.format(test)
        fail
      }
      catch {
        case ex: Exception =>
          assertTrue(expectedEx.isInstance(ex))
      }
    }
  }

  @Test(dataProvider = "sample_isoDate") def test_parse_isoDate(year: Integer, month: Integer, day: Integer, offsetId: String, zoneId: String, input: String, invalid: Class[_]): Unit = {
    if (input != null) {
      val expected: TestDateTimeFormatters.Expected = createDate(year, month, day)
      if (offsetId != null) {
        expected.fieldValues.put(OFFSET_SECONDS, ZoneOffset.of(offsetId).getTotalSeconds.toLong)
      }
      assertParseMatch(DateTimeFormatter.ISO_DATE.parseUnresolved(input, new ParsePosition(0)), expected)
    }
  }

  @DataProvider(name = "sample_isoLocalTime") private[format] def provider_sample_isoLocalTime: Array[Array[Any]] = {
    Array[Array[Any]](Array(11, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, 5, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, 30, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, 1, null, null, null, classOf[DateTimeException]), Array(null, null, null, null, "+01:00", null, null, classOf[DateTimeException]), Array(null, null, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(11, 5, null, null, null, null, "11:05", null), Array(11, 5, 30, null, null, null, "11:05:30", null), Array(11, 5, 30, 500000000, null, null, "11:05:30.5", null), Array(11, 5, 30, 1, null, null, "11:05:30.000000001", null), Array(11, 5, null, null, "+01:00", null, "11:05", null), Array(11, 5, 30, null, "+01:00", null, "11:05:30", null), Array(11, 5, 30, 500000000, "+01:00", null, "11:05:30.5", null), Array(11, 5, 30, 1, "+01:00", null, "11:05:30.000000001", null), Array(11, 5, null, null, "+01:00", "Europe/Paris", "11:05", null), Array(11, 5, 30, null, "+01:00", "Europe/Paris", "11:05:30", null), Array(11, 5, 30, 500000000, "+01:00", "Europe/Paris", "11:05:30.5", null), Array(11, 5, 30, 1, "+01:00", "Europe/Paris", "11:05:30.000000001", null), Array(11, 5, null, null, null, "Europe/Paris", "11:05", null), Array(11, 5, 30, null, null, "Europe/Paris", "11:05:30", null), Array(11, 5, 30, 500000000, null, "Europe/Paris", "11:05:30.5", null), Array(11, 5, 30, 1, null, "Europe/Paris", "11:05:30.000000001", null))
  }

  @Test(dataProvider = "sample_isoLocalTime") def test_print_isoLocalTime(hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, expected: String, expectedEx: Class[_]): Unit = {
    val test: TemporalAccessor = buildAccessor(null, null, null, hour, min, sec, nano, offsetId, zoneId)
    if (expectedEx == null) {
      assertEquals(DateTimeFormatter.ISO_LOCAL_TIME.format(test), expected)
    }
    else {
      try {
        DateTimeFormatter.ISO_LOCAL_TIME.format(test)
        fail
      }
      catch {
        case ex: Exception =>
          assertTrue(expectedEx.isInstance(ex))
      }
    }
  }

  @Test(dataProvider = "sample_isoLocalTime") def test_parse_isoLocalTime(hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, input: String, invalid: Class[_]): Unit = {
    if (input != null) {
      val expected: TestDateTimeFormatters.Expected = createTime(hour, min, sec, nano)
      assertParseMatch(DateTimeFormatter.ISO_LOCAL_TIME.parseUnresolved(input, new ParsePosition(0)), expected)
    }
  }

  @DataProvider(name = "sample_isoOffsetTime") private[format] def provider_sample_isoOffsetTime: Array[Array[Any]] = {
    Array[Array[Any]](Array(11, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, 5, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, 30, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, 1, null, null, null, classOf[DateTimeException]), Array(null, null, null, null, "+01:00", null, null, classOf[DateTimeException]), Array(null, null, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(11, 5, 30, null, null, null, null, classOf[DateTimeException]), Array(11, 5, 30, 500000000, null, null, null, classOf[DateTimeException]), Array(11, 5, 30, 1, null, null, null, classOf[DateTimeException]), Array(11, 5, null, null, "+01:00", null, "11:05+01:00", null), Array(11, 5, 30, null, "+01:00", null, "11:05:30+01:00", null), Array(11, 5, 30, 500000000, "+01:00", null, "11:05:30.5+01:00", null), Array(11, 5, 30, 1, "+01:00", null, "11:05:30.000000001+01:00", null), Array(11, 5, null, null, "+01:00", "Europe/Paris", "11:05+01:00", null), Array(11, 5, 30, null, "+01:00", "Europe/Paris", "11:05:30+01:00", null), Array(11, 5, 30, 500000000, "+01:00", "Europe/Paris", "11:05:30.5+01:00", null), Array(11, 5, 30, 1, "+01:00", "Europe/Paris", "11:05:30.000000001+01:00", null), Array(11, 5, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(11, 5, 30, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(11, 5, 30, 500000000, null, "Europe/Paris", null, classOf[DateTimeException]), Array(11, 5, 30, 1, null, "Europe/Paris", null, classOf[DateTimeException]))
  }

  @Test(dataProvider = "sample_isoOffsetTime") def test_print_isoOffsetTime(hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, expected: String, expectedEx: Class[_]): Unit = {
    val test: TemporalAccessor = buildAccessor(null, null, null, hour, min, sec, nano, offsetId, zoneId)
    if (expectedEx == null) {
      assertEquals(DateTimeFormatter.ISO_OFFSET_TIME.format(test), expected)
    }
    else {
      try {
        DateTimeFormatter.ISO_OFFSET_TIME.format(test)
        fail
      }
      catch {
        case ex: Exception =>
          assertTrue(expectedEx.isInstance(ex))
      }
    }
  }

  @Test(dataProvider = "sample_isoOffsetTime") def test_parse_isoOffsetTime(hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, input: String, invalid: Class[_]): Unit = {
    if (input != null) {
      val expected: TestDateTimeFormatters.Expected = createTime(hour, min, sec, nano)
      buildCalendrical(expected, offsetId, null)
      assertParseMatch(DateTimeFormatter.ISO_OFFSET_TIME.parseUnresolved(input, new ParsePosition(0)), expected)
    }
  }

  @DataProvider(name = "sample_isoTime") private[format] def provider_sample_isoTime: Array[Array[Any]] = {
    Array[Array[Any]](Array(11, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, 5, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, 30, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, 1, null, null, null, classOf[DateTimeException]), Array(null, null, null, null, "+01:00", null, null, classOf[DateTimeException]), Array(null, null, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(11, 5, null, null, null, null, "11:05", null), Array(11, 5, 30, null, null, null, "11:05:30", null), Array(11, 5, 30, 500000000, null, null, "11:05:30.5", null), Array(11, 5, 30, 1, null, null, "11:05:30.000000001", null), Array(11, 5, null, null, "+01:00", null, "11:05+01:00", null), Array(11, 5, 30, null, "+01:00", null, "11:05:30+01:00", null), Array(11, 5, 30, 500000000, "+01:00", null, "11:05:30.5+01:00", null), Array(11, 5, 30, 1, "+01:00", null, "11:05:30.000000001+01:00", null), Array(11, 5, null, null, "+01:00", "Europe/Paris", "11:05+01:00", null), Array(11, 5, 30, null, "+01:00", "Europe/Paris", "11:05:30+01:00", null), Array(11, 5, 30, 500000000, "+01:00", "Europe/Paris", "11:05:30.5+01:00", null), Array(11, 5, 30, 1, "+01:00", "Europe/Paris", "11:05:30.000000001+01:00", null), Array(11, 5, null, null, null, "Europe/Paris", "11:05", null), Array(11, 5, 30, null, null, "Europe/Paris", "11:05:30", null), Array(11, 5, 30, 500000000, null, "Europe/Paris", "11:05:30.5", null), Array(11, 5, 30, 1, null, "Europe/Paris", "11:05:30.000000001", null))
  }

  @Test(dataProvider = "sample_isoTime") def test_print_isoTime(hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, expected: String, expectedEx: Class[_]): Unit = {
    val test: TemporalAccessor = buildAccessor(null, null, null, hour, min, sec, nano, offsetId, zoneId)
    if (expectedEx == null) {
      assertEquals(DateTimeFormatter.ISO_TIME.format(test), expected)
    }
    else {
      try {
        DateTimeFormatter.ISO_TIME.format(test)
        fail
      }
      catch {
        case ex: Exception =>
          assertTrue(expectedEx.isInstance(ex))
      }
    }
  }

  @Test(dataProvider = "sample_isoTime") def test_parse_isoTime(hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, input: String, invalid: Class[_]): Unit = {
    if (input != null) {
      val expected: TestDateTimeFormatters.Expected = createTime(hour, min, sec, nano)
      if (offsetId != null) {
        expected.fieldValues.put(OFFSET_SECONDS, ZoneOffset.of(offsetId).getTotalSeconds.toLong)
      }
      assertParseMatch(DateTimeFormatter.ISO_TIME.parseUnresolved(input, new ParsePosition(0)), expected)
    }
  }

  @DataProvider(name = "sample_isoLocalDateTime") private[format] def provider_sample_isoLocalDateTime: Array[Array[Any]] = {
    Array[Array[Any]](Array(2008, null, null, null, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, 6, null, null, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, 30, null, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, 11, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, null, 5, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, null, null, null, null, "+01:00", null, null, classOf[DateTimeException]), Array(null, null, null, null, null, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, 30, 11, null, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, null, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, null, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, null, 30, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(null, 6, 30, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, null, null, null, null, "2008-06-30T11:05", null), Array(2008, 6, 30, 11, 5, 30, null, null, null, "2008-06-30T11:05:30", null), Array(2008, 6, 30, 11, 5, 30, 500000000, null, null, "2008-06-30T11:05:30.5", null), Array(2008, 6, 30, 11, 5, 30, 1, null, null, "2008-06-30T11:05:30.000000001", null), Array(2008, 6, 30, 11, 5, null, null, "+01:00", null, "2008-06-30T11:05", null), Array(2008, 6, 30, 11, 5, 30, null, "+01:00", null, "2008-06-30T11:05:30", null), Array(2008, 6, 30, 11, 5, 30, 500000000, "+01:00", null, "2008-06-30T11:05:30.5", null), Array(2008, 6, 30, 11, 5, 30, 1, "+01:00", null, "2008-06-30T11:05:30.000000001", null), Array(2008, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris", "2008-06-30T11:05", null), Array(2008, 6, 30, 11, 5, 30, null, "+01:00", "Europe/Paris", "2008-06-30T11:05:30", null), Array(2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "Europe/Paris", "2008-06-30T11:05:30.5", null), Array(2008, 6, 30, 11, 5, 30, 1, "+01:00", "Europe/Paris", "2008-06-30T11:05:30.000000001", null), Array(2008, 6, 30, 11, 5, null, null, null, "Europe/Paris", "2008-06-30T11:05", null), Array(2008, 6, 30, 11, 5, 30, null, null, "Europe/Paris", "2008-06-30T11:05:30", null), Array(2008, 6, 30, 11, 5, 30, 500000000, null, "Europe/Paris", "2008-06-30T11:05:30.5", null), Array(2008, 6, 30, 11, 5, 30, 1, null, "Europe/Paris", "2008-06-30T11:05:30.000000001", null), Array(123456, 6, 30, 11, 5, null, null, null, null, "+123456-06-30T11:05", null))
  }

  @Test(dataProvider = "sample_isoLocalDateTime") def test_print_isoLocalDateTime(year: Integer, month: Integer, day: Integer, hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, expected: String, expectedEx: Class[_]): Unit = {
    val test: TemporalAccessor = buildAccessor(year, month, day, hour, min, sec, nano, offsetId, zoneId)
    if (expectedEx == null) {
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(test), expected)
    }
    else {
      try {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(test)
        fail
      }
      catch {
        case ex: Exception =>
          assertTrue(expectedEx.isInstance(ex))
      }
    }
  }

  @Test(dataProvider = "sample_isoLocalDateTime") def test_parse_isoLocalDateTime(year: Integer, month: Integer, day: Integer, hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, input: String, invalid: Class[_]): Unit = {
    if (input != null) {
      val expected: TestDateTimeFormatters.Expected = createDateTime(year, month, day, hour, min, sec, nano)
      assertParseMatch(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parseUnresolved(input, new ParsePosition(0)), expected)
    }
  }

  @DataProvider(name = "sample_isoOffsetDateTime") private[format] def provider_sample_isoOffsetDateTime: Array[Array[Any]] = {
    Array[Array[Any]](Array(2008, null, null, null, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, 6, null, null, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, 30, null, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, 11, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, null, 5, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, null, null, null, null, "+01:00", null, null, classOf[DateTimeException]), Array(null, null, null, null, null, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, 30, 11, null, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, null, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, null, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, null, 30, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(null, 6, 30, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, 30, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, 30, 500000000, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, 30, 1, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, null, null, "+01:00", null, "2008-06-30T11:05+01:00", null), Array(2008, 6, 30, 11, 5, 30, null, "+01:00", null, "2008-06-30T11:05:30+01:00", null), Array(2008, 6, 30, 11, 5, 30, 500000000, "+01:00", null, "2008-06-30T11:05:30.5+01:00", null), Array(2008, 6, 30, 11, 5, 30, 1, "+01:00", null, "2008-06-30T11:05:30.000000001+01:00", null), Array(2008, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris", "2008-06-30T11:05+01:00", null), Array(2008, 6, 30, 11, 5, 30, null, "+01:00", "Europe/Paris", "2008-06-30T11:05:30+01:00", null), Array(2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "Europe/Paris", "2008-06-30T11:05:30.5+01:00", null), Array(2008, 6, 30, 11, 5, 30, 1, "+01:00", "Europe/Paris", "2008-06-30T11:05:30.000000001+01:00", null), Array(2008, 6, 30, 11, 5, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, 30, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, 30, 500000000, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, 30, 1, null, "Europe/Paris", null, classOf[DateTimeException]), Array(123456, 6, 30, 11, 5, null, null, "+01:00", null, "+123456-06-30T11:05+01:00", null))
  }

  @Test(dataProvider = "sample_isoOffsetDateTime") def test_print_isoOffsetDateTime(year: Integer, month: Integer, day: Integer, hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, expected: String, expectedEx: Class[_]): Unit = {
    val test: TemporalAccessor = buildAccessor(year, month, day, hour, min, sec, nano, offsetId, zoneId)
    if (expectedEx == null) {
      assertEquals(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(test), expected)
    }
    else {
      try {
        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(test)
        fail
      }
      catch {
        case ex: Exception =>
          assertTrue(expectedEx.isInstance(ex))
      }
    }
  }

  @Test(dataProvider = "sample_isoOffsetDateTime") def test_parse_isoOffsetDateTime(year: Integer, month: Integer, day: Integer, hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, input: String, invalid: Class[_]): Unit = {
    if (input != null) {
      val expected: TestDateTimeFormatters.Expected = createDateTime(year, month, day, hour, min, sec, nano)
      buildCalendrical(expected, offsetId, null)
      assertParseMatch(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parseUnresolved(input, new ParsePosition(0)), expected)
    }
  }

  @DataProvider(name = "sample_isoZonedDateTime") private[format] def provider_sample_isoZonedDateTime: Array[Array[Any]] = {
    Array[Array[Any]](Array(2008, null, null, null, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, 6, null, null, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, 30, null, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, 11, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, null, 5, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, null, null, null, null, "+01:00", null, null, classOf[DateTimeException]), Array(null, null, null, null, null, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, 30, 11, null, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, null, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, null, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, null, 30, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(null, 6, 30, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, 30, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, 30, 500000000, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, 30, 1, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, null, null, "+01:00", null, "2008-06-30T11:05+01:00", null), Array(2008, 6, 30, 11, 5, 30, null, "+01:00", null, "2008-06-30T11:05:30+01:00", null), Array(2008, 6, 30, 11, 5, 30, 500000000, "+01:00", null, "2008-06-30T11:05:30.5+01:00", null), Array(2008, 6, 30, 11, 5, 30, 1, "+01:00", null, "2008-06-30T11:05:30.000000001+01:00", null), Array(2008, 6, 30, 11, 5, null, null, "+01:00", "+01:00", "2008-06-30T11:05+01:00", null), Array(2008, 6, 30, 11, 5, 30, null, "+01:00", "+01:00", "2008-06-30T11:05:30+01:00", null), Array(2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "+01:00", "2008-06-30T11:05:30.5+01:00", null), Array(2008, 6, 30, 11, 5, 30, 1, "+01:00", "+01:00", "2008-06-30T11:05:30.000000001+01:00", null), Array(2008, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris", "2008-06-30T11:05+01:00[Europe/Paris]", null), Array(2008, 6, 30, 11, 5, 30, null, "+01:00", "Europe/Paris", "2008-06-30T11:05:30+01:00[Europe/Paris]", null), Array(2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "Europe/Paris", "2008-06-30T11:05:30.5+01:00[Europe/Paris]", null), Array(2008, 6, 30, 11, 5, 30, 1, "+01:00", "Europe/Paris", "2008-06-30T11:05:30.000000001+01:00[Europe/Paris]", null), Array(2008, 6, 30, 11, 5, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, 30, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, 30, 500000000, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, 30, 1, null, "Europe/Paris", null, classOf[DateTimeException]), Array(123456, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris", "+123456-06-30T11:05+01:00[Europe/Paris]", null))
  }

  @Test(dataProvider = "sample_isoZonedDateTime") def test_print_isoZonedDateTime(year: Integer, month: Integer, day: Integer, hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, expected: String, expectedEx: Class[_]): Unit = {
    val test: TemporalAccessor = buildAccessor(year, month, day, hour, min, sec, nano, offsetId, zoneId)
    if (expectedEx == null) {
      assertEquals(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(test), expected)
    }
    else {
      try {
        DateTimeFormatter.ISO_ZONED_DATE_TIME.format(test)
        fail(test.toString)
      }
      catch {
        case ex: Exception =>
          assertTrue(expectedEx.isInstance(ex))
      }
    }
  }

  @Test(dataProvider = "sample_isoZonedDateTime") def test_parse_isoZonedDateTime(year: Integer, month: Integer, day: Integer, hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, input: String, invalid: Class[_]): Unit = {
    if (input != null) {
      val expected: TestDateTimeFormatters.Expected = createDateTime(year, month, day, hour, min, sec, nano)
      if (offsetId == zoneId) {
        buildCalendrical(expected, offsetId, null)
      }
      else {
        buildCalendrical(expected, offsetId, zoneId)
      }
      assertParseMatch(DateTimeFormatter.ISO_ZONED_DATE_TIME.parseUnresolved(input, new ParsePosition(0)), expected)
    }
  }

  @DataProvider(name = "sample_isoDateTime") private[format] def provider_sample_isoDateTime: Array[Array[Any]] = {
    Array[Array[Any]](Array(2008, null, null, null, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, 6, null, null, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, 30, null, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, 11, null, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, null, 5, null, null, null, null, null, classOf[DateTimeException]), Array(null, null, null, null, null, null, null, "+01:00", null, null, classOf[DateTimeException]), Array(null, null, null, null, null, null, null, null, "Europe/Paris", null, classOf[DateTimeException]), Array(2008, 6, 30, 11, null, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, null, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, null, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, null, 30, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(null, 6, 30, 11, 5, null, null, null, null, null, classOf[DateTimeException]), Array(2008, 6, 30, 11, 5, null, null, null, null, "2008-06-30T11:05", null), Array(2008, 6, 30, 11, 5, 30, null, null, null, "2008-06-30T11:05:30", null), Array(2008, 6, 30, 11, 5, 30, 500000000, null, null, "2008-06-30T11:05:30.5", null), Array(2008, 6, 30, 11, 5, 30, 1, null, null, "2008-06-30T11:05:30.000000001", null), Array(2008, 6, 30, 11, 5, null, null, "+01:00", null, "2008-06-30T11:05+01:00", null), Array(2008, 6, 30, 11, 5, 30, null, "+01:00", null, "2008-06-30T11:05:30+01:00", null), Array(2008, 6, 30, 11, 5, 30, 500000000, "+01:00", null, "2008-06-30T11:05:30.5+01:00", null), Array(2008, 6, 30, 11, 5, 30, 1, "+01:00", null, "2008-06-30T11:05:30.000000001+01:00", null), Array(2008, 6, 30, 11, 5, null, null, "+01:00", "Europe/Paris", "2008-06-30T11:05+01:00[Europe/Paris]", null), Array(2008, 6, 30, 11, 5, 30, null, "+01:00", "Europe/Paris", "2008-06-30T11:05:30+01:00[Europe/Paris]", null), Array(2008, 6, 30, 11, 5, 30, 500000000, "+01:00", "Europe/Paris", "2008-06-30T11:05:30.5+01:00[Europe/Paris]", null), Array(2008, 6, 30, 11, 5, 30, 1, "+01:00", "Europe/Paris", "2008-06-30T11:05:30.000000001+01:00[Europe/Paris]", null), Array(2008, 6, 30, 11, 5, null, null, null, "Europe/Paris", "2008-06-30T11:05", null), Array(2008, 6, 30, 11, 5, 30, null, null, "Europe/Paris", "2008-06-30T11:05:30", null), Array(2008, 6, 30, 11, 5, 30, 500000000, null, "Europe/Paris", "2008-06-30T11:05:30.5", null), Array(2008, 6, 30, 11, 5, 30, 1, null, "Europe/Paris", "2008-06-30T11:05:30.000000001", null), Array(123456, 6, 30, 11, 5, null, null, null, null, "+123456-06-30T11:05", null))
  }

  @Test(dataProvider = "sample_isoDateTime") def test_print_isoDateTime(year: Integer, month: Integer, day: Integer, hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, expected: String, expectedEx: Class[_]): Unit = {
    val test: TemporalAccessor = buildAccessor(year, month, day, hour, min, sec, nano, offsetId, zoneId)
    if (expectedEx == null) {
      assertEquals(DateTimeFormatter.ISO_DATE_TIME.format(test), expected)
    }
    else {
      try {
        DateTimeFormatter.ISO_DATE_TIME.format(test)
        fail
      }
      catch {
        case ex: Exception =>
          assertTrue(expectedEx.isInstance(ex))
      }
    }
  }

  @Test(dataProvider = "sample_isoDateTime") def test_parse_isoDateTime(year: Integer, month: Integer, day: Integer, hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String, input: String, invalid: Class[_]): Unit = {
    if (input != null) {
      val expected: TestDateTimeFormatters.Expected = createDateTime(year, month, day, hour, min, sec, nano)
      if (offsetId != null) {
        expected.fieldValues.put(OFFSET_SECONDS, ZoneOffset.of(offsetId).getTotalSeconds.toLong)
        if (zoneId != null) {
          expected.zone = ZoneId.of(zoneId)
        }
      }
      assertParseMatch(DateTimeFormatter.ISO_DATE_TIME.parseUnresolved(input, new ParsePosition(0)), expected)
    }
  }

  @Test def test_print_isoOrdinalDate(): Unit = {
    val test: TemporalAccessor = buildAccessor(LocalDateTime.of(2008, 6, 3, 11, 5, 30), null, null)
    assertEquals(DateTimeFormatter.ISO_ORDINAL_DATE.format(test), "2008-155")
  }

  @Test def test_print_isoOrdinalDate_offset(): Unit = {
    val test: TemporalAccessor = buildAccessor(LocalDateTime.of(2008, 6, 3, 11, 5, 30), "Z", null)
    assertEquals(DateTimeFormatter.ISO_ORDINAL_DATE.format(test), "2008-155Z")
  }

  @Test def test_print_isoOrdinalDate_zoned(): Unit = {
    val test: TemporalAccessor = buildAccessor(LocalDateTime.of(2008, 6, 3, 11, 5, 30), "+02:00", "Europe/Paris")
    assertEquals(DateTimeFormatter.ISO_ORDINAL_DATE.format(test), "2008-155+02:00")
  }

  @Test def test_print_isoOrdinalDate_zoned_largeYear(): Unit = {
    val test: TemporalAccessor = buildAccessor(LocalDateTime.of(123456, 6, 3, 11, 5, 30), "Z", null)
    assertEquals(DateTimeFormatter.ISO_ORDINAL_DATE.format(test), "+123456-155Z")
  }

  @Test def test_print_isoOrdinalDate_fields(): Unit = {
    val test: TemporalAccessor = new TemporalAccessor() {
      def isSupported(field: TemporalField): Boolean = (field eq YEAR) || (field eq DAY_OF_YEAR)

      def getLong(field: TemporalField): Long = {
        if (field eq YEAR) {
          return 2008
        }
        if (field eq DAY_OF_YEAR) {
          return 231
        }
        throw new DateTimeException("Unsupported")
      }
    }
    assertEquals(DateTimeFormatter.ISO_ORDINAL_DATE.format(test), "2008-231")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_print_isoOrdinalDate_missingField(): Unit = {
    val test: TemporalAccessor = Year.of(2008)
    DateTimeFormatter.ISO_ORDINAL_DATE.format(test)
  }

  @Test def test_parse_isoOrdinalDate(): Unit = {
    val expected: TestDateTimeFormatters.Expected = new TestDateTimeFormatters.Expected(YEAR, 2008, DAY_OF_YEAR, 123)
    assertParseMatch(DateTimeFormatter.ISO_ORDINAL_DATE.parseUnresolved("2008-123", new ParsePosition(0)), expected)
  }

  @Test def test_parse_isoOrdinalDate_largeYear(): Unit = {
    val expected: TestDateTimeFormatters.Expected = new TestDateTimeFormatters.Expected(YEAR, 123456, DAY_OF_YEAR, 123)
    assertParseMatch(DateTimeFormatter.ISO_ORDINAL_DATE.parseUnresolved("+123456-123", new ParsePosition(0)), expected)
  }

  @Test def test_print_basicIsoDate(): Unit = {
    val test: TemporalAccessor = buildAccessor(LocalDateTime.of(2008, 6, 3, 11, 5, 30), null, null)
    assertEquals(DateTimeFormatter.BASIC_ISO_DATE.format(test), "20080603")
  }

  @Test def test_print_basicIsoDate_offset(): Unit = {
    val test: TemporalAccessor = buildAccessor(LocalDateTime.of(2008, 6, 3, 11, 5, 30), "Z", null)
    assertEquals(DateTimeFormatter.BASIC_ISO_DATE.format(test), "20080603Z")
  }

  @Test def test_print_basicIsoDate_zoned(): Unit = {
    val test: TemporalAccessor = buildAccessor(LocalDateTime.of(2008, 6, 3, 11, 5, 30), "+02:00", "Europe/Paris")
    assertEquals(DateTimeFormatter.BASIC_ISO_DATE.format(test), "20080603+0200")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_print_basicIsoDate_largeYear(): Unit = {
    val test: TemporalAccessor = buildAccessor(LocalDateTime.of(123456, 6, 3, 11, 5, 30), "Z", null)
    DateTimeFormatter.BASIC_ISO_DATE.format(test)
  }

  @Test def test_print_basicIsoDate_fields(): Unit = {
    val test: TemporalAccessor = buildAccessor(LocalDate.of(2008, 6, 3), null, null)
    assertEquals(DateTimeFormatter.BASIC_ISO_DATE.format(test), "20080603")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_print_basicIsoDate_missingField(): Unit = {
    val test: TemporalAccessor = YearMonth.of(2008, 6)
    DateTimeFormatter.BASIC_ISO_DATE.format(test)
  }

  @Test def test_parse_basicIsoDate(): Unit = {
    val expected: LocalDate = LocalDate.of(2008, 6, 3)
    assertEquals(DateTimeFormatter.BASIC_ISO_DATE.parse("20080603", (temporal: TemporalAccessor) => LocalDate.from(temporal)), expected)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeParseException])) def test_parse_basicIsoDate_largeYear(): Unit = {
    try {
      val expected: LocalDate = LocalDate.of(123456, 6, 3)
      assertEquals(DateTimeFormatter.BASIC_ISO_DATE.parse("+1234560603", (temporal: TemporalAccessor) => LocalDate.from(temporal)), expected)
    }
    catch {
      case ex: DateTimeParseException =>
        assertEquals(ex.getErrorIndex, 0)
        assertEquals(ex.getParsedString, "+1234560603")
        throw ex
    }
  }

  @DataProvider(name = "weekDate") private[format] def weekDate: java.util.Iterator[Array[AnyRef]] = {
    new java.util.Iterator[Array[AnyRef]]() {
      private var date: ZonedDateTime = ZonedDateTime.of(LocalDateTime.of(2003, 12, 29, 11, 5, 30), ZoneId.of("Europe/Paris"))
      private var endDate: ZonedDateTime = date.withYear(2005).withMonth(1).withDayOfMonth(2)
      private var week: Int = 1
      private var day: Int = 1

      def hasNext: Boolean = {
        !date.isAfter(endDate)
      }

      def next: Array[AnyRef] = {
        val sb: StringBuilder = new StringBuilder("2004-W")
        if (week < 10) {
          sb.append('0')
        }
        sb.append(week).append('-').append(day).append(date.getOffset)
        val ret: Array[AnyRef] = Array[AnyRef](date, sb.toString)
        date = date.plusDays(1)
        day += 1
        if (day == 8) {
          day = 1
          week += 1
        }
        ret
      }

      override def remove(): Unit = {
        throw new UnsupportedOperationException
      }
    }
  }

  @Test(dataProvider = "weekDate") def test_print_isoWeekDate(test: TemporalAccessor, expected: String): Unit = {
    assertEquals(DateTimeFormatter.ISO_WEEK_DATE.format(test), expected)
  }

  @Test def test_print_isoWeekDate_zoned_largeYear(): Unit = {
    val test: TemporalAccessor = buildAccessor(LocalDateTime.of(123456, 6, 3, 11, 5, 30), "Z", null)
    assertEquals(DateTimeFormatter.ISO_WEEK_DATE.format(test), "+123456-W23-2Z")
  }

  @Test def test_print_isoWeekDate_fields(): Unit = {
    val test: TemporalAccessor = buildAccessor(LocalDate.of(2004, 1, 27), null, null)
    assertEquals(DateTimeFormatter.ISO_WEEK_DATE.format(test), "2004-W05-2")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_print_isoWeekDate_missingField(): Unit = {
    val test: TemporalAccessor = YearMonth.of(2008, 6)
    DateTimeFormatter.ISO_WEEK_DATE.format(test)
  }

  @Test def test_parse_weekDate(): Unit = {
    val expected: LocalDate = LocalDate.of(2004, 1, 28)
    assertEquals(DateTimeFormatter.ISO_WEEK_DATE.parse("2004-W05-3", (temporal: TemporalAccessor) => LocalDate.from(temporal)), expected)
  }

  @Test def test_parse_weekDate_largeYear(): Unit = {
    val parsed: TemporalAccessor = DateTimeFormatter.ISO_WEEK_DATE.parseUnresolved("+123456-W04-5", new ParsePosition(0))
    assertEquals(parsed.get(IsoFields.WEEK_BASED_YEAR), 123456)
    assertEquals(parsed.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), 4)
    assertEquals(parsed.get(DAY_OF_WEEK), 5)
  }

  @DataProvider(name = "rfc") private[format] def data_rfc: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(LocalDateTime.of(2008, 6, 3, 11, 5, 30), "Z", "Tue, 3 Jun 2008 11:05:30 GMT"), Array(LocalDateTime.of(2008, 6, 30, 11, 5, 30), "Z", "Mon, 30 Jun 2008 11:05:30 GMT"), Array(LocalDateTime.of(2008, 6, 3, 11, 5, 30), "+02:00", "Tue, 3 Jun 2008 11:05:30 +0200"), Array(LocalDateTime.of(2008, 6, 30, 11, 5, 30), "-03:00", "Mon, 30 Jun 2008 11:05:30 -0300"))
  }

  @Test(dataProvider = "rfc") def test_print_rfc1123(base: LocalDateTime, offsetId: String, expected: String): Unit = {
    val test: TemporalAccessor = buildAccessor(base, offsetId, null)
    assertEquals(DateTimeFormatter.RFC_1123_DATE_TIME.format(test), expected)
  }

  @Test(dataProvider = "rfc") def test_print_rfc1123_french(base: LocalDateTime, offsetId: String, expected: String): Unit = {
    val test: TemporalAccessor = buildAccessor(base, offsetId, null)
    assertEquals(DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.FRENCH).format(test), expected)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def test_print_rfc1123_missingField(): Unit = {
    val test: TemporalAccessor = YearMonth.of(2008, 6)
    DateTimeFormatter.RFC_1123_DATE_TIME.format(test)
  }

  private def createDate(year: Integer, month: Integer, day: Integer): TestDateTimeFormatters.Expected = {
    val test: TestDateTimeFormatters.Expected = new TestDateTimeFormatters.Expected
    if (year != null) {
      test.fieldValues.put(YEAR, year.toLong)
    }
    if (month != null) {
      test.fieldValues.put(MONTH_OF_YEAR, month.toLong)
    }
    if (day != null) {
      test.fieldValues.put(DAY_OF_MONTH, day.toLong)
    }
    test
  }

  private def createTime(hour: Integer, min: Integer, sec: Integer, nano: Integer): TestDateTimeFormatters.Expected = {
    val test: TestDateTimeFormatters.Expected = new TestDateTimeFormatters.Expected
    if (hour != null) {
      test.fieldValues.put(HOUR_OF_DAY, hour.toLong)
    }
    if (min != null) {
      test.fieldValues.put(MINUTE_OF_HOUR, min.toLong)
    }
    if (sec != null) {
      test.fieldValues.put(SECOND_OF_MINUTE, sec.toLong)
    }
    if (nano != null) {
      test.fieldValues.put(NANO_OF_SECOND, nano.toLong)
    }
    test
  }

  private def createDateTime(year: Integer, month: Integer, day: Integer, hour: Integer, min: Integer, sec: Integer, nano: Integer): TestDateTimeFormatters.Expected = {
    val test: TestDateTimeFormatters.Expected = new TestDateTimeFormatters.Expected
    if (year != null) {
      test.fieldValues.put(YEAR, year.toLong)
    }
    if (month != null) {
      test.fieldValues.put(MONTH_OF_YEAR, month.toLong)
    }
    if (day != null) {
      test.fieldValues.put(DAY_OF_MONTH, day.toLong)
    }
    if (hour != null) {
      test.fieldValues.put(HOUR_OF_DAY, hour.toLong)
    }
    if (min != null) {
      test.fieldValues.put(MINUTE_OF_HOUR, min.toLong)
    }
    if (sec != null) {
      test.fieldValues.put(SECOND_OF_MINUTE, sec.toLong)
    }
    if (nano != null) {
      test.fieldValues.put(NANO_OF_SECOND, nano.toLong)
    }
    test
  }

  private def buildAccessor(year: Integer, month: Integer, day: Integer, hour: Integer, min: Integer, sec: Integer, nano: Integer, offsetId: String, zoneId: String): TemporalAccessor = {
    val mock: TestDateTimeFormatters.MockAccessor = new TestDateTimeFormatters.MockAccessor
    if (year != null) {
      mock.fields.put(YEAR, year.toLong)
    }
    if (month != null) {
      mock.fields.put(MONTH_OF_YEAR, month.toLong)
    }
    if (day != null) {
      mock.fields.put(DAY_OF_MONTH, day.toLong)
    }
    if (hour != null) {
      mock.fields.put(HOUR_OF_DAY, hour.toLong)
    }
    if (min != null) {
      mock.fields.put(MINUTE_OF_HOUR, min.toLong)
    }
    if (sec != null) {
      mock.fields.put(SECOND_OF_MINUTE, sec.toLong)
    }
    if (nano != null) {
      mock.fields.put(NANO_OF_SECOND, nano.toLong)
    }
    mock.setOffset(offsetId)
    mock.setZone(zoneId)
    mock
  }

  private def buildAccessor(base: LocalDateTime, offsetId: String, zoneId: String): TemporalAccessor = {
    val mock: TestDateTimeFormatters.MockAccessor = new TestDateTimeFormatters.MockAccessor
    mock.setFields(base)
    mock.setOffset(offsetId)
    mock.setZone(zoneId)
    mock
  }

  private def buildAccessor(base: LocalDate, offsetId: String, zoneId: String): TemporalAccessor = {
    val mock: TestDateTimeFormatters.MockAccessor = new TestDateTimeFormatters.MockAccessor
    mock.setFields(base)
    mock.setOffset(offsetId)
    mock.setZone(zoneId)
    mock
  }

  private def buildCalendrical(expected: TestDateTimeFormatters.Expected, offsetId: String, zoneId: String): Unit = {
    if (offsetId != null) {
      expected.add(ZoneOffset.of(offsetId))
    }
    if (zoneId != null) {
      expected.zone = ZoneId.of(zoneId)
    }
  }

  private def assertParseMatch(parsed: TemporalAccessor, expected: TestDateTimeFormatters.Expected): Unit = {
    import scala.collection.JavaConversions._
    for (field <- expected.fieldValues.keySet) {
      assertEquals(parsed.isSupported(field), true)
      parsed.getLong(field)
    }
    assertEquals(parsed.query(TemporalQueries.chronology), expected.chrono)
    assertEquals(parsed.query(TemporalQueries.zoneId), expected.zone)
  }
}
