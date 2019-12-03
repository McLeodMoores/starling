/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

import au.com.bytecode.opencsv.CSVReader;

/**
 * A source of positions based on CSV-formatted files.
 */
public class CSVPositionSource implements PositionSource {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CSVPositionSource.class);

  /**
   * The base file directory.
   */
  private final File _baseDirectory;
  /**
   * The portfolio by identifier.
   */
  private final ConcurrentMap<ObjectId, Object> _portfolios = new ConcurrentSkipListMap<>();
  /**
   * The nodes by identifier.
   */
  private final Map<UniqueId, PortfolioNode> _nodes = new TreeMap<>();
  /**
   * The positions by identifier.
   */
  private final Map<ObjectId, Position> _positions = new TreeMap<>();
  /**
   * The trades by identifier.
   */
  private final Map<UniqueId, Trade> _trades = new TreeMap<>();

  /**
   * Creates an empty CSV position source.
   */
  public CSVPositionSource() {
    _baseDirectory = null;
  }

  /**
   * Creates a CSV position source using the specified directory.
   * @param baseDirectoryName  the directory name, not null
   */
  public CSVPositionSource(final String baseDirectoryName) {
    this(new File(baseDirectoryName));
  }

  /**
   * Creates a CSV position source using the specified directory.
   * @param baseDirectory  the directory, not null
   */
  public CSVPositionSource(final File baseDirectory) {
    ArgumentChecker.notNull(baseDirectory, "base directory");
    if (!baseDirectory.exists()) {
      throw new IllegalArgumentException("Base directory must exist: " + baseDirectory);
    }
    if (!baseDirectory.isDirectory()) {
      throw new IllegalArgumentException("Base directory must be a directory: " + baseDirectory);
    }
    try {
      _baseDirectory = baseDirectory.getCanonicalFile();
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Base directory must resolve to a canonical reference: " + baseDirectory, ex);
    }
    populatePortfolioIds();
  }

  /**
   * Populate the portfolio identifiers from the base directory.
   */
  private void populatePortfolioIds() {
    final File[] filesInBaseDirectory = getBaseDirectory().listFiles();
    for (final File file : filesInBaseDirectory) {
      if (!file.isFile() || file.isHidden() || !file.canRead()) {
        continue;
      }
      final String portfolioName = buildPortfolioName(file.getName());
      _portfolios.put(ObjectId.of("CSV-" + file.getName(), portfolioName), file);
    }
  }

  private String buildPortfolioName(final String fileName) {
    if (fileName.endsWith(".csv") || fileName.endsWith(".txt")) {
      return fileName.substring(0, fileName.length() - 4);
    }
    return fileName;
  }

  private Position getPosition(final ObjectId positionId) {
    final Position position = _positions.get(positionId);
    if (position == null) {
      throw new DataNotFoundException("Unable to find position: " + positionId);
    }
    return position;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base directory.
   * @return the baseDirectory, may be null
   */
  public File getBaseDirectory() {
    return _baseDirectory;
  }

  //-------------------------------------------------------------------------
  public Set<ObjectId> getPortfolioIds() {
    return Collections.unmodifiableSet(_portfolios.keySet());
  }

  @Override
  public Portfolio getPortfolio(final UniqueId portfolioId, final VersionCorrection versionCorrection) {
    // Ignore the version
    return getPortfolio(portfolioId.getObjectId(), VersionCorrection.LATEST);
  }

  @Override
  public Portfolio getPortfolio(final ObjectId objectId, final VersionCorrection versionCorrection) {
    Object portfolio = _portfolios.get(objectId);
    if (portfolio instanceof File) {
      final Portfolio created = loadPortfolio(objectId, (File) portfolio);
      _portfolios.replace(objectId, portfolio, created);
      portfolio = _portfolios.get(objectId);
    }
    if (portfolio instanceof Portfolio) {
      return (Portfolio) portfolio;
    }
    throw new DataNotFoundException("Unable to find portfolio: " + objectId);
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueId nodeId, final VersionCorrection versionCorrection) {
    final PortfolioNode node = _nodes.get(nodeId);
    if (node == null) {
      throw new DataNotFoundException("Unable to find node: " + nodeId);
    }
    return node;
  }

  @Override
  public Position getPosition(final UniqueId positionId) {
    // Ignore the version
    return getPosition(positionId.getObjectId());
  }

  @Override
  public Position getPosition(final ObjectId positionId, final VersionCorrection versionCorrection) {
    // Ignore the version
    return getPosition(positionId);
  }

  @Override
  public Trade getTrade(final UniqueId tradeId) {
    final Trade trade = _trades.get(tradeId);
    if (trade == null) {
      throw new DataNotFoundException("Unable to find trade: " + tradeId);
    }
    return trade;
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  //-------------------------------------------------------------------------
  private Portfolio loadPortfolio(final ObjectId portfolioId, final File file) {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      return loadPortfolio(portfolioId, fis);
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Unable to parse portfolio file: " + file, ex);
    } finally {
      IOUtils.closeQuietly(fis);
    }
  }

  private Portfolio loadPortfolio(final ObjectId portfolioId, final InputStream inStream) throws IOException {
    final SimplePortfolio portfolio = new SimplePortfolio(portfolioId.atVersion("0"), portfolioId.getValue());
    final UniqueId rootNodeId = UniqueId.of(portfolioId.getScheme(), "0");
    portfolio.getRootNode().setUniqueId(rootNodeId);
    _nodes.put(rootNodeId, portfolio.getRootNode());

    final CSVReader csvReader = new CSVReader(new InputStreamReader(inStream));
    String[] tokens = null;
    int curIndex = 1;
    UniqueId positionId = UniqueId.of(portfolioId.getScheme(), Integer.toString(curIndex));
    while ((tokens = csvReader.readNext()) != null) {
      final SimplePosition position = parseLine(tokens, positionId);
      if (position != null) {
        portfolio.getRootNode().addPosition(position);
        _positions.put(position.getUniqueId().getObjectId(), position);
        positionId = UniqueId.of(portfolioId.getScheme(), Integer.toString(++curIndex));
      }
    }
    LOGGER.info("{} parsed stream with {} positions", portfolioId, portfolio.getRootNode().getPositions().size());
    return portfolio;
  }

  /**
   * @param tokens  the tokens to parse, not null
   * @param positionId  the portfolio id, not null
   * @return the position
   */
  /* package for testing */ static SimplePosition parseLine(final String[] tokens, final UniqueId positionId) {
    if (tokens.length < 3) {
      return null;
    }
    // First token is the quantity
    final BigDecimal quantity = new BigDecimal(tokens[0].trim());

    // Each set of 2 tokens is then security id domain and then id
    final List<ExternalId> securityIdentifiers = new ArrayList<>();
    for (int i = 1; i < tokens.length - 1; i++) {
      final String idScheme = tokens[i].trim();
      final String idValue = tokens[++i].trim();
      final ExternalId id = ExternalId.of(idScheme, idValue);
      securityIdentifiers.add(id);
    }
    final ExternalIdBundle securityKey = ExternalIdBundle.of(securityIdentifiers);
    LOGGER.debug("Loaded position: {} in {}", quantity, securityKey);

    return new SimplePosition(positionId, quantity, securityKey);
  }

}
