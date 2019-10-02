/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.test;

import java.net.URL;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LogNaturalCubicMonotonicityPreservingInterpolator1D;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LogLinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.MonotonicLogNaturalCubicSplineInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.csv.CSVDocumentReader;

import au.com.bytecode.opencsv.CSVParser;

public class IRCurveParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(IRSwapTradeParser.class);

  private static final String CURVE_NAME = "Curve Name";
  private static final String THREE_MONTHS = "3M";
  private static final String SIX_MONTHS = "6M";
  private static final String NINE_MONTHS = "9M";
  private static final String ONE_YEAR = "1Y";
  private static final String FIFTEEN_MONTHS = "15M";
  private static final String HEIGHTEEN_MONTHS = "18M";
  private static final String TWENTY_ONE_MONTHS = "21M";
  private static final String TWO_YEARS = "2Y";
  private static final String THREE_YEARS = "3Y";
  private static final String FOUR_YEARS = "4Y";
  private static final String FIVE_YEARS = "5Y";
  private static final String SIX_YEARS = "6Y";
  private static final String SEVEN_YEARS = "7Y";
  private static final String HEIGHT_YEARS = "8Y";
  private static final String NINE_YEARS = "9Y";
  private static final String TEN_YEARS = "10Y";
  private static final String TWELVE_YEARS = "12Y";
  private static final String FIFTEEN_YEARS = "15Y";
  private static final String TWENTY_YEARS = "20Y";
  private static final String TWENTY_FIVE_YEARS = "25Y";
  private static final String THIRTY_YEARS = "30Y";
  private static final String FORTY_YEARS = "40Y";
  private static final String FIFTY_YEARS = "50Y";
  private static final String[] DATES = new String[] { THREE_MONTHS, SIX_MONTHS, NINE_MONTHS, ONE_YEAR, FIFTEEN_MONTHS, HEIGHTEEN_MONTHS, TWENTY_ONE_MONTHS,
      TWO_YEARS, THREE_YEARS, FOUR_YEARS, FIVE_YEARS, SIX_YEARS, SEVEN_YEARS, HEIGHT_YEARS, NINE_YEARS, TEN_YEARS,
      TWELVE_YEARS, FIFTEEN_YEARS, TWENTY_YEARS, TWENTY_FIVE_YEARS, THIRTY_YEARS, FORTY_YEARS, FIFTY_YEARS };
  public static final LogNaturalCubicMonotonicityPreservingInterpolator1D LOG_NATURAL_CUBIC_MONOTONE_INSTANCE = new LogNaturalCubicMonotonicityPreservingInterpolator1D();
  private static final double[] TIMES = { 0.249144422, 0.501026694, 0.750171116, 0.999315537, 1.25119781, 1.500342231,
      1.749486653, 2.001368925, 3.000684463, 4, 4.999315537, 6.001368925, 7.000684463, 8, 8.999315537, 10.00136893,
      12, 15.00068446, 20, 24.99931554, 30.00136893, 40, 50.00136893 };

  public List<InterpolatedDoublesCurve> parseCSVFile(final URL fileUrl) {
    ArgumentChecker.notNull(fileUrl, "fileUrl");

    final List<InterpolatedDoublesCurve> curves = Lists.newArrayList();
    final CSVDocumentReader csvDocumentReader = new CSVDocumentReader(fileUrl, CSVParser.DEFAULT_SEPARATOR,
        CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, new FudgeContext());

    final List<FudgeMsg> rowsWithError = Lists.newArrayList();
    for (final FudgeMsg row : csvDocumentReader) {
      try {
        curves.add(createCurve(row));
      } catch (final Exception ex) {
        ex.printStackTrace();
        rowsWithError.add(row);
      }
    }

    LOGGER.warn("Total unprocessed rows: {}", rowsWithError.size());
    for (final FudgeMsg fudgeMsg : rowsWithError) {
      LOGGER.warn("{}", fudgeMsg);
    }
    return curves;
  }

  private static InterpolatedDoublesCurve createCurve(final FudgeMsg row) {
    final double[] discountFactors = new double[DATES.length];
    for (int i = 0; i < DATES.length; i++) {
      discountFactors[i] = row.getDouble(DATES[i]);
    }
    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(TIMES, discountFactors,
        NamedInterpolator1dFactory.of(MonotonicLogNaturalCubicSplineInterpolator1dAdapter.NAME, LogLinearExtrapolator1dAdapter.NAME,
            LinearExtrapolator1dAdapter.NAME),
        true, row.getString(CURVE_NAME));
    return curve;
  }

}
