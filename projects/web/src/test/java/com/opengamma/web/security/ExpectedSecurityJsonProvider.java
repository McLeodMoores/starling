/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.google.common.collect.Maps;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.time.Expiry;

/**
 * Gets expected Json document for different security types
 */
/* package */ class ExpectedSecurityJsonProvider extends FinancialSecurityVisitorSameValueAdapter<JSONObject> {

  ExpectedSecurityJsonProvider() {
    super(null);
  }

  private static final String TEMPLATE_DATA = "template_data";

  @Override
  public JSONObject visitEquitySecurity(final EquitySecurity security) {
    final Map<String, Object> secMap = Maps.newHashMap();

    final Map<String, Object> templateData = Maps.newHashMap();
    addDefaultFields(security, templateData);

    if (StringUtils.isNotBlank(security.getShortName())) {
      templateData.put("shortName", security.getShortName());
    }
    if (StringUtils.isNotBlank(security.getExchange())) {
      templateData.put("exchange", security.getExchange());
    }
    if (security.getCurrency() != null && StringUtils.isNotBlank(security.getCurrency().getCode())) {
      templateData.put("currency", security.getCurrency().getCode());
    }
    if (StringUtils.isNotBlank(security.getCompanyName())) {
      templateData.put("companyName", security.getCompanyName());
    }
    if (StringUtils.isNotBlank(security.getExchangeCode())) {
      templateData.put("exchangeCode", security.getExchangeCode());
    }
    if (security.getGicsCode() != null && StringUtils.isNotBlank(security.getGicsCode().toString())) {
      templateData.put("gicsCode", security.getGicsCode().toString());
    }
    secMap.put(TEMPLATE_DATA, templateData);
    addSecurityXml(security, secMap);
    addExternalIds(security, secMap);
    return new JSONObject(secMap);
  }

  @Override
  public JSONObject visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitBondFutureSecurity(final BondFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitEquityFutureSecurity(final EquityFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitFXFutureSecurity(final FXFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitIndexFutureSecurity(final IndexFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitMetalFutureSecurity(final MetalFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  @Override
  public JSONObject visitStockFutureSecurity(final StockFutureSecurity security) {
    return visitFutureSecurity(security);
  }

  private static JSONObject visitFutureSecurity(final FutureSecurity security) {
    final JSONObject result = security.accept(new FinancialSecurityVisitorSameValueAdapter<JSONObject>(null) {

      @Override
      public JSONObject visitBondFutureSecurity(final BondFutureSecurity bondFutureSecurity) {
        final Map<String, Object> secMap = Maps.newHashMap();

        final Map<String, Object> templateData = Maps.newHashMap();
        addDefaultFields(bondFutureSecurity, templateData);
        addExpiry(templateData, bondFutureSecurity.getExpiry());
        templateData.put("firstDeliveryDate", bondFutureSecurity.getFirstDeliveryDate().toString());
        templateData.put("lastDeliveryDate", bondFutureSecurity.getLastDeliveryDate().toString());
        if (StringUtils.isNotBlank(bondFutureSecurity.getTradingExchange())) {
          templateData.put("tradingExchange", bondFutureSecurity.getTradingExchange());
        }
        if (StringUtils.isNotBlank(bondFutureSecurity.getSettlementExchange())) {
          templateData.put("settlementExchange", bondFutureSecurity.getSettlementExchange());
        }
        if (bondFutureSecurity.getCurrency() != null && StringUtils.isNotBlank(bondFutureSecurity.getCurrency().getCode())) {
          templateData.put("currency", bondFutureSecurity.getCurrency().getCode());
        }
        final List<BondFutureDeliverable> basket = bondFutureSecurity.getBasket();
        if (!basket.isEmpty()) {
          final Map<String, String> underlyingBond = Maps.newHashMap();
          for (final BondFutureDeliverable bondFutureDeliverable : basket) {
            underlyingBond.put(
                ExternalSchemes.BLOOMBERG_TICKER.getName() + "-" + bondFutureDeliverable.getIdentifiers().getValue(ExternalSchemes.BLOOMBERG_TICKER),
                String.valueOf(bondFutureDeliverable.getConversionFactor()));
          }
          templateData.put("underlyingBond", underlyingBond);
        }
        templateData.put("unitAmount", bondFutureSecurity.getUnitAmount());
        secMap.put(TEMPLATE_DATA, templateData);
        addSecurityXml(bondFutureSecurity, secMap);
        addExternalIds(bondFutureSecurity, secMap);
        return new JSONObject(secMap);
      }
    });
    return result;
  }

  private static void addSecurityXml(final FinancialSecurity security, final Map<String, Object> secMap) {
    final String secXml = JodaBeanSerialization.serializer(true).xmlWriter().write(security, true);
    secMap.put("securityXml", secXml);
  }

  private static void addExternalIds(final FinancialSecurity security, final Map<String, Object> secMap) {
    final Map<String, String> identifiers = Maps.newHashMap();
    final ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
    if (externalIdBundle.getExternalId(ExternalSchemes.BLOOMBERG_BUID) != null) {
      identifiers.put(ExternalSchemes.BLOOMBERG_BUID.getName(),
          ExternalSchemes.BLOOMBERG_BUID.getName() + "-" + externalIdBundle.getValue(ExternalSchemes.BLOOMBERG_BUID));
    }
    if (externalIdBundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER) != null) {
      identifiers.put(ExternalSchemes.BLOOMBERG_TICKER.getName(),
          ExternalSchemes.BLOOMBERG_TICKER.getName() + "-" + externalIdBundle.getValue(ExternalSchemes.BLOOMBERG_TICKER));
    }
    if (externalIdBundle.getExternalId(ExternalSchemes.CUSIP) != null) {
      identifiers.put(ExternalSchemes.CUSIP.getName(), ExternalSchemes.CUSIP.getName() + "-" + externalIdBundle.getValue(ExternalSchemes.CUSIP));
    }
    if (externalIdBundle.getExternalId(ExternalSchemes.ISIN) != null) {
      identifiers.put(ExternalSchemes.ISIN.getName(), ExternalSchemes.ISIN.getName() + "-" + externalIdBundle.getValue(ExternalSchemes.ISIN));
    }
    if (externalIdBundle.getExternalId(ExternalSchemes.SEDOL1) != null) {
      identifiers.put(ExternalSchemes.SEDOL1.getName(), ExternalSchemes.SEDOL1.getName() + "-" + externalIdBundle.getValue(ExternalSchemes.SEDOL1));
    }
    secMap.put("identifiers", identifiers);
  }

  private static void addDefaultFields(final FinancialSecurity security, final Map<String, Object> templateData) {
    if (StringUtils.isNotBlank(security.getName())) {
      templateData.put("name", security.getName());
    }
    if (StringUtils.isNotBlank(security.getSecurityType())) {
      templateData.put("securityType", security.getSecurityType());
    }
    if (security.getUniqueId() != null && security.getUniqueId().getObjectId() != null
        && StringUtils.isNotBlank(security.getUniqueId().getObjectId().toString())) {
      templateData.put("object_id", security.getUniqueId().getObjectId().toString());
    }
    if (security.getUniqueId() != null && StringUtils.isNotBlank(security.getUniqueId().getVersion())) {
      templateData.put("version_id", security.getUniqueId().getVersion());
    }
    if (security.getAttributes() != null && !security.getAttributes().isEmpty()) {
      templateData.put("attributes", security.getAttributes());
    }
  }

  private static void addExpiry(final Map<String, Object> templateData, final Expiry expiry) {
    final Map<String, Object> expiryDateMap = Maps.newHashMap();
    expiryDateMap.put("datetime", expiry.getExpiry().toOffsetDateTime().toString());
    expiryDateMap.put("timezone", expiry.getExpiry().getZone().toString());
    templateData.put("expiryAccuracy", expiry.getAccuracy().toString().replace("_", " "));
    templateData.put("expirydate", expiryDateMap);
  }

}
