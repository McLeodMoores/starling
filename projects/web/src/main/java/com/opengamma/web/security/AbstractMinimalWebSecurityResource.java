/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful security resources.
 */
public abstract class AbstractMinimalWebSecurityResource extends AbstractPerRequestWebResource<WebSecuritiesData> {
  /**
   * Security XML parameter name.
   */
  protected static final String SECURITY_XML = "securityXml";
  /**
   * HTML ftl directory.
   */
  protected static final String HTML_DIR = "securities/html/";
  /**
   * JSON ftl directory.
   */
  protected static final String JSON_DIR = "securities/json/";
  /**
   * The template name provider
   */
  private final SecurityTemplateNameProvider _templateNameProvider = new SecurityTemplateNameProvider();

  /**
   * Creates the resource.
   *
   * @param securityMaster  the security master, not null
   */
  protected AbstractMinimalWebSecurityResource(final SecurityMaster securityMaster) {
    super(new WebSecuritiesData());
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    data().setSecurityMaster(securityMaster);
    data().setSecurityTypes(SecurityTypesDescriptionProvider.getInstance().getDescription2Type());
  }

  /**
   * Creates the resource.
   *
   * @param securityMaster  the security master, not null
   * @param securityLoader  the security loader, not null
   */
  protected AbstractMinimalWebSecurityResource(final SecurityMaster securityMaster, final SecurityLoader securityLoader) {
    super(new WebSecuritiesData());
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(securityLoader, "securityLoader");
    data().setSecurityMaster(securityMaster);
    data().setSecurityLoader(securityLoader);
    data().setSecurityTypes(SecurityTypesDescriptionProvider.getInstance().getDescription2Type());
  }

  /**
   * Creates the resource.
   *
   * @param securityMaster  the security master, not null
   * @param securityLoader  the security loader, not null
   * @param htsMaster  the historical time series master
   * @param legalEntityMaster the organization master
   */
  protected AbstractMinimalWebSecurityResource(final SecurityMaster securityMaster, final SecurityLoader securityLoader,
      final HistoricalTimeSeriesMaster htsMaster, final LegalEntityMaster legalEntityMaster) {
    super(new WebSecuritiesData());
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    ArgumentChecker.notNull(securityLoader, "securityLoader");
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(legalEntityMaster, "legalEntityMaster");
    data().setSecurityMaster(securityMaster);
    data().setSecurityLoader(securityLoader);
    data().setHistoricalTimeSeriesMaster(htsMaster);
    data().setLegalEntityMaster(legalEntityMaster);
    data().setSecurityTypes(SecurityTypesDescriptionProvider.getInstance().getDescription2Type());
  }

  /**
   * Creates the resource.
   *
   * @param parent  the parent resource, not null
   */
  protected AbstractMinimalWebSecurityResource(final AbstractMinimalWebSecurityResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    out.put("uris", new MinimalWebSecuritiesUris(data()));
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security template provider.
   *
   * @return the template provider, not null
   */
  protected SecurityTemplateNameProvider getTemplateProvider() {
    return _templateNameProvider;
  }

  protected void addSecuritySpecificMetaData(final ManageableSecurity security, final FlexiBean out) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity financialSec = (FinancialSecurity) security;
      if (data().getLegalEntityMaster() != null) {
        financialSec.accept(new MinimalSecurityTemplateModelObjectBuilder(out, data().getSecurityMaster(), data().getLegalEntityMaster()));
      } else {
        financialSec.accept(new MinimalSecurityTemplateModelObjectBuilder(out, data().getSecurityMaster()));
      }
    }
  }

  public static ManageableSecurity getSecurity(final ExternalId underlyingIdentifier, final SecurityMaster securityMaster) {
    if (underlyingIdentifier == null) {
      return null;
    }
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(underlyingIdentifier);
    final SecuritySearchResult search = securityMaster.search(request);
    return search.getFirstSecurity();
  }
}
