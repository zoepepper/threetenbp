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
package org.threeten.bp.chrono

import org.threeten.bp.temporal.ChronoField.ERA
import java.util.Locale
import org.threeten.bp.DateTimeException
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.Temporal
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.TemporalQueries
import org.threeten.bp.temporal.TemporalQuery
import org.threeten.bp.temporal.UnsupportedTemporalTypeException
import org.threeten.bp.temporal.ValueRange

object IsoEra {
  /** The singleton instance for the era BCE, 'Before Current Era'.
    * The 'ISO' part of the name emphasizes that this differs from the BCE
    * era in the Gregorian calendar system.
    * This has the numeric value of {@code 0}.
    */
  val BCE = new IsoEra("BCE", 0)
  /** The singleton instance for the era CE, 'Current Era'.
    * The 'ISO' part of the name emphasizes that this differs from the CE
    * era in the Gregorian calendar system.
    * This has the numeric value of {@code 1}.
    */
  val CE  = new IsoEra("CE", 1)

  val values: Array[IsoEra] = Array(BCE, CE)

  /** Obtains an instance of {@code IsoEra} from an {@code int} value.
    *
    * {@code IsoEra} is an enum representing the ISO eras of BCE/CE.
    * This factory allows the enum to be obtained from the {@code int} value.
    *
    * @param era  the BCE/CE value to represent, from 0 (BCE) to 1 (CE)
    * @return the era singleton, not null
    * @throws DateTimeException if the value is invalid
    */
  def of(era: Int): IsoEra =
    era match {
      case 0 => BCE
      case 1 => CE
      case _ => throw new DateTimeException(s"Invalid era: $era")
    }
}

/** An era in the ISO calendar system.
  *
  * The ISO-8601 standard does not define eras.
  * A definition has therefore been created with two eras - 'Current era' (CE) for
  * years from 0001-01-01 (ISO) and 'Before current era' (BCE) for years before that.
  *
  * <b>Do not use {@code ordinal()} to obtain the numeric representation of {@code IsoEra}.
  * Use {@code getValue()} instead.</b>
  *
  * <h3>Specification for implementors</h3>
  * This is an immutable and thread-safe enum.
  */
final class IsoEra(name: String, ordinal: Int) extends Enum[IsoEra](name, ordinal) with Era {
  /** Gets the numeric era {@code int} value.
    *
    * The era BCE has the value 0, while the era CE has the value 1.
    *
    * @return the era value, from 0 (BCE) to 1 (CE)
    */
  def getValue: Int = ordinal

  override def isSupported(field: TemporalField): Boolean =
    if (field.isInstanceOf[ChronoField]) field eq ERA
    else field != null && field.isSupportedBy(this)

  override def range(field: TemporalField): ValueRange =
    if (field eq ERA) field.range
    else if (field.isInstanceOf[ChronoField]) throw new UnsupportedTemporalTypeException(s"Unsupported field: $field")
    else field.rangeRefinedBy(this)

  override def get(field: TemporalField): Int =
    if (field eq ERA) getValue
    else range(field).checkValidIntValue(getLong(field), field)

  override def getLong(field: TemporalField): Long =
    if (field eq ERA) getValue
    else if (field.isInstanceOf[ChronoField]) throw new UnsupportedTemporalTypeException(s"Unsupported field: $field")
    else field.getFrom(this)

  override def adjustInto(temporal: Temporal): Temporal = temporal.`with`(ERA, getValue)

  override def query[R >: Null](query: TemporalQuery[R]): R =
    query match {
      case TemporalQueries.precision  => ChronoUnit.ERAS.asInstanceOf[R]
      case TemporalQueries.chronology
         | TemporalQueries.zone
         | TemporalQueries.zoneId
         | TemporalQueries.offset
         | TemporalQueries.localDate
         | TemporalQueries.localTime  => null
      case _                          => query.queryFrom (this)
    }

  override def getDisplayName(style: TextStyle, locale: Locale): String =
    new DateTimeFormatterBuilder().appendText(ERA, style).toFormatter(locale).format(this)
}