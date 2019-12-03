/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.rowparser;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * An abstract row parser class, to be specialised for parsing a specific security/trade/position type from a row's data.
 */
public abstract class RowParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(RowParser.class);

  /** Standard date-time formatter for the input. */
  protected final DateTimeFormatter _csvDateFormatter;

  /** More Excel compatible back-up formatter. */
  protected final DateTimeFormatter _secondaryCsvDateFormatter;

  /** Standard date-time formatter for the output. */
  protected final DateTimeFormatter _outputDateFormatter;

  protected RowParser() {
    _csvDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    _secondaryCsvDateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    _outputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  }

  protected RowParser(final DateTimeFormatter formatter) {
    _csvDateFormatter = ArgumentChecker.notNull(formatter, "formatter");
    _secondaryCsvDateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    _outputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  }

  protected RowParser(final DateTimeFormatter formatter, final DateTimeFormatter secondaryFormatter) {
    _csvDateFormatter = ArgumentChecker.notNull(formatter, "formatter");
    _secondaryCsvDateFormatter = ArgumentChecker.notNull(secondaryFormatter, "secondaryFormatter");
    _outputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  }

  /**
   * Constructs a row from the supplied trade.
   *
   * @param trade
   *          The trade to convert
   * @return The mapping from column names to contents of the current row
   */
  public Map<String, String> constructRow(final ManageableTrade trade) {
    return new HashMap<>();
  }

  /**
   * Constructs a row from the supplied position.
   *
   * @param position
   *          The position to convert
   * @return The mapping from column names to contents of the current row
   */
  public Map<String, String> constructRow(final ManageablePosition position) {
    return new HashMap<>();
  }

  /**
   * Constructs a row from the supplied securities.
   *
   * @param securities
   *          The securities to convert (securities following the first are assumed to be underlyings)
   * @return The mapping from column names to contents of the current row
   */
  public Map<String, String> constructRow(final ManageableSecurity[] securities) {
    return new HashMap<>();
  }

  /**
   * Constructs a row from the supplied security, position and trade.
   *
   * @param securities
   *          The securities to convert
   * @param position
   *          The position to convert
   * @param trade
   *          The trade to convert
   * @return The mapping from column names to contents of the current row
   */
  public Map<String, String> constructRow(final ManageableSecurity[] securities, final ManageablePosition position, final ManageableTrade trade) {

    ArgumentChecker.notNull(securities, "securities");
    ArgumentChecker.notNull(position, "position");
    ArgumentChecker.notNull(trade, "trade");

    final Map<String, String> result = new HashMap<>();
    final Map<String, String> securityRow = constructRow(securities);
    final Map<String, String> positionRow = constructRow(position);
    final Map<String, String> tradeRow = constructRow(trade);
    if (securityRow != null) {
      result.putAll(securityRow);
    }
    if (positionRow != null) {
      result.putAll(positionRow);
    }
    if (tradeRow != null) {
      result.putAll(tradeRow);
    }
    return result;
  }

  /**
   * Constructs one or more securities associated with the supplied row. As a convention, the underlying security is returned at array location 0.
   *
   * @param row
   *          The mapping between column names and contents for the current row
   * @return An array of securities constructed from the current row's data; underlying is at index 0; null or an empty array if unable to construct any
   *         securities from the row; this will cause the entire row to be skipped (constructPosition() won't be called for that row
   */
  public abstract ManageableSecurity[] constructSecurity(Map<String, String> row);

  /**
   * Constructs a position associated with the supplied row.
   *
   * @param row
   *          The mapping between column names and contents for the current row
   * @param security
   *          The associated security
   * @return The constructed position or null if position construction failed
   */
  public ManageablePosition constructPosition(final Map<String, String> row, final ManageableSecurity security) {

    ArgumentChecker.notNull(row, "row");
    ArgumentChecker.notNull(security, "security");

    return new ManageablePosition(BigDecimal.ONE, security.getExternalIdBundle());
  }

  /**
   * Constructs a trade associated with the supplied row.
   *
   * @param row
   *          The mapping between column names and contents for the current row
   * @param security
   *          The associated security
   * @param position
   *          The associated position
   * @return The constructed trade or null if unable to construct a trade
   */
  public ManageableTrade constructTrade(final Map<String, String> row, final ManageableSecurity security, final ManageablePosition position) {
    return null;
  }

  /**
   * Gets the list of column names that this particular row parser knows of.
   *
   * @return A string array containing the column names
   */
  public abstract String[] getColumns();

  public int getSecurityHashCode() {
    return 0;
  }

  public static String getWithException(final Map<String, String> fieldValueMap, final String fieldName) {
    final String result = fieldValueMap.get(fieldName);
    if (result == null) {
      LOGGER.warn("No value for field " + fieldName);
      return null;
    }
    return result;
  }

  public LocalDate getDateWithException(final Map<String, String> fieldValueMap, final String fieldName) {
    return getDateWithException(fieldValueMap, fieldName, _csvDateFormatter, _secondaryCsvDateFormatter);
  }

  public static LocalDate getDateWithException(final Map<String, String> fieldValueMap, final String fieldName, final DateTimeFormatter formatter,
      final DateTimeFormatter alternativeFormatter) {
    try {
      return LocalDate.parse(getWithException(fieldValueMap, fieldName), formatter);
    } catch (final DateTimeParseException ex) {
      return LocalDate.parse(getWithException(fieldValueMap, fieldName), alternativeFormatter);
    }
  }

  public static LocalDate getDateWithException(final Map<String, String> fieldValueMap, final String fieldName, final DateTimeFormatter formatter) {
    return LocalDate.parse(getWithException(fieldValueMap, fieldName), formatter);
  }

  public static void addValueIfNotNull(final Map<String, String> map, final String key, final Object value) {
    if (value != null) {
      map.put(key, value.toString());
    }
  }

}
