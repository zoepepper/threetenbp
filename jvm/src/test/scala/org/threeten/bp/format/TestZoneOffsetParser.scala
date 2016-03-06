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
package org.threeten.bp.format

import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.threeten.bp.temporal.ChronoField.OFFSET_SECONDS
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.TemporalQueries

/** Test OffsetIdPrinterParser. */
@Test class TestZoneOffsetParser extends AbstractTestPrinterParser {
  @DataProvider(name = "error") private[format] def data_error: Array[Array[Any]] = {
    Array[Array[Any]](Array(new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", "+HH:MM:ss"), "hello", -1, classOf[IndexOutOfBoundsException]), Array(new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", "+HH:MM:ss"), "hello", 6, classOf[IndexOutOfBoundsException]))
  }

  @Test(dataProvider = "error") def test_parse_error(pp: DateTimeFormatterBuilder.OffsetIdPrinterParser, text: String, pos: Int, expected: Class[_]): Unit = {
    try {
      pp.parse(parseContext, text, pos)
    }
    catch {
      case ex: RuntimeException =>
        assertTrue(expected.isInstance(ex))
        assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
        assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
    }
  }

  @throws(classOf[Exception])
  def test_parse_exactMatch_UTC(): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", "+HH:MM:ss")
    val result: Int = pp.parse(parseContext, "Z", 0)
    assertEquals(result, 1)
    assertParsed(ZoneOffset.UTC)
  }

  @throws(classOf[Exception])
  def test_parse_startStringMatch_UTC(): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", "+HH:MM:ss")
    val result: Int = pp.parse(parseContext, "ZOTHER", 0)
    assertEquals(result, 1)
    assertParsed(ZoneOffset.UTC)
  }

  @throws(classOf[Exception])
  def test_parse_midStringMatch_UTC(): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", "+HH:MM:ss")
    val result: Int = pp.parse(parseContext, "OTHERZOTHER", 5)
    assertEquals(result, 6)
    assertParsed(ZoneOffset.UTC)
  }

  @throws(classOf[Exception])
  def test_parse_endStringMatch_UTC(): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", "+HH:MM:ss")
    val result: Int = pp.parse(parseContext, "OTHERZ", 5)
    assertEquals(result, 6)
    assertParsed(ZoneOffset.UTC)
  }

  @throws(classOf[Exception])
  def test_parse_exactMatch_UTC_EmptyUTC(): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("", "+HH:MM:ss")
    val result: Int = pp.parse(parseContext, "", 0)
    assertEquals(result, 0)
    assertParsed(ZoneOffset.UTC)
  }

  @throws(classOf[Exception])
  def test_parse_startStringMatch_UTC_EmptyUTC(): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("", "+HH:MM:ss")
    val result: Int = pp.parse(parseContext, "OTHER", 0)
    assertEquals(result, 0)
    assertParsed(ZoneOffset.UTC)
  }

  @throws(classOf[Exception])
  def test_parse_midStringMatch_UTC_EmptyUTC(): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("", "+HH:MM:ss")
    val result: Int = pp.parse(parseContext, "OTHEROTHER", 5)
    assertEquals(result, 5)
    assertParsed(ZoneOffset.UTC)
  }

  @throws(classOf[Exception])
  def test_parse_endStringMatch_UTC_EmptyUTC(): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("", "+HH:MM:ss")
    val result: Int = pp.parse(parseContext, "OTHER", 5)
    assertEquals(result, 5)
    assertParsed(ZoneOffset.UTC)
  }

  @DataProvider(name = "offsets") private[format] def provider_offsets: Array[Array[AnyRef]] = {
    Array[Array[AnyRef]](Array("+HH", "+00", ZoneOffset.UTC), Array("+HH", "-00", ZoneOffset.UTC), Array("+HH", "+01", ZoneOffset.ofHours(1)), Array("+HH", "-01", ZoneOffset.ofHours(-1)), Array("+HHMM", "+0000", ZoneOffset.UTC), Array("+HHMM", "-0000", ZoneOffset.UTC), Array("+HHMM", "+0102", ZoneOffset.ofHoursMinutes(1, 2)), Array("+HHMM", "-0102", ZoneOffset.ofHoursMinutes(-1, -2)), Array("+HH:MM", "+00:00", ZoneOffset.UTC), Array("+HH:MM", "-00:00", ZoneOffset.UTC), Array("+HH:MM", "+01:02", ZoneOffset.ofHoursMinutes(1, 2)), Array("+HH:MM", "-01:02", ZoneOffset.ofHoursMinutes(-1, -2)), Array("+HHMMss", "+0000", ZoneOffset.UTC), Array("+HHMMss", "-0000", ZoneOffset.UTC), Array("+HHMMss", "+0100", ZoneOffset.ofHoursMinutesSeconds(1, 0, 0)), Array("+HHMMss", "+0159", ZoneOffset.ofHoursMinutesSeconds(1, 59, 0)), Array("+HHMMss", "+0200", ZoneOffset.ofHoursMinutesSeconds(2, 0, 0)), Array("+HHMMss", "+1800", ZoneOffset.ofHoursMinutesSeconds(18, 0, 0)), Array("+HHMMss", "+010215", ZoneOffset.ofHoursMinutesSeconds(1, 2, 15)), Array("+HHMMss", "-0100", ZoneOffset.ofHoursMinutesSeconds(-1, 0, 0)), Array("+HHMMss", "-0200", ZoneOffset.ofHoursMinutesSeconds(-2, 0, 0)), Array("+HHMMss", "-1800", ZoneOffset.ofHoursMinutesSeconds(-18, 0, 0)), Array("+HHMMss", "+000000", ZoneOffset.UTC), Array("+HHMMss", "-000000", ZoneOffset.UTC), Array("+HHMMss", "+010000", ZoneOffset.ofHoursMinutesSeconds(1, 0, 0)), Array("+HHMMss", "+010203", ZoneOffset.ofHoursMinutesSeconds(1, 2, 3)), Array("+HHMMss", "+015959", ZoneOffset.ofHoursMinutesSeconds(1, 59, 59)), Array("+HHMMss", "+020000", ZoneOffset.ofHoursMinutesSeconds(2, 0, 0)), Array("+HHMMss", "+180000", ZoneOffset.ofHoursMinutesSeconds(18, 0, 0)), Array("+HHMMss", "-010000", ZoneOffset.ofHoursMinutesSeconds(-1, 0, 0)), Array("+HHMMss", "-020000", ZoneOffset.ofHoursMinutesSeconds(-2, 0, 0)), Array("+HHMMss", "-180000", ZoneOffset.ofHoursMinutesSeconds(-18, 0, 0)), Array("+HH:MM:ss", "+00:00", ZoneOffset.UTC), Array("+HH:MM:ss", "-00:00", ZoneOffset.UTC), Array("+HH:MM:ss", "+01:00", ZoneOffset.ofHoursMinutesSeconds(1, 0, 0)), Array("+HH:MM:ss", "+01:02", ZoneOffset.ofHoursMinutesSeconds(1, 2, 0)), Array("+HH:MM:ss", "+01:59", ZoneOffset.ofHoursMinutesSeconds(1, 59, 0)), Array("+HH:MM:ss", "+02:00", ZoneOffset.ofHoursMinutesSeconds(2, 0, 0)), Array("+HH:MM:ss", "+18:00", ZoneOffset.ofHoursMinutesSeconds(18, 0, 0)), Array("+HH:MM:ss", "+01:02:15", ZoneOffset.ofHoursMinutesSeconds(1, 2, 15)), Array("+HH:MM:ss", "-01:00", ZoneOffset.ofHoursMinutesSeconds(-1, 0, 0)), Array("+HH:MM:ss", "-02:00", ZoneOffset.ofHoursMinutesSeconds(-2, 0, 0)), Array("+HH:MM:ss", "-18:00", ZoneOffset.ofHoursMinutesSeconds(-18, 0, 0)), Array("+HH:MM:ss", "+00:00:00", ZoneOffset.UTC), Array("+HH:MM:ss", "-00:00:00", ZoneOffset.UTC), Array("+HH:MM:ss", "+01:00:00", ZoneOffset.ofHoursMinutesSeconds(1, 0, 0)), Array("+HH:MM:ss", "+01:02:03", ZoneOffset.ofHoursMinutesSeconds(1, 2, 3)), Array("+HH:MM:ss", "+01:59:59", ZoneOffset.ofHoursMinutesSeconds(1, 59, 59)), Array("+HH:MM:ss", "+02:00:00", ZoneOffset.ofHoursMinutesSeconds(2, 0, 0)), Array("+HH:MM:ss", "+18:00:00", ZoneOffset.ofHoursMinutesSeconds(18, 0, 0)), Array("+HH:MM:ss", "-01:00:00", ZoneOffset.ofHoursMinutesSeconds(-1, 0, 0)), Array("+HH:MM:ss", "-02:00:00", ZoneOffset.ofHoursMinutesSeconds(-2, 0, 0)), Array("+HH:MM:ss", "-18:00:00", ZoneOffset.ofHoursMinutesSeconds(-18, 0, 0)), Array("+HHMMSS", "+000000", ZoneOffset.UTC), Array("+HHMMSS", "-000000", ZoneOffset.UTC), Array("+HHMMSS", "+010203", ZoneOffset.ofHoursMinutesSeconds(1, 2, 3)), Array("+HHMMSS", "-010203", ZoneOffset.ofHoursMinutesSeconds(-1, -2, -3)), Array("+HH:MM:SS", "+00:00:00", ZoneOffset.UTC), Array("+HH:MM:SS", "-00:00:00", ZoneOffset.UTC), Array("+HH:MM:SS", "+01:02:03", ZoneOffset.ofHoursMinutesSeconds(1, 2, 3)), Array("+HH:MM:SS", "-01:02:03", ZoneOffset.ofHoursMinutesSeconds(-1, -2, -3)))
  }

  @Test(dataProvider = "offsets")
  @throws(classOf[Exception])
  def test_parse_exactMatch(pattern: String, parse: String, expected: ZoneOffset): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", pattern)
    val result: Int = pp.parse(parseContext, parse, 0)
    assertEquals(result, parse.length)
    assertParsed(expected)
  }

  @Test(dataProvider = "offsets")
  @throws(classOf[Exception])
  def test_parse_startStringMatch(pattern: String, parse: String, expected: ZoneOffset): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", pattern)
    val result: Int = pp.parse(parseContext, parse + ":OTHER", 0)
    assertEquals(result, parse.length)
    assertParsed(expected)
  }

  @Test(dataProvider = "offsets")
  @throws(classOf[Exception])
  def test_parse_midStringMatch(pattern: String, parse: String, expected: ZoneOffset): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", pattern)
    val result: Int = pp.parse(parseContext, "OTHER" + parse + ":OTHER", 5)
    assertEquals(result, parse.length + 5)
    assertParsed(expected)
  }

  @Test(dataProvider = "offsets")
  @throws(classOf[Exception])
  def test_parse_endStringMatch(pattern: String, parse: String, expected: ZoneOffset): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", pattern)
    val result: Int = pp.parse(parseContext, "OTHER" + parse, 5)
    assertEquals(result, parse.length + 5)
    assertParsed(expected)
  }

  @Test(dataProvider = "offsets")
  @throws(classOf[Exception])
  def test_parse_exactMatch_EmptyUTC(pattern: String, parse: String, expected: ZoneOffset): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("", pattern)
    val result: Int = pp.parse(parseContext, parse, 0)
    assertEquals(result, parse.length)
    assertParsed(expected)
  }

  @Test(dataProvider = "offsets")
  @throws(classOf[Exception])
  def test_parse_startStringMatch_EmptyUTC(pattern: String, parse: String, expected: ZoneOffset): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("", pattern)
    val result: Int = pp.parse(parseContext, parse + ":OTHER", 0)
    assertEquals(result, parse.length)
    assertParsed(expected)
  }

  @Test(dataProvider = "offsets")
  @throws(classOf[Exception])
  def test_parse_midStringMatch_EmptyUTC(pattern: String, parse: String, expected: ZoneOffset): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("", pattern)
    val result: Int = pp.parse(parseContext, "OTHER" + parse + ":OTHER", 5)
    assertEquals(result, parse.length + 5)
    assertParsed(expected)
  }

  @Test(dataProvider = "offsets")
  @throws(classOf[Exception])
  def test_parse_endStringMatch_EmptyUTC(pattern: String, parse: String, expected: ZoneOffset): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("", pattern)
    val result: Int = pp.parse(parseContext, "OTHER" + parse, 5)
    assertEquals(result, parse.length + 5)
    assertParsed(expected)
  }

  @DataProvider(name = "bigOffsets") private[format] def provider_bigOffsets: Array[Array[Any]] = {
    Array[Array[Any]](Array("+HH", "+59", 59 * 3600), Array("+HH", "-19", -(19 * 3600)), Array("+HHMM", "+1801", 18 * 3600 + 1 * 60), Array("+HHMM", "-1801", -(18 * 3600 + 1 * 60)), Array("+HH:MM", "+18:01", 18 * 3600 + 1 * 60), Array("+HH:MM", "-18:01", -(18 * 3600 + 1 * 60)), Array("+HHMMss", "+180103", 18 * 3600 + 1 * 60 + 3), Array("+HHMMss", "-180103", -(18 * 3600 + 1 * 60 + 3)), Array("+HH:MM:ss", "+18:01:03", 18 * 3600 + 1 * 60 + 3), Array("+HH:MM:ss", "-18:01:03", -(18 * 3600 + 1 * 60 + 3)), Array("+HHMMSS", "+180103", 18 * 3600 + 1 * 60 + 3), Array("+HHMMSS", "-180103", -(18 * 3600 + 1 * 60 + 3)), Array("+HH:MM:SS", "+18:01:03", 18 * 3600 + 1 * 60 + 3), Array("+HH:MM:SS", "-18:01:03", -(18 * 3600 + 1 * 60 + 3)))
  }

  @Test(dataProvider = "bigOffsets")
  @throws(classOf[Exception])
  def test_parse_bigOffsets(pattern: String, parse: String, offsetSecs: Long): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", pattern)
    val result: Int = pp.parse(parseContext, parse, 0)
    assertEquals(result, parse.length)
    assertEquals(parseContext.getParsed(OFFSET_SECONDS), offsetSecs.asInstanceOf[Long])
  }

  @DataProvider(name = "badOffsets") private[format] def provider_badOffsets: Array[Array[Any]] = {
    Array[Array[Any]](Array("+HH", "+1", ~0), Array("+HH", "-1", ~0), Array("+HH", "01", ~0), Array("+HH", "01", ~0), Array("+HH", "+AA", ~0), Array("+HHMM", "+1", ~0), Array("+HHMM", "+01", ~0), Array("+HHMM", "+001", ~0), Array("+HHMM", "0102", ~0), Array("+HHMM", "+01:02", ~0), Array("+HHMM", "+AAAA", ~0), Array("+HH:MM", "+1", ~0), Array("+HH:MM", "+01", ~0), Array("+HH:MM", "+0:01", ~0), Array("+HH:MM", "+00:1", ~0), Array("+HH:MM", "+0:1", ~0), Array("+HH:MM", "+:", ~0), Array("+HH:MM", "01:02", ~0), Array("+HH:MM", "+0102", ~0), Array("+HH:MM", "+AA:AA", ~0), Array("+HHMMss", "+1", ~0), Array("+HHMMss", "+01", ~0), Array("+HHMMss", "+001", ~0), Array("+HHMMss", "0102", ~0), Array("+HHMMss", "+01:02", ~0), Array("+HHMMss", "+AAAA", ~0), Array("+HH:MM:ss", "+1", ~0), Array("+HH:MM:ss", "+01", ~0), Array("+HH:MM:ss", "+0:01", ~0), Array("+HH:MM:ss", "+00:1", ~0), Array("+HH:MM:ss", "+0:1", ~0), Array("+HH:MM:ss", "+:", ~0), Array("+HH:MM:ss", "01:02", ~0), Array("+HH:MM:ss", "+0102", ~0), Array("+HH:MM:ss", "+AA:AA", ~0), Array("+HHMMSS", "+1", ~0), Array("+HHMMSS", "+01", ~0), Array("+HHMMSS", "+001", ~0), Array("+HHMMSS", "0102", ~0), Array("+HHMMSS", "+01:02", ~0), Array("+HHMMSS", "+AAAA", ~0), Array("+HH:MM:SS", "+1", ~0), Array("+HH:MM:SS", "+01", ~0), Array("+HH:MM:SS", "+0:01", ~0), Array("+HH:MM:SS", "+00:1", ~0), Array("+HH:MM:SS", "+0:1", ~0), Array("+HH:MM:SS", "+:", ~0), Array("+HH:MM:SS", "01:02", ~0), Array("+HH:MM:SS", "+0102", ~0), Array("+HH:MM:SS", "+AA:AA", ~0))
  }

  @Test(dataProvider = "badOffsets")
  @throws(classOf[Exception])
  def test_parse_invalid(pattern: String, parse: String, expectedPosition: Int): Unit = {
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", pattern)
    val result: Int = pp.parse(parseContext, parse, 0)
    assertEquals(result, expectedPosition)
  }

  @throws(classOf[Exception])
  def test_parse_caseSensitiveUTC_matchedCase(): Unit = {
    parseContext.setCaseSensitive(true)
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", "+HH:MM:ss")
    val result: Int = pp.parse(parseContext, "Z", 0)
    assertEquals(result, 1)
    assertParsed(ZoneOffset.UTC)
  }

  @throws(classOf[Exception])
  def test_parse_caseSensitiveUTC_unmatchedCase(): Unit = {
    parseContext.setCaseSensitive(true)
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", "+HH:MM:ss")
    val result: Int = pp.parse(parseContext, "z", 0)
    assertEquals(result, ~0)
    assertParsed(null)
  }

  @throws(classOf[Exception])
  def test_parse_caseInsensitiveUTC_matchedCase(): Unit = {
    parseContext.setCaseSensitive(false)
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", "+HH:MM:ss")
    val result: Int = pp.parse(parseContext, "Z", 0)
    assertEquals(result, 1)
    assertParsed(ZoneOffset.UTC)
  }

  @throws(classOf[Exception])
  def test_parse_caseInsensitiveUTC_unmatchedCase(): Unit = {
    parseContext.setCaseSensitive(false)
    val pp: DateTimeFormatterBuilder.OffsetIdPrinterParser = new DateTimeFormatterBuilder.OffsetIdPrinterParser("Z", "+HH:MM:ss")
    val result: Int = pp.parse(parseContext, "z", 0)
    assertEquals(result, 1)
    assertParsed(ZoneOffset.UTC)
  }

  private def assertParsed(expectedOffset: ZoneOffset): Unit = {
    assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
    assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
    if (expectedOffset == null) {
      assertEquals(parseContext.getParsed(OFFSET_SECONDS), null)
    }
    else {
      assertEquals(parseContext.getParsed(OFFSET_SECONDS), expectedOffset.getTotalSeconds.toLong)
    }
  }
}