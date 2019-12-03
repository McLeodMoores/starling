/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import static com.opengamma.util.ehcache.EHCacheUtils.putException;
import static com.opengamma.util.ehcache.EHCacheUtils.putValue;

import java.util.Arrays;
import java.util.Collection;

import org.threeten.bp.LocalDate;

import com.opengamma.core.AbstractEHCachingSource;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.holiday.ChangeHolidaySource;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.money.Currency;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * An EHCache based {@link HolidaySource}.
 * <p>
 * This source does not cache the latest version / correction of a holiday. It also caches individual dates that
 * are a holiday in a particular holiday configuration - this is incorrect behaviour and will be removed at
 * some point in the future.
 * <p>
 * This source tracks changes only if the underlying source is a {@link ChangeHolidaySource}.
 */
public class EHCachingHolidaySource extends AbstractEHCachingSource<Holiday, HolidaySource> implements HolidaySource {
  /** The holiday object cache name. */
  /*package*/static final String CACHE_NAME = "holiday";
  private static final String DATE_CACHE_NAME = "date-cache";
  private final Cache _cache;
  private final Cache _dateCache;

  /**
   * Constructs a source.
   *
   * @param underlying  the underlying source
   * @param cacheManager  the cache manager
   */
  public EHCachingHolidaySource(final HolidaySource underlying, final CacheManager cacheManager) {
    super(underlying, cacheManager);
    EHCacheUtils.addCache(cacheManager, CACHE_NAME);
    EHCacheUtils.addCache(cacheManager, DATE_CACHE_NAME);
    _cache = EHCacheUtils.getCacheFromManager(cacheManager, CACHE_NAME);
    _dateCache = EHCacheUtils.getCacheFromManager(cacheManager, DATE_CACHE_NAME);
    if (getUnderlying() instanceof ChangeHolidaySource) {
      final ChangeHolidaySource changeUnderlying = (ChangeHolidaySource) getUnderlying();
      // this is not nice, but it's better than a stale cache.
      changeUnderlying.changeManager().addChangeListener(new ChangeListener() {
        @Override
        public void entityChanged(final ChangeEvent event) {
          switch (event.getType()) {
            case ADDED:
              break;
            case CHANGED:
              _cache.flush();
              _dateCache.flush();
              flush();
              break;
            case REMOVED:
              _cache.flush();
              _dateCache.flush();
              flush();
              break;
            default:
              break;
          }
        }
      });
    }
  }

  /**
   * Gets the cache containing holiday objects.
   *
   * @return  the cache
   */
  protected Cache getCache() {
    return _cache;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<Holiday> get(final HolidayType holidayType,
                                 final ExternalIdBundle regionOrExchangeIds) {
    final Object key = Arrays.asList(holidayType, regionOrExchangeIds);
    final Element e = _cache.get(key);
    if (e != null) {
      return (Collection<Holiday>) EHCacheUtils.get(e);
    }
    try {
      return putValue(key, getUnderlying().get(holidayType, regionOrExchangeIds), _cache);
    } catch (final RuntimeException ex) {
      return (Collection<Holiday>) putException(key, ex, _cache);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<Holiday> get(final Currency currency) {
    final Element e = _cache.get(currency);
    if (e != null) {
      return (Collection<Holiday>) EHCacheUtils.get(e);
    }
    try {
      return putValue(currency, getUnderlying().get(currency), _cache);
    } catch (final RuntimeException ex) {
      return (Collection<Holiday>) putException(currency, ex, _cache);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    final Object key = Arrays.asList(dateToCheck, currency);
    final Element e = _dateCache.get(key);
    if (e != null) {
      return (Boolean) EHCacheUtils.get(e);
    }
    try {
      return putValue(key, getUnderlying().isHoliday(dateToCheck, currency), _dateCache);
    } catch (final RuntimeException ex) {
      return (Boolean) putException(key, ex, _dateCache);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    final Object key = Arrays.asList(dateToCheck, holidayType, regionOrExchangeIds);
    final Element e = _dateCache.get(key);
    if (e != null) {
      return (Boolean) EHCacheUtils.get(e);
    }
    try {
      return putValue(key, getUnderlying().isHoliday(dateToCheck, holidayType, regionOrExchangeIds), _dateCache);
    } catch (final RuntimeException ex) {
      return (Boolean) putException(key, ex, _dateCache);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
    final Object key = Arrays.asList(dateToCheck, holidayType, regionOrExchangeId);
    final Element e = _dateCache.get(key);
    if (e != null) {
      return (Boolean) EHCacheUtils.get(e);
    }
    try {
      return putValue(key, getUnderlying().isHoliday(dateToCheck, holidayType, regionOrExchangeId), _dateCache);
    } catch (final RuntimeException ex) {
      return (Boolean) putException(key, ex, _dateCache);
    }
  }

  /**
   * Call this at the end of a unit test run to clear the state of EHCache. It should not be part of a generic lifecycle method.
   */
  @Override
  public void shutdown() {
    super.shutdown();
    _cache.getCacheManager().removeCache(CACHE_NAME);
    _dateCache.getCacheManager().removeCache(DATE_CACHE_NAME);
  }

}
