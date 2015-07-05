/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.analytics.formatting;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.id.ExternalIdOrderConfig;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/* package */ class YieldCurveDataFormatter extends AbstractFormatter<YieldCurveData> {

  private static final String DATA = "data";
  private static final String LABELS = "labels";
  private static final String ID = "ID";
  private static final String VALUE = "Value";

  private final ExternalIdOrderConfig _orderConfig;
  private final DoubleFormatter _doubleFormatter;

  /* package */ YieldCurveDataFormatter(final DoubleFormatter doubleFormatter) {
    this(ExternalIdOrderConfig.DEFAULT_CONFIG, doubleFormatter);
  }

  /* package */ YieldCurveDataFormatter(final ExternalIdOrderConfig config, final DoubleFormatter doubleFormatter) {
    super(YieldCurveData.class);
    ArgumentChecker.notNull(config, "config");
    ArgumentChecker.notNull(doubleFormatter, "doubleFormatter");
    _orderConfig = config;
    _doubleFormatter = doubleFormatter;
    addFormatter(new Formatter<YieldCurveData>(Format.EXPANDED) {
      @Override
      protected Map<String, Object> formatValue(final YieldCurveData value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(final YieldCurveData curveData, final ValueSpecification valueSpec, final Object inlineKey) {
    return "Data Bundle (" + curveData.getDataPoints().size() + " points)";
  }

  private Map<String, Object> formatExpanded(final YieldCurveData curveData, final ValueSpecification valueSpec) {
    final List<List<String>> results = Lists.newArrayListWithCapacity(curveData.getDataPoints().size());
    final Map<String, Object> resultsMap = Maps.newHashMap();
    for (final Map.Entry<ExternalIdBundle, Double> entry : curveData.getDataPoints().entrySet()) {
      final ExternalId id = _orderConfig.getPreferred(entry.getKey());
      final String idStr = id != null ? id.toString() : "";
      final String formattedValue = _doubleFormatter.formatCell(entry.getValue(), valueSpec, null);
      results.add(ImmutableList.of(idStr, formattedValue));
    }
    resultsMap.put(DATA, results);
    resultsMap.put(LABELS, ImmutableList.of(ID, VALUE));
    return resultsMap;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_1D;
  }
}
