/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import static org.threeten.bp.temporal.ChronoField.DAY_OF_MONTH;
import static org.threeten.bp.temporal.ChronoField.HOUR_OF_DAY;
import static org.threeten.bp.temporal.ChronoField.MINUTE_OF_HOUR;
import static org.threeten.bp.temporal.ChronoField.MONTH_OF_YEAR;
import static org.threeten.bp.temporal.ChronoField.SECOND_OF_MINUTE;
import static org.threeten.bp.temporal.ChronoField.YEAR;

import java.util.List;
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.format.SignStyle;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.integration.tool.marketdata.SnapshotUtils.VersionInfo;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Utility methods for the MarketDataSnapshot Import/Export tools.
 */
public class MarketDataSnapshotToolUtils {
  private static final String VERSION_FROM = "Version From";
  private static final String VERSION_TO = "Version To";
  private static final String CORRECTION_FROM = "Correction From";
  private static final String CORRECTION_TO = "Correction To";
  private static final String UNIQUE_ID = "UniqueId";
  private static final String NOT_SPECIFIED = "Not Specified";
  /** Snapshot listing option flag */
  private static final String SNAPSHOT_LIST_OPTION = "s";
  /** Snapshot query option flag */
  private static final String SNAPSHOT_QUERY_OPTION = "q";
  /** Snapshot version list option flag */
  private static final String SNAPSHOT_VERSION_LIST_OPTION = "v";
  private static final Logger LOGGER = LoggerFactory.getLogger(MarketDataSnapshotToolUtils.class);

  public static Option createSnapshotListOption() {
    final Option option = new Option(SNAPSHOT_LIST_OPTION, "snapshot-list", false, "List the snapshots available");
    return option;
  }

  public static Option createSnapshotQueryOption() {
    final Option option = new Option(SNAPSHOT_QUERY_OPTION, "snapshot-query", true, "List the snapshots available according to a glob");
    option.setArgName("snapshot name glob");
    return option;
  }

  public static Option createSnapshotVersionListOption() {
    final Option option = new Option(SNAPSHOT_VERSION_LIST_OPTION, "snapshot-versions", true, "List the versions available for a named snapshot");
    option.setArgName("snapshot name");
    return option;
  }

  public static boolean handleQueryOptions(final SnapshotUtils snapshotUtils, final CommandLine commandLine) {
    if (commandLine.hasOption(SNAPSHOT_LIST_OPTION)) {
      printSnapshotList(snapshotUtils);
      return true;
    } else if (commandLine.hasOption(SNAPSHOT_QUERY_OPTION)) {
      printSnapshotQuery(snapshotUtils, commandLine.getOptionValue(SNAPSHOT_QUERY_OPTION));
      return true;
    } else if (commandLine.hasOption(SNAPSHOT_VERSION_LIST_OPTION)) {
      printVersionListQuery(snapshotUtils, commandLine.getOptionValue(SNAPSHOT_VERSION_LIST_OPTION));
      return true;
    } else {
      return false;
    }
  }

  private static void printSnapshotQuery(final SnapshotUtils snapshotUtils, final String query) {
    final List<String> snapshotsByGlob = snapshotUtils.snapshotByGlob(query);
    for (final String info : snapshotsByGlob) {
      System.out.println(info);
    }
  }

  private static void printSnapshotList(final SnapshotUtils snapshotUtils) {
    final List<String> allSnapshots = snapshotUtils.allSnapshots();
    for (final String info : allSnapshots) {
      System.out.println(info);
    }
  }

  private static void printVersionListQuery(final SnapshotUtils snapshotUtils, final String optionValue) {
    final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
        .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
        .appendLiteral('-')
        .appendValue(MONTH_OF_YEAR, 2)
        .appendLiteral('-')
        .appendValue(DAY_OF_MONTH, 2)
        .appendValue(HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2)
        .optionalStart()
        .appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2)
        .appendOffsetId()
        .toFormatter();

    final List<VersionInfo> snapshotVersions = snapshotUtils.snapshotVersionsByName(optionValue);
    System.out.println(OffsetDateTime.now().format(dateTimeFormatter));

    final int fieldWidth = OffsetDateTime.now().format(dateTimeFormatter).length(); // Assumes all offset date times have same width

    header(fieldWidth);
    final String id = TimeZone.getDefault().getID();
    for (final VersionInfo versionInfo : snapshotVersions) {
      final OffsetDateTime versionFrom = versionInfo.getVersionFrom() != null ? OffsetDateTime.ofInstant(versionInfo.getVersionFrom(), ZoneId.of(id)) : null;
      final OffsetDateTime versionTo = versionInfo.getVersionTo() != null ? OffsetDateTime.ofInstant(versionInfo.getVersionTo(), ZoneId.of(id)) : null;
      final OffsetDateTime correctionFrom = versionInfo.getCorrectionFrom() != null ? OffsetDateTime.ofInstant(versionInfo.getCorrectionFrom(), ZoneId.of(id))
          : null;
      final OffsetDateTime correctionTo = versionInfo.getCorrectionTo() != null ? OffsetDateTime.ofInstant(versionInfo.getCorrectionTo(), ZoneId.of(id)) : null;
      if (versionFrom != null) {
        System.out.print(versionFrom.format(dateTimeFormatter));
      } else {
        notSpecified(fieldWidth);
      }
      spaces();
      if (versionTo != null) {
        System.out.print(versionTo.format(dateTimeFormatter));
      } else {
        notSpecified(fieldWidth);
      }
      spaces();
      if (correctionFrom != null) {
        System.out.print(correctionFrom.format(dateTimeFormatter));
      } else {
        notSpecified(fieldWidth);
      }
      spaces();
      if (correctionTo != null) {
        System.out.print(correctionTo.format(dateTimeFormatter));
      } else {
        notSpecified(fieldWidth);
      }
      spaces();
      System.out.println(versionInfo.getUniqueId());
    }
  }

  private static void header(final int fieldWidth) {
    System.out.print(VERSION_FROM);
    pad(fieldWidth - VERSION_FROM.length());
    spaces();
    System.out.print(VERSION_TO);
    pad(fieldWidth - VERSION_TO.length());
    spaces();
    System.out.print(CORRECTION_FROM);
    pad(fieldWidth - CORRECTION_FROM.length());
    spaces();
    System.out.print(CORRECTION_TO);
    pad(fieldWidth - CORRECTION_TO.length());
    spaces();
    System.out.println(UNIQUE_ID);
  }

  private static void spaces() {
    System.out.print("  ");
  }

  private static void notSpecified(final int fieldWidth) {
    System.out.print(NOT_SPECIFIED);
    pad(fieldWidth - NOT_SPECIFIED.length());
  }

  private static void pad(final int n) {
    final String repeat = org.apache.commons.lang.StringUtils.repeat(" ", n);
    System.out.print(repeat);
  }

  public static ValueSnapshot createValueSnapshot(final String market, final String override) {
    Object marketValue = null;
    Object overrideValue = null;

    // marketValue can only be Double, LocalDate, empty or (FudgeMsg which is special cased for Market_All)
    if (market != null && !market.isEmpty()) {
      if (NumberUtils.isNumber(market)) {
        marketValue = NumberUtils.createDouble(market);
      } else {
        try {
          marketValue = LocalDate.parse(market);
        } catch (final IllegalArgumentException e) {
          LOGGER.error("Market value {} should be a Double, LocalDate or empty.", market);
        }
      }
    }

    // overrideValue can only be Double, LocalDate or empty
    if (override != null && !override.isEmpty()) {
      if (NumberUtils.isNumber(override)) {
        overrideValue = NumberUtils.createDouble(override);
      } else {
        try {
          overrideValue = LocalDate.parse(override);
        } catch (final IllegalArgumentException e) {
          LOGGER.error("Override value {} should be a Double, LocalDate or empty.", override);
        }
      }
    }

    return ValueSnapshot.of(marketValue, overrideValue);
  }

  private static Boolean isTenor(final String tenor) {
    try {
      Tenor.parse(tenor);
      return true;
    } catch (final IllegalArgumentException e) {
      return false;
    }
  }

  public static Pair<Object, Object> createOrdinatePair(final String xValue, final String yValue) {
    final String[] yValues = yValue.split("\\|");
    Object surfaceX = null;
    Object surfaceY = null;

    if (xValue != null) {
      if (NumberUtils.isNumber(xValue)) {
        surfaceX = NumberUtils.createDouble(xValue);
      } else if (isTenor(xValue)) {
        surfaceX = Tenor.parse(xValue);
      } else {
        LOGGER.error("Volatility surface X ordinate {} should be a Double, Tenor or empty.", xValue);
      }
    }

    if (yValues != null) {
      if (yValues.length > 1) {
        try {
          surfaceY = createYOrdinatePair(yValues);
        } catch (final IllegalArgumentException e) {
          LOGGER.error("Volatility surface Y ordinate {} should be a Double, Pair<Number, FXVolQuoteType> or empty.", xValue);
        }
      } else if (yValues.length == 1) {
        if (NumberUtils.isNumber(yValues[0])) {
          surfaceY = NumberUtils.createDouble(yValues[0]);
        } else if (isTenor(yValues[0])) {
          surfaceY = Tenor.parse(yValues[0]);
        }
      }
    }

    return Pairs.of(surfaceX, surfaceY);
  }

  // Bloomberg FX option volatility surface codes given a tenor, quote type (ATM, butterfly, risk reversal) and distance from ATM.
  private static Pair<Number, BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType> createYOrdinatePair(final String[] yPair) {
    Number firstElement = null;
    BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType secondElement = null;
    if (NumberUtils.isNumber(yPair[0])) {
      firstElement = NumberUtils.createDouble(yPair[0]);
    }
    switch (yPair[1]) {
      case "ATM":
        secondElement = BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType.ATM;
        break;
      case "RISK_REVERSAL":
        secondElement = BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType.RISK_REVERSAL;
        break;
      case "BUTTERFLY":
        secondElement = BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType.BUTTERFLY;
        break;
    }
    return Pairs.of(firstElement, secondElement);
  }

  public static Pair<String, String> ordinalsAsString(final Pair<Object, Object> rawOrdinates) {
    String surfaceX;
    if (rawOrdinates.getFirst() instanceof Tenor) {
      surfaceX = ((Tenor) rawOrdinates.getFirst()).toFormattedString();
    } else {
      surfaceX = rawOrdinates.getFirst().toString();
    }

    String surfaceY;
    if (rawOrdinates.getSecond() instanceof Pair) {
      surfaceY = ((Pair<?, ?>) rawOrdinates.getSecond()).getFirst() + "|" + ((Pair<?, ?>) rawOrdinates.getSecond()).getSecond();
    } else if (rawOrdinates.getSecond() instanceof Tenor) {
      surfaceY = ((Tenor) rawOrdinates.getSecond()).toFormattedString();
    } else {
      surfaceY = rawOrdinates.getSecond().toString();
    }

    return ObjectsPair.of(surfaceX, surfaceY);
  }

}
