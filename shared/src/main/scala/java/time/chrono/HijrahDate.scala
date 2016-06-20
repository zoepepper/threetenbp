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


import java.time.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH
import java.time.temporal.ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR
import java.time.temporal.ChronoField.ALIGNED_WEEK_OF_MONTH
import java.time.temporal.ChronoField.ALIGNED_WEEK_OF_YEAR
import java.time.temporal.ChronoField.EPOCH_DAY
import java.time.temporal.ChronoField.DAY_OF_WEEK
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.DAY_OF_YEAR
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.time.temporal.ChronoField.YEAR
import java.time.temporal.ChronoField.YEAR_OF_ERA
import java.time.temporal.ChronoField.ERA
import java.io.BufferedReader
import java.io.DataInput
import java.io.DataOutput
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Serializable
import java.text.ParseException
import java.time
import java.time.Clock
import java.util.{Objects, StringTokenizer}
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalField
import java.time.temporal.TemporalUnit
import java.time.temporal.UnsupportedTemporalTypeException
import java.time.temporal.ValueRange

@SerialVersionUID(-5207853542612002020L)
object HijrahDate {
  /** The minimum valid year-of-era. */
  val MIN_VALUE_OF_ERA: Int = 1
  /** The maximum valid year-of-era.
    * This is currently set to 9999 but may be changed to increase the valid range
    * in a future version of the specification.
    */
  val MAX_VALUE_OF_ERA: Int = 9999
  /** 0-based, for number of day-of-year in the beginning of month in normal
    * year.
    */
  private val NUM_DAYS: Array[Int] = Array(0, 30, 59, 89, 118, 148, 177, 207, 236, 266, 295, 325)
  /** 0-based, for number of day-of-year in the beginning of month in leap year. */
  private val LEAP_NUM_DAYS: Array[Int] = Array(0, 30, 59, 89, 118, 148, 177, 207, 236, 266, 295, 325)
  /** 0-based, for day-of-month in normal year. */
  private val MONTH_LENGTH: Array[Int] = Array(30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29)
  /** 0-based, for day-of-month in leap year. */
  private val LEAP_MONTH_LENGTH: Array[Int] = Array(30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30)
  /** <pre>
    * Greatest       Least
    * Field name        Minimum   Minimum     Maximum     Maximum
    * ----------        -------   -------     -------     -------
    * ERA                     0         0           1           1
    * YEAR_OF_ERA             1         1        9999        9999
    * MONTH_OF_YEAR           1         1          12          12
    * DAY_OF_MONTH            1         1          29          30
    * DAY_OF_YEAR             1         1         354         355
    * </pre>
    *
    * Minimum values.
    */
  private val MIN_VALUES: Array[Int] = Array(0, MIN_VALUE_OF_ERA, 0, 1, 0, 1, 1)
  /** Least maximum values. */
  private val LEAST_MAX_VALUES: Array[Int] = Array(1, MAX_VALUE_OF_ERA, 11, 51, 5, 29, 354)
  /** Maximum values. */
  private val MAX_VALUES: Array[Int] = Array(1, MAX_VALUE_OF_ERA, 11, 52, 6, 30, 355)
  /** Position of day-of-month. This value is used to get the min/max value
    * from an array.
    */
  private val POSITION_DAY_OF_MONTH: Int = 5
  /** Position of day-of-year. This value is used to get the min/max value from
    * an array.
    */
  private val POSITION_DAY_OF_YEAR: Int = 6
  /** Zero-based start date of cycle year. */
  private val CYCLEYEAR_START_DATE: Array[Int] = Array(0, 354, 709, 1063, 1417, 1772, 2126, 2481, 2835, 3189, 3544, 3898, 4252, 4607, 4961, 5315, 5670, 6024, 6379, 6733, 7087, 7442, 7796, 8150, 8505, 8859, 9214, 9568, 9922, 10277)
  /** File separator. */
  private val FILE_SEP: Char = File.separatorChar
  /** Path separator. */
  private val PATH_SEP: String = File.pathSeparator
  /** Default config file name. */
  private val DEFAULT_CONFIG_FILENAME: String = "hijrah_deviation.cfg"
  /** Default path to the config file. */
  private val DEFAULT_CONFIG_PATH: String = s"org${FILE_SEP}threeten${FILE_SEP}bp${FILE_SEP}chrono"

  /** number of 30-year cycles to hold the deviation data. */
  private val MAX_ADJUSTED_CYCLE: Int = 334

  /** Holding the adjusted month days in year. The key is a year (Integer) and
    * the value is the all the month days in year (Integer[]).
    */
  private val ADJUSTED_MONTH_DAYS: java.util.HashMap[Integer, Array[Integer]] = new java.util.HashMap[Integer, Array[Integer]]
  /** Holding the adjusted month length in year. The key is a year (Integer)
    * and the value is the all the month length in year (Integer[]).
    */
  private val ADJUSTED_MONTH_LENGTHS: java.util.HashMap[Integer, Array[Integer]] = new java.util.HashMap[Integer, Array[Integer]]
  /** Holding the adjusted days in the 30 year cycle. The key is a cycle number
    * (Integer) and the value is the all the starting days of the year in the
    * cycle (Integer[]).
    */
  private val ADJUSTED_CYCLE_YEARS: java.util.HashMap[Integer, Array[Integer]] = new java.util.HashMap[Integer, Array[Integer]]
  /** Holding the adjusted cycle in the 1 - 30000 year. The key is the cycle
    * number (Integer) and the value is the starting days in the cycle in the
    * term.
    */
  private val ADJUSTED_CYCLES: Array[Long] = {
    val cycles = new Array[Long](MAX_ADJUSTED_CYCLE)
    var i: Int = 0
    while (i < MAX_ADJUSTED_CYCLE) {
      cycles(i) = new java.lang.Long(10631 * i)
      i += 1
    }
    cycles
  }

  /** Holding the adjusted min values. */
  private val ADJUSTED_MIN_VALUES: Array[Integer] = {
    val values = new Array[Integer](MIN_VALUES.length)
    var i: Int = 0
    while (i < MIN_VALUES.length) {
      values(i) = new Integer(MIN_VALUES(i))
      i += 1
    }
    values
  }
  /** Holding the adjusted max least max values. */
  private val ADJUSTED_LEAST_MAX_VALUES: Array[Integer] = {
    val values = new Array[Integer](LEAST_MAX_VALUES.length)
    var i: Int = 0
    while (i < LEAST_MAX_VALUES.length) {
      values(i) = new Integer(LEAST_MAX_VALUES(i))
      i += 1
    }
    values
  }
  /** Holding adjusted max values. */
  private val ADJUSTED_MAX_VALUES: Array[Integer] = {
    val values = new Array[Integer](MAX_VALUES.length)
    var i: Int = 0
    while (i < MAX_VALUES.length) {
      values(i) = new Integer(MAX_VALUES(i))
      i += 1
    }
    values
  }
  /** Holding the non-adjusted month days in year for non leap year. */
  private val DEFAULT_MONTH_DAYS: Array[Integer] = {
    val days = new Array[Integer](NUM_DAYS.length)
    var i: Int = 0
    while (i < NUM_DAYS.length) {
      days(i) = new Integer(NUM_DAYS(i))
      i += 1
    }
    days
  }

  /** Holding the non-adjusted month days in year for leap year. */
  private val DEFAULT_LEAP_MONTH_DAYS: Array[Integer] = {
    val days = new Array[Integer](LEAP_NUM_DAYS.length)
    var i: Int = 0
    while (i < LEAP_NUM_DAYS.length) {
      days(i) = new Integer(LEAP_NUM_DAYS(i))
      i += 1
    }
    days
  }
  /** Holding the non-adjusted month length for non leap year. */
  private val DEFAULT_MONTH_LENGTHS: Array[Integer] = {
    val lengths = new Array[Integer](MONTH_LENGTH.length)
    var i: Int = 0
    while (i < MONTH_LENGTH.length) {
      lengths(i) = new Integer(MONTH_LENGTH(i))
      i += 1
    }
    lengths
  }
  /** Holding the non-adjusted month length for leap year. */
  private val DEFAULT_LEAP_MONTH_LENGTHS: Array[Integer] = {
    val lengths = new Array[Integer](LEAP_MONTH_LENGTH.length)
    var i: Int = 0
    while (i < LEAP_MONTH_LENGTH.length) {
      lengths(i) = new Integer(LEAP_MONTH_LENGTH(i))
      i += 1
    }
    lengths
  }
  /** Holding the non-adjusted 30 year cycle starting day. */
  private val DEFAULT_CYCLE_YEARS: Array[Integer] = {
    val years = new Array[Integer](CYCLEYEAR_START_DATE.length)
    var i: Int = 0
    while (i < CYCLEYEAR_START_DATE.length) {
      years(i) = new Integer(CYCLEYEAR_START_DATE(i))
      i += 1
    }
    years
  }
  /** Number of Gregorian day of July 19, year 622 (Gregorian), which is epoch day
    * of Hijrah calendar.
    */
  private val HIJRAH_JAN_1_1_GREGORIAN_DAY: Int = -492148

  try readDeviationConfig()
  catch {
    case e: IOException =>
    case e: ParseException =>
  }

  /** Obtains the current {@code HijrahDate} of the Islamic Umm Al-Qura calendar
    * in the default time-zone.
    *
    * This will query the {@link Clock#systemDefaultZone() system clock} in the default
    * time-zone to obtain the current date.
    *
    * Using this method will prevent the ability to use an alternate clock for testing
    * because the clock is hard-coded.
    *
    * @return the current date using the system clock and default time-zone, not null
    */
  def now: HijrahDate = now(time.Clock.systemDefaultZone)

  /** Obtains the current {@code HijrahDate} of the Islamic Umm Al-Qura calendar
    * in the specified time-zone.
    *
    * This will query the {@link Clock#system(ZoneId) system clock} to obtain the current date.
    * Specifying the time-zone avoids dependence on the default time-zone.
    *
    * Using this method will prevent the ability to use an alternate clock for testing
    * because the clock is hard-coded.
    *
    * @param zone  the zone ID to use, not null
    * @return the current date using the system clock, not null
    */
  def now(zone: ZoneId): HijrahDate = now(time.Clock.system(zone))

  /** Obtains the current {@code HijrahDate} of the Islamic Umm Al-Qura calendar
    * from the specified clock.
    *
    * This will query the specified clock to obtain the current date - today.
    * Using this method allows the use of an alternate clock for testing.
    * The alternate clock may be introduced using {@linkplain Clock dependency injection}.
    *
    * @param clock  the clock to use, not null
    * @return the current date, not null
    * @throws DateTimeException if the current date cannot be obtained
    */
  def now(clock: Clock): HijrahDate = HijrahChronology.INSTANCE.dateNow(clock)

  /** Obtains an instance of {@code HijrahDate} from the Hijrah era year,
    * month-of-year and day-of-month. This uses the Hijrah era.
    *
    * @param prolepticYear  the proleptic year to represent in the Hijrah
    * @param monthOfYear  the month-of-year to represent, from 1 to 12
    * @param dayOfMonth  the day-of-month to represent, from 1 to 30
    * @return the Hijrah date, never null
    * @throws DateTimeException if the value of any field is out of range
    * @throws DateTimeException if the day-of-month is invalid for the month-year
    */
  def of(prolepticYear: Int, monthOfYear: Int, dayOfMonth: Int): HijrahDate =
    if (prolepticYear >= 1) HijrahDate.of(HijrahEra.AH, prolepticYear, monthOfYear, dayOfMonth)
    else HijrahDate.of(HijrahEra.BEFORE_AH, 1 - prolepticYear, monthOfYear, dayOfMonth)

  /** Obtains an instance of {@code HijrahDate} from the era, year-of-era
    * month-of-year and day-of-month.
    *
    * @param era  the era to represent, not null
    * @param yearOfEra  the year-of-era to represent, from 1 to 9999
    * @param monthOfYear  the month-of-year to represent, from 1 to 12
    * @param dayOfMonth  the day-of-month to represent, from 1 to 31
    * @return the Hijrah date, never null
    * @throws DateTimeException if the value of any field is out of range
    * @throws DateTimeException if the day-of-month is invalid for the month-year
    */
  private[chrono] def of(era: HijrahEra, yearOfEra: Int, monthOfYear: Int, dayOfMonth: Int): HijrahDate = {
    Objects.requireNonNull(era, "era")
    checkValidYearOfEra(yearOfEra)
    checkValidMonth(monthOfYear)
    checkValidDayOfMonth(dayOfMonth)
    val gregorianDays: Long = getGregorianEpochDay(era.prolepticYear(yearOfEra), monthOfYear, dayOfMonth)
    new HijrahDate(gregorianDays)
  }

  /** Check the validity of a yearOfEra.
    *
    * @param yearOfEra the year to check
    */
  private def checkValidYearOfEra(yearOfEra: Int): Unit =
    if (yearOfEra < MIN_VALUE_OF_ERA || yearOfEra > MAX_VALUE_OF_ERA)
      throw new DateTimeException("Invalid year of Hijrah Era")

  private def checkValidDayOfYear(dayOfYear: Int): Unit =
    if (dayOfYear < 1 || dayOfYear > getMaximumDayOfYear)
      throw new DateTimeException("Invalid day of year of Hijrah date")

  private def checkValidMonth(month: Int): Unit =
    if (month < 1 || month > 12)
      throw new DateTimeException("Invalid month of Hijrah date")

  private def checkValidDayOfMonth(dayOfMonth: Int): Unit =
    if (dayOfMonth < 1 || dayOfMonth > getMaximumDayOfMonth)
      throw new DateTimeException(s"Invalid day of month of Hijrah date, day $dayOfMonth greater than $getMaximumDayOfMonth or less than 1")

  /** Obtains an instance of {@code HijrahDate} from a date.
    *
    * @param date  the date to use, not null
    * @return the Hijrah date, never null
    * @throws DateTimeException if the year is invalid
    */
  private[chrono] def of(date: LocalDate): HijrahDate = {
    val gregorianDays: Long = date.toEpochDay
    new HijrahDate(gregorianDays)
  }

  private[chrono] def ofEpochDay(epochDay: Long): HijrahDate = new HijrahDate(epochDay)

  /** Obtains a {@code HijrahDate} of the Islamic Umm Al-Qura calendar from a temporal object.
    *
    * This obtains a date in the Hijrah calendar system based on the specified temporal.
    * A {@code TemporalAccessor} represents an arbitrary set of date and time information,
    * which this factory converts to an instance of {@code HijrahDate}.
    *
    * The conversion typically uses the {@link ChronoField#EPOCH_DAY EPOCH_DAY}
    * field, which is standardized across calendar systems.
    *
    * This method matches the signature of the functional interface {@link TemporalQuery}
    * allowing it to be used as a query via method reference, {@code HijrahDate::from}.
    *
    * @param temporal  the temporal object to convert, not null
    * @return the date in Hijrah calendar system, not null
    * @throws DateTimeException if unable to convert to a { @code HijrahDate}
    */
  def from(temporal: TemporalAccessor): HijrahDate = HijrahChronology.INSTANCE.date(temporal)

  private def resolvePreviousValid(yearOfEra: Int, month: Int, day: Int): HijrahDate = {
    var _day = day
    val monthDays: Int = getMonthDays(month - 1, yearOfEra)
    if (_day > monthDays) {
      _day = monthDays
    }
    HijrahDate.of(yearOfEra, month, _day)
  }

  /** Returns the int array containing the following field from the julian day.
    *
    * int[0] = ERA
    * int[1] = YEAR
    * int[2] = MONTH
    * int[3] = DATE
    * int[4] = DAY_OF_YEAR
    * int[5] = DAY_OF_WEEK
    *
    * @param julianDay  a julian day.
    */
  private def getHijrahDateInfo(gregorianDays: Long): Array[Int] = {
    var era: Int = 0
    var year: Int = 0
    var month: Int = 0
    var date: Int = 0
    var dayOfWeek: Int = 0
    var dayOfYear: Int = 0
    var cycleNumber: Int = 0
    var yearInCycle: Int = 0
    var dayOfCycle: Int = 0
    val epochDay: Long = gregorianDays - HIJRAH_JAN_1_1_GREGORIAN_DAY
    if (epochDay >= 0) {
      cycleNumber = getCycleNumber(epochDay)
      dayOfCycle = getDayOfCycle(epochDay, cycleNumber)
      yearInCycle = getYearInCycle(cycleNumber, dayOfCycle)
      dayOfYear = getDayOfYear(cycleNumber, dayOfCycle, yearInCycle)
      year = cycleNumber * 30 + yearInCycle + 1
      month = getMonthOfYear(dayOfYear, year)
      date = getDayOfMonth(dayOfYear, month, year)
      date += 1
      era = HijrahEra.AH.getValue
    }
    else {
      cycleNumber = epochDay.toInt / 10631
      dayOfCycle = epochDay.toInt % 10631
      if (dayOfCycle == 0) {
        dayOfCycle = -10631
        cycleNumber += 1
      }
      yearInCycle = getYearInCycle(cycleNumber, dayOfCycle)
      dayOfYear = getDayOfYear(cycleNumber, dayOfCycle, yearInCycle)
      year = cycleNumber * 30 - yearInCycle
      year = 1 - year
      dayOfYear = if (isLeapYear(year)) dayOfYear + 355 else dayOfYear + 354
      month = getMonthOfYear(dayOfYear, year)
      date = getDayOfMonth(dayOfYear, month, year)
      date += 1
      era = HijrahEra.BEFORE_AH.getValue
    }
    dayOfWeek = ((epochDay + 5) % 7).toInt
    dayOfWeek += (if (dayOfWeek <= 0) 7 else 0)
    Array[Int](era, year, month + 1, date, dayOfYear + 1, dayOfWeek)
  }

  /** Return Gregorian epoch day from Hijrah year, month, and day.
    *
    * @param prolepticYear  the year to represent, caller calculated
    * @param monthOfYear  the month-of-year to represent, caller calculated
    * @param dayOfMonth  the day-of-month to represent, caller calculated
    * @return a julian day
    */
  private def getGregorianEpochDay(prolepticYear: Int, monthOfYear: Int, dayOfMonth: Int): Long = {
    var day: Long = yearToGregorianEpochDay(prolepticYear)
    day += getMonthDays(monthOfYear - 1, prolepticYear)
    day += dayOfMonth
    day
  }

  /** Returns the Gregorian epoch day from the proleptic year
    *
    * @param prolepticYear the proleptic year
    * @return the Epoch day
    */
  private def yearToGregorianEpochDay(prolepticYear: Int): Long = {
    val cycleNumber: Int = (prolepticYear - 1) / 30
    val yearInCycle: Int = (prolepticYear - 1) % 30
    var dayInCycle: Int = getAdjustedCycle(cycleNumber)(Math.abs(yearInCycle)).intValue
    if (yearInCycle < 0) {
      dayInCycle = -dayInCycle
    }
    var cycleDays: java.lang.Long = null
    try {
      cycleDays = ADJUSTED_CYCLES(cycleNumber)
    }
    catch {
      case e: ArrayIndexOutOfBoundsException =>
        cycleDays = null
    }
    if (cycleDays == null) {
      cycleDays = new java.lang.Long(cycleNumber * 10631)
    }
    (cycleDays.longValue + dayInCycle + HIJRAH_JAN_1_1_GREGORIAN_DAY - 1)
  }

  /** Returns the 30 year cycle number from the epoch day.
    *
    * @param epochDay  an epoch day
    * @return a cycle number
    */
  private def getCycleNumber(epochDay: Long): Int = {
    val days: Array[Long] = ADJUSTED_CYCLES
    var cycleNumber: Int = 0
    try {
      var i: Int = 0
      while (i < days.length) {
        if (epochDay < days(i).longValue)
          return i - 1
          i += 1
      }
      cycleNumber = epochDay.toInt / 10631
    } catch {
      case e: ArrayIndexOutOfBoundsException => cycleNumber = epochDay.toInt / 10631
    }
    cycleNumber
  }

  /** Returns day of cycle from the epoch day and cycle number.
    *
    * @param epochDay  an epoch day
    * @param cycleNumber  a cycle number
    * @return a day of cycle
    */
  private def getDayOfCycle(epochDay: Long, cycleNumber: Int): Int = {
    var day: java.lang.Long = null
    try day = ADJUSTED_CYCLES(cycleNumber)
    catch {
      case e: ArrayIndexOutOfBoundsException =>
        day = null
    }
    if (day == null) {
      day = new java.lang.Long(cycleNumber * 10631)
    }
    (epochDay - day.longValue).toInt
  }

  /** Returns the year in cycle from the cycle number and day of cycle.
    *
    * @param cycleNumber  a cycle number
    * @param dayOfCycle  day of cycle
    * @return a year in cycle
    */
  private def getYearInCycle(cycleNumber: Int, dayOfCycle: Long): Int = {
    var _dayOfCycle = dayOfCycle
    val cycles: Array[Integer] = getAdjustedCycle(cycleNumber)
    if (_dayOfCycle == 0) {
      return 0
    }
    if (_dayOfCycle > 0) {
      var i: Int = 0
      while (i < cycles.length) {
        if (_dayOfCycle < cycles(i).intValue) {
          return i - 1
        }
        i += 1
      }
      29
    }
    else {
      _dayOfCycle = -_dayOfCycle

      var i: Int = 0
      while (i < cycles.length) {
        if (_dayOfCycle <= cycles(i).intValue) {
          return i - 1
        }
        i += 1
      }
      29
    }
  }

  /** Returns adjusted 30 year cycle startind day as Integer array from the
    * cycle number specified.
    *
    * @param cycleNumber  a cycle number
    * @return an Integer array
    */
  private def getAdjustedCycle(cycleNumber: Int): Array[Integer] = {
    var cycles: Array[Integer] = null
    try cycles = ADJUSTED_CYCLE_YEARS.get(new Integer(cycleNumber))
    catch {
      case e: ArrayIndexOutOfBoundsException =>
        cycles = null
    }
    if (cycles == null)
      cycles = DEFAULT_CYCLE_YEARS
    cycles
  }

  /** Returns adjusted month days as Integer array form the year specified.
    *
    * @param year  a year
    * @return an Integer array
    */
  private def getAdjustedMonthDays(year: Int): Array[Integer] = {
    var newMonths: Array[Integer] = null
    try newMonths = ADJUSTED_MONTH_DAYS.get(new Integer(year))
    catch {
      case e: ArrayIndexOutOfBoundsException =>
        newMonths = null
    }
    if (newMonths == null) {
      if (isLeapYear(year))
        newMonths = DEFAULT_LEAP_MONTH_DAYS
      else
        newMonths = DEFAULT_MONTH_DAYS
    }
    newMonths
  }

  /** Returns adjusted month length as Integer array form the year specified.
    *
    * @param year  a year
    * @return an Integer array
    */
  private def getAdjustedMonthLength(year: Int): Array[Integer] = {
    var newMonths: Array[Integer] = null
    try newMonths = ADJUSTED_MONTH_LENGTHS.get(new Integer(year))
    catch {
      case e: ArrayIndexOutOfBoundsException =>
        newMonths = null
    }
    if (newMonths == null) {
      if (isLeapYear(year))
        newMonths = DEFAULT_LEAP_MONTH_LENGTHS
      else
        newMonths = DEFAULT_MONTH_LENGTHS
    }
    newMonths
  }

  /** Returns day-of-year.
    *
    * @param cycleNumber  a cycle number
    * @param dayOfCycle  day of cycle
    * @param yearInCycle  year in cycle
    * @return day-of-year
    */
  private def getDayOfYear(cycleNumber: Int, dayOfCycle: Int, yearInCycle: Int): Int = {
    val cycles: Array[Integer] = getAdjustedCycle(cycleNumber)
    if (dayOfCycle > 0)
      dayOfCycle - cycles(yearInCycle).intValue
    else
      cycles(yearInCycle).intValue + dayOfCycle
  }

  /** Returns month-of-year. 0-based.
    *
    * @param dayOfYear  day-of-year
    * @param year  a year
    * @return month-of-year
    */
  private def getMonthOfYear(dayOfYear: Int, year: Int): Int = {
    var _dayOfYear = dayOfYear
    val newMonths: Array[Integer] = getAdjustedMonthDays(year)
    if (_dayOfYear >= 0) {

      var i: Int = 0
      while (i < newMonths.length) {
        if (_dayOfYear < newMonths(i).intValue)
          return i - 1
        i += 1
        }
      11
    }
    else {
      _dayOfYear = if (isLeapYear(year)) _dayOfYear + 355 else _dayOfYear + 354

      var i: Int = 0
      while (i < newMonths.length) {
        if (_dayOfYear < newMonths(i).intValue)
          return i - 1
        i += 1
      }
      11
    }
  }

  /** Returns day-of-month.
    *
    * @param dayOfYear  day of  year
    * @param month  month
    * @param year  year
    * @return day-of-month
    */
  private def getDayOfMonth(dayOfYear: Int, month: Int, year: Int): Int = {
    var _dayOfYear = dayOfYear
    val newMonths: Array[Integer] = getAdjustedMonthDays(year)
    if (_dayOfYear >= 0) {
      if (month > 0)
        _dayOfYear - newMonths(month).intValue
      else
        _dayOfYear
    } else {
      _dayOfYear = if (isLeapYear(year)) dayOfYear + 355 else dayOfYear + 354
      if (month > 0)
        _dayOfYear - newMonths(month).intValue
      else
        _dayOfYear
    }
  }

  /** Determines if the given year is a leap year.
    *
    * @param year  year
    * @return true if leap year
    */
  private[chrono] def isLeapYear(year: Long): Boolean = (14 + 11 * (if (year > 0) year else -year)) % 30 < 11

  /** Returns month days from the beginning of year.
    *
    * @param month  month (0-based)
    * @param year  year
    * @return month days from the beginning of year
    */
  private def getMonthDays(month: Int, year: Int): Int = {
    val newMonths: Array[Integer] = getAdjustedMonthDays(year)
    newMonths(month).intValue
  }

  /** Returns month length.
    *
    * @param month  month (0-based)
    * @param year  year
    * @return month length
    */
  private[chrono] def getMonthLength(month: Int, year: Int): Int = {
    val newMonths: Array[Integer] = getAdjustedMonthLength(year)
    newMonths(month).intValue
  }

  /** Returns year length.
    *
    * @param year  year
    * @return year length
    */
  private[chrono] def getYearLength(year: Int): Int = {
    val cycleNumber: Int = (year - 1) / 30
    var cycleYears: Array[Integer] = null
    try cycleYears = ADJUSTED_CYCLE_YEARS.get(cycleNumber)
    catch {
      case e: ArrayIndexOutOfBoundsException =>
        cycleYears = null
    }
    if (cycleYears != null) {
      val yearInCycle: Int = (year - 1) % 30
      if (yearInCycle == 29) {
        return ADJUSTED_CYCLES(cycleNumber + 1).intValue - ADJUSTED_CYCLES(cycleNumber).intValue - cycleYears(yearInCycle).intValue
      }
      cycleYears(yearInCycle + 1).intValue - cycleYears(yearInCycle).intValue
    }
    else {
      if (isLeapYear(year)) 355 else 354
    }
  }

  /** Returns maximum day-of-month.
    *
    * @return maximum day-of-month
    */
  private[chrono] def getMaximumDayOfMonth: Int = ADJUSTED_MAX_VALUES(POSITION_DAY_OF_MONTH)

  /** Returns smallest maximum day-of-month.
    *
    * @return smallest maximum day-of-month
    */
  private[chrono] def getSmallestMaximumDayOfMonth: Int = ADJUSTED_LEAST_MAX_VALUES(POSITION_DAY_OF_MONTH)

  /** Returns maximum day-of-year.
    *
    * @return maximum day-of-year
    */
  private[chrono] def getMaximumDayOfYear: Int = ADJUSTED_MAX_VALUES(POSITION_DAY_OF_YEAR)

  /** Returns smallest maximum day-of-year.
    *
    * @return smallest maximum day-of-year
    */
  private[chrono] def getSmallestMaximumDayOfYear: Int = ADJUSTED_LEAST_MAX_VALUES(POSITION_DAY_OF_YEAR)

  /** Adds deviation definition. The year and month sepcifed should be the
    * caluculated Hijrah year and month. The month is 0 based. e.g. 8 for
    * Ramadan (9th month) Addition of anything minus deviation days is
    * calculated negatively in the case the user wants to subtract days from
    * the calendar. For example, adding -1 days will subtract one day from the
    * current date. Please note that this behavior is different from the
    * addDeviaiton method.
    *
    * @param startYear  start year
    * @param startMonth  start month
    * @param endYear  end year
    * @param endMonth  end month
    * @param offset  offset
    */
  private def addDeviationAsHijrah(startYear: Int, startMonth: Int, endYear: Int, endMonth: Int, offset: Int): Unit = {
    if (startYear < 1)
      throw new IllegalArgumentException("startYear < 1")
    if (endYear < 1)
      throw new IllegalArgumentException("endYear < 1")
    if (startMonth < 0 || startMonth > 11)
      throw new IllegalArgumentException("startMonth < 0 || startMonth > 11")
    if (endMonth < 0 || endMonth > 11)
      throw new IllegalArgumentException("endMonth < 0 || endMonth > 11")
    if (endYear > 9999)
      throw new IllegalArgumentException("endYear > 9999")
    if (endYear < startYear)
      throw new IllegalArgumentException("startYear > endYear")
    if (endYear == startYear && endMonth < startMonth)
      throw new IllegalArgumentException("startYear == endYear && endMonth < startMonth")
    val isStartYLeap: Boolean = isLeapYear(startYear)
    var orgStartMonthNums: Array[Integer] = ADJUSTED_MONTH_DAYS.get(new Integer(startYear))
    if (orgStartMonthNums == null) {
      if (isStartYLeap) {
        orgStartMonthNums = new Array[Integer](LEAP_NUM_DAYS.length)

        {
          var l: Int = 0
          while (l < LEAP_NUM_DAYS.length) {
            orgStartMonthNums(l) = new Integer(LEAP_NUM_DAYS(l))
            l += 1
          }
        }
      }
      else {
        orgStartMonthNums = new Array[Integer](NUM_DAYS.length)

        {
          var l: Int = 0
          while (l < NUM_DAYS.length) {
            orgStartMonthNums(l) = new Integer(NUM_DAYS(l))
            l += 1
          }
        }
      }
    }
    val newStartMonthNums: Array[Integer] = new Array[Integer](orgStartMonthNums.length)

    {
      var month: Int = 0
      while (month < 12) {
        if (month > startMonth)
          newStartMonthNums(month) = new Integer(orgStartMonthNums(month).intValue - offset)
        else
          newStartMonthNums(month) = new Integer(orgStartMonthNums(month).intValue)
        month += 1
      }
    }

    ADJUSTED_MONTH_DAYS.put(new Integer(startYear), newStartMonthNums)
    var orgStartMonthLengths: Array[Integer] = ADJUSTED_MONTH_LENGTHS.get(new Integer(startYear))
    if (orgStartMonthLengths == null) {
      if (isStartYLeap) {
        orgStartMonthLengths = new Array[Integer](LEAP_MONTH_LENGTH.length)

        var l: Int = 0
        while (l < LEAP_MONTH_LENGTH.length) {
          orgStartMonthLengths(l) = new Integer(LEAP_MONTH_LENGTH(l))
          l += 1
        }
      }
      else {
        orgStartMonthLengths = new Array[Integer](MONTH_LENGTH.length)
        var l: Int = 0
        while (l < MONTH_LENGTH.length) {
          orgStartMonthLengths(l) = new Integer(MONTH_LENGTH(l))
          l += 1
        }
      }
    }
    val newStartMonthLengths: Array[Integer] = new Array[Integer](orgStartMonthLengths.length)

    {
      var month: Int = 0
      while (month < 12) {
        if (month == startMonth)
          newStartMonthLengths(month) = new Integer(orgStartMonthLengths(month).intValue - offset)
        else
          newStartMonthLengths(month) = new Integer(orgStartMonthLengths(month).intValue)
        month += 1
      }
    }

    ADJUSTED_MONTH_LENGTHS.put(new Integer(startYear), newStartMonthLengths)
    if (startYear != endYear) {
      val sCycleNumber: Int = (startYear - 1) / 30
      val sYearInCycle: Int = (startYear - 1) % 30
      var startCycles: Array[Integer] = ADJUSTED_CYCLE_YEARS.get(new Integer(sCycleNumber))
      if (startCycles == null) {
        startCycles = new Array[Integer](CYCLEYEAR_START_DATE.length)
        var j: Int = 0
        while (j < startCycles.length) {
          startCycles(j) = new Integer(CYCLEYEAR_START_DATE(j))
          j += 1
        }
      }

      var j: Int = sYearInCycle + 1
      while (j < CYCLEYEAR_START_DATE.length) {
        startCycles(j) = new Integer(startCycles(j).intValue - offset)
        j += 1
      }

      ADJUSTED_CYCLE_YEARS.put(new Integer(sCycleNumber), startCycles)
      val sYearInMaxY: Int = (startYear - 1) / 30
      val sEndInMaxY: Int = (endYear - 1) / 30
      if (sYearInMaxY != sEndInMaxY) {
        {
          var j: Int = sYearInMaxY + 1
          while (j < ADJUSTED_CYCLES.length) {
            ADJUSTED_CYCLES(j) = new java.lang.Long(ADJUSTED_CYCLES(j).longValue - offset)
            j += 1
          }
        }
        {
          var j: Int = sEndInMaxY + 1
          while (j < ADJUSTED_CYCLES.length) {
            ADJUSTED_CYCLES(j) = new java.lang.Long(ADJUSTED_CYCLES(j).longValue + offset)
            j += 1
          }
        }
      }
      val eCycleNumber: Int = (endYear - 1) / 30
      val sEndInCycle: Int = (endYear - 1) % 30
      var endCycles: Array[Integer] = ADJUSTED_CYCLE_YEARS.get(new Integer(eCycleNumber))
      if (endCycles == null) {
        endCycles = new Array[Integer](CYCLEYEAR_START_DATE.length)
        var j: Int = 0
        while (j < endCycles.length) {
          endCycles(j) = new Integer(CYCLEYEAR_START_DATE(j))
          j += 1
        }
      }

      {
        var j: Int = sEndInCycle + 1
        while (j < CYCLEYEAR_START_DATE.length) {
          endCycles(j) = new Integer(endCycles(j).intValue + offset)
          j += 1
        }
      }
      ADJUSTED_CYCLE_YEARS.put(new Integer(eCycleNumber), endCycles)
    }
    val isEndYLeap: Boolean = isLeapYear(endYear)
    var orgEndMonthDays: Array[Integer] = ADJUSTED_MONTH_DAYS.get(new Integer(endYear))
    if (orgEndMonthDays == null) {
      if (isEndYLeap) {
        orgEndMonthDays = new Array[Integer](LEAP_NUM_DAYS.length)
        var l: Int = 0
        while (l < LEAP_NUM_DAYS.length) {
          orgEndMonthDays(l) = new Integer(LEAP_NUM_DAYS(l))
          l += 1
        }
      }
      else {
        orgEndMonthDays = new Array[Integer](NUM_DAYS.length)

        {
          var l: Int = 0
          while (l < NUM_DAYS.length) {
            orgEndMonthDays(l) = new Integer(NUM_DAYS(l))
            l += 1
          }
        }
      }
    }
    val newEndMonthDays: Array[Integer] = new Array[Integer](orgEndMonthDays.length)

    {
      var month: Int = 0
      while (month < 12) {
          if (month > endMonth)
            newEndMonthDays(month) = new Integer(orgEndMonthDays(month).intValue + offset)
          else
            newEndMonthDays(month) = new Integer(orgEndMonthDays(month).intValue)
          month += 1
      }
    }
    ADJUSTED_MONTH_DAYS.put(new Integer(endYear), newEndMonthDays)
    var orgEndMonthLengths: Array[Integer] = ADJUSTED_MONTH_LENGTHS.get(new Integer(endYear))
    if (orgEndMonthLengths == null) {
      if (isEndYLeap) {
        orgEndMonthLengths = new Array[Integer](LEAP_MONTH_LENGTH.length)

        {
          var l: Int = 0
          while (l < LEAP_MONTH_LENGTH.length) {
            orgEndMonthLengths(l) = new Integer(LEAP_MONTH_LENGTH(l))
            l += 1
          }
        }
      }
      else {
        orgEndMonthLengths = new Array[Integer](MONTH_LENGTH.length)

        {
          var l: Int = 0
          while (l < MONTH_LENGTH.length) {
            orgEndMonthLengths(l) = new Integer(MONTH_LENGTH(l))
            l += 1; l - 1
          }
        }
      }
    }
    val newEndMonthLengths: Array[Integer] = new Array[Integer](orgEndMonthLengths.length)

    {
      var month: Int = 0
      while (month < 12) {
        if (month == endMonth)
          newEndMonthLengths(month) = new Integer(orgEndMonthLengths(month).intValue + offset)
        else
          newEndMonthLengths(month) = new Integer(orgEndMonthLengths(month).intValue)
        month += 1
      }
    }
    ADJUSTED_MONTH_LENGTHS.put(new Integer(endYear), newEndMonthLengths)
    val startMonthLengths: Array[Integer] = ADJUSTED_MONTH_LENGTHS.get(new Integer(startYear))
    val endMonthLengths: Array[Integer] = ADJUSTED_MONTH_LENGTHS.get(new Integer(endYear))
    val startMonthDays: Array[Integer] = ADJUSTED_MONTH_DAYS.get(new Integer(startYear))
    val endMonthDays: Array[Integer] = ADJUSTED_MONTH_DAYS.get(new Integer(endYear))
    val startMonthLength: Int = startMonthLengths(startMonth).intValue
    val endMonthLength: Int = endMonthLengths(endMonth).intValue
    val startMonthDay: Int = startMonthDays(11).intValue + startMonthLengths(11).intValue
    val endMonthDay: Int = endMonthDays(11).intValue + endMonthLengths(11).intValue
    var maxMonthLength: Int = ADJUSTED_MAX_VALUES(POSITION_DAY_OF_MONTH).intValue
    var leastMaxMonthLength: Int = ADJUSTED_LEAST_MAX_VALUES(POSITION_DAY_OF_MONTH).intValue
    if (maxMonthLength < startMonthLength) {
      maxMonthLength = startMonthLength
    }
    if (maxMonthLength < endMonthLength) {
      maxMonthLength = endMonthLength
    }
    ADJUSTED_MAX_VALUES(POSITION_DAY_OF_MONTH) = new Integer(maxMonthLength)
    if (leastMaxMonthLength > startMonthLength) {
      leastMaxMonthLength = startMonthLength
    }
    if (leastMaxMonthLength > endMonthLength) {
      leastMaxMonthLength = endMonthLength
    }
    ADJUSTED_LEAST_MAX_VALUES(POSITION_DAY_OF_MONTH) = new Integer(leastMaxMonthLength)
    var maxMonthDay: Int = ADJUSTED_MAX_VALUES(POSITION_DAY_OF_YEAR).intValue
    var leastMaxMonthDay: Int = ADJUSTED_LEAST_MAX_VALUES(POSITION_DAY_OF_YEAR).intValue
    if (maxMonthDay < startMonthDay) {
      maxMonthDay = startMonthDay
    }
    if (maxMonthDay < endMonthDay) {
      maxMonthDay = endMonthDay
    }
    ADJUSTED_MAX_VALUES(POSITION_DAY_OF_YEAR) = new Integer(maxMonthDay)
    if (leastMaxMonthDay > startMonthDay) {
      leastMaxMonthDay = startMonthDay
    }
    if (leastMaxMonthDay > endMonthDay) {
      leastMaxMonthDay = endMonthDay
    }
    ADJUSTED_LEAST_MAX_VALUES(POSITION_DAY_OF_YEAR) = new Integer(leastMaxMonthDay)
  }

  /** Read hijrah_deviation.cfg file. The config file contains the deviation data with
    * following format.
    *
    * StartYear/StartMonth(0-based)-EndYear/EndMonth(0-based):Deviation day (1,
    * 2, -1, or -2)
    *
    * Line separator or ";" is used for the separator of each deviation data.
    *
    * Here is the example.
    *
    * 1429/0-1429/1:1
    * 1429/2-1429/7:1;1429/6-1429/11:1
    * 1429/11-9999/11:1
    *
    * @throws IOException for zip/jar file handling exception.
    * @throws ParseException if the format of the configuration file is wrong.
    */
  @throws[IOException]
  @throws[ParseException]
  private def readDeviationConfig(): Unit = {
    val is: InputStream = getConfigFileInputStream
    if (is != null) {
      var br: BufferedReader = null
      try {
        br = new BufferedReader(new InputStreamReader(is))
        var line: String = ""
        var num: Int = 0
        while ({line = br.readLine; line} != null) {
          num += 1
          line = line.trim
          parseLine(line, num)
        }
      } finally {
        if (br != null)
          br.close()
      }
    }
  }

  /** Parse each deviation element.
    *
    * @param line  a line to parse
    * @param num  line number
    * @throws ParseException if line has incorrect format.
    */
  @throws[ParseException]
  private def parseLine(line: String, num: Int): Unit = {
    val st: StringTokenizer = new StringTokenizer(line, ";")
    while (st.hasMoreTokens) {
      val deviationElement: String = st.nextToken
      val offsetIndex: Int = deviationElement.indexOf(':')
      if (offsetIndex != -1) {
        val offsetString: String = deviationElement.substring(offsetIndex + 1, deviationElement.length)
        var offset: Int = 0
        try offset = offsetString.toInt
        catch {
          case ex: NumberFormatException =>
            throw new ParseException(s"Offset is not properly set at line $num.", num)
        }
        val separatorIndex: Int = deviationElement.indexOf('-')
        if (separatorIndex != -1) {
          val startDateStg: String = deviationElement.substring(0, separatorIndex)
          val endDateStg: String = deviationElement.substring(separatorIndex + 1, offsetIndex)
          val startDateYearSepIndex: Int = startDateStg.indexOf('/')
          val endDateYearSepIndex: Int = endDateStg.indexOf('/')
          var startYear: Int = -1
          var endYear: Int = -1
          var startMonth: Int = -1
          var endMonth: Int = -1
          if (startDateYearSepIndex != -1) {
            val startYearStg: String = startDateStg.substring(0, startDateYearSepIndex)
            val startMonthStg: String = startDateStg.substring(startDateYearSepIndex + 1, startDateStg.length)
            try startYear = startYearStg.toInt
            catch {
              case ex: NumberFormatException =>
                throw new ParseException(s"Start year is not properly set at line $num.", num)
            }
            try startMonth = startMonthStg.toInt
            catch {
              case ex: NumberFormatException =>
                throw new ParseException(s"Start month is not properly set at line $num.", num)
            }
          }
          else
            throw new ParseException(s"Start year/month has incorrect format at line $num.", num)
          if (endDateYearSepIndex != -1) {
            val endYearStg: String = endDateStg.substring(0, endDateYearSepIndex)
            val endMonthStg: String = endDateStg.substring(endDateYearSepIndex + 1, endDateStg.length)
            try endYear = endYearStg.toInt
            catch {
              case ex: NumberFormatException =>
                throw new ParseException(s"End year is not properly set at line $num.", num)
            }
            try {
              endMonth = endMonthStg.toInt
            }
            catch {
              case ex: NumberFormatException =>
                throw new ParseException(s"End month is not properly set at line $num.", num)
            }
          }
          else
            throw new ParseException(s"End year/month has incorrect format at line $num.", num)
          if (startYear != -1 && startMonth != -1 && endYear != -1 && endMonth != -1)
            addDeviationAsHijrah(startYear, startMonth, endYear, endMonth, offset)
          else
            throw new ParseException(s"Unknown error at line $num.", num)
        }
        else
          throw new ParseException(s"Start and end year/month has incorrect format at line $num.", num)
      }
      else
        throw new ParseException(s"Offset has incorrect format at line $num.", num)
    }
  }

  /** Return InputStream for deviation configuration file.
    * The default location of the deviation file is:
    * <pre>
    * $CLASSPATH/org/threeten/bp/chrono
    * </pre>
    * And the default file name is:
    * <pre>
    * hijrah_deviation.cfg
    * </pre>
    * The default location and file name can be overriden by setting
    * following two Java's system property.
    * <pre>
    * Location: java.time.i18n.HijrahDate.deviationConfigDir
    * File name: java.time.i18n.HijrahDate.deviationConfigFile
    * </pre>
    * Regarding the file format, see readDeviationConfig() method for details.
    *
    * @return InputStream for file reading exception.
    * @throws IOException for zip/jar file handling exception.
    */
  @throws[IOException]
  private def getConfigFileInputStream: InputStream = {
    var fileName: String = System.getProperty("java.time.i18n.HijrahDate.deviationConfigFile")
    if (fileName == null)
      fileName = DEFAULT_CONFIG_FILENAME
    var dir: String = System.getProperty("java.time.i18n.HijrahDate.deviationConfigDir")
    if (dir != null) {
      if (!(dir.length == 0 && dir.endsWith(System.getProperty("file.separator"))))
        dir = dir + System.getProperty("file.separator")
      val file: File = new File(dir + FILE_SEP + fileName)
      if (file.exists) {
        try new FileInputStream(file)
        catch {
          case ioe: IOException =>
            throw ioe
        }
      }
      else null
    }
    else {
      val classPath: String = System.getProperty("java.class.path")
      val st: StringTokenizer = new StringTokenizer(classPath, PATH_SEP)
      while (st.hasMoreTokens) {
        val path: String = st.nextToken
        val file: File = new File(path)
        if (file.exists) {
          if (file.isDirectory) {
            val f: File = new File(path + FILE_SEP + DEFAULT_CONFIG_PATH, fileName)
            if (f.exists) {
              try return new FileInputStream(path + FILE_SEP + DEFAULT_CONFIG_PATH + FILE_SEP + fileName)
              catch {
                case ioe: IOException => throw ioe
              }
            }
          }
          else {
            var zip: ZipFile = null
            try zip = new ZipFile(file)
            catch {
              case ioe: IOException => zip = null
            }
            if (zip != null) {
              var targetFile: String = DEFAULT_CONFIG_PATH + FILE_SEP + fileName
              var entry: ZipEntry = zip.getEntry(targetFile)
              if (entry == null) {
                if (FILE_SEP == '/')
                  targetFile = targetFile.replace('/', '\\')
                else if (FILE_SEP == '\\')
                  targetFile = targetFile.replace('\\', '/')
                entry = zip.getEntry(targetFile)
              }
              if (entry != null) {
                try return zip.getInputStream(entry)
                catch {
                  case ioe: IOException => throw ioe
                }
              }
            }
          }
        }
      }
      null
    }
  }

  @throws[IOException]
  private[chrono] def readExternal(in: DataInput): ChronoLocalDate = {
    val year: Int = in.readInt
    val month: Int = in.readByte
    val dayOfMonth: Int = in.readByte
    HijrahChronology.INSTANCE.date(year, month, dayOfMonth)
  }
}

/** A date in the Hijrah calendar system.
  *
  * This implements {@code ChronoLocalDate} for the {@link HijrahChronology Hijrah calendar}.
  *
  * The Hijrah calendar has a different total of days in a year than
  * Gregorian calendar, and a month is based on the period of a complete
  * revolution of the moon around the earth (as between successive new moons).
  * The calendar cycles becomes longer and unstable, and sometimes a manual
  * adjustment (for entering deviation) is necessary for correctness
  * because of the complex algorithm.
  *
  * HijrahDate supports the manual adjustment feature by providing a configuration
  * file. The configuration file contains the adjustment (deviation) data with following format.
  * <pre>
  * StartYear/StartMonth(0-based)-EndYear/EndMonth(0-based):Deviation day (1, 2, -1, or -2)
  * Line separator or ";" is used for the separator of each deviation data.</pre>
  * Here is the example.
  * <pre>
  * 1429/0-1429/1:1
  * 1429/2-1429/7:1;1429/6-1429/11:1
  * 1429/11-9999/11:1</pre>
  * The default location of the configuration file is:
  * <pre>
  * $CLASSPATH/org/threeten/bp/chrono</pre>
  * And the default file name is:
  * <pre>
  * hijrah_deviation.cfg</pre>
  * The default location and file name can be overriden by setting
  * following two Java's system property.
  * <pre>
  * Location: java.time.i18n.HijrahDate.deviationConfigDir
  * File name: java.time.i18n.HijrahDate.deviationConfigFile</pre>
  *
  * <h3>Specification for implementors</h3>
  * This class is immutable and thread-safe.
  *
  * @constructor Constructs an instance with the specified date.
  * @param gregorianEpochDay  the number of days from 0001/01/01 (Gregorian), caller calculated
  */
@SerialVersionUID(-5207853542612002020L)
final class HijrahDate private (private val gregorianEpochDay: Long) extends ChronoDateImpl[HijrahDate] with Serializable {
  /** The era.
    */
  @transient
  private var era: HijrahEra = null
  /** The year.
    */
  @transient
  private var yearOfEra: Int = 0
  /** The month-of-year.
    */
  @transient
  private var monthOfYear: Int = 0
  /** The day-of-month.
    */
  @transient
  private var dayOfMonth: Int = 0
  /** The day-of-year.
    */
  @transient
  private var dayOfYear: Int = 0
  /** The day-of-week.
    */
  @transient
  private var dayOfWeek: DayOfWeek = null
  /** True if year is leap year.
    */
  @transient
  private var _isLeapYear: Boolean = false

  { /// FIXME
    val dateInfo: Array[Int] = HijrahDate.getHijrahDateInfo(gregorianEpochDay)
    HijrahDate.checkValidYearOfEra(dateInfo(1))
    HijrahDate.checkValidMonth(dateInfo(2))
    HijrahDate.checkValidDayOfMonth(dateInfo(3))
    HijrahDate.checkValidDayOfYear(dateInfo(4))
    this.era = HijrahEra.of(dateInfo(0))
    this.yearOfEra = dateInfo(1)
    this.monthOfYear = dateInfo(2)
    this.dayOfMonth = dateInfo(3)
    this.dayOfYear = dateInfo(4)
    this.dayOfWeek = DayOfWeek.of(dateInfo(5))
    this._isLeapYear = HijrahDate.isLeapYear(this.yearOfEra)
  }

  /** Replaces the date instance from the stream with a valid one.
    *
    * @return the resolved date, never null
    */
  private def readResolve: AnyRef = new HijrahDate(this.gregorianEpochDay)

  def getChronology: HijrahChronology = HijrahChronology.INSTANCE

  override def getEra: HijrahEra = this.era

  override def range(field: TemporalField): ValueRange = {
    if (field.isInstanceOf[ChronoField]) {
      if (isSupported(field)) {
        val f: ChronoField = field.asInstanceOf[ChronoField]
        f match {
          case DAY_OF_MONTH          => ValueRange.of(1, lengthOfMonth)
          case DAY_OF_YEAR           => ValueRange.of(1, lengthOfYear)
          case ALIGNED_WEEK_OF_MONTH => ValueRange.of(1, 5)
          case YEAR_OF_ERA           => ValueRange.of(1, 1000)
          case _                     => getChronology.range(f)
        }
      } else {
        throw new UnsupportedTemporalTypeException(s"Unsupported field: $field")
      }
    } else {
      field.rangeRefinedBy(this)
    }
  }

  def getLong(field: TemporalField): Long = {
    field match {
      case chronoField: ChronoField =>
        chronoField match {
          case DAY_OF_WEEK                  => dayOfWeek.getValue
          case ALIGNED_DAY_OF_WEEK_IN_MONTH => ((dayOfWeek.getValue - 1) % 7) + 1
          case ALIGNED_DAY_OF_WEEK_IN_YEAR  => ((dayOfYear - 1) % 7) + 1
          case DAY_OF_MONTH                 => this.dayOfMonth
          case DAY_OF_YEAR                  => this.dayOfYear
          case EPOCH_DAY                    => toEpochDay
          case ALIGNED_WEEK_OF_MONTH        => ((dayOfMonth - 1) / 7) + 1
          case ALIGNED_WEEK_OF_YEAR         => ((dayOfYear - 1) / 7) + 1
          case MONTH_OF_YEAR                => monthOfYear
          case YEAR_OF_ERA                  => yearOfEra
          case YEAR                         => yearOfEra
          case ERA                          => era.getValue
          case _                            => throw new UnsupportedTemporalTypeException(s"Unsupported field: $field")
        }
      case _ => field.getFrom(this)
    }

  }

  override def `with`(adjuster: TemporalAdjuster): HijrahDate = super.`with`(adjuster).asInstanceOf[HijrahDate]

  def `with`(field: TemporalField, newValue: Long): HijrahDate =
    if (field.isInstanceOf[ChronoField]) {
      val f: ChronoField = field.asInstanceOf[ChronoField]
      f.checkValidValue(newValue)
      val nvalue: Int = newValue.toInt
      f match {
        case DAY_OF_WEEK                  => plusDays(newValue - dayOfWeek.getValue)
        case ALIGNED_DAY_OF_WEEK_IN_MONTH => plusDays(newValue - getLong(ALIGNED_DAY_OF_WEEK_IN_MONTH))
        case ALIGNED_DAY_OF_WEEK_IN_YEAR  => plusDays(newValue - getLong(ALIGNED_DAY_OF_WEEK_IN_YEAR))
        case DAY_OF_MONTH                 => HijrahDate.resolvePreviousValid(yearOfEra, monthOfYear, nvalue)
        case DAY_OF_YEAR                  => HijrahDate.resolvePreviousValid(yearOfEra, ((nvalue - 1) / 30) + 1, ((nvalue - 1) % 30) + 1)
        case EPOCH_DAY                    => new HijrahDate(nvalue)
        case ALIGNED_WEEK_OF_MONTH        => plusDays((newValue - getLong(ALIGNED_WEEK_OF_MONTH)) * 7)
        case ALIGNED_WEEK_OF_YEAR         => plusDays((newValue - getLong(ALIGNED_WEEK_OF_YEAR)) * 7)
        case MONTH_OF_YEAR                => HijrahDate.resolvePreviousValid(yearOfEra, nvalue, dayOfMonth)
        case YEAR_OF_ERA                  => HijrahDate.resolvePreviousValid(if (yearOfEra >= 1) nvalue else 1 - nvalue, monthOfYear, dayOfMonth)
        case YEAR                         => HijrahDate.resolvePreviousValid(nvalue, monthOfYear, dayOfMonth)
        case ERA                          => HijrahDate.resolvePreviousValid(1 - yearOfEra, monthOfYear, dayOfMonth)
        case _                            => throw new UnsupportedTemporalTypeException(s"Unsupported field: $field")
      }
    } else {
      field.adjustInto(this, newValue)
    }

  override def plus(amount: TemporalAmount): HijrahDate =
    super.plus(amount).asInstanceOf[HijrahDate]

  override def plus(amountToAdd: Long, unit: TemporalUnit): HijrahDate =
    super.plus(amountToAdd, unit).asInstanceOf[HijrahDate]

  override def minus(amount: TemporalAmount): HijrahDate =
    super.minus(amount).asInstanceOf[HijrahDate]

  override def minus(amountToAdd: Long, unit: TemporalUnit): HijrahDate =
    super.minus(amountToAdd, unit).asInstanceOf[HijrahDate]

  override def atTime(localTime: LocalTime): ChronoLocalDateTime[HijrahDate] =
    super.atTime(localTime).asInstanceOf[ChronoLocalDateTime[HijrahDate]]

  override def toEpochDay: Long = HijrahDate.getGregorianEpochDay(yearOfEra, monthOfYear, dayOfMonth)

  /** Checks if the year is a leap year, according to the Hijrah calendar system rules.
    *
    * @return true if this date is in a leap year
    */
  override def isLeapYear: Boolean = this._isLeapYear

  private[chrono] def plusYears(years: Long): HijrahDate =
    if (years == 0)
      this
    else {
      val newYear: Int = Math.addExact(this.yearOfEra, years.toInt)
      HijrahDate.of(this.era, newYear, this.monthOfYear, this.dayOfMonth)
    }

  private[chrono] def plusMonths(months: Long): HijrahDate = {
    if (months == 0)
      return this
    var newMonth: Int = this.monthOfYear - 1
    newMonth = newMonth + months.toInt
    var years: Int = newMonth / 12
    newMonth = newMonth % 12
    while (newMonth < 0) {
      newMonth += 12
      years = Math.subtractExact(years, 1)
    }
    val newYear: Int = Math.addExact(this.yearOfEra, years)
    HijrahDate.of(this.era, newYear, newMonth + 1, this.dayOfMonth)
  }

  private[chrono] def plusDays(days: Long): HijrahDate = new HijrahDate(this.gregorianEpochDay + days)

  def lengthOfMonth: Int = HijrahDate.getMonthLength(monthOfYear - 1, yearOfEra)

  override def lengthOfYear: Int = HijrahDate.getYearLength(yearOfEra)

  private def writeReplace: AnyRef = new Ser(Ser.HIJRAH_DATE_TYPE, this)

  @throws[IOException]
  private[chrono] def writeExternal(out: DataOutput): Unit = {
    out.writeInt(get(YEAR))
    out.writeByte(get(MONTH_OF_YEAR))
    out.writeByte(get(DAY_OF_MONTH))
  }
}
