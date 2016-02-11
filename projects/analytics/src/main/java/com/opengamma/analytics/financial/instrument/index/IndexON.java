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

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;

/**
 * Class describing an OIS-like index. The fixing period is always one business day.
 */
public class IndexON extends IndexDeposit {
  /** The delegated index */
  private final OvernightIndex _index;

  /**
   * Index constructor from all the details.
   * @param name The name of the index. Not null.
   * @param currency The index currency. Not null.
   * @param dayCount The day count convention associated to the overnight rate. Not null.
   * @param publicationLag The number of days between start of the fixing period and the publication of the index value.
   */
  public IndexON(final String name, final Currency currency, final DayCount dayCount, final int publicationLag) {
    super(name, currency);
    _index = new OvernightIndex(name, currency, dayCount, publicationLag);
  }

  /**
   * Gets this index as an {@link OvernightIndex}.
   * @return  an overnight index
   */
  public OvernightIndex toOvernightIndex() {
    return new OvernightIndex(getName(), getCurrency(), getDayCount(), getPublicationLag());
  }

  /**
   * Gets the day count convention associated to the overnight rate.
   * @return The day count convention.
   */
  public DayCount getDayCount() {
    return _index.getDayCount();
  }

  /**
   * Gets the number of days between start of the fixing period and the publication of the index value.
   * @return The number of lag days.
   */
  public int getPublicationLag() {
    return _index.getPublicationLag();
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
    if (!(obj instanceof IndexON)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    final IndexON other = (IndexON) obj;
    if (!ObjectUtils.equals(_index.getDayCount(), other.getDayCount())) {
      return false;
    }
    if (_index.getPublicationLag() != other.getPublicationLag()) {
      return false;
    }
    return true;
  }

}
