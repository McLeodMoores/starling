/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ExternalId;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * An implemention of {@link LastKnownValueStoreProvider} which backs onto Redis.
 * <p>
 * It has the following properties that should be set:
 * <dl>
 *   <dt>server</dt>
 *   <dd>The hostname of the server that should be used. Defaults to localhost.</dd>
 *   <dt>port</dt>
 *   <dd>The redis port to connect to. Defaults to 6379.</dd>
 *   <dt>globalPrefix</dt>
 *   <dd>A string that will be prepended onto all keys to separate different uses on the same
 *       Redis cluster. Defaults to empty string, for unit testing should probably be set
 *       to user name.</dd>
 *   <dt>writeThrough</dt>
 *   <dd>Whether all writes should flow through to the underlying Redis store.
 *       Defaults to true.
 *       If there are multiple live data servers, only one of which is in charge
 *       of updating Redis, set this to false on all but the master updating
 *       version.</dd>
 * </dl>
 *
 */
public class RedisLastKnownValueStoreProvider implements LastKnownValueStoreProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(RedisLastKnownValueStoreProvider.class);
  private String _server = "localhost";
  private int _port = 6379;
  private String _globalPrefix = "";
  private boolean _writeThrough = true;
  private volatile boolean _isInitialized;
  private JedisPool _jedisPool;

  /**
   * Gets the server.
   * @return the server
   */
  public String getServer() {
    return _server;
  }

  /**
   * Sets the server.
   * @param server  the server
   */
  public void setServer(final String server) {
    _server = server;
  }

  /**
   * Gets the port.
   * @return the port
   */
  public int getPort() {
    return _port;
  }

  /**
   * Sets the port.
   * @param port  the port
   */
  public void setPort(final int port) {
    _port = port;
  }

  /**
   * Gets the globalPrefix.
   * @return the globalPrefix
   */
  public String getGlobalPrefix() {
    return _globalPrefix;
  }

  /**
   * Sets the globalPrefix.
   * @param globalPrefix  the globalPrefix
   */
  public void setGlobalPrefix(final String globalPrefix) {
    _globalPrefix = globalPrefix;
  }

  /**
   * Gets the writeThrough.
   * @return the writeThrough
   */
  public boolean isWriteThrough() {
    return _writeThrough;
  }

  /**
   * Sets the writeThrough.
   * @param writeThrough  the writeThrough
   */
  public void setWriteThrough(final boolean writeThrough) {
    _writeThrough = writeThrough;
  }

  @Override
  public LastKnownValueStore newInstance(final ExternalId security, final String normalizationRuleSetId) {
    initIfNecessary();
    final String redisKey = generateRedisKey(security, normalizationRuleSetId);
    LOGGER.debug("Creating Redis LKV store on {}/{} with key name {}", new Object[] {security, normalizationRuleSetId, redisKey});
    updateIdentifiers(security);
    final RedisLastKnownValueStore store = new RedisLastKnownValueStore(_jedisPool, redisKey, isWriteThrough());
    return store;
  }

  /**
   * @param security
   * @param normalizationRuleSetId
   * @return
   */
  private String generateRedisKey(final ExternalId security, final String normalizationRuleSetId) {
    final StringBuilder sb = new StringBuilder();
    if (getGlobalPrefix() != null) {
      sb.append(getGlobalPrefix());
    }
    sb.append(security.getScheme().getName());
    sb.append("-");
    sb.append(security.getValue());
    sb.append("[");
    sb.append(normalizationRuleSetId);
    sb.append("]");
    return sb.toString();
  }

  private String generateAllSchemesKey() {
    final StringBuilder sb = new StringBuilder();
    if (getGlobalPrefix() != null) {
      sb.append(getGlobalPrefix());
    }
    sb.append("-<ALL_SCHEMES>");
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
    return sb.toString();
  }

  protected void initIfNecessary() {
    if (_isInitialized) {
      return;
    }
    synchronized (this) {
      assert _jedisPool == null;
      LOGGER.info("Connecting to {}:{}. Write-through set to: {}", new Object[] {getServer(), getPort(), _writeThrough});
      final JedisPoolConfig poolConfig = new JedisPoolConfig();
      //poolConfig.set...
      final JedisPool pool = new JedisPool(poolConfig, getServer(), getPort());
      _jedisPool = pool;

      _isInitialized = true;
    }
  }

  protected void updateIdentifiers(final ExternalId security) {
    final Jedis jedis = _jedisPool.getResource();
    jedis.sadd(generateAllSchemesKey(), security.getScheme().getName());
    jedis.sadd(generatePerSchemeKey(security.getScheme().getName()), security.getValue());
    _jedisPool.close();
  }

  @Override
  public Set<String> getAllIdentifiers(final String identifierScheme) {
    initIfNecessary();
    final Jedis jedis = _jedisPool.getResource();
    final Set<String> allMembers = jedis.smembers(generatePerSchemeKey(identifierScheme));
    _jedisPool.close();
    LOGGER.info("Loaded {} identifiers from Jedis (full contents in Debug level log)", allMembers.size());
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Loaded identifiers from Jedis: {}", allMembers);
    }
    return allMembers;
  }

  @Override
  public boolean isAvailable(final ExternalId security, final String normalizationRuleSetId) {
    initIfNecessary();
    final String redisKey = generateRedisKey(security, normalizationRuleSetId);
    final Jedis jedis = _jedisPool.getResource();
    final boolean isAvailable = jedis.exists(redisKey);
    _jedisPool.close();
    return isAvailable;
  }

}
