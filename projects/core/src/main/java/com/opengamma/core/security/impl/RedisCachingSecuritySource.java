/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.security.AbstractSecuritySource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

// TODO kirk 2013-04-16 -- Redis allows TTL to be set on values.
// To match a typical cache, we should give the option to set that.
// Note that as unique id lookups never change, we'd probably need a different
// TTL on the unique ID lookups from any other form of ID lookup.
/**
 * <em>DO NOT USE THIS CLASS</em>. This class is a work in progress and cannot be used in its current state.
 * <p>
 * A caching {@link SecuritySource} which is only capable of satisfying certain very specific calls. It is <em>not</em> intended to be a general purpose cache.
 * <p>
 * <strong>This class is a work in progress and is <em>NOT</em> production capable. The javadocs below are for indication of expected future functionality when
 * fully completed.</strong>
 * <p>
 * While the results of other calls will be used to populate the cache, only three calls can be satisfied from the cache:
 * <ul>
 * <li>{@link #get(UniqueId)}</li>
 * <li>{@link #get(ExternalIdBundle)}</li>
 * <li>{@link #get(ExternalIdBundle, VersionCorrection)}</li>
 * </ul>
 * <p>
 * In addition, this implementation <strong>does not support {@link ExternalId} changes</strong>. While {@link #get(UniqueId)} by definition can always be
 * cached, because a {@link Security} never changes within a particular unique identifier, external identifiers can change over time. <em>This implementation
 * may return incorrect results if used in an environment where external identifiers <strong>that are used for lookups</strong> are used.</em>
 * <p>
 * This fundamentally limits the utility of this source to the the following conditions:
 * <ul>
 * <li>Identifier changes (such as ticker rolls or corporate actions) happen as part of a maintenance window, during which time the Redis cache is cleared as
 * well; and/or</li>
 * <li>The only external identifiers used for lookups are ones that will never change (because they are surrogate keys into an existing system that guarantees
 * consistency and uniqueness over time).</li>
 * </ul>
 * <p>
 * Where there are multiple instances of the same {@code RedisCachingSecuritySource} being pointed at the same repository (given as a combination of the same
 * pool and same prefix), by default, all instances will attempt to update the Redis instance, which is not ideal.
 */
public class RedisCachingSecuritySource extends AbstractSecuritySource {
  private static final Logger LOGGER = LoggerFactory.getLogger(RedisCachingSecuritySource.class);
  private final SecuritySource _underlying;
  private final JedisPool _jedisPool;
  private final String _redisPrefix;
  private final FudgeContext _fudgeContext;
  private final Set<UniqueId> _knownInRedis = new HashSet<>();

  // REVIEW kirk 2013-04-17 -- It's really not clear at all that any of the locking
  // is necessary or desirable at all. Since we're not actually holding any state,
  // and the underlying source would synchronize anything else, it's really not clear
  // that we're getting any advantage out of the locking.
  // That being said, I've left in the locking logic for now until we determine
  // whether it's desirable.

  private final ReadWriteLock _lock = new ReentrantReadWriteLock();

  public RedisCachingSecuritySource(final SecuritySource underlying, final JedisPool jedisPool) {
    this(underlying, jedisPool, "");
  }

  public RedisCachingSecuritySource(final SecuritySource underlying, final JedisPool jedisPool, final String redisPrefix) {
    this(underlying, jedisPool, redisPrefix, OpenGammaFudgeContext.getInstance());
  }

  public RedisCachingSecuritySource(final SecuritySource underlying, final JedisPool jedisPool, final String redisPrefix, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(jedisPool, "jedisPool");
    ArgumentChecker.notNull(redisPrefix, "redisPrefix");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _jedisPool = jedisPool;
    _redisPrefix = redisPrefix;
    _fudgeContext = fudgeContext;
  }

  /**
   * Gets the underlying.
   * @return the underlying
   */
  protected SecuritySource getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the jedisPool.
   * @return the jedisPool
   */
  protected JedisPool getJedisPool() {
    return _jedisPool;
  }

  /**
   * Gets the redisPrefix.
   * @return the redisPrefix
   */
  protected String getRedisPrefix() {
    return _redisPrefix;
  }

  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    final Collection<Security> results = getUnderlying().get(bundle, versionCorrection);
    processResults(results);
    return results;
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle) {
    final Collection<Security> results = getUnderlying().get(bundle);
    processResults(results);
    return results;
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle) {
    final Security result = getUnderlying().getSingle(bundle);
    processResult(result);
    return result;
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    final Security result = getUnderlying().getSingle(bundle, versionCorrection);
    processResult(result);
    return result;
  }

  @Override
  public Security get(final UniqueId uniqueId) {
    Security security = getFromRedis(uniqueId);
    if (security == null) {
      LOGGER.warn("Unable to satisfy {} using Redis", uniqueId);
      security = getUnderlying().get(uniqueId);
      processResult(security);
    } else {
      LOGGER.warn("Satisfied {} using Redis", uniqueId);
    }
    return security;
  }

  @Override
  public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    final Security result = getUnderlying().get(objectId, versionCorrection);
    processResult(result);
    return result;
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  protected Security getFromRedis(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final byte[] redisKey = toRedisKey(uniqueId);
    final Jedis jedis = getJedisPool().getResource();

    try {
      try {
        _lock.readLock().lock();

        final byte[] data = jedis.get(redisKey);
        if (data == null) {
          return null;
        }

        Security security = null;
        try {
          // REVIEW kirk 2013-06-05 -- This will definitely fail, but this class is a work in progress
          // and likely to never work in its current form.
          security = SecurityFudgeUtil.convertFromFudge(getFudgeContext(), null, data);
        } catch (final Exception e) {
          LOGGER.error("Unserializable data in Redis for uniqueId " + uniqueId + ". Clearing redis.", e);
          try {
            _lock.writeLock().lock();
            jedis.del(redisKey);
          } finally {
            _lock.writeLock().unlock();
          }
        }

        return security;
      } finally {
        _lock.readLock().unlock();
      }
    } finally {
      getJedisPool().close();
    }
  }

  protected void processResults(final Collection<Security> securities) {
    for (final Security security : securities) {
      processResult(security);
    }
  }

  protected void processResult(final Security security) {
    if (security == null) {
      // REVIEW kirk 2013-04-16 -- It may be desirable to cache the null result for optimization.
      // If so, this and getFromRedis() should be changed to match.
      return;
    }

    final byte[] redisKey = toRedisKey(security.getUniqueId());

    final Jedis jedis = getJedisPool().getResource();
    try {
      try {
        _lock.readLock().lock();

        if (_knownInRedis.contains(security.getUniqueId())) {
          // Already in the cache. Nothing to do here.
          // This may happen if it is being processed as a part of a collection getter.
          return;
        }

        if (jedis.exists(redisKey)) {
          // Already in the cache. Nothing to do here.
          // This may happen if it is being processed as a part of a collection getter.
          //LOGGER.warn("Not storing {} as already in Redis", security.getUniqueId());
          return;
        }

        LOGGER.warn("Storing security type {} id {} bundle {} to Redis",
            new Object[] {security.getSecurityType(), security.getUniqueId(), security.getExternalIdBundle()});
        final byte[] fudgeData = SecurityFudgeUtil.convertToFudge(getFudgeContext(), security);
        _lock.writeLock().lock();
        try {
          jedis.set(redisKey, fudgeData);
          processBundle(security.getExternalIdBundle(), security.getUniqueId().getObjectId(), jedis);
          //processObjectVersionToUniqueIdMap(security.getUniqueId(), jedis);
        } finally {
          _lock.writeLock().unlock();
        }
      } finally {
        _lock.readLock().unlock();
      }
    } finally {
      getJedisPool().close();
    }

  }

  /**
   * This should only be called when the write lock has been locked.
   * @param bundle bundle of the security
   * @param objectId id of the security
   * @param jedis open connection to Redis
   */
  protected void processBundle(final ExternalIdBundle bundle, final ObjectId objectId, final Jedis jedis) {
    final byte[] valueData = toRedisData(objectId);

    for (final ExternalId externalId: bundle) {
      final byte[] keyData = toRedisKey(externalId);
      jedis.sadd(keyData, valueData);
    }
  }

  private byte[] toRedisKey(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final String key = getRedisPrefix() + "U-" + uniqueId.toString();
    final byte[] bytes = Charsets.UTF_8.encode(key).array();
    return bytes;
  }

  private byte[] toRedisKey(final ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    final String key = getRedisPrefix() + "E-" + externalId.toString();
    final byte[] bytes = Charsets.UTF_8.encode(key).array();
    return bytes;
  }

  /*private byte[] toRedisKey(ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    String key = getRedisPrefix() + "O-" + objectId.toString();
    byte[] bytes = Charsets.UTF_8.encode(key).array();
    return bytes;
  }*/

  private static byte[] toRedisData(final ObjectId objectId) {
    ArgumentChecker.notNull(objectId, "objectId");
    final String data = objectId.toString();
    final byte[] bytes = Charsets.UTF_8.encode(data).array();
    return bytes;
  }

}
