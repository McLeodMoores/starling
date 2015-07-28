/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.master.holiday.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.WeekendTypeProvider;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * A cache to optimize the results of {@code MasterHolidaySource}.
 * <p>
 * Previously, this source hard-coded the weekend to be Saturday and Sunday. Now the holiday is checked and if it is a
 * {@link WeekendTypeProvider}, the provider is used to determine if a day is a weekend. If this information is not available
 * from a holiday, the previous hard-coding is used.
 */
public class EHCachingMasterHolidaySource extends MasterHolidaySource {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(EHCachingMasterHolidaySource.class);
  /**
   * Cache key for holidays.
   */
  /*pacakge*/ static final String HOLIDAY_CACHE = "holiday";

  /**
   * The result cache.
   */
  private final Cache _holidayCache;

  /**
   * Creates the cache around an underlying holiday source.
   *
   * @param underlying  the underlying data, not null
   * @param cacheManager  the cache manager, not null
   */
  public EHCachingMasterHolidaySource(final HolidayMaster underlying, final CacheManager cacheManager) {
    super(underlying);

    LOGGER.warn("EHCache doesn't perform well here (see PLAT-1015)");

    ArgumentChecker.notNull(cacheManager, "cacheManager");
    EHCacheUtils.addCache(cacheManager, HOLIDAY_CACHE);
    _holidayCache = EHCacheUtils.getCacheFromManager(cacheManager, HOLIDAY_CACHE);
  }

  //-------------------------------------------------------------------------
  @Override
  protected boolean isHoliday(final HolidaySearchRequest request, final LocalDate dateToCheck) {
    final Element e = _holidayCache.get(request);
    final HolidayDocument doc;
    if (e != null) {
      doc = (HolidayDocument) e.getObjectValue();
    } else {
      doc = getMaster().search(request).getFirstDocument();
      final Element element = new Element(request, doc);
      element.setTimeToLive(10); // TODO PLAT-1308: I've set TTL short to hide the fact that we return stale data
      _holidayCache.put(element);
    }
    // TODO can this be null?
    final Holiday holiday = doc.getHoliday();
    if (holiday instanceof WeekendTypeProvider) {
      if (((WeekendTypeProvider) holiday).getWeekendType().isWeekend(dateToCheck)) {
        return true;
      }
    } else {
      if (isWeekend(dateToCheck)) {
        return true;
      }
    }
    return isHoliday(doc, dateToCheck);
  }

}
