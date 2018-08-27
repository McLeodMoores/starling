/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Visitor that returns the value of a security's underlying ID.
 */
public class UnderlyingIdVisitor extends FinancialSecurityVisitorAdapter<String> {

  /**
   * Code to use for not applicable.
   */
  public static final String NOT_APPLICABLE = "N/A";

  private final ExternalScheme _preferredScheme;
  private final SecuritySource _securitySource;

  /**
   * Creates an instance.
   *
   * @param preferredScheme  the preferred scheme, not null
   * @param securitySource  the security source, not null
   */
  public UnderlyingIdVisitor(final ExternalScheme preferredScheme, final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "secSource");
    ArgumentChecker.notNull(preferredScheme, "preferredScheme");
    _preferredScheme = preferredScheme;
    _securitySource = securitySource;
  }

  //-------------------------------------------------------------------------
  @Override
  public String visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    if (security.getUnderlyingId().isScheme(_preferredScheme)) {
      final String identifier = security.getUnderlyingId().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    } else {
      final Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      if (underlying != null) {
        final String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
        return identifier != null ? identifier : NOT_APPLICABLE;
      } else {
        final String identifier = security.getUnderlyingId() != null ? security.getUnderlyingId().getValue() : null;
        return identifier != null ? identifier : NOT_APPLICABLE;
      }
    }
  }

  @Override
  public String visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    final String identifier = security.getUnderlyingId().getValue();
    return identifier != null ? identifier : NOT_APPLICABLE;
  }

  @Override
  public String visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    final String identifier = security.getUnderlyingId().getValue();
    return identifier != null ? identifier : NOT_APPLICABLE;
  }

  @Override
  public String visitEquityOptionSecurity(final EquityOptionSecurity security) {
    if (security.getUnderlyingId().isScheme(_preferredScheme)) {
      final String identifier = security.getUnderlyingId().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    } else {
      final Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      if (underlying != null) {
        final String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
        return identifier != null ? identifier : NOT_APPLICABLE;
      } else {
        final String identifier = security.getUnderlyingId() != null ? security.getUnderlyingId().getValue() : null;
        return identifier != null ? identifier : NOT_APPLICABLE;
      }
    }
  }

  @Override
  public String visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    if (security.getUnderlyingId().isScheme(_preferredScheme)) {
      final String identifier = security.getUnderlyingId().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    } else {
      final Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
      if (underlying != null) {
        final String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
        return identifier != null ? identifier : NOT_APPLICABLE;
      } else {
        final String identifier = security.getUnderlyingId() != null ? security.getUnderlyingId().getValue() : null;
        return identifier != null ? identifier : NOT_APPLICABLE;
      }
    }
  }

  @Override
  public String visitFXOptionSecurity(final FXOptionSecurity fxOptionSecurity) {
    final UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(),
                                                                   fxOptionSecurity.getPutCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitEquitySecurity(final EquitySecurity equitySecurity) {
    final String ticker = equitySecurity.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    final String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitMetalFutureSecurity(final MetalFutureSecurity security) {
    final String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitBondFutureSecurity(final BondFutureSecurity security) {
    final String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    final String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
    final String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitEquityFutureSecurity(final EquityFutureSecurity security) {
    final String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    final String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitFXFutureSecurity(final FXFutureSecurity security) {
    final String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitIndexFutureSecurity(final IndexFutureSecurity security) {
    final String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    final String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitStockFutureSecurity(final StockFutureSecurity security) {
    final String ticker = security.getExternalIdBundle().getValue(_preferredScheme);
    return ticker != null ? ticker : NOT_APPLICABLE;
  }

  @Override
  public String visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity fxOptionSecurity) {
    final UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(),
                                                                   fxOptionSecurity.getPutCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity fxOptionSecurity) {
    final UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(),
                                                                   fxOptionSecurity.getPutCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity fxOptionSecurity) {
    final UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxOptionSecurity.getCallCurrency(),
                                                                   fxOptionSecurity.getPutCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity fxBarrierOptionSecurity) {
    final UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxBarrierOptionSecurity.getCallCurrency(),
                                                                   fxBarrierOptionSecurity.getPutCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitFXForwardSecurity(final FXForwardSecurity fxForwardSecurity) {
    final UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(fxForwardSecurity.getPayCurrency(),
                                                                   fxForwardSecurity.getReceiveCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity ndfFxForwardSecurity) {
    final UnorderedCurrencyPair unorderedPair = UnorderedCurrencyPair.of(ndfFxForwardSecurity.getPayCurrency(),
                                                                   ndfFxForwardSecurity.getReceiveCurrency());
    return unorderedPair.getFirstCurrency() + "/" + unorderedPair.getSecondCurrency();
  }

  @Override
  public String visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    if (security.getUnderlyingId().isScheme(_preferredScheme)) {
      final String identifier = security.getUnderlyingId().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    }
    final Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    final String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
    return identifier != null ? identifier : NOT_APPLICABLE;
  }

  @Override
  public String visitSwaptionSecurity(final SwaptionSecurity security) {
    final SwapSecurity underlying = (SwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    final String name = underlying.getName();
    return name != null && name.length() > 0 ? name : NOT_APPLICABLE;
  }

  @Override
  public String visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    if (security.getReferenceEntity().isScheme(_preferredScheme)) {
      final String identifier = security.getReferenceEntity().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    }
    final Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getReferenceEntity()));
    final String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
    return identifier != null ? identifier : NOT_APPLICABLE;
  }

  @Override
  public String visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    if (security.getUnderlyingId().isScheme(_preferredScheme)) {
      final String identifier = security.getUnderlyingId().getValue();
      return identifier != null ? identifier : NOT_APPLICABLE;
    }
    final Security underlying = _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    final String identifier = underlying.getExternalIdBundle().getValue(_preferredScheme);
    return identifier != null ? identifier : NOT_APPLICABLE;
  }

}
