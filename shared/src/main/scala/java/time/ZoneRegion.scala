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

import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.io.InvalidObjectException
import java.io.ObjectStreamException
import java.io.Serializable
import java.util.Objects
import java.util.regex.Pattern
import org.threeten.bp.zone.ZoneRules
import org.threeten.bp.zone.ZoneRulesException
import org.threeten.bp.zone.ZoneRulesProvider
import scala.annotation.meta.field

@SerialVersionUID(8386373296231747096L)
private object ZoneRegion {
  /** The regex pattern for region IDs. */
  private val PATTERN: Pattern = Pattern.compile("[A-Za-z][A-Za-z0-9~/._+-]+")

  /** Obtains an instance of {@code ZoneRegion} from an identifier without checking
    * if the time-zone has available rules.
    *
    * This method parses the ID and applies any appropriate normalization.
    * It does not validate the ID against the known set of IDsfor which rules are available.
    *
    * This method is intended for advanced use cases.
    * For example, consider a system that always retrieves time-zone rules from a remote server.
    * Using this factory would allow a {@code ZoneRegion}, and thus a {@code ZonedDateTime},
    * to be created without loading the rules from the remote server.
    *
    * @param zoneId  the time-zone ID, not null
    * @return the zone ID, not null
    * @throws DateTimeException if the ID format is invalid
    */
  private def ofLenient(zoneId: String): ZoneRegion = {
    if ((zoneId == "Z") || zoneId.startsWith("+") || zoneId.startsWith("-"))
      throw new DateTimeException(s"Invalid ID for region-based ZoneId, invalid format: $zoneId")
    if ((zoneId == "UTC") || (zoneId == "GMT") || (zoneId == "UT"))
      return new ZoneRegion(zoneId, ZoneOffset.UTC.getRules)
    if (zoneId.startsWith("UTC+") || zoneId.startsWith("GMT+") || zoneId.startsWith("UTC-") || zoneId.startsWith("GMT-")) {
      val offset: ZoneOffset = ZoneOffset.of(zoneId.substring(3))
      if (offset.getTotalSeconds == 0)
        return new ZoneRegion(zoneId.substring(0, 3), offset.getRules)
      return new ZoneRegion(zoneId.substring(0, 3) + offset.getId, offset.getRules)
    }
    if (zoneId.startsWith("UT+") || zoneId.startsWith("UT-")) {
      val offset: ZoneOffset = ZoneOffset.of(zoneId.substring(2))
      if (offset.getTotalSeconds == 0)
        return new ZoneRegion("UT", offset.getRules)
      return new ZoneRegion(s"UT${offset.getId}", offset.getRules)
    }
    ofId(zoneId, false)
  }

  /** Obtains an instance of {@code ZoneId} from an identifier.
    *
    * @param zoneId  the time-zone ID, not null
    * @param checkAvailable  whether to check if the zone ID is available
    * @return the zone ID, not null
    * @throws DateTimeException if the ID format is invalid
    * @throws DateTimeException if checking availability and the ID cannot be found
    */
  private[bp] def ofId(zoneId: String, checkAvailable: Boolean): ZoneRegion = {
    Objects.requireNonNull(zoneId, "zoneId")
    if (zoneId.length < 2 || !PATTERN.matcher(zoneId).matches)
      throw new DateTimeException(s"Invalid ID for region-based ZoneId, invalid format: $zoneId")
    var rules: ZoneRules = null
    try rules = ZoneRulesProvider.getRules(zoneId, true)
    catch {
      case ex: ZoneRulesException =>
        if (zoneId == "GMT0")
          rules = ZoneOffset.UTC.getRules
        else if (checkAvailable)
          throw ex
    }
    new ZoneRegion(zoneId, rules)
  }

  @throws[IOException]
  private[bp] def readExternal(in: DataInput): ZoneId = {
    val id: String = in.readUTF
    ofLenient(id)
  }
}

/** A geographical region where the same time-zone rules apply.
  *
  * Time-zone information is categorized as a set of rules defining when and
  * how the offset from UTC/Greenwich changes. These rules are accessed using
  * identifiers based on geographical regions, such as countries or states.
  * The most common region classification is the Time Zone Database (TZDB),
  * which defines regions such as 'Europe/Paris' and 'Asia/Tokyo'.
  *
  * The region identifier, modeled by this class, is distinct from the
  * underlying rules, modeled by {@link ZoneRules}.
  * The rules are defined by governments and change frequently.
  * By contrast, the region identifier is well-defined and long-lived.
  * This separation also allows rules to be shared between regions if appropriate.
  *
  * <h3>Specification for implementors</h3>
  * This class is immutable and thread-safe.
  *
  * @constructor
  * @param id  the time-zone ID, not null
  * @param rules  the rules, null for lazy lookup
  */
@SerialVersionUID(8386373296231747096L)
final class ZoneRegion private[bp](private val id: String, @(transient @field) private val rules: ZoneRules) extends ZoneId with Serializable {

  def getId: String = id

  def getRules: ZoneRules = if (rules != null) rules else ZoneRulesProvider.getRules(id, false)

  private def writeReplace: AnyRef = new Ser(Ser.ZONE_REGION_TYPE, this)

  /** Defend against malicious streams.
    *
    * @return never
    * @throws InvalidObjectException always
    */
  @throws[ObjectStreamException]
  private def readResolve: AnyRef = throw new InvalidObjectException("Deserialization via serialization delegate")

  @throws[IOException]
  private[bp] def write(out: DataOutput): Unit = {
    out.writeByte(Ser.ZONE_REGION_TYPE)
    writeExternal(out)
  }

  @throws[IOException]
  private[bp] def writeExternal(out: DataOutput): Unit = out.writeUTF(id)
}