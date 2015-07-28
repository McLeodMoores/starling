/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;
import com.opengamma.web.security.MinimalWebSecuritiesUris;
import com.opengamma.web.security.WebSecuritiesData;

/**
 * Abstract base class for RESTful position resources.
 */
public abstract class AbstractMinimalWebPositionResource extends AbstractPerRequestWebResource<WebPositionsData> {

  /**
   * Position XML parameter name.
   */
  protected static final String POSITION_XML = "positionXml";
  /**
   * HTML ftl directory.
   */
  protected static final String HTML_DIR = "positions/html/";
  /**
   * JSON ftl directory.
   */
  protected static final String JSON_DIR = "positions/json/";

  /**
   * Creates the resource.
   *
   * @param positionMaster  the position master, not null
   * @param securitySource  the security source, not null
   * @param externalSchemes the map of external schemes, with {@link ExternalScheme} as key and description as value
   */
  protected AbstractMinimalWebPositionResource(final PositionMaster positionMaster, final SecuritySource securitySource,
      final Map<ExternalScheme, String> externalSchemes) {
    super(new WebPositionsData());
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notEmpty(externalSchemes, "externalSchemes");
    data().setPositionMaster(positionMaster);
    data().setSecuritySource(securitySource);
    data().setExternalSchemes(externalSchemes);
  }

  /**
   * Creates the resource.
   *
   * @param positionMaster  the position master, not null
   * @param securityLoader  the security loader, not null
   * @param securitySource  the security source, not null
   * @param htsSource  the historical time series source, not null
   * @param externalSchemes the map of external schemes, with {@link ExternalScheme} as key and description as value
   */
  protected AbstractMinimalWebPositionResource(final PositionMaster positionMaster, final SecurityLoader securityLoader, final SecuritySource securitySource,
      final HistoricalTimeSeriesSource htsSource, final Map<ExternalScheme, String> externalSchemes) {
    super(new WebPositionsData());
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securityLoader, "securityLoader");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(htsSource, "htsSource");
    ArgumentChecker.notEmpty(externalSchemes, "externalSchemes");
    data().setPositionMaster(positionMaster);
    data().setSecurityLoader(securityLoader);
    data().setSecuritySource(securitySource);
    data().setHistoricalTimeSeriesSource(htsSource);
    data().setExternalSchemes(externalSchemes);
  }

  /**
   * Creates the resource.
   *
   * @param parent  the parent resource, not null
   */
  protected AbstractMinimalWebPositionResource(final AbstractMinimalWebPositionResource parent) {
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
    out.put("uris", new MinimalWebPositionsUris(data()));
    final WebSecuritiesData secData = new WebSecuritiesData(data().getUriInfo());
    out.put("securityUris", new MinimalWebSecuritiesUris(secData));
    out.put("externalSchemes", getExternalSchemes());
    return out;
  }

  private Map<String, String> getExternalSchemes() {
    final Map<String, String> result = new TreeMap<>();
    for (final Entry<ExternalScheme, String> entry : data().getExternalSchemes().entrySet()) {
      result.put(entry.getKey().getName(), entry.getValue());
    }
    return result;
  }

  //-------------------------------------------------------------------------
  protected Set<ManageableTrade> parseTrades(final String tradesJson) {
    return TradeJsonConverter.fromJson(tradesJson);
  }

  protected String getPositionXml(final ManageablePosition manageablePosition) {
    final ManageablePosition position = manageablePosition.clone();
    final ManageableSecurityLink securityLink = position.getSecurityLink();
    if (securityLink != null) {
      securityLink.setTarget(null);
    }
    for (final ManageableTrade manageableTrade : position.getTrades()) {
      final ManageableSecurityLink manageableSecurityLink = manageableTrade.getSecurityLink();
      if (manageableSecurityLink != null) {
        manageableSecurityLink.setTarget(null);
      }
    }
    return createBeanXML(position);
  }

}
