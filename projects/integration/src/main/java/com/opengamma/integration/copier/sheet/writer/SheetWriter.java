/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.integration.copier.sheet.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.util.ArgumentChecker;

/**
 * This abstract class represents a sheet writer that, given a map from column names to data, writes out a row containing that data under the matching columns.
 */
public abstract class SheetWriter {

  private String[] _columns; // The column names and order

  /**
   * Creates sheet writer that is specific to the file type.
   *
   * @param filename
   *          the file name, not null or empty
   * @param columns
   *          the columns, not null
   * @return a sheet writer
   */
  public static SheetWriter newSheetWriter(final String filename, final String[] columns) {
    ArgumentChecker.notEmpty(filename, "filename");
    final OutputStream outputStream = openFile(filename);
    return newSheetWriter(SheetFormat.of(filename), outputStream, columns);
  }

  /**
   * Creates a sheet writer.
   *
   * @param sheetFormat
   *          the sheet format, not null
   * @param outputStream
   *          the output stream, not null
   * @param columns
   *          the columns, not null
   * @return a sheet writer
   */
  public static SheetWriter newSheetWriter(final SheetFormat sheetFormat, final OutputStream outputStream, final String[] columns) {
    ArgumentChecker.notNull(sheetFormat, "sheetFormat");
    ArgumentChecker.notNull(outputStream, "outputStream");
    ArgumentChecker.notNull(columns, "columns");

    switch (sheetFormat) {
      case CSV:
        return new CsvSheetWriter(outputStream, columns);
      default:
        throw new OpenGammaRuntimeException("Could not create a writer for the sheet output format " + sheetFormat.toString());
    }
  }

  /**
   * Writes the next row.
   *
   * @param row
   *          the row
   */
  public abstract void writeNextRow(Map<String, String> row);

  /**
   * Flushes the stream.
   */
  public abstract void flush();

  /**
   * Closes the file.
   */
  public abstract void close();

  /**
   * Gets the columns.
   *
   * @return the columns
   */
  protected String[] getColumns() {
    return _columns;
  }

  /**
   * Sets the columns.
   *
   * @param columns
   *          the columns
   */
  protected void setColumns(final String[] columns) {
    _columns = columns;
  }

  /**
   * Opens the file for writing and returns the output stream.
   *
   * @param filename
   *          the file name
   * @return the output stream
   */
  protected static OutputStream openFile(final String filename) {
    // Open input file for writing
    FileOutputStream fileOutputStream;
    try {
      fileOutputStream = new FileOutputStream(filename);
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Could not open file " + filename + " for writing.");
    }
    return fileOutputStream;
  }

}
