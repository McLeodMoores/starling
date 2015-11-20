package com.mcleodmoores.starling.client.portfolio;

import au.com.bytecode.opencsv.CSVReader;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.util.ArgumentChecker;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.format.DateTimeParseException;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for FXForward trade CSV file (or String).
 */
public class FXForwardTradeFileParser {
  private static final Logger LOGGER = LoggerFactory.getLogger(FXForwardTradeFileParser.class);
  /**
   * Name of column heading in which to put portfolio name.
   */
  public static final String PORTFOLIO_COLUMN_NAME = "Portfolio".toUpperCase();
  /**
   * Name of column heading in which to put portfolio path.
   */
  public static final String PORTFOLIO_PATH_COLUMN_NAME = "Path".toUpperCase();

  private String _defaultPortfolioName;
  private final Pattern _pathRegex = Pattern.compile("(?:\\\\.|[^\\/\\\\]++)*");

  /**
   * Constructor.
   * @param defaultPortfolioName  the name to use if the portfolio name isn't specified in a column or null if none
   */
  public FXForwardTradeFileParser(final String defaultPortfolioName) {
    _defaultPortfolioName = defaultPortfolioName;
  }

  /**
   * Parse a trade file from a string
   * @param csv  a string containing the CSV file
   * @return a map of portfolio name to portfolio
   */
  public Map<String, SimplePortfolio> parseCSV(final String csv) {
    StringReader reader = new StringReader(csv);
    return parseCSV(reader);
  }

  /**
   * Parse the trade file from the given reader
   * @param reader  the reader from which to read the file, unbuffered is fine, not null
   * @return a map of portfolio name to portfolio
   */
  public Map<String, SimplePortfolio> parseCSV(final Reader reader) {
    ArgumentChecker.notNull(reader, "reader");
    int lineNum = 0;
    final Map<String, String> upperCaseProperties = mapUpperCaseProperties(FXForwardTrade.meta());
    try (CSVReader csvLoader = new CSVReader(new BufferedReader(reader))) {
      final String[] headerLine = csvLoader.readNext();
      Map<String, Integer> headerMap = extractHeader(headerLine);
      final Map<String, SimplePortfolio> portfolios = new HashMap<>();

      lineNum++;
      String[] line;
      while ((line = csvLoader.readNext()) != null) {
        Trade trade = readFXForwardTrade(line, headerMap, upperCaseProperties, lineNum);
        addTrade(portfolios, trade, line, headerMap, lineNum);
        lineNum++;
      }
      return portfolios;
    } catch (Exception e) {
      LOGGER.error("Error while loading CSV file", e);
      throw new RuntimeException("Error while loading CSV file", e);
    }
  }

  private void addTrade(final Map<String, SimplePortfolio> portfolios, final Trade trade, final String[] line,
                        final Map<String, Integer> headerMap, final int lineNum) {
    // figure out the name of the portfolio to put this trade into
    String portfolioName;
    if (_defaultPortfolioName != null) { // if the user specified an override portfolio
      portfolioName = _defaultPortfolioName;
    } else if (headerMap.containsKey(PORTFOLIO_COLUMN_NAME)) { // if there's a Portfolio column
      String portfolioNameStr = line[headerMap.get(PORTFOLIO_COLUMN_NAME)];
      if (portfolioNameStr != null && !portfolioNameStr.isEmpty()) { // if there's something in the column
        portfolioName = portfolioNameStr;
      } else {
        LOGGER.info("Portfolio column present but not filled in and no portfolio override has been specified on line {}", lineNum);
        System.exit(1);
        return;
      }
    } else {
      LOGGER.info("No Portfolio column present and no portfolio override has been specified on line {}", lineNum);
      System.exit(1);
      return;
    }
    // get/create the appropriate portfolio
    SimplePortfolio portfolio;
    if (portfolios.containsKey(portfolioName)) {
      portfolio = portfolios.get(portfolioName);
    } else {
      portfolio = new SimplePortfolio(portfolioName);
      portfolios.put(portfolioName, portfolio);
    }
    SimplePortfolioNode root = portfolio.getRootNode();
    if (headerMap.containsKey(PORTFOLIO_PATH_COLUMN_NAME)) {
      String portfolioPathStr = line[headerMap.get(PORTFOLIO_PATH_COLUMN_NAME)];
      if (portfolioPathStr != null && !portfolioPathStr.isEmpty()) {
        Deque<String> nodePathNames = new ArrayDeque<>();
        final Matcher matcher = _pathRegex.matcher(portfolioPathStr);
        while (matcher.find()) {
          String match = matcher.group();
          if (!match.isEmpty()) {
            nodePathNames.add(match);
            SimplePortfolioNode node = walkDown(root, nodePathNames);
            node.addPosition(trade.toPosition());
          }
        }
      } else {
        root.addPosition(trade.toPosition());
      }
    } else {
      root.addPosition(trade.toPosition());
    }
  }

  private SimplePortfolioNode walkDown(final SimplePortfolioNode current, final Deque<String> nodesDown) {
    if (nodesDown.isEmpty()) {
      return current;
    }
    String subNodeName = nodesDown.removeFirst();
    for (PortfolioNode node : current.getChildNodes()) {
      if (node.getName().equals(subNodeName)) {
        return walkDown((SimplePortfolioNode) node, nodesDown);
      }
    }
    SimplePortfolioNode newNode = new SimplePortfolioNode(subNodeName);
    current.addChildNode(newNode);
    return walkDown(newNode, nodesDown);
  }

  private Map<String, String> mapUpperCaseProperties(final MetaBean metaBean) {
    Map<String, String> map = new HashMap<>();
    for (MetaProperty property : metaBean.metaPropertyIterable()) {
      map.put(property.name().toUpperCase(), property.name());
    }
    return map;
  }

  private Trade readFXForwardTrade(final String[] line, final Map<String, Integer> headerMap, final Map<String, String> upperCaseProperties, int lineNum) {
    final FXForwardTrade.Builder builder = FXForwardTrade.builder();
    for (String upperCaseProperty : upperCaseProperties.keySet()) {
      if (headerMap.containsKey(upperCaseProperty)) {
        String value = line[headerMap.get(upperCaseProperty)];
        if (value != null && !value.isEmpty()) {
          try {
            builder.setString(upperCaseProperties.get(upperCaseProperty), value);
          } catch (DateTimeParseException dtpe) {
            LOGGER.error("Could not parse {} on line {} - try setting the date format option", value, lineNum);
            System.exit(1);
          }
        } else {
          LOGGER.error("No value for column {} on line {}", upperCaseProperty, lineNum);
          System.exit(1);
        }
      } else {
        LOGGER.error("No required column {} in file (note column headers are case insensitive)", upperCaseProperty);
        System.exit(1);
      }
    }
    try {
      return builder.build();
    } catch (IllegalArgumentException iae) {
      LOGGER.error("Required fields were missing when constructing trade", iae);
      System.exit(1);
      return null;
    }
  }

  private Map<String, Integer> extractHeader(final String[] headerRow) {
    Map<String, Integer> columnHeaders = new HashMap<>();
    for (int i = 0; i < headerRow.length; i++) {
      String columnName = headerRow[i];
      if (!columnName.isEmpty()) {
        if (!columnHeaders.containsKey(columnName)) {
          columnHeaders.put(columnName.toUpperCase(), i);
        } else {
          LOGGER.error("Column header contains duplicate column label");
          System.exit(1);
        }
      } else {
        LOGGER.error("Column {} does not have a non-empty header", i);
        System.exit(1);
      }
    }
    return columnHeaders;
  }
}
