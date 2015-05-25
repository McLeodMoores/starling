/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CalendarSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeVisitor;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.PeriodicallyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ThreeLegBasisSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.financial.convention.BondConvention;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.EquityConvention;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.convention.FinancialConventionVisitor;
import com.opengamma.financial.convention.FixedInterestRateSwapLegConvention;
import com.opengamma.financial.convention.FixedLegRollDateConvention;
import com.opengamma.financial.convention.FloatingInterestRateSwapLegConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.ONArithmeticAverageLegConvention;
import com.opengamma.financial.convention.ONCompoundedLegRollDateConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.RollDateFRAConvention;
import com.opengamma.financial.convention.RollDateSwapConvention;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.VanillaIborLegRollDateConvention;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Returns all of the currencies relevant for a {@link com.opengamma.financial.analytics.ircurve.strips.CurveNode}.
 * This information is pulled from the convention(s) associated with the node. Returns null if there are no currencies applicable
 * to the curve node.
 * <p>
 * This code differs from the base OpenGamma release in the way that it handles indices (ibor, overnight and price). The
 * convention source is first searched and then the security source is searched only if no convention is found. This is to
 * remove the requirement to have a security for each underlying, which is overly restrictive in cases where all useful
 * information comes from the convention. However, backwards compatibility is maintained in the case where a security is loaded but not
 * an underlying convention
 */
public class CurveNodeCurrencyVisitor implements CurveNodeVisitor<Set<Currency>>, FinancialConventionVisitor<Set<Currency>> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(CurveNodeCurrencyVisitor.class);
  /** The security source */
  private final SecuritySource _securitySource;
  /** The convention source */
  private final ConventionSource _conventionSource;
  /** The config source */
  private final ConfigSource _configSource;

  /**
   * @param securitySource The security source. Not null.
   * @param conventionSource The convention source, not null
   */
  public CurveNodeCurrencyVisitor(final ConventionSource conventionSource, final SecuritySource securitySource) {
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _conventionSource = conventionSource;
    _securitySource = securitySource;
    _configSource = null;
  }

  /**
   * @param securitySource The security source. Not null.
   * @param conventionSource The convention source, not null
   * @param configSource The config source, not null
   */
  public CurveNodeCurrencyVisitor(final ConventionSource conventionSource, final SecuritySource securitySource,
      final ConfigSource configSource) {
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(configSource, "configSource");
    _conventionSource = conventionSource;
    _securitySource = securitySource;
    _configSource = configSource;
  }

  /**
   * Gets the convention source.
   * @return The convention source
   */
  protected ConventionSource getConventionSource() {
    return _conventionSource;
  }

  /**
   * Gets the security source.
   * @return The security source
   */
  protected SecuritySource getSecuritySource() {
    return _securitySource;
  }

  /**
   * Gets the config source.
   * @return The config source
   */
  protected ConfigSource getConfigSource() {
    return _configSource;
  }

  @Override
  public Set<Currency> visitBillNode(final BillNode node) {
    if (_configSource == null) {
      throw new OpenGammaRuntimeException("Config source was null");
    }
    final CurveNodeIdMapper idMapper = _configSource.getSingle(CurveNodeIdMapper.class, node.getCurveNodeIdMapperName(), VersionCorrection.LATEST);
    if (idMapper == null) {
      throw new OpenGammaRuntimeException("CurveNodeIdMapper called " + node.getCurveNodeIdMapperName() + " was not present in config source");
    }
    final Security security =
        _securitySource.getSingle(idMapper.getBillNodeId(null, node.getMaturityTenor()).toBundle()); // curve date is not relevant for bills
    if (security == null) {
      throw new OpenGammaRuntimeException("Security underlying bill node " + node + " was not present in security source");
    }
    if (security instanceof BillSecurity) {
      return Collections.singleton(((BillSecurity) security).getCurrency());
    }
    throw new OpenGammaRuntimeException("Security underlying bill node " + node + " was not a bill); have " + security.getClass());
  }

  @Override
  public Set<Currency> visitBondNode(final BondNode node) {
    if (_configSource == null) {
      throw new OpenGammaRuntimeException("Config source was null");
    }
    final CurveNodeIdMapper idMapper = _configSource.getSingle(CurveNodeIdMapper.class, node.getCurveNodeIdMapperName(), VersionCorrection.LATEST);
    if (idMapper == null) {
      throw new OpenGammaRuntimeException("CurveNodeIdMapper called " + node.getCurveNodeIdMapperName() + " was not present in config source");
    }
    final Security security =
        _securitySource.getSingle(idMapper.getBondNodeId(null, node.getMaturityTenor()).toBundle()); // curve date is not relevant for bonds
    if (security == null) {
      throw new OpenGammaRuntimeException("Security underlying bond node " + node + " was not present in security soource");
    }
    if (security instanceof BondSecurity) {
      return Collections.singleton(((BondSecurity) security).getCurrency());
    }
    throw new OpenGammaRuntimeException("Security underlying bond node " + node + " was not a bond; have " + security.getClass());
  }

  @Override
  public Set<Currency> visitCalendarSwapNode(final CalendarSwapNode node) {
    final FinancialConvention convention = _conventionSource.getSingle(node.getSwapConvention(), SwapConvention.class);
    return convention.accept(this);
  }

  @Override
  public Set<Currency> visitCashNode(final CashNode node) {
    try {
      final FinancialConvention convention = _conventionSource.getSingle(node.getConvention(), FinancialConvention.class);
      return convention.accept(this);
    } catch (final DataNotFoundException e) {
      // If the convention is not found in the convention source then try the convention referenced in the underlying security
      final Security security = _securitySource.getSingle(node.getConvention().toBundle());
      if (security == null) {
        throw new OpenGammaRuntimeException("Could not get security with id " + node.getConvention());
      }
      if (security instanceof IborIndex) {
        final IborIndex indexSecurity = (IborIndex) security;
        final IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
        return indexConvention.accept(this);
      } else if (security instanceof OvernightIndex) {
        final OvernightIndex indexSecurity = (OvernightIndex) security;
        final OvernightIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), OvernightIndexConvention.class);
        return indexConvention.accept(this);
      }
      throw new OpenGammaRuntimeException("Security should be of type IborIndex or OvernightIndex, was " + security);
    }
  }

  @Override
  public Set<Currency> visitContinuouslyCompoundedRateNode(final ContinuouslyCompoundedRateNode node) {
    return Collections.emptySet();
  }

  @Override
  public Set<Currency> visitPeriodicallyCompoundedRateNode(final PeriodicallyCompoundedRateNode node) {
    return Collections.emptySet();
  }

  @Override
  public Set<Currency> visitCreditSpreadNode(final CreditSpreadNode node) {
    return Collections.emptySet();
  }

  @Override
  public Set<Currency> visitDeliverableSwapFutureNode(final DeliverableSwapFutureNode node) {
    final FinancialConvention convention = _conventionSource.getSingle(node.getFutureConvention(), FinancialConvention.class);
    return convention.accept(this);
  }

  @Override
  public Set<Currency> visitDiscountFactorNode(final DiscountFactorNode node) {
    return Collections.emptySet();
  }

  @Override
  public Set<Currency> visitFRANode(final FRANode node) {
    try {
      final FinancialConvention convention = _conventionSource.getSingle(node.getConvention(), IborIndexConvention.class);
      return convention.accept(this);
    } catch (final DataNotFoundException e) {
      // If the convention is not found in the convention source then try the convention referenced in the underlying security
      final Security sec = _securitySource.getSingle(node.getConvention().toBundle());
      if (sec == null) {
        throw new OpenGammaRuntimeException("Could not get underlying security for FRANode with id " + node.getConvention() + " from source");
      }
      if (sec instanceof IborIndex) {
        final IborIndex indexSecurity = (IborIndex) sec;
        final IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
        return indexConvention.accept(this);
      }
      throw new OpenGammaRuntimeException("Underlying security with id " + node.getConvention() + " was not an IborIndex: have " + sec);
    }
  }

  @Override
  public Set<Currency> visitFXForwardNode(final FXForwardNode node) {
    return Sets.newHashSet(node.getPayCurrency(), node.getReceiveCurrency());
  }

  @Override
  public Set<Currency> visitRollDateFRANode(final RollDateFRANode node) {
    final RollDateFRAConvention convention = _conventionSource.getSingle(node.getRollDateFRAConvention(), RollDateFRAConvention.class);
    return convention.accept(this);
  }

  @Override
  public Set<Currency> visitRollDateSwapNode(final RollDateSwapNode node) {
    final RollDateSwapConvention convention = _conventionSource.getSingle(node.getRollDateSwapConvention(), RollDateSwapConvention.class);
    return convention.accept(this);
  }

  @Override
  public Set<Currency> visitRateFutureNode(final RateFutureNode node) {
    final FinancialConvention convention = _conventionSource.getSingle(node.getFutureConvention(), FinancialConvention.class);
    if (convention instanceof InterestRateFutureConvention) {
      final InterestRateFutureConvention irFutureConvention = (InterestRateFutureConvention) convention;
      return irFutureConvention.accept(this);
    } else if (convention instanceof FederalFundsFutureConvention) {
      final FederalFundsFutureConvention ffFutureConvention = (FederalFundsFutureConvention) convention;
      return ffFutureConvention.accept(this);
    }
    throw new OpenGammaRuntimeException("Unhandled convention type " + convention);
  }

  @Override
  public Set<Currency> visitSwapNode(final SwapNode node) {
    final FinancialConvention payConvention = _conventionSource.getSingle(node.getPayLegConvention(), FinancialConvention.class);
    final FinancialConvention receiveConvention = _conventionSource.getSingle(node.getReceiveLegConvention(), FinancialConvention.class);
    final Set<Currency> currencies = new HashSet<>(payConvention.accept(this));
    currencies.addAll(receiveConvention.accept(this));
    return currencies;
  }

  @Override
  public Set<Currency> visitThreeLegBasisSwapNode(final ThreeLegBasisSwapNode node) {
    final FinancialConvention payConvention = _conventionSource.getSingle(node.getPayLegConvention(), FinancialConvention.class);
    final FinancialConvention receiveConvention = _conventionSource.getSingle(node.getReceiveLegConvention(), FinancialConvention.class);
    final FinancialConvention spreadConvention = _conventionSource.getSingle(node.getSpreadLegConvention(), FinancialConvention.class);
    final Set<Currency> currencies = new HashSet<>(payConvention.accept(this));
    currencies.addAll(receiveConvention.accept(this));
    currencies.addAll(spreadConvention.accept(this));
    return currencies;
  }

  @Override
  public Set<Currency> visitZeroCouponInflationNode(final ZeroCouponInflationNode node) {
    final FinancialConvention inflationLegConvention = _conventionSource.getSingle(node.getInflationLegConvention(), FinancialConvention.class);
    final FinancialConvention fixedLegConvention = _conventionSource.getSingle(node.getFixedLegConvention(), FinancialConvention.class);
    final Set<Currency> currencies = new HashSet<>(inflationLegConvention.accept(this));
    currencies.addAll(fixedLegConvention.accept(this));
    return currencies;
  }

  @Override
  public Set<Currency> visitBondConvention(final BondConvention convention) {
    return Collections.emptySet();
  }

  @Override
  public Set<Currency> visitCMSLegConvention(final CMSLegConvention convention) {
    final FinancialConvention underlyingConvention = _conventionSource.getSingle(convention.getSwapIndexConvention(), FinancialConvention.class);
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitCompoundingIborLegConvention(final CompoundingIborLegConvention convention) {
    final Security sec = _securitySource.getSingle(convention.getIborIndexConvention().toBundle());
    if (sec == null) {
      throw new OpenGammaRuntimeException("CurveNodeCurrencyVisitor.visitCompoundingIborLegConvention: Ibor index with id "
          + convention.getIborIndexConvention() + " was null");
    }
    final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
    final IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
    if (indexConvention == null) {
      throw new OpenGammaRuntimeException("CurveNodeCurrencyVisitor.visitCompoundingIborLegConvention: Convention with id "
          + indexSecurity.getConventionId() + " was null");
    }
    return indexConvention.accept(this);
  }

  @Override
  public Set<Currency> visitDepositConvention(final DepositConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitEquityConvention(final EquityConvention convention) {
    return Collections.emptySet();
  }

  @Override
  public Set<Currency> visitDeliverablePriceQuotedSwapFutureConvention(final DeliverablePriceQuotedSwapFutureConvention convention) {
    final FinancialConvention underlyingConvention = _conventionSource.getSingle(convention.getSwapConvention(), FinancialConvention.class);
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitFederalFundsFutureConvention(final FederalFundsFutureConvention convention) {
    try {
      final OvernightIndexConvention indexConvention = _conventionSource.getSingle(convention.getIndexConvention(), OvernightIndexConvention.class);
      return indexConvention.accept(this);
    } catch (final DataNotFoundException e) {
      final Security security = _securitySource.getSingle(convention.getIndexConvention().toBundle());
      if (security instanceof OvernightIndex) {
        return _conventionSource.getSingle(((OvernightIndex) security).getConventionId(), OvernightIndexConvention.class).accept(this);
      }
      throw new OpenGammaRuntimeException("Could not get security or convention with id " + convention.getIndexConvention());
    }
  }

  @Override
  public Set<Currency> visitFixedInterestRateSwapLegConvention(final FixedInterestRateSwapLegConvention convention) {
    return Collections.emptySet();
  }

  @Override
  public Set<Currency> visitFloatingInterestRateSwapLegConvention(final FloatingInterestRateSwapLegConvention convention) {
    return Collections.emptySet();
  }

  @Override
  public Set<Currency> visitFXForwardAndSwapConvention(final FXForwardAndSwapConvention convention) {
    return Collections.emptySet();
  }

  @Override
  public Set<Currency> visitFXSpotConvention(final FXSpotConvention convention) {
    return Collections.emptySet();
  }

  @Override
  public Set<Currency> visitIborIndexConvention(final IborIndexConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitIMMFRAConvention(final RollDateFRAConvention convention) {
    try {
      final FinancialConvention indexConvention = _conventionSource.getSingle(convention.getIndexConvention(), IborIndexConvention.class);
      return indexConvention.accept(this);
    } catch (final DataNotFoundException e) {
      // If the convention is not found in the convention source then try the convention referenced in the underlying security
      final Security sec = _securitySource.getSingle(convention.getIndexConvention().toBundle());
      if (sec == null) {
        throw new OpenGammaRuntimeException("Could not get underlying security for RollDateFRAConvention with id "
            + convention.getIndexConvention() + " from source");
      }
      if (sec instanceof IborIndex) {
        final IborIndex indexSecurity = (IborIndex) sec;
        final IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
        return indexConvention.accept(this);
      }
      throw new OpenGammaRuntimeException("Underlying security with id " + convention.getIndexConvention() + " was not an IborIndex: have " + sec);
    }
  }

  @Override
  public Set<Currency> visitIMMSwapConvention(final RollDateSwapConvention convention) {
    final FinancialConvention payConvention = _conventionSource.getSingle(convention.getPayLegConvention(), FinancialConvention.class);
    final FinancialConvention receiveConvention = _conventionSource.getSingle(convention.getReceiveLegConvention(), FinancialConvention.class);
    final Set<Currency> currencies = new HashSet<>(payConvention.accept(this));
    currencies.addAll(receiveConvention.accept(this));
    return currencies;
  }

  @Override
  public Set<Currency> visitInflationLegConvention(final InflationLegConvention convention) {
    try {
      final FinancialConvention indexConvention = _conventionSource.getSingle(convention.getPriceIndexConvention(), PriceIndexConvention.class);
      return indexConvention.accept(this);
    } catch (final DataNotFoundException e) {
      // If the convention is not found in the convention source then try the convention referenced in the underlying security
      final Security sec = _securitySource.getSingle(convention.getPriceIndexConvention().toBundle());
      if (sec == null) {
        throw new OpenGammaRuntimeException("Could not get underlying security for InflationLegConvention with id "
            + convention.getPriceIndexConvention() + " from source");
      }
      if (sec instanceof PriceIndex) {
        final PriceIndex indexSecurity = (PriceIndex) sec;
        return _conventionSource.getSingle(indexSecurity.getConventionId(), PriceIndexConvention.class).accept(this);
      }
      throw new OpenGammaRuntimeException("Underlying security with id " + convention.getPriceIndexConvention() + " was not a PriceIndex: have " + sec);
    }
  }

  @Override
  public Set<Currency> visitInterestRateFutureConvention(final InterestRateFutureConvention convention) {
    try {
      final IborIndexConvention indexConvention = _conventionSource.getSingle(convention.getIndexConvention(), IborIndexConvention.class);
      return indexConvention.accept(this);
    } catch (final DataNotFoundException e) {
      final Security security = _securitySource.getSingle(convention.getIndexConvention().toBundle());
      if (security instanceof IborIndex) {
        return _conventionSource.getSingle(((IborIndex) security).getConventionId(), IborIndexConvention.class).accept(this);
      }
      throw new OpenGammaRuntimeException("Could not get security or convention with id " + convention.getIndexConvention());
    }
  }

  @Override
  public Set<Currency> visitOISLegConvention(final OISLegConvention convention) {
    final OvernightIndex index = (OvernightIndex) _securitySource.getSingle(convention.getOvernightIndexConvention().toBundle());
    if (index == null) {
      throw new OpenGammaRuntimeException("Overnight index with id " + convention.getOvernightIndexConvention() + " not found in security source.");
    }
    final FinancialConvention underlyingConvention = _conventionSource.getSingle(index.getConventionId(), FinancialConvention.class);
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitONCompoundedLegRollDateConvention(final ONCompoundedLegRollDateConvention convention) {
    final OvernightIndex index = (OvernightIndex) _securitySource.getSingle(convention.getOvernightIndexConvention().toBundle());
    final FinancialConvention underlyingConvention = _conventionSource.getSingle(index.getConventionId(), FinancialConvention.class);
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitONArithmeticAverageLegConvention(final ONArithmeticAverageLegConvention convention) {
    final OvernightIndex index = (OvernightIndex) _securitySource.getSingle(convention.getOvernightIndexConvention().toBundle());
    final FinancialConvention underlyingConvention = _conventionSource.getSingle(index.getConventionId(), FinancialConvention.class);
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitOvernightIndexConvention(final OvernightIndexConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitPriceIndexConvention(final PriceIndexConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitSwapConvention(final SwapConvention convention) {
    final FinancialConvention payConvention = _conventionSource.getSingle(convention.getPayLegConvention(), FinancialConvention.class);
    final FinancialConvention receiveConvention = _conventionSource.getSingle(convention.getReceiveLegConvention(), FinancialConvention.class);
    final Set<Currency> currencies = new HashSet<>(payConvention.accept(this));
    currencies.addAll(receiveConvention.accept(this));
    return currencies;
  }

  @Override
  public Set<Currency> visitSwapFixedLegConvention(final SwapFixedLegConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitFixedLegRollDateConvention(final FixedLegRollDateConvention convention) {
    return Collections.singleton(convention.getCurrency());
  }

  @Override
  public Set<Currency> visitSwapIndexConvention(final SwapIndexConvention convention) {
    final FinancialConvention underlyingConvention = _conventionSource.getSingle(convention.getSwapConvention(), FinancialConvention.class);
    return underlyingConvention.accept(this);
  }

  @Override
  public Set<Currency> visitVanillaIborLegConvention(final VanillaIborLegConvention convention) {
    try {
      final FinancialConvention indexConvention = _conventionSource.getSingle(convention.getIborIndexConvention(), IborIndexConvention.class);
      return indexConvention.accept(this);
    } catch (final DataNotFoundException e) {
      // If the convention is not found in the convention source then try the convention referenced in the underlying security
      final Security sec = _securitySource.getSingle(convention.getIborIndexConvention().toBundle());
      if (sec == null) {
        throw new OpenGammaRuntimeException("Could not get underlying security for VanillaIborLegConvention with id "
            + convention.getIborIndexConvention() + " from source");
      }
      if (sec instanceof IborIndex) {
        final IborIndex indexSecurity = (IborIndex) sec;
        final IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
        return indexConvention.accept(this);
      }
      throw new OpenGammaRuntimeException("Underlying security with id " + convention.getIborIndexConvention() + " was not an IborIndex: have " + sec);
    }
  }

  @Override
  public Set<Currency> visitVanillaIborLegRollDateConvention(final VanillaIborLegRollDateConvention convention) {
    try {
      final FinancialConvention indexConvention = _conventionSource.getSingle(convention.getIborIndexConvention(), IborIndexConvention.class);
      return indexConvention.accept(this);
    } catch (final DataNotFoundException e) {
      // If the convention is not found in the convention source then try the convention referenced in the underlying security
      final Security sec = _securitySource.getSingle(convention.getIborIndexConvention().toBundle());
      if (sec == null) {
        throw new OpenGammaRuntimeException("Could not get underlying security for VanillaIborLegConvention with id "
            + convention.getIborIndexConvention() + " from source");
      }
      if (sec instanceof IborIndex) {
        final IborIndex indexSecurity = (IborIndex) sec;
        final IborIndexConvention indexConvention = _conventionSource.getSingle(indexSecurity.getConventionId(), IborIndexConvention.class);
        return indexConvention.accept(this);
      }
      throw new OpenGammaRuntimeException("Underlying security with id " + convention.getIborIndexConvention() + " was not an IborIndex: have " + sec);
    }
  }

}
