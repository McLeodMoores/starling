/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Proxies on top of another {@link HolidaySource} and converts the schemes of all
 * identifiers requested from one scheme to another, but <em>ONLY</em> for the
 * {@code isHoliday} calls. For all other requests, the underlying is invoked
 * unmodified.
 * <strong>This class should only be used in conjunction with HolidaySourceCalendarAdapter</strong>.
 */
@SuppressWarnings("deprecation")
public class SchemeAlteringHolidaySource implements HolidaySource {
  private final HolidaySource _underlying;
  private final Map<String, String> _schemeMappings = new ConcurrentHashMap<>();

  /**
   * Constructs a source.
   *
   * @param underlying  the underlying source, not null
   */
  public SchemeAlteringHolidaySource(final HolidaySource underlying) {
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
  }

  /**
   * Adds a scheme mapping.
   *
   * @param sourceScheme  the source scheme, not null
   * @param targetScheme  the target shceme, not null
   */
  public void addMapping(final String sourceScheme, final String targetScheme) {
    ArgumentChecker.notNull(sourceScheme, "sourceScheme");
    ArgumentChecker.notNull(targetScheme, "targetScheme");
    _schemeMappings.put(sourceScheme, targetScheme);
  }

  /**
   * Gets the underlying.
   * @return the underlying
   */
  public HolidaySource getUnderlying() {
    return _underlying;
  }

  /**
   * Returns the scheme mapping or the input, if no mapping is available.
   *
   * @param scheme  the scheme to map
   * @return  the mapping or the input
   */
  protected String translateScheme(final String scheme) {
    String result = _schemeMappings.get(scheme);
    if (result == null) {
      result = scheme;
    }
    return result;
  }

  /**
   * Replaces the scheme of an external id.
   *
   * @param externalId  the identifier
   * @return  an identifier with the mapped scheme
   */
  protected ExternalId translateExternalId(final ExternalId externalId) {
    final String newScheme = translateScheme(externalId.getScheme().getName());
    return ExternalId.of(newScheme, externalId.getValue());
  }

  @Override
  public Holiday get(final UniqueId uniqueId) {
    return getUnderlying().get(uniqueId);
  }

  @Override
  public Holiday get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return getUnderlying().get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, Holiday> get(final Collection<UniqueId> uniqueIds) {
    return getUnderlying().get(uniqueIds);
  }

  @Override
  public Map<ObjectId, Holiday> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    return getUnderlying().get(objectIds, versionCorrection);
  }

  @Override
  public Collection<Holiday> get(final HolidayType holidayType,
                                 final ExternalIdBundle regionOrExchangeIds) {
    return getUnderlying().get(holidayType, regionOrExchangeIds);
  }

  @Override
  public Collection<Holiday> get(final Currency currency) {
    return getUnderlying().get(currency);
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    return getUnderlying().isHoliday(dateToCheck, currency);
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    ArgumentChecker.notNull(regionOrExchangeIds, "regionOrExchangeIds");

    final Set<ExternalId> translatedIds = new HashSet<>();
    for (final ExternalId externalId : regionOrExchangeIds.getExternalIds()) {
      final ExternalId translatedId = translateExternalId(externalId);
      translatedIds.add(translatedId);
    }
    final ExternalIdBundle translatedBundle = ExternalIdBundle.of(translatedIds);

    return getUnderlying().isHoliday(dateToCheck, holidayType, translatedBundle);
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    ArgumentChecker.notNull(regionOrExchangeId, "regionOrExchangeId");
    final ExternalId translatedId = translateExternalId(regionOrExchangeId);
    return getUnderlying().isHoliday(dateToCheck, holidayType, translatedId);
  }

}
