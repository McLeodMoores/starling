/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 */
public class UploaderHelper {

  public static SheetFormat getFormatForFileName(final String fileName) {
    if (fileName.toLowerCase().endsWith("csv")) {
      return SheetFormat.CSV;
    } else if (fileName.toLowerCase().endsWith("xls")) {
      return SheetFormat.XLS;
    }
    final Response response = Response.status(Response.Status.BAD_REQUEST).entity(
        "Holiday upload only supports CSV/XLS files and Excel worksheets").build();
    throw new WebApplicationException(response);
  }
}
