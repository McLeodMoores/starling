/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.id.ExternalId;

/**
 * Converter for {@link LabelledMatrix1D} results.
 */
@SuppressWarnings("rawtypes")
public class LabelledMatrix1DConverter implements ResultConverter<LabelledMatrix1D> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final LabelledMatrix1D value,
      final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<>();
    final int length = value.getKeys().length;
    result.put("summary", length);

    if (mode == ConversionMode.FULL) {
      // Only interested in labels and values
      final Map<Object, Object> labelledValues = new LinkedHashMap<>();
      for (int i = 0; i < length; i++) {
        final Object labelObject = value.getLabels()[i];
        final String label = labelObject instanceof ExternalId ? ((ExternalId) labelObject).getValue() : labelObject.toString();
        final Object currentLabel = context.convert(label, ConversionMode.SUMMARY);
        final Object currentValue = context.getDoubleConverter().convertForDisplay(context, valueSpec, value.getValues()[i], ConversionMode.SUMMARY);
        labelledValues.put(currentLabel, currentValue);
      }
      result.put("full", labelledValues);
    }

    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final LabelledMatrix1D value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final LabelledMatrix1D value) {
    final StringBuilder sb = new StringBuilder();
    boolean isFirst = true;
    for (int i = 0; i < value.getKeys().length; i++) {
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append("; ").append(value.getValues()[i]);
      }
      final Object label = value.getLabels()[i];
      final Object currentLabel = context.convert(label, ConversionMode.SUMMARY);
      sb.append(currentLabel).append("=").append(value.getValues()[i]);
    }
    return sb.length() > 0 ? sb.toString() : null;
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_1D";
  }

}
