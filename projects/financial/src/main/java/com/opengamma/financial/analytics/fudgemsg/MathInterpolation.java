/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.FlatExtrapolator1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LinearExtrapolator1D;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1d;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;

/**
 * Holds Fudge builders for the interpolation model.
 */
/* package */final class MathInterpolation {

  /**
   * Restricted constructor.
   */
  private MathInterpolation() {
  }

  //-------------------------------------------------------------------------
  /**
   * Fudge builder for {@code Interpolator1D}.
   */
  @GenericFudgeBuilderFor(Interpolator1D.class)
  public static final class Interpolator1DBuilder implements FudgeBuilder<Interpolator1D> {
    private static final String TYPE_FIELD_NAME = "type";
    private static final String FLAG = "namedInterpolator";

    @SuppressWarnings("deprecation")
    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Interpolator1D object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(0, Interpolator1D.class.getName());
      if (object instanceof NamedInterpolator1d) {
        message.add(FLAG, Boolean.TRUE);
        message.add(TYPE_FIELD_NAME, ((NamedInterpolator1d) object).getName());
      } else {
        // backwards compatibility
        message.add(TYPE_FIELD_NAME, Interpolator1DFactory.getInterpolatorName(object));
        message.add(FLAG, Boolean.FALSE);
      }
      return message;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Interpolator1D buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      if (message.hasField(FLAG)) {
        final Boolean flag = message.getBoolean(FLAG);
        if (flag) {
          return NamedInterpolator1dFactory.of(message.getFieldValue(String.class, message.getByName(TYPE_FIELD_NAME)));
        }
        // backwards compatibility
        return Interpolator1DFactory.getInterpolator(message.getFieldValue(String.class, message.getByName(TYPE_FIELD_NAME)));
      }
      try {
        return NamedInterpolator1dFactory.of(message.getFieldValue(String.class, message.getByName(TYPE_FIELD_NAME)));
      } catch (final Exception e) {
        // backwards compatibility
        return Interpolator1DFactory.getInterpolator(message.getFieldValue(String.class, message.getByName(TYPE_FIELD_NAME)));
      }
    }
  }

  /**
   * Fudge builder for {@code CombinedInterpolatorExtrapolator}.
   */
  @FudgeBuilderFor(CombinedInterpolatorExtrapolator.class)
  public static final class CombinedInterpolatorExtrapolatorBuilder extends AbstractFudgeBuilder<CombinedInterpolatorExtrapolator> {
    private static final String LEFT_EXTRAPOLATOR_FIELD_NAME = "leftExtrapolator";
    private static final String RIGHT_EXTRAPOLATOR_FIELD_NAME = "rightExtrapolator";
    private static final String INTERPOLATOR_FIELD_NAME = "interpolator";
    private static final String FLAG = "namedInterpolator";

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final CombinedInterpolatorExtrapolator object) {
      final Interpolator1D interpolator = object.getInterpolator();
      message.add(INTERPOLATOR_FIELD_NAME, Interpolator1DFactory.getInterpolatorName(interpolator));
      message.add(LEFT_EXTRAPOLATOR_FIELD_NAME, Interpolator1DFactory.getInterpolatorName(object.getLeftExtrapolator()));
      message.add(RIGHT_EXTRAPOLATOR_FIELD_NAME, Interpolator1DFactory.getInterpolatorName(object.getRightExtrapolator()));
    }

    @Override
    public CombinedInterpolatorExtrapolator buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String interpolatorName = message.getString(INTERPOLATOR_FIELD_NAME);
      final String leftExtrapolatorName = message.getString(LEFT_EXTRAPOLATOR_FIELD_NAME);
      final String rightExtrapolatorName = message.getString(RIGHT_EXTRAPOLATOR_FIELD_NAME);
      final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
      final Interpolator1D leftExtrapolator = getExtrapolator(leftExtrapolatorName, interpolator);
      final Interpolator1D rightExtrapolator = getExtrapolator(rightExtrapolatorName, interpolator);
      return new CombinedInterpolatorExtrapolator(interpolator, leftExtrapolator, rightExtrapolator);
    }

    private Interpolator1D getExtrapolator(final String extrapolatorName, final Interpolator1D interpolator) {
      if (extrapolatorName.equals(Interpolator1DFactory.FLAT_EXTRAPOLATOR)) {
        return new FlatExtrapolator1D();
      }
      if (extrapolatorName.equals(Interpolator1DFactory.LINEAR_EXTRAPOLATOR)) {
        return new LinearExtrapolator1D(interpolator);
      }
      return Interpolator1DFactory.getInterpolator(extrapolatorName);
    }
  }

  /**
   * Fudge builder for {@code GridInterpolator2D}.
   */
  @GenericFudgeBuilderFor(GridInterpolator2D.class)
  public static final class GridInterpolator2DBuilder extends AbstractFudgeBuilder<GridInterpolator2D> {
    private static final String X_FIELD_NAME = "x";
    private static final String Y_FIELD_NAME = "y";

    @Override
    public void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final GridInterpolator2D object) {
      serializer.addToMessage(message, X_FIELD_NAME, null, object.getXInterpolator());
      serializer.addToMessage(message, Y_FIELD_NAME, null, object.getYInterpolator());
    }

    @Override
    public GridInterpolator2D buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      return new GridInterpolator2D(deserializer.fieldValueToObject(Interpolator1D.class, message.getByName(X_FIELD_NAME)),
          deserializer.fieldValueToObject(Interpolator1D.class, message.getByName(Y_FIELD_NAME)));
    }
  }
}
