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
import java.lang.reflect.Modifier
import java.util.Collections
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

/**
  * Test.
  */
@Test class TestDateTimes_implementation {
  @SuppressWarnings(Array("rawtypes"))
  @throws(classOf[Exception])
  def test_constructor(): Unit = {
    for (constructor <- classOf[Math].getDeclaredConstructors) {
      assertTrue(Modifier.isPrivate(constructor.getModifiers))
      constructor.setAccessible(true)
      constructor.newInstance(Collections.nCopies(constructor.getParameterTypes.length, null).toArray)
    }
  }

  @DataProvider(name = "safeAddIntProvider") private[time] def safeAddIntProvider: Array[Array[Int]] = {
    Array[Array[Int]](Array(Integer.MIN_VALUE, 1, Integer.MIN_VALUE + 1), Array(-1, 1, 0), Array(0, 0, 0), Array(1, -1, 0), Array(Integer.MAX_VALUE, -1, Integer.MAX_VALUE - 1))
  }

  @Test(dataProvider = "safeAddIntProvider") def test_safeAddInt(a: Int, b: Int, expected: Int): Unit = {
    assertEquals(Math.addExact(a, b), expected)
  }

  @DataProvider(name = "safeAddIntProviderOverflow") private[time] def safeAddIntProviderOverflow: Array[Array[Int]] = {
    Array[Array[Int]](Array(Integer.MIN_VALUE, -1), Array(Integer.MIN_VALUE + 1, -2), Array(Integer.MAX_VALUE - 1, 2), Array(Integer.MAX_VALUE, 1))
  }

  @Test(dataProvider = "safeAddIntProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeAddInt_overflow(a: Int, b: Int): Unit = {
    Math.addExact(a, b)
  }

  @DataProvider(name = "safeAddLongProvider") private[time] def safeAddLongProvider: Array[Array[Long]] = {
    Array[Array[Long]](Array(Long.MinValue, 1, Long.MinValue + 1), Array(-1, 1, 0), Array(0, 0, 0), Array(1, -1, 0), Array(Long.MaxValue, -1, Long.MaxValue - 1))
  }

  @Test(dataProvider = "safeAddLongProvider") def test_safeAddLong(a: Long, b: Long, expected: Long): Unit = {
    assertEquals(Math.addExact(a, b), expected)
  }

  @DataProvider(name = "safeAddLongProviderOverflow") private[time] def safeAddLongProviderOverflow: Array[Array[Long]] = {
    Array[Array[Long]](Array(Long.MinValue, -1), Array(Long.MinValue + 1, -2), Array(Long.MaxValue - 1, 2), Array(Long.MaxValue, 1))
  }

  @Test(dataProvider = "safeAddLongProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeAddLong_overflow(a: Long, b: Long): Unit = {
    Math.addExact(a, b)
  }

  @DataProvider(name = "safeSubtractIntProvider") private[time] def safeSubtractIntProvider: Array[Array[Int]] = {
    Array[Array[Int]](Array(Integer.MIN_VALUE, -1, Integer.MIN_VALUE + 1), Array(-1, -1, 0), Array(0, 0, 0), Array(1, 1, 0), Array(Integer.MAX_VALUE, 1, Integer.MAX_VALUE - 1))
  }

  @Test(dataProvider = "safeSubtractIntProvider") def test_safeSubtractInt(a: Int, b: Int, expected: Int): Unit = {
    assertEquals(Math.subtractExact(a, b), expected)
  }

  @DataProvider(name = "safeSubtractIntProviderOverflow") private[time] def safeSubtractIntProviderOverflow: Array[Array[Int]] = {
    Array[Array[Int]](Array(Integer.MIN_VALUE, 1), Array(Integer.MIN_VALUE + 1, 2), Array(Integer.MAX_VALUE - 1, -2), Array(Integer.MAX_VALUE, -1))
  }

  @Test(dataProvider = "safeSubtractIntProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeSubtractInt_overflow(a: Int, b: Int): Unit = {
    Math.subtractExact(a, b)
  }

  @DataProvider(name = "safeSubtractLongProvider") private[time] def safeSubtractLongProvider: Array[Array[Long]] = {
    Array[Array[Long]](Array(Long.MinValue, -1, Long.MinValue + 1), Array(-1, -1, 0), Array(0, 0, 0), Array(1, 1, 0), Array(Long.MaxValue, 1, Long.MaxValue - 1))
  }

  @Test(dataProvider = "safeSubtractLongProvider") def test_safeSubtractLong(a: Long, b: Long, expected: Long): Unit = {
    assertEquals(Math.subtractExact(a, b), expected)
  }

  @DataProvider(name = "safeSubtractLongProviderOverflow") private[time] def safeSubtractLongProviderOverflow: Array[Array[Long]] = {
    Array[Array[Long]](Array(Long.MinValue, 1), Array(Long.MinValue + 1, 2), Array(Long.MaxValue - 1, -2), Array(Long.MaxValue, -1))
  }

  @Test(dataProvider = "safeSubtractLongProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeSubtractLong_overflow(a: Long, b: Long): Unit = {
    Math.subtractExact(a, b)
  }

  @DataProvider(name = "safeMultiplyIntProvider") private[time] def safeMultiplyIntProvider: Array[Array[Int]] = {
    Array[Array[Int]](Array(Integer.MIN_VALUE, 1, Integer.MIN_VALUE), Array(Integer.MIN_VALUE / 2, 2, Integer.MIN_VALUE), Array(-1, -1, 1), Array(-1, 1, -1), Array(0, -1, 0), Array(0, 0, 0), Array(0, 1, 0), Array(1, -1, -1), Array(1, 1, 1), Array(Integer.MAX_VALUE / 2, 2, Integer.MAX_VALUE - 1), Array(Integer.MAX_VALUE, -1, Integer.MIN_VALUE + 1))
  }

  @Test(dataProvider = "safeMultiplyIntProvider") def test_safeMultiplyInt(a: Int, b: Int, expected: Int): Unit = {
    assertEquals(Math.multiplyExact(a, b), expected)
  }

  @DataProvider(name = "safeMultiplyIntProviderOverflow") private[time] def safeMultiplyIntProviderOverflow: Array[Array[Int]] = {
    Array[Array[Int]](Array(Integer.MIN_VALUE, 2), Array(Integer.MIN_VALUE / 2 - 1, 2), Array(Integer.MAX_VALUE, 2), Array(Integer.MAX_VALUE / 2 + 1, 2), Array(Integer.MIN_VALUE, -1), Array(-1, Integer.MIN_VALUE))
  }

  @Test(dataProvider = "safeMultiplyIntProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeMultiplyInt_overflow(a: Int, b: Int): Unit = {
    Math.multiplyExact(a, b)
  }

  @DataProvider(name = "safeMultiplyLongProvider") private[time] def safeMultiplyLongProvider: Array[Array[Long]] = {
    Array[Array[Long]](Array(Long.MinValue, 1, Long.MinValue), Array(Long.MinValue / 2, 2, Long.MinValue), Array(-1, -1, 1), Array(-1, 1, -1), Array(0, -1, 0), Array(0, 0, 0), Array(0, 1, 0), Array(1, -1, -1), Array(1, 1, 1), Array(Long.MaxValue / 2, 2, Long.MaxValue - 1), Array(Long.MaxValue, -1, Long.MinValue + 1), Array(-1, Integer.MIN_VALUE, -Integer.MIN_VALUE.toLong))
  }

  @Test(dataProvider = "safeMultiplyLongProvider") def test_safeMultiplyLong(a: Long, b: Int, expected: Long): Unit = {
    assertEquals(Math.multiplyExact(a, b), expected)
  }

  @DataProvider(name = "safeMultiplyLongProviderOverflow") private[time] def safeMultiplyLongProviderOverflow: Array[Array[Long]] = {
    Array[Array[Long]](Array(Long.MinValue, 2), Array(Long.MinValue / 2 - 1, 2), Array(Long.MaxValue, 2), Array(Long.MaxValue / 2 + 1, 2), Array(Long.MinValue, -1))
  }

  @Test(dataProvider = "safeMultiplyLongProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeMultiplyLong_overflow(a: Long, b: Int): Unit = {
    Math.multiplyExact(a, b)
  }

  @DataProvider(name = "safeMultiplyLongLongProvider") private[time] def safeMultiplyLongLongProvider: Array[Array[Long]] = {
    Array[Array[Long]](Array(Long.MinValue, 1, Long.MinValue), Array(Long.MinValue / 2, 2, Long.MinValue), Array(-1, -1, 1), Array(-1, 1, -1), Array(0, -1, 0), Array(0, 0, 0), Array(0, 1, 0), Array(1, -1, -1), Array(1, 1, 1), Array(Long.MaxValue / 2, 2, Long.MaxValue - 1), Array(Long.MaxValue, -1, Long.MinValue + 1))
  }

  @Test(dataProvider = "safeMultiplyLongLongProvider") def test_safeMultiplyLongLong(a: Long, b: Long, expected: Long): Unit = {
    assertEquals(Math.multiplyExact(a, b), expected)
  }

  @DataProvider(name = "safeMultiplyLongLongProviderOverflow") private[time] def safeMultiplyLongLongProviderOverflow: Array[Array[Long]] = {
    Array[Array[Long]](Array(Long.MinValue, 2), Array(Long.MinValue / 2 - 1, 2), Array(Long.MaxValue, 2), Array(Long.MaxValue / 2 + 1, 2), Array(Long.MinValue, -1), Array(-1, Long.MinValue))
  }

  @Test(dataProvider = "safeMultiplyLongLongProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeMultiplyLongLong_overflow(a: Long, b: Long): Unit = {
    Math.multiplyExact(a, b)
  }

  @DataProvider(name = "safeToIntProvider") private[time] def safeToIntProvider: Array[Array[Int]] = {
    Array[Array[Int]](Array(Integer.MIN_VALUE), Array(Integer.MIN_VALUE + 1), Array(-1), Array(0), Array(1), Array(Integer.MAX_VALUE - 1), Array(Integer.MAX_VALUE))
  }

  @Test(dataProvider = "safeToIntProvider") def test_safeToInt(l: Long): Unit = {
    assertEquals(Math.toIntExact(l), l)
  }

  @DataProvider(name = "safeToIntProviderOverflow") private[time] def safeToIntProviderOverflow: Array[Array[Long]] = {
    Array[Array[Long]](Array(Long.MinValue), Array(Integer.MIN_VALUE - 1L), Array(Integer.MAX_VALUE + 1L), Array(Long.MaxValue))
  }

  @Test(dataProvider = "safeToIntProviderOverflow", expectedExceptions = Array(classOf[ArithmeticException])) def test_safeToInt_overflow(l: Long): Unit = {
    Math.toIntExact(l)
  }

  def test_safeCompare_int(): Unit = {
    doTest_safeCompare_int(Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, -2, -1, 0, 1, 2, Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE)
  }

  private def doTest_safeCompare_int(values: Int*): Unit = {
    {
      var i: Int = 0
      while (i < values.length) {
        {
          val a: Int = values(i)

          {
            var j: Int = 0
            while (j < values.length) {
              {
                val b: Int = values(j)
                assertEquals(Integer.compare(a, b), if (a < b) -1 else if (a > b) 1 else 0, a + " <=> " + b)
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

  def test_safeCompare_long(): Unit = {
    doTest_safeCompare_long(Long.MinValue, Long.MinValue + 1, Long.MinValue + 2, Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, -2, -1, 0, 1, 2, Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE, Long.MaxValue - 2, Long.MaxValue - 1, Long.MaxValue)
  }

  private def doTest_safeCompare_long(values: Long*): Unit = {
    {
      var i: Int = 0
      while (i < values.length) {
        {
          val a: Long = values(i)

          {
            var j: Int = 0
            while (j < values.length) {
              {
                val b: Long = values(j)
                assertEquals(java.lang.Long.compare(a, b), if (a < b) -1 else if (a > b) 1 else 0, a + " <=> " + b)
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

  @DataProvider(name = "FloorDiv") private[time] def data_floorDiv: Array[Array[Long]] = {
    Array[Array[Long]](Array(5L, 4, 1L), Array(4L, 4, 1L), Array(3L, 4, 0L), Array(2L, 4, 0L), Array(1L, 4, 0L), Array(0L, 4, 0L), Array(-1L, 4, -1L), Array(-2L, 4, -1L), Array(-3L, 4, -1L), Array(-4L, 4, -1L), Array(-5L, 4, -2L))
  }

  @Test(dataProvider = "FloorDiv") def test_floorDiv_long(a: Long, b: Int, expected: Long): Unit = {
    assertEquals(Math.floorDiv(a, b), expected)
  }

  @Test(dataProvider = "FloorDiv") def test_floorDiv_int(a: Long, b: Int, expected: Long): Unit = {
    if (a <= Integer.MAX_VALUE && a >= Integer.MIN_VALUE) {
      assertEquals(Math.floorDiv(a.toInt, b), expected.toInt)
    }
  }

  @DataProvider(name = "FloorMod") private[time] def data_floorMod: Array[Array[Long]] = {
    Array[Array[Long]](Array(5L, 4, 1), Array(4L, 4, 0), Array(3L, 4, 3), Array(2L, 4, 2), Array(1L, 4, 1), Array(0L, 4, 0), Array(-1L, 4, 3), Array(-2L, 4, 2), Array(-3L, 4, 1), Array(-4L, 4, 0), Array(-5L, 4, 3))
  }

  @Test(dataProvider = "FloorMod") def test_floorMod_long(a: Long, b: Long, expected: Int): Unit = {
    assertEquals(Math.floorMod(a, b), expected)
  }

  @Test(dataProvider = "FloorMod") def test_floorMod_long(a: Long, b: Int, expected: Int): Unit = {
    assertEquals(Math.floorMod(a, b), expected)
  }

  @Test(dataProvider = "FloorMod") def test_floorMod_int(a: Long, b: Int, expected: Int): Unit = {
    if (a <= Integer.MAX_VALUE && a >= Integer.MIN_VALUE) {
      assertEquals(Math.floorMod(a.toInt, b), expected)
    }
  }
}