/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.holiday;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.scripts.Scriptable;

/**
 * Populates the holiday master with settlement calendars.
 */
@Scriptable
public class ExamplesBankHolidayLoader extends AbstractTool<ToolContext> {
  /** A list of holiday dates used by all currencies */
  private static final List<LocalDate> HOLIDAYS = new ArrayList<>();

  static {
    final int currentYear = LocalDate.now().getYear();
    for (int i = 0; i < 50; i++) {
      HOLIDAYS.add(LocalDate.of(currentYear + i, 12, 25));
    }
  }

  /**
   * @param args
   *          the arguments
   */
  public static void main(final String[] args) {
    new ExamplesBankHolidayLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() {
    final HolidayMaster holidayMaster = getToolContext().getHolidayMaster();
    final RegionMaster regionMaster = getToolContext().getRegionMaster();
    final RegionSearchRequest searchRequest = new RegionSearchRequest();
    searchRequest.setName("*");
    searchRequest.setClassification(RegionClassification.INDEPENDENT_STATE);
    final RegionSearchResult searchResult = regionMaster.search(searchRequest);
    for (final RegionDocument doc : searchResult.getDocuments()) {
      final Region region = doc.getRegion();
      final ManageableHoliday calendar = new ManageableHoliday();
      calendar.setType(HolidayType.BANK);
      calendar.setRegionExternalId(ExternalSchemes.financialRegionId(region.getCountry().getCode()));
      calendar.setHolidayDates(HOLIDAYS);
      storeHolidays(calendar, holidayMaster);
    }
  }

  private void storeHolidays(final ManageableHoliday calendar, final HolidayMaster holidayMaster) {
    final HolidaySearchRequest request = new HolidaySearchRequest();
    request.setType(calendar.getType());
    switch (calendar.getType()) {
      case BANK:
        request.addRegionExternalIds(calendar.getRegionExternalId());
        break;
      default:
        throw new IllegalStateException("The only supported holiday type is BANK");
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
