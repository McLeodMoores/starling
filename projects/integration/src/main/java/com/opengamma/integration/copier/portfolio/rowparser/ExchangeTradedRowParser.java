/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.rowparser;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.integration.copier.portfolio.writer.SingleSheetPositionWriter;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A row parser that reads in a ticker for an exchange-traded security, a quantity for a position, and optionally a trade date, premium and counterparty for a
 * trade.
 */
public class ExchangeTradedRowParser extends RowParser {

  public enum DateFormat {

    ISO(DateTimeFormatter.ISO_DATE, DateTimeFormatter.BASIC_ISO_DATE), UK(DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy")), US(DateTimeFormatter.ofPattern("MM/dd/yyyy"), DateTimeFormatter.ofPattern("MM-dd-yyyy"));

    private final DateTimeFormatter _primaryFormatter;
    private final DateTimeFormatter _secondaryFormatter;

    DateFormat(final DateTimeFormatter primaryFormatter, final DateTimeFormatter secondaryFormatter) {
      _primaryFormatter = primaryFormatter;
      _secondaryFormatter = secondaryFormatter;
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeTradedRowParser.class);

  private static final String TICKER = "ticker";
  private static final String ATTRIBUTES = "attributes";
  private static final String QUANTITY = "quantity";
  private static final String TRADE_DATE = "trade date";
  private static final String PREMIUM = "premium";
  private static final String PREMIUM_CURRENCY = "premiumcurrency";
  private static final String PREMIUM_DATE = "premiumdate";
  private static final String COUNTERPARTY = "counterparty";

  private final String[] _columns = { TICKER, QUANTITY, TRADE_DATE, PREMIUM, COUNTERPARTY, ATTRIBUTES };

  private final SecurityProvider _securityProvider;

  public ExchangeTradedRowParser(final SecurityProvider securityProvider, final DateFormat dateFormat) {
    super(dateFormat._primaryFormatter, dateFormat._secondaryFormatter);
    ArgumentChecker.notNull(securityProvider, "securityProvider");
    _securityProvider = securityProvider;
  }

  public ExchangeTradedRowParser(final SecurityProvider securityProvider, final DateTimeFormatter dateFormatter) {
    super(dateFormatter);
    ArgumentChecker.notNull(securityProvider, "securityProvider");
    _securityProvider = securityProvider;
  }

  private static final ExternalScheme[] SCHEME_WATERFALL = {
      ExternalSchemes.BLOOMBERG_TICKER,
      ExternalSchemes.BLOOMBERG_TCM,
      ExternalSchemes.BLOOMBERG_BUID,
      ExternalSchemes.CUSIP,
      ExternalSchemes.ISIN
  };

  @Override
  public ManageableSecurity[] constructSecurity(final Map<String, String> row) {
    ArgumentChecker.notNull(row, "row");
    final String idStr = getWithException(row, TICKER);
    if (idStr == null) {
      LOGGER.error("Ticker column contained no value, skipping row");
      return new ManageableSecurity[] {};
    }
    try {
      final ExternalIdBundle id = ExternalId.parse(idStr).toBundle();
      final Security security = _securityProvider.getSecurity(id);
      if (security != null && security instanceof ManageableSecurity) {
        return new ManageableSecurity[] { (ManageableSecurity) security };
      }
    } catch (final IllegalArgumentException iae) {
      for (final ExternalScheme scheme : SCHEME_WATERFALL) {
        final ExternalIdBundle id = ExternalId.of(scheme, idStr).toBundle();
        final Security security = _securityProvider.getSecurity(id);
        if (security != null && security instanceof ManageableSecurity) {
          return new ManageableSecurity[] { (ManageableSecurity) security };
        }
      }
    }

    return new ManageableSecurity[] {};
  }

  @Override
  public ManageablePosition constructPosition(final Map<String, String> row, final ManageableSecurity security) {
    // Create position using the quantity field

    ArgumentChecker.notNull(row, "row");
    ArgumentChecker.notNull(security, "security");

    if (row.containsKey(QUANTITY)) {
      return new ManageablePosition(
          BigDecimal.valueOf(Integer.parseInt(getWithException(row, QUANTITY))),
          security.getExternalIdBundle());
    }
    return new ManageablePosition(BigDecimal.ONE, security.getExternalIdBundle());
  }

  @Override
  public ManageableTrade constructTrade(final Map<String, String> row, final ManageableSecurity security, final ManageablePosition position) {

    ArgumentChecker.notNull(row, "row");
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(position, "position");

    // Create trade using trade date, premium and counterparty if available in current row
    if (row.containsKey(TRADE_DATE) && row.containsKey(PREMIUM) && row.containsKey(COUNTERPARTY)) {
      final LocalDate tradeDate = getDateWithException(row, TRADE_DATE);
      final ExternalId counterpartyId = ExternalId.of(Counterparty.DEFAULT_SCHEME, getWithException(row, COUNTERPARTY));
      final ManageableTrade result = new ManageableTrade(
          position.getQuantity(),
          security.getExternalIdBundle(),
          tradeDate,
          LocalTime.of(11, 11).atOffset(ZoneOffset.UTC),
          counterpartyId);
      result.setPremium(Double.parseDouble(row.get(PREMIUM)));
      if (row.containsKey(PREMIUM_CURRENCY)) {
        result.setPremiumCurrency(Currency.parse(getWithException(row, PREMIUM_CURRENCY)));
      } else {
        if (security instanceof FinancialSecurity) {
          final Currency currency = FinancialSecurityUtils.getCurrency(security);
          if (currency != null) {
            result.setPremiumCurrency(currency);
          }
        }
      }
      if (row.containsKey(PREMIUM_DATE)) {
        result.setPremiumDate(getDateWithException(row, PREMIUM_DATE));
      }
      return result;

    }
    return null;

  }

  @Override
  public Map<String, String> constructRow(final ManageableTrade trade) {
    final Map<String, String> map = new HashMap<>();
    addValueIfNotNull(map, QUANTITY, trade.getQuantity());
    addValueIfNotNull(map, TRADE_DATE, trade.getTradeDate());
    addValueIfNotNull(map, PREMIUM, trade.getPremium());
    return map;
  }

  @Override
  public Map<String, String> constructRow(final ManageablePosition position) {
    final BigDecimal quantity = position.getQuantity();
    if (quantity != null) {
      return Collections.singletonMap(QUANTITY, quantity.toString());
    }
    return null;
  }

  @Override
  public Map<String, String> constructRow(final ManageableSecurity[] securities) {
    if (securities.length < 1) {
      return null;
    }
    final String ticker = securities[0].getExternalIdBundle().getValue(ExternalSchemes.BLOOMBERG_TICKER);
    if (ticker != null) {
      final String attributes = SingleSheetPositionWriter.attributesToString(securities[0].getAttributes());
      return ImmutableMap.of(
          TICKER, ticker,
          ATTRIBUTES, attributes);
    }
    return null;
  }

  @Override
  public String[] getColumns() {
    return _columns;
  }

}
