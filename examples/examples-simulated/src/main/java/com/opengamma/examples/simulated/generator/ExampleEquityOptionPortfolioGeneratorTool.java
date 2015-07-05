/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.examples.simulated.generator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.generator.TreePortfolioNodeGenerator;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * Generates a portfolio of equity options.
 */
@Scriptable
public class ExampleEquityOptionPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {
  /** The counterparty */
  private static final String COUNTERPARTY = "Cpty";
  /** Equity options */
  private static final List<EquityOptionSecurity> OPTIONS = new ArrayList<>();
  /** Generates expiry dates for the options */
  private static final FutureOptionExpiries EXPIRY_GENERATOR = FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1));
  /** The strike formatter */
  private static final DecimalFormat FORMATTER = new DecimalFormat("###.##");

  static {
    final Random rng = new Random(1023567L);
    final LocalDate date = LocalDate.now();
    final LocalTime time = LocalTime.of(16, 0);
    final ZoneOffset zone = ZoneOffset.UTC;
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 50; j++) {
        final double strike = 399 + Math.floor(30 * (1 - rng.nextDouble()));
        final OptionType type = rng.nextBoolean() ? OptionType.PUT : OptionType.CALL;
        final LocalDate expiryDate = EXPIRY_GENERATOR.getExpiry(i + 1, date, Tenor.ONE_MONTH);
        final Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(expiryDate, time), zone));
        final EquityOptionSecurity option = new EquityOptionSecurity(type, strike, Currency.USD, ExternalSchemes.syntheticSecurityId("AAPL"),
            new AmericanExerciseType(), expiry, 25, "EX");
        final String name = "AAPL " + expiryDate + " " + FORMATTER.format(strike) + "" + (type == OptionType.PUT ? "P" : "C");
        option.setName(name);
        OPTIONS.add(option);
      }
    }
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final SecurityGenerator<ManageableSecurity> securities = new EquityOptionSecurityGenerator<>(OPTIONS, "Equity Options");
    configure(securities);
    final TreePortfolioNodeGenerator rootNode = new TreePortfolioNodeGenerator(new StaticNameGenerator("Equity Options"));
    rootNode.addChildNode((PortfolioNodeGenerator) securities);
    return rootNode;
  }

  /**
   * Creates a portfolio node for an array of government bond securities.
   * @param <T> The type of the security
   */
  private class EquityOptionSecurityGenerator<T extends ManageableSecurity> extends SecurityGenerator<T> implements PortfolioNodeGenerator {
    /** The securities */
    private final List<EquityOptionSecurity> _securities;
    /** The name */
    private final String _name;

    /**
     * @param securities The government bond securities
     * @param name The name of the portfolio
     */
    public EquityOptionSecurityGenerator(final List<EquityOptionSecurity> securities, final String name) {
      _securities = securities;
      _name = name;
    }

    @Override
    public PortfolioNode createPortfolioNode() {
      final SimplePortfolioNode node = new SimplePortfolioNode(_name);
      for (int i = 0; i < _securities.size(); i++) {
        final EquityOptionSecurity option = _securities.get(i);
        final BigDecimal amount = BigDecimal.valueOf(100); //TODO
        final LocalDate tradeDate = LocalDate.now().minusDays(7);
        final ManageableTrade trade = new ManageableTrade(amount, getSecurityPersister().storeSecurity(option), tradeDate,
            OffsetTime.of(LocalTime.of(11, 0), ZoneOffset.UTC), ExternalId.of(Counterparty.DEFAULT_SCHEME, COUNTERPARTY));
        trade.setPremium(0.);
        trade.setPremiumCurrency(option.getCurrency());
        final Position position = SimplePositionGenerator.createPositionFromTrade(trade);
        node.addPosition(position);
      }
      return node;
    }

    @Override
    public T createSecurity() {
      return null;
    }
  }
}
