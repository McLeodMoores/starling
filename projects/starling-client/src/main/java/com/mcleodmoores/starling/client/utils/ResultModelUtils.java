package com.mcleodmoores.starling.client.utils;

import au.com.bytecode.opencsv.CSVWriter;
import com.mcleodmoores.starling.client.results.MarketDataTargetKey;
import com.mcleodmoores.starling.client.results.PortfolioNodeTargetKey;
import com.mcleodmoores.starling.client.results.PositionTargetKey;
import com.mcleodmoores.starling.client.results.ResultKey;
import com.mcleodmoores.starling.client.results.ResultModel;
import com.mcleodmoores.starling.client.results.ResultModelImpl;
import com.mcleodmoores.starling.client.results.TargetKey;
import com.opengamma.engine.value.ComputedValueResult;
import org.joda.beans.JodaBeanUtils;
import org.joda.convert.StringConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * File/printing utilities for ResultModel.
 */
public class ResultModelUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResultModelUtils.class);

  public static void saveResult(final ResultModel resultModel, final File file) {
    String[] blankRow = new String[0];
    try (CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(file)))) {
      String[][] portfolioTable = tabulatePortfolioResult(resultModel, true);
      for (String[] row : portfolioTable) {
        writer.writeNext(row);
      }
      writer.writeNext(blankRow);
      List<String[][]> mktDataTables = tabulateMarketDataResult(resultModel, true);
      for (String[][] mktDataTable : mktDataTables) {
        for (String[] row : mktDataTable) {
          writer.writeNext(row);
        }
        writer.writeNext(blankRow);
      }
      writer.flush();
      writer.close();
    } catch (IOException ioe) {
      LOGGER.error("Exception writing to file", ioe);
    }

  }

  public static String[][] tabulatePortfolioResult(final ResultModel resultModel, final boolean includeProperties) {
    StringConvert converter = JodaBeanUtils.stringConverter();
    Set<ResultKey> portfolioResultKeys = resultModel.getRequestedPortfolioResultKeys();
    List<TargetKey> portfolioTargetKeys = resultModel.getTargetKeys(EnumSet.of(ResultModelImpl.TargetType.PORTFOLIO_NODE, ResultModelImpl.TargetType.POSITION));
    String[][] table = new String[portfolioTargetKeys.size() + 1][portfolioResultKeys.size() + 1];
    int column = 1;
    int row = 0;
    table[0][0] = "Node/Position";
    // do header row.
    for (ResultKey key : portfolioResultKeys) {
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
    for (TargetKey targetKey : portfolioTargetKeys) {
      if (targetKey instanceof PortfolioNodeTargetKey) {
        PortfolioNodeTargetKey portfolioNodeTargetKey = (PortfolioNodeTargetKey) targetKey;
        table[row][0] = portfolioNodeTargetKey.getNodePath();
      } else if (targetKey instanceof PositionTargetKey) {
        PositionTargetKey positionTargetKey = (PositionTargetKey) targetKey;
        table[row][0] = positionTargetKey.getCorrelationId().toString();
      }
      Map<ResultKey, ComputedValueResult> resultsForTarget = resultModel.getResultsForTarget(targetKey);
      int col = 1;
      for (ResultKey portfolioResultKey : portfolioResultKeys) {
        ComputedValueResult result = resultsForTarget.get(portfolioResultKey);
        if (result != null) {
          Object value = result.getValue();
          if (value != null) {
            try {
              table[row][col] = converter.convertToString(value);
            } catch (IllegalStateException ise) {
              // no converter, use toString()
              table[row][col] = value.toString();
            }
          } else {
            table[row][col] = "null";
          }
        } else {
          table[row][col] = "";
        }
        col++;
      }
      row++;
    }
    return table;
  }

  public static List<String[][]> tabulateMarketDataResult(final ResultModel resultModel, final boolean includeProperties) {
    StringConvert converter = JodaBeanUtils.stringConverter();
    List<String[][]> tables = new ArrayList<>();
    List<TargetKey> marketDataTargetKeys = resultModel.getTargetKeys(EnumSet.of(ResultModelImpl.TargetType.MARKET_DATA));
    for (TargetKey targetKey : marketDataTargetKeys) {
      if (targetKey instanceof MarketDataTargetKey) {
        MarketDataTargetKey marketDataTargetKey = (MarketDataTargetKey) targetKey;
        Set<ResultKey> resultKeys = resultModel.getRequestedMarketDataResultKeys();
        Map<ResultKey, ComputedValueResult> mktResults = resultModel.getResultsForTarget(marketDataTargetKey);
        String[][] marketDataTable = new String[resultKeys.size() + 1][2];
        marketDataTable[0][0] = "Result Key";
        marketDataTable[0][1] = "Value";
        int mktRow = 1;
        for (ResultKey resultKey : resultKeys) {
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
          ComputedValueResult res = mktResults.get(resultKey);
          try {
            marketDataTable[mktRow][1] = converter.convertToString(res.getValue());
          } catch (IllegalStateException ise) {
            // no converter, use toString()
            marketDataTable[mktRow][1] = res.getValue().toString();
          }
          mktRow++;
        }
        tables.add(marketDataTable);
      }
    }
    return tables;
  }

  public static void displayResult(final ResultModel resultModel) {
    String[][] table = tabulatePortfolioResult(resultModel, false);
    System.out.println(TablePrinter.toPrettyPrintedString(table));
    List<String[][]> tables = tabulateMarketDataResult(resultModel, false);
    for (String[][] mdTable : tables) {
      System.out.println(TablePrinter.toPrettyPrintedString(mdTable));
    }
  }
}
