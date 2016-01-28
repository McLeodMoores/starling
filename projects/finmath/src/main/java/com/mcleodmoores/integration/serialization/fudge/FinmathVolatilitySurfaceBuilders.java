/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.serialization.fudge;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.joda.time.LocalDate;

import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.CurveInterface;
import net.finmath.marketdata.model.curves.DiscountCurveInterface;
import net.finmath.marketdata.model.curves.ForwardCurveInterface;
import net.finmath.marketdata.model.volatilities.AbstractVolatilitySurface;
import net.finmath.marketdata.model.volatilities.CapletVolatilities;
import net.finmath.marketdata.model.volatilities.CapletVolatilitiesParametric;
import net.finmath.marketdata.model.volatilities.CapletVolatilitiesParametricFourParameterPicewiseConstant;
import net.finmath.marketdata.model.volatilities.SwaptionMarketData;
import net.finmath.marketdata.model.volatilities.VolatilitySurfaceInterface.QuotingConvention;
import net.finmath.time.Tenor;
import net.finmath.time.TimeDiscretizationInterface;

/**
 * Fudge builders for classes that extend {@link net.finmath.marketdata.model.volatilities.AbstractVolatilitySurface}.
 */
/* package */ final class FinmathVolatilitySurfaceBuilders {
  /** The point class name - used to get the x, y, and parameter points from curves */
  private static final String POINT_CLASS_NAME = "net.finmath.marketdata.model.curves.Curve$Point";
  /** The name of the points field in Curves.Point */
  private static final String POINTS_FIELD_NAME = "points";
  /** The name of the time field in Curves.Point */
  private static final String TIME_FIELD_NAME = "time";
  /** The name of the value field in Curves.Point */
  private static final String VALUE_FIELD_NAME = "value";

  /**
   * Restricted constructor.
   */
  private FinmathVolatilitySurfaceBuilders() {
  }

  /**
   * Fudge builder for {@link CapletVolatilitiesParametric}.
   */
  @FudgeBuilderFor(CapletVolatilitiesParametric.class)
  public static class CapletVolatilitiesParametricBuilder implements FudgeBuilder<CapletVolatilitiesParametric> {
    /** The name field ordinal */
    private static final int NAME_ORDINAL = 1;
    /** The time scaling field ordinal */
    private static final int TIME_SCALING_ORDINAL = 2;
    /** The parameters field ordinal */
    private static final int PARAMETERS_ORDINAL = 3;
    /** The reference date field ordinal */
    private static final int REFERENCE_DATE_ORDINAL = 4;
    /** The time scaling field name */
    private static final String TIME_SCALING_FIELD = "timeScaling";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CapletVolatilitiesParametric object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(NAME_ORDINAL, object.getName());
      if (object.getReferenceDate() != null) {
        serializer.addToMessage(message, null, REFERENCE_DATE_ORDINAL, object.getReferenceDate());
      }
      try {
        final Field field = CapletVolatilitiesParametric.class.getDeclaredField(TIME_SCALING_FIELD);
        field.setAccessible(true);
        final double timeScaling = (double) field.get(object);
        message.add(TIME_SCALING_ORDINAL, timeScaling);
        serializer.addToMessage(message, null, PARAMETERS_ORDINAL, object.getParameter());
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public CapletVolatilitiesParametric buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_ORDINAL);
      LocalDate referenceDate;
      if (message.hasField(REFERENCE_DATE_ORDINAL)) {
        referenceDate = deserializer.fieldValueToObject(LocalDate.class, message.getByOrdinal(REFERENCE_DATE_ORDINAL));
      } else {
        referenceDate = null;
      }
      final double[] parameters = deserializer.fieldValueToObject(double[].class, message.getByOrdinal(PARAMETERS_ORDINAL));
      final double timeScaling = message.getDouble(TIME_SCALING_ORDINAL);
      return new CapletVolatilitiesParametric(name, referenceDate, parameters[0], parameters[1], parameters[2], parameters[3], timeScaling);
    }

  }

  /**
   * Fudge builder for {@link CapletVolatilities}.
   */
  @FudgeBuilderFor(CapletVolatilities.class)
  public static class CapletVolatilitiesBuilder implements FudgeBuilder<CapletVolatilities> {
    /** The name field ordinal */
    private static final int NAME_ORDINAL = 1;
    /** The caplet volatilities ordinal */
    private static final int CAPLET_VOLATILITIES_ORDINAL = 2;
    /** The forward curve field ordinal */
    private static final int FORWARD_CURVE_ORDINAL = 3;
    /** The discount curve ordinal */
    private static final int DISCOUNT_CURVE_ORDINAL = 4;
    /** The maturities ordinal */
    private static final int MATURITIES_ORDINAL = 5;
    /** The strikes ordinal */
    private static final int STRIKES_ORDINAL = 6;
    /** The volatilities ordinal */
    private static final int VOLATILITIES_ORDINAL = 7;
    /** The quoting convention ordinal */
    private static final int QUOTING_CONVENTION_ORDINAL = 8;
    /** The reference date field ordinal */
    private static final int REFERENCE_DATE_ORDINAL = 9;
    /** The caplet volatilities field name */
    private static final String CAPLET_VOLATILITIES_FIELD = "capletVolatilities";
    /** The forward curve field name */
    private static final String FORWARD_CURVE_FIELD = "forwardCurve";
    /** The discount curve field name */
    private static final String DISCOUNT_CURVE_FIELD = "discountCurve";
    /** The quoting convention field name */
    private static final String QUOTING_CONVENTION_FIELD = "quotingConvention";

    @SuppressWarnings("unchecked")
    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CapletVolatilities object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(NAME_ORDINAL, object.getName());
      if (object.getReferenceDate() != null) {
        serializer.addToMessage(message, null, REFERENCE_DATE_ORDINAL, object.getReferenceDate());
      }
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
        final Field capletVolatilitiesField = CapletVolatilities.class.getDeclaredField(CAPLET_VOLATILITIES_FIELD);
        final Field forwardCurveField = AbstractVolatilitySurface.class.getDeclaredField(FORWARD_CURVE_FIELD);
        final Field discountCurveField = AbstractVolatilitySurface.class.getDeclaredField(DISCOUNT_CURVE_FIELD);
        final Field quotingConventionField = AbstractVolatilitySurface.class.getDeclaredField(QUOTING_CONVENTION_FIELD);
        AccessibleObject.setAccessible(new AccessibleObject[] {capletVolatilitiesField, forwardCurveField, discountCurveField, quotingConventionField}, true);
        final Map<Double, CurveInterface> capletVolatilities = (Map<Double, CurveInterface>) capletVolatilitiesField.get(object);
        final int capletsSize = capletVolatilities.size();
        final double[] maturities = new double[capletsSize];
        final CurveInterface[] curves = new CurveInterface[capletsSize];
        final double[] strikes = new double[capletsSize];
        final double[] volatilities = new double[capletsSize];
        int i = 0;
        for (final Map.Entry<Double, CurveInterface> entry : capletVolatilities.entrySet()) {
          maturities[i] = entry.getKey();
          final CurveInterface curve = entry.getValue();
          final Field pointsField = Curve.class.getDeclaredField(POINTS_FIELD_NAME);
          final Field strikesField = pointsClass.getDeclaredField(TIME_FIELD_NAME);
          final Field maturitiesField = pointsClass.getDeclaredField(VALUE_FIELD_NAME);
          AccessibleObject.setAccessible(new AccessibleObject[] {pointsField, strikesField, maturitiesField, capletVolatilitiesField,
            forwardCurveField, discountCurveField, quotingConventionField}, true);
          final ArrayList<?> pointsList = (ArrayList<?>) pointsField.get(curve);
          if (pointsList.size() != 1) {
            throw new IllegalStateException("Have more than one strike / volatility point for " + entry.getKey() + ": should not happen");
          }
          final Object points = pointsList.get(0);
          strikes[i] = (Double) strikesField.get(points);
          volatilities[i] = (Double) maturitiesField.get(points);
          curves[i++] = curve;
        }
        serializer.addToMessage(message, null, MATURITIES_ORDINAL, maturities);
        serializer.addToMessageWithClassHeaders(message, null, CAPLET_VOLATILITIES_ORDINAL, curves);
        serializer.addToMessage(message, null, STRIKES_ORDINAL, strikes);
        serializer.addToMessage(message, null, VOLATILITIES_ORDINAL, volatilities);
        serializer.addToMessageWithClassHeaders(message, null, FORWARD_CURVE_ORDINAL, forwardCurveField.get(object));
        serializer.addToMessageWithClassHeaders(message, null, DISCOUNT_CURVE_ORDINAL, discountCurveField.get(object));
        message.add(QUOTING_CONVENTION_ORDINAL, ((QuotingConvention) quotingConventionField.get(object)).name());
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public CapletVolatilities buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_ORDINAL);
      LocalDate referenceDate;
      if (message.hasField(REFERENCE_DATE_ORDINAL)) {
        referenceDate = deserializer.fieldValueToObject(LocalDate.class, message.getByOrdinal(REFERENCE_DATE_ORDINAL));
      } else {
        referenceDate = null;
      }
      final ForwardCurveInterface forwardCurve = deserializer.fieldValueToObject(ForwardCurveInterface.class, message.getByOrdinal(FORWARD_CURVE_ORDINAL));
      final double[] maturities = deserializer.fieldValueToObject(double[].class, message.getByOrdinal(MATURITIES_ORDINAL));
      final double[] strikes = deserializer.fieldValueToObject(double[].class, message.getByOrdinal(STRIKES_ORDINAL));
      final double[] logVolatilities = deserializer.fieldValueToObject(double[].class, message.getByOrdinal(VOLATILITIES_ORDINAL));
      final double[] volatilities = new double[logVolatilities.length];
      for (int i = 0; i < logVolatilities.length; i++) {
        volatilities[i] = Math.exp(logVolatilities[i]);
      }
      final QuotingConvention quotingConvention = QuotingConvention.valueOf(message.getString(QUOTING_CONVENTION_ORDINAL));
      final DiscountCurveInterface discountCurve = deserializer.fieldValueToObject(DiscountCurveInterface.class, message.getByOrdinal(DISCOUNT_CURVE_ORDINAL));
      return new CapletVolatilities(name, referenceDate, forwardCurve, maturities, strikes, volatilities, quotingConvention, discountCurve);
    }

  }

  /**
   * Fudge builder for {@link CapletVolatilitiesParametricFourParameterPicewiseConstant}.
   */
  @FudgeBuilderFor(CapletVolatilitiesParametricFourParameterPicewiseConstant.class)
  public static class CapletVolatilitiesParametricFourParameterPiecewiseConstantBuilder
    implements FudgeBuilder<CapletVolatilitiesParametricFourParameterPicewiseConstant> {
    /** The name field ordinal */
    private static final int NAME_ORDINAL = 1;
    /** The first parameter ordinal */
    private static final int A_ORDINAL = 2;
    /** The second parameter ordinal */
    private static final int B_ORDINAL = 3;
    /** The third parameter ordinal */
    private static final int C_ORDINAL = 4;
    /** The fourth parameter ordinal */
    private static final int D_ORDINAL = 5;
    /** The time discretization ordinal */
    private static final int TIME_DISCRETIZATION_ORDINAL = 6;
    /** The reference date field ordinal */
    private static final int REFERENCE_DATE_ORDINAL = 7;
    /** The first parameter field name */
    private static final String A_FIELD_NAME = "a";
    /** The second parameter field name */
    private static final String B_FIELD_NAME = "b";
    /** The third parameter field name */
    private static final String C_FIELD_NAME = "c";
    /** The fourth parameter field name */
    private static final String D_FIELD_NAME = "d";
    /** The time discretization field name */
    private static final String TIME_DISCRETIZATION_FIELD_NAME = "timeDiscretization";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CapletVolatilitiesParametricFourParameterPicewiseConstant object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(NAME_ORDINAL, object.getName());
      if (object.getReferenceDate() != null) {
        serializer.addToMessage(message, null, REFERENCE_DATE_ORDINAL, object.getReferenceDate());
      }
      try {
        final Field aField = CapletVolatilitiesParametricFourParameterPicewiseConstant.class.getDeclaredField(A_FIELD_NAME);
        final Field bField = CapletVolatilitiesParametricFourParameterPicewiseConstant.class.getDeclaredField(B_FIELD_NAME);
        final Field cField = CapletVolatilitiesParametricFourParameterPicewiseConstant.class.getDeclaredField(C_FIELD_NAME);
        final Field dField = CapletVolatilitiesParametricFourParameterPicewiseConstant.class.getDeclaredField(D_FIELD_NAME);
        final Field timeDiscretizationField = CapletVolatilitiesParametricFourParameterPicewiseConstant.class.getDeclaredField(TIME_DISCRETIZATION_FIELD_NAME);
        AccessibleObject.setAccessible(new AccessibleObject[] {aField, bField, cField, dField, timeDiscretizationField}, true);
        message.add(A_ORDINAL, aField.get(object));
        message.add(B_ORDINAL, bField.get(object));
        message.add(C_ORDINAL, cField.get(object));
        message.add(D_ORDINAL, dField.get(object));
        serializer.addToMessageWithClassHeaders(message, null, TIME_DISCRETIZATION_ORDINAL, timeDiscretizationField.get(object));
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public CapletVolatilitiesParametricFourParameterPicewiseConstant buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_ORDINAL);
      LocalDate referenceDate;
      if (message.hasField(REFERENCE_DATE_ORDINAL)) {
        referenceDate = deserializer.fieldValueToObject(LocalDate.class, message.getByOrdinal(REFERENCE_DATE_ORDINAL));
      } else {
        referenceDate = null;
      }
      final double a = message.getDouble(A_ORDINAL);
      final double b = message.getDouble(B_ORDINAL);
      final double c = message.getDouble(C_ORDINAL);
      final double d = message.getDouble(D_ORDINAL);
      final Tenor timeDiscretization = deserializer.fieldValueToObject(Tenor.class, message.getByOrdinal(TIME_DISCRETIZATION_ORDINAL));
      return new CapletVolatilitiesParametricFourParameterPicewiseConstant(name, referenceDate, a, b, c, d, timeDiscretization);
    }

  }

  /**
   * Fudge builder for {@link SwaptionMarketData}.
   */
  @FudgeBuilderFor(SwaptionMarketData.class)
  public static class SwaptionMarketDataBuilder implements FudgeBuilder<SwaptionMarketData> {
    /** The forward curve ordinal */
    private static final int FORWARD_CURVE_ORDINAL = 1;
    /** The discount curve ordinal */
    private static final int DISCOUNT_CURVE_ORDINAL = 2;
    /** The option maturities ordinal */
    private static final int OPTION_MATURITIES_ORDINAL = 3;
    /** The tenor ordinal */
    private static final int TENOR_ORDINAL = 4;
    /** The swap period length ordinal */
    private static final int SWAP_PERIOD_LENGTH_ORDINAL = 5;
    /** The implied volatilities ordinal */
    private static final int IMPLIED_VOLATILITIES_ORDINAL = 6;
    /** The forward curve field */
    private static final String FORWARD_CURVE_FIELD = "forwardCurve";
    /** The discount curve field */
    private static final String DISCOUNT_CURVE_FIELD = "discountCurve";
    /** The implied volatilities field */
    private static final String IMPLIED_VOLATILITIES_FIELD = "impliedVolatilities";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SwaptionMarketData object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      try {
        serializer.addToMessageWithClassHeaders(message, null, OPTION_MATURITIES_ORDINAL, object.getOptionMaturities());
        serializer.addToMessageWithClassHeaders(message, null, TENOR_ORDINAL, object.getTenor());
        message.add(SWAP_PERIOD_LENGTH_ORDINAL, object.getSwapPeriodLength());
        final Field forwardCurveField = SwaptionMarketData.class.getDeclaredField(FORWARD_CURVE_FIELD);
        final Field discountCurveField = SwaptionMarketData.class.getDeclaredField(DISCOUNT_CURVE_FIELD);
        final Field impliedVolatilitiesField = SwaptionMarketData.class.getDeclaredField(IMPLIED_VOLATILITIES_FIELD);
        AccessibleObject.setAccessible(new AccessibleObject[] {forwardCurveField, discountCurveField, impliedVolatilitiesField}, true);
        serializer.addToMessageWithClassHeaders(message, null, FORWARD_CURVE_ORDINAL, forwardCurveField.get(object));
        serializer.addToMessageWithClassHeaders(message, null, DISCOUNT_CURVE_ORDINAL, discountCurveField.get(object));
        serializer.addToMessageWithClassHeaders(message, null, IMPLIED_VOLATILITIES_ORDINAL, impliedVolatilitiesField.get(object));
        return message;
      } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
        throw new FudgeRuntimeException("Could not serialize " + object, e);
      }
    }

    @Override
    public SwaptionMarketData buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final ForwardCurveInterface forwardCurve = deserializer.fieldValueToObject(ForwardCurveInterface.class, message.getByOrdinal(FORWARD_CURVE_ORDINAL));
      final DiscountCurveInterface discountCurve = deserializer.fieldValueToObject(DiscountCurveInterface.class, message.getByOrdinal(DISCOUNT_CURVE_ORDINAL));
      final TimeDiscretizationInterface optionMaturities = deserializer.fieldValueToObject(TimeDiscretizationInterface.class,
          message.getByOrdinal(OPTION_MATURITIES_ORDINAL));
      final TimeDiscretizationInterface tenor = deserializer.fieldValueToObject(TimeDiscretizationInterface.class, message.getByOrdinal(TENOR_ORDINAL));
      final double swapPeriodLength = message.getDouble(SWAP_PERIOD_LENGTH_ORDINAL);
      final double[][] impliedVolatilities = deserializer.fieldValueToObject(double[][].class, message.getByOrdinal(IMPLIED_VOLATILITIES_ORDINAL));
      return new SwaptionMarketData(forwardCurve, discountCurve, optionMaturities, tenor, swapPeriodLength, impliedVolatilities);
    }

  }
}
