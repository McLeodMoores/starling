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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Formatter for {@link FudgeMsg}.
 */
/*package*/ class FudgeMsgFormatter extends AbstractFormatter<FudgeMsg> {

  private static final Comparator<FudgeField> NAME_COMPARATOR;

  static {
    NAME_COMPARATOR = new Comparator<FudgeField>() {
      @Override
      public int compare(final FudgeField msg1, final FudgeField msg2) {
        return msg1.getName().compareTo(msg2.getName());
      }
    };

  }

  /*package*/ FudgeMsgFormatter() {
    super(FudgeMsg.class);
    addFormatter(new Formatter<FudgeMsg>(Format.EXPANDED) {
      @Override
      protected Map<String, Object> formatValue(final FudgeMsg msg, final ValueSpecification valueSpec, final Object inlineKey) {
        final int fieldCount = msg.getNumFields();
        final List<List<String>> matrix = Lists.newArrayListWithCapacity(fieldCount);
        final List<String> yLabels = Lists.newArrayListWithCapacity(fieldCount);
        // Sorting fields to ensure a consistent order for display purposes.
        // This could change the meaning of the Fudge message so assumes no repeated fields.
        final List<FudgeField> orderedFields = new ArrayList<>(msg.getAllFields());
        Collections.sort(orderedFields, NAME_COMPARATOR);
        for (final FudgeField field : orderedFields) {
          final List<String> row = Lists.newArrayListWithCapacity(2);
          row.add(field.getType().getJavaType().getSimpleName());
          String displayValue;
          if (field.getValue() == null) {
            displayValue = "";
          } else if (field.getValue() instanceof FudgeMsg) {
            displayValue = "Sub-message";
          } else {
            displayValue = field.getValue().toString();
          }
          row.add(displayValue);
          matrix.add(row);
          yLabels.add(field.getName());
        }
        final Map<String, Object> output = Maps.newHashMap();
        output.put(LabelledMatrix2DFormatter.MATRIX, matrix);
        output.put(LabelledMatrix2DFormatter.X_LABELS, Lists.newArrayList("Name", "Type", "Value"));
        output.put(LabelledMatrix2DFormatter.Y_LABELS, yLabels);
        return output;
      }
    });
  }

  @Override
  public Object formatCell(final FudgeMsg value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "Fudge Message (" + value.getAllFields().size() + " fields)";
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }

}
