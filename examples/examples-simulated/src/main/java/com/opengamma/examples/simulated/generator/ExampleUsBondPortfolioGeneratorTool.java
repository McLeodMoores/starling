/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.examples.simulated.generator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.LeafPortfolioNodeGenerator;
import com.opengamma.financial.generator.NameGenerator;
import com.opengamma.financial.generator.PortfolioGenerator;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.PositionGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Generates a portfolio of fixed-coupon bonds.
 */
@Scriptable
public class ExampleUsBondPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The counterparty */
  private static final String COUNTERPARTY = "Cpty";
  /** Bonds */
  private static final List<BondSecurity> BONDS = new ArrayList<>();
  /** Amounts of each bond */
  private static final List<Double> AMOUNTS = new ArrayList<>();
  /** List of (issuer domicile, currency, issuer name, issuer type, market, yield convention name, coupon type, issuer prefix) */
  private static final List<List<String>> ISSUERS = new ArrayList<>();

  static {
    ISSUERS.add(Arrays.asList("US", "USD", "US TREASURY N/B", "Sovereign", "US GOVERNMENT", "US street", "FIXED", "T "));
    final Frequency couponFrequency = PeriodFrequency.SEMI_ANNUAL;
    final DayCount dayCountConvention = DayCounts.ACT_ACT_ICMA;
    final double totalAmountIssued = 10000000000.;
    final double minimumAmount = 100;
    final double minimumIncrement = 100;
    final double parAmount = 100;
    final double redemptionValue = 100;
    final int nBonds = 100;
    final Random rng = new Random(345);
    final LocalDate referenceDate = LocalDate.now().minusYears(1).withMonth(2).withDayOfMonth(15);
    for (int i = 0; i < nBonds; i++) {
      final List<String> issuer = ISSUERS.get(rng.nextInt(ISSUERS.size()));
      final int monthsAhead = 18 + rng.nextInt(50) * 6;
      final ZonedDateTime maturity = referenceDate.plusMonths(monthsAhead).atStartOfDay(ZoneOffset.UTC);
      final int tenor = monthsAhead + 6 * (1 + rng.nextInt(20));
      final ZonedDateTime startDate = maturity.minusMonths(tenor);
      final double coupon = (1 + rng.nextInt(39)) / 8.;
      final double issuePrice = 100 - 3 * rng.nextDouble();
      final GovernmentBondSecurity bond = new GovernmentBondSecurity(issuer.get(2), issuer.get(3), issuer.get(0), issuer.get(4), Currency.of(issuer.get(1)),
          YieldConventionFactory.of(issuer.get(5)), new Expiry(maturity), issuer.get(6), coupon, couponFrequency, dayCountConvention,
          startDate, startDate, startDate.plusMonths(6), issuePrice, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
      final String name = generateName(issuer.get(7), maturity.toLocalDate(), coupon);
      bond.setName(name);
      bond.setExternalIdBundle(ExternalIdBundle.of(ExternalSchemes.syntheticSecurityId(name)));
      BONDS.add(bond);
      AMOUNTS.add(10000. * (1 + rng.nextInt(200)));
    }
  }

  /**
   * Generates a ticker / coupon / maturity name for a bond.
   * @param issuerPrefix The bond prefix for an issuer
   * @param maturity The bond maturity date
   * @param coupon The (fixed) coupon
   * @return The T/C/M
   */
  private static String generateName(final String issuerPrefix, final LocalDate maturity, final double coupon) {
    final StringBuilder sb = new StringBuilder(issuerPrefix);
    final int floor = (int) Math.floor(coupon);
    final double remainder = coupon - floor;
    final StringBuilder couponString = new StringBuilder(Integer.toString(floor));
    couponString.append(" ");
    if (Double.compare(floor, coupon) != 0) {
      boolean remainderSet = false;
      for (int i = 2; i <= 8; i *= 2) {
        final double scaled = remainder * i;
        if (Double.compare(scaled, Math.round(remainder * i)) == 0) {
          couponString.append(Integer.toString((int) scaled));
          couponString.append("/");
          couponString.append(Integer.toString(i));
          remainderSet = true;
          break;
        }
      }
      if (!remainderSet) {
        throw new IllegalArgumentException("Coupon was not expressed in 8ths");
      }
    }
    sb.append(couponString);
    sb.append(" ");
    sb.append(maturity.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
    return sb.toString();
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final BondSecurityAndPositionGenerator securitiesAndPositions = new BondSecurityAndPositionGenerator(BONDS, AMOUNTS);
    configure(securitiesAndPositions);
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("Bonds"), securitiesAndPositions, BONDS.size());
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final BondSecurityAndPositionGenerator securitiesAndPositions = new BondSecurityAndPositionGenerator(BONDS, AMOUNTS);
    configure(securitiesAndPositions);
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Bonds"), securitiesAndPositions, BONDS.size());
  }

  /**
   * Creates a security, position and portfolio node for list of government bond securities.
   */
  private class BondSecurityAndPositionGenerator extends SecurityGenerator<BondSecurity> implements PortfolioNodeGenerator, PositionGenerator {
    /** The securities */
    private final List<BondSecurity> _securities;
    /** The amounts */
    private final List<Double> _amounts;
    /** The security count */
    private int _securityCount;
    /** The position count */
    private int _positionCount;

    /**
     * @param securities The government bond securities
     * @param amounts The amount in each position
     */
    public BondSecurityAndPositionGenerator(final List<BondSecurity> securities, final List<Double> amounts) {
      _securities = securities;
      _amounts = amounts;
    }

    @Override
    public PortfolioNode createPortfolioNode() {
      final SimplePortfolioNode node = new SimplePortfolioNode("Bonds");
      for (int i = 0; i < _securities.size(); i++) {
        node.addPosition(createPosition());
      }
      return node;
    }

    @Override
    public BondSecurity createSecurity() {
      return _securities.get(_securityCount++);
    }

    @Override
    public Position createPosition() {
      final BigDecimal n = new BigDecimal(_amounts.get(_positionCount));
      final BondSecurity bond = _securities.get(_positionCount++);
      final ZonedDateTime tradeDate = bond.getSettlementDate();
      final ManageableTrade trade = new ManageableTrade(n, getSecurityPersister().storeSecurity(bond), tradeDate.toLocalDate(),
          tradeDate.toOffsetDateTime().toOffsetTime(), ExternalId.of(Counterparty.DEFAULT_SCHEME, COUNTERPARTY));
      trade.setPremium(bond.getIssuancePrice());
      trade.setPremiumCurrency(bond.getCurrency());
      trade.setPremiumDate(tradeDate.toLocalDate());
      return SimplePositionGenerator.createPositionFromTrade(trade);
    }
  }
}
