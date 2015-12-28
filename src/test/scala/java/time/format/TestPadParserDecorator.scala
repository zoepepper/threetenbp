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
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import org.testng.annotations.Test
import java.time.temporal.TemporalField

/**
  * Test PadPrinterParserDecorator.
  */
@Test class TestPadParserDecorator extends AbstractTestPrinterParser {
  @Test(expectedExceptions = Array(classOf[IndexOutOfBoundsException]))
  @throws(classOf[Exception])
  def test_parse_negativePosition(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.CharLiteralPrinterParser('Z'), 3, '-')
    pp.parse(parseContext, "--Z", -1)
  }

  @Test(expectedExceptions = Array(classOf[IndexOutOfBoundsException]))
  @throws(classOf[Exception])
  def test_parse_offEndPosition(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.CharLiteralPrinterParser('Z'), 3, '-')
    pp.parse(parseContext, "--Z", 4)
  }

  @throws(classOf[Exception])
  def test_parse(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.NumberPrinterParser(MONTH_OF_YEAR, 1, 3, SignStyle.NEVER), 3, '-')
    val result: Int = pp.parse(parseContext, "--2", 0)
    assertEquals(result, 3)
    assertParsed(MONTH_OF_YEAR, 2L)
  }

  @throws(classOf[Exception])
  def test_parse_noReadBeyond(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.NumberPrinterParser(MONTH_OF_YEAR, 1, 3, SignStyle.NEVER), 3, '-')
    val result: Int = pp.parse(parseContext, "--22", 0)
    assertEquals(result, 3)
    assertParsed(MONTH_OF_YEAR, 2L)
  }

  @throws(classOf[Exception])
  def test_parse_textLessThanPadWidth(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.NumberPrinterParser(MONTH_OF_YEAR, 1, 3, SignStyle.NEVER), 3, '-')
    val result: Int = pp.parse(parseContext, "-1", 0)
    assertEquals(result, ~0)
  }

  @throws(classOf[Exception])
  def test_parse_decoratedErrorPassedBack(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.NumberPrinterParser(MONTH_OF_YEAR, 1, 3, SignStyle.NEVER), 3, '-')
    val result: Int = pp.parse(parseContext, "--A", 0)
    assertEquals(result, ~2)
  }

  @throws(classOf[Exception])
  def test_parse_decoratedDidNotParseToPadWidth(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.NumberPrinterParser(MONTH_OF_YEAR, 1, 3, SignStyle.NEVER), 3, '-')
    val result: Int = pp.parse(parseContext, "-1X", 0)
    assertEquals(result, ~1)
  }

  private def assertParsed(field: TemporalField, value: java.lang.Long): Unit = {
    if (value == null) {
      assertEquals(parseContext.getParsed(field), null)
    }
    else {
      assertEquals(parseContext.getParsed(field), value)
    }
  }
}