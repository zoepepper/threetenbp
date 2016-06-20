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
import java.time.temporal.ChronoField.DAY_OF_YEAR
import java.time.temporal.ChronoField.YEAR
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.time.temporal.TemporalField
import java.time.temporal.TemporalQueries

/** Test ReducedPrinterParser. */
@Test class TestReducedParser extends AbstractTestPrinterParser {
  @DataProvider(name = "error") private[format] def data_error: Array[Array[Any]] = {
    Array[Array[Any]](Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "12", -1, classOf[IndexOutOfBoundsException]), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "12", 3, classOf[IndexOutOfBoundsException]))
  }

  @Test(dataProvider = "error") def test_parse_error(pp: DateTimeFormatterBuilder.ReducedPrinterParser, text: String, pos: Int, expected: Class[_]): Unit = {
    try pp.parse(parseContext, text, pos)
    catch {
      case ex: RuntimeException =>
        assertTrue(expected.isInstance(ex))
        assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
        assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
    }
  }

  @throws(classOf[Exception])
  def test_parse_fieldRangeIgnored(): Unit = {
    val pp: DateTimeFormatterBuilder.ReducedPrinterParser = new DateTimeFormatterBuilder.ReducedPrinterParser(DAY_OF_YEAR, 3, 3, 10, null)
    val newPos: Int = pp.parse(parseContext, "456", 0)
    assertEquals(newPos, 3)
    assertParsed(DAY_OF_YEAR, 456L)
  }

  @DataProvider(name = "Parse") private[format] def provider_parse: Array[Array[Any]] = {
    Array[Array[Any]](Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2010, null), "-0", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "Xxx12Xxx", 3, 5, 2012), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "12345", 0, 2, 2012), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "12-45", 0, 2, 2012), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "0", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "1", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "1", 1, ~1, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "1-2", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "9", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "A0", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "0A", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "  1", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "-1", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "-10", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2010, null), "0", 0, 1, 2010), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2010, null), "9", 0, 1, 2019), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2010, null), "10", 0, 1, 2011), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2005, null), "0", 0, 1, 2010), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2005, null), "4", 0, 1, 2014), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2005, null), "5", 0, 1, 2005), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2005, null), "9", 0, 1, 2009), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2005, null), "10", 0, 1, 2011), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "00", 0, 2, 2100), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "09", 0, 2, 2109), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "10", 0, 2, 2010), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "99", 0, 2, 2099), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "100", 0, 2, 2010), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, -2005, null), "05", 0, 2, -2005), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, -2005, null), "00", 0, 2, -2000), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, -2005, null), "99", 0, 2, -1999), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, -2005, null), "06", 0, 2, -1906), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, -2005, null), "100", 0, 2, -1910))
  }

  @Test(dataProvider = "Parse") def test_parse(pp: DateTimeFormatterBuilder.ReducedPrinterParser, input: String, pos: Int, parseLen: Int, parseVal: Integer): Unit = {
    val newPos: Int = pp.parse(parseContext, input, pos)
    assertEquals(newPos, parseLen)
    assertParsed(YEAR, if (parseVal != null) parseVal.toLong else null)
  }

  @DataProvider(name = "ParseLenient") private[format] def provider_parseLenient: Array[Array[Any]] = {
    Array[Array[Any]](Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2010, null), "-0", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "Xxx12Xxx", 3, 5, 2012), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "12345", 0, 5, 12345), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "12-45", 0, 2, 2012), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "0", 0, 1, 0), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "1", 0, 1, 1), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "1", 1, ~1, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "1-2", 0, 1, 1), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "9", 0, 1, 9), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "A0", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "0A", 0, 1, 0), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "  1", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "-1", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "-10", 0, ~0, null), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2010, null), "0", 0, 1, 2010), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2010, null), "9", 0, 1, 2019), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2010, null), "10", 0, 2, 10), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2005, null), "0", 0, 1, 2010), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2005, null), "4", 0, 1, 2014), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2005, null), "5", 0, 1, 2005), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2005, null), "9", 0, 1, 2009), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 1, 1, 2005, null), "10", 0, 2, 10), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "00", 0, 2, 2100), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "09", 0, 2, 2109), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "10", 0, 2, 2010), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "99", 0, 2, 2099), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null), "100", 0, 3, 100), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, -2005, null), "05", 0, 2, -2005), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, -2005, null), "00", 0, 2, -2000), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, -2005, null), "99", 0, 2, -1999), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, -2005, null), "06", 0, 2, -1906), Array(new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, -2005, null), "100", 0, 3, 100))
  }

  @Test(dataProvider = "ParseLenient") def test_parseLenient(pp: DateTimeFormatterBuilder.ReducedPrinterParser, input: String, pos: Int, parseLen: Int, parseVal: Integer): Unit = {
    parseContext.setStrict(false)
    val newPos: Int = pp.parse(parseContext, input, pos)
    assertEquals(newPos, parseLen)
    assertParsed(YEAR, if (parseVal != null) parseVal.toLong else null)
  }

  private def assertParsed(field: TemporalField, value: java.lang.Long): Unit = {
    if (value == null)
      assertEquals(parseContext.getParsed(field), null)
    else
      assertEquals(parseContext.getParsed(field), value)
  }
}