/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.loader.future;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.convention.QuandlFedFundsFutureConvention;
import com.mcleodmoores.quandl.convention.QuandlFinancialConventionVisitorSameValueAdapter;
import com.mcleodmoores.quandl.convention.QuandlStirFutureConvention;
import com.mcleodmoores.quandl.future.FutureExpiryCalculator;
import com.mcleodmoores.quandl.loader.QuandlSecurityLoader;
import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Rate future loader that uses codes and information from Quandl. If any errors occur, this loader
 * will return null, as it is likely to be used by other classes that will try to load multiple
 * rate futures.
 */
@Scriptable
public class QuandlRateFutureGenerator extends QuandlSecurityLoader {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuandlRateFutureGenerator.class);

  /**
   * Main method to run this tool.
   * @param args The program arguments
   */
  public static void main(final String[] args) {
    new QuandlRateFutureGenerator().invokeAndTerminate(args);
  }

  /**
   * Short-term interest rate future category.
   */
  public static final String STIR_CATEGORY = "STIR FUTURE";
  /**
   * Fed funds future category.
   */
  public static final String FED_FUND_FUTURE_CATEGORY = "FED FUNDS FUTURE";

  /**
   * Creates a security from an array of string inputs. If the future cannot be created,
   * returns null.
   * <p>
   * The inputs are assumed to be:
   * <ul>
   *  <li> Quandl code e.g. CME/EDZ2014
   *  <li> Category e.g. STIR FUTURE
   *  <li> Expiry date in the form yyyy-mm-dd
   *  <li> Unit amount e.g. 2500
   *  <li> Time zone
   *  <li> Future trading hours in the form hh:mm - hh:mm
   *  <li> Trading exchange
   *  <li> Settlement exchange
   *  <li> Currency
   * </ul> Underlying index code
   * @param inputs The string inputs
   * @return A rate future ({@link InterestRateFutureSecurity} or {@link FederalFundsFutureSecurity}), or null
   */
  @Override
  protected ManageableSecurity createSecurity(final String[] inputs) {
    if (inputs == null) {
      LOGGER.error("Input was null");
      return null;
    }
    if (inputs.length != 10) {
      LOGGER.error("Input {} did not contain 10 elements", Arrays.toString(inputs));
      return null;
    }
    final String quandlCode = inputs[0];
    final String category = inputs[1];
    final String expiryDateString = inputs[2];
    final String unitAmountString = inputs[3];
    final String timeZoneString = inputs[4];
    final String futureTradingHours = inputs[5];
    final String tradingExchange = inputs[6];
    final String settlementExchange = inputs[7];
    final String currencyString = inputs[8];
    final String underlyingIndexCode = inputs[9];

    final ZonedDateTime expiryDate;
    try {
      expiryDate = parseDate(expiryDateString, timeZoneString, futureTradingHours);
    } catch (final Exception e) {
      LOGGER.error("Could not parse expiry date from inputs {}, {}, {} for {}", expiryDateString, timeZoneString, futureTradingHours,
          quandlCode);
      return null;
    }
    final Expiry expiry = new Expiry(expiryDate, ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    final Double unitAmount;
    try {
      unitAmount = Double.parseDouble(unitAmountString);
    } catch (final Exception e) {
      LOGGER.error("Could not parse unit amount from inputs {} for {}", unitAmountString, quandlCode);
      return null;
    }
    final Currency currency;
    try {
      currency = Currency.of(currencyString);
    } catch (final Exception e) {
      LOGGER.error("Could not parse currency from inputs {} for {}", currencyString, quandlCode);
      return null;
    }
    final ExternalId underlyingIndex;
    try {
      underlyingIndex = QuandlConstants.ofCode(underlyingIndexCode);
    } catch (final Exception e) {
      LOGGER.error("Could not create external id for underlying from {} for {}", underlyingIndexCode, quandlCode);
      return null;
    }
    final ManageableSecurity security;
    switch (category.trim().toUpperCase()) {
      case STIR_CATEGORY: {
        security = new InterestRateFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, underlyingIndex, STIR_CATEGORY);
        break;
      }
      case FED_FUND_FUTURE_CATEGORY: {
        security = new FederalFundsFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, underlyingIndex, FED_FUND_FUTURE_CATEGORY);
        break;
      }
      default:
        LOGGER.error("Unrecognised rate future category {}", category);
        return null;
    }
    security.setExternalIdBundle(QuandlConstants.ofCode(quandlCode).toBundle());
    security.setName(quandlCode);
    return security;
  }

  /**
   * Creates a security from a Quandl code and a convention. This method looks for a convention with an id
   * that matches the Quandl code first and then falls back to the Quandl prefix id (e.g. the prefix for CME/EDF2014 is CME/ED)
   * if there is no appropriate convention. If there is a problem in creating the security, returns null
   * @param conventionSource The convention source, not null
   * @param quandlCode The quandl code
   * @return A rate future ({@link InterestRateFutureSecurity} or {@link FederalFundsFutureSecurity}), or null
   */
  @Override
  protected ManageableSecurity createSecurity(final ConventionSource conventionSource, final String quandlCode) {
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    if (quandlCode == null) {
      LOGGER.error("Quandl code was null");
      return null;
    }
    final int codeLength = quandlCode.length();
    if (codeLength < 6) {
      LOGGER.error("Quandl code must be of the form [PREFIX][MONTH_CODE][YYYY], have {}", quandlCode);
      return null;
    }
    final String quandlPrefix = quandlCode.substring(0, codeLength - 5);
    final char monthCode = quandlCode.charAt(codeLength - 5);
    final Integer expiryYear;
    try {
      expiryYear = Integer.parseInt(quandlCode.substring(codeLength - 4, codeLength));
    } catch (final Exception e) {
      LOGGER.error("Could not parse expiry year from {}", quandlCode);
      return null;
    }
    FinancialConvention futureConvention;
    //try the code first, then the prefix
    final ExternalId prefixId = QuandlConstants.ofPrefix(quandlPrefix);
    final ExternalId codeId = QuandlConstants.ofCode(quandlCode);
    try {
      futureConvention = (FinancialConvention) conventionSource.getSingle(codeId);
    } catch (final Exception e) {
      LOGGER.info("FinancialConvention with id {} not found, trying {} ", codeId, prefixId);
      futureConvention = (FinancialConvention) conventionSource.getSingle(prefixId);
      if (futureConvention == null) {
        LOGGER.error("FinancialConvention with id {} not found ", prefixId);
        return null;
      }
    }
    if (futureConvention == null) {
      // not clear whether DataNotFoundException or null should be returned for a missing convention,
      // so double-checking
      LOGGER.info("FinancialConvention with id {} not found, trying {} ", codeId, prefixId);
      futureConvention = (FinancialConvention) conventionSource.getSingle(prefixId);
      if (futureConvention == null) {
        LOGGER.error("FinancialConvention with id {} not found ", prefixId);
        return null;
      }
    }
    try {
      FutureExpiryCalculator expiryCalculator;
      try {
        final HolidaySource holidaySource = getToolContext().getHolidaySource();
        expiryCalculator = new FutureExpiryCalculator(holidaySource);
      } catch (final Exception e) {
        // yuck
        expiryCalculator = new FutureExpiryCalculator();
      }
      final Expiry expiry = futureConvention.accept(expiryCalculator).apply(monthCode, expiryYear);
      final ManageableSecurity security = futureConvention.accept(new QuandlFinancialConventionVisitorSameValueAdapter<ManageableSecurity>(null) {

        @Override
        public ManageableSecurity visitQuandlStirFutureConvention(final QuandlStirFutureConvention convention) {
          return new InterestRateFutureSecurity(expiry, convention.getTradingExchange(), convention.getSettlementExchange(),
              convention.getCurrency(), convention.getUnitAmount(), convention.getUnderlyingConventionId(), STIR_CATEGORY);
        }

        @Override
        public ManageableSecurity visitQuandlFedFundsFutureConvention(final QuandlFedFundsFutureConvention convention) {
          return new FederalFundsFutureSecurity(expiry, convention.getTradingExchange(), convention.getSettlementExchange(),
              convention.getCurrency(), convention.getUnitAmount(), convention.getUnderlyingConventionId(), FED_FUND_FUTURE_CATEGORY);
        }
      });
      security.setExternalIdBundle(QuandlConstants.ofCode(quandlCode).toBundle());
      security.setName(quandlCode);
      return security;
    } catch (final Exception e) {
      LOGGER.error("Could not create security: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Parses the expiry date, time zone and trade time strings.
   * @param dateString The expiry date
   * @param timeZoneString The time zone string
   * @param tradingHoursString The trading hours string
   * @return The expiry
   */
  private static ZonedDateTime parseDate(final String dateString, final String timeZoneString, final String tradingHoursString) {
    final LocalDate date = LocalDate.parse(dateString); // TODO #9
    final ZoneId timeZone = ZoneId.of(timeZoneString);
    final List<String> timeStrings = new ArrayList<>();
    if (tradingHoursString.contains("&")) {
      for (final String time : tradingHoursString.split("&")) {
        timeStrings.addAll(splitTimes(time));
      }
    } else {
      timeStrings.addAll(splitTimes(tradingHoursString));
    }
    final LocalTime time = LocalTime.parse(timeStrings.get(timeStrings.size() - 1));
    return ZonedDateTime.of(LocalDateTime.of(date, time), timeZone);
  }

  /**
   * Splits a string on "-".
   * @param times The string
   * @return A list of substrings.
   */
  private static List<String> splitTimes(final String times) {
    return Arrays.asList(times.split("-"));
  }
}
