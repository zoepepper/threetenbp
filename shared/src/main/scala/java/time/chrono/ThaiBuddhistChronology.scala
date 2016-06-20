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

import java.time.temporal.ChronoField._
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.MONTHS
import java.time.temporal.ChronoUnit.WEEKS
import java.time.temporal.TemporalAdjusters.nextOrSame
import java.io.Serializable
import java.time.Clock
import java.util.{Arrays, Objects}

import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.ResolverStyle
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalField
import java.time.temporal.ValueRange

@SerialVersionUID(2775954514031616474L)
object ThaiBuddhistChronology {
  /** Singleton instance of the Buddhist chronology. */
  val INSTANCE: ThaiBuddhistChronology = new ThaiBuddhistChronology
  /** Containing the offset to add to the ISO year. */
  private[chrono] val YEARS_DIFFERENCE: Int = 543

  /** Fallback language for the era names. */
  private val FALLBACK_LANGUAGE: String = "en"
  /** Language that has the era names. */
  private val TARGET_LANGUAGE: String = "th"

  /** Narrow names for eras. */
  private val ERA_NARROW_NAMES: java.util.HashMap[String, Array[String]] = {
    val names = new java.util.HashMap[String, Array[String]]
    names.put(FALLBACK_LANGUAGE, Array[String]("BB", "BE"))
    names.put(TARGET_LANGUAGE, Array[String]("BB", "BE"))
    names
  }
  /** Short names for eras. */
  private val ERA_SHORT_NAMES: java.util.HashMap[String, Array[String]] = {
    val names = new java.util.HashMap[String, Array[String]]
    names.put(FALLBACK_LANGUAGE, Array[String]("B.B.", "B.E."))
    names.put(TARGET_LANGUAGE, Array[String]("\u0e1e.\u0e28.", "\u0e1b\u0e35\u0e01\u0e48\u0e2d\u0e19\u0e04\u0e23\u0e34\u0e2a\u0e15\u0e4c\u0e01\u0e32\u0e25\u0e17\u0e35\u0e48"))
    names
  }
  /** Full names for eras. */
  private val ERA_FULL_NAMES: java.util.HashMap[String, Array[String]] = {
    val names = new java.util.HashMap[String, Array[String]]
    names.put(FALLBACK_LANGUAGE, Array[String]("Before Buddhist", "Budhhist Era"))
    names.put(TARGET_LANGUAGE, Array[String]("\u0e1e\u0e38\u0e17\u0e18\u0e28\u0e31\u0e01\u0e23\u0e32\u0e0a", "\u0e1b\u0e35\u0e01\u0e48\u0e2d\u0e19\u0e04\u0e23\u0e34\u0e2a\u0e15\u0e4c\u0e01\u0e32\u0e25\u0e17\u0e35\u0e48"))
    names
  }
}

/** The Thai Buddhist calendar system.
  *
  * This chronology defines the rules of the Thai Buddhist calendar system.
  * This calendar system is primarily used in Thailand.
  * Dates are aligned such that {@code 2484-01-01 (Buddhist)} is {@code 1941-01-01 (ISO)}.
  *
  * The fields are defined as follows:
  *<ul>
  * <li>era - There are two eras, the current 'Buddhist' (BE) and the previous era (BEFORE_BE).
  * <li>year-of-era - The year-of-era for the current era increases uniformly from the epoch at year one.
  * For the previous era the year increases from one as time goes backwards.
  * The value for the current era is equal to the ISO proleptic-year plus 543.
  * <li>proleptic-year - The proleptic year is the same as the year-of-era for the
  * current era. For the previous era, years have zero, then negative values.
  * The value is equal to the ISO proleptic-year plus 543.
  * <li>month-of-year - The ThaiBuddhist month-of-year exactly matches ISO.
  * <li>day-of-month - The ThaiBuddhist day-of-month exactly matches ISO.
  * <li>day-of-year - The ThaiBuddhist day-of-year exactly matches ISO.
  * <li>leap-year - The ThaiBuddhist leap-year pattern exactly matches ISO, such that the two calendars
  * are never out of step.
  * </ul><p>
  *
  * <h3>Specification for implementors</h3>
  * This class is immutable and thread-safe.
  */
@SerialVersionUID(2775954514031616474L)
final class ThaiBuddhistChronology private() extends Chronology with Serializable {

  /** Resolve singleton.
    *
    * @return the singleton instance, not null
    */
  private def readResolve: AnyRef = ThaiBuddhistChronology.INSTANCE

  /** Gets the ID of the chronology - 'ThaiBuddhist'.
    *
    * The ID uniquely identifies the {@code Chronology}.
    * It can be used to lookup the {@code Chronology} using {@link #of(String)}.
    *
    * @return the chronology ID - 'ThaiBuddhist'
    * @see #getCalendarType()
    */
  def getId: String = "ThaiBuddhist"

  /** Gets the calendar type of the underlying calendar system - 'buddhist'.
    *
    * The calendar type is an identifier defined by the
    * <em>Unicode Locale Data Markup Language (LDML)</em> specification.
    * It can be used to lookup the {@code Chronology} using {@link #of(String)}.
    * It can also be used as part of a locale, accessible via
    * {@link Locale#getUnicodeLocaleType(String)} with the key 'ca'.
    *
    * @return the calendar system type - 'buddhist'
    * @see #getId()
    */
  def getCalendarType: String = "buddhist"

  override def date(era: Era, yearOfEra: Int, month: Int, dayOfMonth: Int): ThaiBuddhistDate =
    super.date(era, yearOfEra, month, dayOfMonth).asInstanceOf[ThaiBuddhistDate]

  def date(prolepticYear: Int, month: Int, dayOfMonth: Int): ThaiBuddhistDate =
    new ThaiBuddhistDate(LocalDate.of(prolepticYear - ThaiBuddhistChronology.YEARS_DIFFERENCE, month, dayOfMonth))

  override def dateYearDay(era: Era, yearOfEra: Int, dayOfYear: Int): ThaiBuddhistDate =
    super.dateYearDay(era, yearOfEra, dayOfYear).asInstanceOf[ThaiBuddhistDate]

  def dateYearDay(prolepticYear: Int, dayOfYear: Int): ThaiBuddhistDate =
    new ThaiBuddhistDate(LocalDate.ofYearDay(prolepticYear - ThaiBuddhistChronology.YEARS_DIFFERENCE, dayOfYear))

  def dateEpochDay(epochDay: Long): ThaiBuddhistDate =
    new ThaiBuddhistDate(LocalDate.ofEpochDay(epochDay))

  def date(temporal: TemporalAccessor): ThaiBuddhistDate =
    temporal match {
      case date: ThaiBuddhistDate => date
      case _                      => new ThaiBuddhistDate(LocalDate.from(temporal))
    }

  override def localDateTime(temporal: TemporalAccessor): ChronoLocalDateTime[ThaiBuddhistDate] =
    super.localDateTime(temporal).asInstanceOf[ChronoLocalDateTime[ThaiBuddhistDate]]

  override def zonedDateTime(temporal: TemporalAccessor): ChronoZonedDateTime[ThaiBuddhistDate] =
    super.zonedDateTime(temporal).asInstanceOf[ChronoZonedDateTime[ThaiBuddhistDate]]

  override def zonedDateTime(instant: Instant, zone: ZoneId): ChronoZonedDateTime[ThaiBuddhistDate] =
    super.zonedDateTime(instant, zone).asInstanceOf[ChronoZonedDateTime[ThaiBuddhistDate]]

  override def dateNow: ThaiBuddhistDate = super.dateNow.asInstanceOf[ThaiBuddhistDate]

  override def dateNow(zone: ZoneId): ThaiBuddhistDate = super.dateNow(zone).asInstanceOf[ThaiBuddhistDate]

  override def dateNow(clock: Clock): ThaiBuddhistDate = {
    Objects.requireNonNull(clock, "clock")
    super.dateNow(clock).asInstanceOf[ThaiBuddhistDate]
  }

  /** Checks if the specified year is a leap year.
    *
    * Thai Buddhist leap years occur exactly in line with ISO leap years.
    * This method does not validate the year passed in, and only has a
    * well-defined result for years in the supported range.
    *
    * @param prolepticYear  the proleptic-year to check, not validated for range
    * @return true if the year is a leap year
    */
  def isLeapYear(prolepticYear: Long): Boolean =
    IsoChronology.INSTANCE.isLeapYear(prolepticYear - ThaiBuddhistChronology.YEARS_DIFFERENCE)

  def prolepticYear(era: Era, yearOfEra: Int): Int =
    if (!era.isInstanceOf[ThaiBuddhistEra])
      throw new ClassCastException("Era must be BuddhistEra")
    else
      if (era eq ThaiBuddhistEra.BE) yearOfEra else 1 - yearOfEra

  def eraOf(eraValue: Int): ThaiBuddhistEra = ThaiBuddhistEra.of(eraValue)

  def eras: java.util.List[Era] = Arrays.asList[Era](ThaiBuddhistEra.values: _*)

  def range(field: ChronoField): ValueRange =
    field match {
      case PROLEPTIC_MONTH => val range: ValueRange = PROLEPTIC_MONTH.range
                              ValueRange.of(range.getMinimum + ThaiBuddhistChronology.YEARS_DIFFERENCE * 12L, range.getMaximum + ThaiBuddhistChronology.YEARS_DIFFERENCE * 12L)
      case YEAR_OF_ERA     => val range: ValueRange = YEAR.range
                              ValueRange.of(1, -(range.getMinimum + ThaiBuddhistChronology.YEARS_DIFFERENCE) + 1, range.getMaximum + ThaiBuddhistChronology.YEARS_DIFFERENCE)
      case YEAR            => val range: ValueRange = YEAR.range
                              ValueRange.of(range.getMinimum + ThaiBuddhistChronology.YEARS_DIFFERENCE, range.getMaximum + ThaiBuddhistChronology.YEARS_DIFFERENCE)
      case _               => field.range
    }

  override def resolveDate(fieldValues: java.util.Map[TemporalField, java.lang.Long], resolverStyle: ResolverStyle): ThaiBuddhistDate = {
    if (fieldValues.containsKey(EPOCH_DAY))
      return dateEpochDay(fieldValues.remove(EPOCH_DAY))
    val prolepticMonth: java.lang.Long = fieldValues.remove(PROLEPTIC_MONTH)
    if (prolepticMonth != null) {
      if (resolverStyle ne ResolverStyle.LENIENT)
        PROLEPTIC_MONTH.checkValidValue(prolepticMonth)
      updateResolveMap(fieldValues, MONTH_OF_YEAR, Math.floorMod(prolepticMonth, 12) + 1)
      updateResolveMap(fieldValues, YEAR, Math.floorDiv(prolepticMonth, 12))
    }
    val yoeLong: java.lang.Long = fieldValues.remove(YEAR_OF_ERA)
    if (yoeLong != null) {
      if (resolverStyle ne ResolverStyle.LENIENT)
        YEAR_OF_ERA.checkValidValue(yoeLong)
      val era: java.lang.Long = fieldValues.remove(ERA)
      if (era == null) {
        val year: java.lang.Long = fieldValues.get(YEAR)
        if (resolverStyle eq ResolverStyle.STRICT) {
          if (year != null)
            updateResolveMap(fieldValues, YEAR, if (year > 0) yoeLong else Math.subtractExact(1, yoeLong))
          else
            fieldValues.put(YEAR_OF_ERA, yoeLong)
        }
        else
          updateResolveMap(fieldValues, YEAR, if (year == null || year > 0) yoeLong else Math.subtractExact(1, yoeLong))
      }
      else if (era.longValue == 1L)
        updateResolveMap(fieldValues, YEAR, yoeLong)
      else if (era.longValue == 0L)
        updateResolveMap(fieldValues, YEAR, Math.subtractExact(1, yoeLong))
      else
        throw new DateTimeException(s"Invalid value for era: $era")
    }
    else if (fieldValues.containsKey(ERA))
      ERA.checkValidValue(fieldValues.get(ERA))
    if (fieldValues.containsKey(YEAR)) {
      if (fieldValues.containsKey(MONTH_OF_YEAR)) {
        if (fieldValues.containsKey(DAY_OF_MONTH)) {
          val y: Int = YEAR.checkValidIntValue(fieldValues.remove(YEAR))
          if (resolverStyle eq ResolverStyle.LENIENT) {
            val months: Long = Math.subtractExact(fieldValues.remove(MONTH_OF_YEAR), 1)
            val days: Long = Math.subtractExact(fieldValues.remove(DAY_OF_MONTH), 1)
            return date(y, 1, 1).plusMonths(months).plusDays(days)
          }
          else {
            val moy: Int = range(MONTH_OF_YEAR).checkValidIntValue(fieldValues.remove(MONTH_OF_YEAR), MONTH_OF_YEAR)
            var dom: Int = range(DAY_OF_MONTH).checkValidIntValue(fieldValues.remove(DAY_OF_MONTH), DAY_OF_MONTH)
            if ((resolverStyle eq ResolverStyle.SMART) && dom > 28)
              dom = Math.min(dom, date(y, moy, 1).lengthOfMonth)
            return date(y, moy, dom)
          }
        }
        if (fieldValues.containsKey(ALIGNED_WEEK_OF_MONTH)) {
          if (fieldValues.containsKey(ALIGNED_DAY_OF_WEEK_IN_MONTH)) {
            val y: Int = YEAR.checkValidIntValue(fieldValues.remove(YEAR))
            if (resolverStyle eq ResolverStyle.LENIENT) {
              val months: Long = Math.subtractExact(fieldValues.remove(MONTH_OF_YEAR), 1)
              val weeks: Long = Math.subtractExact(fieldValues.remove(ALIGNED_WEEK_OF_MONTH), 1)
              val days: Long = Math.subtractExact(fieldValues.remove(ALIGNED_DAY_OF_WEEK_IN_MONTH), 1)
              return date(y, 1, 1).plus(months, MONTHS).plus(weeks, WEEKS).plus(days, DAYS)
            }
            val moy: Int = MONTH_OF_YEAR.checkValidIntValue(fieldValues.remove(MONTH_OF_YEAR))
            val aw: Int = ALIGNED_WEEK_OF_MONTH.checkValidIntValue(fieldValues.remove(ALIGNED_WEEK_OF_MONTH))
            val ad: Int = ALIGNED_DAY_OF_WEEK_IN_MONTH.checkValidIntValue(fieldValues.remove(ALIGNED_DAY_OF_WEEK_IN_MONTH))
            val tbDate: ThaiBuddhistDate = date(y, moy, 1).plus((aw - 1) * 7 + (ad - 1), DAYS)
            if ((resolverStyle eq ResolverStyle.STRICT) && tbDate.get(MONTH_OF_YEAR) != moy)
              throw new DateTimeException("Strict mode rejected date parsed to a different month")
            return tbDate
          }
          if (fieldValues.containsKey(DAY_OF_WEEK)) {
            val y: Int = YEAR.checkValidIntValue(fieldValues.remove(YEAR))
            if (resolverStyle eq ResolverStyle.LENIENT) {
              val months: Long = Math.subtractExact(fieldValues.remove(MONTH_OF_YEAR), 1)
              val weeks: Long = Math.subtractExact(fieldValues.remove(ALIGNED_WEEK_OF_MONTH), 1)
              val days: Long = Math.subtractExact(fieldValues.remove(DAY_OF_WEEK), 1)
              return date(y, 1, 1).plus(months, MONTHS).plus(weeks, WEEKS).plus(days, DAYS)
            }
            val moy: Int = MONTH_OF_YEAR.checkValidIntValue(fieldValues.remove(MONTH_OF_YEAR))
            val aw: Int = ALIGNED_WEEK_OF_MONTH.checkValidIntValue(fieldValues.remove(ALIGNED_WEEK_OF_MONTH))
            val dow: Int = DAY_OF_WEEK.checkValidIntValue(fieldValues.remove(DAY_OF_WEEK))
            val tbDate: ThaiBuddhistDate = date(y, moy, 1).plus(aw - 1, WEEKS).`with`(nextOrSame(DayOfWeek.of(dow)))
            if ((resolverStyle eq ResolverStyle.STRICT) && tbDate.get(MONTH_OF_YEAR) != moy)
              throw new DateTimeException("Strict mode rejected date parsed to a different month")
            return tbDate
          }
        }
      }
      if (fieldValues.containsKey(DAY_OF_YEAR)) {
        val y: Int = YEAR.checkValidIntValue(fieldValues.remove(YEAR))
        if (resolverStyle eq ResolverStyle.LENIENT) {
          val days: Long = Math.subtractExact(fieldValues.remove(DAY_OF_YEAR), 1)
          return dateYearDay(y, 1).plusDays(days)
        }
        val doy: Int = DAY_OF_YEAR.checkValidIntValue(fieldValues.remove(DAY_OF_YEAR))
        return dateYearDay(y, doy)
      }
      if (fieldValues.containsKey(ALIGNED_WEEK_OF_YEAR)) {
        if (fieldValues.containsKey(ALIGNED_DAY_OF_WEEK_IN_YEAR)) {
          val y: Int = YEAR.checkValidIntValue(fieldValues.remove(YEAR))
          if (resolverStyle eq ResolverStyle.LENIENT) {
            val weeks: Long = Math.subtractExact(fieldValues.remove(ALIGNED_WEEK_OF_YEAR), 1)
            val days: Long = Math.subtractExact(fieldValues.remove(ALIGNED_DAY_OF_WEEK_IN_YEAR), 1)
            return date(y, 1, 1).plus(weeks, WEEKS).plus(days, DAYS)
          }
          val aw: Int = ALIGNED_WEEK_OF_YEAR.checkValidIntValue(fieldValues.remove(ALIGNED_WEEK_OF_YEAR))
          val ad: Int = ALIGNED_DAY_OF_WEEK_IN_YEAR.checkValidIntValue(fieldValues.remove(ALIGNED_DAY_OF_WEEK_IN_YEAR))
          val tbDate: ThaiBuddhistDate = date(y, 1, 1).plusDays((aw - 1) * 7 + (ad - 1))
          if ((resolverStyle eq ResolverStyle.STRICT) && tbDate.get(YEAR) != y)
            throw new DateTimeException("Strict mode rejected date parsed to a different year")
          return tbDate
        }
        if (fieldValues.containsKey(DAY_OF_WEEK)) {
          val y: Int = YEAR.checkValidIntValue(fieldValues.remove(YEAR))
          if (resolverStyle eq ResolverStyle.LENIENT) {
            val weeks: Long = Math.subtractExact(fieldValues.remove(ALIGNED_WEEK_OF_YEAR), 1)
            val days: Long = Math.subtractExact(fieldValues.remove(DAY_OF_WEEK), 1)
            return date(y, 1, 1).plus(weeks, WEEKS).plus(days, DAYS)
          }
          val aw: Int = ALIGNED_WEEK_OF_YEAR.checkValidIntValue(fieldValues.remove(ALIGNED_WEEK_OF_YEAR))
          val dow: Int = DAY_OF_WEEK.checkValidIntValue(fieldValues.remove(DAY_OF_WEEK))
          val tbDate: ThaiBuddhistDate = date(y, 1, 1).plus(aw - 1, WEEKS).`with`(nextOrSame(DayOfWeek.of(dow)))
          if ((resolverStyle eq ResolverStyle.STRICT) && tbDate.get(YEAR) != y)
            throw new DateTimeException("Strict mode rejected date parsed to a different month")
          return tbDate
        }
      }
    }
    null
  }
}