/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.testutils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.threeten.bp.LocalDate;

import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * Simple mock holiday source for use in tests. The source is backed by a {@link ConcurrentHashMap} that maps
 * a {@link UniqueId} of the form (scheme, value + "~" + HolidayType) (e.g. (TestHol, "US~BANK")) to {@link Holiday}
 * and ignores {@link VersionCorrection}s unless an {@link ObjectId} is supplied.
 */
public class MockHolidaySource implements HolidaySource {
  /** The backing map */
  private final ConcurrentMap<UniqueId, Holiday> _holidays = new ConcurrentHashMap<>(new HashMap<UniqueId, Holiday>());

  /**
   * Adds a holiday to the source.
   * @param id  the holiday id, not null
   * @param holidayType  the holiday type, not null
   * @param holiday  the holiday, not null
   * @return  the previous holiday associated with this id, or null if there was no entry
   */
  public Holiday addHoliday(final ExternalId id, final HolidayType holidayType, final Holiday holiday) {
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(holidayType, "holidayType");
    ArgumentChecker.notNull(holiday, "holiday");
    return _holidays.putIfAbsent(createId(id.getScheme().getName(), id.getValue(), holidayType), holiday);
  }

  @Override
  public Holiday get(final UniqueId uniqueId) {
    return _holidays.get(uniqueId);
  }

  @Override
  public Map<UniqueId, Holiday> get(final Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, Holiday> holidays = new HashMap<>();
    for (final UniqueId uniqueId : uniqueIds) {
      final Holiday holiday = _holidays.get(uniqueId);
      if (holiday != null) {
        holidays.put(uniqueId, holiday);
      }
    }
    return holidays;
  }

  @Override
  public Holiday get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    return get(UniqueId.of(objectId, versionCorrection.toString()));
  }

  @Override
  public Map<ObjectId, Holiday> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    final Map<ObjectId, Holiday> holidays = new HashMap<>();
    for (final ObjectId objectId : objectIds) {
      final Holiday holiday = _holidays.get(UniqueId.of(objectId, versionCorrection.toString()));
      if (holiday != null) {
        holidays.put(objectId, holiday);
      }
    }
    return holidays;
  }

  @Override
  public Collection<Holiday> get(final Currency currency) {
    final UniqueId uniqueId = createId(currency.getUniqueId().getScheme(), currency.getUniqueId().getValue(), HolidayType.CURRENCY);
    return Collections.singleton(get(uniqueId));
  }

  @Override
  public Collection<Holiday> get(final HolidayType holidayType, final ExternalIdBundle externalIds) {
    final Collection<Holiday> holidays = new HashSet<>();
    for (final ExternalId id : externalIds) {
      final UniqueId uniqueId = createId(id.getScheme().getName(), id.getValue(), holidayType);
      final Holiday holiday = _holidays.get(uniqueId);
      if (holiday != null) {
        holidays.add(holiday);
      }
    }
    return holidays;
  }

  @Override
  public boolean isHoliday(final LocalDate date, final Currency currency) {
    switch (date.getDayOfWeek()) {
      case SATURDAY:
        return true;
      case SUNDAY:
        return true;
      default:
        final Collection<Holiday> holidays = get(currency);
        if (holidays == null) {
          throw new Quandl4OpenGammaRuntimeException("Could not get holiday for " + currency);
        }
        for (final Holiday holiday : holidays) {
          if (holiday.getHolidayDates().contains(date)) {
            return true;
          }
        }
        return false;
    }
  }

  @Override
  public boolean isHoliday(final LocalDate date, final HolidayType holidayType, final ExternalIdBundle externalIds) {
    switch (date.getDayOfWeek()) {
      case SATURDAY:
        return true;
      case SUNDAY:
        return true;
      default:
        final Collection<Holiday> holidays = get(holidayType, externalIds);
        if (holidays == null) {
          throw new Quandl4OpenGammaRuntimeException("Could not get holiday for with ids " + externalIds + " and holiday type " + holidayType);
        }
        for (final Holiday holiday : holidays) {
          if (holiday.getHolidayDates().contains(date)) {
            return true;
          }
        }
        return false;
    }
  }

  @Override
  public boolean isHoliday(final LocalDate date, final HolidayType holidayType, final ExternalId externalId) {
    return isHoliday(date, holidayType, externalId.toBundle());
  }

  /**
   * Creates the unique ids that are the keys used in the backing map.
   * @param schemeName  the scheme name
   * @param idValue  the id value
   * @param holidayType  the scheme value
   * @return  an id in the form used in the backing map
   */
  private static UniqueId createId(final String schemeName, final String idValue, final HolidayType holidayType) {
    return UniqueId.of(schemeName, idValue + "~" + holidayType.name());
  }


}
