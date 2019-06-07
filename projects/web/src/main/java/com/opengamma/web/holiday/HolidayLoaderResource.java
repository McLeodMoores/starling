/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.holiday;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.ManageableHolidayWithWeekend;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.web.SheetFormat;
import com.opengamma.web.UploaderUtils;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import au.com.bytecode.opencsv.CSVReader;

/**
 * REST resource that uploads a CSV file containing a list of dates that define the holidays for a holiday type (currency / bank / settlement / trading).
 */
@Path("holidayupload")
public class HolidayLoaderResource {

  /**
   * Sets the allowable date formats for holiday data.
   */
  public enum DateFormat {

    /**
     * ISO date formats (yyyy-MM-dd or yyyyMMdd).
     */
    ISO(DateTimeFormatter.ISO_DATE, DateTimeFormatter.BASIC_ISO_DATE),
    /**
     * UK date formats (dd/MM/yyyy or dd-MM-yyyy).
     */
    UK(DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ofPattern("dd-MM-yyyy")),
    /**
     * US date formats (MM/dd/yyyy or MM-dd-yyyy).
     */
    US(DateTimeFormatter.ofPattern("MM/dd/yyyy"), DateTimeFormatter.ofPattern("MM-dd-yyyy"));

    private final DateTimeFormatter _primaryFormatter;
    private final DateTimeFormatter _secondaryFormatter;

    DateFormat(final DateTimeFormatter primaryFormatter, final DateTimeFormatter secondaryFormatter) {
      _primaryFormatter = primaryFormatter;
      _secondaryFormatter = secondaryFormatter;
    }

    /**
     * Parses a date string by trying the primary formatter, then the secondary.
     *
     * @param dateString
     *          the date string, not null
     * @return the parsed date or throws <code>DateTimeParseException</code>
     */
    public LocalDate parse(final String dateString) {
      try {
        return LocalDate.parse(dateString, _primaryFormatter);
      } catch (final DateTimeParseException e) {
        return LocalDate.parse(dateString, _secondaryFormatter);
      }
    }
  }

  private final HolidayMaster _holidayMaster;

  /**
   * Creates an instance.
   *
   * @param holidayMaster
   *          the master, not null
   */
  public HolidayLoaderResource(final HolidayMaster holidayMaster) {
    _holidayMaster = ArgumentChecker.notNull(holidayMaster, "holidayMaster");
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_PLAIN)
  public Response uploadHoliday(final FormDataMultiPart formData) throws IOException {
    final FormDataBodyPart fileBodyPart = UploaderUtils.getBodyPart(formData, "file");
    final Object fileEntity = fileBodyPart.getEntity();
    if (fileEntity == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    final String fileName = fileBodyPart.getFormDataContentDisposition().getFileName();
    final HolidayType holidayType = Enum.valueOf(HolidayType.class, UploaderUtils.getString(formData, "holidayType"));
    final WeekendType weekendType = Enum.valueOf(WeekendType.class, UploaderUtils.getString(formData, "weekendType"));
    final ExternalId id = ExternalId.parse(UploaderUtils.getString(formData, "id"));
    final DateFormat dateFormatter = Enum.valueOf(DateFormat.class, UploaderUtils.getString(formData, "dateFormat").toUpperCase());
    final SheetFormat format = UploaderUtils.getFormatForFileName(fileName);
    final InputStream fileStream = new UploaderUtils.WorkaroundInputStream(((BodyPartEntity) fileEntity).getInputStream());
    final List<LocalDate> dateList = new ArrayList<>();
    final StreamingOutput streamingOutput = new StreamingOutput() {

      @Override
      public void write(final OutputStream output) throws IOException, WebApplicationException {
        switch (format) {
          case CSV:
            final CSVReader csvReader = new CSVReader(new InputStreamReader(fileStream));
            try {
              String[] row = csvReader.readNext();
              while (row != null) {
                for (int i = 0; i < row.length; i++) {
                  final String element = row[i];
                  // be forgiving about entries
                  if (!element.isEmpty()) {
                    try {
                      dateList.add(dateFormatter.parse(element));
                    } catch (final DateTimeParseException e) {
                      output.write(("Malformed date entry on line " + (i + 1) + ": " + element + "\n").getBytes());
                    }
                  }
                }
                row = csvReader.readNext();
              }
            } catch (final IOException e) {
              output.write(("Error reading CSV file data row: " + e.getMessage() + "\n").getBytes());
            }
            break;
          default:
            output.write("Unsupported file format type\n".getBytes());
            break;
        }
        if (dateList.isEmpty()) {
          output.write("No dates could be found\n".getBytes());
        } else {
          try {
            storeHoliday(holidayType, weekendType, id, dateList);
          } catch (final Exception e) {
            output.write(("Problem writing holiday to database: " + e.getMessage() + "\n").getBytes());
          }
        }
        output.write("Upload complete".getBytes());
      }

    };
    return Response.ok(streamingOutput).build();
  }

  void storeHoliday(final HolidayType holidayType, final WeekendType weekendType, final ExternalId id, final List<LocalDate> dates) {
    final HolidaySearchRequest searchRequest = new HolidaySearchRequest();
    searchRequest.setName(id.getValue());
    searchRequest.setType(holidayType);
    final HolidaySearchResult searchResult = _holidayMaster.search(searchRequest);
    final ManageableHolidayWithWeekend holiday = new ManageableHolidayWithWeekend();
    holiday.setType(holidayType);
    holiday.setHolidayDates(dates);
    holiday.setWeekendType(weekendType);
    switch (holidayType) {
      case CURRENCY:
        holiday.setCurrency(Currency.parse(id.getValue()));
        break;
      case CUSTOM:
        holiday.setCustomExternalId(id);
        break;
      case BANK:
        holiday.setRegionExternalId(id);
        break;
      case TRADING:
      case SETTLEMENT:
        holiday.setExchangeExternalId(id);
        break;
      default:
        throw new IllegalArgumentException("Unknown holiday type " + holidayType);
    }
    if (searchResult.getDocuments().isEmpty()) {
      // add new document
      final HolidayDocument document = new HolidayDocument(holiday);
      _holidayMaster.add(document);
    } else {
      // update existing document
      final HolidayDocument document = searchResult.getFirstDocument();
      document.setHoliday(holiday);
      _holidayMaster.update(document);
    }
  }
}
