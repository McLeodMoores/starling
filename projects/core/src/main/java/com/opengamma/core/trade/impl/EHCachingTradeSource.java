/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.trade.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.apache.shiro.crypto.AesCipherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.trade.Trade;
import com.opengamma.core.trade.TradeSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.id.VersionCorrectionUtils;
import com.opengamma.id.VersionCorrectionUtils.VersionCorrectionLockListener;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.WeakInstanceCache;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.map.HashMap2;
import com.opengamma.util.map.Map2;
import com.opengamma.util.map.WeakValueHashMap2;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * A cache decorating a {@code PositionSource}.
 * <p>
 * The cache is implemented using {@code EHCache}.
 * <p>
 * Any requests with a "latest" version/correction or unversioned unique identifier are not cached and will always hit the underlying.
 * This should not be an issue in practice as the engine components which use the position source will always specify an exact
 * version/correction and versioned unique identifiers.
 */
public class EHCachingTradeSource implements TradeSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(EHCachingTradeSource.class);

  /**
   * Cache key for trades.
   */
  private static final String TRADE_CACHE = "trade";

  /**
   * The trade cache.
   */
  private final Cache _tradeCache;

  private final ConcurrentMap<UniqueId, Object> _frontTradeCache = new MapMaker().weakValues().makeMap();
  private final Map2<VersionCorrection, UniqueId, Object> _frontCacheByUID = new WeakValueHashMap2<>(HashMap2.STRONG_KEYS);
  private final Map2<VersionCorrection, ObjectId, Object> _frontCacheByOID = new WeakValueHashMap2<>(HashMap2.STRONG_KEYS);

  private final VersionCorrectionLockListener _frontCacheCleaner = new VersionCorrectionLockListener() {
    @Override
    public void versionCorrectionUnlocked(final VersionCorrection unlocked, final Collection<VersionCorrection> stillLocked) {
      _frontCacheByUID.retainAllKey1(stillLocked);
      _frontCacheByOID.retainAllKey1(stillLocked);
    }
  };

  private final CacheManager _cacheManager;
  private final TradeSource _underlying;

  /**
   * Creates the cache around an underlying position source.
   *
   * @param underlying the underlying data, not null
   * @param cacheManager the cache manager, not null
   */
  public EHCachingTradeSource(final TradeSource underlying, final CacheManager cacheManager) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _underlying = underlying;
    _cacheManager = cacheManager;
    EHCacheUtils.addCache(cacheManager, TRADE_CACHE);
    _tradeCache = EHCacheUtils.getCacheFromManager(cacheManager, TRADE_CACHE);
    VersionCorrectionUtils.addVersionCorrectionLockListener(_frontCacheCleaner);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying source of positions.
   *
   * @return the underlying source of positions, not null
   */
  protected TradeSource getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the cache manager.
   *
   * @return the cache manager, not null
   */
  protected CacheManager getCacheManager() {
    return _cacheManager;
  }

  protected Trade addToFrontCache(final Trade trade, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(trade, "trade");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(trade.getUniqueId(), "trade.getUniqueId");
    Trade t = (Trade) _frontTradeCache.putIfAbsent(trade.getUniqueId(), trade);
    _frontCacheByOID.put(versionCorrection, trade.getUniqueId().getObjectId(), t);
    return t;
  }

  @Override
  public Trade getTrade(final UniqueId uniqueId) {
    Object f;
    if (uniqueId.isVersioned()) {
      f = _frontTradeCache.get(uniqueId);
      if (f instanceof Trade) {
        LOGGER.debug("getTradeByUniqueId: Front cache hit on {}", uniqueId);
        return (Trade) f;
      }
      final Element e = _tradeCache.get(uniqueId);
      if (e != null) {
        LOGGER.debug("getTradeByUniqueId: EHCache hit on {}", uniqueId);
        final Trade trade = (Trade) e.getObjectValue();
        f = _frontTradeCache.putIfAbsent(uniqueId, trade);
        if (f instanceof Trade) {
          LOGGER.debug("getTradeByUniqueId: Late front cache hit on {}", uniqueId);
          return (Trade) f;
        }
        return trade;
      }
    } else {
      LOGGER.debug("getPositionByUniqueId: Pass through on {}", uniqueId);
    }
    final Trade trade = getUnderlying().getTrade(uniqueId);
    f = _frontTradeCache.putIfAbsent(trade.getUniqueId(), trade);
    if (f instanceof Trade) {
      LOGGER.debug("getPositionByUniqueId: Late front cache hit on {}", uniqueId);
      return (Trade) f;
    }
    _tradeCache.put(new Element(trade.getUniqueId(), trade));
    return trade;
  }

  @Override
  public Trade getTrade(final ObjectId tradeId, final VersionCorrection versionCorrection) {
    if (versionCorrection.containsLatest()) {
      LOGGER.debug("getTradeByObjectId: Skipping cache for {}/{}", tradeId, versionCorrection);
      return getUnderlying().getTrade(tradeId, versionCorrection);
    }
    Object t = _frontCacheByOID.get(versionCorrection, tradeId);
    if (t instanceof Trade) {
      LOGGER.debug("getTradeByObjectId: Front cache hit on {}/{}", tradeId, versionCorrection);
      return (Trade) t;
    }
    final Pair<ObjectId, VersionCorrection> key = Pairs.of(tradeId, versionCorrection);
    final Element e = _tradeCache.get(key);
    if (e != null) {
      LOGGER.debug("getTradeByObjectId: EHCache hit on {}/{}", tradeId, versionCorrection);
      final Trade trade = (Trade) e.getObjectValue();
      t = _frontCacheByOID.putIfAbsent(versionCorrection, tradeId, trade);
      if (t instanceof Trade) {
        LOGGER.debug("getTradeByObjectId: Late front cache hit on {}/{}", tradeId, versionCorrection);
        return (Trade) t;
      }
      return trade;
    }
    LOGGER.debug("getTradeByObjectId: Cache miss on {}/{}", tradeId, versionCorrection);
    final Trade trade = getUnderlying().getTrade(tradeId, versionCorrection);
    t = _frontTradeCache.putIfAbsent(trade.getUniqueId(), trade);
    if (t instanceof Trade) {
      LOGGER.debug("getTradeByObjectId: Late front cache hit on {}/{}", tradeId, versionCorrection);
      _frontCacheByOID.put(versionCorrection, tradeId, t);
      return (Trade) t;
    }
    _frontCacheByOID.put(versionCorrection, tradeId, trade);
    _tradeCache.put(new Element(key, trade));
    _tradeCache.put(new Element(trade.getUniqueId(), trade));
    return trade;
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  protected void shutdown() {
    _cacheManager.removeCache(TRADE_CACHE);
    _frontCacheByUID.clear();
    _frontCacheByOID.clear();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getUnderlying() + "]";
  }

}
