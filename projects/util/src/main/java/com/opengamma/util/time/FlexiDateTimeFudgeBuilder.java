/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.FudgeDateTime;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.Temporal;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code FlexiDateTime}.
 */
@FudgeBuilderFor(FlexiDateTime.class)
public final class FlexiDateTimeFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<FlexiDateTime> {

  /** Field name. */
  public static final String DATETIME_FIELD_NAME = "datetime";
  /** Field name. */
  public static final String ZONE_FIELD_NAME = "zone";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FlexiDateTime object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Performs the serialization, returning null if the object is null.
   *
   * @param serializer  the serializer
   * @param object  the object
   * @return  a message
   */
  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final FlexiDateTime object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Adds the serialized object to a message.
   *
   * @param serializer  the serializer
   * @param object  the object
   * @param msg  the message
   */
  public static void toFudgeMsg(final FudgeSerializer serializer, final FlexiDateTime object, final MutableFudgeMsg msg) {
    Temporal best = object.toBest();
    best = best instanceof ZonedDateTime ? ((ZonedDateTime) best).toOffsetDateTime() : best;
    addToMessage(msg, DATETIME_FIELD_NAME, best);
    final ZoneId zone = object.getZone();
    if (zone != null && !(zone instanceof ZoneOffset)) {
      addToMessage(msg, ZONE_FIELD_NAME, zone);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public FlexiDateTime buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  /**
   * Deserializes a message, returning null if the message is null.
   *
   * @param deserializer  the deserializer
   * @param msg  the message
   * @return  a flexi date time
   */
  public static FlexiDateTime fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    final ZoneId zone = msg.getValue(ZoneId.class, ZONE_FIELD_NAME);
    final Object obj = msg.getValue(DATETIME_FIELD_NAME);
    if (obj instanceof FudgeDateTime) {
      final FudgeDateTime fudge = (FudgeDateTime) obj;
      if (fudge.getTime().hasTimezoneOffset()) {
        final OffsetDateTime odt = fudge.toOffsetDateTime();
        if (zone != null) {
          return FlexiDateTime.of(odt.atZoneSameInstant(zone));
        }
        return FlexiDateTime.of(odt);
      }
      return FlexiDateTime.of(fudge.toLocalDateTime());
    } else if (obj instanceof FudgeDate) {
      final FudgeDate fudge = (FudgeDate) obj;
      return FlexiDateTime.of(fudge.toLocalDate());
    } else if (obj instanceof OffsetDateTime) {
      final OffsetDateTime odt = (OffsetDateTime) obj;
      if (zone != null) {
        return FlexiDateTime.of(odt.atZoneSameInstant(zone));
      }
      return FlexiDateTime.of(odt);
    } else if (obj instanceof LocalDateTime) {
      return FlexiDateTime.of((LocalDateTime) obj);
    } else if (obj instanceof LocalDate) {
      return FlexiDateTime.of((LocalDate) obj);
    } else {
      throw new IllegalStateException("Fudge message did not contain a valid date-time");
    }
  }

}
