/**
 *
 */
package com.mcleodmoores.examples.simulated.loader.holiday;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.money.Currency;

/**
 *
 */
@Scriptable
public class ExamplesCurrencyHolidayLoader extends AbstractTool<ToolContext> {
  /** A list of holiday dates used by all currencies */
  private static final List<LocalDate> HOLIDAYS = new ArrayList<>();

  static {
    final int currentYear = LocalDate.now().getYear();
    for (int i = 0; i < 50; i++) {
      HOLIDAYS.add(LocalDate.of(currentYear + i, 12, 25));
    }
  }

  public static void main(final String[] args) {
    new ExamplesCurrencyHolidayLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() {
    final HolidayMaster holidayMaster = getToolContext().getHolidayMaster();
    final CurrencyPairs ccyConfig = getToolContext().getConfigSource().getSingle(CurrencyPairs.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS, VersionCorrection.LATEST);
    final Set<Currency> stored = new HashSet<>();
    for (final CurrencyPair pair : ccyConfig.getPairs()) {
      Currency ccy = pair.getBase();
      if (!stored.contains(ccy)) {
        final ManageableHoliday calendar = new ManageableHoliday();
        calendar.setType(HolidayType.CURRENCY);
        calendar.setCurrency(ccy);
        calendar.setHolidayDates(HOLIDAYS);
        storeHolidays(calendar, holidayMaster);
        stored.add(ccy);
      }
      ccy = pair.getCounter();
      if (!stored.contains(ccy)) {
        final ManageableHoliday calendar = new ManageableHoliday();
        calendar.setType(HolidayType.CURRENCY);
        calendar.setCurrency(ccy);
        calendar.setHolidayDates(HOLIDAYS);
        storeHolidays(calendar, holidayMaster);
        stored.add(ccy);
      }
    }
  }

  private void storeHolidays(final ManageableHoliday calendar, final HolidayMaster holidayMaster) {
    final HolidaySearchRequest request = new HolidaySearchRequest();
    request.setType(calendar.getType());
    switch (calendar.getType()) {
      case CURRENCY:
        request.setCurrency(calendar.getCurrency());
        break;
      default:
        throw new IllegalStateException("The only supported holiday type is CURRENCY");
    }
    final HolidaySearchResult result = holidayMaster.search(request);
    if (result.getFirstDocument() != null) {
      final HolidayDocument document = result.getFirstDocument();
      document.setHoliday(calendar);
      holidayMaster.update(document);
    } else {
      holidayMaster.add(new HolidayDocument(calendar));
    }
  }
}
