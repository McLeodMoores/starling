/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.temporal.ChronoUnit;

import com.opengamma.analytics.math.curve.NodalObjectsCurve;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.time.Tenor;

/**
 * Formats {@link NodalObjectsCurve}s with {@link Tenor} x values and {@link Double} y values.
 */
@SuppressWarnings("rawtypes")
/* package */ class NodalObjectsCurveFormatter extends AbstractFormatter<NodalObjectsCurve> {

  private static final Logger s_logger = LoggerFactory.getLogger(NodalObjectsCurveFormatter.class);

  NodalObjectsCurveFormatter() {
    super(NodalObjectsCurve.class);
    addFormatter(new Formatter<NodalObjectsCurve>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final NodalObjectsCurve value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  @Override
  public List<Double[]> formatCell(final NodalObjectsCurve value, final ValueSpecification valueSpec, final Object inlineKey) {
    if (value.size() != 0 && value.getXData()[0] instanceof Tenor && value.getYData()[0] instanceof Double) {
      final Tenor[] tenors = (Tenor[]) value.getXData();
      final Object[] ys = value.getYData();
      ParallelArrayBinarySort.parallelBinarySort(tenors, ys);
      final List<Double[]> data = new ArrayList<>();
      for (int i = 0; i < tenors.length; i++) {
        final double x = tenors[i].getPeriod().get(ChronoUnit.YEARS);
        data.add(new Double[] {x, (Double) ys[i]});
      }
      return data;
    } else {
      s_logger.info("Unable to format curve {}", value);
      return null;
    }
  }

  private List<Double[]> formatExpanded(final NodalObjectsCurve value) {
    if (value.size() != 0 && value.getXData()[0] instanceof Tenor && value.getYData()[0] instanceof Double) {
      final Tenor[] tenors = (Tenor[]) value.getXData();
      final Object[] ys = value.getYData();
      ParallelArrayBinarySort.parallelBinarySort(tenors, ys);
      final int dataLength = tenors.length;
      final Double[] xs = new Double[dataLength];
      for (int i = 0; i < dataLength; i++) {
        xs[i] = (double) tenors[i].getPeriod().get(ChronoUnit.YEARS);
      }
      final List<Double[]> detailedData = new ArrayList<>();
      for (int i = 0; i < ys.length; i++) {
        detailedData.add(new Double[]{xs[i], (Double) ys[i]});
      }
      return detailedData;
    } else {
      s_logger.info("Unable to format curve {}", value);
      return null;
    }
  }

  @Override
  public DataType getDataType() {
    return DataType.CURVE;
  }
}
