/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import org.fudgemsg.types.DateTimeAccuracy;
import org.fudgemsg.types.FudgeDate;
import org.fudgemsg.types.FudgeDateTime;
import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.FudgeTime;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code Expiry}.
 */
@FudgeBuilderFor(Expiry.class)
public final class ExpiryFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<Expiry> {

  /** Field name. */
  public static final String DATETIME_FIELD_NAME = "datetime";
  /** Field name. */
  public static final String TIMEZONE_FIELD_NAME = "timezone";
  /** Field name. */
  public static final String EXPIRY_ACCURACY_NAME = "accuracy";

  /**
   * Dummy secondary type to force serialization.
   */
  @FudgeSecondaryType
  public static final SecondaryFieldType<Expiry, FudgeMsg> SECONDARY_TYPE_INSTANCE =
       new SecondaryFieldType<Expiry, FudgeMsg>(FudgeWireType.SUB_MESSAGE, Expiry.class) {
    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    @Override
    public FudgeMsg secondaryToPrimary(final Expiry object) {
      throw new OpenGammaRuntimeException("Expiry should be serialized, not added directly to a Fudge message");
    }

    @Override
    public Expiry primaryToSecondary(final FudgeMsg message) {
      return fromFudgeMsg(null, message);
    }
  };

  /**
   * Converts an expiry to a Fudge date time.
   *
   * @param object  the expiry
   * @return  the Fudge date time
   */
  protected static FudgeDateTime expiryToDateTime(final Expiry object) {
    ExpiryAccuracy accuracy = object.getAccuracy();
    if (accuracy == null) {
      accuracy = ExpiryAccuracy.DAY_MONTH_YEAR;
    }
    switch (accuracy) {
      case MIN_HOUR_DAY_MONTH_YEAR:
        return new FudgeDateTime(DateTimeAccuracy.MINUTE, object.getExpiry().toOffsetDateTime());
      case HOUR_DAY_MONTH_YEAR:
        return new FudgeDateTime(DateTimeAccuracy.HOUR, object.getExpiry().toOffsetDateTime());
      case DAY_MONTH_YEAR:
        return new FudgeDateTime(FudgeDate.from(object.getExpiry()), new FudgeTime(DateTimeAccuracy.DAY, 0, 0, 0));
      case MONTH_YEAR:
        return new FudgeDateTime(FudgeDate.ofYearMonth(object.getExpiry().getYear(), object.getExpiry().getMonthValue()),
            new FudgeTime(DateTimeAccuracy.MONTH, 0, 0, 0));
      case YEAR:
        return new FudgeDateTime(FudgeDate.ofYear(object.getExpiry().getYear()), new FudgeTime(DateTimeAccuracy.YEAR, 0, 0, 0));
      default:
        throw new IllegalArgumentException("Invalid accuracy value on " + object);
    }
  }

  /**
   * Converts a Fudge date time and time zone string to an expiry.
   *
   * @param datetime  a Fudge date time
   * @param timezone  the time zone
   * @return  the expiry
   */
  protected static Expiry dateTimeToExpiry(final FudgeDateTime datetime, final String timezone) {
    switch (datetime.getAccuracy()) {
      case NANOSECOND:
      case MILLISECOND:
      case MICROSECOND:
      case SECOND:
      case MINUTE:
        return new Expiry(ZonedDateTime.ofInstant(datetime.toInstant(), ZoneId.of(timezone)), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
      case HOUR:
        return new Expiry(ZonedDateTime.ofInstant(datetime.toInstant(), ZoneId.of(timezone)), ExpiryAccuracy.HOUR_DAY_MONTH_YEAR);
      case DAY:
        return new Expiry(datetime.getDate().toLocalDate().atStartOfDay(ZoneId.of(timezone)), ExpiryAccuracy.DAY_MONTH_YEAR);
      case MONTH:
        return new Expiry(datetime.getDate().toLocalDate().atStartOfDay(ZoneId.of(timezone)), ExpiryAccuracy.MONTH_YEAR);
      case YEAR:
        return new Expiry(datetime.getDate().toLocalDate().atStartOfDay(ZoneId.of(timezone)), ExpiryAccuracy.YEAR);
      default:
        throw new IllegalArgumentException("Invalid accuracy value on " + datetime);
    }
  }

  /**
   * Converts a Fudge date time and time zone string to an expiry.
   *
   * @param datetime  a Fudge date time
   * @param timezone  the time zone
   * @param accuracy  the accuracy
   * @return  the expiry
   */
  protected static Expiry dateTimeToExpiry(final FudgeDateTime datetime, final String timezone, final ExpiryAccuracy accuracy) {
    switch (accuracy) {
      case MIN_HOUR_DAY_MONTH_YEAR:
        return new Expiry(ZonedDateTime.ofInstant(datetime.toInstant(), ZoneId.of(timezone)), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
      case HOUR_DAY_MONTH_YEAR:
        return new Expiry(ZonedDateTime.ofInstant(datetime.toInstant(), ZoneId.of(timezone)), ExpiryAccuracy.HOUR_DAY_MONTH_YEAR);
      case DAY_MONTH_YEAR:
        return new Expiry(datetime.getDate().toLocalDate().atStartOfDay(ZoneId.of(timezone)), ExpiryAccuracy.DAY_MONTH_YEAR);
      case MONTH_YEAR:
        return new Expiry(datetime.getDate().toLocalDate().atStartOfDay(ZoneId.of(timezone)), ExpiryAccuracy.MONTH_YEAR);
      case YEAR:
        return new Expiry(datetime.getDate().toLocalDate().atStartOfDay(ZoneId.of(timezone)), ExpiryAccuracy.YEAR);
      default:
        throw new IllegalArgumentException("Invalid accuracy value on " + datetime);
    }
  }
  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Expiry object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Converts an expiry to a message, returning null if the expiry is null.
   *
   * @param serializer  a serializer
   * @param object  an expiry
   * @return  a message
   */
  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final Expiry object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Adds an encoded expiry to a message.
   *
   * @param serializer  a serializer
   * @param object  an expiry
   * @param msg  the message
   */
  public static void toFudgeMsg(final FudgeSerializer serializer, final Expiry object, final MutableFudgeMsg msg) {
    addToMessage(msg, DATETIME_FIELD_NAME, expiryToDateTime(object));
    addToMessage(msg, TIMEZONE_FIELD_NAME, object.getExpiry().getZone().getId());
    addToMessage(msg, EXPIRY_ACCURACY_NAME, object.getAccuracy().name());
  }

  //-------------------------------------------------------------------------
  @Override
  public Expiry buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  /**
   * Converts a message to an expiry, returning null if the message is null.
   *
   * @param deserializer  a deserializer
   * @param msg  a message
   * @return  an expiry
   */
  public static Expiry fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    return dateTimeToExpiry(
        msg.getValue(FudgeDateTime.class, DATETIME_FIELD_NAME),
        msg.getString(TIMEZONE_FIELD_NAME),
        ExpiryAccuracy.valueOf(msg.getString(EXPIRY_ACCURACY_NAME)));
  }

}
