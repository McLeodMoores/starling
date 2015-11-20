/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.HolidayWithWeekendAdapter;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.core.holiday.WeekendTypeProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A simple implementation of a holiday source intended for use in tests. Note that the weekend can be
 * explicitly provided when adding a holiday to the source or that the holiday itself can implement
 * {@link WeekendTypeProvider}. Otherwise, a {@link HolidayWithWeekendAdapter} will be stored that sets
 * the weekends to Saturday and Sunday if that option is set in the constructor.
 * <p>
 * This source does not support versioning. It is not thread-safe and so is not suitable for use in production.
 */
public class InMemoryHolidaySource implements HolidaySource {
  /** A map from identifier to holiday */
  private final Map<ObjectId, Holiday> _holidays = new HashMap<>();
  /** True if the holiday should hard-code weekends to be Saturday or Sunday if they are not set */
  private final boolean _setWeekends;
  /** Supplies unique identifiers */
  private final UniqueIdSupplier _uidSupplier;

  /**
   * Creates an empty holiday source that will set weekend days to be Saturday or Sunday if the
   * holiday does not explicitly set them.
   */
  public InMemoryHolidaySource() {
    _uidSupplier = new UniqueIdSupplier("Mock");
    _setWeekends = true;
  }

  /**
   * Creates an empty holiday source.
   * @param setWeekends  if true, sets any holidays without explicit weekend information to have
   * Saturday and Sunday as the weekend
   */
  public InMemoryHolidaySource(final boolean setWeekends) {
    _uidSupplier = new UniqueIdSupplier("Mock");
    _setWeekends = setWeekends;
  }

  @Override
  public Holiday get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final Holiday holiday = _holidays.get(uniqueId.getObjectId());
    if (holiday == null) {
      throw new DataNotFoundException("Holiday not found: " + uniqueId);
    }
    return holiday;
  }

  @Override
  public Holiday get(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final Holiday holiday = _holidays.get(objectId);
    if (holiday == null) {
      throw new DataNotFoundException("Holiday not found:" + objectId);
    }
    return holiday;
  }

  @Override
  public Map<UniqueId, Holiday> get(final Collection<UniqueId> uniqueIds) {
    ArgumentChecker.notNull(uniqueIds, "uniqueIds");
    final Map<UniqueId, Holiday> holidays = new HashMap<>();
    for (final UniqueId uniqueId : uniqueIds) {
      final Holiday holiday = _holidays.get(uniqueId.getObjectId());
      if (holiday != null) {
        holidays.put(uniqueId, holiday);
      }
    }
    return holidays;
  }

  @Override
  public Map<ObjectId, Holiday> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectIds, "objectIds");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final Map<ObjectId, Holiday> holidays = new HashMap<>();
    for (final ObjectId objectId : objectIds) {
      final Holiday holiday = _holidays.get(objectId);
      if (holiday != null) {
        holidays.put(objectId, holiday);
      }
    }
    return holidays;
  }

  @Override
  public Collection<Holiday> get(final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    ArgumentChecker.notNull(holidayType, "holidayType");
    ArgumentChecker.notNull(regionOrExchangeIds, "regionOrExchangeIds");
    ArgumentChecker.isFalse(holidayType == HolidayType.CURRENCY, "HolidayType cannot be CURRENCY");
    final Collection<Holiday> holidays = new HashSet<>();
    for (final ExternalId externalId : regionOrExchangeIds) {
      final Holiday holiday = _holidays.get(ObjectId.parse(externalId.toString()));
      if (holiday != null) {
        holidays.add(holiday);
      }
    }
    return holidays;
  }

  @Override
  public Collection<Holiday> get(final Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    final Collection<Holiday> holidays = new HashSet<>();
    final Holiday holiday = _holidays.get(currency.getObjectId());
    if (holiday != null) {
      ArgumentChecker.isTrue(holiday.getType() == HolidayType.CURRENCY, "HolidayType must be CURRENCY");
      holidays.add(holiday);
    }
    return holidays;
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final Currency currency) {
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    ArgumentChecker.notNull(currency, "currency");
    final Holiday holiday = _holidays.get(currency.getObjectId());
    if (holiday == null) {
      throw new DataNotFoundException("Holiday not found:" + currency);
    }
    ArgumentChecker.isTrue(holiday.getType() == HolidayType.CURRENCY, "HolidayType must be CURRENCY");
    if (holiday instanceof WeekendTypeProvider) {
      if (((WeekendTypeProvider) holiday).getWeekendType().isWeekend(dateToCheck)) {
        return true;
      }
    } else {
      if (WeekendType.SATURDAY_SUNDAY.isWeekend(dateToCheck)) {
        return true;
      }
    }
    return holiday.getHolidayDates().contains(dateToCheck);
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalIdBundle regionOrExchangeIds) {
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    ArgumentChecker.notNull(holidayType, "holidayType");
    ArgumentChecker.notNull(regionOrExchangeIds, "regionOrExchangeIds");
    ArgumentChecker.isFalse(holidayType == HolidayType.CURRENCY, "HolidayType cannot be CURRENCY");
    boolean anyHolidayFound = false;
    for (final ExternalId externalId : regionOrExchangeIds) {
      final Holiday holiday = _holidays.get(ObjectId.parse(externalId.toString()));
      if (holiday != null) {
        anyHolidayFound = true;
        if (holiday instanceof WeekendTypeProvider) {
          if (((WeekendTypeProvider) holiday).getWeekendType().isWeekend(dateToCheck)) {
            return true;
          }
        } else {
          if (WeekendType.SATURDAY_SUNDAY.isWeekend(dateToCheck)) {
            return true;
          }
        }
        final List<LocalDate> holidayDates = holiday.getHolidayDates();
        if (holidayDates.contains(dateToCheck)) {
          return true;
        }
      }
    }
    if (!anyHolidayFound) {
      throw new DataNotFoundException("No holidays found for " + regionOrExchangeIds);
    }
    return false;
  }

  @Override
  public boolean isHoliday(final LocalDate dateToCheck, final HolidayType holidayType, final ExternalId regionOrExchangeId) {
    ArgumentChecker.notNull(dateToCheck, "dateToCheck");
    ArgumentChecker.notNull(holidayType, "holidayType");
    ArgumentChecker.notNull(regionOrExchangeId, "regionOrExchangeId");
    final Collection<Holiday> holidays = get(holidayType, regionOrExchangeId.toBundle());
    if (holidays.isEmpty()) {
      throw new DataNotFoundException("No holidays found for " + regionOrExchangeId);
    }
    for (final Holiday holiday : holidays) {
      if (holiday instanceof WeekendTypeProvider) {
        if (((WeekendTypeProvider) holiday).getWeekendType().isWeekend(dateToCheck)) {
          return true;
        }
      } else {
        if (WeekendType.SATURDAY_SUNDAY.isWeekend(dateToCheck)) {
          return true;
        }
      }
      final List<LocalDate> holidayDates = holiday.getHolidayDates();
      if (holidayDates.contains(dateToCheck)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adds a holiday to the source. If the holiday is not a {@link WeekendTypeProvider}, weekends days will be set to Saturday and Sunday.
   * @param objectIdentifiable  the id, not null
   * @param holiday  the holiday, not null
   */
  public void addHoliday(final ObjectIdentifiable objectIdentifiable, final Holiday holiday) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    ArgumentChecker.notNull(holiday, "holiday");
    IdUtils.setInto(holiday, _uidSupplier.get());
    final ObjectId objectId = objectIdentifiable.getObjectId();
    if (holiday instanceof WeekendTypeProvider) {
      _holidays.put(objectId, holiday);
    } else {
      if (_setWeekends) {
        _holidays.put(objectId, new HolidayWithWeekendAdapter(holiday, WeekendType.SATURDAY_SUNDAY));
      } else {
        _holidays.put(objectId, holiday);
      }
    }
  }

  /**
   * Adds a holiday to the source. Weekends will be set to Saturday and Sunday by default.
   * @param externalIdentifiable  the id, not null
   * @param holiday  the holiday, not null
   */
  public void addHoliday(final ExternalIdentifiable externalIdentifiable, final Holiday holiday) {
    ArgumentChecker.notNull(externalIdentifiable, "externalIdentifiable");
    ArgumentChecker.notNull(holiday, "holiday");
    IdUtils.setInto(holiday, _uidSupplier.get());
    final ObjectId objectId = ObjectId.parse(externalIdentifiable.getExternalId().toString());
    if (holiday instanceof WeekendTypeProvider) {
      _holidays.put(objectId, holiday);
    } else {
      if (_setWeekends) {
        _holidays.put(objectId, new HolidayWithWeekendAdapter(holiday, WeekendType.SATURDAY_SUNDAY));
      } else {
        _holidays.put(objectId, holiday);
      }
    }
  }

}
