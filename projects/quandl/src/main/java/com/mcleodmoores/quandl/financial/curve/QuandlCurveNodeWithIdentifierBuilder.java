/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.financial.curve;

import java.util.Map;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.quandl.future.QuandlFedFundsFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveNodeWithIdentifierBuilder;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * Constructs a {@link CurveNodeWithIdentifier} for a curve node and id mapper. If the node is a {@link RateFutureNode}
 * and the curve instrument provider is a {@link QuandlFedFundsFutureCurveInstrumentProvider}, uses the information about
 * the underlying and underlying id to construct a {@link QuandlCurveNodeWithIdentifierAndUnderlying}.
 */
public class QuandlCurveNodeWithIdentifierBuilder extends CurveNodeWithIdentifierBuilder {

  /**
   * Creates an instance.
   * @param curveDate  the curve date, not null
   * @param nodeIdMapper  the node id mapper, not null
   */
  public QuandlCurveNodeWithIdentifierBuilder(final LocalDate curveDate, final CurveNodeIdMapper nodeIdMapper) {
    super(curveDate, nodeIdMapper);
  }

  @Override
  public CurveNodeWithIdentifier visitRateFutureNode(final RateFutureNode node) {
    final Map<Tenor, CurveInstrumentProvider> ids = getCurveNodeIdMapper().getRateFutureNodeIds();
    final Tenor tenor = node.getStartTenor();
    if (ids.get(tenor) instanceof QuandlFedFundsFutureCurveInstrumentProvider) {
      final QuandlFedFundsFutureCurveInstrumentProvider provider = (QuandlFedFundsFutureCurveInstrumentProvider) ids.get(tenor);
      final Tenor startTenor = node.getStartTenor();
      final ExternalId identifier = getCurveNodeIdMapper().getRateFutureNodeId(getCurveDate(), startTenor, node.getFutureTenor(), node.getFutureNumber());
      final String dataField = getCurveNodeIdMapper().getRateFutureNodeDataField(startTenor);
      final DataFieldType fieldType = getCurveNodeIdMapper().getRateFutureNodeDataFieldType(startTenor);
      final ExternalId underlyingId = provider.getUnderlyingId();
      final String underlyingDataField = provider.getUnderlyingDataField();
      return new QuandlCurveNodeWithIdentifierAndUnderlying(node, identifier, dataField, fieldType, underlyingId, underlyingDataField);
    }
    final Tenor startTenor = node.getStartTenor();
    final ExternalId identifier = getCurveNodeIdMapper().getRateFutureNodeId(getCurveDate(), startTenor, node.getFutureTenor(), node.getFutureNumber());
    final String dataField = getCurveNodeIdMapper().getRateFutureNodeDataField(startTenor);
    final DataFieldType fieldType = getCurveNodeIdMapper().getRateFutureNodeDataFieldType(startTenor);
    return new CurveNodeWithIdentifier(node, identifier, dataField, fieldType);
  }
}
