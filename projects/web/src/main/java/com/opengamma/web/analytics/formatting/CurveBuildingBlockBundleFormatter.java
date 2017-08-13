/**
 *
 */
package com.opengamma.web.analytics.formatting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class CurveBuildingBlockBundleFormatter extends AbstractFormatter<CurveBuildingBlockBundle> {
  private static final String X_LABELS = "xLabels";
  private static final String Y_LABELS = "yLabels";
  private static final String MATRIX = "matrix";

  private final DoubleFormatter _doubleFormatter;

  /**
   * @param doubleFormatter  formats the discount factors
   */
  /* package */ CurveBuildingBlockBundleFormatter(final DoubleFormatter doubleFormatter) {
    super(CurveBuildingBlockBundle.class);
    _doubleFormatter = doubleFormatter;
    addFormatter(new Formatter<CurveBuildingBlockBundle>(Format.EXPANDED) {
      @Override
      protected Object formatValue(final CurveBuildingBlockBundle value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public Object formatCell(final CurveBuildingBlockBundle value, final ValueSpecification valueSpec, final Object inlineKey) {
    return "Jacobian";
  }

  /**
   * Transforms the details object to an amount that can be displayed.
   * @param value  the FX forward details
   * @param valueSpec  the value specification
   * @return  the data
   */
  /* package */ Map<String, Object> formatExpanded(final CurveBuildingBlockBundle value, final ValueSpecification valueSpec) {
    final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> data = value.getData();
    int rows = 0;
    int columns = 0;
    for (final Map.Entry<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> entry : data.entrySet()) {
      final LinkedHashMap<String, Pair<Integer, Integer>> temp = entry.getValue().getFirst().getData();
      rows += entry.getValue().getSecond().getNumberOfRows();
      columns += entry.getValue().getSecond().getNumberOfColumns();
    }
    rows *= data.size();
    columns *= data.size();
    final String[] xLabels = new String[rows];
    final String[] yLabels = new String[columns];
    Arrays.fill(xLabels, "");
    Arrays.fill(yLabels, "");
    for (final Map.Entry<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> entry1 : data.entrySet()) {
      final LinkedHashMap<String, Pair<Integer, Integer>> block = entry1.getValue().getFirst().getData();
      for (final Map.Entry<String, Pair<Integer, Integer>> entry2 : block.entrySet()) {
        xLabels[entry2.getValue().getFirst()] = entry2.getKey();
        yLabels[entry2.getValue().getFirst()] = entry2.getKey();
      }
    }
    final Map<String, Object> results = new HashMap<>();
    final Object[][] values = new Object[rows][columns];

    results.put(MATRIX, values);
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }

}
