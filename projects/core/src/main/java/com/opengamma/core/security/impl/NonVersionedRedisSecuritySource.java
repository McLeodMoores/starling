/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Charsets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.metric.OpenGammaMetricRegistry;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/*
 * REDIS DATA STRUCTURES:
 * UniqueIds for each ExternalId:
 *     Key["EXT-"ExternalId] -> Set[UniqueId]
 * Data for a particular security by UniqueId:
 *     Key["UNQ-"UniqueId] -> Hash
 *       Hash["DATA"] -> Fudge encoded security document
 *
 * While this data structure is more than necessary (in that you could cut out the hash for
 * the security data), it allows future expansion if more data is required to be stored
 * later without reformatting the Redis instance.
 *
 */

/**
 * A lightweight {@link SecuritySource} that cannot handle any versioning, and
 * which stores all Security documents as a Fudge-encoded BLOB in Redis as a
 * backing store.
 */
public class NonVersionedRedisSecuritySource implements SecuritySource {
  private static final Logger LOGGER = LoggerFactory.getLogger(NonVersionedRedisSecuritySource.class);
  private final JedisPool _jedisPool;
  private final FudgeContext _fudgeContext;
  private final String _redisPrefix;
  private Timer _getTimer = new Timer();
  private Timer _putTimer = new Timer();

  private static final byte[] DATA_NAME_AS_BYTES = "DATA".getBytes(Charsets.UTF_8);
  private static final byte[] CLASS_NAME_AS_BYTES = "CLASS".getBytes(Charsets.UTF_8);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "RedisSec";

  public NonVersionedRedisSecuritySource(final JedisPool jedisPool) {
    this(jedisPool, "");
  }

  public NonVersionedRedisSecuritySource(final JedisPool jedisPool, final String redisPrefix) {
    this(jedisPool, redisPrefix, OpenGammaFudgeContext.getInstance());
  }

  public NonVersionedRedisSecuritySource(final JedisPool jedisPool, final String redisPrefix, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(jedisPool, "jedisPool");
    ArgumentChecker.notNull(redisPrefix, "redisPrefix");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");

    _jedisPool = jedisPool;
    _redisPrefix = redisPrefix;
    _fudgeContext = fudgeContext;
    registerMetrics(OpenGammaMetricRegistry.getSummaryInstance(), OpenGammaMetricRegistry.getDetailedInstance(), "NonVersionedRedisSecuritySource");
  }

  /**
   * Gets the jedisPool.
   * @return the jedisPool
   */
  protected JedisPool getJedisPool() {
    return _jedisPool;
  }

  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the redisPrefix.
   * @return the redisPrefix
   */
  protected String getRedisPrefix() {
    return _redisPrefix;
  }

  public void registerMetrics(final MetricRegistry summaryRegistry, final MetricRegistry detailRegistry, final String namePrefix) {
    _getTimer = summaryRegistry.timer(namePrefix + ".get");
    _putTimer = summaryRegistry.timer(namePrefix + ".put");
  }


  // ---------------------------------------------------------------------
  // REDIS KEY MANAGEMENT
  // ---------------------------------------------------------------------

  protected byte[] toRedisKey(final UniqueId uniqueId) {
    final StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append("UNQ-");
    sb.append(uniqueId);
    final String keyText = sb.toString();
    final byte[] bytes = keyText.getBytes(Charsets.UTF_8);
    return bytes;
  }

  protected byte[] toRedisKey(final ObjectId objectId) {
    return toRedisKey(UniqueId.of(objectId, null));
  }

  protected String toRedisKey(final ExternalId externalId) {
    final StringBuilder sb = new StringBuilder();
    if (!getRedisPrefix().isEmpty()) {
      sb.append(getRedisPrefix());
      sb.append("-");
    }
    sb.append("EXT-");
    sb.append(externalId);
    return sb.toString();
  }

  // ---------------------------------------------------------------------
  // DATA SETTING/UPDATING OPERATIONS
  // UNIQUE TO THIS CLASS
  // ---------------------------------------------------------------------

  public UniqueId put(final Security security) {
    ArgumentChecker.notNull(security, "security");
    //ArgumentChecker.notNull(security.getUniqueId(), "security uniqueId");

    UniqueId uniqueId = security.getUniqueId();
    if (uniqueId == null) {
      uniqueId = UniqueId.of(IDENTIFIER_SCHEME_DEFAULT, GUIDGenerator.generate().toString());
    }
    if (uniqueId.getVersion() != null) {
      uniqueId = UniqueId.of(uniqueId.getObjectId(), null);
    }
    if (security instanceof MutableUniqueIdentifiable) {
      final MutableUniqueIdentifiable mutableSecurity = (MutableUniqueIdentifiable) security;
      mutableSecurity.setUniqueId(uniqueId);
    }

    try (Timer.Context context = _putTimer.time()) {
      final byte[] securityData = SecurityFudgeUtil.convertToFudge(getFudgeContext(), security);

      final Jedis jedis = getJedisPool().getResource();
      try {

        for (final ExternalId externalId : security.getExternalIdBundle()) {
          final String redisKey = toRedisKey(externalId);

          jedis.sadd(redisKey, uniqueId.toString());
          if (jedis.scard(redisKey) > 1) {
            LOGGER.warn("Multiple securities with same ExternalId {}. Probable misuse.", externalId);
          }
        }

        final byte[] redisKey = toRedisKey(uniqueId);
        jedis.hset(redisKey, DATA_NAME_AS_BYTES, securityData);
        jedis.hset(redisKey, CLASS_NAME_AS_BYTES, security.getClass().getName().getBytes(Charsets.UTF_8));

        getJedisPool().returnResource(jedis);
      } catch (final Exception e) {
        LOGGER.error("Unable to put security " + security, e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to put security " + security, e);
      }

    }
    return uniqueId;
  }

  // ---------------------------------------------------------------------
  // IMPLEMENTATION OF SECURITYSOURCE
  // ---------------------------------------------------------------------

  private interface GetWorker<T> {
    T query(Jedis jedis);
  }

  protected <T> T executeGet(final GetWorker<T> getWorker) {
    try (Timer.Context context = _getTimer.time()) {
      final Jedis jedis = getJedisPool().getResource();

      T result = null;
      try {
        result = getWorker.query(jedis);
        getJedisPool().returnResource(jedis);
      } catch (final Exception e) {
        LOGGER.error("Unable to execute get", e);
        getJedisPool().returnBrokenResource(jedis);
        throw new OpenGammaRuntimeException("Unable to execute get()", e);
      }

      return result;
    }
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    return get(bundle);
  }

  @Override
  public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Collection<Security>> result = new HashMap<>();

    for (final ExternalIdBundle bundle : bundles) {
      result.put(bundle, get(bundle));
    }

    return result;
  }

  @Override
  public Collection<Security> get(final ExternalIdBundle bundle) {
    final Security security = getSingle(bundle);
    if (security == null) {
      return Collections.emptySet();
    } else {
      return Collections.singleton(security);
    }
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");

    if (bundle.size() != 1) {
      LOGGER.warn("Possible bad use of NonVersionedRedisSecuritySource: bundle size {} not equal to 1.", bundle);
    }

    final ExternalId externalId = bundle.iterator().next();
    final Security result = executeGet(new GetWorker<Security>() {
      @Override
      public Security query(final Jedis jedis) {
        final Set<String> uniqueIds = jedis.smembers(toRedisKey(externalId));
        if (uniqueIds.isEmpty()) {
          return null;
        }
        if (uniqueIds.size() > 1) {
          LOGGER.info("Following unique IDs for externalId {} : {}. Choosing randomly.", externalId, uniqueIds);
        }
        final UniqueId uniqueId = UniqueId.parse(uniqueIds.iterator().next());

        return getInJedis(jedis, uniqueId);
      }

    });
    return result;
  }

  @Override
  public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
    return getSingle(bundle);
  }

  @Override
  public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
    final Map<ExternalIdBundle, Security> result = new HashMap<>();

    for (final ExternalIdBundle bundle : bundles) {
      final Security security = getSingle(bundle);
      result.put(bundle, security);
    }

    return result;
  }

  @Override
  public Security get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");

    final Security result = executeGet(new GetWorker<Security>() {
      @Override
      public Security query(final Jedis jedis) {
        return getInJedis(jedis, uniqueId);
      }
    });
    return result;
  }

  @Override
  public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");

    final Security result = executeGet(new GetWorker<Security>() {
      @Override
      public Security query(final Jedis jedis) {
        return getInJedis(jedis, UniqueId.of(objectId, null));
      }
    });
    return result;
  }

  @Override
  public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, Security> result = new HashMap<>();

    for (final UniqueId uniqueId : uniqueIds) {
      result.put(uniqueId, get(uniqueId));
    }

    return result;
  }

  @Override
  public Map<ObjectId, Security> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    final Map<ObjectId, Security> result = new HashMap<>();

    for (final ObjectId objectId : objectIds) {
      result.put(objectId, get(UniqueId.of(objectId, null)));
    }

    return result;
  }

  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  protected Security getInJedis(final Jedis jedis, final UniqueId uniqueId) {
    final byte[] redisKey = toRedisKey(uniqueId);
    final byte[] securityData = jedis.hget(redisKey, DATA_NAME_AS_BYTES);
    final byte[] classNameData = jedis.hget(redisKey, CLASS_NAME_AS_BYTES);
    if (securityData == null) {
      LOGGER.warn("No data for security unique ID {}", uniqueId);
      return null;
    } else {
      final String className = Charsets.UTF_8.decode(ByteBuffer.wrap(classNameData)).toString();
      Security security = null;
      try {
        security = SecurityFudgeUtil.convertFromFudge(getFudgeContext(), className, securityData);
      } catch (final Exception ex) {
        LOGGER.warn("Unable to convert from fudge for security unique ID " + uniqueId, ex);
      }
      return security;
    }

  }

}
