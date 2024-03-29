/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.util.HashMap;
import java.util.Map;

import com.mcleodmoores.date.WorkingDayCalendar;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarNoHoliday;

/**
 * A list of swap generators that can be used in the tests.
 */
public final class GeneratorSwapIborIborMaster {

  /**
   * The method unique instance.
   */
  private static final GeneratorSwapIborIborMaster INSTANCE = new GeneratorSwapIborIborMaster();

  /**
   * Return the unique instance of the class.
   * 
   * @return The instance.
   */
  public static GeneratorSwapIborIborMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of names and the swap generators.
   */
  private final Map<String, GeneratorSwapIborIbor> _generatorSwap;

  /**
   * The list of Ibor indexes for test purposes.
   */
  private final IndexIborMaster _iborIndexMaster;

  /**
   * Private constructor.
   */
  private GeneratorSwapIborIborMaster() {
    _iborIndexMaster = IndexIborMaster.getInstance();
    final Calendar baseCalendar = new CalendarNoHoliday("No Holidays");
    _generatorSwap = new HashMap<>();
    _generatorSwap.put("AUDBBSW3MBBSW6M",
        new GeneratorSwapIborIbor("AUDBBSW3MBBSW6M", _iborIndexMaster.getIndex("AUDBB3M"), _iborIndexMaster.getIndex("AUDBB6M"),
            baseCalendar, baseCalendar));
  }

  public GeneratorSwapIborIbor getGenerator(final String name, final WorkingDayCalendar cal) {
    final GeneratorSwapIborIbor generatorNoCalendar = _generatorSwap.get(name);
    if (generatorNoCalendar == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index for " + name);
    }
    return new GeneratorSwapIborIbor(generatorNoCalendar.getName(),
        _iborIndexMaster.getIndex(generatorNoCalendar.getIborIndex1().getName()), _iborIndexMaster.getIndex(generatorNoCalendar
            .getIborIndex2().getName()),
        cal, cal);
  }

}
