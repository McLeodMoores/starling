/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import com.opengamma.util.ArgumentChecker;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * Helper methods for uploading configurations, holidays, etc. from the UI.
 */
public final class UploaderUtils {

  /**
   * Determines whether a file format is one of the acceptable forms (.csv and .xls).
   *
   * @param fileName  the file name, not null
   * @return  the sheet format or throws <code>WebApplicationException</code>
   */
  public static SheetFormat getFormatForFileName(final String fileName) {
    ArgumentChecker.notNull(fileName, "fileName");
    if (fileName.toLowerCase().endsWith("csv")) {
      return SheetFormat.CSV;
    } else if (fileName.toLowerCase().endsWith("xls")) {
      return SheetFormat.XLS;
    }
    final Response response = Response.status(Response.Status.BAD_REQUEST).entity(
        "Upload only supports CSV/XLS files and Excel worksheets").build();
    throw new WebApplicationException(response);
  }

  public static FormDataBodyPart getBodyPart(final FormDataMultiPart formData, final String fieldName) {
    final FormDataBodyPart bodyPart = formData.getField(fieldName);
    if (bodyPart == null) {
      final Response response = Response.status(Response.Status.BAD_REQUEST).entity("Missing form field: " + fieldName).build();
      throw new WebApplicationException(response);
    }
    return bodyPart;
  }

  public static String getString(final FormDataMultiPart formData, final String fieldName) {
    final FormDataBodyPart bodyPart = formData.getField(fieldName);
    final String value = bodyPart.getValue();
    if (StringUtils.isEmpty(value)) {
      final Response response = Response.status(Response.Status.BAD_REQUEST).entity("Missing form value: " + fieldName).build();
      throw new WebApplicationException(response);
    }
    return value;
  }

  /**
   * This wraps the file upload input stream to work around a bug in {@code org.jvnet.mimepull} which is used by Jersey
   * Multipart.  The bug causes the {@code read()} method of the file upload stream to throw an exception if it is
   * called twice at the end of the stream which violates the contract of {@link InputStream}.  It ought to
   * keep returning {@code -1} indefinitely.  This class restores that behaviour.
   *
   * Copied from <code>PortfolioLoaderResource</code>.
   */
  public static class WorkaroundInputStream extends FilterInputStream {

    private boolean _ended;

    /**
     * Constructs the wrapper.
     *
     * @param inStream  the input stream
     */
    public WorkaroundInputStream(final InputStream inStream) {
      super(inStream);
    }

    @Override
    public int read() throws IOException {
      if (_ended) {
        return -1;
      }
      final int i = super.read();
      if (i == -1) {
        _ended = true;
      }
      return i;
    }

    @Override
    public int read(final byte[] b) throws IOException {
      if (_ended) {
        return -1;
      }
      final int i = super.read(b);
      if (i == -1) {
        _ended = true;
      }
      return i;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
      if (_ended) {
        return -1;
      }
      final int i = super.read(b, off, len);
      if (i == -1) {
        _ended = true;
      }
      return i;
    }
  }

  private UploaderUtils() {
  }
}
