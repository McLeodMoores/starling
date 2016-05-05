/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio;

import au.com.bytecode.opencsv.CSVWriter;
import com.mcleodmoores.starling.client.portfolio.FXForwardTrade;
import com.mcleodmoores.starling.client.portfolio.Trade;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.util.ArgumentChecker;
import org.apache.commons.lang.WordUtils;
import org.joda.beans.MetaProperty;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File writer for FX Forward trades.
 */
public class FXForwardTradeFileWriter {
  /**
   * Name of column heading in which to put portfolio name (not uppercase though).
   */
  public static final String PORTFOLIO_COLUMN_NAME = "Portfolio";
  /**
   * Name of column heading in which to put portfolio path (not uppercase though).
   */
  public static final String PORTFOLIO_PATH_COLUMN_NAME = "Path";
  private String[] _columnNames;
  private Map<String, MetaProperty<?>> _fieldNamesToProperties = new HashMap<>();

  /**
   * No-arg constructor
   */
  public FXForwardTradeFileWriter() {
    _columnNames = buildHeaders();
  }

  private String[] buildHeaders() {
    List<String> columnNames = new ArrayList<>();
    columnNames.add(PORTFOLIO_COLUMN_NAME);
    columnNames.add(PORTFOLIO_PATH_COLUMN_NAME);
    for (MetaProperty<?> metaProperty : FXForwardTrade.meta().metaPropertyIterable()) {
      String capitalizedName = WordUtils.capitalize(metaProperty.name());
      columnNames.add(capitalizedName);
      _fieldNamesToProperties.put(capitalizedName, metaProperty);
    }
    return columnNames.toArray(new String[columnNames.size()]);
  }

  public void writePortfolio(final Writer writer, final SimplePortfolio portfolio) {
    writePortfolios(writer, Collections.singletonList(portfolio));
  }

  public void writePortfolios(final Writer writer, final List<SimplePortfolio> portfolios) {
    try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(writer))) {
      writeHeader(csvWriter, _columnNames);
      for (SimplePortfolio portfolio : portfolios) {
        writePortfolio(csvWriter, portfolio);
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private void writeHeader(final CSVWriter csvWriter, final String[] columnNames) {
    csvWriter.writeNext(columnNames);
  }

  private void writePortfolio(final CSVWriter csvWriter, final SimplePortfolio portfolio) {
    writePortfolioNode(csvWriter, portfolio.getName(), null, portfolio.getRootNode());
  }

  private void writePortfolioNode(final CSVWriter csvWriter, final String portfolioName, final String path, final PortfolioNode portfolioNode) {
    String myPath = path == null ? portfolioNode.getName() : path + "/" + portfolioNode.getName();
    for (PortfolioNode child : portfolioNode.getChildNodes()) {
      writePortfolioNode(csvWriter, portfolioName, myPath, child);
    }
    for (Position position : portfolioNode.getPositions()) {
      writePosition(csvWriter, portfolioName, myPath, position);
    }
  }

  /**
   * Escape forward slashes and backslashes.  e.g. "AUD/EUR 1\3" becomes "AUD\/EUD 1\\3" so not confused with path separators.
   * @param name  name of a portfolio node
   * @return escaped string
   */
  private String escape(final String name) {
    return name.replace("\\", "\\\\").replace("/", "\\/");
  }

  private void writePosition(final CSVWriter csvWriter, final String portfolioName, final String path, final Position position) {
    String[] line = new String[_columnNames.length];
    for (int i = 0; i < _columnNames.length; i++) {
      String columnName = _columnNames[i];
      MetaProperty<?> metaProperty =  _fieldNamesToProperties.get(columnName);
      switch (columnName) {
        case PORTFOLIO_COLUMN_NAME:
          line[i] = portfolioName;
          break;
        case PORTFOLIO_PATH_COLUMN_NAME:
          line[i] = path;
          break;
        default:
          if (metaProperty != null) {
            FXForwardTrade trade = toTrade(position);
            line[i] = metaProperty.getString(trade);
          } else {
            throw new IllegalStateException("Logic error in write position, property " + columnName + " not in meta property map");
          }
          break;
      }
    }
    csvWriter.writeNext(line);
  }

  private FXForwardTrade toTrade(final Position position) {
    return FXForwardTrade.from(position);
  }
}
