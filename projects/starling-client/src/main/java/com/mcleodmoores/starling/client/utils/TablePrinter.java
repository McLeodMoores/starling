package com.mcleodmoores.starling.client.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.convert.StringConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.starling.client.marketdata.MarketDataKey;
import com.mcleodmoores.starling.client.marketdata.MarketDataSet;
import com.mcleodmoores.starling.client.results.MarketDataTargetKey;
import com.mcleodmoores.starling.client.results.PortfolioNodeTargetKey;
import com.mcleodmoores.starling.client.results.PositionTargetKey;
import com.mcleodmoores.starling.client.results.PrimitiveTargetKey;
import com.mcleodmoores.starling.client.results.ResultKey;
import com.mcleodmoores.starling.client.results.ResultModel;
import com.mcleodmoores.starling.client.results.ResultModelImpl;
import com.mcleodmoores.starling.client.results.TargetKey;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Created by jim on 13/05/15.
 */
public class TablePrinter {
  private static final Logger LOGGER = LoggerFactory.getLogger(TablePrinter.class);
  private static final String NULL = "null";
  private static final int NULL_SIZE = NULL.length();
  private static final String T = "\u251c\u2500";
  private static final String L = "\u2514\u2500";
  private static final String I = "\u2502 ";

  public static String toPrettyPrintedString(final Portfolio portfolio) {
    final StringBuilder sb = new StringBuilder();
    sb.append(portfolio.getName());
    sb.append("\n");
    buildPrettyPrintedString(sb, "  ", portfolio.getRootNode(), true);
    return sb.toString();
  }

  private static void buildPrettyPrintedString(final StringBuilder output, final String indent, final PortfolioNode portfolioNode, final boolean last) {
    String newIndent = indent;
    output.append(indent);
    if (last) {
      output.append(L);
      newIndent += "  ";
    } else {
      output.append(T);
      newIndent += T;
    }

    output.append(portfolioNode.getName().isEmpty() ? "<no name>" : portfolioNode.getName());
    output.append("\n");

    final Iterator<Position> posIter = portfolioNode.getPositions().iterator();
    while (posIter.hasNext()) {
      buildPrettyPrintedString(output, newIndent, posIter.next(), !posIter.hasNext());
    }
    final Iterator<PortfolioNode> nodeIter = portfolioNode.getChildNodes().iterator();
    while (nodeIter.hasNext()) {
      buildPrettyPrintedString(output, newIndent, nodeIter.next(), !nodeIter.hasNext());
    }
  }

  private static void buildPrettyPrintedString(final StringBuilder output, final String indent, final Position position, final boolean last) {
    String newIndent = indent;
    output.append(indent);
    if (last) {
      output.append(L);
      newIndent += "  ";
    } else {
      output.append(T);
      newIndent += I;
    }
    output.append(" ");
    output.append(position.getQuantity());
    output.append(" x ");
    output.append(position.getSecurity().getName());
    output.append("\n");
  }

  public static String toPrettyPrintedString(final MarketDataSet dataSet) {
    final Set<MarketDataKey> marketDataKeys = dataSet.keySet();
    final String[][] table = new String[marketDataKeys.size() + 1][MarketDataKey.meta().metaPropertyCount() + 1];
    int i = 0;
    int j = 0;
    for (final MetaProperty metaProperty : MarketDataKey.meta().metaPropertyIterable()) {
      table[i][j++] = metaProperty.name();
    }
    table[i][j] = "Value";
    i++;
    for (final MarketDataKey key : marketDataKeys) {
      j = 0;
      for (final MetaProperty metaProperty : MarketDataKey.meta().metaPropertyIterable()) {
        table[i][j++] = key.property(metaProperty.name()).get().toString();
      }
      final Object value = dataSet.get(key);
      if (value != null) {
        table[i][j] = value.toString();
      }
      i++;
    }
    return toPrettyPrintedString(table);
  }

  private static String[][] tabulatePortfolioResult(final ResultModel resultModel, final boolean includeProperties) {
    final StringConvert converter = JodaBeanUtils.stringConverter();
    final Set<ResultKey> portfolioResultKeys = resultModel.getRequestedPortfolioResultKeys();
    final List<TargetKey> portfolioTargetKeys = resultModel.getTargetKeys(EnumSet.of(ResultModelImpl.TargetType.PORTFOLIO_NODE, ResultModelImpl.TargetType.POSITION));
    final String[][] table = new String[portfolioTargetKeys.size() + 1][portfolioResultKeys.size() + 1];
    int column = 1;
    int row = 0;
    table[0][0] = "Node/Position";
    // do header row.
    for (final ResultKey key : portfolioResultKeys) {
      String columnLabel;
      if (!key.isDefaultColumnSet()) {
        columnLabel = key.getColumnSet() + "/" + key.getResultType().getValueRequirementName();
      } else {
        columnLabel = key.getResultType().getValueRequirementName();
      }
      if (includeProperties) {
        columnLabel = columnLabel + "[" + key.getResultType().getProperties().toSimpleString() + "]";
      }
      table[0][column++] = columnLabel;
    }
    row++;
    // do portfolio entries
    for (final TargetKey targetKey : portfolioTargetKeys) {
      if (targetKey instanceof PortfolioNodeTargetKey) {
        final PortfolioNodeTargetKey portfolioNodeTargetKey = (PortfolioNodeTargetKey) targetKey;
        table[row][0] = portfolioNodeTargetKey.getNodePath();
      } else if (targetKey instanceof PositionTargetKey) {
        final PositionTargetKey positionTargetKey = (PositionTargetKey) targetKey;
        table[row][0] = positionTargetKey.getCorrelationId().toString();
      }
      final Map<ResultKey, ComputedValueResult> resultsForTarget = resultModel.getResultsForTarget(targetKey);
      int col = 1;
      for (final ResultKey portfolioResultKey : portfolioResultKeys) {
        if (resultsForTarget != null) {
          final ComputedValueResult result = resultsForTarget.get(portfolioResultKey);
          if (result != null) {
            final Object value = result.getValue();
            try {
              table[row][col] = converter.convertToString(value);
            } catch (final IllegalStateException ise) {
              // no converter, use toString()
              table[row][col] = value.toString();
            }
          } else {
            LOGGER.error("Result key {} returned null when the API should have returned a value.", portfolioResultKey);
            LOGGER.error("Result keys available were {}", resultsForTarget.keySet());
            table[row][col] = null;
          }
        } else {
          LOGGER.error("No results for target {}.", resultsForTarget);
          table[row][col] = null;
        }
        col++;
      }
      row++;
    }
    return table;
  }

  private static List<String[][]> tabulateMarketDataAndSpecificRequirementResult(final ResultModel resultModel, final boolean includeProperties) {
    final StringConvert converter = JodaBeanUtils.stringConverter();
    final List<String[][]> tables = new ArrayList<>();
    final List<TargetKey> marketDataTargetKeys = resultModel.getTargetKeys(EnumSet.of(ResultModelImpl.TargetType.MARKET_DATA));
    for (final TargetKey targetKey : marketDataTargetKeys) {
      if (targetKey instanceof MarketDataTargetKey) {
        final MarketDataTargetKey marketDataTargetKey = (MarketDataTargetKey) targetKey;
        final Set<ResultKey> resultKeys = resultModel.getRequestedMarketDataResultKeys();
        final Map<ResultKey, ComputedValueResult> mktResults = resultModel.getResultsForTarget(marketDataTargetKey);
        final String[][] marketDataTable = new String[resultKeys.size() + 1][2];
        marketDataTable[0][0] = "Result Key";
        marketDataTable[0][1] = "Value";
        int mktRow = 1;
        for (final ResultKey resultKey : resultKeys) {
          String rowLabel;
          if (!resultKey.isDefaultColumnSet()) {
            rowLabel = resultKey.getColumnSet() + "/" + resultKey.getResultType().getValueRequirementName();
          } else {
            rowLabel = resultKey.getResultType().getValueRequirementName();
          }
          if (includeProperties) {
            rowLabel = rowLabel + "[" + resultKey.getResultType().getProperties().toSimpleString() + "]";
          }
          marketDataTable[mktRow][0] = rowLabel;
          final ComputedValueResult res = mktResults.get(resultKey);
          if (res != null) {
            try {
              marketDataTable[mktRow][1] = converter.convertToString(res.getValue());
            } catch (final IllegalStateException ise) {
              // no converter, use toString()
              marketDataTable[mktRow][1] = res.getValue().toString();
            }
          } else {
            LOGGER.error("Result key {} returned null when the API should have returned a value.", resultKeys);
            LOGGER.error("Result keys available were {}", mktResults.keySet());
            marketDataTable[mktRow][1] = null;
          }
          mktRow++;
        }
        tables.add(marketDataTable);
      }
    }
    final List<TargetKey> primitiveTargetKeys = resultModel.getTargetKeys(EnumSet.of(ResultModelImpl.TargetType.PRIMITIVE));
    for (final TargetKey targetKey : primitiveTargetKeys) {
      if (targetKey instanceof PrimitiveTargetKey) {
        final PrimitiveTargetKey primitiveTargetKey = (PrimitiveTargetKey) targetKey;
        final Set<ResultKey> resultKeys = resultModel.getRequestedMarketDataResultKeys();
        final Map<ResultKey, ComputedValueResult> mktResults = resultModel.getResultsForTarget(primitiveTargetKey);
        final String[][] marketDataTable = new String[resultKeys.size() + 1][2];
        marketDataTable[0][0] = "Result Key";
        marketDataTable[0][1] = "Value";
        int mktRow = 1;
        for (final ResultKey resultKey : resultKeys) {
          String rowLabel;
          if (!resultKey.isDefaultColumnSet()) {
            rowLabel = resultKey.getColumnSet() + "/" + resultKey.getResultType().getValueRequirementName();
          } else {
            rowLabel = resultKey.getResultType().getValueRequirementName();
          }
          if (includeProperties) {
            rowLabel = rowLabel + "[" + resultKey.getResultType().getProperties().toSimpleString() + "]";
          }
          marketDataTable[mktRow][0] = rowLabel;
          final ComputedValueResult res = mktResults.get(resultKey);
          if (res != null) {
            try {
              marketDataTable[mktRow][1] = converter.convertToString(res.getValue());
            } catch (final IllegalStateException ise) {
              // no converter, use toString()
              marketDataTable[mktRow][1] = res.getValue().toString();
            }
          } else {
            LOGGER.error("Result key {} returned null when the API should have returned a value.", resultKeys);
            LOGGER.error("Result keys available were {}", mktResults.keySet());
            marketDataTable[mktRow][1] = null;
          }
          mktRow++;
        }
        tables.add(marketDataTable);
      }
    }
    return tables;
  }

  public static String toPrettyPrintedString(final ResultModel resultModel) {
    final String[][] table = tabulatePortfolioResult(resultModel, false);
    final StringBuilder sb = new StringBuilder();
    sb.append(TablePrinter.toPrettyPrintedString(table) + "\n");
    final List<String[][]> tables = tabulateMarketDataAndSpecificRequirementResult(resultModel, false);
    for (final String[][] mdTable : tables) {
      sb.append(TablePrinter.toPrettyPrintedString(mdTable) + "\n");
    }
    return sb.toString();
  }

  /**
   * Pretty print a TabularResult in a text-based table format.
   * @param table a 2D array of Strings, not null
   * @return a String containing the table to be printed, not null.
   */
  public static String toPrettyPrintedString(final String[][] table) {
    ArgumentChecker.notNull(table, "table");
    if (table.length == 0) {
      throw new IllegalArgumentException("table must have at least one row (header)");
    }
    ensureSquare(table);
    final StringBuilder sb = new StringBuilder();
    final int[] maxWidths = maximumWidths(table);
    top(sb, maxWidths);
    header(sb, maxWidths, table[0]);
    middle(sb, maxWidths);
    for (int i = 1; i < table.length; i++) {
      row(sb, maxWidths, table[i]);
    }
    bottom(sb, maxWidths);
    return sb.toString();
  }

  private static void separator(final StringBuilder sb, final int[] maxWidths) {
    for (final int width : maxWidths) {
      sb.append("+");
      sb.append(repeat(width + 2, '-'));
    }
    sb.append("+");
    sb.append("\n");
  }

  private static void top(final StringBuilder sb, final int[] maxWidths) {
    separator(sb, maxWidths, '\u250c', '\u252c', '\u2510', '\u2500');
  }

  private static void middle(final StringBuilder sb, final int[] maxWidths) {
    separator(sb, maxWidths, '\u251c', '\u253c', '\u2524', '\u2500');
  }

  private static void bottom(final StringBuilder sb, final int[] maxWidths) {
    separator(sb, maxWidths, '\u2514', '\u2534', '\u2518', '\u2500');
  }

  private static void separator(final StringBuilder sb, final int[] maxWidths, final char lEdge, final char mid, final char rEdge, final char line) {
    sb.append(lEdge);
    for (int i = 0; i < maxWidths.length; i++) {
      sb.append(repeat(maxWidths[i] + 2, line));
      if (i < maxWidths.length - 1) {
        sb.append(mid);
      }
    }
    sb.append(rEdge);
    sb.append("\n");
  }



  private static void row(final StringBuilder sb, final int[] maxWidths, final String[] row) {
    int i = 0;
    for (final int width : maxWidths) {
      final String value = row[i++];
      sb.append("\u2502 ");
      if (value != null) {
        sb.append(value);
        sb.append(repeat(width - value.length(), ' '));
      } else {
        sb.append(NULL);
        sb.append(repeat(width - NULL_SIZE, ' '));
      }
      sb.append(" ");
    }
    sb.append("\u2502");
    sb.append("\n");
  }

  private static void header(final StringBuilder sb, final int[] maxWidths, final String[] header) {
    int i = 0;
    for (final int width : maxWidths) {
      final String value = header[i++];
      sb.append("\u2502 ");
      if (value != null) {
        sb.append(value);
        sb.append(repeat(width - value.length(), ' '));
      } else {
        sb.append(NULL);
        sb.append(repeat(width - NULL_SIZE, ' '));
      }
      sb.append(" ");
    }
    sb.append("\u2502");
    sb.append("\n");
  }

  private static String repeat(final int n, final char v) {
    final char[] repeated = new char[n];
    Arrays.fill(repeated, v);
    return new String(repeated); // this removes the need for commons lang StringUtils.repeat dependency
  }

  private static int[] maximumWidths(final String[][] result) {
    final int[] maxWidths = new int[result[0].length];
    for (final String[] row : result) {
      for (int i =0; i < row.length; i++) {
        final String value = row[i];
        if (value != null) {
          maxWidths[i] = Math.max(maxWidths[i], value.length());
        } else {
          maxWidths[i] = Math.max(maxWidths[i], NULL_SIZE);
        }
      }
    }
    return maxWidths;
  }

  private static void ensureSquare(final String[][] result) {
    int width = 0;
    for (final String[] row : result) {
      if (width == 0) {
        width = row.length;
      }
      if (width != row.length) {
        throw new OpenGammaRuntimeException("table array is not square");
      }
      width = row.length;
    }
  }
}
