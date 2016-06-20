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
import org.testng.annotations.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate

/** Test PadPrinterDecorator. */
@Test class TestPadPrinterDecorator extends AbstractTestPrinterParser {
  @throws(classOf[Exception])
  def test_print_emptyCalendrical(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.CharLiteralPrinterParser('Z'), 3, '-')
    pp.print(printEmptyContext, buf)
    assertEquals(buf.toString, "--Z")
  }

  @throws(classOf[Exception])
  def test_print_fullDateTime(): Unit = {
    printContext.setDateTime(LocalDate.of(2008, 12, 3))
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.CharLiteralPrinterParser('Z'), 3, '-')
    pp.print(printContext, buf)
    assertEquals(buf.toString, "--Z")
  }

  @throws(classOf[Exception])
  def test_print_append(): Unit = {
    buf.append("EXISTING")
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.CharLiteralPrinterParser('Z'), 3, '-')
    pp.print(printEmptyContext, buf)
    assertEquals(buf.toString, "EXISTING--Z")
  }

  @throws(classOf[Exception])
  def test_print_noPadRequiredSingle(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.CharLiteralPrinterParser('Z'), 1, '-')
    pp.print(printEmptyContext, buf)
    assertEquals(buf.toString, "Z")
  }

  @throws(classOf[Exception])
  def test_print_padRequiredSingle(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.CharLiteralPrinterParser('Z'), 5, '-')
    pp.print(printEmptyContext, buf)
    assertEquals(buf.toString, "----Z")
  }

  @throws(classOf[Exception])
  def test_print_noPadRequiredMultiple(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.StringLiteralPrinterParser("WXYZ"), 4, '-')
    pp.print(printEmptyContext, buf)
    assertEquals(buf.toString, "WXYZ")
  }

  @throws(classOf[Exception])
  def test_print_padRequiredMultiple(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.StringLiteralPrinterParser("WXYZ"), 5, '-')
    pp.print(printEmptyContext, buf)
    assertEquals(buf.toString, "-WXYZ")
  }

  @Test(expectedExceptions = Array(classOf[DateTimeException]))
  @throws(classOf[Exception])
  def test_print_overPad(): Unit = {
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(new DateTimeFormatterBuilder.StringLiteralPrinterParser("WXYZ"), 3, '-')
    pp.print(printEmptyContext, buf)
  }

  @throws(classOf[Exception])
  def test_toString1(): Unit = {
    val wrapped: DateTimeFormatterBuilder.CharLiteralPrinterParser = new DateTimeFormatterBuilder.CharLiteralPrinterParser('Y')
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(wrapped, 5, ' ')
    assertEquals(pp.toString, "Pad('Y',5)")
  }

  @throws(classOf[Exception])
  def test_toString2(): Unit = {
    val wrapped: DateTimeFormatterBuilder.CharLiteralPrinterParser = new DateTimeFormatterBuilder.CharLiteralPrinterParser('Y')
    val pp: DateTimeFormatterBuilder.PadPrinterParserDecorator = new DateTimeFormatterBuilder.PadPrinterParserDecorator(wrapped, 5, '-')
    assertEquals(pp.toString, "Pad('Y',5,'-')")
  }
}