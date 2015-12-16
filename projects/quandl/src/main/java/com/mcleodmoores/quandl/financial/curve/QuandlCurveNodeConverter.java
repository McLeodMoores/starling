/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.financial.curve;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.quandl.convention.QuandlFedFundsFutureConvention;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.financial.analytics.conversion.CurveNodeConverter;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class QuandlCurveNodeConverter extends CurveNodeConverter {
  private final ConventionSource _conventionSource;

  /**
   * @param conventionSource  the convention source, not null
   */
  public QuandlCurveNodeConverter(final ConventionSource conventionSource) {
    super(conventionSource);
    _conventionSource = conventionSource;
  }

  /**
   * Given an {@link InstrumentDefinition} (the time-independent form used in the analytics library) and a valuation time, converts to the
   * time-dependent {@link InstrumentDerivative} form.
   * @param node The curve node, not null
   * @param definition The definition, not null
   * @param now The valuation time, not null
   * @param timeSeries A fixing time series, not null if {@link #requiresFixingSeries(CurveNode)} is true and definition is an instance of
   * {@link InstrumentDefinitionWithData}.
   * @return A derivative instrument
   */
  @Override
  @SuppressWarnings("unchecked")
  public InstrumentDerivative getDerivative(final CurveNodeWithIdentifier node, final InstrumentDefinition<?> definition, final ZonedDateTime now,
      final HistoricalTimeSeriesBundle timeSeries) {
    ArgumentChecker.notNull(node, "node");
    ArgumentChecker.notNull(definition, "definition");
    ArgumentChecker.notNull(now, "now");
    if (definition instanceof InstrumentDefinitionWithData<?, ?> && requiresFixingSeries(node.getCurveNode())) {
      if (definition instanceof FederalFundsFutureTransactionDefinition) {
        ArgumentChecker.notNull(timeSeries, "timeSeries");
        final RateFutureNode fedFundsFutureNode = (RateFutureNode) node.getCurveNode();
        final Convention convention = ConventionLink.resolvable(fedFundsFutureNode.getFutureConvention()).resolve();
        if (convention instanceof QuandlFedFundsFutureConvention) {
          // TODO this code will throw exceptions if the convention is not available
          final QuandlFedFundsFutureConvention fedFundsConvention = (QuandlFedFundsFutureConvention) convention;
          // Retrieving id of the underlying index.
          HistoricalTimeSeries historicalTimeSeriesUnderlyingIndex = timeSeries.get(node.getDataField(), fedFundsConvention.getUnderlyingConventionId());
          if (historicalTimeSeriesUnderlyingIndex == null) {
            // try the external ids of the underlying convention in case the convention id used in the Fed funds convention
            // is not the overnight index ticker
            final OvernightIndexConvention underlyingConvention = ConventionLink.resolvable(fedFundsConvention.getUnderlyingConventionId(),
                OvernightIndexConvention.class).resolve();
            historicalTimeSeriesUnderlyingIndex = timeSeries.get(node.getDataField(), underlyingConvention.getExternalIdBundle());
            if (historicalTimeSeriesUnderlyingIndex == null) {
              throw new OpenGammaRuntimeException("Could not get price time series for " + fedFundsConvention.getUnderlyingConventionId()
                  + " or " + underlyingConvention.getExternalIdBundle());
            }
          }
          final DoubleTimeSeries<ZonedDateTime>[] tsArray = new DoubleTimeSeries[1];
          tsArray[0] = convertTimeSeries(now.getZone(), historicalTimeSeriesUnderlyingIndex.getTimeSeries());
          // No time series is passed for the closing price; for curve calibration only the trade price is required.
          final InstrumentDefinitionWithData<?, DoubleTimeSeries<ZonedDateTime>[]> definitonInstWithData =
              (InstrumentDefinitionWithData<?, DoubleTimeSeries<ZonedDateTime>[]>) definition;
          return definitonInstWithData.toDerivative(now, tsArray);
        }
      }
    }
    return super.getDerivative(node, definition, now, timeSeries);
  }

  //TODO expose in superclass
  private static ZonedDateTimeDoubleTimeSeries convertTimeSeries(final ZoneId timeZone, final LocalDateDoubleTimeSeries localDateTS) {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(timeZone);
    for (final LocalDateDoubleEntryIterator it = localDateTS.iterator(); it.hasNext();) {
      final LocalDate date = it.nextTime();
      final ZonedDateTime zdt = date.atStartOfDay(timeZone);
      bld.put(zdt, it.currentValueFast());
    }
    return bld.build();
  }
}
