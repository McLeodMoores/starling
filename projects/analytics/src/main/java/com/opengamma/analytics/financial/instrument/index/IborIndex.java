/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.Objects;

import org.threeten.bp.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Class describing an ibor-like index.
 * @deprecated  use {@link IborTypeIndex}
 */
@Deprecated
public class IborIndex extends IndexDeposit {
  /** The delegated index */
  private final IborTypeIndex _index;

  /**
   * Constructor from the index details. The name is set to "Ibor".
   * @param currency  the index currency, not null
   * @param tenor  the index tenor, not null
   * @param spotLag  the index spot lag (usually 2 or 0)
   * @param dayCount  the day count convention associated with the index, not null
   * @param businessDayConvention  the business day convention associated with the index, not null
   * @param endOfMonth  the end-of-month flag
   * @deprecated Use the constructor that takes an index name.
   */
  @Deprecated
  public IborIndex(final Currency currency, final Period tenor, final int spotLag, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final boolean endOfMonth) {
    this(currency, tenor, spotLag, dayCount, businessDayConvention, endOfMonth, "Ibor");
  }

  /**
   * Constructor from the index details.
   * @param currency  the index currency, not null
   * @param tenor  the index tenor, not null
   * @param spotLag  the index spot lag (usually 2 or 0)
   * @param dayCount  the day count convention associated with the index, not null
   * @param businessDayConvention  the business day convention associated with the index, not null
   * @param endOfMonth  the end-of-month flag
   * @param name  the index name, not null
   */
  public IborIndex(final Currency currency, final Period tenor, final int spotLag, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final boolean endOfMonth, final String name) {
    super(name, currency);
    _index = new IborTypeIndex(name, currency, Tenor.of(tenor), spotLag, dayCount, businessDayConvention, endOfMonth);
  }

  /**
   * Gets this index as an {@link IborTypeIndex}.
   * @return  an ibor index
   */
  public IborTypeIndex toIborTypeIndex() {
    return _index;
  }

  /**
   * Gets the index tenor.
   * @return  the index tenor
   */
  public Period getTenor() {
    return _index.getTenor().getPeriod();
  }

  /**
   * Gets the spot lag (in days).
   * @return  the spot lag
   */
  public int getSpotLag() {
    return _index.getSpotLag();
  }

  /**
   * Gets the day count.
   * @return  the day count
   */
  public DayCount getDayCount() {
    return _index.getDayCount();
  }

  /**
   * Gets the business day convention.
   * @return  the business day convention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _index.getBusinessDayConvention();
  }

  /**
   * Returns true if the end-of-month rule is used.
   * @return  true if the end-of-month rule is used
   */
  public boolean isEndOfMonth() {
    return _index.isEndOfMonth();
  }

  @Override
  public String toString() {
    return _index.toString();
  }

  @Override
  public int hashCode() {
    return _index.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IborIndex)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    final IborIndex other = (IborIndex) obj;
    if (!Objects.equals(_index.getTenor().getPeriod(), other.getTenor())) {
      return false;
    }
    if (_index.isEndOfMonth() != other.isEndOfMonth()) {
      return false;
    }
    if (_index.getSpotLag() != other.getSpotLag()) {
      return false;
    }
    if (!Objects.equals(_index.getBusinessDayConvention(), other.getBusinessDayConvention())) {
      return false;
    }
    if (!Objects.equals(_index.getDayCount(), other.getDayCount())) {
      return false;
    }
    return true;
  }

}
