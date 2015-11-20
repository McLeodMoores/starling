/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Formats {@link ValueProperties} as a grid with 3 columns: Property name, property values (comma separated) and
 * whether the property is optional.
 */
/* package */ class ValuePropertiesFormatter extends AbstractFormatter<ValueProperties> {

  /* package */ ValuePropertiesFormatter() {
    super(ValueProperties.class);
    addFormatter(new Formatter<ValueProperties>(Format.EXPANDED) {
      @Override
      protected Map<String, Object> formatValue(final ValueProperties properties, final ValueSpecification valueSpec, final Object inlineKey) {
        final Set<String> names = properties.getProperties();
        final List<List<String>> matrix = Lists.newArrayListWithCapacity(names.size());
        final List<String> yLabels = Lists.newArrayListWithCapacity(names.size());
        for (final String name : names) {
          final Set<String> values = properties.getValues(name);
          final boolean optional = properties.isOptional(name);
          final List<String> row = Lists.newArrayListWithCapacity(2);
          row.add(StringUtils.join(values, ", "));
          row.add(optional ? "true" : "false");
          matrix.add(row);
          yLabels.add(name);
        }
        final Map<String, Object> output = Maps.newHashMap();
        output.put(LabelledMatrix2DFormatter.MATRIX, matrix);
        // TODO it would be good if the UI could handle a label for the first column: "Property"
        output.put(LabelledMatrix2DFormatter.X_LABELS, Lists.newArrayList("Property", "Value", "Optional"));
        output.put(LabelledMatrix2DFormatter.Y_LABELS, yLabels);
        return output;
      }
    });
  }

  @Override
  public Object formatCell(final ValueProperties properties, final ValueSpecification valueSpec, final Object inlineKey) {
    return "Value Properties (" + properties.getProperties().size() + ")";
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }
}
