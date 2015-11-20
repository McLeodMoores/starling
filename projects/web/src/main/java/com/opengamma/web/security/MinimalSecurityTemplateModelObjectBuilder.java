/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexComponent;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FixedVarianceSwapLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.FloatingVarianceSwapLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.time.Tenor;

/**
 * Builds the model object used in the security freemarker templates.
 */
public class MinimalSecurityTemplateModelObjectBuilder extends FinancialSecurityVisitorSameValueAdapter<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MinimalSecurityTemplateModelObjectBuilder.class);

  private final FlexiBean _out;
  private final SecurityMaster _securityMaster;
  private final LegalEntityMaster _legalEntityMaster;

  public MinimalSecurityTemplateModelObjectBuilder(final FlexiBean out, final SecurityMaster securityMaster) {
    this(out, securityMaster, null);
  }

  public MinimalSecurityTemplateModelObjectBuilder(final FlexiBean out, final SecurityMaster securityMaster,
      final LegalEntityMaster legalEntityMaster) {
    super(null);
    _out = out;
    _securityMaster = securityMaster;
    _legalEntityMaster = legalEntityMaster;
  }

  private void addFutureSecurityType(final String futureType) {
    _out.put("futureSecurityType", futureType);
  }

  private void addUnderlyingSecurity(final ExternalId underlyingId) {
    final ManageableSecurity security = getSecurity(underlyingId);
    if (security != null) {
      _out.put("underlyingSecurity", security);
    }
  }

  private ManageableSecurity getSecurity(final ExternalId underlyingIdentifier) {
    return AbstractMinimalWebSecurityResource.getSecurity(underlyingIdentifier, _securityMaster);
  }

  @Override
  public Void visitSwapSecurity(final SwapSecurity security) {
    _out.put("payLegType", security.getPayLeg().accept(new SwapLegClassifierVisitor()));
    _out.put("receiveLegType", security.getReceiveLeg().accept(new SwapLegClassifierVisitor()));
    return null;
  }

  @Override
  public Void visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    addFutureSecurityType("InterestRate");
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitBondFutureSecurity(final BondFutureSecurity security) {
    addFutureSecurityType("BondFuture");
    final Map<String, String> basket = new TreeMap<String, String>();
    for (final BondFutureDeliverable bondFutureDeliverable : security.getBasket()) {
      final String identifierValue = bondFutureDeliverable.getIdentifiers().getValue(ExternalSchemes.BLOOMBERG_TICKER);
      basket.put(ExternalSchemes.BLOOMBERG_TICKER.getName() + "-" + identifierValue, String.valueOf(bondFutureDeliverable.getConversionFactor()));
    }
    _out.put("basket", basket);
    return null;
  }

  @Override
  public Void visitCapFloorSecurity(final CapFloorSecurity security) {
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
    final Security shortUnderlying = getSecurity(security.getShortId());
    final Security longUnderlying = getSecurity(security.getLongId());
    if (shortUnderlying != null) {
      _out.put("shortSecurity", shortUnderlying);
    }
    if (longUnderlying != null) {
      _out.put("longSecurity", longUnderlying);
    }
    return null;
  }

  @Override
  public Void visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    addFutureSecurityType("EnergyFuture");
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitEquityFutureSecurity(final EquityFutureSecurity security) {
    addFutureSecurityType("EquityFuture");
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    addFutureSecurityType("EquityIndexDividendFuture");
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitEquityOptionSecurity(final EquityOptionSecurity security) {
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitFRASecurity(final FRASecurity security) {
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitFXFutureSecurity(final FXFutureSecurity security) {
    addFutureSecurityType("FxFuture");
    return null;
  }

  @Override
  public Void visitIndexFutureSecurity(final IndexFutureSecurity security) {
    addFutureSecurityType("IndexFuture");
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitMetalFutureSecurity(final MetalFutureSecurity security) {
    addFutureSecurityType("MetalFuture");
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitStockFutureSecurity(final StockFutureSecurity security) {
    addFutureSecurityType("StockFuture");
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitSwaptionSecurity(final SwaptionSecurity security) {
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    addFutureSecurityType("AgricultureFuture");
    return null;
  }

  @Override
  public Void visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
    addFutureSecurityType("DeliverableSwapFuture");
    return null;
  }

  @Override
  public Void visitCreditDefaultSwapIndexDefinitionSecurity(final CreditDefaultSwapIndexDefinitionSecurity security) {
    final List<String> tenors = Lists.newArrayList();
    for (final Tenor tenor : security.getTerms()) {
      tenors.add(tenor.getPeriod().toString());
    }
    _out.put("terms", ImmutableList.copyOf(tenors));
    final Set<CreditDefaultSwapIndexComponent> components = new TreeSet<>(Collections.reverseOrder());
    for (final CreditDefaultSwapIndexComponent component : security.getComponents()) {
      components.add(component);
    }
    _out.put("components", ImmutableList.copyOf(components));
    return null;
  }

  @Override
  public Void visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    final ExternalId underlyingId = security.getUnderlyingId();
    if (underlyingId != null) {
      if (_legalEntityMaster == null) {
        LOGGER.warn("Legal entity master not available: cannot get the underlying security for {}", security);
      } else {
        final LegalEntitySearchRequest request = new LegalEntitySearchRequest();
        if (underlyingId.getScheme().equals(ExternalSchemes.MARKIT_RED_CODE)) {
          request.addExternalId(underlyingId);
          final LegalEntitySearchResult searchResult = _legalEntityMaster.search(request);
          final LegalEntity legalEntity = searchResult.getSingleLegalEntity();
          if (legalEntity != null) {
            _out.put("underlyingLegalEntity", legalEntity);
          }
        } else {
          LOGGER.warn("{} does not currently support CDSOption underlying lookup based on {}", MinimalWebSecuritiesResource.class,
              underlyingId.getScheme().getName());
        }
      }
    }
    return null;
  }

  @Override
  public Void visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
    addFutureSecurityType("FederalFundsFutureSecurity");
    addUnderlyingSecurity(security.getUnderlyingId());
    return null;
  }

  @Override
  public Void visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
    _out.put("payLegType", security.getPayLeg().accept(new SwapLegClassifierVisitor()));
    _out.put("receiveLegType", security.getReceiveLeg().accept(new SwapLegClassifierVisitor()));
    return null;
  }

  @Override
  public Void visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
    _out.put("payLegType", security.getPayLeg().accept(new SwapLegClassifierVisitor()));
    _out.put("receiveLegType", security.getReceiveLeg().accept(new SwapLegClassifierVisitor()));
    return null;
  }

  /**
   * SwapLegClassifierVisitor
   */
  private static class SwapLegClassifierVisitor implements SwapLegVisitor<String> {
    @Override
    public String visitFixedInterestRateLeg(final FixedInterestRateLeg swapLeg) {
      return "FixedInterestRateLeg";
    }

    @Override
    public String visitFloatingInterestRateLeg(final FloatingInterestRateLeg swapLeg) {
      return "FloatingInterestRateLeg";
    }

    @Override
    public String visitFloatingSpreadIRLeg(final FloatingSpreadIRLeg swapLeg) {
      return "FloatingSpreadInterestRateLeg";
    }

    @Override
    public String visitFloatingGearingIRLeg(final FloatingGearingIRLeg swapLeg) {
      return "FloatingGearingInterestRateLeg";
    }

    @Override
    public String visitFixedVarianceSwapLeg(final FixedVarianceSwapLeg swapLeg) {
      return "FixedVarianceLeg";
    }

    @Override
    public String visitFloatingVarianceSwapLeg(final FloatingVarianceSwapLeg swapLeg) {
      return "FloatingVarianceLeg";
    }

    @Override
    public String visitFixedInflationSwapLeg(final FixedInflationSwapLeg swapLeg) {
      return "FixedInflationLeg";
    }

    @Override
    public String visitInflationIndexSwapLeg(final InflationIndexSwapLeg swapLeg) {
      return "InflationIndexLeg";
    }
  }
}
