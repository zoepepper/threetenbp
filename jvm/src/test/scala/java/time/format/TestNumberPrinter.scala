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
import org.testng.Assert.fail
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.HOUR_OF_DAY
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.time.DateTimeException
import java.time.LocalDate
import java.time.temporal.MockFieldValue

/** Test SimpleNumberPrinterParser. */
@Test class TestNumberPrinter extends AbstractTestPrinterParser {
  @Test(expectedExceptions = Array(classOf[DateTimeException]))
  @throws(classOf[Exception])
  def test_print_emptyCalendrical(): Unit = {
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_MONTH, 1, 2, SignStyle.NEVER)
    pp.print(printEmptyContext, buf)
  }

  @throws(classOf[Exception])
  def test_print_append(): Unit = {
    printContext.setDateTime(LocalDate.of(2012, 1, 3))
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_MONTH, 1, 2, SignStyle.NEVER)
    buf.append("EXISTING")
    pp.print(printContext, buf)
    assertEquals(buf.toString, "EXISTING3")
  }

  @DataProvider(name = "Pad") private[format] def provider_pad: Array[Array[Any]] = {
    Array[Array[Any]](Array(1, 1, -10, null), Array(1, 1, -9, "9"), Array(1, 1, -1, "1"), Array(1, 1, 0, "0"), Array(1, 1, 3, "3"), Array(1, 1, 9, "9"), Array(1, 1, 10, null), Array(1, 2, -100, null), Array(1, 2, -99, "99"), Array(1, 2, -10, "10"), Array(1, 2, -9, "9"), Array(1, 2, -1, "1"), Array(1, 2, 0, "0"), Array(1, 2, 3, "3"), Array(1, 2, 9, "9"), Array(1, 2, 10, "10"), Array(1, 2, 99, "99"), Array(1, 2, 100, null), Array(2, 2, -100, null), Array(2, 2, -99, "99"), Array(2, 2, -10, "10"), Array(2, 2, -9, "09"), Array(2, 2, -1, "01"), Array(2, 2, 0, "00"), Array(2, 2, 3, "03"), Array(2, 2, 9, "09"), Array(2, 2, 10, "10"), Array(2, 2, 99, "99"), Array(2, 2, 100, null), Array(1, 3, -1000, null), Array(1, 3, -999, "999"), Array(1, 3, -100, "100"), Array(1, 3, -99, "99"), Array(1, 3, -10, "10"), Array(1, 3, -9, "9"), Array(1, 3, -1, "1"), Array(1, 3, 0, "0"), Array(1, 3, 3, "3"), Array(1, 3, 9, "9"), Array(1, 3, 10, "10"), Array(1, 3, 99, "99"), Array(1, 3, 100, "100"), Array(1, 3, 999, "999"), Array(1, 3, 1000, null), Array(2, 3, -1000, null), Array(2, 3, -999, "999"), Array(2, 3, -100, "100"), Array(2, 3, -99, "99"), Array(2, 3, -10, "10"), Array(2, 3, -9, "09"), Array(2, 3, -1, "01"), Array(2, 3, 0, "00"), Array(2, 3, 3, "03"), Array(2, 3, 9, "09"), Array(2, 3, 10, "10"), Array(2, 3, 99, "99"), Array(2, 3, 100, "100"), Array(2, 3, 999, "999"), Array(2, 3, 1000, null), Array(3, 3, -1000, null), Array(3, 3, -999, "999"), Array(3, 3, -100, "100"), Array(3, 3, -99, "099"), Array(3, 3, -10, "010"), Array(3, 3, -9, "009"), Array(3, 3, -1, "001"), Array(3, 3, 0, "000"), Array(3, 3, 3, "003"), Array(3, 3, 9, "009"), Array(3, 3, 10, "010"), Array(3, 3, 99, "099"), Array(3, 3, 100, "100"), Array(3, 3, 999, "999"), Array(3, 3, 1000, null), Array(1, 10, Integer.MAX_VALUE - 1, "2147483646"), Array(1, 10, Integer.MAX_VALUE, "2147483647"), Array(1, 10, Integer.MIN_VALUE + 1, "2147483647"), Array(1, 10, Integer.MIN_VALUE, "2147483648"))
  }

  @Test(dataProvider = "Pad")
  @throws(classOf[Exception])
  def test_pad_NOT_NEGATIVE(minPad: Int, maxPad: Int, value: Long, result: String): Unit = {
    printContext.setDateTime(new MockFieldValue(DAY_OF_MONTH, value))
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_MONTH, minPad, maxPad, SignStyle.NOT_NEGATIVE)
    try {
      pp.print(printContext, buf)
      if (result == null || value < 0)
        fail("Expected exception")
      assertEquals(buf.toString, result)
    }
    catch {
      case ex: DateTimeException =>
        if (result == null || value < 0)
          assertEquals(ex.getMessage.contains(DAY_OF_MONTH.toString), true)
        else
          throw ex
    }
  }

  @Test(dataProvider = "Pad")
  @throws(classOf[Exception])
  def test_pad_NEVER(minPad: Int, maxPad: Int, value: Long, result: String): Unit = {
    printContext.setDateTime(new MockFieldValue(DAY_OF_MONTH, value))
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_MONTH, minPad, maxPad, SignStyle.NEVER)
    try {
      pp.print(printContext, buf)
      if (result == null)
        fail("Expected exception")
      assertEquals(buf.toString, result)
    }
    catch {
      case ex: DateTimeException =>
        if (result != null)
          throw ex
        assertEquals(ex.getMessage.contains(DAY_OF_MONTH.toString), true)
    }
  }

  @Test(dataProvider = "Pad")
  @throws(classOf[Exception])
  def test_pad_NORMAL(minPad: Int, maxPad: Int, value: Long, result: String): Unit = {
    printContext.setDateTime(new MockFieldValue(DAY_OF_MONTH, value))
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_MONTH, minPad, maxPad, SignStyle.NORMAL)
    try {
      pp.print(printContext, buf)
      if (result == null)
        fail("Expected exception")
      assertEquals(buf.toString, if (value < 0) "-" + result else result)
    }
    catch {
      case ex: DateTimeException =>
        if (result != null)
          throw ex
        assertEquals(ex.getMessage.contains(DAY_OF_MONTH.toString), true)
    }
  }

  @Test(dataProvider = "Pad")
  @throws(classOf[Exception])
  def test_pad_ALWAYS(minPad: Int, maxPad: Int, value: Long, result: String): Unit = {
    printContext.setDateTime(new MockFieldValue(DAY_OF_MONTH, value))
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_MONTH, minPad, maxPad, SignStyle.ALWAYS)
    try {
      pp.print(printContext, buf)
      if (result == null)
        fail("Expected exception")
      assertEquals(buf.toString, if (value < 0) "-" + result else "+" + result)
    }
    catch {
      case ex: DateTimeException =>
        if (result != null)
          throw ex
        assertEquals(ex.getMessage.contains(DAY_OF_MONTH.toString), true)
    }
  }

  @Test(dataProvider = "Pad")
  @throws(classOf[Exception])
  def test_pad_EXCEEDS_PAD(minPad: Int, maxPad: Int, value: Long, result: String): Unit = {
    var _result = result
    printContext.setDateTime(new MockFieldValue(DAY_OF_MONTH, value))
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(DAY_OF_MONTH, minPad, maxPad, SignStyle.EXCEEDS_PAD)
    try {
      pp.print(printContext, buf)
      if (_result == null) {
        fail("Expected exception")
        return
      }
      if (_result.length > minPad || value < 0) {
        _result = if (value < 0) "-" + _result else "+" + _result
      }
      assertEquals(buf.toString, _result)
    }
    catch {
      case ex: DateTimeException =>
        if (_result != null)
          throw ex
        assertEquals(ex.getMessage.contains(DAY_OF_MONTH.toString), true)
    }
  }

  @throws(classOf[Exception])
  def test_toString1(): Unit = {
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(HOUR_OF_DAY, 1, 19, SignStyle.NORMAL)
    assertEquals(pp.toString, "Value(HourOfDay)")
  }

  @throws(classOf[Exception])
  def test_toString2(): Unit = {
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(HOUR_OF_DAY, 2, 2, SignStyle.NOT_NEGATIVE)
    assertEquals(pp.toString, "Value(HourOfDay,2)")
  }

  @throws(classOf[Exception])
  def test_toString3(): Unit = {
    val pp: DateTimeFormatterBuilder.NumberPrinterParser = new DateTimeFormatterBuilder.NumberPrinterParser(HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
    assertEquals(pp.toString, "Value(HourOfDay,1,2,NOT_NEGATIVE)")
  }
}