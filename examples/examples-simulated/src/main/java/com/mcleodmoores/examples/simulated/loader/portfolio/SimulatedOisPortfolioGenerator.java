package com.mcleodmoores.examples.simulated.loader.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.LeafPortfolioNodeGenerator;
import com.opengamma.financial.generator.NameGenerator;
import com.opengamma.financial.generator.PortfolioGenerator;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.PositionGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

public class SimulatedOisPortfolioGenerator extends AbstractPortfolioGeneratorTool {

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SecurityGenerator<SwapSecurity> securities = OisSecurityGenerator.getInstance();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("OIS"), positions, PORTFOLIO_SIZE);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final SecurityGenerator<SwapSecurity> securities = OisSecurityGenerator.getInstance();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<>(securities, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("OIS"), positions, portfolioSize);
  }

  /**
   * Generates OIS swaps.
   */
  private static final class OisSecurityGenerator extends SecurityGenerator<SwapSecurity> {
    /** The logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedOisPortfolioGenerator.OisSecurityGenerator.class);
    /** The tickers of the OIS rates for each currency */
    private static final List<Pair<Currency, ExternalId>> FIXINGS = new ArrayList<>();
    /** The swap tenors */
    private static final Tenor[] TENORS = new Tenor[] {Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FIVE_YEARS,
        Tenor.ofYears(7), Tenor.TEN_YEARS, Tenor.ofYears(15), Tenor.ofYears(20) };
    /** The trade date */
    private static final LocalDate TODAY = LocalDate.now();
    /** The counterparty */
    private static final String CTPTY = "SwapCo";
    /** The singleton instance of this class */
    private static final SecurityGenerator<SwapSecurity> INSTANCE = new OisSecurityGenerator();

    static {
      FIXINGS.add(Pairs.of(Currency.USD, ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDFF")));
      FIXINGS.add(Pairs.of(Currency.GBP, ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "SONIO")));
      FIXINGS.add(Pairs.of(Currency.EUR, ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "EONIA")));
      FIXINGS.add(Pairs.of(Currency.JPY, ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "TONAR")));
    }

    /**
     * Gets the singleton instance.
     * @return The instance
     */
    public static SecurityGenerator<SwapSecurity> getInstance() {
      return INSTANCE;
    }

    /**
     * Private constructor
     */
    private OisSecurityGenerator() {
    }

    @Override
    public SwapSecurity createSecurity() {
      final Pair<Currency, ExternalId> pair = FIXINGS.get(getRandom().nextInt(FIXINGS.size()));
      final Tenor tenor = TENORS[getRandom().nextInt(TENORS.length)];
      final LocalDate maturity = TODAY.plus(tenor.getPeriod());
      final Currency ccy = pair.getFirst();
      final ExternalId floatingRateId = pair.getSecond();
      final HistoricalTimeSeries initialRateSeries = getHistoricalSource().getHistoricalTimeSeries(MarketDataRequirementNames.MARKET_VALUE,
          floatingRateId.toBundle(), null, TODAY, true, TODAY, true);
      if (initialRateSeries == null || initialRateSeries.getTimeSeries() == null) {
        LOGGER.error("Could not get time series for {}", floatingRateId);
        return null;
      }
      final double initialRate = initialRateSeries.getTimeSeries().getEarliestValue();
      final double fixedRate = initialRate * (1 + (getRandom().nextBoolean() ? -1 : 1) * getRandom().nextDouble() / 100.);
      final Double notional = (getRandom().nextInt(9999) + 1) * 10000.;
      final ZonedDateTime tradeDateTime = TODAY.atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTime maturityDateTime = maturity.atStartOfDay(ZoneOffset.UTC);
      final ConventionBundle swapConvention =
          getConventionBundleSource().getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, ccy.getCode() + "_OIS_SWAP"));
      if (swapConvention == null) {
        LOGGER.error("Couldn't get swap convention for {}", ccy.getCode());
        return null;
      }
      final InterestRateNotional interestRateNotional = new InterestRateNotional(ccy, notional);
      final SwapLeg fixedLeg = new FixedInterestRateLeg(swapConvention.getSwapFixedLegDayCount(),
          swapConvention.getSwapFixedLegFrequency(),
          swapConvention.getSwapFixedLegRegion(),
          swapConvention.getSwapFixedLegBusinessDayConvention(),
          interestRateNotional,
          false,
          fixedRate);
      final FloatingInterestRateLeg floatingLeg = new FloatingInterestRateLeg(swapConvention.getSwapFloatingLegDayCount(),
          swapConvention.getSwapFloatingLegFrequency(),
          swapConvention.getSwapFloatingLegRegion(),
          swapConvention.getSwapFloatingLegBusinessDayConvention(),
          interestRateNotional,
          false,
          floatingRateId,
          FloatingRateType.OIS);
      floatingLeg.setInitialFloatingRate(initialRate);
      final boolean isPayFixed = getRandom().nextBoolean();
      final SwapLeg payLeg;
      final SwapLeg receiveLeg;
      if (isPayFixed) {
        payLeg = fixedLeg;
        receiveLeg = floatingLeg;
      } else {
        payLeg = floatingLeg;
        receiveLeg = fixedLeg;
      }
      final SwapSecurity swap = new SwapSecurity(tradeDateTime, tradeDateTime, maturityDateTime, CTPTY, payLeg, receiveLeg);
      final StringBuilder sb = new StringBuilder();
      sb.append(tenor.getPeriod().toString().substring(1));
      sb.append(" OIS, ");
      sb.append(ccy.getCode());
      sb.append(" ");
      sb.append(NOTIONAL_FORMATTER.format(notional));
      sb.append(isPayFixed ? ", pay " : ", receive ");
      sb.append(RATE_FORMATTER.format(fixedRate));
      swap.setName(sb.toString());
      return swap;
    }

  }
}
