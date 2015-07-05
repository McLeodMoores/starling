/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.examples.simulated.volatility.surface;

import java.util.HashMap;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.financial.analytics.volatility.surface.CallPutSurfaceInstrumentProvider;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Generates tickers for a volatility surface relevant for a particular calculation date. The tickers
 * are time-dependent and use the nth option expiry (e.g. the second monthly option expiry from 1/1/2014,
 * which would be 21/2/2014 if the third Friday rule was in effect). The surface uses both puts and
 * calls, and there is a value set for the strike at which to switch between put and call quotes.
 * <p>
 * This instrument provider is intended to be used with OpenGamma integration functions.
 */
public class ExampleCallPutVolatilitySurfaceInstrumentProvider implements CallPutSurfaceInstrumentProvider<Number, Double> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleCallPutVolatilitySurfaceInstrumentProvider.class);
  /** The date-time formatter */
  private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
  /** The expiry rules */
  private static final HashMap<String, ExchangeTradedInstrumentExpiryCalculator> EXPIRY_RULES;
  /** An empty holiday calendar */
  private static final Calendar NO_HOLIDAYS = new NoHolidayCalendar();
  static {
    EXPIRY_RULES = new HashMap<>();
    EXPIRY_RULES.put("AAPL", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1)));
    EXPIRY_RULES.put("DEFAULT", FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1)));
  }
  /** The ticker prefix */
  private final String _optionPrefix;
  /** The data field name */
  private final String _dataFieldName;
  /** The value above which to use calls */
  private final Double _useCallAboveStrike;

  /**
   * @param optionPrefix the prefix to the resulting code (e.g. DJX), not null
   * @param dataFieldName the name of the data field, not null.
   * @param useCallAboveStrike the strike above which to use calls rather than puts, not null
   */
  public ExampleCallPutVolatilitySurfaceInstrumentProvider(final String optionPrefix, final String dataFieldName, final Double useCallAboveStrike) {
    ArgumentChecker.notNull(optionPrefix, "option prefix");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    ArgumentChecker.notNull(useCallAboveStrike, "use call above this strike");
    _optionPrefix = optionPrefix;
    _dataFieldName = dataFieldName;
    _useCallAboveStrike = useCallAboveStrike;
  }

  /**
   * Provides an ExternalID for an {@link ExternalSchemes#OG_SYNTHETIC_TICKER},
   * given a reference date and an integer offset, the n'th subsequent option <p>
   * The format is prefix + date(MM/dd/yy) + callPutFlag + strike <p>
   * e.g. AAA 12/21/13 C100.
   * <p>
   * @param expiryNumber nth expiry following curve date, not null
   * @param strike option's strike, expressed as price, e.g. 98.750, not null
   * @param surfaceDate date of curve validity; valuation date, not null
   * @return the id of the Bloomberg ticker
   */
  @Override
  public ExternalId getInstrument(final Number expiryNumber, final Double strike, final LocalDate surfaceDate) {
    ArgumentChecker.notNull(expiryNumber, "expiryNumber");
    ArgumentChecker.notNull(strike, "strike");
    ArgumentChecker.notNull(surfaceDate, "surfaceDate");
    final StringBuffer ticker = new StringBuffer(_optionPrefix);
    ticker.append(" ");
    final ExchangeTradedInstrumentExpiryCalculator expiryRule = getExpiryRuleCalculator();
    final LocalDate expiry = expiryRule.getExpiryDate(expiryNumber.intValue(), surfaceDate, NO_HOLIDAYS);
    ticker.append(FORMAT.format(expiry));
    ticker.append(" ");
    ticker.append(strike > useCallAboveStrike() ? "C" : "P");
    ticker.append(strike);
    return ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, ticker.toString());
  }

  @Override
  public ExternalId getInstrument(final Number xAxis, final Double yAxis) {
    throw new OpenGammaRuntimeException("Need a surface date to create an option surface");
  }

  /**
   * Gets the expiryRules.
   * @return the expiryRules
   */
  public static HashMap<String, ExchangeTradedInstrumentExpiryCalculator> getExpiryRules() {
    return EXPIRY_RULES;
  }

  @Override
  public ExchangeTradedInstrumentExpiryCalculator getExpiryRuleCalculator() {
    final String prefix = _optionPrefix;
    ExchangeTradedInstrumentExpiryCalculator expiryRule = EXPIRY_RULES.get(prefix);
    if (expiryRule == null) {
      LOGGER.info("No expiry rule has been setup for " + prefix + ". Using Default of 3rd Friday.");
      expiryRule = EXPIRY_RULES.get("DEFAULT");
    }
    return expiryRule;
  }

  /**
   * Gets the option prefix.
   * @return The option prefix
   */
  public String getOptionPrefix() {
    return _optionPrefix;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  @Override
  public Double useCallAboveStrike() {
    return _useCallAboveStrike;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _dataFieldName.hashCode();
    result = prime * result + _optionPrefix.hashCode();
    result = prime * result + _useCallAboveStrike.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ExampleCallPutVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final ExampleCallPutVolatilitySurfaceInstrumentProvider other = (ExampleCallPutVolatilitySurfaceInstrumentProvider) obj;
    if (Double.compare(_useCallAboveStrike.doubleValue(), other._useCallAboveStrike.doubleValue()) != 0) {
      return false;
    }
    if (!Objects.equals(_optionPrefix, other._optionPrefix)) {
      return false;
    }
    if (!Objects.equals(_dataFieldName, other._dataFieldName)) {
      return false;
    }
    return true;
  }

}
