/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.writer;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.JodaBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.integration.copier.portfolio.rowparser.JodaBeanRowParser;
import com.opengamma.integration.copier.portfolio.rowparser.RowParser;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.sheet.writer.SheetWriter;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Writes positions of a single security type to a single sheet.
 */
public class SingleSheetSimplePositionWriter extends SingleSheetPositionWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(SingleSheetSimplePositionWriter.class);

  private final RowParser _rowParser;

  // current row context
  private Map<String, String> _currentRow = new HashMap<>();

  private final ManageablePortfolioNode _currentNode;
  private final ManageablePortfolio _portfolio;

  /** Generate one row per trade instead of one row per position */
  private final boolean _includeTrades;

  public SingleSheetSimplePositionWriter(final SheetWriter sheet, final RowParser rowParser, final boolean includeTrades) {
    super(sheet);

    ArgumentChecker.notNull(rowParser, "rowParser");
    _rowParser = rowParser;

    // create virtual manageable portfolio
    _currentNode = new ManageablePortfolioNode("Root");
    _portfolio = new ManageablePortfolio("Portfolio", _currentNode);
    _currentNode.setPortfolioId(_portfolio.getUniqueId());

    _includeTrades = includeTrades;
  }

  public SingleSheetSimplePositionWriter(final SheetWriter sheet, final RowParser rowParser) {
    this(sheet, rowParser, false);
  }

  public SingleSheetSimplePositionWriter(final SheetWriter sheet, final String securityType) {
    this(sheet, JodaBeanRowParser.newJodaBeanRowParser(securityType));
  }

  public SingleSheetSimplePositionWriter(final SheetFormat sheetFormat, final OutputStream outputStream, final RowParser rowParser) {
    this(SheetWriter.newSheetWriter(sheetFormat, outputStream, rowParser.getColumns()), rowParser);
  }

  public SingleSheetSimplePositionWriter(final SheetFormat sheetFormat, final OutputStream outputStream, final RowParser rowParser,
      final boolean includeTrades) {
    this(SheetWriter.newSheetWriter(sheetFormat, outputStream, rowParser.getColumns()), rowParser, includeTrades);
  }

  public SingleSheetSimplePositionWriter(final String filename, final RowParser rowParser) {
    this(SheetWriter.newSheetWriter(filename, rowParser.getColumns()), rowParser);
  }

  public SingleSheetSimplePositionWriter(final String filename, final RowParser rowParser, final boolean includeTrades) {
    this(SheetWriter.newSheetWriter(filename, rowParser.getColumns()), rowParser, includeTrades);
  }

  public SingleSheetSimplePositionWriter(final String filename, final String securityType) {
    this(filename, JodaBeanRowParser.newJodaBeanRowParser(securityType));
  }

  public SingleSheetSimplePositionWriter(final String filename, final String securityType, final boolean includeTrades) {
    this(filename, JodaBeanRowParser.newJodaBeanRowParser(securityType), includeTrades);
  }

  public SingleSheetSimplePositionWriter(final SheetFormat sheetFormat, final OutputStream outputStream, final String securityType) {
    this(sheetFormat, outputStream, JodaBeanRowParser.newJodaBeanRowParser(securityType));
  }

  public SingleSheetSimplePositionWriter(final SheetFormat sheetFormat, final OutputStream outputStream, final String securityType,
      final boolean includeTrades) {
    this(sheetFormat, outputStream, JodaBeanRowParser.newJodaBeanRowParser(securityType), includeTrades);
  }

  @Override
  public void addAttribute(final String key, final String value) {
    // Not supported
  }

  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> writePosition(final ManageablePosition position, final ManageableSecurity[] securities) {
    ArgumentChecker.notNull(position, "position");
    ArgumentChecker.notNull(securities, "securities");

    // Write securities
    _currentRow.putAll(_rowParser.constructRow(securities));

    if (_includeTrades) {
      // Write each trade as a separate row if the current position contains trades
      if (position.getTrades().size() > 0) {
        final ManageablePosition subPosition = JodaBeanUtils.clone(position);
        for (final ManageableTrade trade : position.getTrades()) {
          final Map<String, String> tempRow = new HashMap<>();
          tempRow.putAll(_currentRow);
          tempRow.putAll(_rowParser.constructRow(trade));

          // Set position quantity to its trade's quantity and write position
          subPosition.setQuantity(trade.getQuantity());
          tempRow.putAll(_rowParser.constructRow(subPosition));

          // Flush out the current row with trade
          if (!tempRow.isEmpty()) {
            getSheet().writeNextRow(tempRow);
          }
        }
      } else {
        // Write position
        _currentRow.putAll(_rowParser.constructRow(position));

        // Flush out the current row (excluding trades)
        if (!_currentRow.isEmpty()) {
          getSheet().writeNextRow(_currentRow);
        }
      }
    } else {
      // Write position
      _currentRow.putAll(_rowParser.constructRow(position));

      // Export only the first trade of each position or none at all
      if (!position.getTrades().isEmpty()) {
        _currentRow.putAll(_rowParser.constructRow(position.getTrades().get(0)));
      }
      if (position.getTrades().size() > 1) {
        LOGGER.warn("Omitting extra trades: only one trade per position is supported in the current mode");
      }
      if (!_currentRow.isEmpty()) {
        getSheet().writeNextRow(_currentRow);
      }
    }

    // Empty the current row buffer
    _currentRow = new HashMap<>();

    return ObjectsPair.of(position, securities);
  }

  @Override
  public String[] getCurrentPath() {
    return new String[] {};
  }

  @Override
  public void setPath(final String[] newPath) {
    // Nothing to do
  }

}
