/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.impl.NonVersionedRedisSecuritySource;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.metric.OpenGammaMetricRegistry;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/*
 * REDIS DATA STRUCTURES:
 * Portfolio Names:
 *     Key["PORTFOLIOS"] -> Hash
 *        Hash[Name] -> UniqueId for the portfolio
 * Portfolio Unique ID Lookups:
 *     Key["NAME-"Name] -> Hash
 *        Hash[UNIQUE_ID] -> UniqueId for the portfolio
 * Portfolio objects themselves:
 *     Key["PRT-"UniqueId] -> Hash
 *        Hash[NAME] -> Name
 *        HASH["ATT-"AttributeName] -> Attribute Value
 * Portfolio contents:
 *     Key["PRTPOS-"UniqueId] -> Set
 *        Each item in the list is a UniqueId for a position
 * Positions:
 *     Key["POS-"UniqueId] -> Hash
 *        Hash[QTY] -> Quantity
 *        Hash[SEC] -> ExternalId for the security
 *        Hash["ATT-"AttributeName] -> Attribute Value
 * Position contents:
 *     Key["POSTRADES-"UniqueId] -> Set
 *        Each item in the list is a UniqueId for a trade
 * Trades:
 *        Key["TRADE-"UniqueId] -> Hash
 *        Hash[QTY] -> Quantity
 *        Hash[SEC] -> ExternalId for the security
 *        Hash["ATT-"AttributeName] -> Attribute Value
 */

/**
 * A lightweight {@link PositionSource} that cannot handle any versioning, and
 * which stores all positions and portfolios as Redis-native data structures
 * (rather than Fudge encoding).
 */
public class NonVersionedRedisPositionSource implements PositionSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(NonVersionedRedisSecuritySource.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "RedisPos";
  /**
   * The default scheme for trade unique identifiers.
   */
  public static final String TRADE_IDENTIFIER_SCHEME_DEFAULT = "RedisTrade";

  private static final String PORTFOLIOS_HASH_KEY_NAME = "PORTFOLIOS";

  private final JedisPool _jedisPool;
  private final String _redisPrefix;
  private final String _portfoliosHashKeyName;
  private Timer _getPortfolioTimer = new Timer();
  private Timer _getPositionTimer = new Timer();
  private Timer _portfolioStoreTimer = new Timer();
  private Timer _positionStoreTimer = new Timer();
  private Timer _positionSetTimer = new Timer();
  private Timer _positionAddTimer = new Timer();

  public NonVersionedRedisPositionSource(final JedisPool jedisPool) {
    this(jedisPool, "");
  }

  public NonVersionedRedisPositionSource(final JedisPool jedisPool, final String redisPrefix) {
    ArgumentChecker.notNull(jedisPool, "jedisPool");
    ArgumentChecker.notNull(redisPrefix, "redisPrefix");

    _jedisPool = jedisPool;
    _redisPrefix = redisPrefix.intern();
    _portfoliosHashKeyName = constructallPortfoliosRedisKey();
    registerMetrics(OpenGammaMetricRegistry.getSummaryInstance(), OpenGammaMetricRegistry.getDetailedInstance(), "NonVersionedRedisPositionSource");
  }

  /**
   * Gets the jedisPool.
   * @return the jedisPool
   */
  public JedisPool getJedisPool() {
    return _jedisPool;
  }

  /**
   * Gets the redisPrefix.
   * @return the redisPrefix
   */
  public String getRedisPrefix() {
    return _redisPrefix;
  }

  public void registerMetrics(final MetricRegistry summaryRegistry, final MetricRegistry detailRegistry, final String namePrefix) {
    _getPortfolioTimer = summaryRegistry.timer(namePrefix + ".getPortfolio");
    _getPositionTimer = summaryRegistry.timer(namePrefix + ".getPosition");
    _portfolioStoreTimer = summaryRegistry.timer(namePrefix + ".portfolioStore");
    _positionStoreTimer = summaryRegistry.timer(namePrefix + ".positionStore");
    _positionSetTimer = summaryRegistry.timer(namePrefix + ".positionSet");
    _positionAddTimer = summaryRegistry.timer(namePrefix + ".positionAdd");
  }

  protected static UniqueId generateUniqueId() {
    return UniqueId.of(IDENTIFIER_SCHEME_DEFAULT, GUIDGenerator.generate().toString());
  }

  protected static UniqueId generateTradeUniqueId() {
    return UniqueId.of(TRADE_IDENTIFIER_SCHEME_DEFAULT, GUIDGenerator.generate().toString());
  }

  // ---------------------------------------------------------------------------------------
  // REDIS KEY MANAGEMENT
  // ---------------------------------------------------------------------------------------

  protected final String toRedisKey(final String id, final String intermediate) {
    final StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append(intermediate);
    sb.append(id);
    final String keyText = sb.toString();
    return keyText;
  }

  protected final String toRedisKey(final UniqueId uniqueId, final String intermediate) {
    return toRedisKey(uniqueId.toString(), intermediate);
  }

  protected final String toPortfolioRedisKey(final UniqueId uniqueId) {
    return toRedisKey(uniqueId, "PRT-");
  }

  protected final String toPortfolioPositionsRedisKey(final UniqueId uniqueId) {
    return toRedisKey(uniqueId, "PRTPOS-");
  }

  protected final String toPositionRedisKey(final UniqueId uniqueId) {
    return toRedisKey(uniqueId, "POS-");
  }

  protected final String toTradeRedisKey(final UniqueId uniqueId) {
    return toRedisKey(uniqueId, "TRADE-");
  }

  protected final String toPositionTradesRedisKey(final UniqueId uniqueId) {
    return toRedisKey(uniqueId, "POSTRADE-");
  }

  protected final String constructallPortfoliosRedisKey() {
    return toRedisKey(PORTFOLIOS_HASH_KEY_NAME, "");
  }

  protected final String toPortfolioNameRedisKey(final String portfolioName) {
    return toRedisKey(portfolioName, "NAME-");
  }

  // ---------------------------------------------------------------------------------------
  // DATA MANIPULATION
  // ---------------------------------------------------------------------------------------

  /**
   * Deep store an entire portfolio, including all positions.
   * The portfolio itself is not modified, including setting the unique ID.
   *
   * @param portfolio The portfolio to store.
   * @return the UniqueId of the portfolio.
   */
  public UniqueId storePortfolio(final Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    UniqueId uniqueId = null;

    try (Timer.Context context = _portfolioStoreTimer.time()) {

      final Jedis jedis = getJedisPool().getResource();
      try {

        uniqueId = storePortfolio(jedis, portfolio);
        storePortfolioNodes(jedis, toPortfolioPositionsRedisKey(uniqueId), portfolio.getRootNode());

        getJedisPool().returnResource(jedis);
      } catch (final Exception e) {
        LOGGER.error("Unable to store portfolio " + portfolio, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to store portfolio " + portfolio, e);
      }

    }

    return uniqueId;
  }

  public UniqueId storePosition(final Position position) {
    ArgumentChecker.notNull(position, "position");
    UniqueId uniqueId = null;

    try (Timer.Context context = _positionStoreTimer.time()) {

      final Jedis jedis = getJedisPool().getResource();
      try {

        uniqueId = storePosition(jedis, position);

        getJedisPool().returnResource(jedis);
      } catch (final Exception e) {
        LOGGER.error("Unable to store position " + position, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to store position " + position, e);
      }

    }

    return uniqueId;
  }

  /**
   * A special fast-pass method to just update a position quantity, without
   * updating any of the other fields. Results in a single Redis write.
   *
   * @param position The position, which must already be in the source.
   */
  public void updatePositionQuantity(final Position position) {
    ArgumentChecker.notNull(position, "position");

    try (Timer.Context context = _positionSetTimer.time()) {

      final Jedis jedis = getJedisPool().getResource();
      try {

        final String redisKey = toPositionRedisKey(position.getUniqueId());
        jedis.hset(redisKey, "QTY", position.getQuantity().toPlainString());

        getJedisPool().returnResource(jedis);
      } catch (final Exception e) {
        LOGGER.error("Unable to store position " + position, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to store position " + position, e);
      }

    }
  }

  /**
   * Store a new position and attach it to the specified portfolio.
   * @param portfolio the existing portfolio. Must already be in this source.
   * @param position the new position to store and attach.
   * @return map of id to position, not-null.
   */
  public Map<String, Position> addPositionToPortfolio(final Portfolio portfolio, final Position position) {
    return addPositionsToPortfolio(portfolio, Collections.singleton(position));
  }

  /**
   * Store a new set of positions and attach it to the specified portfolio.
   * @param portfolio the existing portfolio. Must already be in this source.
   * @param positions the new positions to store and attach.
   * @return map of id to position, not-null.
   */
  public Map<String, Position> addPositionsToPortfolio(final Portfolio portfolio, final Collection<Position> positions) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    ArgumentChecker.notNull(portfolio.getUniqueId(), "portfolio UniqueId");
    ArgumentChecker.notNull(positions, "position");

    final Map<String, Position> id2position = Maps.newLinkedHashMap();
    try (Timer.Context context = _positionAddTimer.time()) {

      final Jedis jedis = getJedisPool().getResource();
      try {

        final String[] uniqueIdStrings = new String[positions.size()];
        int i = 0;
        for (final Position position : positions) {
          final String uniqueId = storePosition(jedis, position).toString();
          uniqueIdStrings[i] = uniqueId;
          i++;
          id2position.put(uniqueId, position);
        }
        final UniqueId portfolioUniqueId = portfolio.getUniqueId();
        final String portfolioPositionsKey = toPortfolioPositionsRedisKey(portfolioUniqueId);
        // NOTE kirk 2013-06-18 -- The following call is a known performance bottleneck.
        // I spent a full day attempting almost every single way I could imagine to
        // figure out what was going on, before I gave up for the time being.
        // When we're running in a far more realistic way we need to second guess
        // it, but it is a known performance issue on large portfolio loading.
        jedis.sadd(portfolioPositionsKey, uniqueIdStrings);

        getJedisPool().returnResource(jedis);
      } catch (final Exception e) {
        LOGGER.error("Unable to store positions " + positions, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to store positions " + positions, e);
      }
    }
    return id2position;

  }

  protected UniqueId storePortfolio(final Jedis jedis, final Portfolio portfolio) {
    UniqueId uniqueId = portfolio.getUniqueId();
    if (uniqueId == null) {
      uniqueId = generateUniqueId();
    }

    final String uniqueIdKey = toPortfolioRedisKey(uniqueId);
    final String portfolioNameKey = toPortfolioNameRedisKey(portfolio.getName());
    jedis.hset(portfolioNameKey, "UNIQUE_ID", uniqueId.toString());

    jedis.hset(_portfoliosHashKeyName, portfolio.getName(), uniqueId.toString());

    jedis.hset(uniqueIdKey, "NAME", portfolio.getName());

    for (final Map.Entry<String, String> attribute : portfolio.getAttributes().entrySet()) {
      jedis.hset(uniqueIdKey, "ATT-" + attribute.getKey(), attribute.getValue());
    }

    return uniqueId;
  }

  protected void storePortfolioNodes(final Jedis jedis, final String redisKey, final PortfolioNode node) {
    final Set<String> positionUniqueIds = new HashSet<>();
    for (final Position position : node.getPositions()) {
      final UniqueId uniqueId = storePosition(jedis, position);
      positionUniqueIds.add(uniqueId.toString());
    }
    if (!positionUniqueIds.isEmpty()) {
      jedis.sadd(redisKey, positionUniqueIds.toArray(new String[0]));
    }

    if (!node.getChildNodes().isEmpty()) {
      LOGGER.warn("Possible misuse. Portfolio has a deep structure, but this source flattens. Positions being stored flat.");
    }
    for (final PortfolioNode childNode : node.getChildNodes()) {
      storePortfolioNodes(jedis, redisKey, childNode);
    }
  }

  protected UniqueId storePosition(final Jedis jedis, final Position position) {
    UniqueId uniqueId = position.getUniqueId();
    if (uniqueId == null) {
      uniqueId = generateUniqueId();
    }

    final String redisKey = toPositionRedisKey(uniqueId);
    jedis.hset(redisKey, "QTY", position.getQuantity().toPlainString());
    final ExternalIdBundle securityBundle = position.getSecurityLink().getExternalId();
    if (securityBundle == null) {
      throw new OpenGammaRuntimeException("Can only store positions with a link to an ExternalId");
    }
    if (securityBundle.size() != 1) {
      LOGGER.warn("Bundle {} not exactly one. Possible misuse of this source.", securityBundle);
    }
    final ExternalId securityId = securityBundle.iterator().next();
    jedis.hset(redisKey, "SEC", securityId.toString());

    for (final Map.Entry<String, String> attribute : position.getAttributes().entrySet()) {
      jedis.hset(redisKey, "ATT-" + attribute.getKey(), attribute.getValue());
    }

    if (position.getTrades() != null) {
      final Set<String> tradeUniqueIds = new HashSet<>();
      for (final Trade trade : position.getTrades()) {
        final UniqueId tradeId = storeTrade(jedis, trade);
        tradeUniqueIds.add(tradeId.toString());
      }
      jedis.sadd(toPositionTradesRedisKey(uniqueId), tradeUniqueIds.toArray(new String[tradeUniqueIds.size()]));
    }

    return uniqueId;
  }

  protected UniqueId storeTrade(final Jedis jedis, final Trade trade) {
    UniqueId uniqueId = trade.getUniqueId();
    if (uniqueId == null) {
      uniqueId = generateTradeUniqueId();
    }

    final String redisKey = toTradeRedisKey(uniqueId);
    jedis.hset(redisKey, "QTY", trade.getQuantity().toPlainString());
    final ExternalIdBundle securityBundle = trade.getSecurityLink().getExternalId();
    if (securityBundle == null) {
      throw new OpenGammaRuntimeException("Can only store positions with a link to an ExternalId");
    }
    if (securityBundle.size() != 1) {
      LOGGER.warn("Bundle {} not exactly one. Possible misuse of this source.", securityBundle);
    }
    final ExternalId securityId = securityBundle.iterator().next();
    jedis.hset(redisKey, "SEC", securityId.toString());

    for (final Map.Entry<String, String> attribute : trade.getAttributes().entrySet()) {
      jedis.hset(redisKey, "ATT-" + attribute.getKey(), attribute.getValue());
    }

    return uniqueId;
  }

  // ---------------------------------------------------------------------------------------
  // QUERIES OUTSIDE OF POSITION SOURCE INTERFACE
  // ---------------------------------------------------------------------------------------

  public Portfolio getByName(final String portfolioName) {
    ArgumentChecker.notNull(portfolioName, "portfolioName");

    Portfolio portfolio = null;

    try (Timer.Context context = _getPortfolioTimer.time()) {

      final Jedis jedis = getJedisPool().getResource();
      try {
        final String nameKey = toPortfolioNameRedisKey(portfolioName);
        final String uniqueIdString = jedis.hget(nameKey, "UNIQUE_ID");

        if (uniqueIdString != null) {
          final UniqueId uniqueId = UniqueId.parse(uniqueIdString);
          portfolio = getPortfolioWithJedis(jedis, uniqueId);
        }

        getJedisPool().returnResource(jedis);
      } catch (final Exception e) {
        LOGGER.error("Unable to get portfolio by name " + portfolioName, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to get portfolio by name " + portfolioName, e);
      }

    }
    return portfolio;
  }

  public Map<String, UniqueId> getAllPortfolioNames() {
    final Map<String, UniqueId> result = new TreeMap<>();
    final Jedis jedis = getJedisPool().getResource();
    try {
      final Map<String, String> portfolioNames = jedis.hgetAll(_portfoliosHashKeyName);
      for (final Map.Entry<String, String> entry : portfolioNames.entrySet()) {
        result.put(entry.getKey(), UniqueId.parse(entry.getValue()));
      }

      getJedisPool().returnResource(jedis);
    } catch (final Exception e) {
      LOGGER.error("Unable to get portfolio names", e);
      getJedisPool().returnBrokenResource(jedis);
      throw new OpenGammaRuntimeException("Unable to get portfolio names", e);
    }
    return result;
  }

  // ---------------------------------------------------------------------------------------
  // IMPLEMENTATION OF POSITION SOURCE
  // ---------------------------------------------------------------------------------------

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  @Override
  public Portfolio getPortfolio(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    SimplePortfolio portfolio = null;

    try (Timer.Context context = _getPortfolioTimer.time()) {

      final Jedis jedis = getJedisPool().getResource();
      try {
        portfolio = getPortfolioWithJedis(jedis, uniqueId);

        getJedisPool().returnResource(jedis);
      } catch (final Exception e) {
        LOGGER.error("Unable to get portfolio " + uniqueId, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to get portfolio " + uniqueId, e);
      }

    }

    if (portfolio == null) {
      throw new DataNotFoundException("Unable to locate portfolio with UniqueId " + uniqueId);
    }

    return portfolio;
  }

  protected SimplePortfolio getPortfolioWithJedis(final Jedis jedis, final UniqueId uniqueId) {
    SimplePortfolio portfolio = null;
    final String redisKey = toPortfolioRedisKey(uniqueId);
    if (jedis.exists(redisKey)) {
      final Map<String, String> hashFields = jedis.hgetAll(redisKey);

      portfolio = new SimplePortfolio(hashFields.get("NAME"));
      portfolio.setUniqueId(uniqueId);

      for (final Map.Entry<String, String> field : hashFields.entrySet()) {
        if (!field.getKey().startsWith("ATT-")) {
          continue;
        }
        final String attributeName = field.getKey().substring(4);
        portfolio.addAttribute(attributeName, field.getValue());
      }

      final SimplePortfolioNode portfolioNode = new SimplePortfolioNode();
      portfolioNode.setName(portfolio.getName());

      final String portfolioPositionsKey = toPortfolioPositionsRedisKey(portfolio.getUniqueId());
      final Set<String> positionUniqueIds = jedis.smembers(portfolioPositionsKey);
      for (final String positionUniqueId : positionUniqueIds) {
        final Position position = getPosition(jedis, UniqueId.parse(positionUniqueId));
        if (position != null) {
          portfolioNode.addPosition(position);
        }
      }
      portfolio.setRootNode(portfolioNode);
    }
    return portfolio;
  }

  @Override
  public Portfolio getPortfolio(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return getPortfolio(UniqueId.of(objectId, null), null);
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueId uniqueId, final VersionCorrection versionCorrection) {
    throw new UnsupportedOperationException("Trades not supported.");
  }

  @Override
  public Position getPosition(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    SimplePosition position = null;

    try (Timer.Context context = _getPositionTimer.time()) {

      final Jedis jedis = getJedisPool().getResource();
      try {

        position = getPosition(jedis, uniqueId);

        getJedisPool().returnResource(jedis);
      } catch (final Exception e) {
        LOGGER.error("Unable to get position " + uniqueId, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to get position " + uniqueId, e);
      }

    }

    if (position == null) {
      throw new DataNotFoundException("Unable to find position with UniqueId " + uniqueId);
    }

    return position;
  }

  protected SimplePosition getPosition(final Jedis jedis, final UniqueId uniqueId) {
    final String redisKey = toPositionRedisKey(uniqueId);
    if (!jedis.exists(redisKey)) {
      return null;
    }
    final SimplePosition position = new SimplePosition();
    position.setUniqueId(uniqueId);
    final Map<String, String> hashFields = jedis.hgetAll(redisKey);
    position.setQuantity(new BigDecimal(hashFields.get("QTY")));
    final ExternalId secId = ExternalId.parse(hashFields.get("SEC"));
    final SimpleSecurityLink secLink = new SimpleSecurityLink();
    secLink.addExternalId(secId);
    position.setSecurityLink(secLink);

    for (final Map.Entry<String, String> field : hashFields.entrySet()) {
      if (!field.getKey().startsWith("ATT-")) {
        continue;
      }
      final String attributeName = field.getKey().substring(4);
      position.addAttribute(attributeName, field.getValue());
    }

    // trades
    final String tradesKey = toPositionTradesRedisKey(position.getUniqueId());
    final Set<String> tradesUniqueIds = jedis.smembers(tradesKey);
    for (final String tradesUniqueId : tradesUniqueIds) {
      final Trade trade = getTrade(jedis, UniqueId.parse(tradesUniqueId));
      if (trade != null) {
        position.addTrade(trade);
      }
    }

    return position;
  }

  protected SimpleTrade getTrade(final Jedis jedis, final UniqueId uniqueId) {
    final String redisKey = toTradeRedisKey(uniqueId);
    if (!jedis.exists(redisKey)) {
      return null;
    }
    final SimpleTrade trade = new SimpleTrade();
    trade.setUniqueId(uniqueId);
    final Map<String, String> hashFields = jedis.hgetAll(redisKey);
    trade.setQuantity(new BigDecimal(hashFields.get("QTY")));
    final ExternalId secId = ExternalId.parse(hashFields.get("SEC"));
    final SimpleSecurityLink secLink = new SimpleSecurityLink();
    secLink.addExternalId(secId);
    trade.setSecurityLink(secLink);

    for (final Map.Entry<String, String> field : hashFields.entrySet()) {
      if (!field.getKey().startsWith("ATT-")) {
        continue;
      }
      final String attributeName = field.getKey().substring(4);
      trade.addAttribute(attributeName, field.getValue());
    }

    return trade;
  }

  @Override
  public Position getPosition(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return getPosition(UniqueId.of(objectId, null));
  }

  @Override
  public Trade getTrade(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    SimpleTrade trade = null;

    try (Timer.Context context = _getPositionTimer.time()) {

      final Jedis jedis = getJedisPool().getResource();
      try {

        trade = getTrade(jedis, uniqueId);

        getJedisPool().returnResource(jedis);
      } catch (final Exception e) {
        LOGGER.error("Unable to get position " + uniqueId, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to get trade " + uniqueId, e);
      }

    }

    if (trade == null) {
      throw new DataNotFoundException("Unable to find position with UniqueId " + uniqueId);
    }

    return trade;
  }

}
