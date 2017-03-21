/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.Objects;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * A class describing an IBOR-like index.
 */
public class IborTypeIndex extends Index {
  /** The index spot lag in days between trade and settlement date */
  private final int _spotLag;
  /** The day count convention associated with the index */
  private final DayCount _dayCount;
  /** The business day convention associated with the index */
  private final BusinessDayConvention _businessDayConvention;
  /** True the end-of-month rule is used */
  private final boolean _endOfMonth;
  /** The index tenor */
  private final Tenor _tenor;
  /** This object is used as a key within the curve system, thus {@link #hashCode()} needs to be fast */
  private final int _hashCode;

  /**
   * Constructs an ibor index.
   * @param currency  the index currency, not null
   * @param tenor  the index tenor, not null
   * @param spotLag  the index spot lag in days
   * @param dayCount  the day count convention associated with the index, not null
   * @param businessDayConvention  the business day convention associated with the index, not null
   * @param endOfMonth  the end-of-month flag
   * @param name  the index name, not null
   */
  public IborTypeIndex(final String name, final Currency currency, final Tenor tenor, final int spotLag, final DayCount dayCount,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth) {
    super(name, currency);
    _tenor = ArgumentChecker.notNull(tenor, "tenor");
    _dayCount = ArgumentChecker.notNull(dayCount, "dayCount");
    _businessDayConvention = ArgumentChecker.notNull(businessDayConvention, "businessDayConvention");
    _spotLag = spotLag;
    _endOfMonth = endOfMonth;
    _hashCode = generateHashCode();
  }

  /**
   * Gets the index tenor.
   * @return  the index tenor
   */
  public Tenor getTenor() {
    return _tenor;
  }

  /**
   * Gets the spot lag (in days).
   * @return  the spot lag
   */
  public int getSpotLag() {
    return _spotLag;
  }

  /**
   * Gets the day count.
   * @return  the day count
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Gets the business day convention.
   * @return  the business day convention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Returns true if the end-of-month rule is used.
   * @return  true if the end-of-month rule is used
   */
  public boolean isEndOfMonth() {
    return _endOfMonth;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("IborIndex[");
    sb.append(getName());
    sb.append(", currency=");
    sb.append(getCurrency().getCode());
    sb.append(", tenor=");
    sb.append(_tenor.toFormattedString());
    sb.append(", day count=");
    sb.append(_dayCount.getName());
    sb.append(", business day convention=");
    sb.append(_businessDayConvention.getName());
    sb.append(", spot lag=");
    sb.append(_spotLag);
    sb.append(_endOfMonth ? ", end-of-month" : "");
    sb.append("]");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }

  /**
   * Generates the hash code, which is stored internally.
   * @return  the hash code
   */
  private int generateHashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getCurrency().hashCode();
    result = prime * result + getName().hashCode();
    result = prime * result + _businessDayConvention.hashCode();
    result = prime * result + _dayCount.hashCode();
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + _spotLag;
    result = prime * result + _tenor.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IborTypeIndex)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    final IborTypeIndex other = (IborTypeIndex) obj;
    if (!Objects.equals(_tenor, other._tenor)) {
      return false;
    }
    if (_endOfMonth != other._endOfMonth) {
      return false;
    }
    if (_spotLag != other._spotLag) {
      return false;
    }
    if (!Objects.equals(_businessDayConvention, other._businessDayConvention)) {
      return false;
    }
    if (!Objects.equals(_dayCount, other._dayCount)) {
      return false;
    }
    return true;
  }

}
