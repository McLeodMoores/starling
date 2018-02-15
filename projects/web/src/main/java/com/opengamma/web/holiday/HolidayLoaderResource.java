/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.holiday;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * REST resource that uploads a CSV file containing a list of dates that define the holidays
 * for a holiday type (currency / bank / settlement / trading).
 */
@Path("holidayupload")
public class HolidayLoaderResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(HolidayLoaderResource.class);
  private final HolidayMaster _holidayMaster;

  /**
   * Creates an instance.
   *
   * @param holidayMaster  the master, not null
   */
  public HolidayLoaderResource(final HolidayMaster holidayMaster) {
    _holidayMaster = ArgumentChecker.notNull(holidayMaster, "holidayMaster");
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_PLAIN)
  public Response uploadHoliday(final FormDataMultiPart formData) throws IOException {
    return Response.ok("OIOIOIHOI").build();
  }
}
