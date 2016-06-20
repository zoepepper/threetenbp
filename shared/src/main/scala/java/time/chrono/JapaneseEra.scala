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

import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.io.InvalidObjectException
import java.io.ObjectStreamException
import java.io.Serializable
import java.util.{Objects, Arrays}
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.TemporalField
import org.threeten.bp.temporal.ValueRange
import sun.util.calendar.CalendarDate
import sun.util.calendar.CalendarSystem
import sun.util.calendar.LocalGregorianCalendar

import scala.annotation.meta.field

@SerialVersionUID(1466499369062886794L)
object JapaneseEra {
  private[chrono] val ERA_OFFSET: Int = 2
  private[chrono] val ERA_CONFIG: Array[sun.util.calendar.Era] = CalendarSystem.forName("japanese").asInstanceOf[LocalGregorianCalendar].getEras
  /** The singleton instance for the 'Meiji' era (1868-09-08 - 1912-07-29)
    * which has the value -1.
    */
  val MEIJI: JapaneseEra = new JapaneseEra(-1, LocalDate.of(1868, 9, 8))
  /** The singleton instance for the 'Taisho' era (1912-07-30 - 1926-12-24)
    * which has the value 0.
    */
  val TAISHO: JapaneseEra = new JapaneseEra(0, LocalDate.of(1912, 7, 30))
  /** The singleton instance for the 'Showa' era (1926-12-25 - 1989-01-07)
    * which has the value 1.
    */
  val SHOWA: JapaneseEra = new JapaneseEra(1, LocalDate.of(1926, 12, 25))
  /** The singleton instance for the 'Heisei' era (1989-01-08 - current)
    * which has the value 2.
    */
  val HEISEI: JapaneseEra = new JapaneseEra(2, LocalDate.of(1989, 1, 8))
  private val N_ERA_CONSTANTS: Int = HEISEI.getValue + ERA_OFFSET + 1
  private val KNOWN_ERAS: Array[JapaneseEra] = Array(MEIJI, TAISHO, SHOWA, HEISEI)

  /** Obtains an instance of {@code JapaneseEra} from an {@code int} value.
    *
    * The {@link #SHOWA} era that contains 1970-01-01 (ISO calendar system) has the value 1
    * Later era is numbered 2 ({@link #HEISEI}). Earlier eras are numbered 0 ({@link #TAISHO}),
    * -1 ({@link #MEIJI}), only Meiji and later eras are supported.
    *
    * @param japaneseEra  the era to represent
    * @return the { @code JapaneseEra} singleton, not null
    * @throws DateTimeException if the value is invalid
    */
  def of(japaneseEra: Int): JapaneseEra =
    if (japaneseEra < MEIJI.eraValue || japaneseEra + ERA_OFFSET - 1 >= KNOWN_ERAS.length)
      throw new DateTimeException("japaneseEra is invalid")
    else
      KNOWN_ERAS(ordinal(japaneseEra))

  /** Returns the {@code JapaneseEra} with the name.
    *
    * The string must match exactly the name of the era.
    * (Extraneous whitespace characters are not permitted.)
    *
    * @param japaneseEra  the japaneseEra name; non-null
    * @return the { @code JapaneseEra} singleton, never null
    * @throws IllegalArgumentException if there is not JapaneseEra with the specified name
    */
  def valueOf(japaneseEra: String): JapaneseEra = {
    Objects.requireNonNull(japaneseEra, "japaneseEra")
    for (era <- KNOWN_ERAS) {
      if (japaneseEra == era.getName)
        return era
    }
    throw new IllegalArgumentException(s"Era not found: $japaneseEra")
  }

  /** Returns an array of JapaneseEras.
    *
    * This method may be used to iterate over the JapaneseEras as follows:
    * <pre>
    * for (JapaneseEra c : JapaneseEra.values())
    * System.out.println(c);
    * </pre>
    *
    * @return an array of JapaneseEras
    */
  def values: Array[JapaneseEra] = Arrays.copyOf(KNOWN_ERAS, KNOWN_ERAS.length)

  /** Obtains an instance of {@code JapaneseEra} from a date.
    *
    * @param date  the date, not null
    * @return the Era singleton, never null
    */
  private[chrono] def from(date: LocalDate): JapaneseEra = {
    if (date.isBefore(MEIJI.since))
      throw new DateTimeException(s"Date too early: $date")
    var i: Int = KNOWN_ERAS.length - 1
    while (i >= 0) {
      val era: JapaneseEra = KNOWN_ERAS(i)
      if (date.compareTo(era.since) >= 0)
        return era
      i -= 1
    }
    null
  }

  private[chrono] def toJapaneseEra(privateEra: sun.util.calendar.Era): JapaneseEra = {
    var i: Int = ERA_CONFIG.length - 1
    while (i >= 0) {
      if (ERA_CONFIG(i) == privateEra)
        return KNOWN_ERAS(i)
      i -= 1
    }
    null
  }

  private[chrono] def privateEraFrom(isoDate: LocalDate): sun.util.calendar.Era = {
    if (isoDate.isBefore(MEIJI.since))
      throw new DateTimeException(s"Date too early: $isoDate")
    var i: Int = KNOWN_ERAS.length - 1
    while (i >= 0) {
      val era: JapaneseEra = KNOWN_ERAS(i)
      if (isoDate.compareTo(era.since) >= 0)
        return ERA_CONFIG(i)
      i -= 1
    }
    null
  }

  /** Returns the index into the arrays from the Era value.
    * the eraValue is a valid Era number, -999, -1..2.
 *
    * @param eraValue the era value to convert to the index
    * @return the index of the current Era
    */
  private def ordinal(eraValue: Int): Int = eraValue + ERA_OFFSET - 1

  @throws[IOException]
  private[chrono] def readExternal(in: DataInput): JapaneseEra = JapaneseEra.of(in.readByte)

  // !!! FIXME: WHy are we doing this? Isn't KNOWN_ERAS already initialized above?
  var i: Int = N_ERA_CONSTANTS
  while (i < ERA_CONFIG.length) {
    val date: CalendarDate = ERA_CONFIG(i).getSinceDate
    val isoDate: LocalDate = LocalDate.of(date.getYear, date.getMonth, date.getDayOfMonth)
    KNOWN_ERAS(i) = new JapaneseEra(i - ERA_OFFSET, isoDate)
    i += 1
  }
}

/** An era in the Japanese Imperial calendar system.
  *
  * This class defines the valid eras for the Japanese chronology.
  * Japan introduced the Gregorian calendar starting with Meiji 6.
  * Only Meiji and later eras are supported;
  * dates before Meiji 6, January 1 are not supported.
  *
  * <h3>Specification for implementors</h3>
  * This class is immutable and thread-safe.
  *
  * @constructor Creates an instance.
  * @param eraValue  the era value, validated
  * @param since  the date representing the first date of the era, validated not null
  */
@SerialVersionUID(1466499369062886794L)
final class JapaneseEra private(private val eraValue: Int, @(transient @field) private val since: LocalDate) extends Era with Serializable {

  /** Returns the singleton {@code JapaneseEra} corresponding to this object.
    * It's possible that this version of {@code JapaneseEra} doesn't support the latest era value.
    * In that case, this method throws an {@code ObjectStreamException}.
    *
    * @return the singleton { @code JapaneseEra} for this object
    * @throws ObjectStreamException if the deserialized object has any unknown numeric era value.
    */
  @throws[ObjectStreamException]
  private def readResolve: AnyRef = {
    try JapaneseEra.of(eraValue)
    catch {
      case e: DateTimeException =>
        val ex: InvalidObjectException = new InvalidObjectException("Invalid era")
        ex.initCause(e)
        throw ex
    }
  }

  /** Returns the Sun private Era instance corresponding to this {@code JapaneseEra}.
    * SEIREKI doesn't have its corresponding one.
    *
    * @return the Sun private Era instance for this { @code JapaneseEra}
    */
  private[chrono] def getPrivateEra: sun.util.calendar.Era = JapaneseEra.ERA_CONFIG(JapaneseEra.ordinal(eraValue))

  /** Returns the start date of the era.
 *
    * @return the start date
    */
  private[chrono] def startDate: LocalDate = since

  /** Returns the start date of the era.
 *
    * @return the start date
    */
  private[chrono] def endDate: LocalDate = {
    val ordinal: Int = JapaneseEra.ordinal(eraValue)
    val eras: Array[JapaneseEra] = JapaneseEra.values
    if (ordinal >= eras.length - 1) LocalDate.MAX
    else eras(ordinal + 1).startDate.minusDays(1)
  }

  /** Returns the numeric value of this {@code JapaneseEra}.
    *
    * The {@link #SHOWA} era that contains 1970-01-01 (ISO calendar system) has the value 1.
    * Later eras are numbered from 2 ({@link #HEISEI}).
    * Earlier eras are numbered 0 ({@link #TAISHO}) and -1 ({@link #MEIJI}).
    *
    * @return the era value
    */
  def getValue: Int = eraValue

  override def range(field: TemporalField): ValueRange =
    if (field eq ChronoField.ERA) JapaneseChronology.INSTANCE.range(ChronoField.ERA)
    else super.range(field)

  private[chrono] def getAbbreviation: String = {
    val index: Int = JapaneseEra.ordinal(getValue)
    if (index == 0) ""
    else JapaneseEra.ERA_CONFIG(index).getAbbreviation
  }

  private[chrono] def getName: String = {
    val index: Int = JapaneseEra.ordinal(getValue)
    JapaneseEra.ERA_CONFIG(index).getName
  }

  override def toString: String = getName

  private def writeReplace: AnyRef = new Ser(Ser.JAPANESE_ERA_TYPE, this)

  @throws[IOException]
  private[chrono] def writeExternal(out: DataOutput): Unit = out.writeByte(this.getValue)
}