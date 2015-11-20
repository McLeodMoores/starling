/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.id.ExternalIdFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code SwapLeg} implementations.
 */
public class SwapLegFudgeBuilder extends AbstractFudgeBuilder {

  /** Field name. */
  public static final String DAY_COUNT_FIELD_NAME = "dayCount";
  /** Field name. */
  public static final String FREQUENCY_FIELD_NAME = "frequency";
  /** Field name. */
  public static final String REGION_IDENTIFIER_FIELD_NAME = "regionIdentifier";
  /** Field name. */
  public static final String BUSINESS_DAY_CONVENTION_FIELD_NAME = "businessDayConvention";
  /** Field name. */
  public static final String NOTIONAL_FIELD_NAME = "notional";
  /** Field name. */
  public static final String IS_EOM_FIELD_NAME = "isEOM";

  /** Field name. */
  public static final String FLOATING_REFERENCE_RATE_IDENTIFIER_FIELD_NAME = "floatingReferenceRateIdentifier";
  /** Field name. */
  public static final String INITIAL_FLOATING_RATE_FIELD_NAME = "initialFloatingRate";
  /** Field name. */
  public static final String FLOATING_RATE_TYPE_FIELD_NAME = "floatingRateType";

  public static void toFudgeMsg(final FudgeSerializer serializer, final SwapLeg object, final MutableFudgeMsg msg) {
    addToMessage(msg, DAY_COUNT_FIELD_NAME, object.getDayCount());
    addToMessage(msg, FREQUENCY_FIELD_NAME, object.getFrequency());
    addToMessage(msg, REGION_IDENTIFIER_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getRegionId()));
    addToMessage(msg, BUSINESS_DAY_CONVENTION_FIELD_NAME, object.getBusinessDayConvention());
    addToMessage(serializer, msg, NOTIONAL_FIELD_NAME, object.getNotional(), Notional.class);
    addToMessage(msg, IS_EOM_FIELD_NAME, object.isEom());
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final FloatingInterestRateLeg object, final MutableFudgeMsg msg) {
    toFudgeMsg(serializer, (SwapLeg) object, msg);
    addToMessage(msg, FLOATING_REFERENCE_RATE_IDENTIFIER_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getFloatingReferenceRateId()));
    if (object.getInitialFloatingRate() != null) {
      addToMessage(msg, INITIAL_FLOATING_RATE_FIELD_NAME, object.getInitialFloatingRate());
    }
    addToMessage(msg, FLOATING_RATE_TYPE_FIELD_NAME, object.getFloatingRateType().name());
  }

  public static void fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg, final SwapLeg object) {
    object.setDayCount(msg.getValue(DayCount.class, DAY_COUNT_FIELD_NAME));
    object.setFrequency(msg.getValue(Frequency.class, FREQUENCY_FIELD_NAME));
    object.setRegionId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_IDENTIFIER_FIELD_NAME)));
    object.setBusinessDayConvention(msg.getValue(BusinessDayConvention.class, BUSINESS_DAY_CONVENTION_FIELD_NAME));
    object.setNotional(deserializer.fudgeMsgToObject(Notional.class, msg.getMessage(NOTIONAL_FIELD_NAME)));
    object.setEom(msg.getBoolean(IS_EOM_FIELD_NAME));
  }

  public static void fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg, final FloatingInterestRateLeg object) {
    fromFudgeMsg(deserializer, msg, (SwapLeg) object);
    object.setFloatingReferenceRateId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(FLOATING_REFERENCE_RATE_IDENTIFIER_FIELD_NAME)));
    if (msg.hasField(INITIAL_FLOATING_RATE_FIELD_NAME)) {
      object.setInitialFloatingRate(msg.getDouble(INITIAL_FLOATING_RATE_FIELD_NAME));
    }
    object.setFloatingRateType(FloatingRateType.valueOf(msg.getString(FLOATING_RATE_TYPE_FIELD_NAME)));
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code FixedInterestRateLeg}.
   */
  @FudgeBuilderFor(FixedInterestRateLeg.class)
  public static class FixedInterestRateLegBuilder extends SwapLegFudgeBuilder implements FudgeBuilder<FixedInterestRateLeg>  {
    /** Field name. */
    public static final String RATE_FIELD_NAME = "rate";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FixedInterestRateLeg object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      toFudgeMsg(serializer, object, msg);
      addToMessage(msg, RATE_FIELD_NAME, object.getRate());
      return msg;
    }

    @Override
    public FixedInterestRateLeg buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
      final FixedInterestRateLeg fixedInterestRateLeg = new FixedInterestRateLeg();
      fromFudgeMsg(deserializer, msg, fixedInterestRateLeg);
      fixedInterestRateLeg.setRate(msg.getDouble(RATE_FIELD_NAME));
      return fixedInterestRateLeg;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code FloatingInterestRateLeg}.
   */
  @FudgeBuilderFor(FloatingInterestRateLeg.class)
  public static class FloatingInterestRateLegBuilder extends SwapLegFudgeBuilder implements FudgeBuilder<FloatingInterestRateLeg>  {

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FloatingInterestRateLeg object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      toFudgeMsg(serializer, object, msg);
      return msg;
    }

    @Override
    public FloatingInterestRateLeg buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
      final FloatingInterestRateLeg floatingInterestRateLeg = new FloatingInterestRateLeg();
      fromFudgeMsg(deserializer, msg, floatingInterestRateLeg);
      return floatingInterestRateLeg;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code FloatingSpreadIRLeg}.
   */
  @FudgeBuilderFor(FloatingSpreadIRLeg.class)
  public static class FloatingSpreadIRLegBuilder extends SwapLegFudgeBuilder implements FudgeBuilder<FloatingSpreadIRLeg>  {
    /** Field name. */
    public static final String SPREAD_FIELD_NAME = "spread";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FloatingSpreadIRLeg object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      toFudgeMsg(serializer, object, msg);
      addToMessage(msg, SPREAD_FIELD_NAME, object.getSpread());
      return msg;
    }

    @Override
    public FloatingSpreadIRLeg buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
      final FloatingSpreadIRLeg floatingSpreadIRLeg = new FloatingSpreadIRLeg();
      fromFudgeMsg(deserializer, msg, floatingSpreadIRLeg);
      floatingSpreadIRLeg.setSpread(msg.getDouble(SPREAD_FIELD_NAME));
      return floatingSpreadIRLeg;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code FloatingGearingIRLeg}.
   */
  @FudgeBuilderFor(FloatingGearingIRLeg.class)
  public static class FloatingGearingIRLegBuilder extends SwapLegFudgeBuilder implements FudgeBuilder<FloatingGearingIRLeg>  {
    /** Field name. */
    public static final String GEARING_FIELD_NAME = "gearing";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FloatingGearingIRLeg object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      toFudgeMsg(serializer, object, msg);
      addToMessage(msg, GEARING_FIELD_NAME, object.getGearing());
      return msg;
    }

    @Override
    public FloatingGearingIRLeg buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
      final FloatingGearingIRLeg floatingInterestRateLeg = new FloatingGearingIRLeg();
      fromFudgeMsg(deserializer, msg, floatingInterestRateLeg);
      floatingInterestRateLeg.setGearing(msg.getDouble(GEARING_FIELD_NAME));
      return floatingInterestRateLeg;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code FixedInflationSwapLeg}.
   */
  @FudgeBuilderFor(FixedInflationSwapLeg.class)
  public static class FixedInflationSwapLegBuilder extends SwapLegFudgeBuilder implements FudgeBuilder<FixedInflationSwapLeg>  {
    /** The rate field name */
    private static final String RATE_FIELD_NAME = "rate";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FixedInflationSwapLeg object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      toFudgeMsg(serializer, object, msg);
      addToMessage(msg, RATE_FIELD_NAME, object.getRate());
      return msg;
    }

    @Override
    public FixedInflationSwapLeg buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
      final FixedInflationSwapLeg fixedInflationSwapLeg = new FixedInflationSwapLeg();
      fromFudgeMsg(deserializer, msg, fixedInflationSwapLeg);
      fixedInflationSwapLeg.setRate(msg.getDouble(RATE_FIELD_NAME));
      return fixedInflationSwapLeg;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code InflationIndexSwapLeg}.
   */
  @FudgeBuilderFor(InflationIndexSwapLeg.class)
  public static class InflationIndexSwapLegBuilder extends SwapLegFudgeBuilder implements FudgeBuilder<InflationIndexSwapLeg>  {
    /** The index reference id field name */
    private static final String INDEX_ID_FIELD_NAME = "indexId";
    /** The quotation lag field name */
    private static final String QUOTATION_LAG_FIELD_NAME = "quotationLagField";
    /** The conventional lag field name */
    private static final String CONVENTIONAL_LAG_FIELD_NAME = "conventionalLagField";
    /** The interpolation method field name */
    private static final String INTERPOLATION_METHOD_FIELD_NAME = "interpolationMethod";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final InflationIndexSwapLeg object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      toFudgeMsg(serializer, object, msg);
      addToMessage(msg, INDEX_ID_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getIndexId()));
      addToMessage(msg, QUOTATION_LAG_FIELD_NAME, object.getQuotationIndexationLag());
      addToMessage(msg, CONVENTIONAL_LAG_FIELD_NAME, object.getConventionalIndexationLag());
      addToMessage(msg, INTERPOLATION_METHOD_FIELD_NAME, object.getInterpolationMethod().name());
      return msg;
    }

    @Override
    public InflationIndexSwapLeg buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
      final InflationIndexSwapLeg inflationIndexSwapLeg = new InflationIndexSwapLeg();
      fromFudgeMsg(deserializer, msg, inflationIndexSwapLeg);
      inflationIndexSwapLeg.setIndexId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(INDEX_ID_FIELD_NAME)));
      inflationIndexSwapLeg.setQuotationIndexationLag(msg.getInt(QUOTATION_LAG_FIELD_NAME));
      inflationIndexSwapLeg.setConventionalIndexationLag(msg.getInt(CONVENTIONAL_LAG_FIELD_NAME));
      inflationIndexSwapLeg.setInterpolationMethod(InterpolationMethod.valueOf(msg.getString(INTERPOLATION_METHOD_FIELD_NAME)));
      return inflationIndexSwapLeg;
    }
  }
}
