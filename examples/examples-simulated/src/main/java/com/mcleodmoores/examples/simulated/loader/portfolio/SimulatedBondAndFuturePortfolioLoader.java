/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.portfolio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.TextStyle;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Loads an example bond and bond future portfolio.
 */
public class SimulatedBondAndFuturePortfolioLoader extends AbstractTool<ToolContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedBondAndFuturePortfolioLoader.class);
  private static final LocalTime EXPIRY_TIME = LocalTime.of(16, 00);
  private static final ZoneOffset ZONE = ZoneOffset.UTC;
  private final String _portfolioName;
  private final File _tradeFile;
  private final boolean _updateTrades;

  /**
   * @param portfolioName
   *          the portfolio name, not null
   * @param tradeFileName
   *          the trade file name, not null
   * @param updateTrades
   *          true to update trades i.e. adjust maturities
   */
  public SimulatedBondAndFuturePortfolioLoader(final String portfolioName, final String tradeFileName, final boolean updateTrades) {
    _portfolioName = ArgumentChecker.notNull(portfolioName, "portfolioName");
    _tradeFile = new File(ArgumentChecker.notNull(tradeFileName, "tradeFileName"));
    _updateTrades = updateTrades;
  }

  @Override
  protected void doRun() throws Exception {
    final ToolContext context = getToolContext();
    final List<ManageableTrade> trades = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new BufferedReader(new FileReader(_tradeFile)))) {
      String[] line = reader.readNext();
      String label = "";
      while (line != null && line.length > 0) {
        if (line[0].startsWith("#")) {
          label = line[0].toUpperCase();
          line = reader.readNext(); // ignore the headers
          line = reader.readNext();
        }
        ManageableTrade trade = null;
        final String[] updatedLine = new String[line.length];
        if (label.contains("BOND")) {
          if (label.contains("FUTURE")) {
          } else {
            final LocalDate settlementDate = LocalDate.parse(line[13]);
            final Period diff = settlementDate.until(LocalDate.now());
            final int yearDiff = diff.getYears();
            final int monthDiff = diff.getMonths();
            final Period offset = _updateTrades ? Period.ofYears(yearDiff).plusMonths(monthDiff) : Period.ZERO;
            System.arraycopy(line, 0, updatedLine, 0, line.length);
            updatedLine[6] = LocalDate.parse(line[6]).plus(offset).format(DateTimeFormatter.ISO_DATE);
            updatedLine[12] = LocalDate.parse(line[12]).plus(offset).format(DateTimeFormatter.ISO_DATE);
            updatedLine[13] = settlementDate.plus(offset).format(DateTimeFormatter.ISO_DATE);
            updatedLine[14] = LocalDate.parse(line[14]).plus(offset).format(DateTimeFormatter.ISO_DATE);
            updatedLine[23] = LocalDate.parse(line[23]).plus(offset).format(DateTimeFormatter.ISO_DATE);
            updatedLine[25] = LocalDate.parse(line[25]).plus(offset).format(DateTimeFormatter.ISO_DATE);
            final String name = updatedLine[0];
            updatedLine[0] = _updateTrades ? updateName(name, LocalDate.parse(updatedLine[13])) : name;
            trade = createBondTrade(context, updatedLine);
          }
        } else if (label.contains("BILL")) {
          final LocalDate settlementDate = LocalDate.parse(line[9]);
          final Period diff = settlementDate.until(LocalDate.now());
          final int yearDiff = diff.getYears();
          final int monthDiff = diff.getMonths();
          final Period offset = _updateTrades ? Period.ofYears(yearDiff).plusMonths(monthDiff) : Period.ZERO;
          System.arraycopy(line, 0, updatedLine, 0, line.length);
          updatedLine[6] = LocalDate.parse(line[6]).plus(offset).format(DateTimeFormatter.ISO_DATE);
          updatedLine[9] = settlementDate.plus(offset).format(DateTimeFormatter.ISO_DATE);
          updatedLine[15] = LocalDate.parse(line[15]).plus(offset).format(DateTimeFormatter.ISO_DATE);
          updatedLine[17] = LocalDate.parse(line[17]).plus(offset).format(DateTimeFormatter.ISO_DATE);
          final String name = updatedLine[0];
          updatedLine[0] = _updateTrades ? updateName(name, LocalDate.parse(updatedLine[9])) : name;
          trade = createBillTrade(context, updatedLine);
        } else {
          LOGGER.error("Unknown trade type {}", label);
        }
        if (trade != null) {
          trades.add(trade);
        }
        line = reader.readNext();
      }
      if (trades.isEmpty()) {
        LOGGER.error("Could not create any trades");
        return;
      }
      LOGGER.warn("{} trades created. Generating portfolio...", trades.size());
      createPortfolio(context, trades, _portfolioName);
      line = reader.readNext();
    }
  }

  private static ManageableTrade createBondTrade(final ToolContext context, final String[] details) {
    try {
      final String tradeName = details[0];
      final String issuerName = details[1];
      final String issuerType = details[2];
      final String issuerDomicile = details[3];
      final String market = details[4];
      final Currency currency = Currency.of(details[5]);
      final Expiry lastTradeDate = new Expiry(ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(details[6]), EXPIRY_TIME), ZONE),
          ExpiryAccuracy.DAY_MONTH_YEAR);
      final YieldConvention yieldConvention = YieldConventionFactory.INSTANCE.getYieldConvention(details[7]);
      final String couponType = details[8];
      final double couponRate = Double.parseDouble(details[9]);
      final Frequency frequency = PeriodFrequency.of(Period.parse(details[10]));
      final DayCount dayCount = DayCountFactory.of(details[11]);
      final ZonedDateTime interestAccrualDate = ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(details[12]), EXPIRY_TIME), ZONE);
      final ZonedDateTime settlementDate = ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(details[13]), EXPIRY_TIME), ZONE);
      final ZonedDateTime firstCouponDate = ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(details[14]), EXPIRY_TIME), ZONE);
      final double issuancePrice = Double.parseDouble(details[15]);
      final double totalAmountIssued = Double.parseDouble(details[16]);
      final double minimumAmount = Double.parseDouble(details[17]);
      final double minimumIncrement = Double.parseDouble(details[18]);
      final double parAmount = Double.parseDouble(details[19]);
      final double redemptionValue = Double.parseDouble(details[20]);
      final BondSecurity security;
      if (issuerType.equalsIgnoreCase("Sovereign")) {
        security = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, yieldConvention, lastTradeDate, couponType, couponRate,
            frequency, dayCount, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice, totalAmountIssued, minimumAmount, minimumIncrement,
            parAmount, redemptionValue);
      } else {
        LOGGER.error("Unhandled issuer type {}", issuerType);
        return null;
      }
      final BigDecimal quantity = BigDecimal.valueOf(Double.parseDouble(details[21]));
      final ExternalId counterparty = ExternalId.parse(details[22]);
      final LocalDate tradeDate = LocalDate.parse(details[23]);
      final OffsetTime tradeTime = LocalTime.parse(details[24]).atOffset(ZONE);
      final LocalDate premiumDate = LocalDate.parse(details[25]);
      final OffsetTime premiumTime = LocalTime.parse(details[26]).atOffset(ZONE);
      ExternalIdBundle ids = ExternalIdBundle.EMPTY;
      for (int i = 27; i < details.length; i++) {
        ids = ids.withExternalId(ExternalId.parse(details[i]));
      }
      security.setName(tradeName);
      security.setExternalIdBundle(ids);
      final SecurityDocument stored = context.getSecurityMaster().add(new SecurityDocument(security));
      final ManageableTrade trade = new ManageableTrade(quantity, stored.getSecurity().getExternalIdBundle(), tradeDate, tradeTime, counterparty);
      trade.setPremiumCurrency(currency);
      trade.setPremiumDate(premiumDate);
      trade.setPremiumTime(premiumTime);
      trade.setPremium(issuancePrice / 100.);
      return trade;
    } catch (final Exception e) {
      LOGGER.error("Error creating bond {}, error was {}", Arrays.toString(details), e.getMessage());
      return null;
    }
  }

  private static ManageableTrade createBillTrade(final ToolContext context, final String[] details) {
    try {
      final String tradeName = details[0];
      final String issuerName = details[1];
      final String issuerType = details[2];
      final String issuerDomicile = details[3];
      final String market = details[4];
      final Currency currency = Currency.of(details[5]);
      final Expiry lastTradeDate = new Expiry(ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(details[6]), EXPIRY_TIME), ZONE),
          ExpiryAccuracy.DAY_MONTH_YEAR);
      final YieldConvention yieldConvention = YieldConventionFactory.INSTANCE.getYieldConvention(details[7]);
      final DayCount dayCount = DayCountFactory.of(details[8]);
      final ZonedDateTime settlementDate = ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(details[9]), EXPIRY_TIME), ZONE);
      final int daysToSettle = Integer.parseInt(details[10]);
      final double minimumIncrement = Double.parseDouble(details[11]);
      final ExternalId legalEntityId = ExternalId.parse(details[12]);
      final ExternalId regionId = ExternalSchemes.countryRegionId(Country.of(issuerDomicile));
      final BillSecurity security;
      if (issuerType.equalsIgnoreCase("Sovereign")) {
        security = new BillSecurity(currency, lastTradeDate, settlementDate, minimumIncrement, daysToSettle, regionId, yieldConvention, dayCount,
            legalEntityId);
      } else {
        LOGGER.error("Unhandled issuer type {}", issuerType);
        return null;
      }
      final BigDecimal quantity = BigDecimal.valueOf(Double.parseDouble(details[13]));
      final ExternalId counterparty = ExternalId.parse(details[14]);
      final LocalDate tradeDate = LocalDate.parse(details[15]);
      final OffsetTime tradeTime = LocalTime.parse(details[16]).atOffset(ZONE);
      final LocalDate premiumDate = LocalDate.parse(details[17]);
      final OffsetTime premiumTime = LocalTime.parse(details[18]).atOffset(ZONE);
      final Double premium = Double.parseDouble(details[19]);
      ExternalIdBundle ids = ExternalIdBundle.EMPTY;
      for (int i = 20; i < details.length; i++) {
        ids = ids.withExternalId(ExternalId.parse(details[i]));
      }
      security.setName(tradeName);
      security.setExternalIdBundle(ids);
      security.addAttribute("Issuer name", issuerName);
      security.addAttribute("Issuer type", issuerType);
      security.addAttribute("Market", market);
      final SecurityDocument stored = context.getSecurityMaster().add(new SecurityDocument(security));
      final ManageableTrade trade = new ManageableTrade(quantity, stored.getSecurity().getExternalIdBundle(), tradeDate, tradeTime, counterparty);
      trade.setPremiumCurrency(currency);
      trade.setPremiumDate(premiumDate);
      trade.setPremiumTime(premiumTime);
      trade.setPremium(premium);
      return trade;
    } catch (final Exception e) {
      LOGGER.error("Error creating bill {}, error was {}", Arrays.toString(details), e.getMessage());
      return null;
    }
  }

  private static void createPortfolio(final ToolContext context, final List<ManageableTrade> trades, final String portfolioName) {
    final PositionMaster positionMaster = context.getPositionMaster();
    final SimplePortfolio portfolio = new SimplePortfolio(portfolioName);
    final SimplePortfolioNode root = portfolio.getRootNode();
    final ManageablePortfolioNode manageableRoot = new ManageablePortfolioNode(root.getName());
    for (final ManageableTrade trade : trades) {
      final Position position = SimplePositionGenerator.createPositionFromTrade(trade);
      root.addPosition(position);
      final ManageablePosition newPosition = new ManageablePosition();
      newPosition.setAttributes(position.getAttributes());
      newPosition.setQuantity(position.getQuantity());
      newPosition.setSecurityLink(new ManageableSecurityLink(position.getSecurityLink()));
      newPosition.addTrade(trade);
      manageableRoot.addPosition(positionMaster.add(new PositionDocument(newPosition)).getUniqueId());
    }
    final ManageablePortfolio manageablePortfolio = new ManageablePortfolio(portfolioName);
    manageablePortfolio.setRootNode(manageableRoot);
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setDepth(0);
    request.setIncludePositions(false);
    request.setName(portfolio.getName());
    final PortfolioSearchResult result = context.getPortfolioMaster().search(request);
    PortfolioDocument document = result.getFirstDocument();
    if (document != null) {
      LOGGER.info("Overwriting portfolio {}", document.getUniqueId());
      document.setPortfolio(manageablePortfolio);
      context.getPortfolioMaster().update(document);
    } else {
      document = new PortfolioDocument(manageablePortfolio);
      context.getPortfolioMaster().add(document);
    }
  }

  private static String updateName(final String name, final LocalDate updatedStart) {
    // want to update AUG to SEP and 2017 to 2018, for example
    final String[] parts = name.split(" ");
    if (parts.length < 2) {
      return name;
    }
    final StringBuilder sb = new StringBuilder();
    if (parts[0].toUpperCase().matches("(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)[A-Z]{0,6}")) {
      final String originalMonthName = parts[0];
      if (originalMonthName.length() == 3) {
        if (originalMonthName.toUpperCase().equals(originalMonthName)) {
          sb.append(updatedStart.getMonth().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()).toUpperCase());
        } else {
          sb.append(updatedStart.getMonth().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()));
        }
      } else {
        // know length was greater than 3 or it wouldn't have matched
        // making an assumption that the name is in fact a month name
        if (originalMonthName.toUpperCase().equals(originalMonthName)) {
          sb.append(updatedStart.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()).toUpperCase());
        } else {
          sb.append(updatedStart.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()));
        }
      }
    } else {
      sb.append(parts[0]);
    }
    sb.append(" ");
    if (parts[1].matches("20\\d\\d")) {
      sb.append(updatedStart.getYear());
    } else {
      sb.append(parts[1]);
    }
    if (parts.length > 2) {
      sb.append(" ");
    }
    for (int i = 2; i < parts.length; i++) {
      sb.append(parts[i]);
      if (i < name.length() - 1) {
        sb.append(" ");
      }
    }
    return sb.toString();
  }
}
