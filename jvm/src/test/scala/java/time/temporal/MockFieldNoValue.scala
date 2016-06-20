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
package java.time.temporal

import java.time.temporal.ChronoUnit.MONTHS
import java.time.temporal.ChronoUnit.WEEKS
import java.util.Locale
import java.time.DateTimeException
import java.time.format.ResolverStyle

/** Mock DateTimeField that returns null. */
object MockFieldNoValue {
  val INSTANCE = new MockFieldNoValue("INSTANCE", 0)
}

final class MockFieldNoValue(name: String, ordinal: Int) extends Enum[MockFieldNoValue](name, ordinal) with TemporalField {
  override def toString: String = null
  def getBaseUnit: TemporalUnit = WEEKS
  def getRangeUnit: TemporalUnit = MONTHS
  def range: ValueRange = ValueRange.of(1, 20)
  def isDateBased: Boolean = true
  def isTimeBased: Boolean = false
  def isSupportedBy(dateTime: TemporalAccessor): Boolean = true
  def rangeRefinedBy(dateTime: TemporalAccessor): ValueRange = ValueRange.of(1, 20)
  def getFrom(dateTime: TemporalAccessor): Long = throw new DateTimeException("Mock")
  def adjustInto[R <: Temporal](dateTime: R, newValue: Long): R = throw new DateTimeException("Mock")
  def getDisplayName(locale: Locale): String = "Mock"
  def resolve(fieldValues: java.util.Map[TemporalField, java.lang.Long], partialTemporal: TemporalAccessor, resolverStyle: ResolverStyle): TemporalAccessor =
    null
}