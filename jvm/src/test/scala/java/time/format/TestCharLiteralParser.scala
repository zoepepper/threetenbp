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
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.time.temporal.TemporalQueries

/** Test CharLiteralPrinterParser. */
@Test class TestCharLiteralParser extends AbstractTestPrinterParser {
  @DataProvider(name = "success") private[format] def data_success: Array[Array[Any]] = {
    Array[Array[Any]](Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), true, "a", 0, 1), Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), true, "aOTHER", 0, 1), Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), true, "OTHERaOTHER", 5, 6), Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), true, "OTHERa", 5, 6), Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), true, "", 0, ~0), Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), true, "a", 1, ~1), Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), true, "A", 0, ~0), Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), true, "b", 0, ~0), Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), true, "OTHERbOTHER", 5, ~5), Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), true, "OTHERb", 5, ~5), Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), false, "a", 0, 1), Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), false, "A", 0, 1))
  }

  @Test(dataProvider = "success") def test_parse_success(pp: DateTimeFormatterBuilder.CharLiteralPrinterParser, caseSensitive: Boolean, text: String, pos: Int, expectedPos: Int): Unit = {
    parseContext.setCaseSensitive(caseSensitive)
    val result: Int = pp.parse(parseContext, text, pos)
    assertEquals(result, expectedPos)
    assertEquals(parseContext.toParsed.query(TemporalQueries.chronology), null)
    assertEquals(parseContext.toParsed.query(TemporalQueries.zoneId), null)
  }

  @DataProvider(name = "error") private[format] def data_error: Array[Array[Any]] = {
    Array[Array[Any]](Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), "a", -1, classOf[IndexOutOfBoundsException]), Array(new DateTimeFormatterBuilder.CharLiteralPrinterParser('a'), "a", 2, classOf[IndexOutOfBoundsException]))
  }

  @Test(dataProvider = "error") def test_parse_error(pp: DateTimeFormatterBuilder.CharLiteralPrinterParser, text: String, pos: Int, expected: Class[_]): Unit = {
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
}