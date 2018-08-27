/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.timeseries.snapshot;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.redis.RedisConnector;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/**
 * Snapshot the last known values for all fields in Redis.
 */
public class RedisLKVSnapshotter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedisLKVSnapshotter.class);

  private final Map<String, Boolean> _dataFieldBlackList = Maps.newHashMap();
  private final Map<String, Boolean> _schemeBlackList = Maps.newHashMap();
  private final String _normalizationRuleSetId;
  private final String _globalPrefix;
  private final RedisConnector _redisConnector;

  public RedisLKVSnapshotter(final BlackList dataFieldBlackList, final BlackList schemeBlackList, final String normalizationRuleSetId, final String globalPrefix, final RedisConnector redisConnector) {
    ArgumentChecker.notNull(normalizationRuleSetId, "normalizationRuleSetId");
    ArgumentChecker.notNull(globalPrefix, "globalPrefix");
    ArgumentChecker.notNull(redisConnector, "redisConnector");
    ArgumentChecker.notNull(dataFieldBlackList, "data field black list");
    ArgumentChecker.notNull(schemeBlackList, "scheme black list");

    _normalizationRuleSetId = normalizationRuleSetId;
    _globalPrefix = globalPrefix;
    _redisConnector = redisConnector;
    for (final String dataField : dataFieldBlackList.getBlackList()) {
      _dataFieldBlackList.put(dataField.toUpperCase(), Boolean.TRUE);
    }
    for (final String scheme : schemeBlackList.getBlackList()) {
      _schemeBlackList.put(scheme.toUpperCase(), Boolean.TRUE);
    }

  }

  /**
   * Gets the normalizationRuleSetId.
   * @return the normalizationRuleSetId
   */
  public String getNormalizationRuleSetId() {
    return _normalizationRuleSetId;
  }

  /**
   * Gets the globalPrefix.
   * @return the globalPrefix
   */
  public String getGlobalPrefix() {
    return _globalPrefix;
  }

  /**
   * Gets the redisConnector.
   * @return the redisConnector
   */
  public RedisConnector getRedisConnector() {
    return _redisConnector;
  }

  public Map<ExternalId, Map<String, String>> getLastKnownValues() {
    LOGGER.debug("Reading Redis LKV values for normalizationRuleSetId:{} globalPrefix:{} dataFieldBlackList:{} schemeBlackList:{}",
        new Object[] {getNormalizationRuleSetId(), getGlobalPrefix(), _dataFieldBlackList.keySet(), _schemeBlackList.keySet()});
    final List<ExternalId> allSecurities = getAllSecurities();
    final OperationTimer timer = new OperationTimer(LOGGER, "Reading LKV for {} securities", allSecurities.size());
    final Map<ExternalId, Map<String, String>> result = getLastKnownValues(allSecurities);
    timer.finished();
    return result;
  }

  @SuppressWarnings("unchecked")
  public Map<ExternalId, Map<String, String>> getLastKnownValues(final List<ExternalId> securities) {
    final Map<ExternalId, Map<String, String>> result = Maps.newHashMap();
    final JedisPool jedisPool = _redisConnector.getJedisPool();
    final Jedis jedis = jedisPool.getResource();
    final Pipeline pipeline = jedis.pipelined();
    //start transaction
    pipeline.multi();
    for (final ExternalId identifier : securities) {
      final String redisKey = generateRedisKey(identifier.getScheme().getName(), identifier.getValue(), getNormalizationRuleSetId());
      pipeline.hgetAll(redisKey);
    }
    final Response<List<Object>> response = pipeline.exec();
    pipeline.sync();

    final Iterator<ExternalId> allSecItr = securities.iterator();
    final Iterator<Object> responseItr = response.get().iterator();
    while (responseItr.hasNext() && allSecItr.hasNext()) {
      result.put(allSecItr.next(), filterBlackListedTicks((Map<String, String>) responseItr.next()));
    }
    jedisPool.returnResource(jedis);
    return result;
  }

  private Map<String, String> filterBlackListedTicks(final Map<String, String> ticks) {
    final Map<String, String> result = Maps.newHashMap();
    for (final Entry<String, String> entry : ticks.entrySet()) {
      final String fieldName = entry.getKey();
      if (_dataFieldBlackList.containsKey(fieldName.toUpperCase())) {
        continue;
      }
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  private String generateRedisKey(final String scheme, final String identifier, final String normalizationRuleSetId) {
    final StringBuilder sb = new StringBuilder();
    if (getGlobalPrefix() != null) {
      sb.append(getGlobalPrefix());
    }
    sb.append(scheme);
    sb.append("-");
    sb.append(identifier);
    sb.append("[");
    sb.append(normalizationRuleSetId);
    sb.append("]");
    return sb.toString();
  }

  private List<ExternalId> getAllSecurities() {
    final List<ExternalId> securities = Lists.newArrayList();
    final Set<String> allSchemes = getAllSchemes();
    for (final String scheme : allSchemes) {
      if (_schemeBlackList.containsKey(scheme.toUpperCase())) {
        continue;
      }
      final Set<String> allIdentifiers = getAllIdentifiers(scheme);
      for (final String identifier : allIdentifiers) {
        securities.add(ExternalId.of(scheme, identifier));
      }
    }
    return securities;
  }

  private Set<String> getAllSchemes() {
    final JedisPool jedisPool = _redisConnector.getJedisPool();
    final Jedis jedis = jedisPool.getResource();
    final Set<String> allMembers = jedis.smembers(generateAllSchemesKey());
    jedisPool.returnResource(jedis);
    LOGGER.info("Loaded {} schemes from Jedis (full contents in Debug level log)", allMembers.size());
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Loaded schemes from Jedis: {}", allMembers);
    }
    return allMembers;
  }

  private String generateAllSchemesKey() {
    final StringBuilder sb = new StringBuilder();
    if (getGlobalPrefix() != null) {
      sb.append(getGlobalPrefix());
    }
    sb.append("-<ALL_SCHEMES>");
    LOGGER.debug("AllSchemeKey: {}", sb.toString());
    return sb.toString();
  }

  private String generatePerSchemeKey(final String scheme) {
    final StringBuilder sb = new StringBuilder();
    if (getGlobalPrefix() != null) {
      sb.append(getGlobalPrefix());
    }
    sb.append(scheme);
    sb.append("-");
    sb.append("<ALL_IDENTIFIERS>");
    LOGGER.debug("PerSchemeKey: {}", sb.toString());
    return sb.toString();
  }

  private Set<String> getAllIdentifiers(final String identifierScheme) {
    final JedisPool jedisPool = _redisConnector.getJedisPool();
    final Jedis jedis = jedisPool.getResource();
    final Set<String> allMembers = jedis.smembers(generatePerSchemeKey(identifierScheme));
    jedisPool.returnResource(jedis);
    LOGGER.info("Loaded {} identifiers from Jedis (full contents in Debug level log)", allMembers.size());
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Loaded identifiers from Jedis: {}", allMembers);
    }
    return allMembers;
  }

}
