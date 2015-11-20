/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.exposure.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorSameMethodAdapter;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Implementation of an exposure function that returns ids containing information about the security type,
 * the currency and the region. For example, for a German swap, the identifier would be
 * <code>SecurityType~SWAP_EUR_DE</code>.
 */
public class SecurityCurrencyAndRegionExposureFunction implements NamedExposureFunction {

  /**
   * The name of this exposure function.
   */
  public static final String NAME = "Security / Currency / Region";

  @Override
  public List<ExternalId> getIds(final Trade trade, final FunctionExecutionContext context) {
    final Security security = trade.getSecurity();
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(new SecurityCurrencyAndRegionVisitor(context.getSecuritySource()));
    }
    return null;
  }

  @Override
  public List<ExternalId> getIds(final Trade trade, final FunctionCompilationContext context) {
    final Security security = trade.getSecurity();
    if (security instanceof FinancialSecurity) {
      return ((FinancialSecurity) security).accept(new SecurityCurrencyAndRegionVisitor(context.getSecuritySource()));
    }
    return null;
  }

  @Override
  public List<ExternalId> getIds(final Trade trade) {
    // TODO not all security types require a security source - should this method be allowed for those cases?
    throw new UnsupportedOperationException("Must supply a security source");
  }

  @Override
  public String getName() {
    return NAME;
  }

  /**
   * A visitor that returns a list of ids appropriate for each security type or null if there are no matching ids.
   */
  private static final class SecurityCurrencyAndRegionVisitor extends FinancialSecurityVisitorSameMethodAdapter<List<ExternalId>> {
    /** The security source */
    private final SecuritySource _securitySource;

    /**
     * Creates an instance.
     * @param securitySource The security source, not null
     */
    public SecurityCurrencyAndRegionVisitor(final SecuritySource securitySource) {
      super(null);
      _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
    }

    /**
     * Creates ids for a collection of currencies.
     * @param securityType The security type string
     * @param region The region string
     * @param currencies The currencies
     * @return The ids
     */
    private static List<ExternalId> createIds(final String securityType, final String region, final Collection<Currency> currencies) {
      final List<ExternalId> ids = new ArrayList<>(currencies.size());
      for (final Currency currency : currencies) {
        ids.add(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + currency.getCode() + SEPARATOR + region));
      }
      return ids;
    }

    /**
     * Creates an id.
     * @param securityType The security type string
     * @param region The region string
     * @param currency The currency string
     * @return The id
     */
    private static List<ExternalId> createIds(final String securityType, final String region, final String currency) {
      return Collections.singletonList(ExternalId.of(SECURITY_IDENTIFIER, securityType + SEPARATOR + currency + SEPARATOR + region));
    }

    @Override
    public List<ExternalId> visitCashSecurity(final CashSecurity security) {
      return createIds(CashSecurity.SECURITY_TYPE, security.getRegionId().getValue(), security.getCurrency().getCode());
    }

    @Override
    public List<ExternalId> visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
      return createIds(ContinuousZeroDepositSecurity.SECURITY_TYPE, security.getRegion().getValue(), security.getCurrency().getCode());
    }

    @Override
    public List<ExternalId> visitCorporateBondSecurity(final CorporateBondSecurity security) {
      return createIds(BondSecurity.SECURITY_TYPE, security.getIssuerDomicile(), security.getCurrency().getCode());
    }

    @Override
    public List<ExternalId> visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
      return createIds(EquityVarianceSwapSecurity.SECURITY_TYPE, security.getRegionId().getValue(), security.getCurrency().getCode());
    }

    @Override
    public List<ExternalId> visitFRASecurity(final FRASecurity security) {
      return createIds(FRASecurity.SECURITY_TYPE, security.getRegionId().getValue(), security.getCurrency().getCode());
    }

    @Override
    public List<ExternalId> visitBillSecurity(final BillSecurity security) {
      return createIds(BillSecurity.SECURITY_TYPE, security.getRegionId().getValue(), security.getCurrency().getCode());
    }

    @Override
    public List<ExternalId> visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
      return createIds(BondSecurity.SECURITY_TYPE, security.getIssuerDomicile(), security.getCurrency().getCode());
    }

    @Override
    public List<ExternalId> visitInflationBondSecurity(final InflationBondSecurity security) {
      return createIds(BondSecurity.SECURITY_TYPE, security.getIssuerDomicile(), security.getCurrency().getCode());
    }

    @Override
    public List<ExternalId> visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
      return createIds(BondSecurity.SECURITY_TYPE, security.getIssuerDomicile(), security.getCurrency().getCode());
    }

    @Override
    public List<ExternalId> visitSwapSecurity(final SwapSecurity security) {
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      final Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(security, _securitySource);
      if (currencies == null) {
        return null;
      }
      if (payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
        return createIds(SwapSecurity.SECURITY_TYPE, payLeg.getRegionId().getValue(), currencies);
      }
      final List<ExternalId> result = new ArrayList<>();
      // both legs are InterestRateNotional, as otherwise getCurrencies() would have returned null
      result.addAll(createIds(SwapSecurity.SECURITY_TYPE, payLeg.getRegionId().getValue(),
          ((InterestRateNotional) payLeg.getNotional()).getCurrency().getCode()));
      result.addAll(createIds(SwapSecurity.SECURITY_TYPE, receiveLeg.getRegionId().getValue(),
          ((InterestRateNotional) receiveLeg.getNotional()).getCurrency().getCode()));
      return result;
    }

    @Override
    public List<ExternalId> visitSwaptionSecurity(final SwaptionSecurity security) {
      final SwapSecurity underlyingSwap = (SwapSecurity) _securitySource.getSingle(security.getUnderlyingId().toBundle());
      final SwapLeg payLeg = underlyingSwap.getPayLeg();
      final SwapLeg receiveLeg = underlyingSwap.getReceiveLeg();
      final Collection<Currency> currencies = FinancialSecurityUtils.getCurrencies(security, _securitySource);
      if (currencies == null) {
        return null;
      }
      if (payLeg.getRegionId().equals(receiveLeg.getRegionId())) {
        return createIds(SwaptionSecurity.SECURITY_TYPE, payLeg.getRegionId().getValue(), currencies);
      }
      final List<ExternalId> result = new ArrayList<>();
      // both legs are InterestRateNotional, as otherwise getCurrencies() would have returned null
      result.addAll(createIds(SwaptionSecurity.SECURITY_TYPE, payLeg.getRegionId().getValue(),
          ((InterestRateNotional) payLeg.getNotional()).getCurrency().getCode()));
      result.addAll(createIds(SwaptionSecurity.SECURITY_TYPE, receiveLeg.getRegionId().getValue(),
          ((InterestRateNotional) receiveLeg.getNotional()).getCurrency().getCode()));
      return result;
    }

  }
}
