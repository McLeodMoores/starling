/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.serialization.fudge;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.integration.adapter.FinmathBusinessDay;
import com.mcleodmoores.integration.adapter.FinmathBusinessDayFactory;
import com.mcleodmoores.integration.adapter.FinmathDayCount;
import com.mcleodmoores.integration.adapter.FinmathDayCountFactory;

import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelInterface;
import net.finmath.marketdata.model.curves.AbstractForwardCurve;
import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.Curve.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.Curve.InterpolationEntity;
import net.finmath.marketdata.model.curves.Curve.InterpolationMethod;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.curves.DiscountCurveFromForwardCurve;
import net.finmath.marketdata.model.curves.DiscountCurveFromProductOfCurves;
import net.finmath.marketdata.model.curves.DiscountCurveInterface;
import net.finmath.marketdata.model.curves.DiscountCurveNelsonSiegelSvensson;
import net.finmath.marketdata.model.curves.ForwardCurve;
import net.finmath.marketdata.model.curves.ForwardCurve.InterpolationEntityForward;
import net.finmath.marketdata.model.curves.ForwardCurveFromDiscountCurve;
import net.finmath.marketdata.model.curves.ForwardCurveInterface;
import net.finmath.marketdata.model.curves.ForwardCurveNelsonSiegelSvensson;
import net.finmath.marketdata.model.curves.ForwardCurveWithFixings;
import net.finmath.marketdata.model.curves.PiecewiseCurve;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarInterface;
import net.finmath.time.businessdaycalendar.BusinessdayCalendarInterface.DateRollConvention;
import net.finmath.time.daycount.DayCountConventionInterface;

/**
 * Fudge builders for objects that extend {@link net.finmath.marketdata.model.curves.AbstractCurve}.
 */
/* package */ final class FinmathCurveBuilders {
  /** The logger. */
  /* package */ static final Logger LOGGER = LoggerFactory.getLogger(FinmathCurveBuilders.class);
  /** The point class name - used to get the x, y, and parameter points from curves */
  private static final String POINT_CLASS_NAME = "net.finmath.marketdata.model.curves.Curve$Point";
  /** The name of the points field in Curves.Point */
  private static final String POINTS_FIELD_NAME = "points";
  /** The name of the parameter field in Curves.Point */
  private static final String IS_PARAMETER_FIELD_NAME = "isParameter";
  /** The name of the time field in Curves.Point */
  private static final String TIME_FIELD_NAME = "time";
  /** The name of the value field in Curves.Point */
  private static final String VALUE_FIELD_NAME = "value";
  /** The name of the interpolation entity field in Curve */
  private static final String INTERPOLATION_ENTITY_FIELD = "interpolationEntity";

  /**
   * Restricted constructor.
   */
  private FinmathCurveBuilders() {
  }

  /**
   * Fudge builder for {@link DiscountCurve}.
   */
  @FudgeBuilderFor(DiscountCurve.class)
  public static class DiscountCurveBuilder implements FudgeBuilder<DiscountCurve> {
    /** The name field ordinal */
    private static final int NAME_ORDINAL = 1;
    /** The parameter field ordinal */
    private static final int PARAMETER_ORDINAL = 2;
    /** The times ordinal */
    private static final int TIMES_ORDINAL = 3;
    /** The values ordinal */
    private static final int VALUES_ORDINAL = 4;
    /** The interpolation method ordinal */
    private static final int INTERPOLATION_METHOD_ORDINAL = 5;
    /** The extrapolation method ordinal */
    private static final int EXTRAPOLATION_METHOD_ORDINAL = 6;
    /** The interpolation entity ordinal */
    private static final int INTERPOLATION_ENTITY_ORDINAL = 7;
    /** The reference date ordinal */
    private static final int REFERENCE_DATE_ORDINAL = 8;

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DiscountCurve object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      try {
        final Class<?>[] innerClasses = Curve.class.getDeclaredClasses();
        Class<?> pointsClass = null;
        for (final Class<?> innerClass : innerClasses) {
          if (innerClass.getName().equals(POINT_CLASS_NAME)) {
            pointsClass = innerClass;
            break;
          }
        }
        if (pointsClass == null) {
          throw new IllegalStateException("Could not get Curve$Points class");
        }
        final Field pointsField = Curve.class.getDeclaredField(POINTS_FIELD_NAME);
        final Field parameterField = pointsClass.getDeclaredField(IS_PARAMETER_FIELD_NAME);
        final Field timeField = pointsClass.getDeclaredField(TIME_FIELD_NAME);
        final Field valueField = pointsClass.getDeclaredField(VALUE_FIELD_NAME);
        AccessibleObject.setAccessible(new AccessibleObject[] {pointsField, parameterField, timeField, valueField}, true);
        final ArrayList<?> pointsList = (ArrayList<?>) pointsField.get(object);
        final int size = pointsList.size();
        //TODO why aren't these being accepted as arrays of primitives?
        final Boolean[] parameters = new Boolean[size];
        final Double[] times = new Double[size];
        final Double[] values = new Double[size];
        int i = 0;
        for (final Object points : pointsList) {
          parameters[i] = (Boolean) parameterField.get(points);
          times[i] = (Double) timeField.get(points);
          values[i++] = (Double) valueField.get(points);
        }
        message.add(NAME_ORDINAL, object.getName());
        serializer.addToMessage(message, null, PARAMETER_ORDINAL, parameters);
        serializer.addToMessage(message, null, TIMES_ORDINAL, times);
        serializer.addToMessage(message, null, VALUES_ORDINAL, values);
        message.add(INTERPOLATION_METHOD_ORDINAL, object.getInterpolationMethod().name());
        message.add(EXTRAPOLATION_METHOD_ORDINAL, object.getExtrapolationMethod().name());
        message.add(INTERPOLATION_ENTITY_ORDINAL, object.getInterpolationEntity().name());
        if (object.getReferenceDate() != null) {
          serializer.addToMessage(message, null, REFERENCE_DATE_ORDINAL, object.getReferenceDate());
        }
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public DiscountCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      try {
        final String name = message.getString(NAME_ORDINAL);
        final Boolean[] parameters = deserializer.fieldValueToObject(Boolean[].class, message.getByOrdinal(PARAMETER_ORDINAL));
        final Double[] times = deserializer.fieldValueToObject(Double[].class, message.getByOrdinal(TIMES_ORDINAL));
        final Double[] values = deserializer.fieldValueToObject(Double[].class, message.getByOrdinal(VALUES_ORDINAL));
        final InterpolationMethod interpolationMethod = InterpolationMethod.valueOf(message.getString(INTERPOLATION_METHOD_ORDINAL));
        final ExtrapolationMethod extrapolationMethod = ExtrapolationMethod.valueOf(message.getString(EXTRAPOLATION_METHOD_ORDINAL));
        final InterpolationEntity interpolationEntity = InterpolationEntity.valueOf(message.getString(INTERPOLATION_ENTITY_ORDINAL));
        LocalDate referenceDate;
        if (message.hasField(REFERENCE_DATE_ORDINAL)) {
          referenceDate = deserializer.fieldValueToObject(LocalDate.class, message.getByOrdinal(REFERENCE_DATE_ORDINAL));
        } else {
          referenceDate = null;
        }
        // The y-values are serialized as the values in the Points class, which might have been manipulated depending on the interpolation entity.
        // The interpolation entity needs to be VALUE on initialization and then set to the actual value
        final DiscountCurve discountCurve = DiscountCurve.createDiscountCurveFromDiscountFactors(name, referenceDate, ArrayUtils.toPrimitive(times),
            ArrayUtils.toPrimitive(values), ArrayUtils.toPrimitive(parameters), interpolationMethod, extrapolationMethod, InterpolationEntity.VALUE);
        final Field field = Curve.class.getDeclaredField(INTERPOLATION_ENTITY_FIELD);
        field.setAccessible(true);
        field.set(discountCurve, interpolationEntity);
        return discountCurve;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not deserialize " + message, e);
      }
    }
  }

  /**
   * Fudge builder for {@link DiscountCurveFromProductOfCurves}.
   */
  @FudgeBuilderFor(DiscountCurveFromProductOfCurves.class)
  public static class DiscountCurveFromProductOfCurvesBuilder implements FudgeBuilder<DiscountCurveFromProductOfCurves> {
    /** The name field ordinal */
    private static final int NAME_ORDINAL = 1;
    /** The curves field ordinal */
    private static final int CURVES_ORDINAL = 2;
    /** The reference date ordinal */
    private static final int REFERENCE_DATE_ORDINAL = 3;
    /** The curves field name */
    private static final String CURVES_FIELD = "curves";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DiscountCurveFromProductOfCurves object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      try {
        final Field field = DiscountCurveFromProductOfCurves.class.getDeclaredField(CURVES_FIELD);
        field.setAccessible(true);
        final DiscountCurveInterface[] curves = (DiscountCurveInterface[]) field.get(object);
        message.add(NAME_ORDINAL, object.getName());
        serializer.addToMessageWithClassHeaders(message, null, CURVES_ORDINAL, curves, DiscountCurveInterface[].class);
        if (object.getReferenceDate() != null) {
          serializer.addToMessage(message, null, REFERENCE_DATE_ORDINAL, object.getReferenceDate());
        }
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public DiscountCurveFromProductOfCurves buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_ORDINAL);
      final DiscountCurveInterface[] curves = deserializer.fieldValueToObject(DiscountCurveInterface[].class, message.getByOrdinal(CURVES_ORDINAL));
      LocalDate referenceDate;
      if (message.hasField(REFERENCE_DATE_ORDINAL)) {
        referenceDate = deserializer.fieldValueToObject(LocalDate.class, message.getByOrdinal(REFERENCE_DATE_ORDINAL));
      } else {
        referenceDate = null;
      }
      return new DiscountCurveFromProductOfCurves(name, referenceDate, curves);
    }

  }

  /**
   * Fudge builder for {@link DiscountCurveNelsonSiegelSvensson}.
   */
  @FudgeBuilderFor(DiscountCurveNelsonSiegelSvensson.class)
  public static class DiscountCurveNelsonSiegelSvenssonBuilder implements FudgeBuilder<DiscountCurveNelsonSiegelSvensson> {
    /** The name field ordinal */
    private static final int NAME_ORDINAL = 1;
    /** The time scaling field ordinal */
    private static final int TIME_SCALING_ORDINAL = 2;
    /** The parameters field ordinal */
    private static final int PARAMETERS_ORDINAL = 3;
    /** The reference date ordinal */
    private static final int REFERENCE_DATE_ORDINAL = 4;

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DiscountCurveNelsonSiegelSvensson object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(NAME_ORDINAL, object.getName());
      if (object.getReferenceDate() != null) {
        serializer.addToMessage(message, null, REFERENCE_DATE_ORDINAL, object.getReferenceDate());
      }
      message.add(TIME_SCALING_ORDINAL, object.getTimeScaling());
      serializer.addToMessage(message, null, PARAMETERS_ORDINAL, object.getParameter());
      return message;
    }

    @Override
    public DiscountCurveNelsonSiegelSvensson buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_ORDINAL);
      LocalDate referenceDate;
      if (message.hasField(REFERENCE_DATE_ORDINAL)) {
        referenceDate = deserializer.fieldValueToObject(LocalDate.class, message.getByOrdinal(REFERENCE_DATE_ORDINAL));
      } else {
        referenceDate = null;
      }
      final double timeScaling = message.getDouble(TIME_SCALING_ORDINAL);
      final double[] parameters = deserializer.fieldValueToObject(double[].class, message.getByOrdinal(PARAMETERS_ORDINAL));
      return new DiscountCurveNelsonSiegelSvensson(name, referenceDate, parameters, timeScaling);
    }

  }

  /**
   * Fudge builder for {@link ForwardCurve}.
   */
  @FudgeBuilderFor(ForwardCurve.class)
  public static class ForwardCurveBuilder implements FudgeBuilder<ForwardCurve> {
    /** The name ordinal */
    private static final int NAME_ORDINAL = 1;
    /** The times ordinal */
    private static final int TIMES_ORDINAL = 2;
    /** The values ordinal */
    private static final int VALUES_ORDINAL = 3;
    /** The interpolation method ordinal */
    private static final int INTERPOLATION_METHOD_ORDINAL = 4;
    /** The extrapolation method ordinal */
    private static final int EXTRAPOLATION_METHOD_ORDINAL = 5;
    /** The interpolation entity ordinal */
    private static final int INTERPOLATION_ENTITY_ORDINAL = 6;
    /** The discount curve name ordinal */
    private static final int DISCOUNT_CURVE_NAME_ORDINAL = 7;
    /** The payment offset code ordinal */
    private static final int PAYMENT_OFFSET_ORDINAL = 8;
    /** The business day calendar name ordinal */
    private static final int BUSINESS_DAY_CALENDAR_ORDINAL = 9;
    /** The roll date convention ordinal */
    private static final int ROLL_DATE_CONVENTION_ORDINAL = 10;
    /** The forward interpolation entity ordinal */
    private static final int FORWARD_INTERPOLATION_ENTITY_ORDINAL = 11;
    /** The reference date ordinal */
    private static final int REFERENCE_DATE_ORDINAL = 12;
    /** The payment offset field name */
    private static final String PAYMENT_OFFSET_FIELD = "paymentOffsetCode";
    /** The business day calendar field name */
    private static final String PAYMENT_BUSINESS_DAY_CALENDAR_FIELD = "paymentBusinessdayCalendar";
    /** The payment roll date convention field name */
    private static final String PAYMENT_ROLL_DATE_CONVENTION_FIELD = "paymentDateRollConvention";
    /** The forward interpolation entity field name */
    private static final String FORWARD_INTERPOLATION_ENTITY_FIELD = "interpolationEntityForward";
    /** An empty analytic model */
    private static final AnalyticModelInterface EMPTY_ANALYTIC_MODEL = new AnalyticModel();

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ForwardCurve object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(NAME_ORDINAL, object.getName());
      try {
        final Class<?>[] innerClasses = Curve.class.getDeclaredClasses();
        Class<?> pointsClass = null;
        for (final Class<?> innerClass : innerClasses) {
          if (innerClass.getName().equals(POINT_CLASS_NAME)) {
            pointsClass = innerClass;
            break;
          }
        }
        if (pointsClass == null) {
          throw new IllegalStateException("Could not get Curve$Points class");
        }
        final Field pointsField = Curve.class.getDeclaredField(POINTS_FIELD_NAME);
        final Field timeField = pointsClass.getDeclaredField(TIME_FIELD_NAME);
        final Field valueField = pointsClass.getDeclaredField(VALUE_FIELD_NAME);
        final Field offsetCodeField = AbstractForwardCurve.class.getDeclaredField(PAYMENT_OFFSET_FIELD);
        final Field paymentBusinessDayCalendarField = AbstractForwardCurve.class.getDeclaredField(PAYMENT_BUSINESS_DAY_CALENDAR_FIELD);
        final Field paymentRollDateConventionField = AbstractForwardCurve.class.getDeclaredField(PAYMENT_ROLL_DATE_CONVENTION_FIELD);
        final Field forwardInterpolationEntityField = ForwardCurve.class.getDeclaredField(FORWARD_INTERPOLATION_ENTITY_FIELD);
        AccessibleObject.setAccessible(new AccessibleObject[] {pointsField, timeField, valueField, offsetCodeField,
          paymentBusinessDayCalendarField, paymentRollDateConventionField, forwardInterpolationEntityField}, true);
        final BusinessdayCalendarInterface paymentBusinessDayCalendar = (BusinessdayCalendarInterface) paymentBusinessDayCalendarField.get(object);
        if (paymentBusinessDayCalendar instanceof FinmathBusinessDay) {
          message.add(BUSINESS_DAY_CALENDAR_ORDINAL, ((FinmathBusinessDay) paymentBusinessDayCalendar).getName());
        } else {
          LOGGER.error("Could not serialize business day calendar for forward curve {}, using BusinessdayCalendarAny", object);
          message.add(BUSINESS_DAY_CALENDAR_ORDINAL, "None"); //TODO factory needs to work which is the correct one
        }
        final String paymentOffsetCode = (String) offsetCodeField.get(object);
        message.add(PAYMENT_OFFSET_ORDINAL, paymentOffsetCode);
        message.add(ROLL_DATE_CONVENTION_ORDINAL, ((DateRollConvention) paymentRollDateConventionField.get(object)).name());
        final ArrayList<?> pointsList = (ArrayList<?>) pointsField.get(object);
        final int size = pointsList.size();
        //TODO why aren't these being accepted as arrays of primitives?
        final Double[] times = new Double[size];
        final Double[] values = new Double[size];
        int i = 0;
        for (final Object points : pointsList) {
          times[i] = (Double) timeField.get(points);
          values[i++] = (Double) valueField.get(points);
        }
        message.add(NAME_ORDINAL, object.getName());
        serializer.addToMessage(message, null, TIMES_ORDINAL, times);
        serializer.addToMessage(message, null, VALUES_ORDINAL, values);
        message.add(INTERPOLATION_METHOD_ORDINAL, object.getInterpolationMethod().name());
        message.add(EXTRAPOLATION_METHOD_ORDINAL, object.getExtrapolationMethod().name());
        message.add(INTERPOLATION_ENTITY_ORDINAL, object.getInterpolationEntity().name());
        message.add(DISCOUNT_CURVE_NAME_ORDINAL, object.getDiscountCurveName());
        message.add(FORWARD_INTERPOLATION_ENTITY_ORDINAL,
            ((InterpolationEntityForward) forwardInterpolationEntityField.get(object)).name());
        if (object.getReferenceDate() != null) {
          serializer.addToMessage(message, null, REFERENCE_DATE_ORDINAL, object.getReferenceDate());
        }
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public ForwardCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      try {
        final String name = message.getString(NAME_ORDINAL);
        final double[] times = ArrayUtils.toPrimitive(deserializer.fieldValueToObject(Double[].class, message.getByOrdinal(TIMES_ORDINAL)));
        final double[] values = ArrayUtils.toPrimitive(deserializer.fieldValueToObject(Double[].class, message.getByOrdinal(VALUES_ORDINAL)));
        final InterpolationMethod interpolationMethod = InterpolationMethod.valueOf(message.getString(INTERPOLATION_METHOD_ORDINAL));
        final ExtrapolationMethod extrapolationMethod = ExtrapolationMethod.valueOf(message.getString(EXTRAPOLATION_METHOD_ORDINAL));
        final InterpolationEntity interpolationEntity = InterpolationEntity.valueOf(message.getString(INTERPOLATION_ENTITY_ORDINAL));
        final String paymentOffsetCode = message.getString(PAYMENT_OFFSET_ORDINAL);
        final String businessDayCalendarName = message.getString(BUSINESS_DAY_CALENDAR_ORDINAL);
        final BusinessdayCalendarInterface paymentBusinessDayCalendar = FinmathBusinessDayFactory.of(businessDayCalendarName);
        final BusinessdayCalendarInterface.DateRollConvention paymentRollConvention =
            DateRollConvention.valueOf(message.getString(ROLL_DATE_CONVENTION_ORDINAL));
        final String discountCurveName = message.getString(DISCOUNT_CURVE_NAME_ORDINAL);
        final InterpolationEntityForward forwardInterpolationEntity =
            InterpolationEntityForward.valueOf(message.getString(FORWARD_INTERPOLATION_ENTITY_ORDINAL));
        final LocalDate referenceDate;
        if (message.hasField(REFERENCE_DATE_ORDINAL)) {
          referenceDate = deserializer.fieldValueToObject(LocalDate.class, message.getByOrdinal(REFERENCE_DATE_ORDINAL));
        } else {
          referenceDate = null;
        }
        // The y-values are serialized as the forwards that were calculated from the original inputs, and the analytic model is used only
        // for initialization of the forwards. The interpolation entity needs to be VALUE and the forward interpolation entity needs to be FORWARD
        // on initialization and then set to the actual value, and an empty analytic model must be supplied.
        final ForwardCurve forwardCurve = ForwardCurve.createForwardCurveFromForwards(name, referenceDate, paymentOffsetCode, paymentBusinessDayCalendar,
            paymentRollConvention, interpolationMethod, extrapolationMethod, InterpolationEntity.VALUE, InterpolationEntityForward.FORWARD, discountCurveName,
            EMPTY_ANALYTIC_MODEL, times, values);
        final Field interpolationEntityField = Curve.class.getDeclaredField(INTERPOLATION_ENTITY_FIELD);
        final Field forwardInterpolationEntityField = ForwardCurve.class.getDeclaredField(FORWARD_INTERPOLATION_ENTITY_FIELD);
        AccessibleObject.setAccessible(new AccessibleObject[] {interpolationEntityField, forwardInterpolationEntityField}, true);
        interpolationEntityField.set(forwardCurve, interpolationEntity);
        forwardInterpolationEntityField.set(forwardCurve, forwardInterpolationEntity);
        return forwardCurve;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not deserialize " + message, e);
      }
    }
  }

  /**
   * Fudge builder for {@link DiscountCurveFromForwardCurve}.
   */
  @FudgeBuilderFor(DiscountCurveFromForwardCurve.class)
  public static class DiscountCurveFromForwardCurveBuilder implements FudgeBuilder<DiscountCurveFromForwardCurve> {
    /** The forward curve ordinal */
    private static final int FORWARD_CURVE_ORDINAL = 1;
    /** The forward curve name ordinal */
    private static final int FORWARD_CURVE_NAME_ORDINAL = 2;
    /** The time scaling ordinal */
    private static final int TIME_SCALING_ORDINAL = 3;
    /** The forward curve field name */
    private static final String FORWARD_CURVE_FIELD = "forwardCurve";
    /** The forward curve name field name */
    private static final String FORWARD_CURVE_NAME_FIELD = "forwardCurveName";
    /** The time scaling field name */
    private static final String TIME_SCALING_FIELD = "timeScaling";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DiscountCurveFromForwardCurve object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      try {
        final Field forwardCurveField = DiscountCurveFromForwardCurve.class.getDeclaredField(FORWARD_CURVE_FIELD);
        final Field timeScalingField = DiscountCurveFromForwardCurve.class.getDeclaredField(TIME_SCALING_FIELD);
        AccessibleObject.setAccessible(new AccessibleObject[] {forwardCurveField, timeScalingField}, true);
        final Object forwardCurve = forwardCurveField.get(object);
        if (forwardCurve == null) {
          // serialize forward curve name, even if it is null
          final Field forwardCurveNameField = DiscountCurveFromForwardCurve.class.getDeclaredField(FORWARD_CURVE_NAME_FIELD);
          forwardCurveNameField.setAccessible(true);
          final Object forwardCurveName = forwardCurveNameField.get(object);
          message.add(FORWARD_CURVE_NAME_ORDINAL, forwardCurveName);
        } else {
          serializer.addToMessageWithClassHeaders(message, null, FORWARD_CURVE_ORDINAL, forwardCurve);
        }
        message.add(TIME_SCALING_ORDINAL, timeScalingField.get(object));
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public DiscountCurveFromForwardCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double timeScaling = message.getDouble(TIME_SCALING_ORDINAL);
      if (message.hasField(FORWARD_CURVE_NAME_ORDINAL)) {
        final String forwardCurveName = message.getString(FORWARD_CURVE_NAME_ORDINAL);
        return new DiscountCurveFromForwardCurve(forwardCurveName, timeScaling);
      }
      final ForwardCurve forwardCurve = deserializer.fieldValueToObject(ForwardCurve.class, message.getByOrdinal(FORWARD_CURVE_ORDINAL));
      return new DiscountCurveFromForwardCurve(forwardCurve, timeScaling);
    }

  }

  /**
   * Fudge builder for {@link ForwardCurveFromDiscountCurve}.
   */
  @FudgeBuilderFor(ForwardCurveFromDiscountCurve.class)
  public static class ForwardCurveFromDiscountCurveBuilder implements FudgeBuilder<ForwardCurveFromDiscountCurve> {
    /** The name ordinal */
    private static final int NAME_ORDINAL = 1;
    /** The payment offset code ordinal */
    private static final int PAYMENT_OFFSET_ORDINAL = 2;
    /** The reference date ordinal */
    private static final int REFERENCE_DATE_ORDINAL = 3;
    /** The payment offset field name */
    private static final String PAYMENT_OFFSET_FIELD = "paymentOffsetCode";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ForwardCurveFromDiscountCurve object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      final String name = object.getName();
      final int lastIndex = name.lastIndexOf(',');
      final String originalName = name.substring(30, lastIndex);
      message.add(NAME_ORDINAL, originalName);
      try {
        final Field offsetCodeField = AbstractForwardCurve.class.getDeclaredField(PAYMENT_OFFSET_FIELD);
        offsetCodeField.setAccessible(true);
        final String paymentOffsetCode = (String) offsetCodeField.get(object);
        message.add(PAYMENT_OFFSET_ORDINAL, paymentOffsetCode);
        if (object.getReferenceDate() != null) {
          serializer.addToMessage(message, null, REFERENCE_DATE_ORDINAL, object.getReferenceDate());
        }
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public ForwardCurveFromDiscountCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_ORDINAL);
      final String paymentOffsetCode = message.getString(PAYMENT_OFFSET_ORDINAL);
      final LocalDate referenceDate;
      if (message.hasField(REFERENCE_DATE_ORDINAL)) {
        referenceDate = deserializer.fieldValueToObject(LocalDate.class, message.getByOrdinal(REFERENCE_DATE_ORDINAL));
      } else {
        referenceDate = null;
      }
      return new ForwardCurveFromDiscountCurve(name, referenceDate, paymentOffsetCode);
    }

  }

  /**
   * Fudge builder for {@link ForwardCurveWithFixings}.
   */
  @FudgeBuilderFor(ForwardCurveWithFixings.class)
  public static class ForwardCurveWithFixingsBuilder implements FudgeBuilder<ForwardCurveWithFixings> {
    /** The base curve ordinal */
    private static final int BASE_CURVE_ORDINAL = 1;
    /** The fixed part curve ordinal */
    private static final int FIXED_PART_CURVE_ORDINAL = 2;
    /** The fixed part start time ordinal */
    private static final int FIXED_PART_START_ORDINAL = 3;
    /** The fixed part end time ordinal */
    private static final int FIXED_PART_END_ORDINAL = 4;
    /** The base curve field name */
    private static final String BASE_CURVE_FIELD = "baseCurve";
    /** The fixed part curve field name */
    private static final String FIXED_PART_FIELD = "fixedPartCurve";
    /** The fixed part start time field name */
    private static final String FIXED_PART_START_TIME_FIELD = "fixedPartStartTime";
    /** The fixed part end time field name */
    private static final String FIXED_PART_END_TIME_FIELD = "fixedPartEndTime";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ForwardCurveWithFixings object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      try {
        final Field baseCurveField = PiecewiseCurve.class.getDeclaredField(BASE_CURVE_FIELD);
        final Field fixedPartCurveField = PiecewiseCurve.class.getDeclaredField(FIXED_PART_FIELD);
        final Field fixedPartStartTimeField = PiecewiseCurve.class.getDeclaredField(FIXED_PART_START_TIME_FIELD);
        final Field fixedPartEndTimeField = PiecewiseCurve.class.getDeclaredField(FIXED_PART_END_TIME_FIELD);
        AccessibleObject.setAccessible(new AccessibleObject[] {baseCurveField,  fixedPartCurveField, fixedPartStartTimeField, fixedPartEndTimeField}, true);
        serializer.addToMessageWithClassHeaders(message, null, BASE_CURVE_ORDINAL, baseCurveField.get(object));
        serializer.addToMessageWithClassHeaders(message, null, FIXED_PART_CURVE_ORDINAL, fixedPartCurveField.get(object));
        message.add(FIXED_PART_START_ORDINAL, fixedPartStartTimeField.get(object));
        message.add(FIXED_PART_END_ORDINAL, fixedPartEndTimeField.get(object));
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serializer " + object, e);
      }
    }

    @Override
    public ForwardCurveWithFixings buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final ForwardCurveInterface baseCurve = deserializer.fieldValueToObject(ForwardCurveInterface.class, message.getByOrdinal(BASE_CURVE_ORDINAL));
      final ForwardCurveInterface fixedPartCurve = deserializer.fieldValueToObject(ForwardCurveInterface.class, message.getByOrdinal(FIXED_PART_CURVE_ORDINAL));
      final double fixedPartStartTime = message.getDouble(FIXED_PART_START_ORDINAL);
      final double fixedPartEndTime = message.getDouble(FIXED_PART_END_ORDINAL);
      return new ForwardCurveWithFixings(baseCurve, fixedPartCurve, fixedPartStartTime, fixedPartEndTime);
    }

  }

  /**
   * Fudge builder for {@link ForwardCurveNelsonSiegelSvensson}.
   */
  @FudgeBuilderFor(ForwardCurveNelsonSiegelSvensson.class)
  public static class ForwardCurveNelsonSiegelSvenssonBuilder implements FudgeBuilder<ForwardCurveNelsonSiegelSvensson> {
    /** The payment offset code field ordinal */
    private static final int PAYMENT_OFFSET_CODE_ORDINAL = 1;
    /** The business day calendar field ordinal */
    private static final int BUSINESS_DAY_CALENDAR_ORDINAL = 2;
    /** The roll date convention field ordinal */
    private static final int ROLL_DATE_CONVENTION_ORDINAL = 3;
    /** The day count convention field ordinal */
    private static final int DAY_COUNT_CONVENTION_ORDINAL = 4;
    /** The reference date ordinal */
    private static final int REFERENCE_DATE_ORDINAL = 5;
    /** The discount curve name field ordinal */
    private static final int DISCOUNT_CURVE_NAME_ORDINAL = 6;
    /** The discount curve time scaling field ordinal */
    private static final int DISCOUNT_CURVE_TIME_SCALING_ORDINAL = 7;
    /** The discount curve parameters field ordinal */
    private static final int DISCOUNT_CURVE_PARAMETERS_ORDINAL = 8;
    /** The payment offset field name */
    private static final String PAYMENT_OFFSET_FIELD = "paymentOffsetCode";
    /** The business day calendar field name */
    private static final String PAYMENT_BUSINESS_DAY_CALENDAR_FIELD = "paymentBusinessdayCalendar";
    /** The payment roll date convention field name */
    private static final String PAYMENT_ROLL_DATE_CONVENTION_FIELD = "paymentDateRollConvention";
    /** The day count convention field name */
    private static final String DAY_COUNT_CONVENTION_FIELD = "daycountConvention";
    /** The discount curve field name */
    private static final String DISCOUNT_CURVE_FIELD = "discountCurve";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ForwardCurveNelsonSiegelSvensson object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      try {
        final Field offsetCodeField = ForwardCurveNelsonSiegelSvensson.class.getDeclaredField(PAYMENT_OFFSET_FIELD);
        final Field paymentBusinessDayCalendarField = ForwardCurveNelsonSiegelSvensson.class.getDeclaredField(PAYMENT_BUSINESS_DAY_CALENDAR_FIELD);
        final Field paymentRollDateConventionField = ForwardCurveNelsonSiegelSvensson.class.getDeclaredField(PAYMENT_ROLL_DATE_CONVENTION_FIELD);
        final Field dayCountConventionField = ForwardCurveNelsonSiegelSvensson.class.getDeclaredField(DAY_COUNT_CONVENTION_FIELD);
        final Field discountCurveField = ForwardCurveNelsonSiegelSvensson.class.getDeclaredField(DISCOUNT_CURVE_FIELD);
        AccessibleObject.setAccessible(new AccessibleObject[] {offsetCodeField, paymentBusinessDayCalendarField, paymentRollDateConventionField,
          dayCountConventionField, discountCurveField}, true);
        final BusinessdayCalendarInterface paymentBusinessDayCalendar = (BusinessdayCalendarInterface) paymentBusinessDayCalendarField.get(object);
        if (paymentBusinessDayCalendar instanceof FinmathBusinessDay) {
          message.add(BUSINESS_DAY_CALENDAR_ORDINAL, ((FinmathBusinessDay) paymentBusinessDayCalendar).getName());
        } else {
          LOGGER.error("Could not serialize business day calendar for forward curve {}, using BusinessdayCalendarAny", object);
          message.add(BUSINESS_DAY_CALENDAR_ORDINAL, "None"); //TODO factory needs to work which is the correct one
        }
        final DayCountConventionInterface dayCountConvention = (DayCountConventionInterface) dayCountConventionField.get(object);
        if (dayCountConvention instanceof FinmathDayCount) {
          message.add(DAY_COUNT_CONVENTION_ORDINAL, ((FinmathDayCount) dayCountConvention).getName());
        } else {
          LOGGER.error("Could not serialize day count convention for forward curve {}, using DayCountConvention_ACT_ACT_ISDA", object);
          message.add(DAY_COUNT_CONVENTION_ORDINAL, "Act/Act ISDA"); //TODO factory needs to work which is the correct one
        }
        final String paymentOffsetCode = (String) offsetCodeField.get(object);
        message.add(PAYMENT_OFFSET_CODE_ORDINAL, paymentOffsetCode);
        message.add(ROLL_DATE_CONVENTION_ORDINAL, ((DateRollConvention) paymentRollDateConventionField.get(object)).name());
        final DiscountCurveNelsonSiegelSvensson discountCurve = (DiscountCurveNelsonSiegelSvensson) discountCurveField.get(object);
        message.add(DISCOUNT_CURVE_NAME_ORDINAL, discountCurve.getName());
        if (object.getReferenceDate() != null) {
          serializer.addToMessage(message, null, REFERENCE_DATE_ORDINAL, discountCurve.getReferenceDate());
        }
        message.add(DISCOUNT_CURVE_TIME_SCALING_ORDINAL, discountCurve.getTimeScaling());
        serializer.addToMessage(message, null, DISCOUNT_CURVE_PARAMETERS_ORDINAL, discountCurve.getParameter());
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public ForwardCurveNelsonSiegelSvensson buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(DISCOUNT_CURVE_NAME_ORDINAL);
      final LocalDate referenceDate;
      if (message.hasField(REFERENCE_DATE_ORDINAL)) {
        referenceDate = deserializer.fieldValueToObject(LocalDate.class, message.getByOrdinal(REFERENCE_DATE_ORDINAL));
      } else {
        referenceDate = null;
      }
      final String paymentOffsetCode = message.getString(PAYMENT_OFFSET_CODE_ORDINAL);
      final String businessDayCalendarName = message.getString(BUSINESS_DAY_CALENDAR_ORDINAL);
      final BusinessdayCalendarInterface paymentBusinessDayCalendar = FinmathBusinessDayFactory.of(businessDayCalendarName);
      final BusinessdayCalendarInterface.DateRollConvention paymentRollConvention =
          DateRollConvention.valueOf(message.getString(ROLL_DATE_CONVENTION_ORDINAL));
      final String dayCountConventionName = message.getString(DAY_COUNT_CONVENTION_ORDINAL);
      final DayCountConventionInterface dayCountConvention = FinmathDayCountFactory.of(dayCountConventionName);
      final double timeScaling = message.getDouble(DISCOUNT_CURVE_TIME_SCALING_ORDINAL);
      final double[] parameters = deserializer.fieldValueToObject(double[].class, message.getByOrdinal(DISCOUNT_CURVE_PARAMETERS_ORDINAL));
      return new ForwardCurveNelsonSiegelSvensson(name, referenceDate, paymentOffsetCode, paymentBusinessDayCalendar, paymentRollConvention,
          dayCountConvention, parameters, timeScaling);
    }

  }
}
