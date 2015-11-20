/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.holiday;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSortedMap;
import com.opengamma.core.holiday.HolidayType;

/**
 * Provides all supported holiday types.
 */
public final class HolidayTypesProvider {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(HolidayTypesProvider.class);
  /** An instance */
  private static final HolidayTypesProvider INSTANCE = new HolidayTypesProvider();
  /** A map of holiday types */
  private final ImmutableSortedMap<String, String> _holidayDescriptionMap;

  /**
   * Gets an instance.
   * @return  the instance
   */
  public static HolidayTypesProvider getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private HolidayTypesProvider() {
    final ImmutableSortedMap.Builder<String, String> descriptions = ImmutableSortedMap.naturalOrder();
    descriptions.put(HolidayType.BANK.name(), HolidayType.BANK.name());
    descriptions.put(HolidayType.CURRENCY.name(), HolidayType.CURRENCY.name());
    descriptions.put(HolidayType.CUSTOM.name(), HolidayType.CUSTOM.name());
    descriptions.put(HolidayType.SETTLEMENT.name(), HolidayType.SETTLEMENT.name());
    descriptions.put(HolidayType.TRADING.name(), HolidayType.TRADING.name());
    _holidayDescriptionMap = descriptions.build();
  }

  public Map<String, String> getDescriptionMap() {
    return _holidayDescriptionMap;
  }

  public String getDescription(final String type) {
    final String description = _holidayDescriptionMap.get(type);
    return description != null ? description : type;
  }
}
