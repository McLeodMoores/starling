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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * This class implements a sheet writer that facilitates writing rows to a csv file. The columns and their order are specified
 * in the constructor. Subsequently, rows to be written can be supplied as a map from column headings to the actual data.
 */
public class CsvSheetWriter extends SheetWriter {
  private static final Logger LOGGER = LoggerFactory.getLogger(CsvSheetWriter.class);

  private final CSVWriter _csvWriter;

  /**
   * @param filename  the file name, not null or empty
   * @param columns  the columns, not null
   */
  public CsvSheetWriter(final String filename, final String[] columns) {

    ArgumentChecker.notEmpty(filename, "filename");
    ArgumentChecker.notNull(columns, "columns");

    // Open file
    final OutputStream fileOutputStream = openFile(filename);

    // Set up CSV Writer
    _csvWriter = new CSVWriter(new OutputStreamWriter(fileOutputStream));

    // Set columns
    setColumns(columns);

    // Write the column row
    _csvWriter.writeNext(columns);
    flush();
  }

  /**
   * @param outputStream  the output stream, not null
   * @param columns  the columns, not null
   */
  public CsvSheetWriter(final OutputStream outputStream, final String[] columns) {

    ArgumentChecker.notNull(outputStream, "outputStream");
    ArgumentChecker.notNull(columns, "columns");

    // Set up CSV Writer
    _csvWriter = new CSVWriter(new OutputStreamWriter(outputStream));

    // Set columns
    setColumns(columns);

    // Write the column row
    _csvWriter.writeNext(columns);
  }

  @Override
  public void writeNextRow(final Map<String, String> row) {

    ArgumentChecker.notNull(row, "row");

    final String[] rawRow = new String[getColumns().length];

    for (int i = 0; i < getColumns().length; i++) {
      if ((rawRow[i] = row.get(getColumns()[i])) == null) { //CSIGNORE
        LOGGER.info("Missing data for column '" + getColumns()[i] + "' when writing row to CSV file");
        rawRow[i] = "";
      }
    }

    _csvWriter.writeNext(rawRow);
  }

  @Override
  public void flush() {
    try {
      _csvWriter.flush();
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Could not flush to CSV file");
    }
  }

  @Override
  public void close() {
    try {
      _csvWriter.close();
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Could not close CSV file");
    }
  }

}
