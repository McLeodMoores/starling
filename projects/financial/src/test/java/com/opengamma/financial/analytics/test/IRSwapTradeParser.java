/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.FloatingIndex;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.csv.CSVDocumentReader;
import com.opengamma.util.money.Currency;

import au.com.bytecode.opencsv.CSVParser;

/**
 *
 */
public class IRSwapTradeParser {
  private static final Logger LOGGER = LoggerFactory.getLogger(IRSwapTradeParser.class);

  private static final String EFFECTIVE_DATE = "Effective Date";
  private static final String MATURITY_DATE = "Maturity Date";
  private static final String DIRECTION = "Direction";
  private static final String PAY_LEG_TYPE = "LEG1_TYPE";
  private static final String PAY_LEG_BUS_DAY_CONV = "LEG1_PAY_ADJ_BUS_DAY_CONV";
  private static final String PAY_LEG_DAYCOUNT = "LEG1_DAYCOUNT";
  private static final String PAY_LEG_FREQUENCY = "LEG1_PAY_FREQ";
  private static final String PAY_LEG_REGION = "LEG1_PAY_ADJ_CAL";
  private static final String PAY_NOTIONAL = "LEG1_NOTIONAL";
  private static final String PAY_CURRENCY = "LEG1_CCY";
  private static final String PAY_LEG_EOM = "LEG1_ROLL_CONV";
  private static final String PAY_LEG_FIXED_RATE = "LEG1_FIXED_RATE";
  private static final String PAY_LEG_INDEX = "LEG1_INDEX";

  private static final String RECIEVE_LEG_TYPE = "LEG2_TYPE";
  private static final String RECIEVE_LEG_BUS_DAY_CONV = "LEG2_PAY_ADJ_BUS_DAY_CONV";
  private static final String RECIEVE_LEG_DAYCOUNT = "LEG2_DAYCOUNT";
  private static final String RECIEVE_LEG_FREQUENCY = "LEG2_PAY_FREQ";
  private static final String RECIEVE_LEG_REGION = "LEG2_PAY_ADJ_CAL";
  private static final String RECIEVE_NOTIONAL = "LEG2_NOTIONAL";
  private static final String RECIEVE_CURRENCY = "LEG2_CCY";
  private static final String RECIEVE_LEG_EOM = "LEG2_ROLL_CONV";
  private static final String RECIEVE_LEG_FIXED_RATE = "LEG2_FIXED_RATE";
  private static final String RECIEVE_LEG_INDEX = "LEG2_INDEX";
  private static final String CLEARED_TRADE_ID = "Cleared Trade ID";
  private static final String STATUS = "Status";
  private static final String ERS_PV = "ERS PV";
  private static final String PRODUCT_TYPE = "PRODUCT_TYPE";
  private static final String CME_SWAP_INDICATOR = "CME Swap Indicator";
  private static final String CLIENT_ID = "Client ID";

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");
  private static final Properties DAY_COUNT_MAPPING = getDayCountMapping();
  private static final Properties BUSINESS_DAY_CONVENTION_MAPPING = getBusinessDayConventionMapping();
  private static final Map<String, com.opengamma.financial.convention.FloatingIndex> FLOATING_INDEX_MAPPING = getFloatingIndexMapping();
  private static final List<String> UNSUPPORTED_PRODUCT_TYPES = Lists.newArrayList("FRA", "ZCS");
  private static final List<String> STUB_TRADES = Lists.newArrayList("shortfront", "longfront", "shortback", "longback");
  private static final List<String> COMPOUND_TRADES = Lists.newArrayList("cmpd", "straightcmpnd", "flatcmpnd");

  private final Random _random = new Random();


  public List<IRSwapSecurity> parseCSVFile(final URL tradeFileUrl) {
    ArgumentChecker.notNull(tradeFileUrl, "tradeFileUrl");

    final List<IRSwapSecurity> trades = Lists.newArrayList();
    final CSVDocumentReader csvDocumentReader = new CSVDocumentReader(tradeFileUrl, CSVParser.DEFAULT_SEPARATOR,
        CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, new FudgeContext());

    final List<FudgeMsg> rowsWithError = Lists.newArrayList();
    final List<FudgeMsg> unsupportedProdTypes = Lists.newArrayList();
    final List<FudgeMsg> stubTrades = Lists.newArrayList();
    final List<FudgeMsg> compoundTrades = Lists.newArrayList();
    final List<FudgeMsg> missingPV = Lists.newArrayList();
    final List<FudgeMsg> terminatedTrades = Lists.newArrayList();

    int count = 1;
    for (final FudgeMsg row : csvDocumentReader) {
      count++;
      SwapSecurity swapSecurity = null;
      try {
        if (isUnsupportedProductType(row)) {
          unsupportedProdTypes.add(row);
          continue;
        }
        if (isStubTrade(row)) {
          stubTrades.add(row);
          continue;
        }
        if (isCompoundTrade(row)) {
          compoundTrades.add(row);
          continue;
        }
        if (isErsPVMissing(row)) {
          missingPV.add(row);
          continue;
        }
        if (isTeminatedTrade(row)) {
          terminatedTrades.add(row);
          continue;
        }
        swapSecurity = createSwapSecurity(row);
        trades.add(IRSwapSecurity.of(swapSecurity, row));
      } catch (final Exception ex) {
        ex.printStackTrace();
        rowsWithError.add(row);
      }
    }

    logErrors("unsupportedProdTypes", unsupportedProdTypes, count);
    logErrors("stubTrades", stubTrades, count);
    logErrors("compoundTrades", compoundTrades, count);
    logErrors("missingPV", missingPV, count);
    logErrors("terminatedTrades", terminatedTrades, count);

    LOGGER.warn("Total unprocessed rows: {} out of {}", rowsWithError.size(), count);
    for (final FudgeMsg fudgeMsg : rowsWithError) {
      LOGGER.warn("{}", fudgeMsg);
    }
    return trades;
  }

  private void logErrors(final String type, final List<FudgeMsg> unsupportedProdTypes, final int totalRows) {
    LOGGER.warn("Total {} rows: {} out of {}", type, unsupportedProdTypes.size(), totalRows);
    for (final FudgeMsg fudgeMsg : unsupportedProdTypes) {
      LOGGER.warn("{}", fudgeMsg);
    }
  }

  private boolean isTeminatedTrade(final FudgeMsg row) {
    final String status = row.getString(STATUS);
    return "TERMINATED".equalsIgnoreCase(status);
  }

  private boolean isErsPVMissing(final FudgeMsg row) {
    return row.getString(ERS_PV) == null;
  }

  private boolean isCompoundTrade(final FudgeMsg row) {
    final String clientId = row.getString(CLIENT_ID);
    if (clientId != null) {
      for (final String cmpKeyWord : COMPOUND_TRADES) {
        if (clientId.toLowerCase().contains(cmpKeyWord)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isStubTrade(final FudgeMsg row) {
    final String clientId = row.getString(CLIENT_ID);
    if (clientId != null) {
      for (final String stubKeyWord : STUB_TRADES) {
        if (clientId.toLowerCase().contains(stubKeyWord)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isUnsupportedProductType(final FudgeMsg row) {
    final String productType = row.getString(PRODUCT_TYPE);
    if (productType == null) {
      return true;
    }
    return UNSUPPORTED_PRODUCT_TYPES.contains(productType.toUpperCase());
  }

  private static Map<String, FloatingIndex> getFloatingIndexMapping() {
    final Map<String, FloatingIndex> result = Maps.newHashMap();
    for (final FloatingIndex floatingIndex : FloatingIndex.values()) {
      result.put(floatingIndex.getIsdaName().toUpperCase(), floatingIndex);
    }
    return result;
  }

  private static Properties getBusinessDayConventionMapping() {
    return loadPropertiesFromClassPath("classpath:com/opengamma/financial/analytics/test/businessDayConventionMapping.properties");
  }

  private static Properties getDayCountMapping() {
    return loadPropertiesFromClassPath("classpath:com/opengamma/financial/analytics/test/dayCountMapping.properties");
  }

  private static Properties loadPropertiesFromClassPath(final String resourceLocation) {
    final Resource resource = ResourceUtils.createResource(resourceLocation);
    final Properties properties = new Properties();
    try (InputStream stream = resource.getInputStream()) {
      properties.load(stream);
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Error loading " + resourceLocation + " from classpath", ex);
    }
    return properties;
  }

  private SwapSecurity createSwapSecurity(final FudgeMsg row) {
    final LocalDate effectiveDate = parseDate(row, EFFECTIVE_DATE);
    final LocalDate maturityDate = parseDate(row, MATURITY_DATE);
    SwapLeg payLeg = parsePayLeg(row);
    SwapLeg receiveLeg = parseReceiveLeg(row);

    final String direction = row.getString(DIRECTION);
    if (direction != null) {
      if (direction.equalsIgnoreCase("R")) {
        final SwapLeg temp = payLeg;
        payLeg = receiveLeg;
        receiveLeg = temp;
      }
    }

    final SwapSecurity swap = new SwapSecurity(effectiveDate.atStartOfDay(ZoneOffset.UTC),
        effectiveDate.atStartOfDay(ZoneOffset.UTC),
        maturityDate.atStartOfDay(ZoneOffset.UTC),
        "Cpty " + _random.nextInt(100), payLeg, receiveLeg);
    swap.addExternalId(ExternalId.of("UUID", GUIDGenerator.generate().toString()));
    swap.setName(getSwapName(row, swap));
    return swap;
  }

  private String getSwapName(final FudgeMsg row, final SwapSecurity swap) {
    FixedInterestRateLeg fixedLeg = null;
    FloatingInterestRateLeg floatingLeg = null;
    if (swap.getPayLeg() instanceof FixedInterestRateLeg) {
      fixedLeg = (FixedInterestRateLeg) swap.getPayLeg();
      floatingLeg = (FloatingInterestRateLeg) swap.getReceiveLeg();
    } else {
      fixedLeg = (FixedInterestRateLeg) swap.getReceiveLeg();
      floatingLeg = (FloatingInterestRateLeg) swap.getPayLeg();
    }

    final InterestRateNotional notional = (InterestRateNotional) fixedLeg.getNotional();
    return String.format("#%s %s : pay %s%% fixed vs %s, start=%s, maturity=%s, notional=%s %s", row.getString(CLEARED_TRADE_ID),
        row.getString(CME_SWAP_INDICATOR),
        fixedLeg.getRate() * 100.0,
        floatingLeg.getFloatingReferenceRateId().getValue(),
        swap.getEffectiveDate().toLocalDate(),
        swap.getMaturityDate().toLocalDate(),
        notional.getCurrency(),
        notional.getAmount());
  }

  private SwapLeg parseReceiveLeg(final FudgeMsg row) {
    SwapLeg swapLeg = null;
    final String legType = row.getString(RECIEVE_LEG_TYPE);
    if (legType != null) {
      final DayCount payDayCount = parseDayCount(row, RECIEVE_LEG_DAYCOUNT);
      final Frequency payFrequency = parseFrequency(row, RECIEVE_LEG_FREQUENCY);
      final ExternalId payRegionId = parseRegionId(row, RECIEVE_LEG_REGION);
      final BusinessDayConvention payDayConvention = parseBusinessDayConvention(row, RECIEVE_LEG_BUS_DAY_CONV);
      final Boolean payEom = parseEOM(row, RECIEVE_LEG_EOM);
      final Notional payNotional = parseNotional(row, RECIEVE_NOTIONAL, RECIEVE_CURRENCY);

      switch (legType) {
        case "FIXED":
          final Double payFixedRate = parseFixedRate(row, RECIEVE_LEG_FIXED_RATE);
          try {
            swapLeg = new FixedInterestRateLeg(payDayCount, payFrequency, payRegionId,
                payDayConvention, payNotional, payEom, payFixedRate);
          } catch (final Exception ex) {
            LOGGER.warn(String.format("Error creating swap security from %s", row), ex);
          }
          break;
        case "FLOAT":
          final ExternalId payFloatingRateId = parseFloatingRateId(row, RECIEVE_LEG_INDEX, payFrequency);
          try {
            swapLeg = new FloatingInterestRateLeg(payDayCount, payFrequency, payRegionId, payDayConvention,
                payNotional, payEom, payFloatingRateId, FloatingRateType.IBOR);
          } catch (final Exception ex) {
            LOGGER.warn(String.format("Error creating swap security from %s", row), ex);
          }
          break;
        default:
          LOGGER.warn("Unsupported leg type: {} in {}", legType, row);
          break;
      }
    } else {
      LOGGER.warn("Missing leg type value in column:{} in row:{}", RECIEVE_LEG_TYPE, row);
    }
    return swapLeg;
  }

  private SwapLeg parsePayLeg(final FudgeMsg row) {
    SwapLeg swapLeg = null;
    final String legType = row.getString(PAY_LEG_TYPE);
    if (legType != null) {
      final DayCount payDayCount = parseDayCount(row, PAY_LEG_DAYCOUNT);
      final Frequency payFrequency = parseFrequency(row, PAY_LEG_FREQUENCY);
      final ExternalId payRegionId = parseRegionId(row, PAY_LEG_REGION);
      final BusinessDayConvention payDayConvention = parseBusinessDayConvention(row, PAY_LEG_BUS_DAY_CONV);
      final Boolean payEom = parseEOM(row, PAY_LEG_EOM);
      final Notional payNotional = parseNotional(row, PAY_NOTIONAL, PAY_CURRENCY);
      switch (legType) {
        case "FIXED":
          final Double payFixedRate = parseFixedRate(row, PAY_LEG_FIXED_RATE);
          try {
            swapLeg = new FixedInterestRateLeg(payDayCount, payFrequency, payRegionId,
                payDayConvention, payNotional, payEom, payFixedRate);
          } catch (final Exception ex) {
            LOGGER.warn(String.format("Error creating swap security from %s", row), ex);
          }
          break;
        case "FLOAT":
          final ExternalId payFloatingRateId = parseFloatingRateId(row, PAY_LEG_INDEX, payFrequency);
          try {
            swapLeg = new FloatingInterestRateLeg(payDayCount, payFrequency, payRegionId, payDayConvention,
                payNotional, payEom, payFloatingRateId, FloatingRateType.IBOR);
          } catch (final Exception ex) {
            LOGGER.warn(String.format("Error creating swap security from %s", row), ex);
          }
          break;
        default:
          LOGGER.warn("Unsupported leg type:{} in {}", legType, row);
          break;
      }
    } else {
      LOGGER.warn("Missing leg type value in column:{} in row:{}", PAY_LEG_TYPE, row);
    }
    return swapLeg;
  }

  private ExternalId parseFloatingRateId(final FudgeMsg row, final String columnName, final Frequency frequency) {
    ExternalId floatingRateId = null;
    if (frequency != null) {
      final String floatingIndexStr = row.getString(columnName);
      if (floatingIndexStr != null) {
        final FloatingIndex floatingIndex = FLOATING_INDEX_MAPPING.get(floatingIndexStr.toUpperCase());
        if (floatingIndex != null) {
          floatingRateId = floatingIndex.toFrequencySpecificExternalId(frequency);
        } else {
          LOGGER.warn("Unsupported floating Index: {} in row:{}", floatingIndexStr, row);
        }
      }
    }
    return floatingRateId;
  }

  private Double parseFixedRate(final FudgeMsg row, final String columnName) {
    Double rate = null;
    final String rateStr = row.getString(columnName);
    try {
      rate = Double.parseDouble(rateStr);
    } catch (final Exception ex) {
      LOGGER.warn("Missing or invalid value for fixed rate in column:{} in row:{}", columnName, row);
    }
    return rate;
  }

  private Boolean parseEOM(final FudgeMsg row, final String columnName) {
    Boolean isEom = null;
    final String rollConvStr = row.getString(columnName);
    if (rollConvStr != null) {
      isEom = rollConvStr.equals("EOM");
    } else {
      LOGGER.warn("Missing roll convention in column:{} in row:{}", columnName, row);
    }
    return isEom;
  }

  private Notional parseNotional(final FudgeMsg row, final String notionalColumn, final String ccyColumn) {
    Notional notional = null;
    final String ccyStr = row.getString(ccyColumn);
    if (ccyStr != null) {
      final Currency currency = Currency.of(ccyStr);
      String notionalAmount = row.getString(notionalColumn);
      notionalAmount = notionalAmount.replace(",", "");
      if (notionalAmount != null) {
        notional = new InterestRateNotional(currency, Double.valueOf(notionalAmount));
      } else {
        LOGGER.warn("Missing notional value in column:{} in row:{}", notionalColumn, row);
      }
    } else {
      LOGGER.warn("Missing currency in column:{} in row:{}", ccyColumn, row);
    }
    return notional;
  }

  private ExternalId parseRegionId(final FudgeMsg row, final String columnName) {
    ExternalId regionId = null;
    final String regionIdStr = row.getString(columnName);
    if (regionIdStr != null) {
      final String[] ids = regionIdStr.split(",");
      regionId = ExternalSchemes.isdaHoliday(ids[0]);
    } else {
      LOGGER.warn("Missing RegionId in column:{} in row:{}", columnName, row);
    }
    return regionId;
  }

  private Frequency parseFrequency(final FudgeMsg row, final String columnName) {
    Frequency frequency = null;
    final String frequencyStr = row.getString(columnName);
    if (frequencyStr != null) {
      try {
        frequency = SimpleFrequencyFactory.of(frequencyStr.toLowerCase());
      } catch (final IllegalArgumentException ex) {
        LOGGER.warn("Unknown freqency: {} in  column: {} in row: {}", frequencyStr, columnName, row);
      }
    } else {
      LOGGER.warn("Missing frequency in column:{} in row:{}", columnName, row);
    }
    return frequency;
  }

  private DayCount parseDayCount(final FudgeMsg row, final String columnName) {
    DayCount dayCount = null;
    final String dayCountStr = row.getString(columnName);
    if (dayCountStr != null) {
      final String ogDayCount = DAY_COUNT_MAPPING.getProperty(dayCountStr, null);
      if (ogDayCount != null) {
        dayCount = DayCountFactory.of(ogDayCount);
      } else {
        LOGGER.warn("Missing dayCount mapping for {} in column:{} in row:{}", dayCountStr, columnName, row);
      }

    } else {
      LOGGER.warn("Missing day count in column:{} in row:{}", columnName, row);
    }
    return dayCount;
  }

  private BusinessDayConvention parseBusinessDayConvention(final FudgeMsg row, final String columnName) {
    BusinessDayConvention convention = null;
    final String conventionStr = row.getString(columnName);
    if (conventionStr != null) {
      final String ogBusDayConvention = BUSINESS_DAY_CONVENTION_MAPPING.getProperty(conventionStr, null);
      if (ogBusDayConvention != null) {
        convention = BusinessDayConventionFactory.of(ogBusDayConvention);
      } else {
        LOGGER.warn("Missing convention mapping for {}", conventionStr);
      }

    } else {
      LOGGER.warn("Missing business day convention in column:{}", columnName);
    }
    return convention;
  }

  private LocalDate parseDate(final FudgeMsg row, final String columnName) {
    LocalDate result = null;
    final String dateStr = row.getString(columnName);
    if (dateStr != null) {
      try {
        result = LocalDate.parse(dateStr, DATE_FORMATTER);
      } catch (final DateTimeParseException ex) {
        LOGGER.error("Invalid dateValue:{} in column:{}, skipping...", dateStr, columnName);
      }
    }
    return result;
  }
}
