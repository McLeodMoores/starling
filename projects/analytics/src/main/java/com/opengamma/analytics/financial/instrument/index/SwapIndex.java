/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.Objects;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * A class describing a swap index, for example the reference for a constant-maturity swap.
 */
public class SwapIndex extends Index {
  /** The fixed leg payment tenor */
  private final Tenor _fixedLegPaymentTenor;
  /** The fixed leg day count */
  private final DayCount _fixedLegDayCount;
  /** The underlying ibor index */
  private final IborTypeIndex _iborIndex;
  /** The tenor of the swap index */
  private final Tenor _tenor;
  /** The hash code */
  private final int _hashCode;

  /**
   * Constructs a swap index.
   * @param name  the index name, not null
   * @param currency  the index currency, not null
   * @param fixedLegPaymentTenor  the fixed swap leg payment tenor, not null
   * @param fixedLegDayCount  the fixed swap leg day count, not null
   * @param iborIndex  the ibor index, not null
   * @param tenor  the swap tenor, not null
   */
  public SwapIndex(final String name, final Currency currency, final Tenor fixedLegPaymentTenor, final DayCount fixedLegDayCount, final IborTypeIndex iborIndex, final Tenor tenor) {
    super(name, currency);
    // TODO swap generator
    _fixedLegPaymentTenor = ArgumentChecker.notNull(fixedLegPaymentTenor, "fixedLegPaymentTenor");
    _fixedLegDayCount = ArgumentChecker.notNull(fixedLegDayCount, "fixedLegDayCount");
    _iborIndex = ArgumentChecker.notNull(iborIndex, "iborIndex");
    _tenor = ArgumentChecker.notNull(tenor, "tenor");
    _hashCode = generateHashCode();
  }

  /**
   * Gets the fixed swap leg payment tenor.
   * @return  the fixed swap leg payment tenor
   */
  public Tenor getFixedLegPaymentTenor() {
    return _fixedLegPaymentTenor;
  }

  /**
   * Gets the fixed swap leg day count.
   * @return  the fixed swap leg day count
   */
  public DayCount getFixedLegDayCount() {
    return _fixedLegDayCount;
  }

  /**
   * Gets the ibor index.
   * @return  the ibor index
   */
  public IborTypeIndex getIborIndex() {
    return _iborIndex;
  }

  /**
   * Gets the swap tenor.
   * @return  the swap tenor
   */
  public Tenor getSwapTenor() {
    return _tenor;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("SwapIndex[");
    sb.append(getName());
    sb.append(", currency=");
    sb.append(getCurrency().getCode());
    sb.append(", swap tenor=");
    sb.append(_tenor.toFormattedString());
    sb.append(", ibor index=");
    sb.append(_iborIndex.toString());
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
    result = prime * result + _tenor.hashCode();
    result = prime * result + _fixedLegPaymentTenor.hashCode();
    result = prime * result + _fixedLegDayCount.hashCode();
    result = prime * result + _iborIndex.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SwapIndex)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    final SwapIndex other = (SwapIndex) obj;
    if (!Objects.equals(_tenor, other._tenor)) {
      return false;
    }
    if (!Objects.equals(_fixedLegPaymentTenor, other._fixedLegPaymentTenor)) {
      return false;
    }
    if (!Objects.equals(_fixedLegDayCount, other._fixedLegDayCount)) {
      return false;
    }
    if (!Objects.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    return true;
  }
}
