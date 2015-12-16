/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.financial.curve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.quandl.convention.QuandlFedFundsFutureConvention;
import com.mcleodmoores.quandl.convention.QuandlFinancialConventionVisitor;
import com.mcleodmoores.quandl.convention.QuandlStirFutureConvention;
import com.mcleodmoores.quandl.future.FutureExpiryCalculator;
import com.mcleodmoores.quandl.future.QuandlFutureUtils;
import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.analytics.curve.ConverterUtils;
import com.opengamma.financial.analytics.curve.RateFutureNodeConverter;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalId;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Expiry;

/**
 * Converts {@link RateFutureNode}s to {@link InterestRateFutureTransactionDefinition}. This class extends the functionality of the parent
 * class by handling nodes that refer to {@link QuandlStirFutureConvention} and {@link QuandlFedFundsFutureConvention}.
 */
public class QuandlRateFutureNodeConverter extends RateFutureNodeConverter {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlRateFutureNodeConverter.class);
  /** The security source */
  private final SecuritySource _securitySource;
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The holiday source */
  private final HolidaySource _holidaySource;
  /** The region source */
  private final RegionSource _regionSource;
  /** The market data */
  private final SnapshotDataBundle _marketData;
  /** The market data id */
  private final ExternalId _dataId;
  /** The valuation time */
  private final ZonedDateTime _valuationTime;
  /** Calculates the expiry date of the nth future given a month code and expiry year */
  private final QuandlFinancialConventionVisitor<Function2<Character, Integer, Expiry>> _expiryCalculator;

  /**
   * @param securitySource The security source, not null
   * @param conventionSource The convention source, not null
   * @param holidaySource The holiday source, not null
   * @param regionSource The region source, not null
   * @param marketData The market data, not null
   * @param dataId The id of the market data, not null
   * @param valuationTime The valuation time, not null
   */
  public QuandlRateFutureNodeConverter(final SecuritySource securitySource, final ConventionSource conventionSource, final HolidaySource holidaySource,
      final RegionSource regionSource, final SnapshotDataBundle marketData, final ExternalId dataId, final ZonedDateTime valuationTime) {
    super(securitySource, conventionSource, holidaySource, regionSource, marketData, dataId, valuationTime);
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    ArgumentChecker.notNull(regionSource, "regionSource");
    ArgumentChecker.notNull(marketData, "marketData");
    ArgumentChecker.notNull(dataId, "dataId");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    _securitySource = securitySource;
    _conventionSource = conventionSource;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _marketData = marketData;
    _dataId = dataId;
    _valuationTime = valuationTime;
    _expiryCalculator = new FutureExpiryCalculator(holidaySource, regionSource); //TODO replace with convention from source
  }

  @Override
  public InstrumentDefinition<?> visitRateFutureNode(final RateFutureNode rateFuture) {
    final Convention futureConvention = _conventionSource.getSingle(rateFuture.getFutureConvention());
    final Double price = _marketData.getDataPoint(_dataId);
    if (price == null) {
      throw new Quandl4OpenGammaRuntimeException("Could not get market data for " + _dataId);
    }
    if (futureConvention == null) {
      throw new Quandl4OpenGammaRuntimeException("Could not get convention with id " + rateFuture.getFutureConvention());
    }
    if (futureConvention instanceof QuandlStirFutureConvention) {
      final QuandlStirFutureConvention stirConvention = (QuandlStirFutureConvention) futureConvention;
      final com.opengamma.financial.security.index.IborIndex indexSecurity =
          (com.opengamma.financial.security.index.IborIndex) _securitySource.getSingle(stirConvention.getUnderlyingConventionId().toBundle());
      final IborIndexConvention indexConvention = _conventionSource.getSingle(stirConvention.getUnderlyingConventionId(), IborIndexConvention.class);
      if (indexConvention == null) {
        throw new Quandl4OpenGammaRuntimeException("Ibor index convention with id " + stirConvention.getUnderlyingConventionId()
            + " was null");
      }
      final IborIndex index;
      if (indexSecurity == null) {
        // Note that this behaviour is different from that in the superclass, which insists that the security is present in
        // the source and that there is a convention with a matching entry in the id bundle. This is too restrictive and makes
        // creating configurations and conventions awkward.
        index = ConverterUtils.indexIbor(indexConvention.getName(), indexConvention, rateFuture.getUnderlyingTenor());
      } else {
        if (!indexSecurity.getTenor().equals(rateFuture.getUnderlyingTenor())) {
          LOGGER.error("Ibor index tenor {} was not equal to underlying tenor in node {}, using tenor from security", indexSecurity.getTenor(),
              rateFuture.getUnderlyingTenor());
        }
        index = ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
      }
      final Period indexTenor = rateFuture.getUnderlyingTenor().getPeriod();
      final double paymentAccrualFactor = indexTenor.toTotalMonths() / 12.; //TODO don't use this method
      final Calendar fixingCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getFixingCalendar());
      final ZonedDateTime startDate = _valuationTime.plus(rateFuture.getStartTenor().getPeriod());
      final LocalDate localStartDate = startDate.toLocalDate();
      final Character monthCode = QuandlFutureUtils.getMonthCode(rateFuture.getFutureTenor(), rateFuture.getFutureNumber(), localStartDate);
      final int expiryYear = QuandlFutureUtils.getExpiryYear(rateFuture.getFutureTenor(), rateFuture.getFutureNumber(), localStartDate);
      final ZonedDateTime expiryDate = stirConvention.accept(_expiryCalculator).apply(monthCode, expiryYear).getExpiry();
      final InterestRateFutureSecurityDefinition securityDefinition =
          new InterestRateFutureSecurityDefinition(expiryDate, index, 1, paymentAccrualFactor, "", fixingCalendar);
      final InterestRateFutureTransactionDefinition transactionDefinition =
          new InterestRateFutureTransactionDefinition(securityDefinition, 1, _valuationTime, price);
      return transactionDefinition;
    }
    if (futureConvention instanceof QuandlFedFundsFutureConvention) {
      final QuandlFedFundsFutureConvention fedFundsConvention = (QuandlFedFundsFutureConvention) futureConvention;
      final OvernightIndexConvention indexConvention = _conventionSource.getSingle(fedFundsConvention.getUnderlyingConventionId(),
          OvernightIndexConvention.class);
      if (indexConvention == null) {
        throw new Quandl4OpenGammaRuntimeException("Overnight index convention with id " + fedFundsConvention.getUnderlyingConventionId() + " was null");
      }
      // Note that this behaviour is different from that in the superclass, which insists that the security is present in
      // the source and that there is a convention with a matching entry in the id bundle. This is too restrictive and makes
      // creating configurations and conventions awkward, as only the name of the security is used
      final IndexON index = ConverterUtils.indexON(indexConvention.getName(), indexConvention);
      final double paymentAccrualFactor = 1 / 12.;
      final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, indexConvention.getRegionCalendar());
      final ZonedDateTime startDate = _valuationTime.plus(rateFuture.getStartTenor().getPeriod());
      final LocalDate localStartDate = startDate.toLocalDate();
      final Character monthCode = QuandlFutureUtils.getMonthCode(rateFuture.getFutureTenor(), rateFuture.getFutureNumber(), localStartDate);
      final int expiryYear = QuandlFutureUtils.getExpiryYear(rateFuture.getFutureTenor(), rateFuture.getFutureNumber(), localStartDate);
      final ZonedDateTime expiryDate = fedFundsConvention.accept(_expiryCalculator).apply(monthCode, expiryYear).getExpiry();
      final FederalFundsFutureSecurityDefinition securityDefinition = FederalFundsFutureSecurityDefinition.from(expiryDate,
          index, 1, paymentAccrualFactor, "", calendar);
      final FederalFundsFutureTransactionDefinition transactionDefinition =
          new FederalFundsFutureTransactionDefinition(securityDefinition, 1, _valuationTime, price);
      return transactionDefinition;
    }
    return super.visitRateFutureNode(rateFuture);
  }

}
