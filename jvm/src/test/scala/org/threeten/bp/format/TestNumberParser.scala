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
import org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH
import org.threeten.bp.temporal.ChronoField.DAY_OF_WEEK
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.TemporalQueries

/**
  * Test NumberPrinterParser.
  */
@Test class TestNumberParser extends AbstractTestPrinterParser {
  @DataProvider(name = "error") private[format] def data_error: Array[Array[Any]] = {
    Array[Array[Any]](Array(new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_MONTH, 1, 2, SignStyle.NEVER), "12", -1, classOf[IndexOutOfBoundsException]), Array(new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_MONTH, 1, 2, SignStyle.NEVER), "12", 3, classOf[IndexOutOfBoundsException]))
  }

  @Test(dataProvider = "error") def test_parse_error(pp: DateTimeFormatterBuilder.NumberPrinterParser, text: String, pos: Int, expected: Class[_]): Unit = {
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

  @DataProvider(name = "parseData") private[format] def provider_parseData: Array[Array[Any]] = {
    Array[Array[Any]](Array(1, 2, SignStyle.NEVER, 0, "12", 0, 2, 12L), Array(1, 2, SignStyle.NEVER, 0, "Xxx12Xxx", 3, 5, 12L), Array(1, 2, SignStyle.NEVER, 0, "99912999", 3, 5, 12L), Array(2, 4, SignStyle.NEVER, 0, "12345", 0, 4, 1234L), Array(2, 4, SignStyle.NEVER, 0, "12-45", 0, 2, 12L), Array(2, 4, SignStyle.NEVER, 0, "123-5", 0, 3, 123L), Array(1, 10, SignStyle.NORMAL, 0, "2147483647", 0, 10, Integer.MAX_VALUE), Array(1, 10, SignStyle.NORMAL, 0, "-2147483648", 0, 11, Integer.MIN_VALUE), Array(1, 10, SignStyle.NORMAL, 0, "2147483648", 0, 10, 2147483648L), Array(1, 10, SignStyle.NORMAL, 0, "-2147483649", 0, 11, -2147483649L), Array(1, 10, SignStyle.NORMAL, 0, "987659876598765", 0, 10, 9876598765L), Array(1, 19, SignStyle.NORMAL, 0, "999999999999999999", 0, 18, 999999999999999999L), Array(1, 19, SignStyle.NORMAL, 0, "-999999999999999999", 0, 19, -999999999999999999L), Array(1, 19, SignStyle.NORMAL, 0, "1000000000000000000", 0, 19, 1000000000000000000L), Array(1, 19, SignStyle.NORMAL, 0, "-1000000000000000000", 0, 20, -1000000000000000000L), Array(1, 19, SignStyle.NORMAL, 0, "000000000000000000", 0, 18, 0L), Array(1, 19, SignStyle.NORMAL, 0, "0000000000000000000", 0, 19, 0L), Array(1, 19, SignStyle.NORMAL, 0, "9223372036854775807", 0, 19, Long.MaxValue), Array(1, 19, SignStyle.NORMAL, 0, "-9223372036854775808", 0, 20, Long.MinValue), Array(1, 19, SignStyle.NORMAL, 0, "9223372036854775808", 0, 18, 922337203685477580L), Array(1, 19, SignStyle.NORMAL, 0, "-9223372036854775809", 0, 19, -922337203685477580L), Array(1, 2, SignStyle.NEVER, 1, "A1", 0, ~0, 0), Array(1, 2, SignStyle.NEVER, 1, " 1", 0, ~0, 0), Array(1, 2, SignStyle.NEVER, 1, "  1", 1, ~1, 0), Array(2, 2, SignStyle.NEVER, 1, "1", 0, ~0, 0), Array(2, 2, SignStyle.NEVER, 1, "Xxx1", 0, ~0, 0), Array(2, 2, SignStyle.NEVER, 1, "1", 1, ~1, 0), Array(2, 2, SignStyle.NEVER, 1, "Xxx1", 4, ~4, 0), Array(2, 2, SignStyle.NEVER, 1, "1-2", 0, ~0, 0), Array(1, 19, SignStyle.NORMAL, 0, "-000000000000000000", 0, ~0, 0), Array(1, 19, SignStyle.NORMAL, 0, "-0000000000000000000", 0, ~0, 0), Array(1, 1, SignStyle.NEVER, 1, "12", 0, 1, 1L), Array(1, 19, SignStyle.NEVER, 1, "12", 0, 1, 1L), Array(1, 19, SignStyle.NEVER, 1, "12345", 0, 4, 1234L), Array(1, 19, SignStyle.NEVER, 1, "12345678901", 0, 10, 1234567890L), Array(1, 19, SignStyle.NEVER, 1, "123456789012345678901234567890", 0, 19, 1234567890123456789L), Array(1, 19, SignStyle.NEVER, 1, "1", 0, 1, 1L), Array(2, 2, SignStyle.NEVER, 1, "12", 0, 2, 12L), Array(2, 19, SignStyle.NEVER, 1, "1", 0, ~0, 0), Array(1, 1, SignStyle.NEVER, 2, "123", 0, 1, 1L), Array(1, 19, SignStyle.NEVER, 2, "123", 0, 1, 1L), Array(1, 19, SignStyle.NEVER, 2, "12345", 0, 3, 123L), Array(1, 19, SignStyle.NEVER, 2, "12345678901", 0, 9, 123456789L), Array(1, 19, SignStyle.NEVER, 2, "123456789012345678901234567890", 0, 19, 1234567890123456789L), Array(1, 19, SignStyle.NEVER, 2, "1", 0, 1, 1L), Array(1, 19, SignStyle.NEVER, 2, "12", 0, 1, 1L), Array(2, 2, SignStyle.NEVER, 2, "12", 0, 2, 12L), Array(2, 19, SignStyle.NEVER, 2, "1", 0, ~0, 0), Array(2, 19, SignStyle.NEVER, 2, "1AAAAABBBBBCCCCC", 0, ~0, 0))
  }

  @Test(dataProvider = "parseData") def test_parse_fresh(minWidth: Int, maxWidth: Int, signStyle: SignStyle, subsequentWidth: Int, text: String, pos: Int, expectedPos: Int, expectedValue: Long): Unit = {
    var pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_MONTH, minWidth, maxWidth, signStyle)
    if (subsequentWidth > 0) {
      pp = pp.withSubsequentWidth(subsequentWidth)
    }
    val newPos: Int = pp.parse(parseContext, text, pos)
    assertEquals(newPos, expectedPos)
    if (expectedPos > 0) {
      assertParsed(parseContext, DAY_OF_MONTH, expectedValue)
    }
    else {
      assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
      assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
    }
  }

  @Test(dataProvider = "parseData") def test_parse_textField(minWidth: Int, maxWidth: Int, signStyle: SignStyle, subsequentWidth: Int, text: String, pos: Int, expectedPos: Int, expectedValue: Long): Unit = {
    var pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_WEEK, minWidth, maxWidth, signStyle)
    if (subsequentWidth > 0) {
      pp = pp.withSubsequentWidth(subsequentWidth)
    }
    val newPos: Int = pp.parse(parseContext, text, pos)
    assertEquals(newPos, expectedPos)
    if (expectedPos > 0) {
      assertParsed(parseContext, DAY_OF_WEEK, expectedValue)
    }
  }

  @DataProvider(name = "parseSignsStrict") private[format] def provider_parseSignsStrict: Array[Array[Any]] = {
    Array[Array[Any]](Array("0", 1, 2, SignStyle.NEVER, 1, 0), Array("1", 1, 2, SignStyle.NEVER, 1, 1), Array("2", 1, 2, SignStyle.NEVER, 1, 2), Array("3", 1, 2, SignStyle.NEVER, 1, 3), Array("4", 1, 2, SignStyle.NEVER, 1, 4), Array("5", 1, 2, SignStyle.NEVER, 1, 5), Array("6", 1, 2, SignStyle.NEVER, 1, 6), Array("7", 1, 2, SignStyle.NEVER, 1, 7), Array("8", 1, 2, SignStyle.NEVER, 1, 8), Array("9", 1, 2, SignStyle.NEVER, 1, 9), Array("10", 1, 2, SignStyle.NEVER, 2, 10), Array("100", 1, 2, SignStyle.NEVER, 2, 10), Array("100", 1, 3, SignStyle.NEVER, 3, 100), Array("0", 1, 2, SignStyle.NEVER, 1, 0), Array("5", 1, 2, SignStyle.NEVER, 1, 5), Array("50", 1, 2, SignStyle.NEVER, 2, 50), Array("500", 1, 2, SignStyle.NEVER, 2, 50), Array("-0", 1, 2, SignStyle.NEVER, ~0, null), Array("-5", 1, 2, SignStyle.NEVER, ~0, null), Array("-50", 1, 2, SignStyle.NEVER, ~0, null), Array("-500", 1, 2, SignStyle.NEVER, ~0, null), Array("-AAA", 1, 2, SignStyle.NEVER, ~0, null), Array("+0", 1, 2, SignStyle.NEVER, ~0, null), Array("+5", 1, 2, SignStyle.NEVER, ~0, null), Array("+50", 1, 2, SignStyle.NEVER, ~0, null), Array("+500", 1, 2, SignStyle.NEVER, ~0, null), Array("+AAA", 1, 2, SignStyle.NEVER, ~0, null), Array("0", 1, 2, SignStyle.NOT_NEGATIVE, 1, 0), Array("5", 1, 2, SignStyle.NOT_NEGATIVE, 1, 5), Array("50", 1, 2, SignStyle.NOT_NEGATIVE, 2, 50), Array("500", 1, 2, SignStyle.NOT_NEGATIVE, 2, 50), Array("-0", 1, 2, SignStyle.NOT_NEGATIVE, ~0, null), Array("-5", 1, 2, SignStyle.NOT_NEGATIVE, ~0, null), Array("-50", 1, 2, SignStyle.NOT_NEGATIVE, ~0, null), Array("-500", 1, 2, SignStyle.NOT_NEGATIVE, ~0, null), Array("-AAA", 1, 2, SignStyle.NOT_NEGATIVE, ~0, null), Array("+0", 1, 2, SignStyle.NOT_NEGATIVE, ~0, null), Array("+5", 1, 2, SignStyle.NOT_NEGATIVE, ~0, null), Array("+50", 1, 2, SignStyle.NOT_NEGATIVE, ~0, null), Array("+500", 1, 2, SignStyle.NOT_NEGATIVE, ~0, null), Array("+AAA", 1, 2, SignStyle.NOT_NEGATIVE, ~0, null), Array("0", 1, 2, SignStyle.NORMAL, 1, 0), Array("5", 1, 2, SignStyle.NORMAL, 1, 5), Array("50", 1, 2, SignStyle.NORMAL, 2, 50), Array("500", 1, 2, SignStyle.NORMAL, 2, 50), Array("-0", 1, 2, SignStyle.NORMAL, ~0, null), Array("-5", 1, 2, SignStyle.NORMAL, 2, -5), Array("-50", 1, 2, SignStyle.NORMAL, 3, -50), Array("-500", 1, 2, SignStyle.NORMAL, 3, -50), Array("-AAA", 1, 2, SignStyle.NORMAL, ~1, null), Array("+0", 1, 2, SignStyle.NORMAL, ~0, null), Array("+5", 1, 2, SignStyle.NORMAL, ~0, null), Array("+50", 1, 2, SignStyle.NORMAL, ~0, null), Array("+500", 1, 2, SignStyle.NORMAL, ~0, null), Array("+AAA", 1, 2, SignStyle.NORMAL, ~0, null), Array("0", 1, 2, SignStyle.ALWAYS, ~0, null), Array("5", 1, 2, SignStyle.ALWAYS, ~0, null), Array("50", 1, 2, SignStyle.ALWAYS, ~0, null), Array("500", 1, 2, SignStyle.ALWAYS, ~0, null), Array("-0", 1, 2, SignStyle.ALWAYS, ~0, null), Array("-5", 1, 2, SignStyle.ALWAYS, 2, -5), Array("-50", 1, 2, SignStyle.ALWAYS, 3, -50), Array("-500", 1, 2, SignStyle.ALWAYS, 3, -50), Array("-AAA", 1, 2, SignStyle.ALWAYS, ~1, null), Array("+0", 1, 2, SignStyle.ALWAYS, 2, 0), Array("+5", 1, 2, SignStyle.ALWAYS, 2, 5), Array("+50", 1, 2, SignStyle.ALWAYS, 3, 50), Array("+500", 1, 2, SignStyle.ALWAYS, 3, 50), Array("+AAA", 1, 2, SignStyle.ALWAYS, ~1, null), Array("0", 1, 2, SignStyle.EXCEEDS_PAD, 1, 0), Array("5", 1, 2, SignStyle.EXCEEDS_PAD, 1, 5), Array("50", 1, 2, SignStyle.EXCEEDS_PAD, ~0, null), Array("500", 1, 2, SignStyle.EXCEEDS_PAD, ~0, null), Array("-0", 1, 2, SignStyle.EXCEEDS_PAD, ~0, null), Array("-5", 1, 2, SignStyle.EXCEEDS_PAD, 2, -5), Array("-50", 1, 2, SignStyle.EXCEEDS_PAD, 3, -50), Array("-500", 1, 2, SignStyle.EXCEEDS_PAD, 3, -50), Array("-AAA", 1, 2, SignStyle.EXCEEDS_PAD, ~1, null), Array("+0", 1, 2, SignStyle.EXCEEDS_PAD, ~0, null), Array("+5", 1, 2, SignStyle.EXCEEDS_PAD, ~0, null), Array("+50", 1, 2, SignStyle.EXCEEDS_PAD, 3, 50), Array("+500", 1, 2, SignStyle.EXCEEDS_PAD, 3, 50), Array("+AAA", 1, 2, SignStyle.EXCEEDS_PAD, ~1, null))
  }

  @Test(dataProvider = "parseSignsStrict")
  @throws(classOf[Exception])
  def test_parseSignsStrict(input: String, min: Int, max: Int, style: SignStyle, parseLen: Int, parseVal: Integer): Unit = {
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_MONTH, min, max, style)
    val newPos: Int = pp.parse(parseContext, input, 0)
    assertEquals(newPos, parseLen)
    assertParsed(parseContext, DAY_OF_MONTH, if (parseVal != null) parseVal.toLong else null)
  }

  @DataProvider(name = "parseSignsLenient") private[format] def provider_parseSignsLenient: Array[Array[Any]] = {
    Array[Array[Any]](Array("0", 1, 2, SignStyle.NEVER, 1, 0), Array("5", 1, 2, SignStyle.NEVER, 1, 5), Array("50", 1, 2, SignStyle.NEVER, 2, 50), Array("500", 1, 2, SignStyle.NEVER, 3, 500), Array("-0", 1, 2, SignStyle.NEVER, 2, 0), Array("-5", 1, 2, SignStyle.NEVER, 2, -5), Array("-50", 1, 2, SignStyle.NEVER, 3, -50), Array("-500", 1, 2, SignStyle.NEVER, 4, -500), Array("-AAA", 1, 2, SignStyle.NEVER, ~1, null), Array("+0", 1, 2, SignStyle.NEVER, 2, 0), Array("+5", 1, 2, SignStyle.NEVER, 2, 5), Array("+50", 1, 2, SignStyle.NEVER, 3, 50), Array("+500", 1, 2, SignStyle.NEVER, 4, 500), Array("+AAA", 1, 2, SignStyle.NEVER, ~1, null), Array("50", 2, 2, SignStyle.NEVER, 2, 50), Array("-50", 2, 2, SignStyle.NEVER, ~0, null), Array("+50", 2, 2, SignStyle.NEVER, ~0, null), Array("0", 1, 2, SignStyle.NOT_NEGATIVE, 1, 0), Array("5", 1, 2, SignStyle.NOT_NEGATIVE, 1, 5), Array("50", 1, 2, SignStyle.NOT_NEGATIVE, 2, 50), Array("500", 1, 2, SignStyle.NOT_NEGATIVE, 3, 500), Array("-0", 1, 2, SignStyle.NOT_NEGATIVE, 2, 0), Array("-5", 1, 2, SignStyle.NOT_NEGATIVE, 2, -5), Array("-50", 1, 2, SignStyle.NOT_NEGATIVE, 3, -50), Array("-500", 1, 2, SignStyle.NOT_NEGATIVE, 4, -500), Array("-AAA", 1, 2, SignStyle.NOT_NEGATIVE, ~1, null), Array("+0", 1, 2, SignStyle.NOT_NEGATIVE, 2, 0), Array("+5", 1, 2, SignStyle.NOT_NEGATIVE, 2, 5), Array("+50", 1, 2, SignStyle.NOT_NEGATIVE, 3, 50), Array("+500", 1, 2, SignStyle.NOT_NEGATIVE, 4, 500), Array("+AAA", 1, 2, SignStyle.NOT_NEGATIVE, ~1, null), Array("50", 2, 2, SignStyle.NOT_NEGATIVE, 2, 50), Array("-50", 2, 2, SignStyle.NOT_NEGATIVE, ~0, null), Array("+50", 2, 2, SignStyle.NOT_NEGATIVE, ~0, null), Array("0", 1, 2, SignStyle.NORMAL, 1, 0), Array("5", 1, 2, SignStyle.NORMAL, 1, 5), Array("50", 1, 2, SignStyle.NORMAL, 2, 50), Array("500", 1, 2, SignStyle.NORMAL, 3, 500), Array("-0", 1, 2, SignStyle.NORMAL, 2, 0), Array("-5", 1, 2, SignStyle.NORMAL, 2, -5), Array("-50", 1, 2, SignStyle.NORMAL, 3, -50), Array("-500", 1, 2, SignStyle.NORMAL, 4, -500), Array("-AAA", 1, 2, SignStyle.NORMAL, ~1, null), Array("+0", 1, 2, SignStyle.NORMAL, 2, 0), Array("+5", 1, 2, SignStyle.NORMAL, 2, 5), Array("+50", 1, 2, SignStyle.NORMAL, 3, 50), Array("+500", 1, 2, SignStyle.NORMAL, 4, 500), Array("+AAA", 1, 2, SignStyle.NORMAL, ~1, null), Array("50", 2, 2, SignStyle.NORMAL, 2, 50), Array("-50", 2, 2, SignStyle.NORMAL, 3, -50), Array("+50", 2, 2, SignStyle.NORMAL, 3, 50), Array("0", 1, 2, SignStyle.ALWAYS, 1, 0), Array("5", 1, 2, SignStyle.ALWAYS, 1, 5), Array("50", 1, 2, SignStyle.ALWAYS, 2, 50), Array("500", 1, 2, SignStyle.ALWAYS, 3, 500), Array("-0", 1, 2, SignStyle.ALWAYS, 2, 0), Array("-5", 1, 2, SignStyle.ALWAYS, 2, -5), Array("-50", 1, 2, SignStyle.ALWAYS, 3, -50), Array("-500", 1, 2, SignStyle.ALWAYS, 4, -500), Array("-AAA", 1, 2, SignStyle.ALWAYS, ~1, null), Array("+0", 1, 2, SignStyle.ALWAYS, 2, 0), Array("+5", 1, 2, SignStyle.ALWAYS, 2, 5), Array("+50", 1, 2, SignStyle.ALWAYS, 3, 50), Array("+500", 1, 2, SignStyle.ALWAYS, 4, 500), Array("+AAA", 1, 2, SignStyle.ALWAYS, ~1, null), Array("0", 1, 2, SignStyle.EXCEEDS_PAD, 1, 0), Array("5", 1, 2, SignStyle.EXCEEDS_PAD, 1, 5), Array("50", 1, 2, SignStyle.EXCEEDS_PAD, 2, 50), Array("500", 1, 2, SignStyle.EXCEEDS_PAD, 3, 500), Array("-0", 1, 2, SignStyle.EXCEEDS_PAD, 2, 0), Array("-5", 1, 2, SignStyle.EXCEEDS_PAD, 2, -5), Array("-50", 1, 2, SignStyle.EXCEEDS_PAD, 3, -50), Array("-500", 1, 2, SignStyle.EXCEEDS_PAD, 4, -500), Array("-AAA", 1, 2, SignStyle.EXCEEDS_PAD, ~1, null), Array("+0", 1, 2, SignStyle.EXCEEDS_PAD, 2, 0), Array("+5", 1, 2, SignStyle.EXCEEDS_PAD, 2, 5), Array("+50", 1, 2, SignStyle.EXCEEDS_PAD, 3, 50), Array("+500", 1, 2, SignStyle.EXCEEDS_PAD, 4, 500), Array("+AAA", 1, 2, SignStyle.EXCEEDS_PAD, ~1, null))
  }

  @Test(dataProvider = "parseSignsLenient")
  @throws(classOf[Exception])
  def test_parseSignsLenient(input: String, min: Int, max: Int, style: SignStyle, parseLen: Int, parseVal: Integer): Unit = {
    parseContext.setStrict(false)
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_MONTH, min, max, style)
    val newPos: Int = pp.parse(parseContext, input, 0)
    assertEquals(newPos, parseLen)
    assertParsed(parseContext, DAY_OF_MONTH, if (parseVal != null) parseVal.toLong else null)
  }

  private def assertParsed(context: DateTimeParseContext, field: TemporalField, value: java.lang.Long): Unit = {
    if (value == null) {
      assertEquals(context.getParsed(field), null)
    }
    else {
      assertEquals(context.getParsed(field), value)
    }
  }
}
