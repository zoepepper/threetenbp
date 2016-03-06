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
package org.threeten.bp

import java.io.DataInput
import java.io.DataOutput
import java.io.Externalizable
import java.io.IOException
import java.io.InvalidClassException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.StreamCorruptedException

@SerialVersionUID(-7683839454370182990L)
private object Ser {
  private[bp] val DURATION_TYPE: Byte = 1
  private[bp] val INSTANT_TYPE: Byte = 2
  private[bp] val LOCAL_DATE_TYPE: Byte = 3
  private[bp] val LOCAL_DATE_TIME_TYPE: Byte = 4
  private[bp] val LOCAL_TIME_TYPE: Byte = 5
  private[bp] val ZONED_DATE_TIME_TYPE: Byte = 6
  private[bp] val ZONE_REGION_TYPE: Byte = 7
  private[bp] val ZONE_OFFSET_TYPE: Byte = 8
  private[bp] val MONTH_DAY_TYPE: Byte = 64
  private[bp] val OFFSET_TIME_TYPE: Byte = 66
  private[bp] val YEAR_TYPE: Byte = 67
  private[bp] val YEAR_MONTH_TYPE: Byte = 68
  private[bp] val OFFSET_DATE_TIME_TYPE: Byte = 69

  @throws[IOException]
  private[bp] def writeInternal(`type`: Byte, `object`: AnyRef, out: DataOutput): Unit = {
    out.writeByte(`type`)
    `type` match {
      case DURATION_TYPE         => `object`.asInstanceOf[Duration].writeExternal(out)
      case INSTANT_TYPE          => `object`.asInstanceOf[Instant].writeExternal(out)
      case LOCAL_DATE_TYPE       => `object`.asInstanceOf[LocalDate].writeExternal(out)
      case LOCAL_DATE_TIME_TYPE  => `object`.asInstanceOf[LocalDateTime].writeExternal(out)
      case LOCAL_TIME_TYPE       => `object`.asInstanceOf[LocalTime].writeExternal(out)
      case MONTH_DAY_TYPE        => `object`.asInstanceOf[MonthDay].writeExternal(out)
      case OFFSET_DATE_TIME_TYPE => `object`.asInstanceOf[OffsetDateTime].writeExternal(out)
      case OFFSET_TIME_TYPE      => `object`.asInstanceOf[OffsetTime].writeExternal(out)
      case YEAR_MONTH_TYPE       => `object`.asInstanceOf[YearMonth].writeExternal(out)
      case YEAR_TYPE             => `object`.asInstanceOf[Year].writeExternal(out)
      case ZONE_REGION_TYPE      => `object`.asInstanceOf[ZoneRegion].writeExternal(out)
      case ZONE_OFFSET_TYPE      => `object`.asInstanceOf[ZoneOffset].writeExternal(out)
      case ZONED_DATE_TIME_TYPE  => `object`.asInstanceOf[ZonedDateTime].writeExternal(out)
      case _                     => throw new InvalidClassException("Unknown serialized type")
    }
  }

  @throws[IOException]
  private[bp] def read(in: DataInput): AnyRef = {
    val `type`: Byte = in.readByte
    readInternal(`type`, in)
  }

  @throws[IOException]
  private def readInternal(`type`: Byte, in: DataInput): AnyRef = {
    `type` match {
      case DURATION_TYPE         => Duration.readExternal(in)
      case INSTANT_TYPE          => Instant.readExternal(in)
      case LOCAL_DATE_TYPE       => LocalDate.readExternal(in)
      case LOCAL_DATE_TIME_TYPE  => LocalDateTime.readExternal(in)
      case LOCAL_TIME_TYPE       => LocalTime.readExternal(in)
      case MONTH_DAY_TYPE        => MonthDay.readExternal(in)
      case OFFSET_DATE_TIME_TYPE => OffsetDateTime.readExternal(in)
      case OFFSET_TIME_TYPE      => OffsetTime.readExternal(in)
      case YEAR_TYPE             => Year.readExternal(in)
      case YEAR_MONTH_TYPE       => YearMonth.readExternal(in)
      case ZONED_DATE_TIME_TYPE  => ZonedDateTime.readExternal(in)
      case ZONE_OFFSET_TYPE      => ZoneOffset.readExternal(in)
      case ZONE_REGION_TYPE      => ZoneRegion.readExternal(in)
      case _                     => throw new StreamCorruptedException("Unknown serialized type")
    }
  }
}

/** The shared serialization delegate for this package.
  *
  * <h4>Implementation notes</h4>
  * This class wraps the object being serialized, and takes a byte representing the type of the class to
  * be serialized.  This byte can also be used for versioning the serialization format.  In this case another
  * byte flag would be used in order to specify an alternative version of the type format.
  * For example {@code LOCAL_DATE_TYPE_VERSION_2 = 21}.
  *
  * In order to serialise the object it writes its byte and then calls back to the appropriate class where
  * the serialisation is performed.  In order to deserialise the object it read in the type byte, switching
  * in order to select which class to call back into.
  *
  * The serialisation format is determined on a per class basis.  In the case of field based classes each
  * of the fields is written out with an appropriate size format in descending order of the field's size.  For
  * example in the case of {@link LocalDate} year is written before month.  Composite classes, such as
  * {@link LocalDateTime} are serialised as one object.
  *
  * This class is mutable and should be created once per serialization.
  *
  * @constructor Creates an instance for serialization.
  *
  * @param type  the type being serialized
  * @param object  the object being serialized
  */
@SerialVersionUID(-7683839454370182990L)
final class Ser private[bp](private var `type`: Byte, private var `object`: AnyRef) extends Externalizable {

  /** Constructor for deserialization. */
  def this() {
    this(0, null)
  }

  /** Implements the {@code Externalizable} interface to write the object.
    *
    * @param out  the data stream to write to, not null
    */
  @throws[IOException]
  def writeExternal(out: ObjectOutput): Unit = Ser.writeInternal(`type`, `object`, out)

  /** Implements the {@code Externalizable} interface to read the object.
    *
    * @param in  the data to read, not null
    */
  @throws[IOException]
  def readExternal(in: ObjectInput): Unit = {
    `type` = in.readByte
    `object` = Ser.readInternal(`type`, in)
  }

  /** Returns the object that will replace this one.
    *
    * @return the read object, should never be null
    */
  private def readResolve: AnyRef = `object`
}