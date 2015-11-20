/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.serialization.fudge;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import net.finmath.marketdata.model.AnalyticModel;
import net.finmath.marketdata.model.AnalyticModelInterface;
import net.finmath.marketdata.model.curves.CurveInterface;
import net.finmath.marketdata.model.volatilities.VolatilitySurfaceInterface;
import net.finmath.time.Period;
import net.finmath.time.RegularSchedule;
import net.finmath.time.Schedule;
import net.finmath.time.Tenor;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationInterface;
import net.finmath.time.daycount.DayCountConventionInterface;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.integration.adapter.FinmathDateUtils;
import com.mcleodmoores.integration.adapter.FinmathDayCount;
import com.mcleodmoores.integration.adapter.FinmathDayCountFactory;

/**
 * Fudge builders for Finmath objects.
 */
/* package */ final class FinmathBuilders {
  /** The logger. */
  /* package */ static final Logger LOGGER = LoggerFactory.getLogger(FinmathBuilders.class);

  /**
   * Restricted constructor.
   */
  private FinmathBuilders() {
  }

  /**
   * Fudge builder for {@link Tenor}.
   */
  @FudgeBuilderFor(Tenor.class)
  public static class TenorBuilder implements FudgeBuilder<Tenor> {
    /** The reference date ordinal */
    private static final int REFERENCE_DATE_ORDINAL = 1;
    /** The dates ordinal */
    private static final int DATES_ORDINAL = 2;
    /** The dates field name */
    private static final String DATES_FIELD = "dates";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Tenor object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, null, REFERENCE_DATE_ORDINAL, FinmathDateUtils.convertToLocalDate(object.getReferenceDate()));
      try {
        final Field field = Tenor.class.getDeclaredField(DATES_FIELD);
        field.setAccessible(true);
        final Calendar[] calendar = (Calendar[]) field.get(object);
        final int length = calendar.length;
        final LocalDate[] dates = new LocalDate[length];
        for (int i = 0; i < length; i++) {
          dates[i] = FinmathDateUtils.convertToLocalDate(calendar[i]);
        }
        serializer.addToMessageWithClassHeaders(message, null, DATES_ORDINAL, dates);
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public Tenor buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final LocalDate[] dates = deserializer.fieldValueToObject(LocalDate[].class, message.getByOrdinal(DATES_ORDINAL));
      final int length = dates.length;
      final Calendar[] calendar = new Calendar[length];
      for (int i = 0; i < length; i++) {
        calendar[i] = FinmathDateUtils.convertLocalDate(dates[i]);
      }
      final Calendar referenceDate = FinmathDateUtils.convertLocalDate(deserializer.fieldValueToObject(LocalDate.class,
          message.getByOrdinal(REFERENCE_DATE_ORDINAL)));
      return new Tenor(calendar, referenceDate);
    }

  }


  /**
   * Fudge builder for {@link TimeDiscretization}.
   */
  @FudgeBuilderFor(TimeDiscretization.class)
  public static class TimeDiscretizationBuilder implements FudgeBuilder<TimeDiscretization> {
    /** The times ordinal */
    private static final int TIMES_ORDINAL = 1;

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final TimeDiscretization object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessageWithClassHeaders(message, null, TIMES_ORDINAL, object.getAsDoubleArray());
      return message;
    }

    @Override
    public TimeDiscretization buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] times = deserializer.fieldValueToObject(double[].class, message.getByOrdinal(TIMES_ORDINAL));
      return new TimeDiscretization(times);
    }

  }

  /**
   * Fudge builder for {@link AnalyticModel}.
   */
  @FudgeBuilderFor(AnalyticModel.class)
  public static class AnalyticModelBuilder implements FudgeBuilder<AnalyticModel> {
    /** The curve names ordinal */
    private static final int CURVE_NAMES_ORDINAL = 1;
    /** The curves ordinal */
    private static final int CURVES_ORDINAL = 2;
    /** The surfaces ordinal */
    private static final int SURFACES_ORDINAL = 3;
    /** The curves map field name */
    private static final String CURVES_MAP_FIELD = "curvesMap";
    /** The volatility surface map field name */
    private static final String VOLATILITY_SURFACE_MAP_FIELD = "volatilitySufaceMap";

    @SuppressWarnings("unchecked")
    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final AnalyticModel object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      try {
        final Field curvesField = AnalyticModel.class.getDeclaredField(CURVES_MAP_FIELD);
        final Field surfacesField = AnalyticModel.class.getDeclaredField(VOLATILITY_SURFACE_MAP_FIELD);
        AccessibleObject.setAccessible(new AccessibleObject[] {curvesField, surfacesField}, true);
        final Map<String, CurveInterface> curvesMap = (Map<String, CurveInterface>) curvesField.get(object);
        final Map<String, VolatilitySurfaceInterface> surfacesMap = (Map<String, VolatilitySurfaceInterface>) surfacesField.get(object);
        final int nCurves = curvesMap.size();
        final String[] curveNames = curvesMap.keySet().toArray(new String[nCurves]);
        final CurveInterface[] curves = curvesMap.values().toArray(new CurveInterface[nCurves]);
        serializer.addToMessageWithClassHeaders(message, null, CURVE_NAMES_ORDINAL, curveNames);
        serializer.addToMessageWithClassHeaders(message, null, CURVES_ORDINAL, curves);
        final VolatilitySurfaceInterface[] surfaces = surfacesMap.values().toArray(new VolatilitySurfaceInterface[surfacesMap.size()]);
        serializer.addToMessageWithClassHeaders(message, null, SURFACES_ORDINAL, surfaces);
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public AnalyticModel buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String[] curveNames = deserializer.fieldValueToObject(String[].class, message.getByOrdinal(CURVE_NAMES_ORDINAL));
      final CurveInterface[] curves = deserializer.fieldValueToObject(CurveInterface[].class, message.getByOrdinal(CURVES_ORDINAL));
      final VolatilitySurfaceInterface[] surfaces = deserializer.fieldValueToObject(VolatilitySurfaceInterface[].class, message.getByOrdinal(SURFACES_ORDINAL));
      AnalyticModelInterface model = new AnalyticModel();
      for (int i = 0; i < curveNames.length; i++) {
        model = model.addCurve(curveNames[i], curves[i]);
      }
      model = model.addVolatilitySurfaces(surfaces);
      return (AnalyticModel) model;
    }

  }

  /**
   * Fudge builder for {@link RegularSchedule}.
   */
  @FudgeBuilderFor(RegularSchedule.class)
  public static class RegularScheduleBuilder implements FudgeBuilder<RegularSchedule> {
    /** The time discretization ordinal */
    private static final int TIME_DISCRETIZATION_ORDINAL = 1;
    /** The time discretization field name */
    private static final String TIME_DISCRETIZATION_FIELD = "timeDiscretization";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final RegularSchedule object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      try {
        final Field field = RegularSchedule.class.getDeclaredField(TIME_DISCRETIZATION_FIELD);
        field.setAccessible(true);
        serializer.addToMessageWithClassHeaders(message, null, TIME_DISCRETIZATION_ORDINAL, field.get(object));
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public RegularSchedule buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final TimeDiscretizationInterface times = deserializer.fieldValueToObject(TimeDiscretizationInterface.class,
          message.getByOrdinal(TIME_DISCRETIZATION_ORDINAL));
      return new RegularSchedule(times);
    }

  }

  /**
   * Fudge builder for {@link Schedule}.
   */
  @FudgeBuilderFor(Schedule.class)
  public static class ScheduleBuilder implements FudgeBuilder<Schedule> {
    /** The reference date ordinal */
    private static final int REFERENCE_DATE_ORDINAL = 1;
    /** The periods ordinal */
    private static final int PERIODS_ORDINAL = 2;
    /** The day-count convention ordinal */
    private static final int DAY_COUNT_CONVENTION_ORDINAL = 3;

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Schedule object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, null, REFERENCE_DATE_ORDINAL, FinmathDateUtils.convertToLocalDate(object.getReferenceDate()));
      serializer.addToMessageWithClassHeaders(message, null, PERIODS_ORDINAL, object.getPeriods());
      final DayCountConventionInterface dayCountConvention = object.getDaycountconvention();
      if (dayCountConvention instanceof FinmathDayCount) {
        message.add(DAY_COUNT_CONVENTION_ORDINAL, ((FinmathDayCount) dayCountConvention).getName());
      } else {
        LOGGER.error("Could not serialize day count convention for forward curve {}, using DayCountConvention_ACT_ACT_ISDA", object);
        message.add(DAY_COUNT_CONVENTION_ORDINAL, "Act/Act ISDA"); //TODO factory needs to work which is the correct one
      }
      return message;
    }

    @Override
    public Schedule buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Calendar referenceDate = FinmathDateUtils.convertLocalDate(deserializer.fieldValueToObject(LocalDate.class,
          message.getByOrdinal(REFERENCE_DATE_ORDINAL)));
      final List<Period> periods = deserializer.fieldValueToObject(List.class, message.getByOrdinal(PERIODS_ORDINAL));
      final DayCountConventionInterface dayCount = FinmathDayCountFactory.of(message.getString(DAY_COUNT_CONVENTION_ORDINAL));
      return new Schedule(referenceDate, periods, dayCount);
    }

  }

  /**
   * Fudge builder for {@link Period}.
   */
  @FudgeBuilderFor(Period.class)
  public static class PeriodBuilder implements FudgeBuilder<Period> {
    /** The fixing ordinal */
    private static final int FIXING_ORDINAL = 1;
    /** The payment ordinal */
    private static final int PAYMENT_ORDINAL = 2;
    /** The period start ordinal */
    private static final int PERIOD_START_ORDINAL = 3;
    /** The period end ordinal */
    private static final int PERIOD_END_ORDINAL = 4;

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Period object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessage(message, null, FIXING_ORDINAL, FinmathDateUtils.convertToLocalDate(object.getFixing()));
      serializer.addToMessage(message, null, PAYMENT_ORDINAL, FinmathDateUtils.convertToLocalDate(object.getPayment()));
      serializer.addToMessage(message, null, PERIOD_START_ORDINAL, FinmathDateUtils.convertToLocalDate(object.getPeriodStart()));
      serializer.addToMessage(message, null, PERIOD_END_ORDINAL, FinmathDateUtils.convertToLocalDate(object.getPeriodEnd()));
      return message;
    }

    @Override
    public Period buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Calendar fixing = FinmathDateUtils.convertLocalDate(deserializer.fieldValueToObject(LocalDate.class, message.getByOrdinal(FIXING_ORDINAL)));
      final Calendar payment = FinmathDateUtils.convertLocalDate(deserializer.fieldValueToObject(LocalDate.class, message.getByOrdinal(PAYMENT_ORDINAL)));
      final Calendar periodStart = FinmathDateUtils.convertLocalDate(deserializer.fieldValueToObject(LocalDate.class,
          message.getByOrdinal(PERIOD_START_ORDINAL)));
      final Calendar periodEnd = FinmathDateUtils.convertLocalDate(deserializer.fieldValueToObject(LocalDate.class, message.getByOrdinal(PERIOD_END_ORDINAL)));
      return new Period(fixing, payment, periodStart, periodEnd);
    }

  }
}
