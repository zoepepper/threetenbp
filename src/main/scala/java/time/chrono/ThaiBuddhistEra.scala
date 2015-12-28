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
package java.time.chrono

import java.time.temporal.ChronoField.ERA
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.util.Locale
import java.time.DateTimeException
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalField
import java.time.temporal.TemporalQueries
import java.time.temporal.TemporalQuery
import java.time.temporal.UnsupportedTemporalTypeException
import java.time.temporal.ValueRange

/**
  * An era in the Thai Buddhist calendar system.
  * <p>
  * The Thai Buddhist calendar system has two eras.
  * <p>
  * <b>Do not use ordinal() to obtain the numeric representation of a ThaiBuddhistEra
  * instance. Use getValue() instead.</b>
  *
  * <h3>Specification for implementors</h3>
  * This is an immutable and thread-safe enum.
  */
object ThaiBuddhistEra {
  /**
    * The singleton instance for the era before the current one, 'Before Buddhist Era',
    * which has the value 0.
    */
  val BEFORE_BE = new ThaiBuddhistEra("BEFORE_BE", 0)
  /**
    * The singleton instance for the current era, 'Buddhist Era', which has the value 1.
    */
  val BE        = new ThaiBuddhistEra("BE", 1)

  val values: Array[ThaiBuddhistEra] = Array(BEFORE_BE, BE)

  /**
    * Obtains an instance of {@code ThaiBuddhistEra} from a value.
    * <p>
    * The current era (from ISO year -543 onwards) has the value 1
    * The previous era has the value 0.
    *
    * @param thaiBuddhistEra  the era to represent, from 0 to 1
    * @return the BuddhistEra singleton, never null
    * @throws DateTimeException if the era is invalid
    */
  def of(thaiBuddhistEra: Int): ThaiBuddhistEra =
    thaiBuddhistEra match {
      case 0 => BEFORE_BE
      case 1 => BE
      case _ => throw new DateTimeException("Era is not valid for ThaiBuddhistEra")
  }

  @throws[IOException]
  private[chrono] def readExternal(in: DataInput): ThaiBuddhistEra = {
    val eraValue: Byte = in.readByte
    ThaiBuddhistEra.of(eraValue)
  }
}

final class ThaiBuddhistEra(name: String, ordinal: Int) extends Enum[ThaiBuddhistEra](name, ordinal) with Era {
  /**
    * Gets the era numeric value.
    * <p>
    * The current era (from ISO year -543 onwards) has the value 1
    * The previous era has the value 0.
    *
    * @return the era value, from 0 (BEFORE_BE) to 1 (BE)
    */
  def getValue: Int = ordinal

  override def isSupported(field: TemporalField): Boolean =
    if (field.isInstanceOf[ChronoField])
      field eq ERA
    else
      field != null && field.isSupportedBy(this)

  override def range(field: TemporalField): ValueRange =
    if (field eq ERA)
      field.range
    else if (field.isInstanceOf[ChronoField])
      throw new UnsupportedTemporalTypeException("Unsupported field: " + field)
    else
      field.rangeRefinedBy(this)

  override def get(field: TemporalField): Int =
    if (field eq ERA)
      getValue
    else
      range(field).checkValidIntValue(getLong(field), field)

  override def getLong(field: TemporalField): Long =
    if (field eq ERA)
      getValue
    else if (field.isInstanceOf[ChronoField])
      throw new UnsupportedTemporalTypeException("Unsupported field: " + field)
    else
      field.getFrom(this)

  override def adjustInto(temporal: Temporal): Temporal = temporal.`with`(ERA, getValue)

  override def query[R >: Null](query: TemporalQuery[R]): R =
    if (query eq TemporalQueries.precision)
      ChronoUnit.ERAS.asInstanceOf[R]
    else if ((query eq TemporalQueries.chronology) || (query eq TemporalQueries.zone) || (query eq TemporalQueries.zoneId) || (query eq TemporalQueries.offset) || (query eq TemporalQueries.localDate) || (query eq TemporalQueries.localTime))
      null
    else
      query.queryFrom(this)

  override def getDisplayName(style: TextStyle, locale: Locale): String =
    new DateTimeFormatterBuilder().appendText(ERA, style).toFormatter(locale).format(this)

  private def writeReplace: AnyRef = new Ser(Ser.THAIBUDDHIST_ERA_TYPE, this)

  @throws[IOException]
  private[chrono] def writeExternal(out: DataOutput): Unit = out.writeByte(this.getValue)
}