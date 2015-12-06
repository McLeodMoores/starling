/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.Objects;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A class describing an overnight index, for example the reference for an overnight-indexed swap. By definition, the
 * fixing period for this index is one day.
 */
public class OvernightIndex extends Index {
  /** The day count convention associated with the overnight rate */
  private final DayCount _dayCount;
  /** The number of days between start of the fixing period and the publication of the index value */
  private final int _publicationLag;
  /** This object is used as a key within the curve system, so the hash code should be pre-calculated */
  private final int _hashCode;

  /**
   * Constructs an overnight index.
   * @param name  the name of the index, not null
   * @param currency  the index currency, not null
   * @param dayCount  the day count convention associated with the overnight rate, not null
   * @param publicationLag  the number of days between start of the fixing period and the publication of the index value, must be 0 or 1
   */
  public OvernightIndex(final String name, final Currency currency, final DayCount dayCount, final int publicationLag) {
    super(name, currency);
    ArgumentChecker.isTrue(publicationLag == 0 || publicationLag == 1, "Publication lag for an OvernightIndex must be 0 or 1; have {}", publicationLag);
    _publicationLag = publicationLag;
    _dayCount = ArgumentChecker.notNull(dayCount, "dayCount");
    _hashCode = generateHashCode();
  }

  /**
   * Gets the day count convention associated with the overnight rate.
   * @return  the day count convention.
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Gets the number of days between start of the fixing period and the publication of the index value.
   * @return The number of lag days.
   */
  public int getPublicationLag() {
    return _publicationLag;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("OvernightIndex[");
    sb.append(getName());
    sb.append(", currency=");
    sb.append(getCurrency().getCode());
    sb.append(", day count=");
    sb.append(_dayCount.getName());
    sb.append(", publication lag=");
    sb.append(_publicationLag);
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
    result = prime * result + _dayCount.hashCode();
    result = prime * result + _publicationLag;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof OvernightIndex)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    final OvernightIndex other = (OvernightIndex) obj;
    if (_publicationLag != other._publicationLag) {
      return false;
    }
    if (!Objects.equals(_dayCount, other._dayCount)) {
      return false;
    }
    return true;
  }
}