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
import java.time.temporal.ChronoField.YEAR
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate
import java.time.temporal.MockFieldValue

/** Test ReducedPrinterParser. */
@Test class TestReducedPrinter extends AbstractTestPrinterParser {
  @Test(expectedExceptions = Array(classOf[DateTimeException]))
  @throws(classOf[Exception])
  def test_print_emptyCalendrical(): Unit = {
    val pp: DateTimeFormatterBuilder.ReducedPrinterParser = new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null)
    pp.print(printEmptyContext, buf)
  }

  @throws(classOf[Exception])
  def test_print_append(): Unit = {
    printContext.setDateTime(LocalDate.of(2012, 1, 1))
    val pp: DateTimeFormatterBuilder.ReducedPrinterParser = new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2010, null)
    buf.append("EXISTING")
    pp.print(printContext, buf)
    assertEquals(buf.toString, "EXISTING12")
  }

  @DataProvider(name = "Pivot") private[format] def provider_pivot: Array[Array[Any]] = {
    Array[Array[Any]](Array(1, 2010, 2010, "0"), Array(1, 2010, 2011, "1"), Array(1, 2010, 2012, "2"), Array(1, 2010, 2013, "3"), Array(1, 2010, 2014, "4"), Array(1, 2010, 2015, "5"), Array(1, 2010, 2016, "6"), Array(1, 2010, 2017, "7"), Array(1, 2010, 2018, "8"), Array(1, 2010, 2019, "9"), Array(1, 2010, 2009, "9"), Array(1, 2010, 2020, "0"), Array(2, 2010, 2010, "10"), Array(2, 2010, 2011, "11"), Array(2, 2010, 2021, "21"), Array(2, 2010, 2099, "99"), Array(2, 2010, 2100, "00"), Array(2, 2010, 2109, "09"), Array(2, 2010, 2009, "09"), Array(2, 2010, 2110, "10"), Array(2, 2005, 2005, "05"), Array(2, 2005, 2099, "99"), Array(2, 2005, 2100, "00"), Array(2, 2005, 2104, "04"), Array(2, 2005, 2004, "04"), Array(2, 2005, 2105, "05"), Array(3, 2005, 2005, "005"), Array(3, 2005, 2099, "099"), Array(3, 2005, 2100, "100"), Array(3, 2005, 2999, "999"), Array(3, 2005, 3000, "000"), Array(3, 2005, 3004, "004"), Array(3, 2005, 2004, "004"), Array(3, 2005, 3005, "005"), Array(9, 2005, 2005, "000002005"), Array(9, 2005, 2099, "000002099"), Array(9, 2005, 2100, "000002100"), Array(9, 2005, 999999999, "999999999"), Array(9, 2005, 1000000000, "000000000"), Array(9, 2005, 1000002004, "000002004"), Array(9, 2005, 2004, "000002004"), Array(9, 2005, 1000002005, "000002005"), Array(2, -2005, -2005, "05"), Array(2, -2005, -2000, "00"), Array(2, -2005, -1999, "99"), Array(2, -2005, -1904, "04"), Array(2, -2005, -2006, "06"), Array(2, -2005, -1905, "05"))
  }

  @Test(dataProvider = "Pivot")
  @throws(classOf[Exception])
  def test_pivot(width: Int, baseValue: Int, value: Int, result: String): Unit = {
    printContext.setDateTime(new MockFieldValue(YEAR, value))
    val pp: DateTimeFormatterBuilder.ReducedPrinterParser = new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, width, width, baseValue, null)
    try {
      pp.print(printContext, buf)
      if (result == null)
        fail("Expected exception")
      assertEquals(buf.toString, result)
    }
    catch {
      case ex: DateTimeException =>
        if (result == null || value < 0)
          assertEquals(ex.getMessage.contains(YEAR.toString), true)
        else
          throw ex
    }
  }

  @throws(classOf[Exception])
  def test_toString(): Unit = {
    val pp: DateTimeFormatterBuilder.ReducedPrinterParser = new DateTimeFormatterBuilder.ReducedPrinterParser(YEAR, 2, 2, 2005, null)
    assertEquals(pp.toString, "ReducedValue(Year,2,2,2005)")
  }
}