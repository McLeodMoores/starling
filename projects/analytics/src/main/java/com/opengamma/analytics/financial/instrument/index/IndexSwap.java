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
import org.threeten.bp.Period;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Class describing a swap index.
 * @deprecated  use (@link SwapIndex}
 */
@Deprecated
public class IndexSwap {
  /** The delegated index */
  private final SwapIndex _index;
  /** The swap generator associated to the swap index. */
  private final GeneratorSwapFixedIbor _swapGenerator;

  /**
   * Constructor from all the Swap index details.
   * @param fixedLegPeriod The swap fixed leg payment period.
   * @param fixedLegDayCount The swap fixed leg day count convention.
   * @param iborIndex The Ibor index of swap the floating leg.
   * @param tenor The swap tenor.
   * @param calendar The holiday calendar for the ibor index.
   */
  public IndexSwap(final Period fixedLegPeriod, final DayCount fixedLegDayCount, final IborIndex iborIndex, final Period tenor, final Calendar calendar) {
    ArgumentChecker.notNull(tenor, "swapTenor");
    _swapGenerator = new GeneratorSwapFixedIbor("Swap Generator", fixedLegPeriod, fixedLegDayCount, iborIndex, calendar);
    _index = new SwapIndex(fixedLegPeriod.toString() + " " + iborIndex.getCurrency() + " Swap Index", iborIndex.getCurrency(), Tenor.of(fixedLegPeriod),
        fixedLegDayCount, iborIndex.toIborTypeIndex(), Tenor.of(tenor));
  }

  /**
   * Constructor from a swap generator and the swap tenor.
   * @param swapGenerator The underlying swap generator.
   * @param tenor The swap tenor.
   */
  public IndexSwap(final GeneratorSwapFixedIbor swapGenerator, final Period tenor) {
    ArgumentChecker.notNull(swapGenerator, "swapGenerator");
    ArgumentChecker.notNull(tenor, "tenor");
    _swapGenerator = swapGenerator;
    _index = new SwapIndex(swapGenerator.getFixedLegPeriod() + " " + swapGenerator.getIborIndex().getCurrency() + " Swap Index", swapGenerator.getCurrency(),
        Tenor.of(swapGenerator.getFixedLegPeriod()), swapGenerator.getFixedLegDayCount(), swapGenerator.getIborIndex().toIborTypeIndex(), Tenor.of(tenor));
  }

  /**
   * Gets the index name.
   * @return The name
   */
  public String getName() {
    return _index.getName();
  }

  /**
   * Gets the swap fixed leg payment period.
   * @return The swap fixed leg payment period.
   */
  public Period getFixedLegPeriod() {
    return _index.getFixedLegPaymentTenor().getPeriod();
  }

  /**
   * Gets the swap fixed leg day count convention.
   * @return The swap fixed leg day count convention.
   */
  public DayCount getFixedLegDayCount() {
    return _index.getFixedLegDayCount();
  }

  /**
   * Gets the Ibor index of the swap floating leg.
   * @return The Ibor index of the swap floating leg.
   */
  public IborIndex getIborIndex() {
    //TODO think about this
    return _swapGenerator.getIborIndex();
  }

  /**
   * Gets the tenor of the swap index.
   * @return The tenor.
   */
  public Period getTenor() {
    return _index.getSwapTenor().getPeriod();
  }

  /**
   * Gets the index currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _index.getIborIndex().getCurrency();
  }

  @Override
  public String toString() {
    return _index.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getName().hashCode();
    result = prime * result + _swapGenerator.hashCode();
    result = prime * result + getTenor().hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final IndexSwap other = (IndexSwap) obj;
    if (!ObjectUtils.equals(_swapGenerator, other._swapGenerator)) {
      return false;
    }
    if (!ObjectUtils.equals(getTenor(), other.getTenor())) {
      return false;
    }
    return true;
  }

}
