/**
 *
 */
package com.mcleodmoores.examples.simulated.loader.portfolio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.generator.MasterSecurityPersister;
import com.opengamma.financial.generator.SecurityPersister;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

import au.com.bytecode.opencsv.CSVReader;

/**
 *
 */
public class BondAndFuturePortfolioLoader extends AbstractTool<ToolContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BondAndFuturePortfolioLoader.class);
  private static final LocalTime EXPIRY_TIME = LocalTime.of(16, 00);
  private static final ZoneOffset ZONE = ZoneOffset.UTC;
  private final String _portfolioName;
  private final File _tradeFile;
  private SecurityPersister _securityPersister;

  public BondAndFuturePortfolioLoader(final String portfolioName, final String tradeFileName) {
    _portfolioName = ArgumentChecker.notNull(portfolioName, "portfolioName");
    _tradeFile = new File(ArgumentChecker.notNull(tradeFileName, "tradeFileName"));
  }

  @Override
  protected void doRun() throws Exception {
    _securityPersister = new MasterSecurityPersister(getToolContext().getSecurityMaster());
    try (CSVReader reader = new CSVReader(new BufferedReader(new FileReader(_tradeFile)))) {
      final List<ManageableTrade> trades = new ArrayList<>();
      String[] line = reader.readNext();
      if (line == null || line.length != 1 && !line[0].startsWith("#")) {
        LOGGER.error("First line was not a trade type: expecting \"# Bond\" or \"# Bond future\"");
        return;
      }
      // bonds then futures
      if (line[0].equalsIgnoreCase("# Bond")) {
        // read bonds until we reach the end of the file or bond futures
        line = reader.readNext(); // ignore the headers
        line = reader.readNext();
        while (line != null && line.length > 0 && !line[0].startsWith("#")) {
          final ManageableTrade trade = createBondTrade(line);
          if (trade != null) {
            trades.add(trade);
          }
          line = reader.readNext();
        }
        if (line != null && line[0].equalsIgnoreCase("# Bond future")) {
          // read bond futures until we reach the end of the file
          line = reader.readNext(); // ignore the headers
          while (line != null && line.length > 0 && !line[0].startsWith("#")) {
            line = reader.readNext();
          }
        }
      } else if (line[0].equalsIgnoreCase("# Bond")) {
        // futures then bonds
        // read bonds until we reach the end of the file or bond futures
        line = reader.readNext();
        while (line != null && line.length > 0 && !line[0].startsWith("#")) {
          line = reader.readNext();
        }
        if (line != null && line[0].equalsIgnoreCase("# Bond future")) {
          // read bond futures until we reach the end of the file
          line = reader.readNext();
          while (line != null && line.length > 0 && !line[0].startsWith("#")) {
            line = reader.readNext();
          }
        }
      }
    } catch (final Exception e) {
      LOGGER.error(e.getMessage());
    }
  }

  private ManageableTrade createBondTrade(final String[] details) {
    try {
      final String tradeName = details[0];
      final String issuerName = details[1];
      final String issuerType = details[2];
      final String issuerDomicile = details[3];
      final String market = details[4];
      final Currency currency = Currency.of(details[5]);
      final Expiry lastTradeDate =
          new Expiry(ZonedDateTime.of(LocalDateTime.of(LocalDate.parse(details[6]), EXPIRY_TIME), ZONE), ExpiryAccuracy.DAY_MONTH_YEAR);
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
        security = new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, yieldConvention, lastTradeDate, couponType,
            couponRate, frequency, dayCount, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice, totalAmountIssued, minimumAmount,
            minimumIncrement, parAmount, redemptionValue);
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
      final ExternalIdBundle ids = ExternalIdBundle.EMPTY;
      for (int i = 27; i < details.length; i++) {
        ids.withExternalId(ExternalId.parse(details[i]));
      }
      security.setName(tradeName);
      security.setExternalIdBundle(ids);
      final ManageableTrade trade = new ManageableTrade(quantity, _securityPersister.storeSecurity(security), tradeDate, tradeTime, counterparty);
      trade.setPremiumCurrency(currency);
      trade.setPremiumDate(premiumDate);
      trade.setPremiumTime(premiumTime);
      trade.setPremium(quantity.doubleValue() * issuancePrice);
      return trade;
    } catch (final Exception e) {
      e.printStackTrace(System.err);
      LOGGER.error("Error creating bond {}, error was {}", Arrays.toString(details), e.getMessage());
      return null;
    }
  }
}
