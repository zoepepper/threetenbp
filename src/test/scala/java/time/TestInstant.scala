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
import org.testng.Assert.assertTrue
import java.time.temporal.ChronoField.INSTANT_SECONDS
import java.time.temporal.ChronoField.MICRO_OF_SECOND
import java.time.temporal.ChronoField.MILLI_OF_SECOND
import java.time.temporal.ChronoField.NANO_OF_SECOND
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.NANOS
import java.time.temporal.ChronoUnit.SECONDS
import java.util.Arrays
import java.util.Locale
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.JulianFields
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalField
import java.time.temporal.TemporalQueries

/**
  * Test Instant.
  */
@Test object TestInstant {
  private val MIN_SECOND: Long = Instant.MIN.getEpochSecond
  private val MAX_SECOND: Long = Instant.MAX.getEpochSecond
}

@Test class TestInstant extends AbstractDateTimeTest {
  private var TEST_12345_123456789: Instant = null

  @BeforeMethod def setUp(): Unit = {
    TEST_12345_123456789 = Instant.ofEpochSecond(12345, 123456789)
  }

  protected def samples: java.util.List[TemporalAccessor] = {
    val array: Array[TemporalAccessor] = Array(TEST_12345_123456789, Instant.MIN, Instant.MAX, Instant.EPOCH)
    Arrays.asList(array: _*)
  }

  protected def validFields: java.util.List[TemporalField] = {
    val array: Array[TemporalField] = Array(NANO_OF_SECOND, MICRO_OF_SECOND, MILLI_OF_SECOND, INSTANT_SECONDS)
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

  @Test
  @throws(classOf[Exception])
  def test_serialization(): Unit = {
    AbstractTest.assertSerializable(Instant.ofEpochMilli(134l))
  }

  @Test
  @throws(classOf[Exception])
  def test_serialization_format(): Unit = {
    AbstractTest.assertEqualsSerialisedForm(Instant.ofEpochMilli(1347830279338l))
  }

  private def check(instant: Instant, epochSecs: Long, nos: Int): Unit = {
    assertEquals(instant.getEpochSecond, epochSecs)
    assertEquals(instant.getNano, nos)
    assertEquals(instant, instant)
    assertEquals(instant.hashCode, instant.hashCode)
  }

  @Test def constant_EPOCH(): Unit = {
    check(Instant.EPOCH, 0, 0)
  }

  @Test def constant_MIN(): Unit = {
    check(Instant.MIN, -31557014167219200L, 0)
  }

  @Test def constant_MAX(): Unit = {
    check(Instant.MAX, 31556889864403199L, 999999999)
  }

  @Test def now(): Unit = {
    val expected: Instant = Instant.now(Clock.systemUTC)
    val test: Instant = Instant.now
    val diff: Long = Math.abs(test.toEpochMilli - expected.toEpochMilli)
    assertTrue(diff < 100)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def now_Clock_nullClock(): Unit = {
    Instant.now(null)
  }

  @Test def now_Clock_allSecsInDay_utc(): Unit = {
    {
      var i: Int = 0
      while (i < (2 * 24 * 60 * 60)) {
        {
          val expected: Instant = Instant.ofEpochSecond(i).plusNanos(123456789L)
          val clock: Clock = Clock.fixed(expected, ZoneOffset.UTC)
          val test: Instant = Instant.now(clock)
          assertEquals(test, expected)
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
      while (i >= -(24 * 60 * 60)) {
        {
          val expected: Instant = Instant.ofEpochSecond(i).plusNanos(123456789L)
          val clock: Clock = Clock.fixed(expected, ZoneOffset.UTC)
          val test: Instant = Instant.now(clock)
          assertEquals(test, expected)
        }
        {
          i -= 1
          i + 1
        }
      }
    }
  }

  @Test def factory_seconds_long(): Unit = {
    {
      var i: Long = -2
      while (i <= 2) {
        {
          val t: Instant = Instant.ofEpochSecond(i)
          assertEquals(t.getEpochSecond, i)
          assertEquals(t.getNano, 0)
        }
        {
          i += 1
          i - 1
        }
      }
    }
  }

  @Test def factory_seconds_long_long(): Unit = {
    {
      var i: Long = -2
      while (i <= 2) {
        {
          {
            var j: Int = 0
            while (j < 10) {
              {
                val t: Instant = Instant.ofEpochSecond(i, j)
                assertEquals(t.getEpochSecond, i)
                assertEquals(t.getNano, j)
              }
              {
                j += 1
                j - 1
              }
            }
          }
          {
            var j: Int = -10
            while (j < 0) {
              {
                val t: Instant = Instant.ofEpochSecond(i, j)
                assertEquals(t.getEpochSecond, i - 1)
                assertEquals(t.getNano, j + 1000000000)
              }
              {
                j += 1
                j - 1
              }
            }
          }
          {
            var j: Int = 999999990
            while (j < 1000000000) {
              {
                val t: Instant = Instant.ofEpochSecond(i, j)
                assertEquals(t.getEpochSecond, i)
                assertEquals(t.getNano, j)
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

  @Test def factory_seconds_long_long_nanosNegativeAdjusted(): Unit = {
    val test: Instant = Instant.ofEpochSecond(2L, -1)
    assertEquals(test.getEpochSecond, 1)
    assertEquals(test.getNano, 999999999)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def factory_seconds_long_long_tooBig(): Unit = {
    Instant.ofEpochSecond(TestInstant.MAX_SECOND, 1000000000)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def factory_seconds_long_long_tooBigBig(): Unit = {
    Instant.ofEpochSecond(Long.MaxValue, Long.MaxValue)
  }

  @DataProvider(name = "MillisInstantNoNanos") private[time] def provider_factory_millis_long: Array[Array[Int]] = {
    Array[Array[Int]](Array(0, 0, 0), Array(1, 0, 1000000), Array(2, 0, 2000000), Array(999, 0, 999000000), Array(1000, 1, 0), Array(1001, 1, 1000000), Array(-1, -1, 999000000), Array(-2, -1, 998000000), Array(-999, -1, 1000000), Array(-1000, -1, 0), Array(-1001, -2, 999000000))
  }

  @Test(dataProvider = "MillisInstantNoNanos") def factory_millis_long(millis: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val t: Instant = Instant.ofEpochMilli(millis)
    assertEquals(t.getEpochSecond, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @DataProvider(name = "Parse") private[time] def provider_factory_parse: Array[Array[Any]] = {
    Array[Array[Any]](Array("1970-01-01T00:00:00Z", 0, 0), Array("1970-01-01t00:00:00Z", 0, 0), Array("1970-01-01T00:00:00z", 0, 0), Array("1970-01-01T00:00:00.0Z", 0, 0), Array("1970-01-01T00:00:00.000000000Z", 0, 0), Array("1970-01-01T00:00:00.000000001Z", 0, 1), Array("1970-01-01T00:00:00.100000000Z", 0, 100000000), Array("1970-01-01T00:00:01Z", 1, 0), Array("1970-01-01T00:01:00Z", 60, 0), Array("1970-01-01T00:01:01Z", 61, 0), Array("1970-01-01T00:01:01.000000001Z", 61, 1), Array("1970-01-01T01:00:00.000000000Z", 3600, 0), Array("1970-01-01T01:01:01.000000001Z", 3661, 1), Array("1970-01-02T01:01:01.100000000Z", 90061, 100000000))
  }

  @Test(dataProvider = "Parse") def factory_parse(text: String, expectedEpochSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val t: Instant = Instant.parse(text)
    assertEquals(t.getEpochSecond, expectedEpochSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(dataProvider = "Parse") def factory_parseLowercase(text: String, expectedEpochSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val t: Instant = Instant.parse(text.toLowerCase(Locale.ENGLISH))
    assertEquals(t.getEpochSecond, expectedEpochSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @DataProvider(name = "ParseFailures") private[time] def provider_factory_parseFailures: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(""), Array("Z"), Array("1970-01-01T00:00:00"), Array("1970-01-01T00:00:0Z"), Array("1970-01-01T00:00:00.0000000000Z"))
  }

  @Test(dataProvider = "ParseFailures", expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parseFailures(text: String): Unit = {
    Instant.parse(text)
  }

  @Test(dataProvider = "ParseFailures", expectedExceptions = Array(classOf[DateTimeParseException])) def factory_parseFailures_comma(text: String): Unit = {
    var _text = text
    _text = _text.replace('.', ',')
    Instant.parse(_text)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def factory_parse_nullText(): Unit = {
    Instant.parse(null)
  }

  @Test def test_get_TemporalField(): Unit = {
    val test: Instant = TEST_12345_123456789
    assertEquals(test.get(ChronoField.NANO_OF_SECOND), 123456789)
    assertEquals(test.get(ChronoField.MICRO_OF_SECOND), 123456)
    assertEquals(test.get(ChronoField.MILLI_OF_SECOND), 123)
  }

  @Test def test_getLong_TemporalField(): Unit = {
    val test: Instant = TEST_12345_123456789
    assertEquals(test.getLong(ChronoField.NANO_OF_SECOND), 123456789)
    assertEquals(test.getLong(ChronoField.MICRO_OF_SECOND), 123456)
    assertEquals(test.getLong(ChronoField.MILLI_OF_SECOND), 123)
    assertEquals(test.getLong(ChronoField.INSTANT_SECONDS), 12345)
  }

  @Test def test_query(): Unit = {
    assertEquals(TEST_12345_123456789.query(TemporalQueries.chronology), null)
    assertEquals(TEST_12345_123456789.query(TemporalQueries.localDate), null)
    assertEquals(TEST_12345_123456789.query(TemporalQueries.localTime), null)
    assertEquals(TEST_12345_123456789.query(TemporalQueries.offset), null)
    assertEquals(TEST_12345_123456789.query(TemporalQueries.precision), ChronoUnit.NANOS)
    assertEquals(TEST_12345_123456789.query(TemporalQueries.zone), null)
    assertEquals(TEST_12345_123456789.query(TemporalQueries.zoneId), null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_query_null(): Unit = {
    TEST_12345_123456789.query(null)
  }

  @DataProvider(name = "Plus") private[time] def provider_plus: Array[Array[Long]] = {
    Array[Array[Long]](Array(TestInstant.MIN_SECOND, 0, -TestInstant.MIN_SECOND, 0, 0, 0), Array(TestInstant.MIN_SECOND, 0, 1, 0, TestInstant.MIN_SECOND + 1, 0), Array(TestInstant.MIN_SECOND, 0, 0, 500, TestInstant.MIN_SECOND, 500), Array(TestInstant.MIN_SECOND, 0, 0, 1000000000, TestInstant.MIN_SECOND + 1, 0), Array(TestInstant.MIN_SECOND + 1, 0, -1, 0, TestInstant.MIN_SECOND, 0), Array(TestInstant.MIN_SECOND + 1, 0, 0, -500, TestInstant.MIN_SECOND, 999999500), Array(TestInstant.MIN_SECOND + 1, 0, 0, -1000000000, TestInstant.MIN_SECOND, 0), Array(-4, 666666667, -4, 666666667, -7, 333333334), Array(-4, 666666667, -3, 0, -7, 666666667), Array(-4, 666666667, -2, 0, -6, 666666667), Array(-4, 666666667, -1, 0, -5, 666666667), Array(-4, 666666667, -1, 333333334, -4, 1), Array(-4, 666666667, -1, 666666667, -4, 333333334), Array(-4, 666666667, -1, 999999999, -4, 666666666), Array(-4, 666666667, 0, 0, -4, 666666667), Array(-4, 666666667, 0, 1, -4, 666666668), Array(-4, 666666667, 0, 333333333, -3, 0), Array(-4, 666666667, 0, 666666666, -3, 333333333), Array(-4, 666666667, 1, 0, -3, 666666667), Array(-4, 666666667, 2, 0, -2, 666666667), Array(-4, 666666667, 3, 0, -1, 666666667), Array(-4, 666666667, 3, 333333333, 0, 0), Array(-3, 0, -4, 666666667, -7, 666666667), Array(-3, 0, -3, 0, -6, 0), Array(-3, 0, -2, 0, -5, 0), Array(-3, 0, -1, 0, -4, 0), Array(-3, 0, -1, 333333334, -4, 333333334), Array(-3, 0, -1, 666666667, -4, 666666667), Array(-3, 0, -1, 999999999, -4, 999999999), Array(-3, 0, 0, 0, -3, 0), Array(-3, 0, 0, 1, -3, 1), Array(-3, 0, 0, 333333333, -3, 333333333), Array(-3, 0, 0, 666666666, -3, 666666666), Array(-3, 0, 1, 0, -2, 0), Array(-3, 0, 2, 0, -1, 0), Array(-3, 0, 3, 0, 0, 0), Array(-3, 0, 3, 333333333, 0, 333333333), Array(-2, 0, -4, 666666667, -6, 666666667), Array(-2, 0, -3, 0, -5, 0), Array(-2, 0, -2, 0, -4, 0), Array(-2, 0, -1, 0, -3, 0), Array(-2, 0, -1, 333333334, -3, 333333334), Array(-2, 0, -1, 666666667, -3, 666666667), Array(-2, 0, -1, 999999999, -3, 999999999), Array(-2, 0, 0, 0, -2, 0), Array(-2, 0, 0, 1, -2, 1), Array(-2, 0, 0, 333333333, -2, 333333333), Array(-2, 0, 0, 666666666, -2, 666666666), Array(-2, 0, 1, 0, -1, 0), Array(-2, 0, 2, 0, 0, 0), Array(-2, 0, 3, 0, 1, 0), Array(-2, 0, 3, 333333333, 1, 333333333), Array(-1, 0, -4, 666666667, -5, 666666667), Array(-1, 0, -3, 0, -4, 0), Array(-1, 0, -2, 0, -3, 0), Array(-1, 0, -1, 0, -2, 0), Array(-1, 0, -1, 333333334, -2, 333333334), Array(-1, 0, -1, 666666667, -2, 666666667), Array(-1, 0, -1, 999999999, -2, 999999999), Array(-1, 0, 0, 0, -1, 0), Array(-1, 0, 0, 1, -1, 1), Array(-1, 0, 0, 333333333, -1, 333333333), Array(-1, 0, 0, 666666666, -1, 666666666), Array(-1, 0, 1, 0, 0, 0), Array(-1, 0, 2, 0, 1, 0), Array(-1, 0, 3, 0, 2, 0), Array(-1, 0, 3, 333333333, 2, 333333333), Array(-1, 666666667, -4, 666666667, -4, 333333334), Array(-1, 666666667, -3, 0, -4, 666666667), Array(-1, 666666667, -2, 0, -3, 666666667), Array(-1, 666666667, -1, 0, -2, 666666667), Array(-1, 666666667, -1, 333333334, -1, 1), Array(-1, 666666667, -1, 666666667, -1, 333333334), Array(-1, 666666667, -1, 999999999, -1, 666666666), Array(-1, 666666667, 0, 0, -1, 666666667), Array(-1, 666666667, 0, 1, -1, 666666668), Array(-1, 666666667, 0, 333333333, 0, 0), Array(-1, 666666667, 0, 666666666, 0, 333333333), Array(-1, 666666667, 1, 0, 0, 666666667), Array(-1, 666666667, 2, 0, 1, 666666667), Array(-1, 666666667, 3, 0, 2, 666666667), Array(-1, 666666667, 3, 333333333, 3, 0), Array(0, 0, -4, 666666667, -4, 666666667), Array(0, 0, -3, 0, -3, 0), Array(0, 0, -2, 0, -2, 0), Array(0, 0, -1, 0, -1, 0), Array(0, 0, -1, 333333334, -1, 333333334), Array(0, 0, -1, 666666667, -1, 666666667), Array(0, 0, -1, 999999999, -1, 999999999), Array(0, 0, 0, 0, 0, 0), Array(0, 0, 0, 1, 0, 1), Array(0, 0, 0, 333333333, 0, 333333333), Array(0, 0, 0, 666666666, 0, 666666666), Array(0, 0, 1, 0, 1, 0), Array(0, 0, 2, 0, 2, 0), Array(0, 0, 3, 0, 3, 0), Array(0, 0, 3, 333333333, 3, 333333333), Array(0, 333333333, -4, 666666667, -3, 0), Array(0, 333333333, -3, 0, -3, 333333333), Array(0, 333333333, -2, 0, -2, 333333333), Array(0, 333333333, -1, 0, -1, 333333333), Array(0, 333333333, -1, 333333334, -1, 666666667), Array(0, 333333333, -1, 666666667, 0, 0), Array(0, 333333333, -1, 999999999, 0, 333333332), Array(0, 333333333, 0, 0, 0, 333333333), Array(0, 333333333, 0, 1, 0, 333333334), Array(0, 333333333, 0, 333333333, 0, 666666666), Array(0, 333333333, 0, 666666666, 0, 999999999), Array(0, 333333333, 1, 0, 1, 333333333), Array(0, 333333333, 2, 0, 2, 333333333), Array(0, 333333333, 3, 0, 3, 333333333), Array(0, 333333333, 3, 333333333, 3, 666666666), Array(1, 0, -4, 666666667, -3, 666666667), Array(1, 0, -3, 0, -2, 0), Array(1, 0, -2, 0, -1, 0), Array(1, 0, -1, 0, 0, 0), Array(1, 0, -1, 333333334, 0, 333333334), Array(1, 0, -1, 666666667, 0, 666666667), Array(1, 0, -1, 999999999, 0, 999999999), Array(1, 0, 0, 0, 1, 0), Array(1, 0, 0, 1, 1, 1), Array(1, 0, 0, 333333333, 1, 333333333), Array(1, 0, 0, 666666666, 1, 666666666), Array(1, 0, 1, 0, 2, 0), Array(1, 0, 2, 0, 3, 0), Array(1, 0, 3, 0, 4, 0), Array(1, 0, 3, 333333333, 4, 333333333), Array(2, 0, -4, 666666667, -2, 666666667), Array(2, 0, -3, 0, -1, 0), Array(2, 0, -2, 0, 0, 0), Array(2, 0, -1, 0, 1, 0), Array(2, 0, -1, 333333334, 1, 333333334), Array(2, 0, -1, 666666667, 1, 666666667), Array(2, 0, -1, 999999999, 1, 999999999), Array(2, 0, 0, 0, 2, 0), Array(2, 0, 0, 1, 2, 1), Array(2, 0, 0, 333333333, 2, 333333333), Array(2, 0, 0, 666666666, 2, 666666666), Array(2, 0, 1, 0, 3, 0), Array(2, 0, 2, 0, 4, 0), Array(2, 0, 3, 0, 5, 0), Array(2, 0, 3, 333333333, 5, 333333333), Array(3, 0, -4, 666666667, -1, 666666667), Array(3, 0, -3, 0, 0, 0), Array(3, 0, -2, 0, 1, 0), Array(3, 0, -1, 0, 2, 0), Array(3, 0, -1, 333333334, 2, 333333334), Array(3, 0, -1, 666666667, 2, 666666667), Array(3, 0, -1, 999999999, 2, 999999999), Array(3, 0, 0, 0, 3, 0), Array(3, 0, 0, 1, 3, 1), Array(3, 0, 0, 333333333, 3, 333333333), Array(3, 0, 0, 666666666, 3, 666666666), Array(3, 0, 1, 0, 4, 0), Array(3, 0, 2, 0, 5, 0), Array(3, 0, 3, 0, 6, 0), Array(3, 0, 3, 333333333, 6, 333333333), Array(3, 333333333, -4, 666666667, 0, 0), Array(3, 333333333, -3, 0, 0, 333333333), Array(3, 333333333, -2, 0, 1, 333333333), Array(3, 333333333, -1, 0, 2, 333333333), Array(3, 333333333, -1, 333333334, 2, 666666667), Array(3, 333333333, -1, 666666667, 3, 0), Array(3, 333333333, -1, 999999999, 3, 333333332), Array(3, 333333333, 0, 0, 3, 333333333), Array(3, 333333333, 0, 1, 3, 333333334), Array(3, 333333333, 0, 333333333, 3, 666666666), Array(3, 333333333, 0, 666666666, 3, 999999999), Array(3, 333333333, 1, 0, 4, 333333333), Array(3, 333333333, 2, 0, 5, 333333333), Array(3, 333333333, 3, 0, 6, 333333333), Array(3, 333333333, 3, 333333333, 6, 666666666), Array(TestInstant.MAX_SECOND - 1, 0, 1, 0, TestInstant.MAX_SECOND, 0), Array(TestInstant.MAX_SECOND - 1, 0, 0, 500, TestInstant.MAX_SECOND - 1, 500), Array(TestInstant.MAX_SECOND - 1, 0, 0, 1000000000, TestInstant.MAX_SECOND, 0), Array(TestInstant.MAX_SECOND, 0, -1, 0, TestInstant.MAX_SECOND - 1, 0), Array(TestInstant.MAX_SECOND, 0, 0, -500, TestInstant.MAX_SECOND - 1, 999999500), Array(TestInstant.MAX_SECOND, 0, 0, -1000000000, TestInstant.MAX_SECOND - 1, 0), Array(TestInstant.MAX_SECOND, 0, -TestInstant.MAX_SECOND, 0, 0, 0))
  }

  @Test(dataProvider = "Plus") def plus_Duration(seconds: Long, nanos: Int, otherSeconds: Long, otherNanos: Int, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val i: Instant = Instant.ofEpochSecond(seconds, nanos).plus(Duration.ofSeconds(otherSeconds, otherNanos))
    assertEquals(i.getEpochSecond, expectedSeconds)
    assertEquals(i.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def plus_Duration_overflowTooBig(): Unit = {
    val i: Instant = Instant.ofEpochSecond(TestInstant.MAX_SECOND, 999999999)
    i.plus(Duration.ofSeconds(0, 1))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def plus_Duration_overflowTooSmall(): Unit = {
    val i: Instant = Instant.ofEpochSecond(TestInstant.MIN_SECOND)
    i.plus(Duration.ofSeconds(-1, 999999999))
  }

  @Test(dataProvider = "Plus") def plus_longTemporalUnit(seconds: Long, nanos: Int, otherSeconds: Long, otherNanos: Int, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val i: Instant = Instant.ofEpochSecond(seconds, nanos).plus(otherSeconds, SECONDS).plus(otherNanos, NANOS)
    assertEquals(i.getEpochSecond, expectedSeconds)
    assertEquals(i.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def plus_longTemporalUnit_overflowTooBig(): Unit = {
    val i: Instant = Instant.ofEpochSecond(TestInstant.MAX_SECOND, 999999999)
    i.plus(1, NANOS)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def plus_longTemporalUnit_overflowTooSmall(): Unit = {
    val i: Instant = Instant.ofEpochSecond(TestInstant.MIN_SECOND)
    i.plus(999999999, NANOS)
    i.plus(-1, SECONDS)
  }

  @DataProvider(name = "PlusSeconds") private[time] def provider_plusSeconds_long: Array[Array[Long]] = {
    Array[Array[Long]](Array(0, 0, 0, 0, 0), Array(0, 0, 1, 1, 0), Array(0, 0, -1, -1, 0), Array(0, 0, TestInstant.MAX_SECOND, TestInstant.MAX_SECOND, 0), Array(0, 0, TestInstant.MIN_SECOND, TestInstant.MIN_SECOND, 0), Array(1, 0, 0, 1, 0), Array(1, 0, 1, 2, 0), Array(1, 0, -1, 0, 0), Array(1, 0, TestInstant.MAX_SECOND - 1, TestInstant.MAX_SECOND, 0), Array(1, 0, TestInstant.MIN_SECOND, TestInstant.MIN_SECOND + 1, 0), Array(1, 1, 0, 1, 1), Array(1, 1, 1, 2, 1), Array(1, 1, -1, 0, 1), Array(1, 1, TestInstant.MAX_SECOND - 1, TestInstant.MAX_SECOND, 1), Array(1, 1, TestInstant.MIN_SECOND, TestInstant.MIN_SECOND + 1, 1), Array(-1, 1, 0, -1, 1), Array(-1, 1, 1, 0, 1), Array(-1, 1, -1, -2, 1), Array(-1, 1, TestInstant.MAX_SECOND, TestInstant.MAX_SECOND - 1, 1), Array(-1, 1, TestInstant.MIN_SECOND + 1, TestInstant.MIN_SECOND, 1), Array(TestInstant.MAX_SECOND, 2, -TestInstant.MAX_SECOND, 0, 2), Array(TestInstant.MIN_SECOND, 2, -TestInstant.MIN_SECOND, 0, 2))
  }

  @Test(dataProvider = "PlusSeconds") def plusSeconds_long(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Instant = Instant.ofEpochSecond(seconds, nanos)
    t = t.plusSeconds(amount)
    assertEquals(t.getEpochSecond, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def plusSeconds_long_overflowTooBig(): Unit = {
    val t: Instant = Instant.ofEpochSecond(1, 0)
    t.plusSeconds(Long.MaxValue)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def plusSeconds_long_overflowTooSmall(): Unit = {
    val t: Instant = Instant.ofEpochSecond(-1, 0)
    t.plusSeconds(Long.MinValue)
  }

  @DataProvider(name = "PlusMillis") private[time] def provider_plusMillis_long: Array[Array[Long]] = {
    Array[Array[Long]](Array(0, 0, 0, 0, 0), Array(0, 0, 1, 0, 1000000), Array(0, 0, 999, 0, 999000000), Array(0, 0, 1000, 1, 0), Array(0, 0, 1001, 1, 1000000), Array(0, 0, 1999, 1, 999000000), Array(0, 0, 2000, 2, 0), Array(0, 0, -1, -1, 999000000), Array(0, 0, -999, -1, 1000000), Array(0, 0, -1000, -1, 0), Array(0, 0, -1001, -2, 999000000), Array(0, 0, -1999, -2, 1000000), Array(0, 1, 0, 0, 1), Array(0, 1, 1, 0, 1000001), Array(0, 1, 998, 0, 998000001), Array(0, 1, 999, 0, 999000001), Array(0, 1, 1000, 1, 1), Array(0, 1, 1998, 1, 998000001), Array(0, 1, 1999, 1, 999000001), Array(0, 1, 2000, 2, 1), Array(0, 1, -1, -1, 999000001), Array(0, 1, -2, -1, 998000001), Array(0, 1, -1000, -1, 1), Array(0, 1, -1001, -2, 999000001), Array(0, 1000000, 0, 0, 1000000), Array(0, 1000000, 1, 0, 2000000), Array(0, 1000000, 998, 0, 999000000), Array(0, 1000000, 999, 1, 0), Array(0, 1000000, 1000, 1, 1000000), Array(0, 1000000, 1998, 1, 999000000), Array(0, 1000000, 1999, 2, 0), Array(0, 1000000, 2000, 2, 1000000), Array(0, 1000000, -1, 0, 0), Array(0, 1000000, -2, -1, 999000000), Array(0, 1000000, -999, -1, 2000000), Array(0, 1000000, -1000, -1, 1000000), Array(0, 1000000, -1001, -1, 0), Array(0, 1000000, -1002, -2, 999000000), Array(0, 999999999, 0, 0, 999999999), Array(0, 999999999, 1, 1, 999999), Array(0, 999999999, 999, 1, 998999999), Array(0, 999999999, 1000, 1, 999999999), Array(0, 999999999, 1001, 2, 999999), Array(0, 999999999, -1, 0, 998999999), Array(0, 999999999, -1000, -1, 999999999), Array(0, 999999999, -1001, -1, 998999999), Array(0, 0, Long.MaxValue, Long.MaxValue / 1000, (Long.MaxValue % 1000).toInt * 1000000), Array(0, 0, Long.MinValue, Long.MinValue / 1000 - 1, (Long.MinValue % 1000).toInt * 1000000 + 1000000000))
  }

  @Test(dataProvider = "PlusMillis") def plusMillis_long(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Instant = Instant.ofEpochSecond(seconds, nanos)
    t = t.plusMillis(amount)
    assertEquals(t.getEpochSecond, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(dataProvider = "PlusMillis") def plusMillis_long_oneMore(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Instant = Instant.ofEpochSecond(seconds + 1, nanos)
    t = t.plusMillis(amount)
    assertEquals(t.getEpochSecond, expectedSeconds + 1)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(dataProvider = "PlusMillis") def plusMillis_long_minusOneLess(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Instant = Instant.ofEpochSecond(seconds - 1, nanos)
    t = t.plusMillis(amount)
    assertEquals(t.getEpochSecond, expectedSeconds - 1)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test def plusMillis_long_max(): Unit = {
    var t: Instant = Instant.ofEpochSecond(TestInstant.MAX_SECOND, 998999999)
    t = t.plusMillis(1)
    assertEquals(t.getEpochSecond, TestInstant.MAX_SECOND)
    assertEquals(t.getNano, 999999999)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def plusMillis_long_overflowTooBig(): Unit = {
    val t: Instant = Instant.ofEpochSecond(TestInstant.MAX_SECOND, 999000000)
    t.plusMillis(1)
  }

  @Test def plusMillis_long_min(): Unit = {
    var t: Instant = Instant.ofEpochSecond(TestInstant.MIN_SECOND, 1000000)
    t = t.plusMillis(-1)
    assertEquals(t.getEpochSecond, TestInstant.MIN_SECOND)
    assertEquals(t.getNano, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def plusMillis_long_overflowTooSmall(): Unit = {
    val t: Instant = Instant.ofEpochSecond(TestInstant.MIN_SECOND, 0)
    t.plusMillis(-1)
  }

  @DataProvider(name = "PlusNanos") private[time] def provider_plusNanos_long: Array[Array[Long]] = {
    Array[Array[Long]](Array(0, 0, 0, 0, 0), Array(0, 0, 1, 0, 1), Array(0, 0, 999999999, 0, 999999999), Array(0, 0, 1000000000, 1, 0), Array(0, 0, 1000000001, 1, 1), Array(0, 0, 1999999999, 1, 999999999), Array(0, 0, 2000000000, 2, 0), Array(0, 0, -1, -1, 999999999), Array(0, 0, -999999999, -1, 1), Array(0, 0, -1000000000, -1, 0), Array(0, 0, -1000000001, -2, 999999999), Array(0, 0, -1999999999, -2, 1), Array(1, 0, 0, 1, 0), Array(1, 0, 1, 1, 1), Array(1, 0, 999999999, 1, 999999999), Array(1, 0, 1000000000, 2, 0), Array(1, 0, 1000000001, 2, 1), Array(1, 0, 1999999999, 2, 999999999), Array(1, 0, 2000000000, 3, 0), Array(1, 0, -1, 0, 999999999), Array(1, 0, -999999999, 0, 1), Array(1, 0, -1000000000, 0, 0), Array(1, 0, -1000000001, -1, 999999999), Array(1, 0, -1999999999, -1, 1), Array(-1, 0, 0, -1, 0), Array(-1, 0, 1, -1, 1), Array(-1, 0, 999999999, -1, 999999999), Array(-1, 0, 1000000000, 0, 0), Array(-1, 0, 1000000001, 0, 1), Array(-1, 0, 1999999999, 0, 999999999), Array(-1, 0, 2000000000, 1, 0), Array(-1, 0, -1, -2, 999999999), Array(-1, 0, -999999999, -2, 1), Array(-1, 0, -1000000000, -2, 0), Array(-1, 0, -1000000001, -3, 999999999), Array(-1, 0, -1999999999, -3, 1), Array(1, 1, 0, 1, 1), Array(1, 1, 1, 1, 2), Array(1, 1, 999999998, 1, 999999999), Array(1, 1, 999999999, 2, 0), Array(1, 1, 1000000000, 2, 1), Array(1, 1, 1999999998, 2, 999999999), Array(1, 1, 1999999999, 3, 0), Array(1, 1, 2000000000, 3, 1), Array(1, 1, -1, 1, 0), Array(1, 1, -2, 0, 999999999), Array(1, 1, -1000000000, 0, 1), Array(1, 1, -1000000001, 0, 0), Array(1, 1, -1000000002, -1, 999999999), Array(1, 1, -2000000000, -1, 1), Array(1, 999999999, 0, 1, 999999999), Array(1, 999999999, 1, 2, 0), Array(1, 999999999, 999999999, 2, 999999998), Array(1, 999999999, 1000000000, 2, 999999999), Array(1, 999999999, 1000000001, 3, 0), Array(1, 999999999, -1, 1, 999999998), Array(1, 999999999, -1000000000, 0, 999999999), Array(1, 999999999, -1000000001, 0, 999999998), Array(1, 999999999, -1999999999, 0, 0), Array(1, 999999999, -2000000000, -1, 999999999), Array(TestInstant.MAX_SECOND, 0, 999999999, TestInstant.MAX_SECOND, 999999999), Array(TestInstant.MAX_SECOND - 1, 0, 1999999999, TestInstant.MAX_SECOND, 999999999), Array(TestInstant.MIN_SECOND, 1, -1, TestInstant.MIN_SECOND, 0), Array(TestInstant.MIN_SECOND + 1, 1, -1000000001, TestInstant.MIN_SECOND, 0), Array(0, 0, TestInstant.MAX_SECOND, TestInstant.MAX_SECOND / 1000000000, (TestInstant.MAX_SECOND % 1000000000).toInt), Array(0, 0, TestInstant.MIN_SECOND, TestInstant.MIN_SECOND / 1000000000 - 1, (TestInstant.MIN_SECOND % 1000000000).toInt + 1000000000))
  }

  @Test(dataProvider = "PlusNanos") def plusNanos_long(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var t: Instant = Instant.ofEpochSecond(seconds, nanos)
    t = t.plusNanos(amount)
    assertEquals(t.getEpochSecond, expectedSeconds)
    assertEquals(t.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def plusNanos_long_overflowTooBig(): Unit = {
    val t: Instant = Instant.ofEpochSecond(TestInstant.MAX_SECOND, 999999999)
    t.plusNanos(1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def plusNanos_long_overflowTooSmall(): Unit = {
    val t: Instant = Instant.ofEpochSecond(TestInstant.MIN_SECOND, 0)
    t.plusNanos(-1)
  }

  @DataProvider(name = "Minus") private[time] def provider_minus: Array[Array[Long]] = {
    Array[Array[Long]](Array(TestInstant.MIN_SECOND, 0, TestInstant.MIN_SECOND, 0, 0, 0), Array(TestInstant.MIN_SECOND, 0, -1, 0, TestInstant.MIN_SECOND + 1, 0), Array(TestInstant.MIN_SECOND, 0, 0, -500, TestInstant.MIN_SECOND, 500), Array(TestInstant.MIN_SECOND, 0, 0, -1000000000, TestInstant.MIN_SECOND + 1, 0), Array(TestInstant.MIN_SECOND + 1, 0, 1, 0, TestInstant.MIN_SECOND, 0), Array(TestInstant.MIN_SECOND + 1, 0, 0, 500, TestInstant.MIN_SECOND, 999999500), Array(TestInstant.MIN_SECOND + 1, 0, 0, 1000000000, TestInstant.MIN_SECOND, 0), Array(-4, 666666667, -4, 666666667, 0, 0), Array(-4, 666666667, -3, 0, -1, 666666667), Array(-4, 666666667, -2, 0, -2, 666666667), Array(-4, 666666667, -1, 0, -3, 666666667), Array(-4, 666666667, -1, 333333334, -3, 333333333), Array(-4, 666666667, -1, 666666667, -3, 0), Array(-4, 666666667, -1, 999999999, -4, 666666668), Array(-4, 666666667, 0, 0, -4, 666666667), Array(-4, 666666667, 0, 1, -4, 666666666), Array(-4, 666666667, 0, 333333333, -4, 333333334), Array(-4, 666666667, 0, 666666666, -4, 1), Array(-4, 666666667, 1, 0, -5, 666666667), Array(-4, 666666667, 2, 0, -6, 666666667), Array(-4, 666666667, 3, 0, -7, 666666667), Array(-4, 666666667, 3, 333333333, -7, 333333334), Array(-3, 0, -4, 666666667, 0, 333333333), Array(-3, 0, -3, 0, 0, 0), Array(-3, 0, -2, 0, -1, 0), Array(-3, 0, -1, 0, -2, 0), Array(-3, 0, -1, 333333334, -3, 666666666), Array(-3, 0, -1, 666666667, -3, 333333333), Array(-3, 0, -1, 999999999, -3, 1), Array(-3, 0, 0, 0, -3, 0), Array(-3, 0, 0, 1, -4, 999999999), Array(-3, 0, 0, 333333333, -4, 666666667), Array(-3, 0, 0, 666666666, -4, 333333334), Array(-3, 0, 1, 0, -4, 0), Array(-3, 0, 2, 0, -5, 0), Array(-3, 0, 3, 0, -6, 0), Array(-3, 0, 3, 333333333, -7, 666666667), Array(-2, 0, -4, 666666667, 1, 333333333), Array(-2, 0, -3, 0, 1, 0), Array(-2, 0, -2, 0, 0, 0), Array(-2, 0, -1, 0, -1, 0), Array(-2, 0, -1, 333333334, -2, 666666666), Array(-2, 0, -1, 666666667, -2, 333333333), Array(-2, 0, -1, 999999999, -2, 1), Array(-2, 0, 0, 0, -2, 0), Array(-2, 0, 0, 1, -3, 999999999), Array(-2, 0, 0, 333333333, -3, 666666667), Array(-2, 0, 0, 666666666, -3, 333333334), Array(-2, 0, 1, 0, -3, 0), Array(-2, 0, 2, 0, -4, 0), Array(-2, 0, 3, 0, -5, 0), Array(-2, 0, 3, 333333333, -6, 666666667), Array(-1, 0, -4, 666666667, 2, 333333333), Array(-1, 0, -3, 0, 2, 0), Array(-1, 0, -2, 0, 1, 0), Array(-1, 0, -1, 0, 0, 0), Array(-1, 0, -1, 333333334, -1, 666666666), Array(-1, 0, -1, 666666667, -1, 333333333), Array(-1, 0, -1, 999999999, -1, 1), Array(-1, 0, 0, 0, -1, 0), Array(-1, 0, 0, 1, -2, 999999999), Array(-1, 0, 0, 333333333, -2, 666666667), Array(-1, 0, 0, 666666666, -2, 333333334), Array(-1, 0, 1, 0, -2, 0), Array(-1, 0, 2, 0, -3, 0), Array(-1, 0, 3, 0, -4, 0), Array(-1, 0, 3, 333333333, -5, 666666667), Array(-1, 666666667, -4, 666666667, 3, 0), Array(-1, 666666667, -3, 0, 2, 666666667), Array(-1, 666666667, -2, 0, 1, 666666667), Array(-1, 666666667, -1, 0, 0, 666666667), Array(-1, 666666667, -1, 333333334, 0, 333333333), Array(-1, 666666667, -1, 666666667, 0, 0), Array(-1, 666666667, -1, 999999999, -1, 666666668), Array(-1, 666666667, 0, 0, -1, 666666667), Array(-1, 666666667, 0, 1, -1, 666666666), Array(-1, 666666667, 0, 333333333, -1, 333333334), Array(-1, 666666667, 0, 666666666, -1, 1), Array(-1, 666666667, 1, 0, -2, 666666667), Array(-1, 666666667, 2, 0, -3, 666666667), Array(-1, 666666667, 3, 0, -4, 666666667), Array(-1, 666666667, 3, 333333333, -4, 333333334), Array(0, 0, -4, 666666667, 3, 333333333), Array(0, 0, -3, 0, 3, 0), Array(0, 0, -2, 0, 2, 0), Array(0, 0, -1, 0, 1, 0), Array(0, 0, -1, 333333334, 0, 666666666), Array(0, 0, -1, 666666667, 0, 333333333), Array(0, 0, -1, 999999999, 0, 1), Array(0, 0, 0, 0, 0, 0), Array(0, 0, 0, 1, -1, 999999999), Array(0, 0, 0, 333333333, -1, 666666667), Array(0, 0, 0, 666666666, -1, 333333334), Array(0, 0, 1, 0, -1, 0), Array(0, 0, 2, 0, -2, 0), Array(0, 0, 3, 0, -3, 0), Array(0, 0, 3, 333333333, -4, 666666667), Array(0, 333333333, -4, 666666667, 3, 666666666), Array(0, 333333333, -3, 0, 3, 333333333), Array(0, 333333333, -2, 0, 2, 333333333), Array(0, 333333333, -1, 0, 1, 333333333), Array(0, 333333333, -1, 333333334, 0, 999999999), Array(0, 333333333, -1, 666666667, 0, 666666666), Array(0, 333333333, -1, 999999999, 0, 333333334), Array(0, 333333333, 0, 0, 0, 333333333), Array(0, 333333333, 0, 1, 0, 333333332), Array(0, 333333333, 0, 333333333, 0, 0), Array(0, 333333333, 0, 666666666, -1, 666666667), Array(0, 333333333, 1, 0, -1, 333333333), Array(0, 333333333, 2, 0, -2, 333333333), Array(0, 333333333, 3, 0, -3, 333333333), Array(0, 333333333, 3, 333333333, -3, 0), Array(1, 0, -4, 666666667, 4, 333333333), Array(1, 0, -3, 0, 4, 0), Array(1, 0, -2, 0, 3, 0), Array(1, 0, -1, 0, 2, 0), Array(1, 0, -1, 333333334, 1, 666666666), Array(1, 0, -1, 666666667, 1, 333333333), Array(1, 0, -1, 999999999, 1, 1), Array(1, 0, 0, 0, 1, 0), Array(1, 0, 0, 1, 0, 999999999), Array(1, 0, 0, 333333333, 0, 666666667), Array(1, 0, 0, 666666666, 0, 333333334), Array(1, 0, 1, 0, 0, 0), Array(1, 0, 2, 0, -1, 0), Array(1, 0, 3, 0, -2, 0), Array(1, 0, 3, 333333333, -3, 666666667), Array(2, 0, -4, 666666667, 5, 333333333), Array(2, 0, -3, 0, 5, 0), Array(2, 0, -2, 0, 4, 0), Array(2, 0, -1, 0, 3, 0), Array(2, 0, -1, 333333334, 2, 666666666), Array(2, 0, -1, 666666667, 2, 333333333), Array(2, 0, -1, 999999999, 2, 1), Array(2, 0, 0, 0, 2, 0), Array(2, 0, 0, 1, 1, 999999999), Array(2, 0, 0, 333333333, 1, 666666667), Array(2, 0, 0, 666666666, 1, 333333334), Array(2, 0, 1, 0, 1, 0), Array(2, 0, 2, 0, 0, 0), Array(2, 0, 3, 0, -1, 0), Array(2, 0, 3, 333333333, -2, 666666667), Array(3, 0, -4, 666666667, 6, 333333333), Array(3, 0, -3, 0, 6, 0), Array(3, 0, -2, 0, 5, 0), Array(3, 0, -1, 0, 4, 0), Array(3, 0, -1, 333333334, 3, 666666666), Array(3, 0, -1, 666666667, 3, 333333333), Array(3, 0, -1, 999999999, 3, 1), Array(3, 0, 0, 0, 3, 0), Array(3, 0, 0, 1, 2, 999999999), Array(3, 0, 0, 333333333, 2, 666666667), Array(3, 0, 0, 666666666, 2, 333333334), Array(3, 0, 1, 0, 2, 0), Array(3, 0, 2, 0, 1, 0), Array(3, 0, 3, 0, 0, 0), Array(3, 0, 3, 333333333, -1, 666666667), Array(3, 333333333, -4, 666666667, 6, 666666666), Array(3, 333333333, -3, 0, 6, 333333333), Array(3, 333333333, -2, 0, 5, 333333333), Array(3, 333333333, -1, 0, 4, 333333333), Array(3, 333333333, -1, 333333334, 3, 999999999), Array(3, 333333333, -1, 666666667, 3, 666666666), Array(3, 333333333, -1, 999999999, 3, 333333334), Array(3, 333333333, 0, 0, 3, 333333333), Array(3, 333333333, 0, 1, 3, 333333332), Array(3, 333333333, 0, 333333333, 3, 0), Array(3, 333333333, 0, 666666666, 2, 666666667), Array(3, 333333333, 1, 0, 2, 333333333), Array(3, 333333333, 2, 0, 1, 333333333), Array(3, 333333333, 3, 0, 0, 333333333), Array(3, 333333333, 3, 333333333, 0, 0), Array(TestInstant.MAX_SECOND - 1, 0, -1, 0, TestInstant.MAX_SECOND, 0), Array(TestInstant.MAX_SECOND - 1, 0, 0, -500, TestInstant.MAX_SECOND - 1, 500), Array(TestInstant.MAX_SECOND - 1, 0, 0, -1000000000, TestInstant.MAX_SECOND, 0), Array(TestInstant.MAX_SECOND, 0, 1, 0, TestInstant.MAX_SECOND - 1, 0), Array(TestInstant.MAX_SECOND, 0, 0, 500, TestInstant.MAX_SECOND - 1, 999999500), Array(TestInstant.MAX_SECOND, 0, 0, 1000000000, TestInstant.MAX_SECOND - 1, 0), Array(TestInstant.MAX_SECOND, 0, TestInstant.MAX_SECOND, 0, 0, 0))
  }

  @Test(dataProvider = "Minus") def minus_Duration(seconds: Long, nanos: Int, otherSeconds: Long, otherNanos: Int, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val i: Instant = Instant.ofEpochSecond(seconds, nanos).minus(Duration.ofSeconds(otherSeconds, otherNanos))
    assertEquals(i.getEpochSecond, expectedSeconds)
    assertEquals(i.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def minus_Duration_overflowTooSmall(): Unit = {
    val i: Instant = Instant.ofEpochSecond(TestInstant.MIN_SECOND)
    i.minus(Duration.ofSeconds(0, 1))
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def minus_Duration_overflowTooBig(): Unit = {
    val i: Instant = Instant.ofEpochSecond(TestInstant.MAX_SECOND, 999999999)
    i.minus(Duration.ofSeconds(-1, 999999999))
  }

  @Test(dataProvider = "Minus") def minus_longTemporalUnit(seconds: Long, nanos: Int, otherSeconds: Long, otherNanos: Int, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    val i: Instant = Instant.ofEpochSecond(seconds, nanos).minus(otherSeconds, SECONDS).minus(otherNanos, NANOS)
    assertEquals(i.getEpochSecond, expectedSeconds)
    assertEquals(i.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def minus_longTemporalUnit_overflowTooSmall(): Unit = {
    val i: Instant = Instant.ofEpochSecond(TestInstant.MIN_SECOND)
    i.minus(1, NANOS)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def minus_longTemporalUnit_overflowTooBig(): Unit = {
    val i: Instant = Instant.ofEpochSecond(TestInstant.MAX_SECOND, 999999999)
    i.minus(999999999, NANOS)
    i.minus(-1, SECONDS)
  }

  @DataProvider(name = "MinusSeconds") private[time] def provider_minusSeconds_long: Array[Array[Long]] = {
    Array[Array[Long]](Array(0, 0, 0, 0, 0), Array(0, 0, 1, -1, 0), Array(0, 0, -1, 1, 0), Array(0, 0, -TestInstant.MIN_SECOND, TestInstant.MIN_SECOND, 0), Array(1, 0, 0, 1, 0), Array(1, 0, 1, 0, 0), Array(1, 0, -1, 2, 0), Array(1, 0, -TestInstant.MIN_SECOND + 1, TestInstant.MIN_SECOND, 0), Array(1, 1, 0, 1, 1), Array(1, 1, 1, 0, 1), Array(1, 1, -1, 2, 1), Array(1, 1, -TestInstant.MIN_SECOND, TestInstant.MIN_SECOND + 1, 1), Array(1, 1, -TestInstant.MIN_SECOND + 1, TestInstant.MIN_SECOND, 1), Array(-1, 1, 0, -1, 1), Array(-1, 1, 1, -2, 1), Array(-1, 1, -1, 0, 1), Array(-1, 1, -TestInstant.MAX_SECOND, TestInstant.MAX_SECOND - 1, 1), Array(-1, 1, -(TestInstant.MAX_SECOND + 1), TestInstant.MAX_SECOND, 1), Array(TestInstant.MIN_SECOND, 2, TestInstant.MIN_SECOND, 0, 2), Array(TestInstant.MIN_SECOND + 1, 2, TestInstant.MIN_SECOND, 1, 2), Array(TestInstant.MAX_SECOND - 1, 2, TestInstant.MAX_SECOND, -1, 2), Array(TestInstant.MAX_SECOND, 2, TestInstant.MAX_SECOND, 0, 2))
  }

  @Test(dataProvider = "MinusSeconds") def minusSeconds_long(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var i: Instant = Instant.ofEpochSecond(seconds, nanos)
    i = i.minusSeconds(amount)
    assertEquals(i.getEpochSecond, expectedSeconds)
    assertEquals(i.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def minusSeconds_long_overflowTooBig(): Unit = {
    val i: Instant = Instant.ofEpochSecond(1, 0)
    i.minusSeconds(Long.MinValue + 1)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def minusSeconds_long_overflowTooSmall(): Unit = {
    val i: Instant = Instant.ofEpochSecond(-2, 0)
    i.minusSeconds(Long.MaxValue)
  }

  @DataProvider(name = "MinusMillis") private[time] def provider_minusMillis_long: Array[Array[Long]] = {
    Array[Array[Long]](Array(0, 0, 0, 0, 0), Array(0, 0, 1, -1, 999000000), Array(0, 0, 999, -1, 1000000), Array(0, 0, 1000, -1, 0), Array(0, 0, 1001, -2, 999000000), Array(0, 0, 1999, -2, 1000000), Array(0, 0, 2000, -2, 0), Array(0, 0, -1, 0, 1000000), Array(0, 0, -999, 0, 999000000), Array(0, 0, -1000, 1, 0), Array(0, 0, -1001, 1, 1000000), Array(0, 0, -1999, 1, 999000000), Array(0, 1, 0, 0, 1), Array(0, 1, 1, -1, 999000001), Array(0, 1, 998, -1, 2000001), Array(0, 1, 999, -1, 1000001), Array(0, 1, 1000, -1, 1), Array(0, 1, 1998, -2, 2000001), Array(0, 1, 1999, -2, 1000001), Array(0, 1, 2000, -2, 1), Array(0, 1, -1, 0, 1000001), Array(0, 1, -2, 0, 2000001), Array(0, 1, -1000, 1, 1), Array(0, 1, -1001, 1, 1000001), Array(0, 1000000, 0, 0, 1000000), Array(0, 1000000, 1, 0, 0), Array(0, 1000000, 998, -1, 3000000), Array(0, 1000000, 999, -1, 2000000), Array(0, 1000000, 1000, -1, 1000000), Array(0, 1000000, 1998, -2, 3000000), Array(0, 1000000, 1999, -2, 2000000), Array(0, 1000000, 2000, -2, 1000000), Array(0, 1000000, -1, 0, 2000000), Array(0, 1000000, -2, 0, 3000000), Array(0, 1000000, -999, 1, 0), Array(0, 1000000, -1000, 1, 1000000), Array(0, 1000000, -1001, 1, 2000000), Array(0, 1000000, -1002, 1, 3000000), Array(0, 999999999, 0, 0, 999999999), Array(0, 999999999, 1, 0, 998999999), Array(0, 999999999, 999, 0, 999999), Array(0, 999999999, 1000, -1, 999999999), Array(0, 999999999, 1001, -1, 998999999), Array(0, 999999999, -1, 1, 999999), Array(0, 999999999, -1000, 1, 999999999), Array(0, 999999999, -1001, 2, 999999), Array(0, 0, Long.MaxValue, -(Long.MaxValue / 1000) - 1, -(Long.MaxValue % 1000).toInt * 1000000 + 1000000000), Array(0, 0, Long.MinValue, -(Long.MinValue / 1000), -(Long.MinValue % 1000).toInt * 1000000))
  }

  @Test(dataProvider = "MinusMillis") def minusMillis_long(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var i: Instant = Instant.ofEpochSecond(seconds, nanos)
    i = i.minusMillis(amount)
    assertEquals(i.getEpochSecond, expectedSeconds)
    assertEquals(i.getNano, expectedNanoOfSecond)
  }

  @Test(dataProvider = "MinusMillis") def minusMillis_long_oneMore(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var i: Instant = Instant.ofEpochSecond(seconds + 1, nanos)
    i = i.minusMillis(amount)
    assertEquals(i.getEpochSecond, expectedSeconds + 1)
    assertEquals(i.getNano, expectedNanoOfSecond)
  }

  @Test(dataProvider = "MinusMillis") def minusMillis_long_minusOneLess(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var i: Instant = Instant.ofEpochSecond(seconds - 1, nanos)
    i = i.minusMillis(amount)
    assertEquals(i.getEpochSecond, expectedSeconds - 1)
    assertEquals(i.getNano, expectedNanoOfSecond)
  }

  @Test def minusMillis_long_max(): Unit = {
    var i: Instant = Instant.ofEpochSecond(TestInstant.MAX_SECOND, 998999999)
    i = i.minusMillis(-1)
    assertEquals(i.getEpochSecond, TestInstant.MAX_SECOND)
    assertEquals(i.getNano, 999999999)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def minusMillis_long_overflowTooBig(): Unit = {
    val i: Instant = Instant.ofEpochSecond(TestInstant.MAX_SECOND, 999000000)
    i.minusMillis(-1)
  }

  @Test def minusMillis_long_min(): Unit = {
    var i: Instant = Instant.ofEpochSecond(TestInstant.MIN_SECOND, 1000000)
    i = i.minusMillis(1)
    assertEquals(i.getEpochSecond, TestInstant.MIN_SECOND)
    assertEquals(i.getNano, 0)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def minusMillis_long_overflowTooSmall(): Unit = {
    val i: Instant = Instant.ofEpochSecond(TestInstant.MIN_SECOND, 0)
    i.minusMillis(1)
  }

  @DataProvider(name = "MinusNanos") private[time] def provider_minusNanos_long: Array[Array[Long]] = {
    Array[Array[Long]](Array(0, 0, 0, 0, 0), Array(0, 0, 1, -1, 999999999), Array(0, 0, 999999999, -1, 1), Array(0, 0, 1000000000, -1, 0), Array(0, 0, 1000000001, -2, 999999999), Array(0, 0, 1999999999, -2, 1), Array(0, 0, 2000000000, -2, 0), Array(0, 0, -1, 0, 1), Array(0, 0, -999999999, 0, 999999999), Array(0, 0, -1000000000, 1, 0), Array(0, 0, -1000000001, 1, 1), Array(0, 0, -1999999999, 1, 999999999), Array(1, 0, 0, 1, 0), Array(1, 0, 1, 0, 999999999), Array(1, 0, 999999999, 0, 1), Array(1, 0, 1000000000, 0, 0), Array(1, 0, 1000000001, -1, 999999999), Array(1, 0, 1999999999, -1, 1), Array(1, 0, 2000000000, -1, 0), Array(1, 0, -1, 1, 1), Array(1, 0, -999999999, 1, 999999999), Array(1, 0, -1000000000, 2, 0), Array(1, 0, -1000000001, 2, 1), Array(1, 0, -1999999999, 2, 999999999), Array(-1, 0, 0, -1, 0), Array(-1, 0, 1, -2, 999999999), Array(-1, 0, 999999999, -2, 1), Array(-1, 0, 1000000000, -2, 0), Array(-1, 0, 1000000001, -3, 999999999), Array(-1, 0, 1999999999, -3, 1), Array(-1, 0, 2000000000, -3, 0), Array(-1, 0, -1, -1, 1), Array(-1, 0, -999999999, -1, 999999999), Array(-1, 0, -1000000000, 0, 0), Array(-1, 0, -1000000001, 0, 1), Array(-1, 0, -1999999999, 0, 999999999), Array(1, 1, 0, 1, 1), Array(1, 1, 1, 1, 0), Array(1, 1, 999999998, 0, 3), Array(1, 1, 999999999, 0, 2), Array(1, 1, 1000000000, 0, 1), Array(1, 1, 1999999998, -1, 3), Array(1, 1, 1999999999, -1, 2), Array(1, 1, 2000000000, -1, 1), Array(1, 1, -1, 1, 2), Array(1, 1, -2, 1, 3), Array(1, 1, -1000000000, 2, 1), Array(1, 1, -1000000001, 2, 2), Array(1, 1, -1000000002, 2, 3), Array(1, 1, -2000000000, 3, 1), Array(1, 999999999, 0, 1, 999999999), Array(1, 999999999, 1, 1, 999999998), Array(1, 999999999, 999999999, 1, 0), Array(1, 999999999, 1000000000, 0, 999999999), Array(1, 999999999, 1000000001, 0, 999999998), Array(1, 999999999, -1, 2, 0), Array(1, 999999999, -1000000000, 2, 999999999), Array(1, 999999999, -1000000001, 3, 0), Array(1, 999999999, -1999999999, 3, 999999998), Array(1, 999999999, -2000000000, 3, 999999999), Array(TestInstant.MAX_SECOND, 0, -999999999, TestInstant.MAX_SECOND, 999999999), Array(TestInstant.MAX_SECOND - 1, 0, -1999999999, TestInstant.MAX_SECOND, 999999999), Array(TestInstant.MIN_SECOND, 1, 1, TestInstant.MIN_SECOND, 0), Array(TestInstant.MIN_SECOND + 1, 1, 1000000001, TestInstant.MIN_SECOND, 0), Array(0, 0, Long.MaxValue, -(Long.MaxValue / 1000000000) - 1, -(Long.MaxValue % 1000000000).toInt + 1000000000), Array(0, 0, Long.MinValue, -(Long.MinValue / 1000000000), -(Long.MinValue % 1000000000).toInt))
  }

  @Test(dataProvider = "MinusNanos") def minusNanos_long(seconds: Long, nanos: Int, amount: Long, expectedSeconds: Long, expectedNanoOfSecond: Int): Unit = {
    var i: Instant = Instant.ofEpochSecond(seconds, nanos)
    i = i.minusNanos(amount)
    assertEquals(i.getEpochSecond, expectedSeconds)
    assertEquals(i.getNano, expectedNanoOfSecond)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def minusNanos_long_overflowTooBig(): Unit = {
    val i: Instant = Instant.ofEpochSecond(TestInstant.MAX_SECOND, 999999999)
    i.minusNanos(-1)
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException])) def minusNanos_long_overflowTooSmall(): Unit = {
    val i: Instant = Instant.ofEpochSecond(TestInstant.MIN_SECOND, 0)
    i.minusNanos(1)
  }

  @Test def test_toEpochMilli(): Unit = {
    assertEquals(Instant.ofEpochSecond(1L, 1000000).toEpochMilli, 1001L)
    assertEquals(Instant.ofEpochSecond(1L, 2000000).toEpochMilli, 1002L)
    assertEquals(Instant.ofEpochSecond(1L, 567).toEpochMilli, 1000L)
    assertEquals(Instant.ofEpochSecond(Long.MaxValue / 1000).toEpochMilli, (Long.MaxValue / 1000) * 1000)
    assertEquals(Instant.ofEpochSecond(Long.MinValue / 1000).toEpochMilli, (Long.MinValue / 1000) * 1000)
    assertEquals(Instant.ofEpochSecond(0L, -1000000).toEpochMilli, -1L)
    assertEquals(Instant.ofEpochSecond(0L, 1000000).toEpochMilli, 1)
    assertEquals(Instant.ofEpochSecond(0L, 999999).toEpochMilli, 0)
    assertEquals(Instant.ofEpochSecond(0L, 1).toEpochMilli, 0)
    assertEquals(Instant.ofEpochSecond(0L, 0).toEpochMilli, 0)
    assertEquals(Instant.ofEpochSecond(0L, -1).toEpochMilli, -1L)
    assertEquals(Instant.ofEpochSecond(0L, -999999).toEpochMilli, -1L)
    assertEquals(Instant.ofEpochSecond(0L, -1000000).toEpochMilli, -1L)
    assertEquals(Instant.ofEpochSecond(0L, -1000001).toEpochMilli, -2L)
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_toEpochMilli_tooBig(): Unit = {
    Instant.ofEpochSecond(Long.MaxValue / 1000 + 1).toEpochMilli
  }

  @Test(expectedExceptions = Array(classOf[ArithmeticException])) def test_toEpochMilli_tooSmall(): Unit = {
    Instant.ofEpochSecond(Long.MinValue / 1000 - 1).toEpochMilli
  }

  @Test def test_comparisons(): Unit = {
    doTest_comparisons_Instant(Instant.ofEpochSecond(-2L, 0), Instant.ofEpochSecond(-2L, 999999998), Instant.ofEpochSecond(-2L, 999999999), Instant.ofEpochSecond(-1L, 0), Instant.ofEpochSecond(-1L, 1), Instant.ofEpochSecond(-1L, 999999998), Instant.ofEpochSecond(-1L, 999999999), Instant.ofEpochSecond(0L, 0), Instant.ofEpochSecond(0L, 1), Instant.ofEpochSecond(0L, 2), Instant.ofEpochSecond(0L, 999999999), Instant.ofEpochSecond(1L, 0), Instant.ofEpochSecond(2L, 0))
  }

  private[time] def doTest_comparisons_Instant(instants: Instant*): Unit = {
    {
      var i: Int = 0
      while (i < instants.length) {
        {
          val a: Instant = instants(i)

          {
            var j: Int = 0
            while (j < instants.length) {
              {
                val b: Instant = instants(j)
                if (i < j) {
                  assertEquals(a.compareTo(b) < 0, true, a + " <=> " + b)
                  assertEquals(a.isBefore(b), true, a + " <=> " + b)
                  assertEquals(a.isAfter(b), false, a + " <=> " + b)
                  assertEquals(a == b, false, a + " <=> " + b)
                }
                else if (i > j) {
                  assertEquals(a.compareTo(b) > 0, true, a + " <=> " + b)
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
    val a: Instant = Instant.ofEpochSecond(0L, 0)
    a.compareTo(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isBefore_ObjectNull(): Unit = {
    val a: Instant = Instant.ofEpochSecond(0L, 0)
    a.isBefore(null)
  }

  @Test(expectedExceptions = Array(classOf[NullPointerException])) def test_isAfter_ObjectNull(): Unit = {
    val a: Instant = Instant.ofEpochSecond(0L, 0)
    a.isAfter(null)
  }

  @Test def test_equals(): Unit = {
    val test5a: Instant = Instant.ofEpochSecond(5L, 20)
    val test5b: Instant = Instant.ofEpochSecond(5L, 20)
    val test5n: Instant = Instant.ofEpochSecond(5L, 30)
    val test6: Instant = Instant.ofEpochSecond(6L, 20)
    assertEquals(test5a == test5a, true)
    assertEquals(test5a == test5b, true)
    assertEquals(test5a == test5n, false)
    assertEquals(test5a == test6, false)
    assertEquals(test5b == test5a, true)
    assertEquals(test5b == test5b, true)
    assertEquals(test5b == test5n, false)
    assertEquals(test5b == test6, false)
    assertEquals(test5n == test5a, false)
    assertEquals(test5n == test5b, false)
    assertEquals(test5n == test5n, true)
    assertEquals(test5n == test6, false)
    assertEquals(test6 == test5a, false)
    assertEquals(test6 == test5b, false)
    assertEquals(test6 == test5n, false)
    assertEquals(test6 == test6, true)
  }

  @Test def test_equals_null(): Unit = {
    val test5: Instant = Instant.ofEpochSecond(5L, 20)
    assertEquals(test5 == null, false)
  }

  @Test def test_equals_otherClass(): Unit = {
    val test5: Instant = Instant.ofEpochSecond(5L, 20)
    assertEquals(test5 == "", false)
  }

  @Test def test_hashCode(): Unit = {
    val test5a: Instant = Instant.ofEpochSecond(5L, 20)
    val test5b: Instant = Instant.ofEpochSecond(5L, 20)
    val test5n: Instant = Instant.ofEpochSecond(5L, 30)
    val test6: Instant = Instant.ofEpochSecond(6L, 20)
    assertEquals(test5a.hashCode == test5a.hashCode, true)
    assertEquals(test5a.hashCode == test5b.hashCode, true)
    assertEquals(test5b.hashCode == test5b.hashCode, true)
    assertEquals(test5a.hashCode == test5n.hashCode, false)
    assertEquals(test5a.hashCode == test6.hashCode, false)
  }

  @DataProvider(name = "toStringParse") private[time] def data_toString: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array(Instant.ofEpochSecond(65L, 567), "1970-01-01T00:01:05.000000567Z"), Array(Instant.ofEpochSecond(1, 0), "1970-01-01T00:00:01Z"), Array(Instant.ofEpochSecond(60, 0), "1970-01-01T00:01:00Z"), Array(Instant.ofEpochSecond(3600, 0), "1970-01-01T01:00:00Z"), Array(Instant.ofEpochSecond(-1, 0), "1969-12-31T23:59:59Z"), Array(LocalDateTime.of(0, 1, 2, 0, 0).toInstant(ZoneOffset.UTC), "0000-01-02T00:00:00Z"), Array(LocalDateTime.of(0, 1, 1, 12, 30).toInstant(ZoneOffset.UTC), "0000-01-01T12:30:00Z"), Array(LocalDateTime.of(0, 1, 1, 0, 0, 0, 1).toInstant(ZoneOffset.UTC), "0000-01-01T00:00:00.000000001Z"), Array(LocalDateTime.of(0, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), "0000-01-01T00:00:00Z"), Array(LocalDateTime.of(-1, 12, 31, 23, 59, 59, 999999999).toInstant(ZoneOffset.UTC), "-0001-12-31T23:59:59.999999999Z"), Array(LocalDateTime.of(-1, 12, 31, 12, 30).toInstant(ZoneOffset.UTC), "-0001-12-31T12:30:00Z"), Array(LocalDateTime.of(-1, 12, 30, 12, 30).toInstant(ZoneOffset.UTC), "-0001-12-30T12:30:00Z"), Array(LocalDateTime.of(-9999, 1, 2, 12, 30).toInstant(ZoneOffset.UTC), "-9999-01-02T12:30:00Z"), Array(LocalDateTime.of(-9999, 1, 1, 12, 30).toInstant(ZoneOffset.UTC), "-9999-01-01T12:30:00Z"), Array(LocalDateTime.of(-9999, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), "-9999-01-01T00:00:00Z"), Array(LocalDateTime.of(-10000, 12, 31, 23, 59, 59, 999999999).toInstant(ZoneOffset.UTC), "-10000-12-31T23:59:59.999999999Z"), Array(LocalDateTime.of(-10000, 12, 31, 12, 30).toInstant(ZoneOffset.UTC), "-10000-12-31T12:30:00Z"), Array(LocalDateTime.of(-10000, 12, 30, 12, 30).toInstant(ZoneOffset.UTC), "-10000-12-30T12:30:00Z"), Array(LocalDateTime.of(-15000, 12, 31, 12, 30).toInstant(ZoneOffset.UTC), "-15000-12-31T12:30:00Z"), Array(LocalDateTime.of(-19999, 1, 2, 12, 30).toInstant(ZoneOffset.UTC), "-19999-01-02T12:30:00Z"), Array(LocalDateTime.of(-19999, 1, 1, 12, 30).toInstant(ZoneOffset.UTC), "-19999-01-01T12:30:00Z"), Array(LocalDateTime.of(-19999, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), "-19999-01-01T00:00:00Z"), Array(LocalDateTime.of(-20000, 12, 31, 23, 59, 59, 999999999).toInstant(ZoneOffset.UTC), "-20000-12-31T23:59:59.999999999Z"), Array(LocalDateTime.of(-20000, 12, 31, 12, 30).toInstant(ZoneOffset.UTC), "-20000-12-31T12:30:00Z"), Array(LocalDateTime.of(-20000, 12, 30, 12, 30).toInstant(ZoneOffset.UTC), "-20000-12-30T12:30:00Z"), Array(LocalDateTime.of(-25000, 12, 31, 12, 30).toInstant(ZoneOffset.UTC), "-25000-12-31T12:30:00Z"), Array(LocalDateTime.of(9999, 12, 30, 12, 30).toInstant(ZoneOffset.UTC), "9999-12-30T12:30:00Z"), Array(LocalDateTime.of(9999, 12, 31, 12, 30).toInstant(ZoneOffset.UTC), "9999-12-31T12:30:00Z"), Array(LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999999999).toInstant(ZoneOffset.UTC), "9999-12-31T23:59:59.999999999Z"), Array(LocalDateTime.of(10000, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), "+10000-01-01T00:00:00Z"), Array(LocalDateTime.of(10000, 1, 1, 12, 30).toInstant(ZoneOffset.UTC), "+10000-01-01T12:30:00Z"), Array(LocalDateTime.of(10000, 1, 2, 12, 30).toInstant(ZoneOffset.UTC), "+10000-01-02T12:30:00Z"), Array(LocalDateTime.of(15000, 12, 31, 12, 30).toInstant(ZoneOffset.UTC), "+15000-12-31T12:30:00Z"), Array(LocalDateTime.of(19999, 12, 30, 12, 30).toInstant(ZoneOffset.UTC), "+19999-12-30T12:30:00Z"), Array(LocalDateTime.of(19999, 12, 31, 12, 30).toInstant(ZoneOffset.UTC), "+19999-12-31T12:30:00Z"), Array(LocalDateTime.of(19999, 12, 31, 23, 59, 59, 999999999).toInstant(ZoneOffset.UTC), "+19999-12-31T23:59:59.999999999Z"), Array(LocalDateTime.of(20000, 1, 1, 0, 0).toInstant(ZoneOffset.UTC), "+20000-01-01T00:00:00Z"), Array(LocalDateTime.of(20000, 1, 1, 12, 30).toInstant(ZoneOffset.UTC), "+20000-01-01T12:30:00Z"), Array(LocalDateTime.of(20000, 1, 2, 12, 30).toInstant(ZoneOffset.UTC), "+20000-01-02T12:30:00Z"), Array(LocalDateTime.of(25000, 12, 31, 12, 30).toInstant(ZoneOffset.UTC), "+25000-12-31T12:30:00Z"), Array(LocalDateTime.of(-999999999, 1, 1, 12, 30).toInstant(ZoneOffset.UTC).minus(1, DAYS), "-1000000000-12-31T12:30:00Z"), Array(LocalDateTime.of(999999999, 12, 31, 12, 30).toInstant(ZoneOffset.UTC).plus(1, DAYS), "+1000000000-01-01T12:30:00Z"), Array(Instant.MIN, "-1000000000-01-01T00:00:00Z"), Array(Instant.MAX, "+1000000000-12-31T23:59:59.999999999Z"))
  }

  @Test(dataProvider = "toStringParse") def test_toString(instant: Instant, expected: String): Unit = {
    assertEquals(instant.toString, expected)
  }

  @Test(dataProvider = "toStringParse") def test_parse(instant: Instant, text: String): Unit = {
    assertEquals(Instant.parse(text), instant)
  }

  @Test(dataProvider = "toStringParse") def test_parseLowercase(instant: Instant, text: String): Unit = {
    assertEquals(Instant.parse(text.toLowerCase(Locale.ENGLISH)), instant)
  }
}